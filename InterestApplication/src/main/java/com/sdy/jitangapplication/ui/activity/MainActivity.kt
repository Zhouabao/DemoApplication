package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.*
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
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
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.AllMsgCount
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
import com.sdy.jitangapplication.ui.fragment.MessageListFragment
import com.sdy.jitangapplication.ui.fragment.SquareFragment
import com.sdy.jitangapplication.utils.AMapManager
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.GotoVerifyDialog
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
    //消息
    private val messageListFragment by lazy { MessageListFragment() }

    private val titles = arrayOf("匹配", "发现", "消息")

    private val guideDialog by lazy { GuideDialog(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)
        NIMClient.getService(MsgServiceObserve::class.java).observeReceiveMessage(incomingMessageObserver, true)

        initView()
        //启动时间统计
        mPresenter.startupRecord(UserManager.getToken(), UserManager.getAccid())

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
        llMsgCount.setOnClickListener(this)
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
            msgLike.isVisible = UserManager.getLikeCount() > 0
            msgLike.text = UserManager.getLikeCount().toString()
            //打招呼个数
            msgHi.isVisible = UserManager.getHiCount() > 0
            msgHi.text = UserManager.getHiCount().toString()
            //广场消息个数
            msgSquare.isVisible = UserManager.getSquareCount() > 0
            msgSquare.text = UserManager.getSquareCount().toString()
            //未读消息个数
            val msgCount = NIMClient.getService(MsgService::class.java).totalUnreadCount
            msgChat.isVisible = msgCount > 0
            msgChat.text = "$msgCount"
            startMsgAnimation()
        }

    }


    /*
      初始化Fragment栈管理
   */
    private fun initFragment() {
        mStack.add(matchFragment)
        mStack.add(squareFragment)
        mStack.add(messageListFragment)
        vpMain.adapter = MainPagerAdapter(supportFragmentManager, mStack, titles)
        initIndicator()
        vpMain.currentItem = 0
        vpMain.setScrollable(true)
        vpMain.offscreenPageLimit = 2
        vpMain.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {


            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                val params = vpMain.layoutParams as FrameLayout.LayoutParams
                if (position == 2) {
                    filterBtn.setImageResource(R.drawable.icon_contact_book)
                    headRvLabels.isVisible = false
                    params.topMargin = SizeUtils.dp2px(0F)
                } else {
//                    HarassmentDialog(this@MainActivity,HarassmentDialog.CHATHI).show()
                    filterBtn.setImageResource(R.drawable.icon_filter)
                    headRvLabels.isVisible = true
                    params.topMargin = SizeUtils.dp2px(40F)

                }
                vpMain.layoutParams = params
            }

        })
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
            if (UserManager.isUserVerify() == 2 || UserManager.isUserVerify() == 3) {
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
            if (UserManager.isUserVerify() == 2 || UserManager.isUserVerify() == 3) {
                CommonFunction.toast("认证正在审核中，请耐心等待哦~")
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
                if (vpMain.currentItem != 2)
                    showFilterDialog()
                else
                    startActivity<ContactBookActivity>()
            }
            R.id.llMsgCount -> {
                vpMain.currentItem = 2
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
            //喜欢我的个数
            msgLike.isVisible = UserManager.getLikeCount() > 0
            msgLike.text = UserManager.getLikeCount().toString()
            //打招呼个数
            msgHi.isVisible = UserManager.getHiCount() > 0
            msgHi.text = UserManager.getHiCount().toString()
            //广场消息个数
            msgSquare.isVisible = UserManager.getSquareCount() > 0
            msgSquare.text = UserManager.getSquareCount().toString()

            //未读消息个数
            val msgCount = NIMClient.getService(MsgService::class.java).totalUnreadCount
            var totalMsgUnread = 0
            if (msgCount == 0)
                UserManager.saveHiCount(0)
            else if (msgCount > allMsgCount.greetcount)
                totalMsgUnread = msgCount - allMsgCount.greetcount
            msgChat.isVisible = totalMsgUnread > 0
            msgChat.text = "$totalMsgUnread"
            if ((allMsgCount.likecount > 0 || allMsgCount.greetcount > 0 || allMsgCount.square_count > 0 || totalMsgUnread > 0)) {
                startMsgAnimation()
            }


        }
    }

    private fun startMsgAnimation() {
        if (vpMain.currentItem == 2) {
            ivNewMsg.isVisible = true
            return
        }

        llMsgCount.clearAnimation()
        ivNewMsg.clearAnimation()

        //消息圆点向下动画
        val translateAnimationDown = TranslateAnimation(
            TranslateAnimation.RELATIVE_TO_SELF,
            0f,
            TranslateAnimation.RELATIVE_TO_SELF,
            0f,
            TranslateAnimation.RELATIVE_TO_SELF,
            0F,
            TranslateAnimation.ABSOLUTE,
            SizeUtils.dp2px(12F).toFloat()
        )
        translateAnimationDown.duration = 200
        translateAnimationDown.fillAfter = true
        translateAnimationDown.interpolator = DecelerateInterpolator()
        //消息展开的动画
        val scaleAnimationBig =
            ScaleAnimation(0f, 1f, 0f, 1f, ScaleAnimation.RELATIVE_TO_SELF, 0F, ScaleAnimation.RELATIVE_TO_SELF, 0.5F)
        scaleAnimationBig.duration = 200
        scaleAnimationBig.fillAfter = true
        scaleAnimationBig.interpolator = OvershootInterpolator()
        //消息收起来的动画
        val scaleAnimationSmall =
            ScaleAnimation(1f, 0f, 1f, 0f, ScaleAnimation.RELATIVE_TO_SELF, 0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5F)
        scaleAnimationSmall.duration = 200
        scaleAnimationSmall.fillAfter = true
        scaleAnimationSmall.startOffset = 3000
        scaleAnimationSmall.interpolator = DecelerateInterpolator()
        //消息圆点向上动画
        val translateAnimationTop = TranslateAnimation(
            TranslateAnimation.RELATIVE_TO_SELF,
            0f,
            TranslateAnimation.RELATIVE_TO_SELF,
            0f,
            TranslateAnimation.RELATIVE_TO_SELF,
            0F,
            TranslateAnimation.ABSOLUTE,
            -SizeUtils.dp2px(4F).toFloat()
        )
        translateAnimationTop.duration = 200
        translateAnimationTop.fillAfter = true
        translateAnimationTop.interpolator = DecelerateInterpolator()
        //开始动画
        ivNewMsg.isVisible = true
        ivNewMsg.startAnimation(translateAnimationDown)

        translateAnimationDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                ivNewMsg.clearAnimation()
                ivNewMsg.isVisible = false
                llMsgCount.isVisible = true
                llMsgCount.startAnimation(scaleAnimationBig)
            }

            override fun onAnimationStart(p0: Animation?) {
            }

        })

        scaleAnimationBig.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                llMsgCount.startAnimation(scaleAnimationSmall)

            }

            override fun onAnimationStart(p0: Animation?) {

            }

        })

        scaleAnimationSmall.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(p0: Animation?) {
                ivNewMsg.isVisible = true
                ivNewMsg.startAnimation(translateAnimationTop)
            }

            override fun onAnimationStart(p0: Animation?) {

            }

            override fun onAnimationRepeat(p0: Animation?) {

            }

        })

        translateAnimationTop.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                ivNewMsg.clearAnimation()
                llMsgCount.clearAnimation()
                llMsgCount.isVisible = false
            }

            override fun onAnimationStart(p0: Animation?) {
            }

        })

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
                    startActivityForResult<NewLabelsActivity1>(
                        REQUEST_LABEL_CODE,
                        "from" to "mainactivity"
                    )
                } else {
                    if (labelAdapter.enable) {
                        for (index in 0 until labelAdapter.dataList.size) {
                            labelAdapter.dataList[index].checked = index == position - 1
                        }
                        labelAdapter.notifyDataSetChanged()

                        if (labelAdapter.dataList[position - 1].id == UserManager.getGlobalLabelId()) {
                            return
                        } else {
                            SPUtils.getInstance(Constants.SPNAME)
                                .put("globalLabelId", labelAdapter.dataList[position - 1].id)
                            EventBus.getDefault().postSticky(UpdateLabelEvent(labelList[position - 1]))

                            enableHeadRv(false)
                        }
                    }
                }
            }

        })
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
        labelAdapter.setData(labelList)
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
                simplePagerTitleView.minScale = 0.9F
                simplePagerTitleView.textSize = 20f
                simplePagerTitleView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                simplePagerTitleView.normalColor = resources.getColor(R.color.colorGrayText)
                simplePagerTitleView.selectedColor = resources.getColor(R.color.colorBlackTitle)
                simplePagerTitleView.onClick {
                    vpMain.currentItem = index
                    if (index == 2) {
                        filterBtn.setImageResource(R.drawable.icon_contact_book)
                    } else {
                        filterBtn.setImageResource(R.drawable.icon_filter)
                    }
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
                indicator.endInterpolator = DecelerateInterpolator(1.0f)
                indicator.setColors(resources.getColor(R.color.colorOrange))
                return indicator
            }
        }
        tabMain.navigator = commonNavigator
        ViewPagerHelper.bind(tabMain, vpMain)
    }
//
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onUpdateAvatorEvent(event: UpdateAvatorEvent) {
//        if (event.update) {
//            GlideUtil.loadAvatorImg(this, SPUtils.getInstance(Constants.SPNAME).getString("avatar"), ivUserFace)
//        }
//    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewLabelEvent(event: UpdateAvatorEvent) {
        initData()
        GlideUtil.loadAvatorImg(this, UserManager.getAvator(), ivUserFace)
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
        Log.d(
            "Mainactivity",
            "getLikeCount = ${UserManager.getLikeCount()}, getHicount = ${UserManager.getHiCount()},  getSquareCount = ${UserManager.getSquareCount()},   unreadNum = ${unreadNum}"
        )
        ivNewMsg.isVisible =
            (UserManager.getLikeCount() > 0 || UserManager.getHiCount() > 0 || UserManager.getSquareCount() > 0 || unreadNum > 0)
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
        if (event.type == GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS)
            UserManager.saveNeedChangeAvator(true)
        showGotoVerifyDialog(event.type, event.avator)
    }


}
