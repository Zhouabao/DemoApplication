package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.*
import com.kotlin.base.common.AppManager
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.NimIntent
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.AllMsgCount
import com.sdy.jitangapplication.model.InvestigateBean
import com.sdy.jitangapplication.presenter.MainPresenter
import com.sdy.jitangapplication.presenter.view.MainView
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.dialog.*
import com.sdy.jitangapplication.ui.fragment.IndexFragment
import com.sdy.jitangapplication.ui.fragment.MessageListFragment
import com.sdy.jitangapplication.ui.fragment.SquareFragment
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

//在支持路由的页面上添加注解（必选）
//这里的路径需要注意的是至少需要两级,/xx/xx
//路径标签个人建议写在一个类里面，方便统一管理和维护

class MainActivity : BaseMvpActivity<MainPresenter>(), MainView, View.OnClickListener {
    private val titles = arrayOf("首页", "发现", "消息", "我的")
    //fragment栈管理
    private val mStack = Stack<Fragment>()
    //匹配
//    private val matchFragment by lazy { MatchFragment1() }
    private val matchFragment by lazy { IndexFragment() }
    //广场
    private val squareFragment by lazy { SquareFragment() }
    //消息
    private val messageListFragment by lazy { MessageListFragment() }
    //我
    private val myFragment by lazy { UserCenterFragment() }


    private val guideDialog by lazy { GuideDialog(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        //启动时间统计
        mPresenter.startupRecord(
            UserManager.getToken(),
            UserManager.getAccid(),
            UserManager.getProvince(),
            UserManager.getCity()
        )

        //获取调查问卷数据
        if (UserManager.getCurrentSurveyVersion().isEmpty() || UserManager.getCurrentSurveyVersion() != AppUtils.getAppVersionName())
            mPresenter.getQuestion(UserManager.getToken(), UserManager.getAccid())

        //如果定位信息没有就重新定位
        AMapManager.initLocation(this)
        filterBtn.setOnClickListener(this)

//        IntentionMatchingDialog(this).show()

        if (!UserManager.isShowGuide()) {
            guideDialog.show()
        }

        if (!UserManager.getAlertProtocol())
            PrivacyDialog(this).show()

//        onAccountDangerEvent(AccountDangerEvent(AccountDangerDialog.VERIFY_NEED_AVATOR_INVALID))
//        UserManager.saveUserVerify(0)
        if (UserManager.getAccountDanger() || UserManager.getAccountDangerAvatorNotPass()) {
            //0未认证/认证不成功     1认证通过     2认证中
            if (UserManager.isUserVerify() == 0) {
                if (UserManager.getAccountDanger())
                    onAccountDangerEvent(AccountDangerEvent(AccountDangerDialog.VERIFY_NEED_ACCOUNT_DANGER))
                else
                    onAccountDangerEvent(AccountDangerEvent(AccountDangerDialog.VERIFY_NEED_AVATOR_INVALID))
            } else if (UserManager.isUserVerify() == 2) {
                onAccountDangerEvent(AccountDangerEvent(AccountDangerDialog.VERIFY_ING))
            }
        }

    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        parseIntents()
    }

    private fun parseIntents() {
        // 可以获取消息的发送者，跳转到指定的单聊、群聊界面。
        if (intent != null && intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
            val messages = intent.getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT) as ArrayList<IMMessage>?
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
        NIMClient.getService(MsgServiceObserve::class.java).observeReceiveMessage(incomingMessageObserver, true)

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
        tabMatch.setOnClickListener(this)
        tabMatchCount.setOnClickListener(this)
        tabSquare.setOnClickListener(this)
        tabMe.setOnClickListener(this)
        tabMessage.setOnClickListener(this)

        mStack.add(matchFragment)
        mStack.add(squareFragment)
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

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (position == 3) {
                    EventBus.getDefault().postSticky(UserCenterEvent(true))
                }
                switchTab(position)
            }
        })
    }


    /**
     * 切换tab
     */
    private fun switchTab(position: Int) {
        when (position) {
            0 -> {
                if (!UserManager.isUserVip()) {
                    tabMatch.isVisible = true
                    tabMatchCount.text = "${UserManager.getLeftSlideCount()}"
                    tabMatchCount.setTextColor(resources.getColor(R.color.colorWhite))
                    tabMatchCount.setPadding(0, 0, 0, SizeUtils.dp2px(3F))
                } else {
                    tabMatch.isVisible = false
                    tabMatchCount.text = "首页"
                    tabMatchCount.setTextColor(resources.getColor(R.color.colorOrange))
                    tabMatchCount.setPadding(0)
                }

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
                tabMatch.isVisible = false
                tabMatchCount.text = "首页"
                tabMatchCount.setTextColor(resources.getColor(R.color.colorGrayCCC))
                tabMatchCount.setPadding(0)
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
                tabMatch.isVisible = false
                tabMatchCount.text = "首页"
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
                tabMatch.isVisible = false
                tabMatchCount.text = "首页"
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateSlideCountEvent(event: UpdateSlideCountEvent) {
        if (vpMain.currentItem == 0) {
            if (!UserManager.isUserVip()) {
                tabMatch.isVisible = true
                tabMatchCount.text = "${UserManager.getLeftSlideCount()}"
                tabMatchCount.setTextColor(resources.getColor(R.color.colorWhite))
                tabMatchCount.setPadding(0, 0, 0, SizeUtils.dp2px(3F))
            } else {
                tabMatch.isVisible = false
                tabMatchCount.text = "首页"
                tabMatchCount.setTextColor(resources.getColor(R.color.colorOrange))
                tabMatchCount.setPadding(0)
            }
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.labelAddBtn -> { //标签添加
                startActivity<MyLabelActivity>()
            }
            R.id.tabMatchCount, R.id.tabMatch -> {
                vpMain.currentItem = 0
            }
            R.id.tabSquare -> {
                vpMain.currentItem = 1
            }
            R.id.tabMessage -> {
                vpMain.currentItem = 2
            }
            R.id.tabMe -> {
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
                    showGotoVerifyDialog(GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS, UserManager.getChangeAvator())
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
        EventBus.getDefault().unregister(this)
        NIMClient.getService(MsgServiceObserve::class.java).observeReceiveMessage(incomingMessageObserver, true)
    }


    /**
     * 双击退出APP
     */
    private var firstClickTime = 0L

    override fun onBackPressed() {
        if (guideDialog.isShowing) {
            return
        }

        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }

        val secondTime = System.currentTimeMillis()
        if (secondTime - firstClickTime > 2000) {
            CommonFunction.toast("再按一次退出程序")
            firstClickTime = secondTime
        } else {
            SPUtils.getInstance(Constants.SPNAME).remove("AlertChangeAvator")
            SPUtils.getInstance(Constants.SPNAME).remove("AlertChangeAlbum")
            AppManager.instance.finishAllActivity()
            System.exit(0)//正常退出
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
                        ",greetcount = ${allMsgCount.greetcount},square_count = ${allMsgCount.square_count}"
            )

            showMsgDot((allMsgCount.likecount > 0 || allMsgCount.greetcount > 0 || allMsgCount.square_count > 0 || msgCount > 0))
        }
    }


    /**
     * 显示未读消息红点
     */
    private fun showMsgDot(show: Boolean) {
        ivNewMsg.isVisible = show
    }


    /**
     * 获取调查问卷数据
     */
    private var investigateDialog: InvestigateDialog? = null

    override fun onInvestigateResult(investigateBean: InvestigateBean) {
        //保存滑动多少次引导弹窗
        UserManager.saveShowSurveyCount(investigateBean.showcard_cnt)
        if (investigateDialog == null) {
            investigateDialog = InvestigateDialog(this, investigateBean)
        }
    }

    /**
     * 展示调查问卷dialog
     */
    private fun showInvestigateDialog() {
        if (investigateDialog != null) {
            if (!investigateDialog!!.isShowing) {
                investigateDialog!!.show()
                //显示了调研结果，就保存当前版本号
                UserManager.saveCurrentSurveyVersion()
                //清除保存的展示次数
                SPUtils.getInstance(Constants.SPNAME).remove("showcard_cnt")
                //清除用于显示调研结果弹窗的滑动次数
                SPUtils.getInstance(Constants.SPNAME).remove("SlideSurveyCount")
            }
        }
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onShowSurveyDialogEvent(event: ShowSurveyDialogEvent) {
        if (event.slideCount == UserManager.getShowSurveyCount()) {
            showInvestigateDialog()
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
//    const val TYPE_CHANGE_AVATOR_PASS = 2//头像通过,但是不是真人
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
            GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS -> {//头像违规
                content = "尊敬的用户，您上传的头像未使用真实照片或涉及违规，替换真实照片前您将持续对其他不可见"
                title = "请替换头像"
                confirmText = "修改头像"
            }
            GotoVerifyDialog.TYPE_CHANGE_ABLUM -> {//完善相册
                content = "完善相册会使你的信息更多在匹配页展示\n现在就去完善你的相册吧！"
                title = "完善相册"
                confirmText = "完善相册"
            }
            GotoVerifyDialog.TYPE_CHANGE_AVATOR_PASS -> { //真实头像替换
                content = "当前的匹配率偏低，替换真实头像会使匹配率提高一倍，获得更多的用户好感。"
                title = "获得更多匹配"
                confirmText = "修改头像"
            }
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
        if (event.type == GotoVerifyDialog.TYPE_CHANGE_AVATOR_REAL_NOT_VALID) {
            UserManager.saveNeedChangeAvator(true)//需要换头像
            UserManager.saveForceChangeAvator(false)//是否强制替换过头像
            UserManager.saveChangeAvatorType(2)//真人不合规
            ChangeAvatarRealManDialog(
                ActivityUtils.getTopActivity(),
                ChangeAvatarRealManDialog.VERIFY_NEED_VALID_REAL_MAN,
                event.avator
            ).show()
        } else {
            when {
                event.type == GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS -> {
                    UserManager.saveNeedChangeAvator(true)//需要换头像
                    UserManager.saveForceChangeAvator(false)//是否强制替换过头像
                    UserManager.saveChangeAvatorType(1)//头像不合规
                }
                event.type == GotoVerifyDialog.TYPE_CHANGE_AVATOR_PASS -> UserManager.saveAlertChangeAvator(true)
                event.type == GotoVerifyDialog.TYPE_CHANGE_ABLUM -> UserManager.saveAlertChangeAlbum(true)
            }
            showGotoVerifyDialog(event.type, event.avator)
        }
    }


    private var accountDangerDialog: AccountDangerDialog? = null
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAccountDangerEvent(event: AccountDangerEvent) {
        if (accountDangerDialog != null) {
            accountDangerDialog!!.dismiss()
            accountDangerDialog = null
        }
        accountDangerDialog = AccountDangerDialog(ActivityUtils.getTopActivity())
        accountDangerDialog!!.show()
        accountDangerDialog!!.changeVerifyStatus(event.type)
    }


}
