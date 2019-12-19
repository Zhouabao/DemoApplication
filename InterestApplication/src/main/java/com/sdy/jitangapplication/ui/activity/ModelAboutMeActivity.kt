package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.presenter.ModelAboutMePresenter
import com.sdy.jitangapplication.presenter.view.ModelAboutMeView
import com.sdy.jitangapplication.ui.adapter.ModelLabelIntroduceAdapter
import kotlinx.android.synthetic.main.activity_model_about_me.*
import kotlinx.android.synthetic.main.error_layout.view.*

/**
 * 关于我范本
 */
class ModelAboutMeActivity : BaseMvpActivity<ModelAboutMePresenter>(), ModelAboutMeView, OnRefreshListener,
    OnLoadMoreListener, OnRefreshLoadMoreListener {

    private var page = 1
    private val modelMeAdapter by lazy { ModelLabelIntroduceAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_about_me)
        initView()
        mPresenter.getSignTemplate(page)
    }

    private fun initView() {
        mPresenter = ModelAboutMePresenter()
        mPresenter.mView = this
        mPresenter.context = this

        btnBack.onClick {
            finish()
        }

        modelStateView.retryBtn.onClick {
            modelStateView.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getSignTemplate(page)
        }

        modelRefresh.setOnRefreshLoadMoreListener(this)

        modelMeRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        modelMeRv.adapter = modelMeAdapter

    }


    override fun onRefresh(refreshLayout: RefreshLayout) {
        modelMeAdapter.data.clear()
        modelRefresh.resetNoMoreData()
        page = 1
        mPresenter.getSignTemplate(page)


    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        mPresenter.getSignTemplate(page)
    }


    override fun getSignTemplateResult(code: Int, result: MutableList<LabelQualityBean>?) {
    }


    override fun getTagTraitInfoResult(b: Boolean, mutableList: MutableList<LabelQualityBean>?) {
        if (modelRefresh.state == RefreshState.Refreshing) {
            modelRefresh.finishRefresh(b)
        } else {
            if (b && mutableList.isNullOrEmpty())
                modelRefresh.finishLoadMoreWithNoMoreData()
            else
                modelRefresh.finishLoadMore(b)
        }
        if (b) {
            modelStateView.viewState = MultiStateView.VIEW_STATE_CONTENT
            modelMeAdapter.addData(mutableList ?: mutableListOf())
        } else {
            modelStateView.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }
}
