package com.sdy.jitangapplication.ui.fragment


import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SPUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.api.model.contact.ContactChangedObserver
import com.netease.nim.uikit.api.model.main.OnlineStateChangeObserver
import com.netease.nim.uikit.api.model.user.UserInfoObserver
import com.netease.nim.uikit.business.recent.RecentContactsFragment
import com.netease.nim.uikit.business.recent.TeamMemberAitHelper
import com.netease.nim.uikit.common.CommonUtil
import com.netease.nim.uikit.common.ui.drop.DropCover
import com.netease.nim.uikit.common.ui.drop.DropManager
import com.netease.nim.uikit.common.util.sys.TimeUtil
import com.netease.nim.uikit.impl.NimUIKitImpl
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.MessageReceipt
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.GetNewMsgEvent
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.event.UpdateHiEvent
import com.sdy.jitangapplication.model.MessageListBean
import com.sdy.jitangapplication.model.MessageListBean1
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.nim.attachment.ShareSquareAttachment
import com.sdy.jitangapplication.presenter.MessageListPresenter
import com.sdy.jitangapplication.presenter.view.MessageListView
import com.sdy.jitangapplication.ui.activity.*
import com.sdy.jitangapplication.ui.adapter.MessageCenterAllAdapter
import com.sdy.jitangapplication.ui.adapter.MessageListAdapter
import com.sdy.jitangapplication.ui.adapter.MessageListHeadAdapter
import com.sdy.jitangapplication.ui.dialog.HarassmentDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.error_layout.*
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
class MessageListFragment : BaseMvpLazyLoadFragment<MessageListPresenter>(), MessageListView {
    override fun loadData() {
        initView()
        registerObservers(true)
        registerDropCompletedListener(true)
        registerOnlineStateChangeListener(true)
        mPresenter.messageCensus(params)
    }

    private var cached: MutableMap<String, RecentContact> = mutableMapOf() // 暂缓刷上列表的数据（未读数红点拖拽动画运行时用）

    private val params by lazy { hashMapOf("token" to UserManager.getToken(), "accid" to UserManager.getAccid()) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message_list, container, false)
    }


    private val adapter by lazy { MessageListAdapter() }
    private fun initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val param = customStatusBar.layoutParams as LinearLayout.LayoutParams
            param.height = BarUtils.getStatusBarHeight()
        } else {
            customStatusBar.isVisible = false
        }


        mPresenter = MessageListPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        retryBtn.onClick {
            setViewState(BaseActivity.LOADING)
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
                    if (CommonUtil.isTagSet(recentContact, RecentContactsFragment.RECENT_TAG_STICKY)) {
                        CommonUtil.removeTag(recentContact, RecentContactsFragment.RECENT_TAG_STICKY)
                    } else {
                        CommonUtil.addTag(adapter.data[position], RecentContactsFragment.RECENT_TAG_STICKY)
                    }
                    NIMClient.getService(MsgService::class.java).updateRecentAndNotify(recentContact)
                    refreshMessages()

                }
                //删除会话
                R.id.menuDetele -> {
                    // 删除会话，删除后，消息历史被一起删除
                    NIMClient.getService(MsgService::class.java).deleteRecentContact(recentContact)
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
    private val allMessageTypeAdapter by lazy { MessageCenterAllAdapter() }

    private fun initMessageAllHeader(): View {
        allMessageTypeAdapter.addData(MessageListBean(title = "点赞", icon = R.drawable.icon_message_thumbs_up))
        allMessageTypeAdapter.addData(MessageListBean(title = "评论", icon = R.drawable.icon_message_comment))
        allMessageTypeAdapter.addData(MessageListBean(title = "喜欢我", icon = R.drawable.icon_message_like))
        allMessageTypeAdapter.addData(MessageListBean(title = "招呼", icon = R.drawable.icon_message_greet))
        val friendsView = layoutInflater.inflate(R.layout.headview_message_all, messageListRv, false)
        friendsView.messageCenterRv.layoutManager = GridLayoutManager(activity!!, 4)
        friendsView.messageCenterRv.adapter = allMessageTypeAdapter
        allMessageTypeAdapter.setOnItemClickListener { _, _, position ->
            when (position) {
                0 -> {//点赞
                    startActivity<MessageSquareActivity>("type" to 1)
                }
                1 -> {//评论
                    startActivity<MessageSquareActivity>("type" to 2)
                }
                2 -> {//喜欢我
                    if (like_free_show) {
                        startActivity<LikeMeReceivedActivity>()
                    } else {
                        startActivity<MessageLikeMeActivity>()
                    }
                }
                else -> {//招呼
                    startActivity<GreetReceivedActivity>()
                }
            }
            allMessageTypeAdapter.data[position].count = 0
            allMessageTypeAdapter.notifyItemChanged(position)
        }
        return friendsView
    }

    /**
     * 创建小助手布局
     */
    private val headAdapter by lazy { MessageListHeadAdapter() }

    private fun initAssistHeadsView(): View {
        val headView = LayoutInflater.from(activity!!).inflate(R.layout.headerview_like_me, messageListRv, false)
        headView.rlFriend.visibility = View.GONE
        val linearLayoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        headView.headRv.layoutManager = linearLayoutManager
        headView.headRv.adapter = headAdapter
        //初始化第一个项目
        headAdapter.addData(MessageListBean())
        headAdapter.setOnItemClickListener { adapter, view, position ->
            when (position) {
                0 -> {//官方助手
                    NIMClient.getService(MsgService::class.java)
                        .clearUnreadCount(Constants.ASSISTANT_ACCID, SessionTypeEnum.P2P)
                    ChatActivity.start(activity!!, Constants.ASSISTANT_ACCID)
                    EventBus.getDefault().post(GetNewMsgEvent())
                    headAdapter.data[0].count = 0
                    headAdapter.notifyItemChanged(0)
                }
            }
        }
        return headView
    }


    /**
     * 获取消息中心的顶部数据
     */
    private var msgBean: MessageListBean1? = null
    private var like_free_show: Boolean = false

    override fun onMessageCensusResult(data: MessageListBean1?) {
        ////1广场点赞 2评论我的 3为我评论点赞的 4@我的列表
        allMessageTypeAdapter.data[0].count = data?.thumbs_up_count ?: 0
        allMessageTypeAdapter.data[1].count = data?.comment_count ?: 0
        allMessageTypeAdapter.data[2].count = data?.liked_unread_cnt ?: 0
        allMessageTypeAdapter.data[3].count = data?.greet_count ?: 0
        allMessageTypeAdapter.notifyDataSetChanged()
        like_free_show = data?.like_free_show ?: false
        if ((data?.comment_count ?: 0 > 0) || (data?.thumbs_up_count ?: 0) > 0 || (data?.liked_unread_cnt ?: 0) > 0)
            EventBus.getDefault().post(GetNewMsgEvent())

        //如果满足招呼认证提醒，就开启认证提醒
        if (data?.greet_toast == true && !SPUtils.getInstance(Constants.SPNAME).getBoolean("isShowHarassment", false)) {
            HarassmentDialog(activity!!, HarassmentDialog.CHATEDHI).show()
            SPUtils.getInstance(Constants.SPNAME).put("isShowHarassment", true)
        }
        msgBean = data

        adapter.greetList.clear()
        for (accid in data?.effective_greet ?: mutableListOf()) {
            adapter.greetList.add(accid)
        }
        headAdapter.data[0] = ass
        headAdapter.notifyItemChanged(0)
        //获取最近联系人列表
        mPresenter.getRecentContacts()
    }


    //离线获取@了我的
    override fun updateOfflineContactAited(recentAited: MutableList<RecentContact>) {
    }

    //官方助手
    private val ass by lazy { MessageListBean("官方助手", "暂时没有助手消息哦", 0, "", R.drawable.icon_default_avator_logo) }

    //获取最近会话（但是要获取最近的联系人列表）
    override fun onGetRecentContactResults(result: MutableList<RecentContact>) {
        setViewState(BaseActivity.CONTENT)
        for (loadedRecent in result) {
            if (loadedRecent.contactId == Constants.ASSISTANT_ACCID) {
                ass.msg = when {
                    loadedRecent.attachment is ChatHiAttachment -> when {
                        (loadedRecent.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_HI -> "『招呼消息』"
                        (loadedRecent.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_MATCH -> "『匹配消息』"
                        (loadedRecent.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_RFIEND -> "『好友消息』"
                        (loadedRecent.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_OUTTIME -> "『消息过期』"
                        else -> ""
                    }
                    loadedRecent.attachment is ShareSquareAttachment -> "『动态分享内容』"
                    else -> loadedRecent.content
                }
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
            for (hiBean in msgBean?.no_effective_greet ?: mutableListOf()) {
                if (contact.contactId == hiBean.accid) {
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

    internal var messageObserver: Observer<MutableList<RecentContact>> =
        Observer { recentContacts ->
            if (!DropManager.getInstance().isTouchable) {
                // 正在拖拽红点，缓存数据
                for (r in recentContacts) {
                    cached[r.contactId] = r
                }
                return@Observer
            }
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

    private fun registerDropCompletedListener(register: Boolean) {
        if (register) {
            DropManager.getInstance().addDropCompletedListener(dropCompletedListener)
        } else {
            DropManager.getInstance().removeDropCompletedListener(dropCompletedListener)
        }
    }

    private var dropCompletedListener: DropCover.IDropCompletedListener =
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
                    onRecentContactChanged()
                }
            }
        }

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


    private val TAG = MessageListFragment::class.java.simpleName
    /**
     * 已读回执观察者
     */
    private val messageReceiptObserver = Observer<List<MessageReceipt>> {
        //收到已读回执,调用接口,改变此时招呼或者消息的状态
        Log.d(TAG, "======已读回执=====")
        mPresenter.messageCensus(params)
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
        registerDropCompletedListener(false)
        registerOnlineStateChangeListener(false)
        EventBus.getDefault().unregister(this)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onError(text: String) {
        setViewState(BaseActivity.ERROR)
        errorMsg.text = CommonFunction.getErrorMsg(activity!!)

    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onUpdateHiEvent(event: UpdateHiEvent) {
        mPresenter.messageCensus(params)
    }


    private fun setViewState(state: Int) {
        when (state) {
            BaseActivity.LOADING -> {
                loadingLayout.isVisible = true
                messageContentLl.isVisible = false
                errorLayout.isVisible = false
            }
            BaseActivity.CONTENT -> {
                messageContentLl.isVisible = true
                loadingLayout.isVisible = false
                errorLayout.isVisible = false
            }
            BaseActivity.ERROR -> {
                errorLayout.isVisible = true
                messageContentLl.isVisible = false
                loadingLayout.isVisible = false
            }
        }

    }
}
