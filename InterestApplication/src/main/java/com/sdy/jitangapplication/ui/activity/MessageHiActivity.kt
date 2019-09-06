package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.UpdateHiEvent
import com.sdy.jitangapplication.model.HiMessageBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.nim.attachment.ShareSquareAttachment
import com.sdy.jitangapplication.presenter.MessageHiPresenter
import com.sdy.jitangapplication.presenter.view.MessageHiView
import com.sdy.jitangapplication.ui.adapter.MessageHiListAdapter
import com.sdy.jitangapplication.utils.UserManager
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import kotlinx.android.synthetic.main.activity_message_hi.*
import kotlinx.android.synthetic.main.activity_message_hi.stateview
import kotlinx.android.synthetic.main.activity_message_list.btnBack
import kotlinx.android.synthetic.main.empty_layout.view.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 打招呼列表
 */
class MessageHiActivity : BaseMvpActivity<MessageHiPresenter>(), MessageHiView, OnLoadMoreListener, OnRefreshListener {

    private var page = 1
    private val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "pagesize" to Constants.PAGESIZE,
            "page" to page
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_hi)
        EventBus.getDefault().register(this)
        initView()

    }


    private val adapter by lazy { MessageHiListAdapter() }
    private fun initView() {
        btnBack.onClick {
            finish()
        }

        //删除过期消息
        tagAllRead.onClick {
            mPresenter.delTimeoutGreet(
                hashMapOf(
                    "token" to UserManager.getToken(),
                    "accid" to UserManager.getAccid()
                )
            )
        }

        mPresenter = MessageHiPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.greatLists(params)
        }

        refreshLayout.setOnLoadMoreListener(this)
        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setEnableAutoLoadMore(true)

        messageHiRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        messageHiRv.adapter = adapter
        adapter.bindToRecyclerView(messageHiRv)
        adapter.setEmptyView(R.layout.empty_layout, messageHiRv)
        adapter.emptyView.emptyTip.text = "还没有消息哦，不如主动出击？"

        adapter.setOnItemClickListener { _, view, position ->
            //发送通知告诉剩余时间，并且开始倒计时
            ChatActivity.start(this, adapter.data[position].accid ?: "")
        }
    }

    override fun onGreatListResult(t: BaseResp<MutableList<HiMessageBean>?>) {
        //获取最近联系人列表
        mPresenter.getRecentContacts(t)
    }

    override fun onDelTimeoutGreetResult(t: Boolean) {
        if (t)
            refreshLayout.autoRefresh()
        else
            ToastUtils.showShort("删除超时消息失败！")
    }

    override fun onGetRecentContactResults(
        contacts: MutableList<RecentContact>,
        t: BaseResp<MutableList<HiMessageBean>?>
    ) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        if (t.data.isNullOrEmpty()) {
            refreshLayout.finishLoadMoreWithNoMoreData()
        } else {
            refreshLayout.finishLoadMore(true)
        }
        refreshLayout.finishRefresh(true)

        for (recentContactt in contacts) {
            for (data in t.data ?: mutableListOf()) {
                if (recentContactt.contactId == data.accid) {
                    if (recentContactt.attachment is ChatHiAttachment) {
                        data.content =
                            if ((recentContactt.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_HI) {
                                "[招呼消息]"
                            } else if ((recentContactt.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_MATCH) {
                                "[匹配消息]"
                            } else if ((recentContactt.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_RFIEND) {
                                "[好友消息]"
                            } else if ((recentContactt.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_OUTTIME) {
                                "[消息过期]"
                            } else {
                                ""
                            }
                    } else if (recentContactt.attachment is ShareSquareAttachment) {
                        data.content = "[动态分享内容]"
                    } else {
                        data.content = recentContactt.content
                    }
                }
            }
        }
        adapter.addData(t.data ?: mutableListOf())


        if (adapter.data.isNullOrEmpty()) {
            tagAllRead.isEnabled = false
        } else {
            for (tempdata in t.data!!) {
                if (tempdata.type == 4) {
                    tagAllRead.isEnabled = true
                    break
                }
            }
        }

    }


    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        adapter.data.clear()
        mPresenter.greatLists(params)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.greatLists(params)
    }


    override fun onError(text: String) {
        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
            getString(R.string.retry_load_error)
        } else {
            getString(R.string.retry_net_error)
        }
    }

    override fun onResume() {
        super.onResume()
        //主动刷新列表
        refreshLayout.autoRefresh()
//        setData() 模拟数据请求
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        adapter.cancelAllTimers()//取消所有的定时器
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateHiEvent(event: UpdateHiEvent) {
        refreshLayout.autoRefresh()
    }


}
