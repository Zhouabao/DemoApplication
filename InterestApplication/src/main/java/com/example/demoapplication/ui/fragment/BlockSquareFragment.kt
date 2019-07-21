package com.example.demoapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapplication.R
import com.example.demoapplication.event.BlockDataEvent
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.model.Photos
import com.example.demoapplication.presenter.BlockSquarePresenter
import com.example.demoapplication.presenter.view.BlockSquareView
import com.example.demoapplication.ui.adapter.BlockAdapter
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import kotlinx.android.synthetic.main.fragment_block_square.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 九宫格的广场内容
 */
class BlockSquareFragment : BaseMvpFragment<BlockSquarePresenter>(), BlockSquareView, OnLoadMoreListener {
    private var page = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_block_square, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    private val blockAdapter by lazy { BlockAdapter() }

    private fun initView() {
        mPresenter = BlockSquarePresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!
        refreshBlock.setOnLoadMoreListener(this)

        EventBus.getDefault().register(this)

        blockRv.layoutManager = GridLayoutManager(activity, 3, RecyclerView.VERTICAL, false)
        blockRv.adapter = blockAdapter

    }


    //用户数据源
    var userList: MutableList<MatchBean> = mutableListOf()

    private fun initData() {
        blockAdapter.setNewData(
            mutableListOf(
                Photos(
                    1,
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg"
                ),
                Photos(
                    1,
                    "http://rsrc1.futrueredland.com.cn/ppns/headImage/0ca42c0d253ebee3f2bb197fbfcc5527/1562740286/0uBnhoxs4yRnWl39"
                ),
                Photos(
                    1,
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/0ca42c0d253ebee3f2bb197fbfcc5527/1562759634820/1pw367w0qfwtuwm0.jpg"
                ),
                Photos(
                    1,
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg"
                ),
                Photos(
                    1,
                    "http://rsrc1.futrueredland.com.cn/ppns/headImage/0ca42c0d253ebee3f2bb197fbfcc5527/1562740286/0uBnhoxs4yRnWl39"
                ),
                Photos(
                    1,
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/0ca42c0d253ebee3f2bb197fbfcc5527/1562759634820/1pw367w0qfwtuwm0.jpg"
                )
            )
        )
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        mPresenter.squarePhotosList(params)
    }


    override fun getBlockSquareResult(success: Boolean, data: MutableList<Photos>?) {
        if (success) {
            if (data.isNullOrEmpty()) {
                refreshBlock.finishLoadMoreWithNoMoreData()
            }
            blockAdapter.addData(data?: mutableListOf())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

    }

    //todo 此处刷新通过发送bus来进行
    private val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "page" to page,
            "target_accid" to targetAccid
        )
    }
    private var targetAccid = ""
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBlockDataEvent(event: BlockDataEvent) {
        targetAccid = event.targetAccid
        if (event.refresh) {
            blockAdapter.data.clear()
            refreshBlock.setNoMoreData(false)
            page = 1
            mPresenter.squarePhotosList(params)
        }
    }
}
