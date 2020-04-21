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
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.RefreshMyCandyEvent
import com.sdy.jitangapplication.event.UpdateWantStateEvent
import com.sdy.jitangapplication.model.GoodsCategoryBeans
import com.sdy.jitangapplication.model.ProductBean
import com.sdy.jitangapplication.model.PullWithdrawBean
import com.sdy.jitangapplication.presenter.MyCandyPresenter
import com.sdy.jitangapplication.presenter.view.MyCandyView
import com.sdy.jitangapplication.ui.adapter.CandyProductAdapter
import com.sdy.jitangapplication.ui.dialog.RechargeCandyDialog
import com.sdy.jitangapplication.ui.dialog.WithdrawCandyDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_my_candy.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity


/**
 * 我的糖果中心
 */
class MyCandyActivity : BaseMvpActivity<MyCandyPresenter>(), MyCandyView, View.OnClickListener,
    OnLoadMoreListener {

    private val candyProductAdapter by lazy { CandyProductAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_candy)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            BarUtils.setStatusBarVisibility(this, false)
        }
        initView()
        mPresenter.myCadny()
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = MyCandyPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        btnBack.setOnClickListener(this)
        withdrawCandy.setOnClickListener(this)
        rechargeCandy.setOnClickListener(this)
        candyRecordBtn.setOnClickListener(this)
        allProductBtn.setOnClickListener(this)

        refreshMyCandy.setOnLoadMoreListener(this)
        stateMyCandy.retryBtn.onClick {
            stateMyCandy.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.goodsCategoryList(params)
        }
        productRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        productRv.adapter = candyProductAdapter
        candyProductAdapter.setOnItemClickListener { _, view, position ->
            startActivity<CandyProductDetailActivity>("id" to candyProductAdapter.data[position].id)
        }


        guideCandyList.onClick {
            guideCandyList.isVisible = false
            guideCandyAll.isVisible = true
        }
        guideCandyAll.onClick {
            guideCandyAll.isVisible = false
            if (isWithdraw)
                guideCandyWithdraw.isVisible = true
            else
                guideCandyRecord.isVisible = true
        }
        guideCandyWithdraw.onClick {
            guideCandyWithdraw.isVisible = false
            guideCandyRecord.isVisible = true
        }
        guideCandyRecord.onClick {
            guideCandyRecord.isVisible = false
            UserManager.saveShowGuideCandy(true)
        }

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.allProductBtn -> {
                startActivity<CandyMallActivity>()
            }
            R.id.btnBack -> {
                finish()
            }
            R.id.candyRecordBtn -> { //交易记录
                startActivity<CandyRecordActivity>()
                candyNewRecord.isVisible = false
            }
            R.id.rechargeCandy -> {//充值
                RechargeCandyDialog(this).show()
            }
            R.id.withdrawCandy -> {//提现
                WithdrawCandyDialog(this).show()
            }
        }

    }

    override fun ongoodsCategoryList(success: Boolean, data: GoodsCategoryBeans?) {
        if (success) {
            stateMyCandy.viewState = MultiStateView.VIEW_STATE_CONTENT
            candyProductAdapter.addData(data?.list ?: mutableListOf())
            if ((data?.list ?: mutableListOf<ProductBean>()).size < Constants.PAGESIZE) {
                refreshMyCandy.finishLoadMoreWithNoMoreData()
            } else {
                refreshMyCandy.finishLoadMore(true)
            }
        } else {
            stateMyCandy.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }

    private var isWithdraw = false
    override fun onMyCadnyResult(candyCoun: PullWithdrawBean?) {
        if (candyCoun != null) {
            candyProductAdapter.mycandy = candyCoun!!.candy_amount
            candyCount.text = CommonFunction.num2thousand("${candyCoun.candy_amount}")
            candyNewRecord.isVisible = candyCoun.has_unread
            withdrawCandy.isVisible = candyCoun.is_withdraw
            isWithdraw = candyCoun.is_withdraw
            guideCandyList.isVisible = !UserManager.isShowGuideCandy()

        }
        mPresenter.goodsCategoryList(params)
    }

    private var page = 1
    private val params by lazy {
        hashMapOf<String, Any>(
            "page" to page,
            "pagesize" to Constants.PAGESIZE
        )
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.goodsCategoryList(params)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    //    candyCount
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshMyCandyEvent(event: RefreshMyCandyEvent) {
        if (event.candyCount >= 0) {
            candyCount.text =
                CommonFunction.num2thousand("${candyProductAdapter.mycandy - event.candyCount}")

            candyProductAdapter.mycandy = candyProductAdapter.mycandy - event.candyCount
            candyProductAdapter.notifyDataSetChanged()
        } else {
            mPresenter.myCadny()
            candyProductAdapter.data.clear()
            page = 1
            params["page"] = page
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateWantStateEvent(event: UpdateWantStateEvent) {
        for (data in candyProductAdapter.data.withIndex()) {
            if (data.value.id == event.id) {
                data.value.is_wished = event.want
                candyProductAdapter.notifyItemChanged(data.index)
                break
            }
        }
    }

}
