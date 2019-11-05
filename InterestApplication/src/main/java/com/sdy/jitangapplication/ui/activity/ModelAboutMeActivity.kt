package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.ModelAboutBean
import com.sdy.jitangapplication.presenter.ModelAboutMePresenter
import com.sdy.jitangapplication.presenter.view.ModelAboutMeView
import com.sdy.jitangapplication.ui.adapter.ModelMeAdapter
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_model_about_me.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivityForResult

/**
 * 关于我范本
 */
class ModelAboutMeActivity : BaseMvpActivity<ModelAboutMePresenter>(), ModelAboutMeView, OnRefreshListener,
    OnLoadMoreListener {

    private var page = 1
    private val modelMeAdapter by lazy { ModelMeAdapter() }

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
        hotT1.text = "示例范本"

        modelRefresh.setOnRefreshListener(this)
        modelRefresh.setOnLoadMoreListener(this)

        modelMeRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        modelMeRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.HORIZONTAL_LIST,
                SizeUtils.dp2px(8F),
                Color.parseColor("#FFF1F2F6")
            )
        )
        modelMeRv.adapter = modelMeAdapter
        modelMeAdapter.setOnItemClickListener { _, view, position ->
            startActivityForResult<ModelAboutMeDetailActivity>(100, "content" to modelMeAdapter.data[position])
        }
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        mPresenter.getSignTemplate(page)

    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        modelMeAdapter.data.clear()
        modelRefresh.resetNoMoreData()
        mPresenter.getSignTemplate(page)
    }


    override fun getSignTemplateResult(code: Int, result: MutableList<ModelAboutBean>?) {
        modelRefresh.finishRefresh(code == 200)
        modelRefresh.finishLoadMore(code == 200)
        if (code == 200 && result.isNullOrEmpty())
            modelRefresh.finishLoadMoreWithNoMoreData()
        modelStateView.viewState = MultiStateView.VIEW_STATE_CONTENT
        modelMeAdapter.addData(result ?: mutableListOf())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }
}
