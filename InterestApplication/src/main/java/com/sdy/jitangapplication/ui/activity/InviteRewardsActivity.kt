package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SpanUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.GiftBean
import com.sdy.jitangapplication.ui.adapter.UplevelRewardsAdapter
import com.sdy.jitangapplication.ui.dialog.ShareRuleDialog
import com.sina.weibo.sdk.share.BaseActivity
import kotlinx.android.synthetic.main.activity_invite_rewards.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity

/**
 * 分享有礼页面
 */
class InviteRewardsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_rewards)
        initView()
    }

    private val adapter by lazy { UplevelRewardsAdapter() }
    private fun initView() {
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
}