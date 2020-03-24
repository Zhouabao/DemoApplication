package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.RefreshSquareEvent
import com.sdy.jitangapplication.event.UserCenterEvent
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.presenter.SquarePlayDetaiPresenter
import com.sdy.jitangapplication.presenter.view.SquarePlayDetailView
import com.sdy.jitangapplication.switchplay.SwitchUtil
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.ui.dialog.MoreActionNewDialog
import com.sdy.jitangapplication.ui.dialog.TranspondDialog
import com.sdy.jitangapplication.ui.fragment.MyCollectionAndLikeFragment
import com.sdy.jitangapplication.utils.UserManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_square_play_detail.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.dialog_more_action_new.*
import kotlinx.android.synthetic.main.item_square_detail_play_cover.*
import org.greenrobot.eventbus.EventBus

/**
 * 点击图片、视频、录音进入详情页面，并且支持点击左右切换好友动态
 */
class SquarePlayDetailActivity : BaseMvpActivity<SquarePlayDetaiPresenter>(), SquarePlayDetailView,
    View.OnClickListener {

    private val TAG = SquarePlayDetailActivity::class.java.simpleName
    override fun onGetSquareInfoResults(squareBean: SquareBean?) {
        if (squareBean != null) {
            squareBean.type = when {
                !squareBean.video_json.isNullOrEmpty() -> SquareBean.VIDEO
                !squareBean.audio_json.isNullOrEmpty() -> SquareBean.AUDIO
                !squareBean.photo_json.isNullOrEmpty() ||
                        (squareBean.photo_json.isNullOrEmpty() && squareBean.audio_json.isNullOrEmpty() && squareBean.video_json.isNullOrEmpty()) -> SquareBean.PIC
                else -> SquareBean.PIC
            }

            this.squareBean = squareBean
            initData()
        }
    }

    private lateinit var squareBean: SquareBean

    companion object {
        public val OPTION_VIEW = "VIEW"
        public val REQUEST_CODE = 1002
        fun startActivity(
            activity: Activity,
            transactionView: View? = null,
            data: SquareBean? = null,
            position: Int? = -1,
            type: Int = MyCollectionAndLikeFragment.TYPE_SQUARE,
            id: Int? = -1,
            fromRecommend: Boolean = false
        ) {
            val intent = Intent(activity, SquarePlayDetailActivity::class.java)
            if (data != null)
                intent.putExtra("squareBean", data)
            if (id != null && id != -1)
                intent.putExtra("id", id)
            if (position != null && position != -1)
                intent.putExtra("position", position)
            intent.putExtra("fromRecommend", fromRecommend)
            intent.putExtra("type", type)
            //这里指定了共享的视图元素
            if (transactionView != null) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transactionView, OPTION_VIEW)
                ActivityCompat.startActivityForResult(activity, intent, REQUEST_CODE, options.toBundle())
            } else {
                activity.startActivityForResult(intent, REQUEST_CODE)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_square_play_detail)
        initView()
        if (intent.getSerializableExtra("squareBean") != null) {
            squareBean = intent.getSerializableExtra("squareBean") as SquareBean
            initData()
        } else {
            mPresenter.getSquareInfo(
                hashMapOf(
                    "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                    "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                    "square_id" to intent.getIntExtra("id", -1)
                )
            )
        }

    }


    private fun initView() {
        ScreenUtils.setFullScreen(this)
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT)
        mPresenter = SquarePlayDetaiPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        btnBack.onClick {
            onBackPressed()
        }
//        btnBack.visibility = View.GONE
        detailPlayComment.setTextColor(resources.getColor(R.color.colorWhite))
        //头像点击
        detailPlayUserAvatar.setOnClickListener(this)
        //评论
        detailPlayComment.setOnClickListener(this)
        //更多操作
        detailPlayMoreActions.setOnClickListener(this)
        //点赞
        detailPlaydianzan.setOnClickListener(this)
        //点击内容跳转到评论详情页面
        detailPlayContent.setOnClickListener(this)
        detailPlayCommentBtn.setOnClickListener(this)
        //發送評論
        detailPlayCommentSend.setOnClickListener(this)

    }

    private fun initData() {
        //增加封面
        val imageview = ImageView(this)
        imageview.scaleType = ImageView.ScaleType.CENTER_INSIDE
        GlideUtil.loadImg(this, squareBean.cover_url ?: "", imageview)
        if (imageview.parent != null) {
            val vg = imageview.parent as ViewGroup
            vg.removeView(imageview)
        }
        detailPlayVideo.thumbImageView = imageview

        SwitchUtil.optionPlayer(
            detailPlayVideo,
            squareBean.video_json?.get(0)?.url ?: "",
            true
        )
        if (!intent.getBooleanExtra("fromRecommend", false)) {
            SwitchUtil.clonePlayState(detailPlayVideo)
            // 这里指定了被共享的视图元素
            ViewCompat.setTransitionName(videoFl, OPTION_VIEW)

        }
        GSYVideoManager.instance().isNeedMute = false
        detailPlayVideo.setVideoAllCallBack(object : GSYSampleCallBack() {
            override fun onClickBlank(url: String?, vararg objects: Any?) {
                super.onClickBlank(url, *objects)
                if (videoCover.visibility == View.VISIBLE) {
                    videoCover.visibility = View.GONE
                    btnBack.visibility = View.GONE
                } else {
                    videoCover.visibility = View.VISIBLE
                    btnBack.visibility = View.VISIBLE
                }
            }
        })
//        setSurfaceToPlay()
        detailPlayVideo.startPlayLogic()

        GlideUtil.loadAvatorImg(this, squareBean.avatar, detailPlayUserAvatar)
        detailPlayUserLocationAndTime.text = "${squareBean!!.puber_address}" +
                "${if (!squareBean!!.puber_address.isNullOrEmpty()) {
                    "·"
                } else {
                    ""
                }}${squareBean!!.out_time}"

        detailPlayUserName.text = squareBean.nickname ?: ""
        detailPlayContent.text = squareBean.descr ?: ""

        val drawable1 =
            resources.getDrawable(if (squareBean.isliked == 1) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan_white)
        drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
        detailPlaydianzan.setCompoundDrawables(drawable1, null, null, null)
        detailPlaydianzan.text =
            "${if (squareBean!!.like_cnt < 0) {
                0
            } else {
                squareBean!!.like_cnt
            }}"
        detailPlayUserVipIv.isVisible = squareBean.isvip == 1

    }


    lateinit var moreActionDialog: MoreActionNewDialog
    /**
     * 展示更多操作对话框
     */
    private fun showMoreDialog(position: Int) {
        moreActionDialog = MoreActionNewDialog(this, squareBean)
        moreActionDialog.show()

        if (squareBean!!.iscollected == 0) {
            moreActionDialog.collect.text = "收藏"
            val top = resources.getDrawable(R.drawable.icon_collect1)
            moreActionDialog.collect.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
        } else {
            moreActionDialog.collect.text = "取消收藏"
            val top = resources.getDrawable(R.drawable.icon_collected1)
            moreActionDialog.collect.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
        }
        if (squareBean!!.accid == UserManager.getAccid()) {
            moreActionDialog.delete.visibility = View.VISIBLE
            moreActionDialog.report.visibility = View.GONE
            moreActionDialog.collect.visibility = View.GONE
        } else {
            moreActionDialog.delete.visibility = View.GONE
            moreActionDialog.report.visibility = View.VISIBLE
            moreActionDialog.collect.visibility = View.VISIBLE
        }
        moreActionDialog.delete.onClick {
            val params = hashMapOf(
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                "square_id" to squareBean!!.id!!
            )
            mPresenter.removeMySquare(params, position)
            moreActionDialog.dismiss()

        }


        moreActionDialog.collect.onClick {
            //发起收藏请求
            val params = hashMapOf(
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                "type" to if (squareBean!!.iscollected == 0) {
                    1
                } else {
                    2
                },
                "square_id" to squareBean!!.id!!,
                "_timestamp" to System.currentTimeMillis()
            )
            mPresenter.getSquareCollect(params, position)
            moreActionDialog.dismiss()
        }
        moreActionDialog.report.onClick {
            val dialog = DeleteDialog(this)
            dialog.show()
            dialog.tip.text = getString(R.string.report_square)
            dialog.title.text = "动态举报"
            dialog.confirm.text = "举报"
            dialog.cancel.onClick { dialog.dismiss() }
            dialog.confirm.onClick {
                dialog.dismiss()

                //发起举报请求
                val params = hashMapOf(
                    "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                    "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                    "type" to if (squareBean!!.iscollected == 0) {
                        1
                    } else {
                        2
                    },
                    "square_id" to squareBean!!.id!!,
                    "_timestamp" to System.currentTimeMillis()
                )
                mPresenter.getSquareReport(params)

            }


            moreActionDialog.dismiss()
        }
    }


    override fun onRemoveMySquareResult(b: Boolean, position: Int) {
        if (b) {
            CommonFunction.toast("删除动态成功！")
            finish()
            EventBus.getDefault().post(RefreshSquareEvent(true, TAG))
            EventBus.getDefault().postSticky(UserCenterEvent(true))
        } else {
            CommonFunction.toast("删除动态失败！")
        }

    }

    override fun onGetSquareReport(t: Boolean) {
        if (t) {
            CommonFunction.toast("举报成功！")
            EventBus.getDefault().post(RefreshSquareEvent(true, TAG))
        } else {
            CommonFunction.toast("举报失败！")
        }
    }

    /**
     * 展示转发动态对话框
     */
    private fun showTranspondDialog() {
        val transpondDialog = TranspondDialog(this, squareBean)
        transpondDialog.show()
    }


    override fun onGetSquareLikeResult(position: Int, result: Boolean) {
        if (result) {
            if (squareBean.isliked == 1) {
                squareBean.isliked = 0
                squareBean.like_cnt = squareBean.like_cnt!!.minus(1)
            } else {
                squareBean.isliked = 1
                squareBean.like_cnt = squareBean.like_cnt!!.plus(1)
            }

            val drawable1 =
                resources.getDrawable(if (squareBean.isliked == 1) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan_white)
            drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
            detailPlaydianzan.setCompoundDrawables(drawable1, null, null, null)
            detailPlaydianzan.text = "${squareBean.like_cnt}"
            EventBus.getDefault().post(RefreshSquareEvent(true, TAG))
        }
    }

    override fun onGetSquareCollectResult(position: Int, data: BaseResp<Any?>) {
        CommonFunction.toast(data.msg)
        if (data.code == 200) {
            if (squareBean.iscollected == 1) {
                squareBean.iscollected = 0
            } else {
                squareBean.iscollected = 1
            }
            EventBus.getDefault().post(RefreshSquareEvent(true, TAG))
        }
        if (moreActionDialog != null && moreActionDialog.isShowing) {
            moreActionDialog.dismiss()
        }
    }

    override fun onAddCommentResult(position: Int, data: BaseResp<Any?>?, success: Boolean) {
        detailPlayCommentSend.isEnabled = true
        if (success)
            EventBus.getDefault().post(RefreshSquareEvent(true, TAG))
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.detailPlayUserAvatar -> {
                if (UserManager.getAccid() != squareBean.accid) {
                    MatchDetailActivity.start(this, squareBean.accid)
                    GSYVideoManager.onResume()
                    detailPlayVideo.onVideoResume()
                }
            }
            //更多操作
            R.id.detailPlayMoreActions -> {
                showMoreDialog(0)
            }
            //点赞
            R.id.detailPlaydianzan -> {
                val params = hashMapOf(
                    "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                    "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                    "type" to if (squareBean.isliked == 1) {
                        2
                    } else {
                        1
                    },
                    "square_id" to squareBean.id!!,
                    "_timestamp" to System.currentTimeMillis()
                )
                mPresenter.getSquareLike(params, 0)
            }
            //点击内容跳转到评论详情页面
            R.id.detailPlayContent, R.id.detailPlayCommentBtn -> {
                SquareCommentDetailActivity.start(this, squareBean)
            }
            R.id.detailPlayCommentSend -> {
                if (!detailPlayComment.text.toString().isEmpty()) {
                    mPresenter.addComment(
                        hashMapOf(
                            "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                            "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                            "square_id" to squareBean.id!!,
                            "content" to detailPlayComment.text.toString()
                        ), 0
                    )
                    detailPlayCommentSend.isEnabled = false
                } else
                    CommonFunction.toast("说点什么吧")
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (intent.getBooleanExtra("fromRecommend", false)) {
            GSYVideoManager.releaseAllVideos()
        } else {
            detailPlayVideo.gsyVideoManager.setListener(detailPlayVideo.gsyVideoManager.listener())
            SwitchUtil.savePlayState(detailPlayVideo)
            detailPlayVideo.gsyVideoManager.setLastListener(detailPlayVideo)
//        supportFinishAfterTransition()
            setResult(Activity.RESULT_OK, intent)
        }

        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        detailPlayVideo.onVideoPause()

    }

    override fun onResume() {
        super.onResume()
        detailPlayVideo.onVideoResume(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SquarePlayDetailActivity=", "onDestroy")

        if (intent.getBooleanExtra("fromRecommend", false)) {
            detailPlayVideo.release()
            GSYVideoManager.releaseAllVideos()
        }
//        detailPlayVideo.gsyVideoManager.setListener(detailPlayVideo.gsyVideoManager.lastListener())
//        detailPlayVideo.gsyVideoManager.setLastListener(null)
//        detailPlayVideo.release()
//        GSYVideoManager.releaseAllVideos()
//        SwitchUtil.release()
    }

    override fun finish() {
        if (intent.getBooleanExtra("fromRecommend", false)) {
            detailPlayVideo.release()
            GSYVideoManager.releaseAllVideos()
        } else {
            detailPlayVideo.gsyVideoManager.setListener(detailPlayVideo.gsyVideoManager.listener())
            SwitchUtil.savePlayState(detailPlayVideo)
            detailPlayVideo.gsyVideoManager.setLastListener(detailPlayVideo)
//        supportFinishAfterTransition()
            setResult(Activity.RESULT_OK, intent)
        }

        super.finish()
    }
}
