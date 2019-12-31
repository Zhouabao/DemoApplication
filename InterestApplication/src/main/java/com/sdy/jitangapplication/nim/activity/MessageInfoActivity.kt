package com.sdy.jitangapplication.nim.activity

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.BarUtils
import com.kotlin.base.common.AppManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.ext.setVisible
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.api.model.contact.ContactChangedObserver
import com.netease.nim.uikit.business.session.helper.MessageListPanelHelper
import com.netease.nim.uikit.business.uinfo.UserInfoHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.friend.FriendService
import com.netease.nimlib.sdk.friend.model.MuteListChangedNotify
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.widgets.swipeback.SwipeBackLayout
import com.sdy.baselibrary.widgets.swipeback.Utils
import com.sdy.baselibrary.widgets.swipeback.app.SwipeBackActivityBase
import com.sdy.baselibrary.widgets.swipeback.app.SwipeBackActivityHelper
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.StarEvent
import com.sdy.jitangapplication.event.UpdateContactBookEvent
import com.sdy.jitangapplication.event.UpdateHiEvent
import com.sdy.jitangapplication.nim.DemoCache
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.activity.MessageHiPastActivity
import com.sdy.jitangapplication.ui.activity.ReportReasonActivity
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_message_info.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 * 好友信息界面
 */
class MessageInfoActivity : UI(), SwipeBackActivityBase, View.OnClickListener {

    private var account: String? = null
    private var star: Boolean = false//是否是星标好友
    private var isfriend: Boolean = false//是否是好友

    companion object {
        private const val FLAG_ADD_FRIEND_DIRECTLY = true // 是否直接加为好友开关，false为需要好友申请
        private const val KEY_BLACK_LIST = "black_list"
        private const val KEY_MSG_NOTICE = "msg_notice"
        private const val KEY_RECENT_STICKY = "recent_contacts_sticky"

        private val TAG = MessageInfoActivity::class.java!!.getSimpleName()

        private const val EXTRA_ACCOUNT = "EXTRA_ACCOUNT"
        private const val IS_STAR = "IS_STAR"
        private const val IS_FRIEND = "IS_FRIEND"
        @JvmStatic
        fun startActivity(context: Context, sessionId: String) {
            context.startActivity<MessageInfoActivity>(
                EXTRA_ACCOUNT to sessionId
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_info)
        EventBus.getDefault().register(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BarUtils.setStatusBarColor(this, Color.TRANSPARENT)
            BarUtils.setStatusBarLightMode(this, true)
        }
        AppManager.instance.addActivity(this)
        mHelper = SwipeBackActivityHelper(this)
        mHelper.onActivityCreate()
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT)


        account = intent.getStringExtra(EXTRA_ACCOUNT)
        initView()
        registerObserver(true)

    }


    private fun initView() {
        btnBack.onClick { finish() }

        GlideUtil.loadCircleImg(this, UserInfoHelper.getAvatar(account), civAvator)
        //TA的主页
        chatDetailBtn.onClick { MatchDetailActivity.start(this, account ?: "") }
        //删除好友
        friendDelete.onClick {
            showDeleteDialog(3)
        }

        //投诉举报
        friendReport.onClick {
            startActivity<ReportReasonActivity>(
                "target_accid" to account,
                "nickname" to UserInfoHelper.getUserName(account)
            )
        }

        //查找聊天记录
        friendHistory.onClick {
            //            MessageHistoryActivity.start(this, account ?: "", SessionTypeEnum.P2P)//查看聊天记录
            SearchMessageActivity.start(this, account ?: "", SessionTypeEnum.P2P)//搜索聊天记录

        }

        //清空聊天记录
        friendHistoryClean.onClick {
            showDeleteDialog(1)

//            val title1 = resources.getString(R.string.message_p2p_clear_tips)
//            val alertDialog = CustomAlertDialog(this)
//            alertDialog.setTitle1(title1)
//            alertDialog.addItem("确定") {
//                NIMClient.getService(MsgService::class.java)
//                    .clearServerHistory(account ?: "", SessionTypeEnum.P2P, true)
//                MessageListPanelHelper.getInstance().notifyClearMessages(account ?: "")
//            }
            //漫游
//            val itemText = resources.getString(R.string.sure_keep_roam)
//            alertDialog.addItem(itemText) {
//                NIMClient.getService(MsgService::class.java).clearServerHistory(
//                    account ?: "", SessionTypeEnum.P2P, false
//                )
//                MessageListPanelHelper.getInstance().notifyClearMessages(account)
//            }
//            alertDialog.addItem(
//                "取消"
//            ) { }
//            alertDialog.show()
        }

        //消息免打扰
        llNoBother.setOnClickListener(this)
        //星标好友
        llstar.setOnClickListener(this)
    }

    //显示删除对话
    //1.清空聊天记录  2.投诉举报   3.删除好友
    private fun showDeleteDialog(type: Int) {
        val dialog = DeleteDialog(this)
        dialog.show()
        when (type) {
            1 -> {
                dialog.title.text = "清空消息"
                dialog.tip.text = resources.getString(R.string.message_p2p_clear_tips)
                dialog.cancel.onClick { dialog.dismiss() }
                dialog.confirm.onClick {
                    NIMClient.getService(MsgService::class.java)
                        .clearServerHistory(account ?: "", SessionTypeEnum.P2P, true)
                    MessageListPanelHelper.getInstance().notifyClearMessages(account ?: "")
                    dialog.dismiss()
                }
            }
            3 -> {
                if (isfriend) {
                    dialog.title.text = "删除好友"
                    dialog.tip.text = "确定删除该好友?"
                    dialog.cancel.onClick { dialog.dismiss() }
                    dialog.confirm.onClick {
                        deleteFriends()
                        dialog.dismiss()
                    }
                } else {
                    dialog.title.text = "删除招呼"
                    dialog.tip.text = "确定删除该招呼?"
                    dialog.cancel.onClick { dialog.dismiss() }
                    dialog.confirm.onClick {
                        removeGreet()
                        dialog.dismiss()


//                        AppManager.instance.finishAllActivity()
//                        startActivity<MainActivity>()
                    }
                }
            }
        }
    }

    override fun onClick(p0: View) {
        when (p0.id) {
            R.id.llNoBother, R.id.friendNoBother -> {
                NIMClient.getService(FriendService::class.java).setMessageNotify(
                    account,
                    !NIMClient.getService(FriendService::class.java).isNeedMessageNotify(account)
                )
                    .setCallback(object : RequestCallback<Void?> {
                        override fun onSuccess(param: Void?) {
                            friendNoBother.isChecked = !friendNoBother.isChecked

                        }

                        override fun onFailed(code: Int) {
                        }

                        override fun onException(exception: Throwable) {

                        }
                    })
            }
            R.id.llstar, R.id.friendStar -> {//星标好友
                if (star) {
                    removeStar()
                } else {
                    addStar()
                }
            }
        }

    }


    internal var muteListChangedNotifyObserver: Observer<MuteListChangedNotify> =
        Observer { notify -> friendNoBother.isChecked = notify.isMute }


    internal var friendDataChangedObserver: ContactChangedObserver = object : ContactChangedObserver {
        override fun onAddedOrUpdatedFriends(account: List<String>) {
            updateUserOperatorView()
        }

        override fun onDeletedFriends(account: List<String>) {
            updateUserOperatorView()
        }

        override fun onAddUserToBlackList(account: List<String>) {
            updateUserOperatorView()
        }

        override fun onRemoveUserFromBlackList(account: List<String>) {
            updateUserOperatorView()
        }
    }

    private fun updateUserOperatorView() {
        if (isfriend) {
            friendDelete.visibility = View.VISIBLE
            deleteTv.text = "删除好友"
        } else {
            friendDelete.visibility = View.VISIBLE
            deleteTv.text = "删除招呼"
        }
    }

    /*// 以不接收testAccount帐号消息为例
    NIMClient.getService(FriendService.class).setMessageNotify("testAccount", false).setCallback(new RequestCallback<Void>() {});*/

    private fun registerObserver(register: Boolean) {
//        NimUIKit.getContactChangedObservable().registerObserver(friendDataChangedObserver, register)
//        NIMClient.getService(FriendServiceObserve::class.java).observeMuteListChangedNotify(muteListChangedNotifyObserver, register)
    }

    private fun updateUserInfo() {
        if (NimUIKit.getUserInfoProvider().getUserInfo(account) != null) {
            updateUserInfoView()
            return
        }

        NimUIKit.getUserInfoProvider().getUserInfoAsync(account) { success, result, code -> updateUserInfoView() }
    }

    private fun updateUserInfoView() {
        chatName.text = UserInfoHelper.getUserName(account)
    }


    private fun updateToggleView() {
        if (DemoCache.getAccount() != null && !DemoCache.getAccount().equals(account)) {
            //黑名单
            val black = NIMClient.getService(FriendService::class.java).isInBlackList(account)
//            setToggleBtn(friendReport, black)

            //消息提醒  * @return true表示需要消息提醒；false表示静音
            friendNoBother.isChecked = !NIMClient.getService(FriendService::class.java).isNeedMessageNotify(account)
        }
    }


    override fun onResume() {
        super.onResume()
        updateUserInfo()
        updateToggleView()
    }

    override fun onDestroy() {
        super.onDestroy()
        registerObserver(false)
        EventBus.getDefault().unregister(this)
        AppManager.instance.finishActivity(this)

    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onStarEvent(event: StarEvent) {
        star = event.stared
        isfriend = event.isfriend
        //是好友才显示星标
        flStar.setVisible(isfriend)
        if (isfriend) {
            friendStar.isChecked = star
            deleteTv.text = "删除好友"
        } else {
            deleteTv.text = "删除招呼"
        }

    }


    /*--------------------------接口请求------------------------*/


    /**
     * 移除星标
     */
    private fun removeStar() {
        val params = UserManager.getBaseParams()
        params["target_accid"] = account ?: ""
        RetrofitFactory.instance.create(Api::class.java)
            .removeStarTarget(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        star = false
                        friendStar.isChecked = false
                        EventBus.getDefault().post(UpdateContactBookEvent())
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    CommonFunction.toast(CommonFunction.getErrorMsg(this@MessageInfoActivity))
                }
            })
    }

    /**
     * 添加星标
     */
    private fun addStar() {
        val params = UserManager.getBaseParams()
        params["target_accid"] = account ?: ""
        RetrofitFactory.instance.create(Api::class.java)
            .addStarTarget(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        star = true
                        friendStar.isChecked = true
                        EventBus.getDefault().post(UpdateContactBookEvent())
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    CommonFunction.toast(CommonFunction.getErrorMsg(this@MessageInfoActivity))

                }
            })

    }


    /**
     * 删除招呼
     */
    private fun removeGreet() {
        val params = UserManager.getBaseParams()
        params["target_accid"] = account ?: ""
        RetrofitFactory.instance.create(Api::class.java)
            .removeGreet(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        NIMClient.getService(MsgService::class.java).deleteRecentContact2(account, SessionTypeEnum.P2P)
                        NIMClient.getService(MsgService::class.java).clearServerHistory(account, SessionTypeEnum.P2P)
                        if (AppUtils.isAppForeground() && ActivityUtils.isActivityAlive(MessageInfoActivity::class.java.newInstance()))
                            ActivityUtils.finishActivity(MessageInfoActivity::class.java)
                        if (AppUtils.isAppForeground() && ActivityUtils.isActivityAlive(ChatActivity::class.java.newInstance()))
                            ActivityUtils.finishActivity(ChatActivity::class.java)
                        if (AppUtils.isAppForeground() && ActivityUtils.isActivityAlive(MessageHiPastActivity::class.java.newInstance()))
                            ActivityUtils.finishActivity(MessageHiPastActivity::class.java)
                        EventBus.getDefault().post(UpdateHiEvent())
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    CommonFunction.toast(CommonFunction.getErrorMsg(this@MessageInfoActivity))
                }
            })

    }


    /**
     * 删除好友
     */
    private fun deleteFriends() {
        val params = UserManager.getBaseParams()
        params["target_accid"] = (account ?: "")

        RetrofitFactory.instance.create(Api::class.java)
            .dissolutionFriend(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        CommonFunction.dissolveRelationship(account ?: "")
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    CommonFunction.toast(CommonFunction.getErrorMsg(this@MessageInfoActivity))

                }
            })

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
        swipeBackLayout.scrollToFinishActivity()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mHelper.onPostCreate()
    }
}
