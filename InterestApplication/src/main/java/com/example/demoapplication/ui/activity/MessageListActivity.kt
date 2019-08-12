package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapplication.R
import com.example.demoapplication.model.MessageListBean
import com.example.demoapplication.model.MessageListBean1
import com.example.demoapplication.nim.activity.ChatActivity
import com.example.demoapplication.presenter.MessageListPresenter
import com.example.demoapplication.presenter.view.MessageListView
import com.example.demoapplication.ui.adapter.MessageListAdapter
import com.example.demoapplication.ui.adapter.MessageListFriensAdapter
import com.example.demoapplication.ui.adapter.MessageListHeadAdapter
import com.example.demoapplication.utils.UserManager
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.api.model.contact.ContactChangedObserver
import com.netease.nim.uikit.api.model.main.OnlineStateChangeObserver
import com.netease.nim.uikit.api.model.user.UserInfoObserver
import com.netease.nim.uikit.business.recent.RecentContactsFragment.RECENT_TAG_STICKY
import com.netease.nim.uikit.business.recent.TeamMemberAitHelper
import com.netease.nim.uikit.common.CommonUtil
import com.netease.nim.uikit.common.ui.drop.DropCover
import com.netease.nim.uikit.common.ui.drop.DropManager
import com.netease.nim.uikit.impl.NimUIKitImpl
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import kotlinx.android.synthetic.main.activity_message_list.*
import kotlinx.android.synthetic.main.headerview_label.view.*
import org.jetbrains.anko.startActivity
import java.util.*

/**
 * 总体消息列表
 */
class MessageListActivity : BaseMvpActivity<MessageListPresenter>(), MessageListView {

    private var cached: MutableMap<String, RecentContact> = mutableMapOf() // 暂缓刷上列表的数据（未读数红点拖拽动画运行时用）

    private val params by lazy { hashMapOf("token" to UserManager.getToken(), "accid" to UserManager.getAccid()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)

        initView()
        registerObservers(true)
        registerDropCompletedListener(true)
        registerOnlineStateChangeListener(true)
        //获取最近消息
        mPresenter.messageCensus(params)
        //获取最近联系人列表
        mPresenter.getRecentContacts()
    }

    private val adapter by lazy { MessageListAdapter() }
    private fun initView() {
        mPresenter = MessageListPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        btnBack.onClick {
            finish()
        }
        contactBoockBtn.onClick {
            startActivity<ContactBookActivity>()
        }

        messageListRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        messageListRv.adapter = adapter
        adapter.bindToRecyclerView(messageListRv)
//        adapter.setEmptyView(R.layout.empty_layout, messageListRv)
        adapter.addHeaderView(initHeadsView())

        adapter.setOnItemChildClickListener { _, view, position ->
            val recentContact = adapter.data[position]
            when (view.id) {
                //置顶与取消置顶
                R.id.menuTop -> {
                    if (CommonUtil.isTagSet(recentContact, RECENT_TAG_STICKY)) {
                        CommonUtil.removeTag(recentContact, RECENT_TAG_STICKY)
                    } else {
                        CommonUtil.addTag(adapter.data[position], RECENT_TAG_STICKY)
                    }
                    NIMClient.getService(MsgService::class.java).updateRecentAndNotify(recentContact)
                    refreshMessages()

                }
                //删除会话
                R.id.menuDetele -> {
                    // 删除会话，删除后，消息历史被一起删除
                    NIMClient.getService(MsgService::class.java).deleteRecentContact(recentContact)
                    NIMClient.getService(MsgService::class.java).clearChattingHistory(recentContact.contactId, recentContact.sessionType)
                    adapter.remove(position)
                    refreshMessages()
                }
                R.id.content -> {
                    ChatActivity.start(this, adapter.data[position].contactId)
                }
            }
        }

    }


    //创建打招呼好友布局
    private val hiAdapter by lazy { MessageListFriensAdapter() }

    private fun initFriendsView(): View {
        val friendsView = LayoutInflater.from(this).inflate(R.layout.headerview_hi, messageListRv, false)
        val linearLayoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        friendsView.headRv.layoutManager = linearLayoutManager
        friendsView.headRv.adapter = hiAdapter
        hiAdapter.setOnItemClickListener { adapter, view, position ->
        }

        return friendsView
    }


    /**
     * 创建头布局
     */
    private val headAdapter by lazy { MessageListHeadAdapter() }

    private fun initHeadsView(): View {
        val headView = LayoutInflater.from(this).inflate(R.layout.headerview_hi, messageListRv, false)
        headView.friendTv.visibility = View.GONE
        val linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        headView.headRv.layoutManager = linearLayoutManager
        headView.headRv.adapter = headAdapter
        headAdapter.setOnItemClickListener { adapter, view, position ->
            when (position) {
                0 -> {
                    startActivity<MessageHiActivity>()
                }
                1 -> {
                    startActivity<MessageSquareActivity>()
                }
                2 -> {
                    startActivity<MessageLikeMeActivity>()
                }
            }
        }
        return headView
    }


    override fun onMessageCensusResult(data: MessageListBean1?) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        val ass = MessageListBean("官方助手", "助手推送消息内容", 1, "2分钟前", R.drawable.icon_assistant)

        ////1广场点赞 2评论我的 3为我评论点赞的 4@我的列表
        val squa = MessageListBean(
            "发现", when (data?.square_type) {
                1 -> {
                    "${data.square_nickname}赞了你的动态"
                }
                2 -> {
                    "${data.square_nickname}评论了你的动态"
                }
                3 -> {
                    "${data.square_nickname}赞了你的评论"
                }
                4 -> {
                    "${data.square_nickname}"
                }
                else -> {
                    ""
                }
            }, data?.square_cnt ?: 0, "${data?.square_time}", R.drawable.icon_square_msg
        )


        val like = MessageListBean(
            "对我感兴趣的",
            "有${data?.liked_cnt ?: 0}个人对你感兴趣",
            data?.liked_unread_cnt ?: 0,
            "${data?.liked_time}",
            R.drawable.icon_like_msg
        )

        headAdapter.addData(ass)
        headAdapter.addData(squa)
        headAdapter.addData(like)
    }


    //离线获取@了我的
    override fun updateOfflineContactAited(recentAited: MutableList<RecentContact>) {
    }

    //获取最近会话（但是要获取最近的联系人列表）
    override fun onGetRecentContactResults(result: MutableList<RecentContact>) {
        adapter.data.clear()
        adapter.setNewData(result)
        refreshMessages()

        //初次加载，更新离线的消息中是否有@我的消息
        var recentAited = mutableListOf<RecentContact>()
        for (loadedRecent in result) {
            if (loadedRecent.sessionType == SessionTypeEnum.Team) {
                recentAited.add(loadedRecent)
            }
        }
        if (recentAited.size > 0)
            mPresenter.mView.updateOfflineContactAited(recentAited)
    }


    private fun refreshMessages() {
        sortRecentContacts(adapter.data)
        adapter.notifyDataSetChanged()
    }

    /**
     * **************************** 排序 ***********************************
     */
    private fun sortRecentContacts(list: List<RecentContact>) {
        if (list.size == 0) {
            return
        }
        Collections.sort(list, comp)
    }

    private val comp = Comparator<RecentContact> { o1, o2 ->
        // 先比较置顶tag
        val sticky = (o1.tag and RECENT_TAG_STICKY) - (o2.tag and RECENT_TAG_STICKY)
        if (sticky != 0L) {
            if (sticky > 0) -1 else 1
        } else {
            val time = o1.time - o2.time
            if (time == 0L) 0 else if (time > 0) -1 else 1
        }
    }

    /**
     * ********************** 收消息，处理状态变化 ************************
     */
    private fun registerObservers(register: Boolean) {
        val service = NIMClient.getService(MsgServiceObserve::class.java)
        service.observeReceiveMessage(messageReceiverObserver, register)
        service.observeRecentContact(messageObserver, register)
        service.observeMsgStatus(statusObserver, register)
        service.observeRecentContactDeleted(deleteObserver, register)
        NimUIKit.getContactChangedObservable().registerObserver(friendDataChangedObserver, register)
        if (register) {
            registerUserInfoObserver()
        } else {
            unregisterUserInfoObserver()
        }
    }

    private var userInfoObserver: UserInfoObserver? = null

    private fun registerUserInfoObserver() {
        if (userInfoObserver == null) {
            userInfoObserver = UserInfoObserver { refreshMessages() }
        }
        NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, true)
    }

    private fun unregisterUserInfoObserver() {
        if (userInfoObserver != null) {
            NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, false)
        }
    }

    // 暂存消息，当RecentContact 监听回来时使用，结束后清掉
    private val cacheMessages = HashMap<String, MutableSet<IMMessage>>()

    internal var friendDataChangedObserver: ContactChangedObserver = object : ContactChangedObserver {

        override fun onAddedOrUpdatedFriends(accounts: List<String>) {
            refreshMessages()
        }

        override fun onDeletedFriends(accounts: List<String>) {
            refreshMessages()
        }

        override fun onAddUserToBlackList(account: List<String>) {
            refreshMessages()
        }

        override fun onRemoveUserFromBlackList(account: List<String>) {
            refreshMessages()
        }
    }

    //监听在线消息中是否有@我
    private val messageReceiverObserver =
        Observer<List<IMMessage>> { imMessages ->
            if (imMessages != null) {
                for (imMessage in imMessages) {
                    if (!TeamMemberAitHelper.isAitMessage(imMessage)) {
                        continue
                    }
                    var cacheMessageSet: MutableSet<IMMessage>? = cacheMessages[imMessage.sessionId]
                    if (cacheMessageSet == null) {
                        cacheMessageSet = HashSet()
                        cacheMessages[imMessage.sessionId] = cacheMessageSet
                    }
                    cacheMessageSet.add(imMessage)
                }
            }
        }

    internal var messageObserver: Observer<List<RecentContact>> =
        Observer { recentContacts ->
            if (!DropManager.getInstance().isTouchable) {
                // 正在拖拽红点，缓存数据
                for (r in recentContacts) {
                    cached.put(r.contactId, r)
                }
                return@Observer
            }
            onRecentContactChanged(recentContacts)
        }

    private fun onRecentContactChanged(recentContacts: List<RecentContact>) {
        var index: Int
        for (r in recentContacts) {
            index = -1
            for (i in adapter.data.indices) {
                if (r.contactId == adapter.data.get(i).getContactId() && r.sessionType == adapter.data.get(i)
                        .getSessionType()
                ) {
                    index = i
                    break
                }
            }
            if (index >= 0) {
                adapter.data.removeAt(index)
            }
            adapter.data.add(r)
            if (r.sessionType == SessionTypeEnum.Team && cacheMessages[r.contactId] != null) {
                TeamMemberAitHelper.setRecentContactAited(r, cacheMessages[r.contactId])
            }
        }
        cacheMessages.clear()
        refreshMessages()
    }

    private fun registerOnlineStateChangeListener(register: Boolean) {
        if (!NimUIKitImpl.enableOnlineState()) {
            return
        }
        NimUIKitImpl.getOnlineStateChangeObservable().registerOnlineStateChangeListeners(
            onlineStateChangeObserver,
            register
        )
    }


    internal var onlineStateChangeObserver: OnlineStateChangeObserver =
        OnlineStateChangeObserver { adapter.notifyDataSetChanged() }

    private fun registerDropCompletedListener(register: Boolean) {
        if (register) {
            DropManager.getInstance().addDropCompletedListener(dropCompletedListener)
        } else {
            DropManager.getInstance().removeDropCompletedListener(dropCompletedListener)
        }
    }

    internal var dropCompletedListener: DropCover.IDropCompletedListener =
        DropCover.IDropCompletedListener { id, explosive ->
            if (cached != null && !cached.isEmpty()) {
                // 红点爆裂，已经要清除未读，不需要再刷cached
                if (explosive) {
                    if (id is RecentContact) {
                        cached.remove(id.contactId)
                    } else if (id is String && id.contentEquals("0")) {
                        cached.clear()
                    }
                }
                // 刷cached
                if (!cached.isEmpty()) {
                    val recentContacts = ArrayList<RecentContact>(cached.size)
                    recentContacts.addAll(cached.values)
                    cached.clear()
                    onRecentContactChanged(recentContacts)
                }
            }
        }

    internal var statusObserver: Observer<IMMessage> =
        Observer { message ->
            val index = getItemIndex(message.uuid)
            if (index >= 0 && index < adapter.data.size) {
                val item = adapter.data.get(index)
                item.setMsgStatus(message.status)
                adapter.notifyItemChanged(index)
            }
        }

    internal var deleteObserver: Observer<RecentContact> =
        Observer { recentContact ->
            if (recentContact != null) {
                for (item in adapter.data) {
                    if (TextUtils.equals(
                            item.getContactId(),
                            recentContact.contactId
                        ) && item.getSessionType() == recentContact.sessionType
                    ) {
                        adapter.data.remove(item)
                        refreshMessages()
                        break
                    }
                }
            } else {
                adapter.data.clear()
                refreshMessages()
            }
        }


    private fun getItemIndex(uuid: String): Int {
        for (i in adapter.data.indices) {
            val item = adapter.data.get(i)
            if (TextUtils.equals(item.getRecentMessageId(), uuid)) {
                return i
            }
        }
        return -1
    }

    override fun onDestroy() {
        super.onDestroy()
        registerObservers(false)
    }
}
