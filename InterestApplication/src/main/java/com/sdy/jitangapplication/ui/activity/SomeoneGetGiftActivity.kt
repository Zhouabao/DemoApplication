package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.SpanUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SomeoneGiftBean
import com.sdy.jitangapplication.presenter.SomeoneGetGiftPresenter
import com.sdy.jitangapplication.presenter.view.SomeoneGetGiftView
import com.sdy.jitangapplication.ui.adapter.ReceiveGiftAdapter
import kotlinx.android.synthetic.main.activity_someone_get_gift.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 某人获取的礼物列表
 */
class SomeoneGetGiftActivity : BaseMvpActivity<SomeoneGetGiftPresenter>(), SomeoneGetGiftView {

    private val target_accid by lazy { intent.getStringExtra("target_accid") }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_someone_get_gift)
        initView()
        mPresenter.getSomeoneGiftList(target_accid)
    }

    private val receiveGiftAdapter by lazy { ReceiveGiftAdapter() }

    private fun initView() {
        mPresenter = SomeoneGetGiftPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        btnBack.onClick {
            finish()
        }
        hotT1.text = "礼物墙"
        stateGift.retryBtn.onClick {
            stateGift.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getSomeoneGiftList(target_accid)
        }

        rvGift.layoutManager = GridLayoutManager(this, 4)
        rvGift.adapter = receiveGiftAdapter
    }

    override fun onGetSomeoneGiftList(success: Boolean, data: SomeoneGiftBean?) {
        if (success) {
            stateGift.viewState = MultiStateView.VIEW_STATE_CONTENT
            candyCount.text = SpanUtils.with(candyCount)
                .append("价值糖果量：")
                .append("${data?.amount}")
                .setBold()
                .setForegroundColor(Color.parseColor("#FF191919"))
                .create()
            receiveGiftAdapter.addData(data?.list ?: mutableListOf())
        } else {
            stateGift.viewState = MultiStateView.VIEW_STATE_ERROR
        }

    }
}
