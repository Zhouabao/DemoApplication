package com.sdy.jitangapplication.ui.fragment


import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.MessageReceipt
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.GetNewMsgEvent
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.event.UpdateHiEvent
import com.sdy.jitangapplication.model.AccostBean
import com.sdy.jitangapplication.model.MessageListBean
import com.sdy.jitangapplication.model.MessageListBean1
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ContactAttachment
import com.sdy.jitangapplication.nim.attachment.SendCustomTipAttachment
import com.sdy.jitangapplication.nim.attachment.SendGiftAttachment
import com.sdy.jitangapplication.nim.attachment.ShareSquareAttachment
import com.sdy.jitangapplication.nim.uikit.api.NimUIKit
import com.sdy.jitangapplication.nim.uikit.api.model.contact.ContactChangedObserver
import com.sdy.jitangapplication.nim.uikit.api.model.main.OnlineStateChangeObserver
import com.sdy.jitangapplication.nim.uikit.api.model.user.UserInfoObserver
import com.sdy.jitangapplication.nim.uikit.business.recent.RecentContactsFragment
import com.sdy.jitangapplication.nim.uikit.common.CommonUtil
import com.sdy.jitangapplication.nim.uikit.common.util.sys.TimeUtil
import com.sdy.jitangapplication.nim.uikit.impl.NimUIKitImpl
import com.sdy.jitangapplication.presenter.MessageListPresenter
import com.sdy.jitangapplication.presenter.view.MessageListView
import com.sdy.jitangapplication.ui.activity.AccostListActivity
import com.sdy.jitangapplication.ui.activity.ContactBookActivity
import com.sdy.jitangapplication.ui.activity.MessageSquareActivity
import com.sdy.jitangapplication.ui.adapter.MessageCenterAllAdapter
import com.sdy.jitangapplication.ui.adapter.MessageListAdapter
import com.sdy.jitangapplication.ui.adapter.MessageListHeadAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_message_list.*
import kotlinx.android.synthetic.main.headerview_like_me.view.*
import kotlinx.android.synthetic.main.headview_message_all.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import java.util.*
import kotlin.Comparator


/**
 * 消息中心
 */
class MessageListFragment : BaseMvpFragment<MessageListPresenter>(), MessageListView {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    fun loadData() {
        initView()
        registerObservers(true)
        registerOnlineStateChangeListener(true)
        if (!UserManager.touristMode)
            mPresenter.messageCensus(params)
    }

    private val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid()
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message_list, container, false)
    }


    private val adapter by lazy { MessageListAdapter() }
    private fun initView() {
        mPresenter = MessageListPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        stateMessageList.retryBtn.onClick {
            stateMessageList.viewState = MultiStateView.VIEW_STATE_LOADING
            //获取最近消息
            mPresenter.messageCensus(params)
        }

        contactBookBtn.onClick {
            startActivity<ContactBookActivity>()
        }

        knowBtn.onClick {
            blackLl.isVisible = false
        }


        messageListRv.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        messageListRv.adapter = adapter
        adapter.bindToRecyclerView(messageListRv)
        adapter.addHeaderView(initMessageAllHeader(), 0)
        adapter.addHeaderView(initAssistHeadsView(), 1)
        adapter.setHeaderAndEmpty(true)

        adapter.setOnItemChildClickListener { _, view, position ->
            val recentContact = adapter.data[position]
            when (view.id) {
                //置顶与取消置顶
                R.id.menuTop -> {
                    if (CommonUtil.isTagSet(
                            recentContact,
                            RecentContactsFragment.RECENT_TAG_STICKY
                        )
                    ) {
                        CommonUtil.removeTag(
                            recentContact,
                            RecentContactsFragment.RECENT_TAG_STICKY
                        )
                    } else {
                        CommonUtil.addTag(
                            adapter.data[position],
                            RecentContactsFragment.RECENT_TAG_STICKY
                        )
                    }
                    NIMClient.getService(MsgService::class.java)
                        .updateRecentAndNotify(recentContact)
                    refreshMessages()

                }
                //删除会话
                R.id.menuDetele -> {
                    // 删除会话，删除后，消息历史被一起删除
                    NIMClient.getService(MsgService::class.java).deleteRecentContact(recentContact)
//                    NIMClient.getService(MsgService::class.java).deleteRoamingRecentContact(recentContact.contactId,recentContact.sessionType)
                    adapter.remove(position)
                    refreshMessages()
                }
                R.id.content -> {
                    // 触发 MsgServiceObserve#observeRecentContact(Observer, boolean) 通知，
                    // 通知中的 RecentContact 对象的未读数为0
                    NIMClient.getService(MsgService::class.java)
                        .clearUnreadCount(adapter.data[position].contactId, SessionTypeEnum.P2P)
                    ChatActivity.start(activity!!, adapter.data[position].contactId)
                    EventBus.getDefault().post(GetNewMsgEvent())

                }
            }
        }

    }

    /**
     * 消息汇总中心
     */
    private val accostAdapter by lazy { MessageCenterAllAdapter() }

    private fun initMessageAllHeader(): View {
        val accostView =
            layoutInflater.inflate(R.layout.headview_message_all, messageListRv, false)
        accostView.messageCenterRv.layoutManager =
            LinearLayoutManager(activity!!, RecyclerView.HORIZONTAL, false)
        accostView.messageCenterRv.adapter = accostAdapter
        accostAdapter.setOnItemClickListener { _, _, position ->
            ChatActivity.start(activity!!, accostAdapter.data[position].accid)
            EventBus.getDefault().post(GetNewMsgEvent())
        }
        accostView.moreChatUpBtn.clickWithTrigger {
            startActivity<AccostListActivity>()
        }
        return accostView
    }


    /**
     * 创建小助手布局
     */
    private val headAdapter by lazy { MessageListHeadAdapter() }

    var checked = false
    private fun initAssistHeadsView(): View {
        val headView = LayoutInflater.from(activity!!)
            .inflate(R.layout.headerview_like_me, messageListRv, false)

        headView.questionCheckedBtn.isVisible = UserManager.getGender() == 2
        headView.showCl.clickWithTrigger {
            checked = !checked
            if (checked) {
                headView.questionCheckedBtn.setImageResource(R.drawable.icon_question_checked)
                headView.questionShowIv.isVisible = true
                Handler().postDelayed({
                    if (headView.questionShowIv.isVisible) {
                        headView.questionCheckedBtn.setImageResource(R.drawable.icon_question_uncheked)
                        headView.questionShowIv.isVisible = false
                        checked = false
                    }
                }, 2000L)
            } else {
                headView.questionCheckedBtn.setImageResource(R.drawable.icon_question_uncheked)
                headView.questionShowIv.isVisible = false
            }
        }
        val linearLayoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        headView.headRv.layoutManager = linearLayoutManager
        headView.headRv.adapter = headAdapter
        //初始化第一个项目
        headAdapter.addData(ass)
        headAdapter.addData(square)
        headAdapter.setOnItemClickListener { adapter, view, position ->
            when (position) {
                0 -> {//官方助手
                    NIMClient.getService(MsgService::class.java)
                        .clearUnreadCount(Constants.ASSISTANT_ACCID, SessionTypeEnum.P2P)
                    ChatActivity.start(activity!!, Constants.ASSISTANT_ACCID)

                }
                1 -> {
                    startActivity<MessageSquareActivity>()
                }
            }
            EventBus.getDefault().post(GetNewMsgEvent())
            headAdapter.data[position].count = 0
            headAdapter.notifyItemChanged(position)
        }
        return headView
    }


    /**
     * 获取消息中心的顶部数据
     */
    private var accostIds = mutableListOf<String>()

    override fun onMessageCensusResult(data: MessageListBean1?) {
        if (data?.square_count ?: 0 > 0)
            EventBus.getDefault().post(GetNewMsgEvent())
        headAdapter.data[1].msg = when (data?.square_type) {
            1 -> {
                "${data?.square_nickname}赞了你的动态"
            }
            2 -> {
                "${data?.square_nickname}评论了你的动态"
            }
            3 -> {
                "${data?.square_nickname}赞了你的评论"
            }
            else -> {
                "暂时没有新动态哦"
            }
        }
        headAdapter.data[1].count = (data?.square_count ?: 0)
        headAdapter.data[1].time = (data?.square_time ?: "")
        headAdapter.notifyItemChanged(1)
        adapter.session_list_arr = data?.session_list_arr ?: mutableListOf()

        accostAdapter.setNewData(data?.chatup_list ?: mutableListOf<AccostBean>())
        if ((data?.chatup_list ?: mutableListOf()).size > 0) {
            adapter.headerLayout.moreChatUpBtn.isVisible = (data?.chatup_list ?: mutableListOf()).size > 4
            adapter.headerLayout.getChildAt(0).isVisible = true
        } else {
            adapter.headerLayout.getChildAt(0).isVisible = false
        }
//        moreChatUpBtn
        accostIds = data?.chatup_rid_list ?: mutableListOf()
        //获取最近联系人列表
        mPresenter.getRecentContacts()
    }

    //官方助手
    private val ass by lazy {
        MessageListBean(
            "官方助手",
            "暂时没有助手消息哦",
            0,
            "",
            R.drawable.icon_default_avator_logo
        )
    }
    private val square by lazy {
        MessageListBean(
            "广场消息",
            "暂时没有广场消息哦",
            0,
            "",
            R.drawable.icon_message_square
        )
    }

    //获取最近会话（但是要获取最近的联系人列表）
    override fun onGetRecentContactResults(result: MutableList<RecentContact>) {
        stateMessageList.viewState = MultiStateView.VIEW_STATE_CONTENT

        for (loadedRecent in result) {
            if (loadedRecent.contactId == Constants.ASSISTANT_ACCID) {
                ass.msg = CommonFunction.setMessageContent(loadedRecent)
                ass.count = loadedRecent.unreadCount
                ass.time = TimeUtil.getTimeShowString(loadedRecent.time, true)
                ass.id = loadedRecent.contactId
                //本地小助手发送通知通过认证通知，本地修改认证状态
                if (loadedRecent.content.contains("已通过认证")) {
                    //更改本地的认证状态
                    UserManager.saveUserVerify(1)
                    if (SPUtils.getInstance(Constants.SPNAME).getInt("audit_only", -1) != -1) {
                        SPUtils.getInstance(Constants.SPNAME).remove("audit_only")
                        //发送通知更新内容
                        EventBus.getDefault().postSticky(RefreshEvent(true))
                    }
                }
                headAdapter.setData(0, ass)
                headAdapter.notifyItemChanged(0)
                result.remove(loadedRecent)
                break
            }
        }

        val iterator = result.iterator()
        while (iterator.hasNext()) {
            val contact = iterator.next()
            for (accid in accostIds) {
                if (contact.contactId == accid) {
                    iterator.remove()
                    break
                }
            }
        }
        adapter.setNewData(result)
        refreshMessages()
    }


    private fun refreshMessages() {
        sortRecentContacts(adapter.data)
        adapter.notifyDataSetChanged()
    }

    /**
     * **************************** 排序 ***********************************
     */
    private fun sortRecentContacts(list: List<RecentContact>) {
        if (list.isEmpty()) {
            return
        }
        Collections.sort(list, comp)
    }

    private val comp = Comparator<RecentContact> { o1, o2 ->
        // 先比较置顶tag
        val sticky =
            (o1.tag and RecentContactsFragment.RECENT_TAG_STICKY) - (o2.tag and RecentContactsFragment.RECENT_TAG_STICKY)
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
//        // 已读回执监听
        if (NimUIKitImpl.getOptions().shouldHandleReceipt) {
            service.observeMessageReceipt(messageReceiptObserver, register)
        }

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


    internal var friendDataChangedObserver: ContactChangedObserver =
        object : ContactChangedObserver {

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
                //首先剔除自定义的tip消息
                val iterator = imMessages.iterator()
                while (iterator.hasNext()) {
                    val message = iterator.next()
                    val isSend = message.direct == MsgDirectionEnum.Out
                    if ((message.attachment is SendCustomTipAttachment && (message.attachment as SendCustomTipAttachment).ifSendUserShow != isSend)
                        || (message.attachment is ContactAttachment && message.direct == MsgDirectionEnum.Out)
                    ) {
                        NIMClient.getService(MsgService::class.java).deleteChattingHistory(message)
                    }
                }
            }
        }

    internal var messageObserver: Observer<MutableList<RecentContact>> =
        Observer { recentContacts ->
            onRecentContactChanged()
        }

    private fun onRecentContactChanged() {
        mPresenter.messageCensus(params)
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


    private var onlineStateChangeObserver: OnlineStateChangeObserver =
        OnlineStateChangeObserver { adapter.notifyDataSetChanged() }


    internal var statusObserver: Observer<IMMessage> =
        Observer { message ->
            val index = getItemIndex(message.uuid)
            if (index >= 0 && index < adapter.data.size) {
                val item = adapter.data.get(index)
                item.msgStatus = message.status

                adapter.notifyItemChanged(index + adapter.headerLayoutCount)
            }
        }

    internal var deleteObserver: Observer<RecentContact> =
        Observer { recentContact ->
            if (recentContact != null) {
                for (item in adapter.data) {
                    if (TextUtils.equals(item.getContactId(), recentContact.contactId)
                        && item.getSessionType() == recentContact.sessionType
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


    private val TAG = MessageListFragment::class.java.simpleName

    /**
     * 已读回执观察者
     */
    private val messageReceiptObserver = Observer<List<MessageReceipt>> {
        //收到已读回执,调用接口,改变此时招呼或者消息的状态
        Log.d(TAG, "======已读回执=====")
//        mPresenter.messageCensus(params)
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
        registerOnlineStateChangeListener(false)
        EventBus.getDefault().unregister(this)
        Handler().removeCallbacksAndMessages(null)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onError(text: String) {
        stateMessageList.viewState = MultiStateView.VIEW_STATE_ERROR
        errorMsg.text = CommonFunction.getErrorMsg(activity!!)

    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onUpdateHiEvent(event: UpdateHiEvent) {
        try {
            mPresenter.messageCensus(params)
        } catch (e: Exception) {

        }
    }

}
