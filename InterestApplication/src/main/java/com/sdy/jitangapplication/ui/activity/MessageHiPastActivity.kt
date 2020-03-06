package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.GetNewMsgEvent
import com.sdy.jitangapplication.event.UpdateHiEvent
import com.sdy.jitangapplication.model.HiMessageBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.nim.attachment.ShareSquareAttachment
import com.sdy.jitangapplication.presenter.MessageHiPresenter
import com.sdy.jitangapplication.presenter.view.MessageHiView
import com.sdy.jitangapplication.ui.adapter.MessageHiListAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_message_hi_past.*
import kotlinx.android.synthetic.main.empty_friend_layout.view.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 过往招呼
 */
class MessageHiPastActivity : BaseMvpActivity<MessageHiPresenter>(), MessageHiView, OnLoadMoreListener,
    OnRefreshListener {

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
        setContentView(R.layout.activity_message_hi_past)
        EventBus.getDefault().register(this)
        registerObservers(true)
        initView()
        //主动刷新列表
        mPresenter.greatLists(params)
//        setData() 模拟数据请求
    }


    private val adapter by lazy { MessageHiListAdapter() }
    private fun initView() {
        btnBack.onClick {
            finish()
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
        adapter.setEmptyView(R.layout.empty_friend_layout, messageHiRv)
        adapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_hi_received_bg)
        adapter.emptyView.emptyFriendTitle.text = "这里暂时没有人"
        adapter.emptyView.emptyFriendTip.text = "左滑过的用户会在这里记录"
        adapter.isUseEmpty(false)

        adapter.setOnItemClickListener { _, view, position ->
            //发送通知已读消息
            adapter.data[position].count = 0
            NIMClient.getService(MsgService::class.java)
                .clearUnreadCount(adapter.data[position].accid, SessionTypeEnum.P2P)
            EventBus.getDefault().post(GetNewMsgEvent())
            // 通知中的 RecentContact 对象的未读数为0
            ChatActivity.start(this, adapter.data[position].accid ?: "")
            adapter.notifyItemChanged(position)
        }

        clearBtn.onClick {
            for (data in adapter.data) {
                data.count = 0
            }
            NIMClient.getService(MsgService::class.java).clearAllUnreadCount()
            adapter.notifyDataSetChanged()
        }
    }


    override fun finish() {
        setResult(Activity.RESULT_OK)
        super.finish()
    }

    override fun onGreatListResult(t: BaseResp<MutableList<HiMessageBean>?>) {
        //获取最近联系人列表
        mPresenter.getRecentContacts(t)
    }

    override fun onGetRecentContactResults(
        contacts: MutableList<RecentContact>,
        t: BaseResp<MutableList<HiMessageBean>?>
    ) {
        if (t.data.isNullOrEmpty()) {
            refreshLayout.finishLoadMoreWithNoMoreData()
        } else {
            refreshLayout.finishLoadMore(true)
        }
        if (refreshLayout.state == RefreshState.Refreshing) {
            adapter.data.clear()
            refreshLayout.finishRefresh(true)
        }
        for (recentContactt in contacts) {
            for (data in t.data ?: mutableListOf()) {
                if (recentContactt.contactId == data.accid) {
                    if (recentContactt.attachment is ChatHiAttachment) {
                        data.content =
                            if ((recentContactt.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_HI) {
                                "『招呼消息』"
                            } else if ((recentContactt.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_MATCH) {
//                                "通过『" + (recentContactt.getAttachment() as ChatHiAttachment).tag + "』匹配"
                                "『匹配消息』"
                            } else if ((recentContactt.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_RFIEND) {
                                "『好友消息』"
                            } else if ((recentContactt.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_OUTTIME) {
                                "『消息过期』"
                            } else {
                                ""
                            }
                    } else if (recentContactt.attachment is ShareSquareAttachment) {
                        data.content = "『动态分享内容』"
                    } else {
                        data.content = recentContactt.content
                    }
                    data.count = recentContactt.unreadCount
                }
            }
        }
        adapter.addData(t.data ?: mutableListOf())
//        adapter.notifyDataSetChanged()
        adapter.isUseEmpty(adapter.data.isEmpty())
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT

    }


    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
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

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        registerObservers(false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateHiEvent(event: UpdateHiEvent) {
//        stateview.viewState = MultiStateView.VIEW_STATE_LOADING
//        refreshLayout.resetNoMoreData()
//        page = 1
//        params["page"] = page
//        adapter.data.clear()
//        adapter.notifyDataSetChanged()
//        mPresenter.greatLists(params)
    }


    /**
     * ********************** 收消息，处理状态变化 ************************
     */
    private fun registerObservers(register: Boolean) {
        NIMClient.getService(MsgServiceObserve::class.java).observeReceiveMessage(messageReceiverObserver, register)
    }


    //监听在线消息中是否有@我
    private val messageReceiverObserver =
        Observer<List<IMMessage>> { imMessages ->
            if (imMessages != null) {
                for (contact in adapter.data.withIndex()) {
                    for (imMessage in imMessages) {
                        if (contact.value.accid == imMessage.fromAccount) {
                            if (imMessage.attachment is ChatHiAttachment) {
                                contact.value.content =
                                    if ((imMessage.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_HI) {
                                        "[招呼消息]"
                                    } else if ((imMessage.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_MATCH) {
                                        "[匹配消息]"
                                    } else if ((imMessage.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_RFIEND) {
                                        "[好友消息]"
                                    } else if ((imMessage.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_OUTTIME) {
                                        "[消息过期]"
                                    } else {
                                        ""
                                    }
                            } else if (imMessage.attachment is ShareSquareAttachment) {
                                contact.value.content = "[动态分享内容]"
                            } else {
                                contact.value.content = imMessage.content
                            }
                            contact.value.count = contact.value.count + 1
                            contact.value.msgTime = imMessage.time
                            adapter.notifyItemChanged(contact.index)
                        }
                    }
                }
            }
        }

}
