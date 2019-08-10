package com.example.demoapplication.nim.activity

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.Switch
import com.blankj.utilcode.util.ToastUtils
import com.example.demoapplication.R
import com.example.demoapplication.nim.DemoCache
import com.example.demoapplication.nim.sp.UserPreferences
import com.example.demoapplication.ui.activity.MatchDetailActivity
import com.kotlin.base.ext.onClick
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.api.model.contact.ContactChangedObserver
import com.netease.nim.uikit.business.recent.RecentContactsFragment
import com.netease.nim.uikit.business.session.helper.MessageListPanelHelper
import com.netease.nim.uikit.business.uinfo.UserInfoHelper
import com.netease.nim.uikit.common.CommonUtil
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog
import com.netease.nim.uikit.common.ui.dialog.DialogMaker
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper
import com.netease.nim.uikit.common.ui.dialog.EasyEditDialog
import com.netease.nim.uikit.common.util.sys.NetworkUtil
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.friend.FriendService
import com.netease.nimlib.sdk.friend.FriendServiceObserve
import com.netease.nimlib.sdk.friend.constant.VerifyType
import com.netease.nimlib.sdk.friend.model.AddFriendData
import com.netease.nimlib.sdk.friend.model.MuteListChangedNotify
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import kotlinx.android.synthetic.main.activity_message_info.*
import org.jetbrains.anko.startActivity

/**
 * 好友信息界面
 */
class MessageInfoActivity : UI(), CompoundButton.OnCheckedChangeListener {

    private var account: String? = null

    companion object {
        private val FLAG_ADD_FRIEND_DIRECTLY = true // 是否直接加为好友开关，false为需要好友申请
        private val KEY_BLACK_LIST = "black_list"
        private val KEY_MSG_NOTICE = "msg_notice"
        private val KEY_RECENT_STICKY = "recent_contacts_sticky"

        private val TAG = MessageInfoActivity::class.java!!.getSimpleName()

        private val EXTRA_ACCOUNT = "EXTRA_ACCOUNT"
        @JvmStatic
        fun startActivity(context: Context, sessionId: String) {
            context.startActivity<MessageInfoActivity>(EXTRA_ACCOUNT to sessionId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_info)
        account = intent.getStringExtra(EXTRA_ACCOUNT)

        initView()
        registerObserver(true)

    }

    private fun initView() {
        btnBack.onClick { finish() }
        //TA的主页
        chatDetailBtn.onClick { MatchDetailActivity.start(this, account ?: "") }
        //删除好友
        friendDelete.onClick {
            if (NIMClient.getService(FriendService::class.java).isMyFriend(account)) {
                onRemoveFriend()
            } else {
                //通过验证添加好友
                onAddFriendByVerify()
                //直接添加好友
//                doAddFriend(null,true)
            }
        }

        //查找聊天记录
        friendHistory.onClick {
//            MessageHistoryActivity.start(this, account ?: "", SessionTypeEnum.P2P)//查看聊天记录
            SearchMessageActivity.start(this, account ?: "", SessionTypeEnum.P2P)//搜索聊天记录

        }

        //清空聊天记录
        friendHistoryClean.onClick {
            val title = resources.getString(R.string.message_p2p_clear_tips)
            val alertDialog = CustomAlertDialog(this)
            alertDialog.setTitle(title)
            alertDialog.addItem("确定") {
                NIMClient.getService(MsgService::class.java).clearServerHistory(
                    account ?: "", SessionTypeEnum.P2P
                )
                MessageListPanelHelper.getInstance().notifyClearMessages(account ?: "")
            }
            val itemText = resources.getString(R.string.sure_keep_roam)
            alertDialog.addItem(itemText) {
                NIMClient.getService(MsgService::class.java).clearServerHistory(
                    account ?: "", SessionTypeEnum.P2P, false
                )
                MessageListPanelHelper.getInstance().notifyClearMessages(account)
            }
            alertDialog.addItem(
                "取消"
            ) { }
            alertDialog.show()
        }

        //消息免打扰
        friendNoBother.setOnCheckedChangeListener(this)
        //星标好友
        friendStar.setOnCheckedChangeListener(this)
    }


    override fun onCheckedChanged(p0: CompoundButton, checkState: Boolean) {
        when (p0.id) {
            R.id.friendNoBother -> {
                NIMClient.getService(FriendService::class.java).setMessageNotify(account, checkState)
                    .setCallback(object : RequestCallback<Void?> {
                        override fun onSuccess(param: Void?) {
                            if (checkState) {
                                ToastHelper.showToast(this@MessageInfoActivity, "免打扰已开启")
                            } else {
                                ToastHelper.showToast(this@MessageInfoActivity, "免打扰已关闭")
                            }
                        }

                        override fun onFailed(code: Int) {
                            if (code == 408) {
                                ToastHelper.showToast(this@MessageInfoActivity, R.string.network_is_not_available)
                            } else {
                                ToastHelper.showToast(this@MessageInfoActivity, "on failed:$code")
                            }
//                            friendNoBother.isChecked = !checkState
                        }

                        override fun onException(exception: Throwable) {

                        }
                    })


            }
            R.id.friendStar -> {//星标好友
                //置顶
                //查询之前是不是存在会话记录
                val recentContact =
                    NIMClient.getService(MsgService::class.java).queryRecentContact(account, SessionTypeEnum.P2P)
                if (checkState) {
                    //如果之前不存在，创建一条空的会话记录
                    if (recentContact == null) {
                        // RecentContactsFragment 的 MsgServiceObserve#observeRecentContact 观察者会收到通知
                        NIMClient.getService(MsgService::class.java).createEmptyRecentContact(
                            account,
                            SessionTypeEnum.P2P,
                            RecentContactsFragment.RECENT_TAG_STICKY,
                            System.currentTimeMillis(),
                            true
                        )
                    } else {
                        CommonUtil.addTag(recentContact, RecentContactsFragment.RECENT_TAG_STICKY)
                        NIMClient.getService(MsgService::class.java).updateRecentAndNotify(recentContact)
                    }// 之前存在，更新置顶flag
                } else {
                    if (recentContact != null) {
                        CommonUtil.removeTag(recentContact, RecentContactsFragment.RECENT_TAG_STICKY)
                        NIMClient.getService(MsgService::class.java).updateRecentAndNotify(recentContact)
                    }
                }//取消置顶


            }
        }


    }


    internal var muteListChangedNotifyObserver: Observer<MuteListChangedNotify> =
        Observer { notify -> setToggleBtn(friendNoBother, notify.isMute) }


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
        if (NIMClient.getService(FriendService::class.java).isMyFriend(account)) {
            friendDelete.visibility = View.VISIBLE
            deleteTv.text = "删除好友"
        } else {
            friendDelete.visibility = View.VISIBLE
            deleteTv.text = "加为好友"
        }
    }

    private fun setToggleBtn(btn: Switch, isChecked: Boolean) {
        btn.isChecked = isChecked
    }


    private fun registerObserver(register: Boolean) {
        NimUIKit.getContactChangedObservable().registerObserver(friendDataChangedObserver, register)
        NIMClient.getService(FriendServiceObserve::class.java)
            .observeMuteListChangedNotify(muteListChangedNotifyObserver, register)
    }

    private fun updateUserInfo() {
        if (NimUIKit.getUserInfoProvider().getUserInfo(account) != null) {
            updateUserInfoView()
            return
        }

        NimUIKit.getUserInfoProvider().getUserInfoAsync(
            account
        ) { success, result, code -> updateUserInfoView() }
    }

    private fun updateUserInfoView() {
        chatName.text = UserInfoHelper.getUserName(account)
    }


    private fun updateToggleView() {
        if (DemoCache.getAccount() != null && !DemoCache.getAccount().equals(account)) {
            //黑名单
            val black = NIMClient.getService(FriendService::class.java).isInBlackList(account)
            //消息通知
            val notice = NIMClient.getService(FriendService::class.java).isNeedMessageNotify(account)

//            setToggleBtn(friendReport, black)

            //消息提醒
            setToggleBtn(friendNoBother, !notice)

            if (NIMClient.getService(FriendService::class.java).isMyFriend(account)) {
                val recentContact =
                    NIMClient.getService(MsgService::class.java).queryRecentContact(account, SessionTypeEnum.P2P)
                val isSticky = recentContact != null && CommonUtil.isTagSet(
                    recentContact,
                    RecentContactsFragment.RECENT_TAG_STICKY
                )
                //消息置顶
                setToggleBtn(friendStar, isSticky)
            }
            updateUserOperatorView()
        }
    }


    //删除好友
    private fun onRemoveFriend() {
        Log.i(TAG, "onRemoveFriend")
        if (!NetworkUtil.isNetAvailable(this)) {
            ToastHelper.showToast(this, R.string.network_is_not_available)
            return
        }
        val dialog = EasyAlertDialogHelper.createOkCancelDiolag(this, getString(R.string.remove_friend),
            getString(R.string.remove_friend_tip), true,
            object : EasyAlertDialogHelper.OnDialogActionListener {

                override fun doCancelAction() {

                }

                override fun doOkAction() {
                    DialogMaker.showProgressDialog(this@MessageInfoActivity, "", true)
                    val deleteAlias = UserPreferences.isDeleteFriendAndDeleteAlias()
                    NIMClient.getService(FriendService::class.java).deleteFriend(account, deleteAlias)
                        .setCallback(object :
                            RequestCallback<Void?> {
                            override fun onSuccess(param: Void?) {
                                DialogMaker.dismissProgressDialog()
                                ToastUtils.showShort(resources.getString(R.string.remove_friend_success))
                                finish()
                            }

                            override fun onFailed(code: Int) {
                                DialogMaker.dismissProgressDialog()
                                if (code == 408) {
                                    ToastUtils.showShort(resources.getString(R.string.network_is_not_available))
                                } else {
                                    ToastUtils.showShort("on failed:$code")
                                }
                            }

                            override fun onException(exception: Throwable) {
                                DialogMaker.dismissProgressDialog()
                            }
                        })
                }
            })
        if (!isFinishing && !isDestroyedCompatible()) {
            dialog.show()
        }
    }

    /**
     * 通过验证方式添加好友
     */
    private fun onAddFriendByVerify() {
        val requestDialog = EasyEditDialog(this)
        requestDialog.setEditTextMaxLength(32)
        requestDialog.setTitle(getString(R.string.add_friend_verify_tip))
        requestDialog.addNegativeButtonListener(R.string.cancel) { requestDialog.dismiss() }
        requestDialog.addPositiveButtonListener(R.string.send) {
            requestDialog.dismiss()
            val msg = requestDialog.editMessage
            doAddFriend(msg, false)
        }
        requestDialog.setOnCancelListener { }
        requestDialog.show()
    }

    //添加好友  (null,false) 直接添加  (onAddFriendByVerify)通过验证添加
    private fun doAddFriend(msg: String?, addDirectly: Boolean) {
        if (!NetworkUtil.isNetAvailable(this)) {
            ToastHelper.showToast(this@MessageInfoActivity, R.string.network_is_not_available)
            return
        }
        if (!TextUtils.isEmpty(account) && account == DemoCache.getAccount()) {
            ToastHelper.showToast(this@MessageInfoActivity, "不能加自己为好友")
            return
        }
        val verifyType = if (addDirectly) VerifyType.DIRECT_ADD else VerifyType.VERIFY_REQUEST
        DialogMaker.showProgressDialog(this, "", true)
        NIMClient.getService(FriendService::class.java).addFriend(AddFriendData(account, verifyType, msg))
            .setCallback(object : RequestCallback<Void?> {
                override fun onSuccess(param: Void?) {
                    DialogMaker.dismissProgressDialog()
                    updateUserOperatorView()
                    if (VerifyType.DIRECT_ADD == verifyType) {
                        ToastHelper.showToast(this@MessageInfoActivity, "添加好友成功")
                    } else {
                        ToastHelper.showToast(this@MessageInfoActivity, "添加好友请求发送成功")
                    }
                }

                override fun onFailed(code: Int) {
                    DialogMaker.dismissProgressDialog()
                    if (code == 408) {
                        ToastHelper.showToast(this@MessageInfoActivity, R.string.network_is_not_available)
                    } else {
                        ToastHelper.showToast(this@MessageInfoActivity, "on failed:$code")
                    }
                }

                override fun onException(exception: Throwable) {
                    DialogMaker.dismissProgressDialog()
                }
            })

        Log.i(TAG, "onAddFriendByVerify")
    }


    override fun onResume() {
        super.onResume()
        updateUserInfo()
        updateToggleView()
    }

    override fun onDestroy() {
        super.onDestroy()
        registerObserver(false)
    }
}
