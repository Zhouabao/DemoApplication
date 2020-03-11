package com.sdy.jitangapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.SquareListBean
import com.sdy.jitangapplication.presenter.SquarePresenter
import com.sdy.jitangapplication.presenter.view.SquareView
import com.sdy.jitangapplication.ui.adapter.RecommendSquareAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_recommend_square.*

/**
 * 推荐
 */
class RecommendSquareFragment : BaseMvpLazyLoadFragment<SquarePresenter>(), SquareView,
    OnRefreshListener, OnLoadMoreListener {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recommend_square, container, false)
    }

    //请求广场的参数 TODO要更新tagid
    private val params by lazy {
        hashMapOf(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "page" to 1,
            "pagesize" to Constants.PAGESIZE,
            "type" to 1
        )
    }
    private val adapter by lazy { RecommendSquareAdapter() }
    override fun loadData() {
        initView()
        mPresenter.getSquareList(params, true)
    }

    private fun initView() {
        mPresenter = SquarePresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        stateRecommendSquare.retryBtn.onClick {
            //todo 重新连接网络
        }
        refreshRecommendSquare.setOnRefreshListener(this)
        refreshRecommendSquare.setOnLoadMoreListener(this)

        //android 瀑布流
        rvRecommendSquare.setHasFixedSize(true)
        rvRecommendSquare.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        rvRecommendSquare.adapter = adapter
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        mPresenter.getSquareList(params, true)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        mPresenter.getSquareList(params, true)
    }

    override fun onGetSquareListResult(data: SquareListBean?, result: Boolean, isRefresh: Boolean) {
        adapter.addData(data?.list ?: mutableListOf())
//        if (refreshRecommendSquare.state == RefreshState.Refreshing) {
        refreshRecommendSquare.finishRefresh()
        refreshRecommendSquare.finishLoadMore()
//        }
    }

    override fun onCheckBlockResult(result: Boolean) {
    }

    override fun onGetSquareLikeResult(position: Int, result: Boolean) {
    }

    override fun onGetSquareCollectResult(position: Int, result: BaseResp<Any?>?) {
    }

    override fun onGetSquareReport(baseResp: BaseResp<Any?>?, position: Int) {
    }

    override fun onRemoveMySquareResult(result: Boolean, position: Int) {
    }

    override fun onSquareAnnounceResult(type: Int, b: Boolean, code: Int) {
    }

    override fun onQnUploadResult(b: Boolean, type: Int, key: String?) {
    }


}
