package com.sdy.jitangapplication.nim.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.google.gson.Gson
import com.kotlin.base.common.AppManager
import com.kotlin.base.ext.onClick
import com.kotlin.base.ext.setVisible
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.api.model.contact.ContactChangedObserver
import com.netease.nim.uikit.api.model.main.OnlineStateChangeObserver
import com.netease.nim.uikit.api.model.session.SessionCustomization
import com.netease.nim.uikit.api.model.user.UserInfoObserver
import com.netease.nim.uikit.business.session.constant.Extras
import com.netease.nim.uikit.business.uinfo.UserInfoHelper
import com.netease.nim.uikit.impl.NimUIKitImpl
import com.netease.nim.uikit.impl.customization.DefaultP2PSessionCustomization
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomNotification
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.baselibrary.widgets.swipeback.SwipeBackLayout
import com.sdy.baselibrary.widgets.swipeback.Utils
import com.sdy.baselibrary.widgets.swipeback.app.SwipeBackActivityBase
import com.sdy.baselibrary.widgets.swipeback.app.SwipeBackActivityHelper
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.UpdateContactBookEvent
import com.sdy.jitangapplication.event.UpdateHiEvent
import com.sdy.jitangapplication.model.CustomerMsgBean
import com.sdy.jitangapplication.nim.fragment.ChatMessageFragment
import com.sdy.jitangapplication.ui.activity.MainActivity
import kotlinx.android.synthetic.main.activity_chat.*
import org.greenrobot.eventbus.EventBus


/**
 * 点对点聊天界面
 *
 */
class ChatActivity : ChatBaseMessageActivity(), SwipeBackActivityBase {
    private var isResume = false

    companion object {
        fun start(
            context: Context,
            contactId: String,
            customization: SessionCustomization = DefaultP2PSessionCustomization(),
            anchor: IMMessage? = null
        ) {
            val intent = Intent()
            intent.putExtra(Extras.EXTRA_ACCOUNT, contactId)
            intent.putExtra(Extras.EXTRA_CUSTOMIZATION, customization)
            intent.putExtra(Extras.EXTRA_CUSTOMIZATION, customization)
            if (anchor != null) {
                intent.putExtra(Extras.EXTRA_ANCHOR, anchor)
            }
            intent.setClass(context, ChatActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

            context.startActivity(intent)

        }
    }

    /**
     * 系统通知监听
     */
    private var customNotificationObserver: Observer<CustomNotification> =
        Observer { customNotification ->
            if (customNotification.content != null) {
                val customerMsgBean =
                    Gson().fromJson<CustomerMsgBean>(
                        customNotification.content,
                        CustomerMsgBean::class.java
                    )
                when (customerMsgBean.type) {
                    2 -> {//对方删除自己,本地删除会话列表
                        NIMClient.getService(MsgService::class.java)
                            .deleteRecentContact2(customerMsgBean.accid ?: "", SessionTypeEnum.P2P)
                        EventBus.getDefault().post(UpdateContactBookEvent())
                        val intent = Intent()
                        intent.setClass(this@ChatActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    }
                }
                Log.d(
                    "OkHttp",
                    "${customerMsgBean.type}=================${customerMsgBean.msg}================="
                )
            }
        }


    /**
     * 命令消息接收观察者
     */
    private
    val commandObserver = Observer<CustomNotification> { message ->
        if (sessionId != message.sessionId || message.sessionType != SessionTypeEnum.P2P) {
            return@Observer
        }
        //showCommandMessage(message)
    }

    /**
     * 用户信息变更观察者
     */
    private
    val userInfoObserver = UserInfoObserver { accounts ->
        if (!accounts.contains(sessionId)) {
            return@UserInfoObserver
        }
        requestBuddyInfo()
    }


    /**
     * 好友资料变更（eg:关系）
     */
    private
    val friendDataChangedObserver = object : ContactChangedObserver {
        override fun onAddedOrUpdatedFriends(accounts: List<String>) {
            chatName.text = UserInfoHelper.getUserTitleName(
                sessionId,
                SessionTypeEnum.P2P
            )
        }

        override fun onDeletedFriends(accounts: List<String>) {
            chatName.text = UserInfoHelper.getUserTitleName(
                sessionId,
                SessionTypeEnum.P2P
            )
        }

        override fun onAddUserToBlackList(account: List<String>) {
            chatName.text = UserInfoHelper.getUserTitleName(
                sessionId,
                SessionTypeEnum.P2P
            )
        }

        override fun onRemoveUserFromBlackList(account: List<String>) {
            chatName.text = UserInfoHelper.getUserTitleName(
                sessionId,
                SessionTypeEnum.P2P
            )
        }
    }

    /**
     * 好友在线状态观察者
     */
    private val onlineStateChangeObserver =
        OnlineStateChangeObserver { accounts ->
            if (!accounts.contains(sessionId)) {
                return@OnlineStateChangeObserver
            }
            // 按照交互来展示
            displayOnlineState()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BarUtils.setStatusBarColor(this, Color.TRANSPARENT)
            BarUtils.setStatusBarLightMode(this, true)
        }
        AppManager.instance.addActivity(this)
        mHelper = SwipeBackActivityHelper(this)
        mHelper.onActivityCreate()
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT)

        // 单聊特例话数据，包括个人信息，
        requestBuddyInfo()
        displayOnlineState()
        registerObservers(true)
    }

    override fun onDestroy() {
        super.onDestroy()
//        EventBus.getDefault().post(UpdateHiEvent())
        registerObservers(false)
        if (KeyboardUtils.isSoftInputVisible(this))
            KeyboardUtils.hideSoftInput(this)
        AppManager.instance.finishActivity(this)
    }

    override fun onResume() {
        super.onResume()
        isResume = true
    }

    override fun onStop() {
        super.onStop()
        isResume = false
    }

    private fun requestBuddyInfo() {
        if (sessionId == Constants.ASSISTANT_ACCID) {
            chatMore.visibility = View.INVISIBLE
        } else {
            chatMore.setVisible(true)
        }
        chatName.text = UserInfoHelper.getUserTitleName(
            sessionId,
            SessionTypeEnum.P2P
        )
    }

    private fun displayOnlineState() {
        if (!NimUIKitImpl.enableOnlineState()) {
            return
        }
        val detailContent =
            NimUIKitImpl.getOnlineStateContentProvider()
                .getDetailDisplay(sessionId)
//        setSubTitle(detailContent)
//        chatName.text = "${UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P)}-$detailContent"
        chatName.text = "${UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P)}"
    }

    private fun registerObservers(register: Boolean) {
//        NIMClient.getService(MsgServiceObserve::class.java).observeCustomNotification(commandObserver, register)
        NimUIKit.getUserInfoObservable()
            .registerObserver(userInfoObserver, register)
//        NIMClient.getService(MsgServiceObserve::class.java).observeCustomNotification(customNotificationObserver, register)
        NimUIKit.getContactChangedObservable().registerObserver(
            friendDataChangedObserver,
            register
        )
        if (NimUIKit.enableOnlineState()) {
            NimUIKit.getOnlineStateChangeObservable()
                .registerOnlineStateChangeListeners(
                    onlineStateChangeObserver,
                    register
                )
        }
    }


    private fun showCommandMessage(message: CustomNotification) {
        if (!isResume) {
            return
        }
        val content = message.content
        try {
            val json = JSON.parseObject(content)
            val id = json.getIntValue("id")
            if (id == 1) {
                // 正在输入
                inputTip.text = "对方正在输入..."
            } else {
                inputTip.text = ""
            }
        } catch (ignored: Exception) {

        }

    }

    override fun fragment(): ChatMessageFragment {
        val arguments = intent.extras
        arguments!!.putSerializable(
            Extras.EXTRA_TYPE,
            SessionTypeEnum.P2P
        )
        val fragment = ChatMessageFragment()
        fragment.arguments = arguments
        fragment.containerId = R.id.message_fragment_container
        return fragment
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_chat
    }

    override fun initToolBar() {
        btnBack.onClick {
            KeyboardUtils.hideSoftInput(this)
            finish()
        }
        //打开聊天信息
        chatMore.onClick {
            MessageInfoActivity.startActivity(this, sessionId)
        }
    }

    override fun enableSensor(): Boolean {
        return true
    }


    override fun onBackPressed() {
        if (KeyboardUtils.isSoftInputVisible(this))
            KeyboardUtils.hideSoftInput(this)
        EventBus.getDefault().postSticky(UpdateHiEvent())
        super.onBackPressed()
    }

    /*------------------------侧滑退出-----------------*/
    private lateinit var mHelper: SwipeBackActivityHelper

    override fun getSwipeBackLayout(): SwipeBackLayout {
        return mHelper.swipeBackLayout
    }

    override fun setSwipeBackEnable(enable: Boolean) {
        swipeBackLayout.setEnableGesture(enable)
    }

    override fun scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this)
        if (KeyboardUtils.isSoftInputVisible(this))
            KeyboardUtils.hideSoftInput(this)
        swipeBackLayout.scrollToFinishActivity()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mHelper.onPostCreate()

    }

}
