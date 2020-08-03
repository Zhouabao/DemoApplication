package com.sdy.jitangapplication.ui.activity

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.MyInvitedBeans
import com.sdy.jitangapplication.presenter.MyInvitedPresenter
import com.sdy.jitangapplication.presenter.view.MyInvitedView
import com.sdy.jitangapplication.ui.adapter.MyInvitedAdapter
import kotlinx.android.synthetic.main.activity_invite_rewards.*
import kotlinx.android.synthetic.main.activity_my_invited.*
import kotlinx.android.synthetic.main.activity_my_invited.rewardsMoney
import kotlinx.android.synthetic.main.activity_my_invited.rewardsMoneyMax
import kotlinx.android.synthetic.main.activity_my_invited.rewardsMoreLevel
import kotlinx.android.synthetic.main.activity_my_invited.rewardsPercent
import kotlinx.android.synthetic.main.activity_my_invited.rewardsProgress
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity


/**
 * 我的邀请
 */
class MyInvitedActivity : BaseMvpActivity<MyInvitedPresenter>(), MyInvitedView, OnRefreshListener,
    OnLoadMoreListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_invited)
        initView()
        mPresenter.myinviteLog(page)
    }

    private fun initView() {
        mPresenter = MyInvitedPresenter()
        mPresenter.context = this
        mPresenter.mView = this
        BarUtils.setStatusBarColor(this, resources.getColor(R.color.colorOrange))
        llTitle.setBackgroundColor(resources.getColor(R.color.colorOrange))
        btnBack.setImageResource(R.drawable.icon_back_white)
        hotT1.text = "所有邀请"
        hotT1.setTextColor(Color.WHITE)
        btnBack.clickWithTrigger {
            finish()
        }

        refreshMyInvited.setOnRefreshListener(this)
        refreshMyInvited.setOnLoadMoreListener(this)
        myInvitedRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        myInvitedRv.adapter = adapter

    }

    private val adapter by lazy { MyInvitedAdapter(MyInvitedAdapter.FROM_INVITED) }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        mPresenter.myinviteLog(page)
        refreshLayout.resetNoMoreData()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        mPresenter.myinviteLog(page)
    }


    private var page = 1
    override fun myinviteLogResult(data: MyInvitedBeans?) {
        if (data != null) {
            if (refreshMyInvited.state == RefreshState.Loading) {
                if (data!!.list.isNullOrEmpty() || data!!.list.size < Constants.PAGESIZE) {
                    refreshMyInvited.finishLoadMoreWithNoMoreData()
                } else {
                }
                refreshMyInvited.finishLoadMore()
            } else {
                refreshMyInvited.finishRefresh()

                rewardsPercent.text = "享${data!!.now_rate}%分佣"
                rewardsMoreLevel.text = data!!.title

                rewardsMoney.text = "${data!!.progress.invite_cnt}"
                rewardsMoneyMax.text = "${data!!.progress.reward_money}元"
                rewardsProgress.setProgress(data!!.progress.invite_cnt * 1f / data!!.progress.all_cnt * 100)


                val rate = rewardsMoneyMax.width * 1f / rewardsProgress.width
                val translate = ObjectAnimator.ofFloat(
                    rewardsMoney,
                    "translationX",
                    if (1 - (data!!.progress.invite_cnt * 1f / data!!.progress.all_cnt) > rate
                    ) {
                        rewardsProgress.width * (data!!.progress.invite_cnt * 1f / data!!.progress.all_cnt)
                    } else {
                        rewardsProgress.width * (data!!.progress.invite_cnt * 1f / data!!.progress.all_cnt) - rewardsMoneyMax.width - rewardsMoney.width
                    }
                )
                translate.duration = 100
                translate.start()
            }

            adapter.addData(data!!.list)
        } else {
            refreshMyInvited.finishLoadMore(false)
            refreshMyInvited.finishRefresh(false)
        }

    }
}