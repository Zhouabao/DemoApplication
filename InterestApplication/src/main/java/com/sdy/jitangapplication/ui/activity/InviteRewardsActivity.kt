package com.sdy.jitangapplication.ui.activity

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.InvitePoliteBean
import com.sdy.jitangapplication.presenter.InviteRewardsPresenter
import com.sdy.jitangapplication.presenter.view.InviteRewardsView
import com.sdy.jitangapplication.ui.adapter.UplevelRewardsAdapter
import com.sdy.jitangapplication.ui.dialog.ShareRuleDialog
import kotlinx.android.synthetic.main.activity_invite_rewards.*
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
            ShareRuleDialog(this).show()
        }

        //我邀请的
        seeMyInvited.clickWithTrigger {
            startActivity<MyInvitedActivity>()
        }

        //我的奖励
        seeMyReward.clickWithTrigger {
            startActivity<MyRewardsActivity>()

        }
    }

    override fun invitePoliteResult(invitePoliteBean: InvitePoliteBean?) {
        for (data in invitePoliteBean?.reward_list ?: mutableListOf()) {
            rewardsFl.addView(getMarqueeView(data))
        }
        adapter.addData(invitePoliteBean?.level_list ?: mutableListOf())
        when (invitePoliteBean?.now_level) {
            1 -> rewardsLevel.setImageResource(R.drawable.icon_level1)
            2 -> rewardsLevel.setImageResource(R.drawable.icon_level2)
            3 -> rewardsLevel.setImageResource(R.drawable.icon_level3)
        }
        rewardsPercent.text = "享${invitePoliteBean?.now_rate}%分佣"
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
        rewardsMoney.text = "${invitePoliteBean?.progress?.invite_cnt}"
        rewardsMoneyMax.text = "${invitePoliteBean?.progress?.reward_money}"
        rewardsProgress.maxProgress = (invitePoliteBean?.progress?.all_cnt ?: 0f) as Float
        rewardsProgress.setProgress((invitePoliteBean?.progress?.invite_cnt ?: 0F) as Float)


        rewardsProgress.setProgress(
            (invitePoliteBean?.progress?.invite_cnt ?: 0) * 1f
                    / (invitePoliteBean?.progress?.all_cnt ?: 0) * 100
        )
//                rewardsMoney.
        val translate = ObjectAnimator.ofFloat(
            rewardsMoney,
            "translationX",
            rewardsProgress.width * (invitePoliteBean?.progress?.invite_cnt ?: 0) * 1f
                    / (invitePoliteBean?.progress?.all_cnt ?: 0)
        )
        translate.duration = 100
        translate.start()
    }


    /**
     * 遍历循环到已经成为会员的
     */
    private fun getMarqueeView(content: String): View {
        val view = layoutInflater.inflate(R.layout.item_marquee_share_friends, null, false)
        view.vipShareDescr.text = content
        return view
    }
}