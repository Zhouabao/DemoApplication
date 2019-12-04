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
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.presenter.ModelAboutMePresenter
import com.sdy.jitangapplication.presenter.view.ModelAboutMeView
import com.sdy.jitangapplication.ui.adapter.ModelLabelIntroduceAdapter
import kotlinx.android.synthetic.main.activity_model_label_introduce.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.jetbrains.anko.startActivityForResult

/**
 * 标签介绍
 */
class ModelLabelIntroduceActivity : BaseMvpActivity<ModelAboutMePresenter>(), ModelAboutMeView, OnRefreshListener,
    OnLoadMoreListener {


    companion object {
        const val FROM_ME = 1
        const val FROM_LABEL = 2
    }

    private var page = 1
    private val modelMeAdapter by lazy { ModelLabelIntroduceAdapter() }
    private val from by lazy { intent.getIntExtra("from", FROM_ME) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_label_introduce)
        initView()

        mPresenter.getTagTraitInfo(
            hashMapOf<String, Any>(
                "tag_id" to intent.getIntExtra("tag_id", 0),
                "type" to MyLabelQualityActivity.TYPE_MODEL
            )
        )
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

            mPresenter.getTagTraitInfo(
                hashMapOf<String, Any>(
                    "tag_id" to intent.getIntExtra("tag_id", 0),
                    "type" to MyLabelQualityActivity.TYPE_MODEL
                )
            )
        }

        modelRefresh.setOnRefreshListener(this)
        modelRefresh.setOnLoadMoreListener(this)
        modelRefresh.setEnableLoadMore(true)


        modelMeRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        modelMeRv.adapter = modelMeAdapter
        modelMeAdapter.setOnItemClickListener { _, view, position ->
            startActivityForResult<ModelAboutMeDetailActivity>(
                100,
                "content" to modelMeAdapter.data[position],
                "from" to from
            )
        }
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        mPresenter.getSignTemplate(page)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        modelMeAdapter.data.clear()
        modelRefresh.resetNoMoreData()
        when (from) {
            FROM_ME -> {
                page = 1
                mPresenter.getSignTemplate(page)
            }
            FROM_LABEL -> {
                mPresenter.getTagTraitInfo(
                    hashMapOf<String, Any>(
                        "tag_id" to intent.getIntExtra("tag_id", 0),
                        "type" to MyLabelQualityActivity.TYPE_MODEL
                    )
                )
            }
        }

    }


    override fun getSignTemplateResult(code: Int, result: MutableList<LabelQualityBean>?) {
        modelRefresh.finishRefresh(code == 200)
        modelRefresh.finishLoadMore(code == 200)
        if (code == 200 && result.isNullOrEmpty())
            modelRefresh.finishLoadMoreWithNoMoreData()
        modelStateView.viewState = MultiStateView.VIEW_STATE_CONTENT
        modelMeAdapter.addData(result ?: mutableListOf())
    }


    override fun getTagTraitInfoResult(b: Boolean, mutableList: MutableList<LabelQualityBean>?) {
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
