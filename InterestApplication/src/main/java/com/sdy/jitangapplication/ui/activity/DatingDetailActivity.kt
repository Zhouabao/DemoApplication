package com.sdy.jitangapplication.ui.activity

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.view.isVisible
import com.blankj.utilcode.util.VibrateUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.DatingBean
import com.sdy.jitangapplication.presenter.DatingDetailPresenter
import com.sdy.jitangapplication.presenter.view.DatingDetailView
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.ui.dialog.MoreActionNewDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_dating_detail.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.dialog_more_action_new.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.item_layout_dating_square_man.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity

/**
 * 约会详情
 */
class DatingDetailActivity : BaseMvpActivity<DatingDetailPresenter>(), DatingDetailView,
    OnLazyClickListener {

    private val dating_id by lazy { intent.getIntExtra("dating_id", 0) }

    companion object {
        fun start2Detail(context: Context, id: Int) {
            context.startActivity<DatingDetailActivity>("dating_id" to id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dating_detail)

        initView()
        mPresenter.datingInfo(dating_id)
    }

    private fun initView() {
        mPresenter = DatingDetailPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        hotT1.text = "活动详情"
        btnBack.clickWithTrigger {
            finish()
        }

        retryBtn.clickWithTrigger {
            stateDatingDetail.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.datingInfo(dating_id)
        }
        datingDianzanAni.setOnClickListener(this)
        datingZanCnt.setOnClickListener(this)
        datingAvator.setOnClickListener(this)
        applyForDatingBtn.setOnClickListener(this)
        datingMoreBtn.setOnClickListener(this)

    }


    /**
     * 展示更多操作对话框
     */
    private fun showMoreDialog() {
        val moreActionDialog = MoreActionNewDialog(this, null)
        moreActionDialog.show()

        if (datingBean?.accid == UserManager.getAccid()) {
            moreActionDialog.delete.visibility = View.VISIBLE
            moreActionDialog.report.visibility = View.GONE
            moreActionDialog.collect.isVisible = false
        } else {
            moreActionDialog.delete.visibility = View.GONE
            moreActionDialog.report.visibility = View.VISIBLE
            moreActionDialog.collect.isVisible = false
        }
        moreActionDialog.delete.onClick {
            val params = hashMapOf("dating_id" to datingBean!!.id)
            //todo 删除约会
            moreActionDialog.dismiss()

        }


        moreActionDialog.report.onClick {
            val dialog = DeleteDialog(this)
            dialog.show()
            dialog.title.text = "活动举报"
            dialog.tip.text = getString(R.string.report_dating)
            dialog.confirm.text = "举报"
            dialog.cancel.onClick { dialog.dismiss() }
            dialog.confirm.onClick {
                dialog.dismiss()
                if (datingBean != null)
                    mPresenter.reportDating(datingBean!!.id)
            }
            moreActionDialog.dismiss()

        }

    }


    override fun onLazyClick(v: View) {
        when (v.id) {
            R.id.datingDianzanAni, R.id.datingZanCnt -> {
                handler.removeCallbacksAndMessages(null)
                datingBean!!.tempLike = !datingBean!!.tempLike
                datingBean!!.temp_like_cnt = if (datingBean!!.tempLike) {
                    datingBean!!.temp_like_cnt + 1
                } else {
                    datingBean!!.temp_like_cnt - 1
                }
                setTempLikeState(datingBean!!.tempLike, datingBean!!.temp_like_cnt)
                handler.postDelayed({
                    if (datingBean!!.tempLike != datingBean!!.isliked)
                        mPresenter.doLike(
                            datingBean!!.id, if (datingBean!!.isliked) {
                                2
                            } else {
                                1
                            }
                        )
                }, 1000L)

            }
            R.id.applyForDatingBtn -> {
                if (datingBean != null)
                    CommonFunction.checkApplyForDating(this, datingBean!!)
            }
            R.id.datingAvator -> {
                if (datingBean!!.accid != UserManager.getAccid())
                    MatchDetailActivity.start(this, datingBean!!.accid)
            }
            R.id.datingMoreBtn -> {
                showMoreDialog()
            }
        }

    }

    private var datingBean: DatingBean? = null
    override fun datingInfoResult(datingBean: DatingBean?) {
        if (datingBean != null) {
            stateDatingDetail.viewState = MultiStateView.VIEW_STATE_CONTENT
            datingBean.tempLike = datingBean.isliked
            datingBean.temp_like_cnt = datingBean.like_cnt
            this.datingBean = datingBean
            initData()
        } else {
            stateDatingDetail.viewState = MultiStateView.VIEW_STATE_ERROR
        }

    }


    //	1 点赞 2 取消点赞
    override fun doLikeResult(result: Boolean, isLiked: Boolean) {
        if (result) {
            datingBean!!.isliked = isLiked
            datingBean!!.tempLike = isLiked
            datingBean!!.like_cnt = if (datingBean!!.isliked) {
                datingBean!!.like_cnt + 1
            } else {
                datingBean!!.like_cnt - 1
            }
            setTempLikeState(isLiked, datingBean!!.like_cnt)
        }
    }

    private fun setTempLikeState(isLiked: Boolean, likeCnt: Int) {
        datingZanCnt.text = "${likeCnt}"
        if (isLiked) {
            if (datingBean!!.isliked && datingBean!!.isliked == isLiked) {
                datingDianzanAni.progress = 1f
            } else {
                datingDianzanAni.playAnimation()
                VibrateUtils.vibrate(50L)
            }
        } else {
            datingDianzanAni.progress = 0F
        }
    }


    private val handler by lazy { Handler() }

    private fun initData() {
        if (datingBean != null) {
            datingZanCnt.isVisible = datingBean!!.accid != UserManager.getAccid()
            datingDianzanAni.isVisible = datingBean!!.accid != UserManager.getAccid()
            applyForDatingBtn.isVisible = datingBean!!.accid != UserManager.getAccid()
            datingMoreBtn.isVisible = datingBean!!.accid != UserManager.getAccid()


            GlideUtil.loadCircleImg(this, datingBean!!.avatar, datingAvator)
            datingName.text = datingBean!!.nickname
            datingVip.isVisible = datingBean!!.isplatinumvip
            datingTime.text = datingBean!!.online_time


            //content_type      1文本 2语音
            if (datingBean!!.content_type == 1) {
                datingDescr.isVisible = true
                myDatingAudioView.isVisible = false
                datingDescr.text = datingBean!!.content
            } else {
                datingDescr.isVisible = false
                myDatingAudioView.isVisible = true
                myDatingAudioView.setUi(
                    R.drawable.gradient_rectangle_orange_22dp,
                    audioTip = "点击播放活动语音描述"
                )
                myDatingAudioView.prepareAudio(datingBean!!.content, datingBean!!.duration)

            }


            datingProject.text =
                "${datingBean!!.title}${if (datingBean!!.dating_title.isNotEmpty()) {
                    "·${datingBean!!.dating_title}"
                } else {
                    ""
                }}"
            datingPlace.text = datingBean!!.place
            datingObject.text = datingBean!!.dating_target
            datingMoney.text = "${datingBean!!.cost_money}·${datingBean!!.cost_type}"
            datingPlan.text = datingBean!!.follow_up

            setTempLikeState(datingBean!!.isliked, datingBean!!.like_cnt)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}