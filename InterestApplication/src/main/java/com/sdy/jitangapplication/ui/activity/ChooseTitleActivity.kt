package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import com.google.android.flexbox.*
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
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.presenter.ChooseTitlePresenter
import com.sdy.jitangapplication.presenter.view.ChooseTitleView
import com.sdy.jitangapplication.ui.adapter.ChooseTitleAdapter
import kotlinx.android.synthetic.main.activity_choose_title.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 发布选择标题
 */
class ChooseTitleActivity : BaseMvpActivity<ChooseTitlePresenter>(), ChooseTitleView,
    View.OnClickListener,
    OnRefreshListener, OnLoadMoreListener {


    private val adapter by lazy { ChooseTitleAdapter() }
    private var limitCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_title)
        initView()
        mPresenter.getTagTitleList(page)
    }


    private fun initView() {
        mPresenter = ChooseTitlePresenter()
        mPresenter.mView = this
        mPresenter.context = this

        refreshTitle.setOnLoadMoreListener(this)
        refreshTitle.setOnRefreshListener(this)

        btnBack.setOnClickListener(this)
        hotT1.text = getString(R.string.label_title_hot)
        rightBtn.setOnClickListener(this)
        rightBtn.text = getString(R.string.complete)
        rightBtn.setTextColor(Color.parseColor("#FFFF6318"))
        rightBtn.isVisible = true

//        stateTitle
        stateTitle.retryBtn.onClick {
            stateTitle.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getTagTitleList(page)
        }
        val manager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        manager.justifyContent = JustifyContent.FLEX_START
        titleRv.layoutManager = manager
        titleRv.adapter = adapter
        adapter.setOnItemClickListener { _, view, position ->
            if (!adapter.data[position].isfuse) {
                var chooseCount = 0
                for (data in adapter.data) {
                    if (data.isfuse) {
                        chooseCount++
                    }
                }
                if (titleEt.text.trim().toString().isNotEmpty()) {
                    chooseCount++
                }
                if (chooseCount >= limitCount) {
                    CommonFunction.toast(
                        "${getString(R.string.lable_title_most)}" +
                                "$limitCount" +
                                "${getString(R.string.label_title_most_2)}"
                    )
                    return@setOnItemClickListener
                }
            }

            adapter.data[position].isfuse = !adapter.data[position].isfuse
            adapter.notifyItemChanged(position)
        }



        titleEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                var chooseCount = 0
                for (data in adapter.data) {
                    if (data.isfuse) {
                        chooseCount++
                    }
                }
                if (chooseCount >= limitCount) {
                    CommonFunction.toast(
                        "${getString(R.string.lable_title_most)}${limitCount}${getString(
                            R.string.label_title_most_2
                        )}"
                    )
                    titleEt.text.clear()
                } else
                    titleEt.setSelection(titleEt.text.length)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty() && s.length > 12) {
                    titleEt.setText(s.subSequence(0, 12))
                    CommonFunction.toast(getString(R.string.label_title_max_length))
                    return
                }
            }

        })

    }


    override fun onClick(view: View) {
        when (view) {
            btnBack -> {
                finish()
            }
            rightBtn -> {
                val topics = ArrayList<String>()
                if (titleEt.text.trim().isNotEmpty()) {
                    topics.add(titleEt.text.trim().toString())
                }
                for (data in adapter.data) {
                    if (data.isfuse) {
                        topics.add(data.content)
                    }
                }
                intent.putExtra("title", topics)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    private var page = 1


    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        mPresenter.getTagTitleList(page)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        mPresenter.getTagTitleList(page)
    }


    override fun getTagTraitInfoResult(
        b: Boolean,
        data: MutableList<LabelQualityBean>?,
        maxCount: Int
    ) {
        limitCount = maxCount
        if (refreshTitle.state == RefreshState.Refreshing) {
            adapter.data.clear()
            refreshTitle.finishRefresh(b)
        } else {
            if (data?.size ?: 0 < Constants.PAGESIZE)
                refreshTitle.finishLoadMoreWithNoMoreData()
            else
                refreshTitle.finishLoadMore(b)
        }

        stateTitle.viewState = if (b) {
            MultiStateView.VIEW_STATE_CONTENT
        } else {
            MultiStateView.VIEW_STATE_ERROR
        }

        if (b && !data.isNullOrEmpty()) {
            val topics = intent.getStringArrayListExtra("title")
//            for (topic in topics) {
//                for (topic1 in data) {
//                    if (topic1.content == topic) {
//                        topic1.isfuse = true
//                    }
//                }
//            }
            adapter.addData(data)
        }
    }
}
