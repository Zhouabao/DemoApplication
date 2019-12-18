package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.BlockDataEvent
import com.sdy.jitangapplication.model.Photos
import com.sdy.jitangapplication.presenter.BlockSquarePresenter
import com.sdy.jitangapplication.presenter.view.BlockSquareView
import com.sdy.jitangapplication.ui.activity.SquareCommentDetailActivity
import com.sdy.jitangapplication.ui.adapter.BlockAdapter
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_block_square.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 九宫格的广场内容
 */
class BlockSquareFragment(var targetAccid: String) : BaseMvpLazyLoadFragment<BlockSquarePresenter>(), BlockSquareView,
    OnLoadMoreListener {
    private var page = 1
    private val blockAdapter by lazy { BlockAdapter() }
    private val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "page" to page,
            "target_accid" to targetAccid
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_block_square, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    private fun initView() {
        mPresenter = BlockSquarePresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!
        refreshBlock.setOnLoadMoreListener(this)

        EventBus.getDefault().register(this)

        blockRv.layoutManager = GridLayoutManager(activity, 3, RecyclerView.VERTICAL, false)
        blockRv.addItemDecoration(
            DividerItemDecoration(
                activity!!,
                DividerItemDecoration.BOTH_SET,
                SizeUtils.dp2px(10F),
                resources.getColor(R.color.colorWhite)
            )
        )
        blockRv.adapter = blockAdapter

        blockAdapter.setOnItemClickListener { _, view, position ->
            SquareCommentDetailActivity.start(activity!!, squareId = blockAdapter.data[position].square_id)
        }

        stateBlock.retryBtn.onClick {
            stateBlock.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.squarePhotosList(params)
        }

    }

    override fun loadData() {
        initView()
        mPresenter.squarePhotosList(params)
    }


    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.squarePhotosList(params)
    }


    override fun getBlockSquareResult(success: Boolean, data: MutableList<Photos>?) {
        if (success) {
            stateBlock.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (data.isNullOrEmpty()) {
                if (blockAdapter.data.isNullOrEmpty()) {
                    blockAdapter.setEmptyView(R.layout.empty_layout_block, blockRv)
                    stateBlock.viewState = MultiStateView.VIEW_STATE_EMPTY

                }
                refreshBlock.finishLoadMoreWithNoMoreData()
            } else {
                refreshBlock.finishLoadMore(true)
            }
            blockAdapter.addData(data ?: mutableListOf())
        } else {
            if (page == 1)
                stateBlock.viewState = MultiStateView.VIEW_STATE_ERROR
            else
                refreshBlock.finishLoadMore(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onBlockDataEvent(event: BlockDataEvent) {
        targetAccid = event.targetAccid
        params["target_accid"] = targetAccid
        if (event.refresh) {
            blockAdapter.data.clear()
            refreshBlock.setNoMoreData(false)
            page = 1
            mPresenter.squarePhotosList(params)
        }
    }
}
