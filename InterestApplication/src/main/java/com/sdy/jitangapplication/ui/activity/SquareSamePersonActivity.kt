package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.android.material.appbar.AppBarLayout
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.CustomClickListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SamePersonListBean
import com.sdy.jitangapplication.model.TopicBean
import com.sdy.jitangapplication.presenter.SquareSamePersonPresenter
import com.sdy.jitangapplication.presenter.view.SquareSamePersonView
import com.sdy.jitangapplication.ui.adapter.SquareSamePersonAdapter
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_square_same_person.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.jetbrains.anko.startActivity

/**
 * 通过标题找到的同类人
 */
class SquareSamePersonActivity : BaseMvpActivity<SquareSamePersonPresenter>(), SquareSamePersonView, OnRefreshListener,
    OnLoadMoreListener {

    private var page: Int = 1
    private val topicBean by lazy { intent.getSerializableExtra("topicBean") as TopicBean }
    private val adapter by lazy { SquareSamePersonAdapter() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_square_same_person)
        initView()
        mPresenter.getTitleInfo(page, topicBean.id)
    }

    private fun initView() {
        mPresenter = SquareSamePersonPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        refreshSamePerson.setOnRefreshListener(this)
        refreshSamePerson.setOnLoadMoreListener(this)
        refreshSamePerson.setPrimaryColorsId(R.color.colorTransparent)

        stateSamePerson.retryBtn.onClick {
            stateSamePerson.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getTitleInfo(page, topicBean.id)
        }

        btnBack.onClick {
            finish()
        }
        (statusView.layoutParams as RelativeLayout.LayoutParams).height = BarUtils.getStatusBarHeight()
        llTitle.setBackgroundColor(Color.TRANSPARENT)
        rightBtn1.isVisible = true
        btnBack.setImageResource(R.drawable.icon_back_white)
        hotT1.setTextColor(Color.WHITE)
        rightBtn1.text = "发布"
        rightBtn1.setBackgroundResource(R.drawable.shape_rectangle_orange_25dp)

        rightBtn1.onClick(object : CustomClickListener() {
            override fun onSingleClick(view: View) {
                mPresenter.checkBlock()
            }
        })
        publish.onClick(object : CustomClickListener() {
            override fun onSingleClick(view: View) {
                mPresenter.checkBlock()
            }
        })

        sameAppbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { p0, verticalOffset ->
            llTitle.isVisible = Math.abs(verticalOffset) >= SizeUtils.dp2px(56F) + BarUtils.getStatusBarHeight()

            if (llTitle.isVisible) {
                llSame.visibility = View.INVISIBLE
                samePersonBgBlur.isVisible = true
                samePersonBg.isVisible = false
            } else {
                llSame.visibility = View.VISIBLE
                samePersonBgBlur.isVisible = false
                samePersonBg.isVisible = true
            }
        })

        samePersonRv.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        samePersonRv.adapter = adapter
        adapter.setOnItemClickListener { _, view, position ->
            view.isEnabled = false
            SquareCommentDetailActivity.start(this, squareId = adapter.data[position].id)
            view.postDelayed({
                view.isEnabled = true
            }, 1000L)
        }
        initData()
    }

    private fun initData() {
        GlideUtil.loadImgCenterCrop(this, topicBean.icon, samePersonBg)
        val transformation = MultiTransformation(
            CenterCrop(),
            BlurTransformation(14)
        )
        Glide.with(this)
            .load(topicBean.icon)
            .priority(Priority.LOW)
            .transform(transformation)
            .into(samePersonBgBlur)
        samePersonTitle.text = topicBean.title
        hotT1.text = topicBean.title

    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        mPresenter.getTitleInfo(page, topicBean.id)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        adapter.data.clear()
        mPresenter.getTitleInfo(page, topicBean.id)
    }

    override fun onGetTitleInfoResult(b: Boolean, data: SamePersonListBean?) {
        if (b) {
            stateSamePerson.viewState = MultiStateView.VIEW_STATE_CONTENT
            for (tdata in data?.list ?: mutableListOf()) {
                tdata.originalLike = tdata.isliked
            }
            adapter.addData(data?.list ?: mutableListOf())
            if (data?.people_cnt == 0) {
                samePersonCount.visibility = View.INVISIBLE
            } else {
                samePersonCount.visibility = View.VISIBLE
                samePersonCount.text = "${data?.people_cnt}人参与·${data?.square_cnt}条动态"
            }

        } else {
            stateSamePerson.viewState = MultiStateView.VIEW_STATE_ERROR
        }
        if (refreshSamePerson.state == RefreshState.Refreshing) {
            refreshSamePerson.finishRefresh(b)
            refreshSamePerson.resetNoMoreData()
        } else if (refreshSamePerson.state == RefreshState.Loading) {
            if (b && data?.list.isNullOrEmpty())
                refreshSamePerson.finishLoadMoreWithNoMoreData()
            else
                refreshSamePerson.finishLoadMore(b)
        }
    }

    override fun onCheckBlockResult(b: Boolean) {
        if (b)
            startActivity<PublishActivity>("titleBean" to topicBean)
    }

}
