package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.*
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.flyco.tablayout.utils.UnreadMsgUtils
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
import com.sdy.jitangapplication.model.LabelBean
import com.sdy.jitangapplication.model.TabEntity
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.presenter.MainPresenter
import com.sdy.jitangapplication.presenter.view.MainView
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.adapter.MatchLabelAdapter
import com.sdy.jitangapplication.ui.dialog.*
import com.sdy.jitangapplication.ui.fragment.MatchFragment1
import com.sdy.jitangapplication.ui.fragment.MessageListFragment
import com.sdy.jitangapplication.ui.fragment.SquareFragment
import com.sdy.jitangapplication.ui.fragment.UserCenterFragment
import com.sdy.jitangapplication.utils.AMapManager
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CenterLayoutManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivityForResult
import java.util.*

//在支持路由的页面上添加注解（必选）
//这里的路径需要注意的是至少需要两级,/xx/xx
//路径标签个人建议写在一个类里面，方便统一管理和维护

class MainActivity : BaseMvpActivity<MainPresenter>(), MainView, View.OnClickListener {
    private val iconUnselectIds = arrayListOf<Int>(
        R.drawable.icon_tab_match,
        R.drawable.icon_tab_square,
        R.drawable.icon_tab_message,
        R.drawable.icon_tab_me
    )
    private val iconSelectIds = arrayListOf<Int>(
        R.drawable.icon_tab_match_checked,
        R.drawable.icon_tab_square_checked,
        R.drawable.icon_tab_message_checked,
        R.drawable.icon_tab_me_checked
    )
    private val mTabEntitys = arrayListOf<CustomTabEntity>()
    private val titles = arrayOf("首页", "发现", "消息", "我的")
    //fragment栈管理
    private val mStack = Stack<Fragment>()
    //匹配
    private val matchFragment by lazy { MatchFragment1() }
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
//        if (UserManager.getlatitude().toDouble() == 0.0 || UserManager.getlongtitude().toDouble() == 0.0)
        AMapManager.initLocation(this)
        filterBtn.setOnClickListener(this)

        if (!UserManager.isShowGuide()) {
            guideDialog.show()
        }

        if (!UserManager.getAlertProtocol())
            PrivacyDialog(this).show()
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
                        ChatActivity.start(this, message.sessionId)
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
        filterBtn.setOnClickListener(this)
        labelAddBtn.setOnClickListener(this)
        mPresenter = MainPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        initHeadView()
        initFragment()
        //进入页面弹消息提醒
        if (UserManager.getSquareCount() > 0 || UserManager.getLikeCount() > 0 || UserManager.getHiCount() > 0
            || NIMClient.getService(MsgService::class.java).totalUnreadCount > 0
        ) {
            showMsgDot()
        } else {
            tabBottomMain.hideMsg(2)
        }

    }


    /*
      初始化Fragment栈管理
   */
    private fun initFragment() {
        for (title in titles.withIndex()) {
            mTabEntitys.add(TabEntity(title.value, iconSelectIds[title.index], iconUnselectIds[title.index]))
        }

        mStack.add(matchFragment)
        mStack.add(squareFragment)
        mStack.add(messageListFragment)
        mStack.add(myFragment)

        tabBottomMain.setTabData(mTabEntitys)
        tabBottomMain.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                vpMain.currentItem = position
            }

            override fun onTabReselect(position: Int) {

            }

        })
        vpMain.adapter = MainPagerAdapter(supportFragmentManager, mStack, titles)
        vpMain.currentItem = 0
        vpMain.setScrollable(false)
        vpMain.offscreenPageLimit = 4
        vpMain.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                tabBottomMain.currentTab = position
                tabHeaderCl.isVisible = (position == 0 || position == 1)
                if (position == 3) {
                    if (!UserManager.firstToMine) {
                        mainRoot.setBackgroundResource(R.drawable.gradient_orange)
                    } else
                        UserManager.firstToMine = false
                } else {
                    mainRoot.setBackgroundResource(R.color.colorWhite)
                }

            }

        })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.filterBtn -> {   //筛选对话框
                FilterUserDialog(this).show()
            }
            R.id.labelAddBtn -> { //标签添加
                startActivityForResult<LabelsActivity>(
                    REQUEST_LABEL_CODE,
                    "from" to "mainactivity"
                )
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
                showGotoVerifyDialog(GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS, UserManager.getChangeAvator())
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
            UserManager.saveHiCount(allMsgCount.greetcount)
            UserManager.saveLikeCount(allMsgCount.likecount)
            UserManager.saveSquareCount(allMsgCount.square_count)
            //未读消息个数
            val msgCount = NIMClient.getService(MsgService::class.java).totalUnreadCount
            var totalMsgUnread = 0
            if (msgCount == 0)
                UserManager.saveHiCount(0)
            else if (msgCount > allMsgCount.greetcount)
                totalMsgUnread = msgCount - allMsgCount.greetcount
            if ((allMsgCount.likecount > 0 || allMsgCount.greetcount > 0 || allMsgCount.square_count > 0 || totalMsgUnread > 0)) {
//                startMsgAnimation()
                showMsgDot()
            } else {
                tabBottomMain.hideMsg(2)

            }
        }
    }


    /**
     * 显示未读消息红点
     */
    private fun showMsgDot() {
        tabBottomMain.showDot(2)
        //设置未读消息红点
        val msgDot = tabBottomMain.getMsgView(2)
        if (msgDot != null) {
            UnreadMsgUtils.setSize(msgDot, SizeUtils.dp2px(8F))
            msgDot.backgroundColor = resources.getColor(R.color.colorOrange)
        }
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
        mPresenter.msgList(UserManager.getToken(), UserManager.getAccid())
    }

    /**
     * 好友列表和标签列表
     * 设置头部数据一直居于最顶端
     */
    //标签适配器
    private val labelAdapter: MatchLabelAdapter by lazy { MatchLabelAdapter(this) }
    //标签数据源
    var labelList: MutableList<LabelBean> = mutableListOf()

    companion object {
        const val REQUEST_LABEL_CODE = 2000

        fun start(context: Context, intent: Intent) {
            context.startActivity(intent.setClass(context, MainActivity::class.java))
        }

    }

    private val labelManager = CenterLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    private fun initHeadView() {
        headRvLabels.layoutManager = labelManager
        LinearSnapHelper().attachToRecyclerView(headRvLabels)
        headRvLabels.adapter = labelAdapter
        labelAdapter.setNewData(labelList)
        labelAdapter.setOnItemClickListener { _, view, position ->
            if (labelAdapter.enable) {
                for (index in 0 until labelAdapter.data.size) {
                    labelAdapter.data[index].checked = index == position
                    if (index == position)
                        labelManager.smoothScrollToPosition(headRvLabels, RecyclerView.State(), position)
                }
                labelAdapter.notifyDataSetChanged()
                if (labelAdapter.data[position].id == UserManager.getGlobalLabelId()) {
                    return@setOnItemClickListener
                } else {
                    SPUtils.getInstance(Constants.SPNAME).put("globalLabelId", labelAdapter.data[position].id)
                    EventBus.getDefault().postSticky(UpdateLabelEvent(labelList[position]))
                    enableHeadRv(false)
                }
            }
        }
        initData()
    }

    /**
     * 禁用或者启用标签点击选择
     */
    private fun enableHeadRv(enable: Boolean) {
        labelAdapter.enable = enable
//        labelAdapter.notifyDataSetChanged()
    }

    private fun initData() {
        labelList = UserManager.getSpLabels()
        if (labelList.size > 0) {
            if (SPUtils.getInstance(Constants.SPNAME).getInt("globalLabelId") != -1) {
                val id = SPUtils.getInstance(Constants.SPNAME).getInt("globalLabelId")
                for (label in labelList) {
                    if (label.id == id) {
                        label.checked = true
                        break
                    }
                }
            } else {
                labelList[0].checked = true
                SPUtils.getInstance(Constants.SPNAME).put("globalLabelId", labelList[0].id)
            }
        }
        labelAdapter.setNewData(labelList)
        for (label in labelList.withIndex()) {
            if (label.value.checked) {
                labelManager.smoothScrollToPosition(headRvLabels, RecyclerView.State(), label.index)
                break
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_LABEL_CODE) {
                var checked = false
                val list = UserManager.getSpLabels()
                for (i in 0 until labelList.size) {
                    for (j in 0 until list.size) {
                        if (labelList[i].id == list[j].id) {
                            list[j].checked = labelList[i].checked
                            if (list[j].checked) {
                                checked = true
                                if (UserManager.getGlobalLabelId() != list[j].id) {
                                    SPUtils.getInstance(Constants.SPNAME).put("globalLabelId", list[j].id)
                                    EventBus.getDefault().postSticky(UpdateLabelEvent(list[j]))
                                }
                            }
                        }
                    }
                }
                if (!checked) {
                    list[0].checked = true
                    SPUtils.getInstance(Constants.SPNAME).put("globalLabelId", list[0].id)
                    EventBus.getDefault().postSticky(UpdateLabelEvent(list[0]))
                }
                labelList = list
                labelAdapter.setNewData(labelList)
                for (label in labelList.withIndex()) {
                    if (label.value.checked) {
                        labelManager.smoothScrollToPosition(headRvLabels, RecyclerView.State(), label.index)
                        break
                    }
                }
            } else if (requestCode == SquarePlayDetailActivity.REQUEST_CODE) {
                EventBus.getDefault().post(NotifyEvent(data!!.getIntExtra("position", -1)))
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateAvatorEvent(event: UpdateAvatorEvent) {
        initData()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onShowSurveyDialogEvent(event: ShowSurveyDialogEvent) {
        if (event.slideCount == UserManager.getShowSurveyCount()) {
            showInvestigateDialog()
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onNewLabelEvent(event: EnableLabelEvent) {
        enableHeadRv(event.enable)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetMSGEvent(event: GetNewMsgEvent) {
        mPresenter.msgList(UserManager.getToken(), UserManager.getAccid())
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewMsgEvent(event: NewMsgEvent) {
        val unreadNum = NIMClient.getService(MsgService::class.java).totalUnreadCount
        if (unreadNum == 0) {
            UserManager.saveHiCount(0)
        }
        if (UserManager.getLikeCount() > 0 || UserManager.getHiCount() > 0 || UserManager.getSquareCount() > 0 || unreadNum > 0) {
            showMsgDot()
        }
    }


    /**
     * 重新认证事件总线
     */

//    const val TYPE_VERIFY = 4//认证失败去认证
//    const val TYPE_CHANGE_AVATOR_NOT_PASS = 5//头像违规替换
//    const val TYPE_CHANGE_AVATOR_PASS = 6//头像通过,但是不是真人
//    const val TYPE_CHANGE_ABLUM = 7//完善相册
    private var dialog: GotoVerifyDialog? = null

    private fun showGotoVerifyDialog(type: Int, avator: String = UserManager.getAvator()) {
        var content = ""
        var title = ""
        var confirmText = ""
        when (type) {
            GotoVerifyDialog.TYPE_VERIFY -> { //认证不通过
                content = "尊敬的用户，您当前头像无法通过人脸比对，请变更头像或进入人工审核流程。"
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
        if (dialog != null) {
            dialog!!.dismiss()
            dialog = null
        }

        dialog = GotoVerifyDialog.Builder(ActivityUtils.getTopActivity())
            .setTitle(title)
            .setContent(content)
            .setConfirmText(confirmText)
            .setIcon(avator)
            .setIconVisible(true)
            .setType(type)
            .setCancelIconIsVisibility(type != GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS)
            .setOnCancelable(type != GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS)
            .create()
        dialog?.show()
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onReVerifyEvent(event: ReVerifyEvent) {
        if (event.type == GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS) {
            UserManager.saveNeedChangeAvator(true)//需要换头像
            UserManager.saveForceChangeAvator(false)//是否强制替换过头像
        } else if (event.type == GotoVerifyDialog.TYPE_CHANGE_AVATOR_PASS)
            UserManager.saveAlertChangeAvator(true)
        else if (event.type == GotoVerifyDialog.TYPE_CHANGE_ABLUM)
            UserManager.saveAlertChangeAlbum(true)

        showGotoVerifyDialog(event.type, event.avator)
    }


}
