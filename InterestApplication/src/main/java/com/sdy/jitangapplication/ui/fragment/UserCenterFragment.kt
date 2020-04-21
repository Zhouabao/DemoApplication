package com.sdy.jitangapplication.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.PopupWindow
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.google.android.material.appbar.AppBarLayout
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.RefreshMyCandyEvent
import com.sdy.jitangapplication.event.UpdateMyLabelEvent
import com.sdy.jitangapplication.event.UserCenterEvent
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.model.UserInfoBean
import com.sdy.jitangapplication.model.VipDescr
import com.sdy.jitangapplication.presenter.UserCenterPresenter
import com.sdy.jitangapplication.presenter.view.UserCenterView
import com.sdy.jitangapplication.ui.activity.*
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.adapter.VisitUserAvatorAdater
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.ScaleTransitionPagerTitleView
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_user_center.*
import kotlinx.android.synthetic.main.headerview_user_center_square.*
import kotlinx.android.synthetic.main.item_marquee_power.view.*
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
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.startActivityForResult
import java.util.*
import kotlin.math.abs

/**
 * 我的用户中心
 */
class UserCenterFragment : BaseMvpLazyLoadFragment<UserCenterPresenter>(), UserCenterView,
    View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {

    companion object {
        const val REQUEST_INFO_SETTING = 11
        const val REQUEST_MY_SQUARE = 12
        const val REQUEST_ID_VERIFY = 13
        const val REQUEST_PUBLISH = 14
        const val REQUEST_INTENTION = 15
    }

    //我的访客adapter
    private val visitsAdapter by lazy { VisitUserAvatorAdater() }

    private var userInfoBean: UserInfoBean? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        activity!!.mainRoot.setBackgroundResource(R.color.colorWhite)
        return inflater.inflate(R.layout.fragment_user_center, container, false)
    }


    override fun loadData() {
        EventBus.getDefault().register(this)
        initView()
//        mPresenter.getMemberInfo(params)
    }


    private var tabMySquareAndTagHeight = 0
    private fun initView() {
        mPresenter = UserCenterPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        settingBtn.setOnClickListener(this)
        userInfoSettingBtn.setOnClickListener(this)
        userAvator.setOnClickListener(this)
        isVipPowerBtn.setOnClickListener(this)
        candyCl.setOnClickListener(this)
        userFoot.setOnClickListener(this)
        userVisit.setOnClickListener(this)
        userVerify.setOnClickListener(this)
        candyCount.typeface = Typeface.createFromAsset(activity!!.assets, "DIN_Alternate_Bold.ttf")

        tabMySquareAndTag.viewTreeObserver.addOnGlobalLayoutListener {
            if (tabMySquareAndTagHeight == 0) {
                tabMySquareAndTagHeight = tabMySquareAndTag.top
            }
        }


        multiStateView.retryBtn.onClick {
            multiStateView.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.myInfoCandy()
        }

        //我的访客封面
        val visitLayoutmanager = LinearLayoutManager(activity!!, RecyclerView.HORIZONTAL, false)
        visitLayoutmanager.stackFromEnd = true
        userVisitRv.layoutManager = visitLayoutmanager
        userVisitRv.adapter = visitsAdapter


        userAppbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { p0, p1 ->
            Log.d("onGlobalLayout", "$tabMySquareAndTagHeight,${p1}")
            if (tabMySquareAndTagHeight > 0)
                userName1.isVisible = abs(p1) >= tabMySquareAndTagHeight
        })

        initFragment()
    }

    private val myTagFragment by lazy { MyTagFragment() }
    private val mySquareFragment by lazy { MySquareFragment() }

    private fun initFragment() {
        mStack.add(mySquareFragment)  //我的广场
        mStack.add(myTagFragment)   //我的兴趣
        vpMySquareAndTag.adapter =
            MainPagerAdapter(activity!!.supportFragmentManager, mStack, titles)
        vpMySquareAndTag.offscreenPageLimit = 2
        initIndicator()
        vpMySquareAndTag.currentItem = 0
    }


    //fragment栈管理
    private val mStack = Stack<Fragment>()
    private val titles = arrayOf("动态", "兴趣")

    private fun initIndicator() {
        tabMySquareAndTag.setBackgroundColor(Color.WHITE)
        val commonNavigator = CommonNavigator(activity!!)
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return mStack.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val simplePagerTitleView = ScaleTransitionPagerTitleView(context)
                simplePagerTitleView.text = titles[index]
                simplePagerTitleView.minScale = 0.88F
                simplePagerTitleView.textSize = 18F
                simplePagerTitleView.width =
                    (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(30F)) / 3
                simplePagerTitleView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                simplePagerTitleView.normalColor = Color.parseColor("#FF7E8183")
                simplePagerTitleView.selectedColor = resources.getColor(R.color.colorBlack)
                simplePagerTitleView.onClick {
                    vpMySquareAndTag.currentItem = index
                    if (index == 1)
                        EventBus.getDefault().post(
                            UpdateMyLabelEvent(
                                userInfoBean?.label_quality ?: mutableListOf()
                            )
                        )

                }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineHeight = UIUtil.dip2px(context, 4.0).toFloat()
                indicator.lineWidth = UIUtil.dip2px(context, 25.0).toFloat()
                indicator.roundRadius = UIUtil.dip2px(context, 2.0).toFloat()
                indicator.startInterpolator = AccelerateInterpolator()
                indicator.endInterpolator = DecelerateInterpolator(1.0f)
                indicator.setColors(resources.getColor(R.color.colorOrange))
                return indicator
            }
        }
        tabMySquareAndTag.navigator = commonNavigator
        ViewPagerHelper.bind(tabMySquareAndTag, vpMySquareAndTag)
        vpMySquareAndTag.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (position == 1) {
                    EventBus.getDefault().post(
                        UpdateMyLabelEvent(
                            userInfoBean?.label_quality ?: mutableListOf()
                        )
                    )
                }
            }


        })

    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun initData() {


        //更新了信息之后更新本地缓存
        SPUtils.getInstance(Constants.SPNAME).put("avatar", userInfoBean!!.userinfo?.avatar)

        visitsAdapter.freeShow = userInfoBean?.free_show ?: false
        visitsAdapter.setNewData(userInfoBean?.visitlist ?: mutableListOf())
        GlideUtil.loadAvatorImg(activity!!, userInfoBean?.userinfo?.avatar ?: "", userAvator)
        userName.text = userInfoBean?.userinfo?.nickname ?: ""
        userName1.text = userInfoBean?.userinfo?.nickname ?: ""
        candyCount.text = "${userInfoBean?.userinfo?.my_candy_amount}"
        UserManager.saveUserVip(userInfoBean?.userinfo?.isvip ?: 0)
        UserManager.saveUserVerify(userInfoBean?.userinfo?.isfaced ?: 0)

        checkVerify()
        checkVip()
        for (data in userInfoBean?.vip_descr ?: mutableListOf<VipDescr>())
            marqueeVipPower.addView(getMarqueeView(data))

        if (!UserManager.isShowGuideVerify() && UserManager.isUserVerify() != 1)
            userVerify.viewTreeObserver.addOnGlobalLayoutListener(this)
    }


    //是否认证 0 未认证 1通过 2机审中 3人审中 4被拒（弹框）
    private fun checkVerify() {
        if (userInfoBean?.userinfo?.isfaced == 1) {//已认证
            userVerify.setImageResource(R.drawable.icon_verify_pass)
        } else if (userInfoBean?.userinfo?.isfaced == 2 || userInfoBean?.userinfo?.isfaced == 3) { //审核中
            userVerify.setImageResource(R.drawable.icon_verify_reject)
        } else {
            userVerify.setImageResource(R.drawable.icon_verify_not)
        }
    }

    //是否认证
    private fun checkVip() {
        //是否会员
        if (userInfoBean?.userinfo?.isvip == 1) {
            userVip.visibility = View.VISIBLE
            isVipPowerBtn.isVisible = true
            isVipPowerBtn.text = "会员权益"
        } else {
            userVip.visibility = View.GONE
            isVipPowerBtn.isVisible = true
            isVipPowerBtn.text = "开通会员"

        }
    }


    private fun getMarqueeView(content: VipDescr): View {
        val view = layoutInflater.inflate(R.layout.item_marquee_power, null, false)
        view.powerContent.text = content.title
        return view
    }


    private val guideVerifyWindow by lazy {
        PopupWindow(activity!!).apply {
            contentView = LayoutInflater.from(activity!!)
                .inflate(R.layout.popupwindow_user_center_guide_verify, null, false)
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            setBackgroundDrawable(null)
            animationStyle = R.style.MyDialogLeftBottomAnimation
            isFocusable = true
            isOutsideTouchable = true
        }
    }

    override fun onGetMyInfoResult(userinfo: UserInfoBean?) {
        multiStateView.viewState = MultiStateView.VIEW_STATE_CONTENT
//        guideVerifyWindow.showAsDropDown(userVerify, 0, 0, Gravity.NO_GRAVITY)

        if (userinfo != null) {
            userInfoBean = userinfo
            initData()
        }
    }


    override fun onError(text: String) {
        multiStateView.viewState = MultiStateView.VIEW_STATE_ERROR
        multiStateView.errorMsg.text = if (mPresenter.checkNetWork()) {
            getString(R.string.retry_load_error)
        } else {
            getString(R.string.retry_net_error)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_MY_SQUARE) {
                onRefreshEvent(UserCenterEvent(true))
            } else if (requestCode == REQUEST_ID_VERIFY) {
                userInfoBean?.userinfo?.isfaced = UserManager.isUserVerify()
                checkVerify()
            } else if (requestCode == REQUEST_INTENTION) {
                if (data != null && data.getSerializableExtra("intention") != null)
                    userInfoBean?.userinfo?.intention =
                        data.getSerializableExtra("intention") as LabelQualityBean?
            }
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            //设置
            R.id.settingBtn -> {
                startActivity<SettingsActivity>()
            }
            //个人信息设置
            R.id.userAvator,
            R.id.userInfoSettingBtn -> {
                startActivityForResult<NewUserInfoSettingsActivity>(REQUEST_INFO_SETTING)

            }
            //会员权益
            R.id.isVipPowerBtn -> {
                if (userInfoBean?.userinfo?.isvip != 1) {
                    ChargeVipDialog(ChargeVipDialog.VIP_LOGO, activity!!).show()
                } else {
                    startActivity<VipPowerActivity>(
                        "nickname" to userInfoBean?.userinfo?.nickname,
                        "outtime" to userInfoBean?.userinfo?.vip_express
                    )
                }
            }
            //我的兴趣
            R.id.publishCl -> {
                publishCl.isEnabled = false
                startActivity<MyLabelActivity>()
                publishCl.isEnabled = true
            }
            //我的足迹
            R.id.userFoot -> {
                userFoot.isEnabled = false
                startActivity<MyFootPrintActivity>()
                userFoot.isEnabled = true
            }
            //我的来访
            R.id.userVisit -> {
                userVisit.isEnabled = false
                startActivity<MyVisitActivity>(
                    "isVip" to (userInfoBean?.userinfo?.isvip == 1),
                    "today" to userInfoBean?.userinfo?.todayvisit,
                    "all" to userInfoBean?.userinfo?.allvisit,
                    "freeShow" to userInfoBean?.free_show
                )
                userVisit.postDelayed({
                    userVisit.isEnabled = true
                }, 2000L)
            }
            //认证中心
            R.id.userVerify -> {
                when (userInfoBean?.userinfo?.isfaced) {
                    1 -> {
                        CommonFunction.toast("您已通过认证")
                    }
                    2, 3 -> {
                        CommonFunction.toast("认证审核中...")
                    }
                    else -> {
                        userVerify.isEnabled = false
                        startActivityForResult<IDVerifyActivity>(REQUEST_ID_VERIFY)
                        userVerify.postDelayed({
                            userVerify.isEnabled = true
                        }, 2000L)
                    }

                }

            }
            //我的糖果
            R.id.candyCl -> {
                candyCl.isEnabled = false
                startActivity<MyCandyActivity>()
                candyCl.postDelayed({
                    candyCl.isEnabled = true
                }, 2000L)
            }
        }
    }


    //更新用户中心信息
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshEvent(event: UserCenterEvent) {
//        multiStateView.viewState = MultiStateView.VIEW_STATE_LOADING
        mPresenter.myInfoCandy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshMyCandyEvent(event: RefreshMyCandyEvent) {
        if ((userInfoBean?.userinfo?.my_candy_amount
                ?: 0) >= event.candyCount && event.candyCount >= 0
        ) {
            candyCount.text = "${(userInfoBean?.userinfo?.my_candy_amount ?: 0) - event.candyCount}"
        }
    }


    override fun onGlobalLayout() {
//        if (!UserManager.isShowGuideVerify() && UserManager.isUserVerify() != 1) {
        val width = userVerify.left + userVerify.width
        if (width > 0) {
            if (userInfoBean?.userinfo?.isfaced != 1) {
                guideVerifyWindow.showAtLocation(
                    userVerify,
                    Gravity.TOP and Gravity.LEFT,
                    width - SizeUtils.dp2px(154F),
                    SizeUtils.dp2px(-20F)
                )
                multiStateView.postDelayed({
                    guideVerifyWindow.dismiss()
                    UserManager.saveShowGuideVerify(true)
                    userVerify.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }, 3000L)
            } else {
                userVerify.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        Log.d("onGlobalLayout", "width = $width")
//        }

    }


}
