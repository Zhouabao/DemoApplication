package com.sdy.jitangapplication.ui.activity

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.gson.Gson
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.kotlin.base.common.AppManager
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.NimIntent
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomNotification
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.AllMsgCount
import com.sdy.jitangapplication.model.CustomerMsgBean
import com.sdy.jitangapplication.model.LabelBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.presenter.MainPresenter
import com.sdy.jitangapplication.presenter.view.MainView
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.adapter.MatchLabelAdapter
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.ui.dialog.FilterUserDialog
import com.sdy.jitangapplication.ui.dialog.GuideDialog
import com.sdy.jitangapplication.ui.fragment.MatchFragment1
import com.sdy.jitangapplication.ui.fragment.SquareFragment
import com.sdy.jitangapplication.utils.AMapManager
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.ScaleTransitionPagerTitleView
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_match_filter.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast
import java.util.*

//在支持路由的页面上添加注解（必选）
//这里的路径需要注意的是至少需要两级,/xx/xx
//路径标签个人建议写在一个类里面，方便统一管理和维护

class MainActivity : BaseMvpActivity<MainPresenter>(), MainView, View.OnClickListener {

    //fragment栈管理
    private val mStack = Stack<Fragment>()
    //匹配
    private val matchFragment by lazy { MatchFragment1() }
    //广场
    private val squareFragment by lazy { SquareFragment() }
    private val titles = arrayOf("匹配", "发现")

    private val guideDialog by lazy { GuideDialog(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)
        NIMClient.getService(MsgServiceObserve::class.java).observeCustomNotification(customNotificationObserver, true)
        NIMClient.getService(MsgServiceObserve::class.java).observeReceiveMessage(incomingMessageObserver, true)

        initView()
        //启动时间统计
        mPresenter.startupRecord(UserManager.getToken(),UserManager.getAccid())

        //如果定位信息没有就重新定位
        if (UserManager.getlatitude().toDouble() == 0.0 || UserManager.getlongtitude().toDouble() == 0.0)
            AMapManager.initLocation(this)
        initFragment()
        filterBtn.setOnClickListener(this)

        if (!UserManager.isShowGuide()) {
            guideDialog.show()
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
                        ChatActivity.start(this, message.sessionId)
                    }
                }
            }
        }
    }


    private fun initView() {
        //首页禁止滑动
        setSwipeBackEnable(false)

        filterBtn.setOnClickListener(this)
        notificationBtn.setOnClickListener(this)
        ivUserFace.setOnClickListener(this)
        mPresenter = MainPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        GlideUtil.loadAvatorImg(this, SPUtils.getInstance(Constants.SPNAME).getString("avatar"), ivUserFace)
        initHeadView()


        //进入页面弹消息提醒
        if (UserManager.getSquareCount() > 0 || UserManager.getLikeCount() > 0 || UserManager.getHiCount() > 0
            || NIMClient.getService(MsgService::class.java).totalUnreadCount > 0
        ) {
            //喜欢我的个数
            msgLike.text = UserManager.getLikeCount().toString()
            //打招呼个数
            msgHi.text = UserManager.getHiCount().toString()
            //广场消息个数
            msgSquare.text = UserManager.getSquareCount().toString()
            //未读消息个数
            val msgCount = NIMClient.getService(MsgService::class.java).totalUnreadCount
            msgChat.text = "$msgCount"
            ivNewMsg.isVisible = true
            llMsgCount.visibility = View.VISIBLE
            try {
                llMsgCount.visibility = View.VISIBLE
                Thread.sleep(3000L)
                llMsgCount.visibility = View.GONE
            } catch (e: Exception) {
            }
        }

    }


    /*
      初始化Fragment栈管理
   */
    private fun initFragment() {
        mStack.add(matchFragment)
        mStack.add(squareFragment)
        vpMain.adapter = MainPagerAdapter(supportFragmentManager, mStack, titles)
        initIndicator()
        vpMain.currentItem = 0
        vpMain.setScrollable(false)
    }

    //筛选对话框
    private val filterUserDialog: FilterUserDialog by lazy { FilterUserDialog(this) }


    /**
     * 展示筛选条件对话框
     * //最小年龄  limit_age_low
     * //最大年龄  limit_age_high
     * //标签id
     * //是否同城筛选 1否 2是 local_only
     * //选择了同城 传递城市id city_code
     * //是否筛选认证会员1不用 2需要筛选 audit_only
     * //1男 2女 3不限 gender
     * //toto  这里需要判断是否认证
     */
    private fun showFilterDialog() {
        val sp = SPUtils.getInstance(Constants.SPNAME)
        filterUserDialog.show()

        filterUserDialog.seekBarAge.setProgress(
            sp.getInt("limit_age_low", 18).toFloat(),
            sp.getInt("limit_age_high", 35).toFloat()
        )
        filterUserDialog.filterAge.text =
            "${filterUserDialog.seekBarAge.leftSeekBar.progress.toInt()}-${filterUserDialog.seekBarAge.rightSeekBar.progress.toInt()}岁"

        filterUserDialog.rbSexAll.check(
            when (sp.getInt("filter_gender", 3)) {
                1 -> R.id.switchSexMan
                2 -> R.id.switchSexWoman
                else -> R.id.switchSexAll
            }
        )

        if (UserManager.isUserVip()) {
            filterUserDialog.btnGoVip.visibility = View.GONE
            filterUserDialog.switchSameCity.visibility = View.VISIBLE
            filterUserDialog.switchSameCity.isChecked = sp.getInt("local_only", 1) == 2
        } else {
            filterUserDialog.btnGoVip.visibility = View.VISIBLE
            filterUserDialog.switchSameCity.visibility = View.GONE
        }

        if (UserManager.isUserVerify() == 1) {
            filterUserDialog.btnVerify.visibility = View.GONE
            filterUserDialog.switchShowVerify.visibility = View.VISIBLE
            filterUserDialog.switchShowVerify.isChecked = sp.getInt("audit_only", 1) == 2

        } else {
            if (UserManager.isUserVerify() == 2) {
                filterUserDialog.btnVerify.text = "认证中"
            } else {
                filterUserDialog.btnVerify.text = "未认证"
            }
            filterUserDialog.btnVerify.visibility = View.VISIBLE
            filterUserDialog.switchShowVerify.visibility = View.GONE
        }

        filterUserDialog.btnGoVip.onClick {
            ChargeVipDialog(this).show()
        }
        filterUserDialog.btnVerify.onClick {
            if (UserManager.isUserVerify() == 2) {
                toast("认证正在审核中，请耐心等待哦~")
            } else {
                startActivity<IDVerifyActivity>()
            }
        }
        filterUserDialog.seekBarAge.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }

            override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
                filterUserDialog.filterAge.text = "${leftValue.toInt()}-${rightValue.toInt()}岁"
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }

        })

        filterUserDialog.btnCompleteFilter.onClick {
            sp.put("limit_age_high", filterUserDialog.seekBarAge.rightSeekBar.progress.toInt())
            sp.put("limit_age_low", filterUserDialog.seekBarAge.leftSeekBar.progress.toInt())
            when (filterUserDialog.rbSexAll.checkedRadioButtonId) {
                R.id.switchSexMan -> {
                    sp.put("filter_gender", 1)
                }
                R.id.switchSexWoman -> {
                    sp.put("filter_gender", 2)
                }
                R.id.switchSexAll -> {
                    sp.put("filter_gender", 3)
                }
            }
            if (filterUserDialog.switchSameCity.isChecked) {
                sp.put("local_only", 2)
                sp.put("city_code", UserManager.getCityCode())
            } else {
                sp.put("local_only", 1)
            }
            if (filterUserDialog.switchShowVerify.isChecked) {
                sp.put("audit_only", 2)
            } else {
                sp.put("audit_only", 1)
            }

            EventBus.getDefault().post(RefreshEvent(true))
            filterUserDialog.dismiss()
        }


    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.filterBtn -> {
                showFilterDialog()
            }
            R.id.llMsgCount, R.id.notificationBtn -> {//点击通知，进入消息列表
                startActivity<MessageListActivity>()
            }
            R.id.ivUserFace -> {//点击头像，进入个人中心
                startActivity<UserCenterActivity>()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (KeyboardUtils.isSoftInputVisible(this))
            KeyboardUtils.hideSoftInput(this)
        parseIntents()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        NIMClient.getService(MsgServiceObserve::class.java).observeCustomNotification(customNotificationObserver, false)
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
            ToastUtils.showShort("再按一次退出程序")
            firstClickTime = secondTime
        } else {
            AppManager.instance.finishAllActivity()
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
            //喜欢我的个数
            msgLike.text = allMsgCount.likecount.toString()
            //打招呼个数
            msgHi.text = allMsgCount.greetcount.toString()
            //广场消息个数
            msgSquare.text = allMsgCount.square_count.toString()
            //未读消息个数
            val msgCount = NIMClient.getService(MsgService::class.java).totalUnreadCount
            var totalMsgUnread = 0
            if (msgCount == 0)
                UserManager.saveHiCount(0)
            else if (msgCount > allMsgCount.greetcount)
                totalMsgUnread = msgCount - allMsgCount.greetcount

            msgChat.text = "$totalMsgUnread"
            ivNewMsg.isVisible =
                (allMsgCount.likecount > 0 || allMsgCount.greetcount > 0 || allMsgCount.square_count > 0 || totalMsgUnread > 0)
            llMsgCount.visibility = View.VISIBLE
            YoYo.with(Techniques.Bounce)
                .duration(3000)
                .withListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationStart(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        llMsgCount.visibility = View.GONE
                    }

                })
                .playOn(llMsgCount)
        }
    }

    /**
     * 系统通知监听
     */
    private var customNotificationObserver: Observer<CustomNotification> =
        Observer { customNotification ->
            if (customNotification.content != null) {
                val customerMsgBean =
                    Gson().fromJson<CustomerMsgBean>(customNotification.content, CustomerMsgBean::class.java)
                when (customerMsgBean.type) {
                    1 -> {//系统通知新的消息数量
                        mPresenter.msgList(UserManager.getToken(), UserManager.getAccid())
                        EventBus.getDefault().post(UpdateHiEvent())
                    }
                    2 -> {//对方删除自己,本地删除会话列表
                        NIMClient.getService(MsgService::class.java)
                            .deleteRecentContact2(customerMsgBean.accid ?: "", SessionTypeEnum.P2P)
                    }
                    3 -> { //新的招呼刷新界面
                        EventBus.getDefault().post(UpdateHiEvent())
                    }

                }
                Log.d("OkHttp", "${customerMsgBean.type}=================${customerMsgBean.accid}=================")
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

    private fun initHeadView() {
        val labelManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        headRvLabels.layoutManager = labelManager
        headRvLabels.adapter = labelAdapter
        labelAdapter.dataList = labelList
        labelAdapter.setOnItemClickListener(object : MatchLabelAdapter.OnItemClickListener {
            override fun onItemClick(item: View, position: Int) {
                if (position == 0) {
                    startActivityForResult<LabelsActivity>(
                        REQUEST_LABEL_CODE,
                        "from" to "mainactivity"
                    )
                } else {
                    for (index in 0 until labelAdapter.dataList.size) {
                        labelAdapter.dataList[index].checked = index == position - 1
                    }
                    labelAdapter.notifyDataSetChanged()

                    if (labelAdapter.dataList[position - 1].id == UserManager.getGlobalLabelId()) {
                        return
                    } else {
                        SPUtils.getInstance(Constants.SPNAME)
                            .put("globalLabelId", labelAdapter.dataList[position - 1].id)
                        EventBus.getDefault().post(UpdateLabelEvent(labelList[position - 1]))
                    }
                }
            }

        })
        initData()
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
                labelList[1].checked = true
                SPUtils.getInstance(Constants.SPNAME).put("globalLabelId", labelList[1].id)
            }
        }
        labelAdapter.setData(labelList)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_LABEL_CODE) {
                val list = UserManager.getSpLabels()
                for (i in 0 until labelList.size) {
                    for (j in 0 until list.size) {
                        if (labelList[i].id == list[j].id) {
                            list[j].checked = labelList[i].checked
                        }
                    }
                }
                labelList = list
                labelAdapter.setData(labelList)
            } else if (requestCode == SquarePlayDetailActivity.REQUEST_CODE) {
                EventBus.getDefault().post(NotifyEvent(data!!.getIntExtra("position", -1)))
            }
        }
    }


    private fun initIndicator() {
        tabMain.setBackgroundColor(Color.WHITE)
        val commonNavigator = CommonNavigator(this)
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return mStack.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val simplePagerTitleView = ScaleTransitionPagerTitleView(context)
                simplePagerTitleView.text = titles[index]
                simplePagerTitleView.textSize = 20f
                simplePagerTitleView.normalColor = resources.getColor(R.color.colorGrayText)
                simplePagerTitleView.selectedColor = resources.getColor(R.color.colorBlackTitle)
                simplePagerTitleView.onClick {
                    vpMain.currentItem = index
                }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineHeight = UIUtil.dip2px(context, 4.0).toFloat()
                indicator.lineWidth = UIUtil.dip2px(context, 35.0).toFloat()
                indicator.roundRadius = UIUtil.dip2px(context, 2.0).toFloat()
                indicator.startInterpolator = AccelerateInterpolator()
                indicator.endInterpolator = DecelerateInterpolator(2.0f)
                indicator.setColors(resources.getColor(R.color.colorOrange))
                return indicator
            }
        }
        tabMain.navigator = commonNavigator
        ViewPagerHelper.bind(tabMain, vpMain)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateAvatorEvent(event: UpdateAvatorEvent) {
        if (event.update) {
            GlideUtil.loadAvatorImg(this, SPUtils.getInstance(Constants.SPNAME).getString("avatar"), ivUserFace)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewLabelEvent(event: UpdateAvatorEvent) {
        initData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewMsgEvent(event: NewMsgEvent) {
        val unreadNum = NIMClient.getService(MsgService::class.java).totalUnreadCount
        if (unreadNum == 0) {
            UserManager.saveHiCount(0)
        }
        ivNewMsg.isVisible =
            (UserManager.getLikeCount() > 0 || UserManager.getHiCount() > 0 || UserManager.getSquareCount() > 0 || unreadNum > 0)
    }

}
