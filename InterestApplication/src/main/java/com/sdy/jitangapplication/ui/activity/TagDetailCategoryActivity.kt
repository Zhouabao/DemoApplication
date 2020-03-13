package com.sdy.jitangapplication.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SizeUtils
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
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.SquareBannerBean
import com.sdy.jitangapplication.model.TagSquareListBean
import com.sdy.jitangapplication.presenter.TagDetailCategoryPresenter
import com.sdy.jitangapplication.presenter.view.TagDetailCategoryView
import com.sdy.jitangapplication.ui.adapter.RecommendSquareAdapter
import kotlinx.android.synthetic.main.activity_tag_detail.*
import kotlinx.android.synthetic.main.error_layout.view.*

/**
 * 标签或者话题分类详情
 *
 * 标签分类带了标签
 *
 * 话题分类带了话题
 *
 * type 1兴趣的 2话题
 */
class TagDetailCategoryActivity : BaseMvpActivity<TagDetailCategoryPresenter>(), TagDetailCategoryView,
    OnRefreshListener,
    OnLoadMoreListener {


    private var page: Int = 1
    private val adapter by lazy { RecommendSquareAdapter() }
    private val id by lazy { intent.getIntExtra("id", 0) }
    private val type by lazy { intent.getIntExtra("type", 0) }

    //请求广场的参数 TODO要更新tagid
    private val params by lazy {
        hashMapOf<String, Any>(
            "page" to page,
            "id" to id,
            "type" to type,
            "pagesize" to Constants.PAGESIZE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag_detail)
        initView()
        mPresenter.squareTagInfo(params)
    }

    private fun initView() {
        mPresenter = TagDetailCategoryPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        refreshSamePerson.setOnRefreshListener(this)
        refreshSamePerson.setOnLoadMoreListener(this)
        refreshSamePerson.setPrimaryColorsId(R.color.colorTransparent)
        stateSamePerson.retryBtn.onClick {
            stateSamePerson.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.squareTagInfo(params)
        }

        btnBack.onClick {
            finish()
        }
        btnBack1.onClick {
            finish()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (statusView.layoutParams as RelativeLayout.LayoutParams).height = BarUtils.getStatusBarHeight()
        } else {
            statusView.isVisible = false
        }

        rightBtn1.isVisible = true
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
                samePersonBg.isVisible = false
                btnBack.isVisible = false
            } else {
                btnBack.isVisible = true
                llSame.visibility = View.VISIBLE
                samePersonBg.isVisible = true
            }
        })


        samePersonRv.setHasFixedSize(true)
        val manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        manager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        samePersonRv.layoutManager = manager
        samePersonRv.adapter = adapter
        adapter.setOnItemClickListener { _, view, position ->
            view.isEnabled = false
            SquareCommentDetailActivity.start(this, squareId = adapter.data[position].id)
            view.postDelayed({
                view.isEnabled = true
            }, 1000L)
        }
    }

    private fun initData(bannerBean: SquareBannerBean) {
        GlideUtil.loadImgCenterCrop(this, bannerBean.icon, samePersonBg)
        samePersonTitle.text = bannerBean.title
        hotT1.text = bannerBean.title

    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        mPresenter.squareTagInfo(params)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        adapter.data.clear()
        mPresenter.squareTagInfo(params)
    }


    override fun onCheckBlockResult(b: Boolean) {
//        if (b)
//            startActivity<PublishActivity>("titleBean" to topicBean)
    }


    override fun onGetSquareRecommendResult(data: TagSquareListBean?, b: Boolean) {
        if (b) {
            stateSamePerson.viewState = MultiStateView.VIEW_STATE_CONTENT
        } else {
            stateSamePerson.viewState = MultiStateView.VIEW_STATE_ERROR
        }

        if (refreshSamePerson.state != RefreshState.Loading)
            data?.banner?.let { initData(it) }

        adapter.addData(data?.list ?: mutableListOf())
        refreshSamePerson.finishRefresh()
        refreshSamePerson.finishLoadMore()

    }

}