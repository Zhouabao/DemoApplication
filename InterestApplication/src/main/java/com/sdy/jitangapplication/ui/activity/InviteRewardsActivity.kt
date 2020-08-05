package com.sdy.jitangapplication.ui.activity

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.InvitePoliteBean
import com.sdy.jitangapplication.model.MyInviteBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.uikit.common.util.sys.ClipboardUtil
import com.sdy.jitangapplication.presenter.InviteRewardsPresenter
import com.sdy.jitangapplication.presenter.view.InviteRewardsView
import com.sdy.jitangapplication.ui.adapter.UplevelRewardsAdapter
import com.sdy.jitangapplication.ui.dialog.MoreActionNewDialog
import com.sdy.jitangapplication.ui.dialog.ShareRuleDialog
import kotlinx.android.synthetic.main.activity_invite_rewards.*
import kotlinx.android.synthetic.main.dialog_more_action_new.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.item_marquee_share_friends.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity

/**
 * 分享有礼页面
 */
class InviteRewardsActivity : BaseMvpActivity<InviteRewardsPresenter>(), InviteRewardsView {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_rewards)
        initView()
        mPresenter.invitePolite()
    }

    private val adapter by lazy { UplevelRewardsAdapter() }
    private fun initView() {
        mPresenter = InviteRewardsPresenter()
        mPresenter.context = this
        mPresenter.mView = this



        StatusBarUtil.immersive(this)
        val params11 = llTitle.layoutParams as ConstraintLayout.LayoutParams
        params11.topMargin = StatusBarUtil.getStatusBarHeight(this)
        iconBgTop.layoutParams = params11

        val params1 = iconBgTop.layoutParams as ConstraintLayout.LayoutParams
        params1.width = ScreenUtils.getScreenWidth()
        params1.height = (88 / 375F * params1.width).toInt()
        iconBgTop.layoutParams = params1

        val params = iconBg.layoutParams as ConstraintLayout.LayoutParams
        params.width = ScreenUtils.getScreenWidth()
        params.height = (200 / 375F * params.width).toInt()
        iconBg.layoutParams = params

        llTitle.setBackgroundColor(Color.TRANSPARENT)
        btnBack.setImageResource(R.drawable.icon_back_white)
        btnBack.clickWithTrigger {
            finish()
        }
        hotT1.setTextColor(Color.WHITE)
        hotT1.text = "邀请有礼"
        rightBtn.setTextColor(Color.WHITE)
        rightBtn.text = "联系我们"
        rightBtn.isVisible = true
        rightBtn.clickWithTrigger {
            ChatActivity.start(this, Constants.ASSISTANT_ACCID)
        }



        SpanUtils.with(addKefuWechat)
            .append("分享邀请好友充值付费即享分佣收益，多邀请享更多额外收益，如您有大量用户渠道可直接联系客服微信")
            .append("jitangkefu")
            .setForegroundColor(Color.parseColor("#ff6318"))
            .append("，备注「拉新」")
            .create()

        rewardsRv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rewardsRv.adapter = adapter

        //分享规则
        shareRuleBtn.clickWithTrigger {
            ShareRuleDialog(this, invite_rule).show()
        }

        //我邀请的
        seeMyInvited.clickWithTrigger {
            startActivity<MyInvitedActivity>()
        }

        //我的奖励
        seeMyReward.clickWithTrigger {
            startActivity<MyRewardsActivity>()

        }

        //立即分享
        shareNowBtn.clickWithTrigger {
            if (myInviteBean.invite_url.isNotEmpty())
                showShareDialog()
        }

        retryBtn.clickWithTrigger {
            stateInviteRewards.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.invitePolite()
        }
    }

    private var myInviteBean = MyInviteBean()
    private val invite_rule by lazy { mutableListOf<String>() }
    override fun invitePoliteResult(invitePoliteBean: InvitePoliteBean?) {
        if (invitePoliteBean != null) {
            stateInviteRewards.viewState = MultiStateView.VIEW_STATE_CONTENT

            myInviteBean.invite_descr = invitePoliteBean?.invite_descr
            myInviteBean.invite_title = invitePoliteBean?.invite_title
            myInviteBean.invite_pic = invitePoliteBean?.invite_pic
            myInviteBean.invite_url = invitePoliteBean?.invite_url
            invite_rule.addAll(invitePoliteBean?.invite_rule)

//        invitePoliteBean?.progress?.invite_cnt = invitePoliteBean?.progress?.all_cnt ?: 0
//        invitePoliteBean?.progress?.invite_cnt = 10
            for (data in invitePoliteBean?.reward_list) {
                rewardsFl.addView(getMarqueeView(data))
            }
            adapter.addData(invitePoliteBean?.level_list)
            when (invitePoliteBean?.now_level) {
                1 -> rewardsLevel.setImageResource(R.drawable.icon_level1)
                2 -> rewardsLevel.setImageResource(R.drawable.icon_level2)
                3 -> rewardsLevel.setImageResource(R.drawable.icon_level3)
            }
            rewardsPercent.text = "享${invitePoliteBean?.now_rate}%分佣"
            rewardsMoreLevel.isVisible = !invitePoliteBean?.title.isNullOrEmpty()
            rewardsMoreLevel.text = "${invitePoliteBean?.title}"
            SpanUtils.with(myInvitedCount)
                .append("${invitePoliteBean?.invite_cnt}")
                .setFontSize(30, true)
                .setBold()
                .append("人")
                .setFontSize(12, true)
                .create()
            SpanUtils.with(myRewardsMoney)
                .append("${invitePoliteBean?.invite_amount}")
                .setFontSize(30, true)
                .setBold()
                .append("元")
                .setFontSize(12, true)
                .create()

            if (invitePoliteBean?.progress != null && invitePoliteBean?.progress.all_cnt > 0) {
                rewardsprogressCl.isVisible = true
                rewardsMoney.text = "${invitePoliteBean?.progress?.invite_cnt}"
                rewardsMoneyMax.text = "${invitePoliteBean?.progress?.reward_money}"
                val progress = invitePoliteBean?.progress?.invite_cnt * 1f / invitePoliteBean?.progress?.all_cnt
                val progressWidth = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(60F)
                val rate = rewardsMoneyMax.minWidth * 1f / progressWidth
                val cntRate = rewardsMoney.minWidth * 1f / progressWidth

                rewardsProgress.setProgress(progress * 100)
                val translate = ObjectAnimator.ofFloat(
                    rewardsMoney,
                    "translationX",
                    if (1 - progress > rate) {
                        progressWidth * progress - if (progress > cntRate) {
                            rewardsMoney.minWidth / 2F
                        } else {
                            0F
                        }
                    } else {
                        progressWidth * progress - rewardsMoneyMax.minWidth - rewardsMoney.minWidth
                    }
                )
                translate.duration = 300
                translate.start()
            } else {
                rewardsprogressCl.isVisible = false
            }
        } else {
            stateInviteRewards.viewState = MultiStateView.VIEW_STATE_ERROR
        }


    }


    /**
     * 遍历循环到已经成为会员的
     */
    private fun getMarqueeView(content: String): View {
        val view = layoutInflater.inflate(R.layout.item_marquee_share_friends, null, false)
        view.vipShareDescr.text = content
        return view
    }


    /**
     * 展示更多操作对话框
     */

    lateinit var moreActionDialog: MoreActionNewDialog
    private fun showShareDialog() {
        //todo 拉新分享链接
        moreActionDialog =
            MoreActionNewDialog(
                this,
                url = myInviteBean.invite_url,
                type = MoreActionNewDialog.TYPE_SHARE_VIP_URL,
                title = myInviteBean.invite_title,
                content = myInviteBean.invite_descr,
                pic = myInviteBean.invite_pic
            )
        moreActionDialog.show()

        moreActionDialog.collect.isVisible = true
        moreActionDialog.report.isVisible = false
        moreActionDialog.delete.isVisible = false
        moreActionDialog.transpondFriend.isVisible = false

        moreActionDialog.collect.text = "复制链接"
        val top = resources.getDrawable(R.drawable.icon_copy_url)
        moreActionDialog.collect.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)

        moreActionDialog.collect.onClick {
            ClipboardUtil.clipboardCopyText(this, "")
            CommonFunction.toast("分享链接已复制")
        }
    }
}