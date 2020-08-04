package com.sdy.jitangapplication.ui.activity

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.*
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.NimIntent
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.StatusCode
import com.netease.nimlib.sdk.auth.AuthServiceObserver
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.baselibrary.utils.CustomClickListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.AllMsgCount
import com.sdy.jitangapplication.model.NearCountBean
import com.sdy.jitangapplication.presenter.MainPresenter
import com.sdy.jitangapplication.presenter.view.MainView
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.dialog.*
import com.sdy.jitangapplication.ui.fragment.ContentFragment
import com.sdy.jitangapplication.ui.fragment.IndexFragment
import com.sdy.jitangapplication.ui.fragment.MessageListFragment
import com.sdy.jitangapplication.ui.fragment.UserCenterFragment
import com.sdy.jitangapplication.utils.AMapManager
import com.sdy.jitangapplication.utils.UserManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import java.util.*

class MainActivity : BaseMvpActivity<MainPresenter>(), MainView, View.OnClickListener {
    private val titles = arrayOf("心动", "发现", "消息", "我的")

    //fragment栈管理
    private val mStack = Stack<Fragment>()

    //首页
    private val indexFragment by lazy { IndexFragment() }

    //广场
    private val contentFragment by lazy { ContentFragment() }

    //消息
    private val messageListFragment by lazy { MessageListFragment() }

    //我
    private val myFragment by lazy { UserCenterFragment() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        initView()

        //启动时间统计
        if (!UserManager.touristMode)
            mPresenter.startupRecord(
                UserManager.getProvince(),
                UserManager.getCity()
            )

        //如果定位信息没有就重新定位
        AMapManager.initLocation(this)
        filterBtn.setOnClickListener(this)



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
                        vpMain.currentItem = 2
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
        labelAddBtn.setOnClickListener(this)
        mPresenter = MainPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        initFragment()
        //进入页面弹消息提醒
        mPresenter.msgList()
    }


    /*
      初始化Fragment栈管理
   */
    private fun initFragment() {
        tabMatchCount.setOnClickListener(this)
        tabSquare.setOnClickListener(this)
        tabSquarePublish.onClick(
            object : CustomClickListener() {
                override fun onSingleClick(view: View) {
                    try {
                        //游客模式则提醒登录
                        if (UserManager.touristMode) {
                            TouristDialog(this@MainActivity).show()
                        } else
                            contentFragment.mPresenter.checkBlock()
                    } catch (e: Exception) {

                    }
                }
            }
        )
        tabMe.setOnClickListener(this)
        tabMessage.setOnClickListener(this)

        mStack.add(indexFragment)
        mStack.add(contentFragment)
        mStack.add(messageListFragment)
        mStack.add(myFragment)
        vpMain.adapter = MainPagerAdapter(supportFragmentManager, mStack, titles)
        vpMain.currentItem = 0
        switchTab(0)
        vpMain.setScrollable(false)
        vpMain.offscreenPageLimit = 4
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
                if (UserManager.touristMode && position != 0 && position != 1) {
                    TouristDialog(this@MainActivity).show()
                } else {
                    if (position == 3) {
                        EventBus.getDefault().postSticky(UserCenterEvent(true))
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
                publishGuideIv.isVisible = false
                tabSquarePublish.isVisible = false
                tabSquare.isVisible = true
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
                if (!UserManager.isShowGuidePublish()) {
                    publishGuideIv.isVisible = true
                    val params = publishGuideIv.layoutParams as FrameLayout.LayoutParams
                    params.width = SizeUtils.dp2px(186F)
                    params.height = SizeUtils.dp2px(121F)
                    params.leftMargin =
                        ((1 / 4F + 1 / 8F) * ScreenUtils.getScreenWidth() - SizeUtils.dp2px(186 / 2F)).toInt()
                    publishGuideIv.layoutParams = params

                    val trans = ObjectAnimator.ofFloat(
                        publishGuideIv,
                        "translationY",
                        SizeUtils.dp2px(-5F).toFloat(),
                        SizeUtils.dp2px(0F).toFloat(),
                        SizeUtils.dp2px(-5F).toFloat()
                    )
                    trans.duration = 500
                    trans.repeatCount = 6
                    trans.interpolator = LinearInterpolator()
                    trans.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {

                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            publishGuideIv.isVisible = false
                            UserManager.saveShowGuidePublish(true)
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                        }

                        override fun onAnimationStart(animation: Animator?) {
                        }

                    })
                    trans.start()
                    publishGuideIv.onClick {
                        if (UserManager.touristMode)
                            TouristDialog(this).show()
                        else
                            startActivity<PublishActivity>()
                    }
                } else {
                    publishGuideIv.isVisible = false
                }
                tabMatchCount.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabMatchCount.setPadding(0)
                tabMatchCount.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_match),
                    null,
                    null
                )
                tabSquarePublish.isVisible = true
                tabSquare.visibility = View.INVISIBLE
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

                publishGuideIv.isVisible = false
                tabSquarePublish.isVisible = false
                tabSquare.isVisible = true
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
            3 -> {
                tabMatchCount.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabMatchCount.setPadding(0)
                tabMatchCount.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    resources.getDrawable(R.drawable.icon_tab_match),
                    null,
                    null
                )
                publishGuideIv.isVisible = false
                tabSquarePublish.isVisible = false
                tabSquare.isVisible = true
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
            R.id.labelAddBtn -> { //兴趣添加
                startActivity<MyLabelActivity>()
            }
            R.id.tabMatchCount -> {
                vpMain.currentItem = 0
            }
            R.id.tabSquare -> {
                vpMain.currentItem = 1
            }
            R.id.tabMessage -> {
                if (UserManager.touristMode) {
                    TouristDialog(this).show()
                } else
                    vpMain.currentItem = 2
            }
            R.id.tabMe -> {
                if (UserManager.touristMode) {
                    TouristDialog(this).show()
                } else
                    vpMain.currentItem = 3
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

        if (vpMain.currentItem == 1 && UserManager.isShowGuidePublish()) {
            publishGuideIv.isVisible = false
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
            CommonFunction.toast("再按一次退出程序")
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



    companion object {
        const val REQUEST_LABEL_CODE = 2000

        fun start(context: Context, intent: Intent) {
            context.startActivity(intent.setClass(context, MainActivity::class.java))
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)

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
                content = "您当前头像无法通过人脸对比\n请更换本人头像重新进行认证审核"
                title = "认证审核不通过"
            }
            GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS -> {//7头像违规
                content = "尊敬的用户，您上传的头像未使用真实照片或涉及违规，替换真实照片前您将持续对其他不可见"
                title = "请替换头像"
                confirmText = "修改头像"
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
}
