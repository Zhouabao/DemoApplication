package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.NimIntent
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.baselibrary.utils.ChannelUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.AllMsgCount
import com.sdy.jitangapplication.presenter.MainPresenter
import com.sdy.jitangapplication.presenter.view.MainView
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.dialog.AccountDangerDialog
import com.sdy.jitangapplication.ui.dialog.ChangeAvatarRealManDialog
import com.sdy.jitangapplication.ui.dialog.GotoVerifyDialog
import com.sdy.jitangapplication.ui.dialog.TouristDialog
import com.sdy.jitangapplication.ui.fragment.*
import com.sdy.jitangapplication.utils.AMapManager
import com.sdy.jitangapplication.utils.UserManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.callback.Callback
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Call
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.startActivity
import java.util.*

class MainActivity : BaseMvpActivity<MainPresenter>(), MainView, View.OnClickListener {
    private val titles by lazy {
        arrayOf(
            getString(R.string.tab_heart), getString(R.string.tab_find), getString(
                R.string.tab_dating
            ), getString(R.string.tab_message), getString(R.string.tab_mine)
        )
    }

    companion object {
        const val REQUEST_LABEL_CODE = 2000
        const val POSITION_INDEX = 0
        const val POSITION_CONTENT = 1
        const val POSITION_DATING = 2
        const val POSITION_MESSAGE = 3
        const val POSITION_MINE = 4

        fun start(context: Context, intent: Intent) {
            context.startActivity(intent.setClass(context, MainActivity::class.java))
        }

        fun start(context: Context, clearTop: Boolean = true) {
            if (clearTop)
                context.startActivity(context.intentFor<MainActivity>().clearTask().newTask())
            else
                context.startActivity<MainActivity>()
        }
    }

    //fragment栈管理
    private val mStack = Stack<Fragment>()

    //首页
    private val indexFragment by lazy { IndexFragment() }

    //广场
    private val contentFragment by lazy { ContentFragment() }

    //约会
    private val datingFragment by lazy { DatingFragment() }

    //消息
    private val messageListFragment by lazy { MessageListFragment() }

    //我
    private val myFragment by lazy { UserCenterFragment() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        initView()

        getCode()

        Log.d("channel", ChannelUtils.getChannel(this))

        //启动时间统计
        if (!UserManager.touristMode)
            mPresenter.startupRecord(
                UserManager.getProvince(),
                UserManager.getCity()
            )

        //如果定位信息没有就重新定位
        AMapManager.initLocation(this)



        if (UserManager.getAccountDanger() || UserManager.getAccountDangerAvatorNotPass()) {
            //0未认证/认证不成功     1认证通过     2认证中
            if (UserManager.isUserVerify() == 0) {
//                if (UserManager.getAccountDanger())
//                    onAccountDangerEvent(AccountDangerEvent(AccountDangerDialog.VERIFY_NEED_ACCOUNT_DANGER))
//                else
//                    onAccountDangerEvent(AccountDangerEvent(AccountDangerDialog.VERIFY_NEED_AVATOR_INVALID))
            } else if (UserManager.isUserVerify() == 2) {
                onAccountDangerEvent(AccountDangerEvent(AccountDangerDialog.VERIFY_ING))
            }
        }


//        if (UserManager.getAccountDanger() || UserManager.getAccountDangerAvatorNotPass()) {
//            0未认证/认证不成功     1认证通过     2认证中
//            if (UserManager.isUserVerify() == 0) {
//                if (UserManager.getAccountDanger())
//                    onForceFaceEvent(ForceFaceEvent(VerifyForceDialog.FORCE_FAIL_MV))
//                else
//                    onForceFaceEvent(ForceFaceEvent(VerifyForceDialog.FORCE_FAIL_AVATAR))
//            } else if (UserManager.isUserVerify() == 2) {
//                onForceFaceEvent(ForceFaceEvent(VerifyForceDialog.FORCE_GOING))
//            }
//        }

    }



    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        parseIntents()
    }

    private fun parseIntents() {
        // 可以获取消息的发送者，跳转到指定的单聊、群聊界面。
        if (intent != null && intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
            val messages =
                intent.getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT) as ArrayList<IMMessage>?
            if (messages != null && messages.size > 0) {
                val message = messages[0]
                intent.removeExtra(NimIntent.EXTRA_NOTIFY_CONTENT)
                when (message.sessionType) {
                    SessionTypeEnum.P2P -> {
                        //跳转到消息列表
                        vpMain.currentItem = POSITION_MESSAGE
//                        ChatActivity.start(this, message.sessionId)
                    }
                }
            }
        }
    }


    private fun initView() {
        EventBus.getDefault().register(this)
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeReceiveMessage(incomingMessageObserver, true)
        //首页禁止滑动
        setSwipeBackEnable(false)
        mPresenter = MainPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        initFragment()
        //进入页面弹消息提醒
        mPresenter.msgList()


//        val params = tabBg.layoutParams as ConstraintLayout.LayoutParams
//        params.width = ScreenUtils.getScreenWidth()
//        params.height = (71 / 375F * params.width).toInt()
    }


    /*
      初始化Fragment栈管理
   */
    private fun initFragment() {
        tabMatchCount.setOnClickListener(this)
        tabSquare.setOnClickListener(this)
        tabMe.setOnClickListener(this)
        tabMessage.setOnClickListener(this)
        tabDating.clickWithTrigger {
            if (vpMain.currentItem != POSITION_DATING) {
                vpMain.currentItem = POSITION_DATING
            } else {
                if (UserManager.touristMode) {
                    TouristDialog(this@MainActivity).show()
                } else {
                    CommonFunction.checkPublishDating(this)
                }
            }
        }

        mStack.add(indexFragment)
        mStack.add(contentFragment)
        mStack.add(datingFragment)
        mStack.add(messageListFragment)
        mStack.add(myFragment)
        vpMain.adapter = MainPagerAdapter(supportFragmentManager, mStack, titles)
        vpMain.currentItem = POSITION_INDEX
        switchTab(vpMain.currentItem)
        vpMain.setScrollable(false)
        vpMain.offscreenPageLimit = 5
        vpMain.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (UserManager.touristMode && position != POSITION_INDEX && position != POSITION_CONTENT) {
                    TouristDialog(this@MainActivity).show()
                } else {
                    if (position == POSITION_MINE) {
                        EventBus.getDefault().postSticky(UserCenterEvent(true))
                    }
                    if (position != POSITION_DATING || position != POSITION_MINE) {
                        EventBus.getDefault().post(DatingStopPlayEvent())
                    }
                    switchTab(position)
                }
            }
        })

    }


    /**
     * 切换tab
     */
    private fun switchTab(position: Int) {
        when (position) {
            0 -> {
                tabMatchCount.setTextColor(resources.getColor(R.color.colorOrange))
                tabMatchCount.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_match_checked),
                    null,
                    null
                )
                tabSquare.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabSquare.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_square),
                    null,
                    null
                )
                tabDating.setImageResource(R.drawable.icon_tab_dating)

                tabMe.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabMe.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_me),
                    null,
                    null
                )
                tabMessage.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabMessage.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_message),
                    null,
                    null
                )
            }
            1 -> {
                tabMatchCount.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabMatchCount.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_match),
                    null,
                    null
                )
                tabSquare.setTextColor(resources.getColor(R.color.colorOrange))
                tabSquare.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_square_checked),
                    null,
                    null
                )
                tabDating.setImageResource(R.drawable.icon_tab_dating)

                tabMe.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabMe.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_me),
                    null,
                    null
                )
                tabMessage.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabMessage.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_message),
                    null,
                    null
                )
            }
            2 -> {
                tabMatchCount.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabMatchCount.setPadding(0)
                tabMatchCount.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_match),
                    null,
                    null
                )
                tabSquare.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabSquare.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_square),
                    null,
                    null
                )
                tabDating.setImageResource(R.drawable.icon_tab_dating_checked)

                tabMessage.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabMessage.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_message),
                    null,
                    null
                )
                tabMe.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabMe.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_me),
                    null,
                    null
                )
            }
            3 -> {
                tabMatchCount.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabMatchCount.setPadding(0)
                tabMatchCount.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_match),
                    null,
                    null
                )
                tabSquare.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabSquare.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_square),
                    null,
                    null
                )
                tabDating.setImageResource(R.drawable.icon_tab_dating)

                tabMessage.setTextColor(resources.getColor(R.color.colorOrange))
                tabMessage.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_message_checked),
                    null,
                    null
                )
                tabMe.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabMe.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_me),
                    null,
                    null
                )
            }
            4 -> {
                tabMatchCount.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabMatchCount.setPadding(0)
                tabMatchCount.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_match),
                    null,
                    null
                )
                tabSquare.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabSquare.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_square),
                    null,
                    null
                )
                tabDating.setImageResource(R.drawable.icon_tab_dating)

                tabMessage.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabMessage.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_message),
                    null,
                    null
                )
                tabMe.setTextColor(resources.getColor(R.color.colorOrange))
                tabMe.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_me_checked),
                    null,
                    null
                )
            }
        }

    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.tabMatchCount -> {
                vpMain.currentItem = POSITION_INDEX
            }
            R.id.tabSquare -> {
                vpMain.currentItem = POSITION_CONTENT
            }
            R.id.tabMessage -> {
                if (UserManager.touristMode) {
                    TouristDialog(this).show()
                } else
                    vpMain.currentItem = POSITION_MESSAGE
            }
            R.id.tabMe -> {
                if (UserManager.touristMode) {
                    TouristDialog(this).show()
                } else
                    vpMain.currentItem = POSITION_MINE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (KeyboardUtils.isSoftInputVisible(this))
            KeyboardUtils.hideSoftInput(this)
        parseIntents()
        Log.d("OKhttp", "${UserManager.isNeedChangeAvator()},${UserManager.isForceChangeAvator()}}")
        if (UserManager.isNeedChangeAvator())
            if (!UserManager.isForceChangeAvator()) {
                if (UserManager.getChangeAvatorType() == 1)
                    showGotoVerifyDialog(
                        GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS,
                        UserManager.getChangeAvator()
                    )
                else
                    ChangeAvatarRealManDialog(
                        this,
                        ChangeAvatarRealManDialog.VERIFY_NEED_VALID_REAL_MAN,
                        UserManager.getChangeAvator()
                    ).show()
            } else {
                UserManager.saveNeedChangeAvator(false)
                UserManager.saveForceChangeAvator(true)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (gotoVerifyDialog != null) {
            gotoVerifyDialog!!.dismiss()
            gotoVerifyDialog = null
        }
        EventBus.getDefault().unregister(this)
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeReceiveMessage(incomingMessageObserver, false)

    }


    /**
     * 双击退出APP
     */
    private var firstClickTime = 0L

    override fun onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }

        val secondTime = System.currentTimeMillis()
        if (secondTime - firstClickTime > 2000) {
            CommonFunction.toast(getString(R.string.click_to_exit))
            firstClickTime = secondTime
        } else {
            AppUtils.exitApp()
//            ActivityUtils.finishAllActivities()
//            AppManager.instance.finishAllActivity()
//            System.exit(0)//正常退出
//            AppManager.instance.exitApp(this)
        }
    }


    /**
     * 获取未读消息个数
     */
    override fun onMsgListResult(allMsgCount: AllMsgCount?) {
        if (allMsgCount != null) {
            //未读消息个数
            val msgCount = NIMClient.getService(MsgService::class.java).totalUnreadCount
            Log.d(
                "msgcount", "msgcount = ${msgCount},likecount = ${allMsgCount.likecount}" +
                        ",square_count = ${allMsgCount.square_count}"
            )

            showMsgDot(allMsgCount.square_count > 0 || msgCount > 0)
        }
    }


    /**
     * 显示未读消息红点
     */
    private fun showMsgDot(show: Boolean) {
        ivNewMsg.isVisible = show
    }


    /**
     * 消息接收观察者
     */
    private var incomingMessageObserver: Observer<List<IMMessage>> = Observer {
        mPresenter.msgList()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SquarePlayDetailActivity.REQUEST_CODE) {
                EventBus.getDefault().post(NotifyEvent(data!!.getIntExtra("position", -1)))
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onGetMSGEvent(event: GetNewMsgEvent) {
        mPresenter.msgList()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onJumpToDatingEvent(event: JumpToDatingEvent) {
        vpMain.currentItem = POSITION_DATING
    }


    /**
     * 重新认证事件总线
     */
//    const val TYPE_VERIFY = 4//认证失败去认证
//    const val TYPE_CHANGE_AVATOR_NOT_PASS = 7//头像违规替换
//    const val TYPE_CHANGE_ABLUM = 3//完善相册
    private var gotoVerifyDialog: GotoVerifyDialog? = null

    private fun showGotoVerifyDialog(type: Int, avator: String = UserManager.getAvator()) {
        var content = ""
        var title = ""
        var confirmText = ""
        when (type) {
            GotoVerifyDialog.TYPE_VERIFY -> { //认证不通过
                content = getString(R.string.avatar_compare_fail)
                title = getString(R.string.avata_verify_fail)
            }
            GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS -> {//7头像违规
                content = getString(R.string.avatar_not_pass_content)
                title = getString(R.string.avatar_change)
                confirmText = getString(R.string.avator_change_text)
            }
//            GotoVerifyDialog.TYPE_CHANGE_ABLUM -> {//完善相册
//                content = "完善相册会使你的信息更多在匹配页展示\n现在就去完善你的相册吧！"
//                title = "完善相册"
//                confirmText = "完善相册"
//            }

        }
        if (gotoVerifyDialog != null) {
            gotoVerifyDialog!!.dismiss()
            gotoVerifyDialog = null
        }

        gotoVerifyDialog = GotoVerifyDialog.Builder(ActivityUtils.getTopActivity())
            .setTitle(title)
            .setContent(content)
            .setConfirmText(confirmText)
            .setIcon(avator)
            .setIconVisible(true)
            .setType(type)
            .setCancelIconIsVisibility(type != GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS)
            .setOnCancelable(type != GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS)
            .create()
        gotoVerifyDialog?.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onReVerifyEvent(event: ReVerifyEvent) {
        if (accountDangerDialog != null) {
            accountDangerDialog!!.dismiss()
            accountDangerDialog = null
        }

        if (event.type == GotoVerifyDialog.TYPE_CHANGE_AVATOR_REAL_NOT_VALID) {//11
            UserManager.saveNeedChangeAvator(true)//需要换头像
            UserManager.saveForceChangeAvator(false)//是否强制替换过头像
            UserManager.saveChangeAvatorType(2)//真人不合规
            ChangeAvatarRealManDialog(
                ActivityUtils.getTopActivity(),
                ChangeAvatarRealManDialog.VERIFY_NEED_VALID_REAL_MAN,
                event.avator
            ).show()
        } else if (event.type == GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS) { //7
            UserManager.saveNeedChangeAvator(true)//需要换头像
            UserManager.saveForceChangeAvator(false)//是否强制替换过头像
            UserManager.saveChangeAvatorType(1)//头像不合规
            showGotoVerifyDialog(event.type, event.avator)
        }
        if (EventBus.getDefault().getStickyEvent(ReVerifyEvent::class.java) != null) {
            // 若粘性事件存在，将其删除
            EventBus.getDefault()
                .removeStickyEvent(EventBus.getDefault().getStickyEvent(ReVerifyEvent::class.java))
        }
    }

    private var accountDangerDialog: AccountDangerDialog? = null



    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAccountDangerEvent(event: AccountDangerEvent) {
        if (accountDangerDialog != null) {
            accountDangerDialog!!.dismiss()
            accountDangerDialog = null
        }

        if (UserManager.getAccountDanger() || UserManager.getAccountDangerAvatorNotPass()) {
            accountDangerDialog = AccountDangerDialog(ActivityUtils.getTopActivity())
            accountDangerDialog!!.show()
            accountDangerDialog!!.changeVerifyStatus(event.type)
        }
        if (EventBus.getDefault().getStickyEvent(AccountDangerEvent::class.java) != null) {
            // 若粘性事件存在，将其删除
            EventBus.getDefault()
                .removeStickyEvent(
                    EventBus.getDefault().getStickyEvent(AccountDangerEvent::class.java)
                )
        }
    }


    fun getCode() {
        OkHttpUtils.post()
            .url("https://accounts.google.com/o/oauth2/token")
            .addParams("code", "4/1AY0e-g78wChv7S9N9IWpgFJSld26b_xUQg48euTYoZ1tTqI38Tuu-zOJWd0")
            .addParams(
                "client_id",
                "325834510992-tainpp0lkfm8le7hpff1lksoqhe4bjtm.apps.googleusercontent.com"
            )
            .addParams("client_secret", "AIzaSyDAPU5SjWQwgc4kEq7LQ8ibVxjD5aLEVOc")
            .addParams("redirect_uri", "urn:ietf:wg:oauth:2.0:oob")
            .addParams("grant_type", "authorization_code")
            .build()
            .execute(object : Callback<Any>() {
                override fun onResponse(response: Any?, id: Int) {
                    Log.d(TAG1, "onResponse: $response")


                }

                override fun parseNetworkResponse(response: Response?, id: Int): Any {
                    Log.d(TAG1, "onResponse: $response")
                    return response?.body?.string() ?: ""
                }

                override fun onError(call: Call?, e: Exception?, id: Int) {
                    Log.d(TAG1, "onResponse: ${e.toString()}")

                }

            })

    }
}
