package com.sdy.jitangapplication.ui.fragment


import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
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
import com.sdy.jitangapplication.event.NewMsgEvent
import com.sdy.jitangapplication.event.UpdateHiEvent
import com.sdy.jitangapplication.model.HiMessageBean
import com.sdy.jitangapplication.model.MessageListBean
import com.sdy.jitangapplication.model.MessageListBean1
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.nim.attachment.ShareSquareAttachment
import com.sdy.jitangapplication.presenter.MessageListPresenter
import com.sdy.jitangapplication.presenter.view.MessageListView
import com.sdy.jitangapplication.ui.activity.MessageHiActivity
import com.sdy.jitangapplication.ui.activity.MessageLikeMeActivity
import com.sdy.jitangapplication.ui.activity.MessageSquareActivity
import com.sdy.jitangapplication.ui.adapter.MessageListAdapter
import com.sdy.jitangapplication.ui.adapter.MessageListFriensAdapter
import com.sdy.jitangapplication.ui.adapter.MessageListHeadAdapter
import com.sdy.jitangapplication.ui.dialog.HarassmentDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_message_list.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.headerview_hi.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import java.util.*
import kotlin.Comparator


/**
 * A simple [Fragment] subclass.
 *
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
        mPresenter = MessageListPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        retryBtn.onClick {
            setViewState(BaseActivity.LOADING)
            //获取最近消息
            mPresenter.messageCensus(params)
        }

        messageListRv.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        messageListRv.adapter = adapter
        adapter.bindToRecyclerView(messageListRv)
//        adapter.setEmptyView(R.layout.empty_layout, messageListRv)
        adapter.addHeaderView(initFriendsView(), 0)
        adapter.addHeaderView(initHeadsView(), 1)
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
                    NIMClient.getService(MsgService::class.java)
                        .clearChattingHistory(recentContact.contactId, recentContact.sessionType)
                    adapter.remove(position)
                    refreshMessages()
                }
                R.id.content -> {
                    // 触发 MsgServiceObserve#observeRecentContact(Observer, boolean) 通知，
                    // 通知中的 RecentContact 对象的未读数为0
                    NIMClient.getService(MsgService::class.java)
                        .clearUnreadCount(adapter.data[position].contactId, SessionTypeEnum.P2P)
                    if (UserManager.getHiCount() > 0) {
                        UserManager.saveHiCount(UserManager.getHiCount() - 1)
                    }
                    try {
                        Thread.sleep(500)
                    } catch (e: Exception) {
                    }
                    ChatActivity.start(activity!!, adapter.data[position].contactId)
                    EventBus.getDefault().post(NewMsgEvent())

                }
            }
        }

    }


    //创建打招呼好友布局
    private var hiDatas = mutableListOf<HiMessageBean>()
    private val hiAdapter by lazy { MessageListFriensAdapter(hiDatas) }
    private fun initFriendsView(): View {
        val friendsView = layoutInflater.inflate(R.layout.headerview_hi, messageListRv, false)
        val linearLayoutManager = LinearLayoutManager(activity!!, RecyclerView.HORIZONTAL, false)
        friendsView.headRv.layoutManager = linearLayoutManager
        friendsView.headRv.adapter = hiAdapter
        friendsView.headRv.addItemDecoration(
            DividerItemDecoration(
                activity!!,
                DividerItemDecoration.VERTICAL_LIST,
                SizeUtils.dp2px(10f),
                resources.getColor(R.color.colorWhite)
            )
        )
        friendsView.friendTv.onClick {
            startActivity<MessageHiActivity>()
        }
        hiAdapter.setOnItemClickListener { _, view, position ->
            //发送通知告诉剩余时间，并且开始倒计时
            NIMClient.getService(MsgService::class.java)
                .clearUnreadCount(hiAdapter.data[position].accid, SessionTypeEnum.P2P)

            // 通知中的 RecentContact 对象的未读数为0
            //做招呼的已读状态更新
            if (UserManager.getHiCount() > 0) {
                UserManager.saveHiCount(UserManager.getHiCount() - 1)
            }
            try {
                Thread.sleep(500)
            } catch (e: Exception) {
            }

            ChatActivity.start(activity!!, hiAdapter.data[position].accid ?: "")
            EventBus.getDefault().post(NewMsgEvent())

        }

//        hiAdapter.addData(mutableListOf(""))
        return friendsView
    }


    /**
     * 创建头布局
     */
    private val headAdapter by lazy { MessageListHeadAdapter() }

    private fun initHeadsView(): View {
        val headView = LayoutInflater.from(activity!!).inflate(R.layout.headerview_hi, messageListRv, false)
        headView.rlFriend.visibility = View.GONE
        val linearLayoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        headView.headRv.layoutManager = linearLayoutManager
        headView.headRv.adapter = headAdapter
        //初始化第一个项目
        headAdapter.addData(MessageListBean())
        headAdapter.setOnItemClickListener { adapter, view, position ->
            when (position) {
                0 -> {//官方助手
                    // 触发 MsgServiceObserve#observeRecentContact(Observer, boolean) 通知，
                    // 通知中的 RecentContact 对象的未读数为0
                    NIMClient.getService(MsgService::class.java)
                        .clearUnreadCount(Constants.ASSISTANT_ACCID, SessionTypeEnum.P2P)
                    try {
                        Thread.sleep(500)
                    } catch (e: Exception) {
                    }
                    ChatActivity.start(activity!!, Constants.ASSISTANT_ACCID)
                    EventBus.getDefault().post(NewMsgEvent())
                    headAdapter.data[0].count = 0
                    headAdapter.notifyItemChanged(0)
                }
                1 -> {//广场消息
                    startActivity<MessageSquareActivity>()
                    headAdapter.data[1].count = 0
                    headAdapter.notifyItemChanged(1)
                }
                2 -> {//喜欢我的消息
                    startActivity<MessageLikeMeActivity>()
                    headAdapter.data[2].count = 0
                    headAdapter.notifyItemChanged(2)

                }
            }
        }
        return headView
    }

    override fun onResume() {
        super.onResume()
        //获取最近消息 延迟一秒请求
//        Handler().postDelayed({
//            mPresenter.messageCensus(params)
//        }, 1000)
//        setHiData()  模拟数据请求
    }


    /**
     * 获取消息中心的顶部数据
     */
    override fun onMessageCensusResult(data: MessageListBean1?) {
        ////1广场点赞 2评论我的 3为我评论点赞的 4@我的列表
        UserManager.saveSquareCount(data?.square_cnt ?: 0)
        UserManager.saveLikeCount(data?.liked_unread_cnt ?: 0)
        if (UserManager.getLikeCount() > 0 || UserManager.getSquareCount() > 0)
            EventBus.getDefault().post(NewMsgEvent())

        //如果满足招呼认证提醒，就开启认证提醒
        if (data?.greet_toast == true && !SPUtils.getInstance(Constants.SPNAME).getBoolean("isShowHarassment", false)) {
            HarassmentDialog(activity!!, HarassmentDialog.CHATEDHI).show()
            SPUtils.getInstance(Constants.SPNAME).put("isShowHarassment", true)
        }

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
                    "暂时没有新动态哦"
                }
            }, data?.square_cnt ?: 0, "${data?.square_time}", R.drawable.icon_square_msg
        )


        val like = MessageListBean(
            "对我感兴趣的",
            if ((data?.liked_cnt ?: 0) == 0) {
                "暂时没有最新对你感兴趣的哦"
            } else {
                "有${data?.liked_cnt ?: 0}个人对你感兴趣"
            },
            data?.liked_unread_cnt ?: 0,
            "${data?.liked_time}",
            R.drawable.icon_like_msg
        )

        headAdapter.data.clear()
        headAdapter.addData(ass)
        headAdapter.addData(squa)
        headAdapter.addData(like)
        hiAdapter.data.clear()
        if (data?.greet != null && data?.greet.isNotEmpty()) {
            if (data.greet_cnt > 0) {
                adapter.headerLayout.hiCount.text = "${data.greet_cnt}"
                adapter.headerLayout.hiCount.isVisible = true
            } else {
                adapter.headerLayout.hiCount.isVisible = false
            }
            adapter.headerLayout.rlFriend.visibility = View.VISIBLE
            hiAdapter.setNewData(data.greet)
        } else {
            adapter.headerLayout.rlFriend.visibility = View.GONE
            hiAdapter.notifyDataSetChanged()
        }

        //获取最近联系人列表
        mPresenter.getRecentContacts()
    }

    //离线获取@了我的
    override fun updateOfflineContactAited(recentAited: MutableList<RecentContact>) {
    }

    //官方助手
    var ass = MessageListBean("官方助手", "暂时没有助手消息哦", 0, "", R.drawable.icon_assistant)

    //获取最近会话（但是要获取最近的联系人列表）
    override fun onGetRecentContactResults(result: MutableList<RecentContact>) {
        setViewState(BaseActivity.CONTENT)

        for (loadedRecent in result) {
            if (loadedRecent.contactId == Constants.ASSISTANT_ACCID) {
                ass = MessageListBean(
                    "官方助手",
                    when {
                        loadedRecent.attachment is ChatHiAttachment -> when {
                            (loadedRecent.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_HI -> "『招呼消息』"
                            (loadedRecent.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_MATCH -> "通过『" + (loadedRecent.getAttachment() as ChatHiAttachment).tag + "』匹配"
                            (loadedRecent.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_RFIEND -> "『好友消息』"
                            (loadedRecent.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_OUTTIME -> "『消息过期』"
                            else -> ""
                        }
                        loadedRecent.attachment is ShareSquareAttachment -> "『动态分享内容』"
                        else -> loadedRecent.content
                    },
                    loadedRecent.unreadCount,
                    TimeUtil.getTimeShowString(loadedRecent.time, true),
                    R.drawable.icon_assistant,
                    loadedRecent.contactId
                )
                headAdapter.setData(0, ass)
                headAdapter.notifyItemChanged(0)
                result.remove(loadedRecent)
                break
            }
        }

        //遍历删除招呼消息
        val iterator = result.iterator()
        while (iterator.hasNext()) {
            val contact = iterator.next()
            for (hiBean in hiAdapter.data) {
                if (contact.contactId == hiBean.accid) {
                    iterator.remove()
                    break
                }
            }
        }

        adapter.data.clear()
        adapter.setNewData(result)
        refreshMessages()

        //初次加载，更新离线的消息中是否有@我的消息
//        var recentAited = mutableListOf<RecentContact>()
//        for (loadedRecent in result) {
//            if (loadedRecent.sessionType == SessionTypeEnum.Team) {
//                recentAited.add(loadedRecent)
//            }
//        }
//        if (recentAited.size > 0)
//            mPresenter.mView.updateOfflineContactAited(recentAited)
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
                    cached.put(r.contactId, r)
                }
                return@Observer
            }
            onRecentContactChanged(recentContacts)
        }

    private fun onRecentContactChanged(recentContacts: MutableList<RecentContact>) {
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
        EventBus.getDefault().unregister(this)
        hiAdapter.cancelAllTimers()//取消所有的定时器
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onError(text: String) {
        setViewState(BaseActivity.ERROR)
        errorMsg.text = CommonFunction.getErrorMsg(activity!!)

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateHiEvent(event: UpdateHiEvent) {
        mPresenter.messageCensus(params)
    }


    private fun setViewState(state: Int) {
        when (state) {
            BaseActivity.LOADING -> {
                loadingLayout.isVisible = true
                messageListRv.isVisible = false
                errorLayout.isVisible = false
            }
            BaseActivity.CONTENT -> {
                messageListRv.isVisible = true
                loadingLayout.isVisible = false
                errorLayout.isVisible = false
            }
            BaseActivity.ERROR -> {
                errorLayout.isVisible = true
                messageListRv.isVisible = false
                loadingLayout.isVisible = false
            }
        }

    }
}
