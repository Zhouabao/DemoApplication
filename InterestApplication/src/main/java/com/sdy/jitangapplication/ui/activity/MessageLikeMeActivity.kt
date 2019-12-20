package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.UpdateLikemeEvent
import com.sdy.jitangapplication.event.UpdateLikemeOnePosEvent
import com.sdy.jitangapplication.model.LikeMeListBean
import com.sdy.jitangapplication.presenter.MessageLikeMePresenter
import com.sdy.jitangapplication.presenter.view.MessageLikeMeView
import com.sdy.jitangapplication.ui.adapter.LikeMeAdapter
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_message_like_me.*
import kotlinx.android.synthetic.main.empty_layout.view.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.error_layout.view.emptyImg
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 * 对我感兴趣的
 */
class MessageLikeMeActivity : BaseMvpActivity<MessageLikeMePresenter>(), MessageLikeMeView, OnRefreshListener,
    OnLoadMoreListener, View.OnClickListener {


    private var page = 1
    private val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "page" to page,
            "pagesize" to Constants.PAGESIZE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_like_me)
        initView()
        //获取喜欢我的列表
        mPresenter.likeLists(params)
        stateview.postDelayed({
            //标记已读
            mPresenter.markLikeRead(params)
        }, 100)
    }

    private val adapter by lazy { LikeMeAdapter() }

    private fun initView() {
        EventBus.getDefault().register(this)
        btnBack.onClick {
            onBackPressed()
        }
        hotT1.text = "对我感兴趣的"

        lockToSee.setOnClickListener(this)
        lockToSee.isVisible = !adapter.freeShow
        mPresenter = MessageLikeMePresenter()
        mPresenter.mView = this
        mPresenter.context = this

        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)
        refreshLayout.setEnableAutoLoadMore(true)

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.likeLists(params)
        }

        likeMeRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        likeMeRv.adapter = adapter
        adapter.setEmptyView(R.layout.empty_layout, likeMeRv)
        adapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_like_me)
        adapter.emptyView.emptyTip.text = "更新下个人信息，会有人出现的"


        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.likeMeCount -> {
                    startActivity<MessageLikeMeOneDayActivity>(
                        "date" to "${adapter.data[position].date}",
                        "count" to adapter.data[position].count,
                        "hasread" to adapter.data[position].hasread,
                        "freeShow" to adapter.freeShow,
                        "my_percent_complete" to adapter.my_percent_complete,
                        "normal_percent_complete" to adapter.normal_percent_complete,
                        "myCount" to adapter.myCount,
                        "maxCount" to adapter.maxCount
                    )
                }
            }
        }
    }


    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.likeLists(params)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        adapter.data.clear()
        refreshLayout.setNoMoreData(false)
        page = 1
        params["page"] = page
        mPresenter.likeLists(params)
    }

    override fun onLikeListsResult(data: LikeMeListBean) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        adapter.freeShow = data.free_show
        adapter.my_percent_complete = data.my_percent_complete
        adapter.normal_percent_complete = data.normal_percent_complete
        adapter.addData(data.list ?: mutableListOf())
        lockToSee.isVisible = !adapter.freeShow && adapter.data.size > 0
        if (adapter.data.size < Constants.PAGESIZE * page) {
            refreshLayout.finishLoadMoreWithNoMoreData()
        } else {
            refreshLayout.finishLoadMore(true)
        }
        refreshLayout.finishRefresh()
    }

    override fun onError(text: String) {
        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
            getString(R.string.retry_load_error)
        } else {
            getString(R.string.retry_net_error)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.lockToSee -> {
                ChargeVipDialog(ChargeVipDialog.LIKED_ME, this).show()
            }
        }


    }

    override fun onBackPressed() {

        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            adapter.data.clear()
            refreshLayout.setNoMoreData(false)

            page = 1
            params["page"] = page
            mPresenter.likeLists(params)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateLikemeEvent(event: UpdateLikemeEvent) {
        adapter.data.clear()
        refreshLayout.setNoMoreData(false)
        page = 1
        params["page"] = page
        mPresenter.likeLists(params)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateLikemeOnePosEvent(event: UpdateLikemeOnePosEvent) {
        if (event.parPos != -1 && event.childPos != -1) {
            adapter.data[event.parPos].list?.get(event.childPos)?.isfriend = 1
            adapter.notifyDataSetChanged()
        }
    }


}
