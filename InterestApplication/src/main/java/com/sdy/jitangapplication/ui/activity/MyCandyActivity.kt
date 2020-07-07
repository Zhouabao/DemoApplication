package com.sdy.jitangapplication.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.event.RefreshMyCandyEvent
import com.sdy.jitangapplication.event.SetMyCandyEvent
import com.sdy.jitangapplication.model.BillBean
import com.sdy.jitangapplication.model.GoodsCategoryBeans
import com.sdy.jitangapplication.model.PullWithdrawBean
import com.sdy.jitangapplication.presenter.MyCandyPresenter
import com.sdy.jitangapplication.presenter.view.MyCandyView
import com.sdy.jitangapplication.ui.adapter.CandyRecordAdapter
import com.sdy.jitangapplication.ui.dialog.WithdrawCandyDialog
import kotlinx.android.synthetic.main.activity_my_candy.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity


/**
 * 我的糖果中心
 */
class MyCandyActivity : BaseMvpActivity<MyCandyPresenter>(), MyCandyView, OnLazyClickListener,
    OnLoadMoreListener, OnRefreshListener {

    private val candyProductAdapter by lazy { CandyRecordAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_candy)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            BarUtils.setStatusBarVisibility(this, false)
        }
        initView()
        mPresenter.myCadny()
    }

    private var hasGuideCandy = false
    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = MyCandyPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        btnBack.setOnClickListener(this)
        withdrawCandy.setOnClickListener(this)
        rechargeCandy.setOnClickListener(this)

        refreshMyRecord.setOnLoadMoreListener(this)
        refreshMyRecord.setOnRefreshListener(this)
        stateMyCandy.retryBtn.onClick {
            stateMyCandy.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.myBillList(params)
        }
        recordRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recordRv.adapter = candyProductAdapter
    }


    override fun finish() {
        super.finish()
    }


    override fun ongoodsCategoryList(success: Boolean, data: GoodsCategoryBeans?) {

    }

    private var isWithdraw = false
    override fun onMyCadnyResult(candyCoun: PullWithdrawBean?) {
        if (candyCoun != null) {
            EventBus.getDefault().post(SetMyCandyEvent(candyCoun!!.candy_amount))
            mycandy = candyCoun!!.candy_amount
            candyCount.text = CommonFunction.num2thousand("${candyCoun.candy_amount}")
            withdrawCandy.isVisible = candyCoun.is_withdraw
            isWithdraw = candyCoun.is_withdraw

        }
        mPresenter.myBillList(params)
    }


    override fun onMyBillList(success: Boolean, billList: MutableList<BillBean>?) {
        if (success) {
            stateMyCandy.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (refreshMyRecord.state != RefreshState.Loading) {
                candyProductAdapter.data.clear()
                candyProductAdapter.notifyDataSetChanged()
                refreshMyRecord.finishRefresh()
                refreshMyRecord.resetNoMoreData()
            } else {
                if ((billList ?: mutableListOf<BillBean>()).size < Constants.PAGESIZE) {
                    refreshMyRecord.finishLoadMoreWithNoMoreData()
                } else {
                    refreshMyRecord.finishLoadMore(true)
                }
            }
            candyProductAdapter.addData(billList ?: mutableListOf<BillBean>())
        } else {
            stateMyCandy.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }

    private var page = 1
    private val params by lazy {
        hashMapOf<String, Any>(
            "page" to page,
            "pagesize" to Constants.PAGESIZE
        )
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        mPresenter.myBillList(params)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.myBillList(params)
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    //    candyCount
    var mycandy = 0
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshMyCandyEvent(event: RefreshMyCandyEvent) {
        mycandy -= event.candyCount
        candyCount.text = CommonFunction.num2thousand("$mycandy")

    }


    override fun onLazyClick(v: View) {
        when (v.id) {

            R.id.btnBack -> {
                finish()
            }
            R.id.rechargeCandy -> {//充值
                CommonFunction.gotoCandyRecharge(this)
            }
            R.id.withdrawCandy -> {//提现
                WithdrawCandyDialog(this).show()
            }
        }
    }

}
