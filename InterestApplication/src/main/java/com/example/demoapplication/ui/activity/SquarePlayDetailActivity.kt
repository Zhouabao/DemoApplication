package com.example.demoapplication.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.SPUtils
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.event.PlayVideoEvent
import com.example.demoapplication.model.SquareBean
import com.example.demoapplication.presenter.SquarePlayDetaiPresenter
import com.example.demoapplication.presenter.view.SquarePlayDetailView
import com.example.demoapplication.switchplay.SwitchUtil
import com.example.demoapplication.ui.dialog.MoreActionDialog
import com.example.demoapplication.ui.dialog.TranspondDialog
import com.kotlin.base.common.BaseApplication.Companion.context
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import kotlinx.android.synthetic.main.activity_square_play_detail.*
import kotlinx.android.synthetic.main.dialog_more_action.*
import kotlinx.android.synthetic.main.item_square_detail_play_cover.*
import kotlinx.android.synthetic.main.switch_video.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

/**
 * 点击图片、视频、录音进入详情页面，并且支持点击左右切换好友动态
 */
class SquarePlayDetailActivity : BaseMvpActivity<SquarePlayDetaiPresenter>(), SquarePlayDetailView,
    View.OnClickListener {

    private val squareBean: SquareBean by lazy { intent.getSerializableExtra("squareBean") as SquareBean }
    //外部辅助的旋转，帮助全屏
    private var orientationUtils: OrientationUtils? = null

    companion object {
        public val OPTION_VIEW = "VIEW"
        public val REQUEST_CODE = 1002
        fun startActivity(activity: Activity, transactionView: View, data: SquareBean, position: Int) {
            val intent = Intent(activity, SquarePlayDetailActivity::class.java)
            intent.putExtra("squareBean", data)
            intent.putExtra("position", position)
            //这里指定了共享的视图元素
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transactionView, OPTION_VIEW)
            ActivityCompat.startActivityForResult(activity, intent, REQUEST_CODE, options.toBundle())
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_square_play_detail)
        initView()
        initData()
    }


    private fun initView() {
        GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL)
        mPresenter = SquarePlayDetaiPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        btnBack.onClick {
            onBackPressed()
        }
//        btnBack.visibility = View.GONE

        //评论
        detailPlayComment.setOnClickListener(this)
        //更多操作
        detailPlayMoreActions.setOnClickListener(this)
        //点赞
        detailPlaydianzan.setOnClickListener(this)
        //点击内容跳转到评论详情页面
        detailPlayContent.setOnClickListener(this)
        //發送評論
        detailPlayCommentSend.setOnClickListener(this)
        detailPlayVideo.titleTextView.visibility = View.GONE
        detailPlayVideo.backButton.visibility = View.VISIBLE
        detailPlayVideo.detail_btn.visibility = View.GONE

        //外部辅助的旋转，帮助全屏
        orientationUtils = OrientationUtils(this, detailPlayVideo)
        //初始化不打开外部的旋转
        orientationUtils!!.isEnable = false
//        SwitchUtil.optionPlayer(detailPlayVideo, squareBean.video_json?.get(0) ?: "", true, "这是title")
        SwitchUtil.optionPlayer(
            detailPlayVideo,
            "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4",
            false,
            ""
        )

        SwitchUtil.clonePlayState(detailPlayVideo)
        detailPlayVideo.setIsTouchWiget(true)
        detailPlayVideo.setVideoAllCallBack(object : GSYSampleCallBack() {
            override fun onPrepared(url: String?, vararg objects: Any?) {
                super.onPrepared(url, *objects)
                //开始播放了才能旋转和全屏
                orientationUtils!!.setEnable(true)
            }

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

            override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
                super.onQuitFullscreen(url, *objects)
                if (orientationUtils != null) {
                    orientationUtils!!.backToProtVideo()
                }
            }
        })


        detailPlayVideo.getFullscreenButton().setOnClickListener(View.OnClickListener {
            //直接横屏
            orientationUtils!!.resolveByClick()
            //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
            detailPlayVideo.startWindowFullscreen(this, false, false)
        })

        detailPlayVideo.setSurfaceToPlay()
        // 这里指定了被共享的视图元素
        ViewCompat.setTransitionName(videoFl, OPTION_VIEW)
    }

    private fun initData() {
        GlideUtil.loadAvatorImg(context, squareBean.avatar ?: "", detailPlayUserAvatar)
        detailPlayUserLocationAndTime.text = squareBean.city_name ?: "".plus("\t").plus(squareBean.out_time ?: "")
        detailPlayUserName.text = squareBean.nickname ?: ""
        detailPlayContent.text = squareBean.descr ?: ""

        val drawable1 =
            context.resources.getDrawable(if (squareBean.isliked == 1) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
        drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
        detailPlaydianzan.setCompoundDrawables(drawable1, null, null, null)
        detailPlaydianzan.text = "${squareBean.like_cnt}"

        videoFl.background = BitmapDrawable(
            ImageUtils.fastBlur(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.img_avatar_01
                ),
                1f,
                25f
            )
        )
    }


    lateinit var moreActionDialog: MoreActionDialog

    /**
     * 展示更多操作对话框
     */
    private fun showMoreDialog(position: Int) {
        moreActionDialog = MoreActionDialog(this, "square_detail")
        moreActionDialog.show()

        if (squareBean?.iscollected == 0) {
            moreActionDialog.collect.text = "收藏"
            moreActionDialog.collectBtn.setImageResource(R.drawable.icon_collect_no)
        } else {
            moreActionDialog.collect.text = "取消收藏"
            moreActionDialog.collectBtn.setImageResource(R.drawable.icon_collectt)
        }
        moreActionDialog.llShare.onClick {
            showTranspondDialog(position)
        }
        moreActionDialog.llCollect.onClick {

            //发起收藏请求
            val params = hashMapOf(
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                "type" to if (squareBean.iscollected == 0) {
                    1
                } else {
                    2
                },
                "square_id" to squareBean.id!!,
                "_timestamp" to System.currentTimeMillis()
            )
            mPresenter.getSquareCollect(params, position)
        }
        moreActionDialog.llJubao.onClick {
            //todo 发起举报请求
            AlertDialog.Builder(this)
                .setNegativeButton("取消举报", object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface, p1: Int) {
                        p0.cancel()
                    }
                })
                .setPositiveButton("确认举报", object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        //todo 举报
                        toast("已举报$position")
                    }

                })
                .setTitle("举报")
                .setMessage("是否确认举报该动态？")
                .show()
        }
        moreActionDialog.cancel.onClick {
            moreActionDialog.dismiss()
        }

    }


    private val transpondDialog by lazy { TranspondDialog(this) }

    /**
     * 展示转发动态对话框
     */
    private fun showTranspondDialog(position: Int) {
        if (transpondDialog != null && !transpondDialog.isShowing)
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
                context.resources.getDrawable(if (squareBean.isliked == 1) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
            drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
            detailPlaydianzan.setCompoundDrawables(drawable1, null, null, null)
            detailPlaydianzan.text = "${squareBean.like_cnt}"
        }
    }

    override fun onGetSquareCollectResult(position: Int, data: BaseResp<Any?>) {
        toast(data.msg)
        if (squareBean.iscollected == 1) {
            squareBean.iscollected = 0
        } else {
            squareBean.iscollected = 1
        }
        if (moreActionDialog != null && moreActionDialog.isShowing) {
            moreActionDialog.dismiss()
        }
    }

    override fun onAddCommentResult(position: Int, data: BaseResp<Any?>) {
        toast(data.msg)
    }


    override fun onClick(view: View) {
        when (view.id) {

            //评论
            R.id.detailPlayComment -> {
                //todo 评论
                toast(detailPlayComment.text.toString() ?: "")
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
            R.id.detailPlayContent -> {
                startActivity<SquareCommentDetailActivity>("squareBean" to squareBean)
            }
            R.id.detailPlayCommentSend -> {
                if (!detailPlayComment.text.toString().isEmpty())
                    mPresenter.addComment(
                        hashMapOf(
                            "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                            "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                            "square_id" to squareBean.id!!,
                            "content" to detailPlayComment.text.toString()
                        ), 0
                    )
                else
                    toast("说点什么吧")
            }

        }
    }

    private val isPlay = true
    private var isPause: Boolean = false


    override fun onBackPressed() {
        if (orientationUtils != null) {
            orientationUtils!!.backToProtVideo()
        }
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        detailPlayVideo.getGSYVideoManager().setListener(detailPlayVideo.getGSYVideoManager().listener())
        SwitchUtil.savePlayState(detailPlayVideo)
        EventBus.getDefault().post(PlayVideoEvent(intent.getIntExtra("position", -1)))
        super.onBackPressed()
    }

    override fun onPause() {
        detailPlayVideo.onVideoPause()
        super.onPause()
        isPause = true
    }

    override fun onResume() {
        detailPlayVideo.onVideoResume(false)
        super.onResume()
        isPause = false
    }

    override fun onDestroy() {
        super.onDestroy()
        detailPlayVideo.getGSYVideoManager().setListener(detailPlayVideo.getGSYVideoManager().lastListener())
        detailPlayVideo.getGSYVideoManager().setLastListener(null)
        GSYVideoManager.releaseAllVideos()
        if (orientationUtils != null)
            orientationUtils!!.releaseListener()
        SwitchUtil.release()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //如果旋转了就全屏
        if (isPlay && !isPause) {
            detailPlayVideo.onConfigurationChanged(this, newConfig, orientationUtils, true, true)
        }
    }

    override fun onGetRecentlySquaresResults(mutableList: MutableList<SquareBean?>) {


    }
}
