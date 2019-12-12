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
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.presenter.ModelAboutMePresenter
import com.sdy.jitangapplication.presenter.view.ModelAboutMeView
import com.sdy.jitangapplication.ui.adapter.ModelLabelIntroduceAdapter
import kotlinx.android.synthetic.main.activity_model_label_introduce.*
import kotlinx.android.synthetic.main.error_layout.view.*

/**
 * 标签介绍
 */
class ModelLabelIntroduceActivity : BaseMvpActivity<ModelAboutMePresenter>(), ModelAboutMeView, OnRefreshListener {


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
                "type" to LabelQualityActivity.TYPE_MODEL
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
                    "type" to LabelQualityActivity.TYPE_MODEL
                )
            )
        }

        modelRefresh.setOnRefreshListener(this)


        modelMeRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        modelMeRv.adapter = modelMeAdapter

    }


    override fun onRefresh(refreshLayout: RefreshLayout) {
        modelMeAdapter.data.clear()
        modelRefresh.resetNoMoreData()

        mPresenter.getTagTraitInfo(
            hashMapOf<String, Any>(
                "tag_id" to intent.getIntExtra("tag_id", 0),
                "type" to LabelQualityActivity.TYPE_MODEL
            )
        )

    }


    override fun getSignTemplateResult(code: Int, result: MutableList<LabelQualityBean>?) {
    }


    override fun getTagTraitInfoResult(b: Boolean, mutableList: MutableList<LabelQualityBean>?) {
        modelRefresh.finishRefresh(b)
        modelRefresh.finishLoadMore(b)
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
