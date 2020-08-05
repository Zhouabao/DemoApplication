package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.RefreshMyWithDraw
import com.sdy.jitangapplication.model.MyRewardBeans
import com.sdy.jitangapplication.presenter.MyRewardsPresenter
import com.sdy.jitangapplication.presenter.view.MyRewardsView
import com.sdy.jitangapplication.ui.adapter.MyInvitedAdapter
import com.sdy.jitangapplication.ui.dialog.WithdrawCandyDialog
import kotlinx.android.synthetic.main.activity_my_rewards.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity


/**
 * 我的奖励
 */
class MyRewardsActivity : BaseMvpActivity<MyRewardsPresenter>(), MyRewardsView, OnRefreshListener,
    OnLoadMoreListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_rewards)
        initView()
        mPresenter.myinviteLog(page)
    }

    private fun initView() {
        EventBus.getDefault().register(this)
        mPresenter = MyRewardsPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        BarUtils.setStatusBarColor(this, resources.getColor(R.color.colorOrange))
        llTitle.setBackgroundColor(resources.getColor(R.color.colorOrange))
        btnBack.setImageResource(R.drawable.icon_back_white)
        hotT1.text = "我的奖励"
        hotT1.setTextColor(Color.WHITE)
        rightBtn.isVisible = true
        rightBtn.text = "提现记录"
        rightBtn.setTextColor(Color.WHITE)

        btnBack.clickWithTrigger {
            finish()
        }
        rightBtn.clickWithTrigger {
            startActivity<CandyRecordActivity>()
        }

        refreshMyRewards.setOnRefreshListener(this)
        refreshMyRewards.setOnLoadMoreListener(this)
        myRewardsdRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        myRewardsdRv.adapter = adapter
        adapter.setEmptyView(R.layout.empty_layout, myRewardsdRv)
        adapter.isUseEmpty(false)

        withdrawBtn.clickWithTrigger {
            WithdrawCandyDialog(this, false).show()
        }

        retryBtn.clickWithTrigger {
            stateMyRewards.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.myinviteLog(page)
        }

    }

    private val adapter by lazy { MyInvitedAdapter(MyInvitedAdapter.FROM_REWARDS) }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        mPresenter.myinviteLog(page)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        mPresenter.myinviteLog(page)
    }


    private var page = 1
    private var myBanlanceMoney = 0F
    private var myWithdrawMoney = 0F
    override fun myInviteRewardResult(data: MyRewardBeans?) {
        if (data != null) {
            stateMyRewards.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (refreshMyRewards.state == RefreshState.Loading) {
                if (data!!.list.isNullOrEmpty() || data!!.list.size < Constants.PAGESIZE) {
                    refreshMyRewards.finishLoadMoreWithNoMoreData()
                } else {
                    refreshMyRewards.finishLoadMore()
                }
            } else {
                adapter.data.clear()
                refreshMyRewards.finishRefresh()
                myRewardsMoney.text = "${data!!.red_balance_money}"
                myWithdrawRewardsMoney.text = "已提现${data!!.red_withdraw_money}"
                myBanlanceMoney = data!!.red_balance_money
                myWithdrawMoney = data!!.red_withdraw_money
            }
            adapter.addData(data!!.list)
            adapter.isUseEmpty(adapter.data.isEmpty())
            adapter.notifyDataSetChanged()
        } else {
            stateMyRewards.viewState = MultiStateView.VIEW_STATE_ERROR
            refreshMyRewards.finishLoadMore(false)
            refreshMyRewards.finishRefresh(false)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshMyWithDraw(event: RefreshMyWithDraw) {
        myBanlanceMoney -= event.redMoney
        myWithdrawMoney += event.redMoney
        myRewardsMoney.text = "$myBanlanceMoney"
        myWithdrawRewardsMoney.text = "已提现${myWithdrawMoney}"
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}