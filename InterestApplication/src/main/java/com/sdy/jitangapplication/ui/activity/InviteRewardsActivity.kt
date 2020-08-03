package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.GiftBean
import com.sdy.jitangapplication.model.InvitePoliteBean
import com.sdy.jitangapplication.model.ViplistBean
import com.sdy.jitangapplication.presenter.InviteRewardsPresenter
import com.sdy.jitangapplication.presenter.view.InviteRewardsView
import com.sdy.jitangapplication.ui.adapter.UplevelRewardsAdapter
import com.sdy.jitangapplication.ui.dialog.ShareRuleDialog
import kotlinx.android.synthetic.main.activity_invite_rewards.*
import kotlinx.android.synthetic.main.item_marquee_vip_friends.view.*
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
        adapter.addData(GiftBean(checked = true))
        adapter.addData(GiftBean(checked = false))
        adapter.addData(GiftBean(checked = false))

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

    }


    /**
     * 遍历循环到已经成为会员的
     */
    private fun getMarqueeView(content: String): View {
        val view = layoutInflater.inflate(R.layout.item_marquee_vip_friends, null, false)
        view.vipFriendsName.text = content
        return view
    }
}