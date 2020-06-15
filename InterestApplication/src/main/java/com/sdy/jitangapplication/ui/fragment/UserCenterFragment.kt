package com.sdy.jitangapplication.ui.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.UserInfoBean
import com.sdy.jitangapplication.model.VipDescr
import com.sdy.jitangapplication.model.VipPowerBean
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
import org.jetbrains.anko.textColor
import java.util.*
import kotlin.math.abs

/**
 * 我的用户中心
 */
class UserCenterFragment : BaseMvpLazyLoadFragment<UserCenterPresenter>(), UserCenterView,
    OnLazyClickListener, ViewTreeObserver.OnGlobalLayoutListener {

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
        verifyState.setOnClickListener(this)
        femalePowerLl.setOnClickListener(this)
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

        if (UserManager.getGender() == 1) {
            femalePowerLl.isVisible = false
            userSign.visibility = View.VISIBLE

            userVerify.isVisible = true
            verifyState.isVisible = true

        } else {
            femalePowerLl.isVisible = true
            userSign.visibility = View.INVISIBLE

            userVerify.isVisible = false
            verifyState.isVisible = false
        }

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
        if ((userInfoBean?.userinfo?.nickname ?: "").length > 6) {
            userName.textSize = 16F
        } else {
            userName.textSize = 22F
        }
        userName1.text = userInfoBean?.userinfo?.nickname ?: ""
        userSign.text = userInfoBean?.sign ?: ""
        candyCount.text = "${userInfoBean?.userinfo?.my_candy_amount}"
        UserManager.saveUserVip(userInfoBean?.userinfo?.isvip ?: false)
        UserManager.saveUserVerify(userInfoBean?.userinfo?.isfaced ?: 0)

        checkVerify()
        checkVip()
        setUserPower()



        if (!UserManager.isShowGuideVerify() && UserManager.isUserVerify() != 1)
            userVerify.viewTreeObserver.addOnGlobalLayoutListener(this)

        EventBus.getDefault()
            .post(UpdateMyLabelEvent(userInfoBean?.label_quality ?: mutableListOf()))
        EventBus.getDefault().post(userInfoBean?.userinfo?.isplatinum ?: false)
    }


    /**
     * 设置女性用户的权益栏位
     */
    private fun setUserPower() {
        if (userInfoBean?.userinfo?.mv_faced == 1) {
            videoIntroduceIv.setImageResource(R.drawable.icon_female_video_open_small)
        } else {
            videoIntroduceIv.setImageResource(R.drawable.icon_female_video_no_small)
        }
        if (userInfoBean?.userinfo?.isfaced == 1) {
            userVerifyIv.setImageResource(R.drawable.icon_female_verify_open_small)
        } else {
            userVerifyIv.setImageResource(R.drawable.icon_female_verify_no_small)
        }
        if (userInfoBean?.userinfo?.contact_way == 0) {
            contactWayIv.setImageResource(R.drawable.icon_female_contact_no_small)
        } else {
            contactWayIv.setImageResource(R.drawable.icon_female_contact_open_small)
        }
    }


    //是否认证 0 未认证 1通过 2机审中 3人审中 4被拒（弹框）
    private fun checkVerify() {
        if (userInfoBean?.userinfo?.isfaced == 1) {//已认证
            userVerify.imageAssetsFolder = "images_verify"
            userVerify.setAnimation("data_verify.json")
            userVerify.playAnimation()
            verifyState.text = "已认证"
            verifyState.textColor = resources.getColor(R.color.colorWhite)
            verifyState.setBackgroundResource(R.drawable.gradient_blue_11dp)
        } else if (userInfoBean?.userinfo?.isfaced == 2 || userInfoBean?.userinfo?.isfaced == 3) { //审核中
            userVerify.setImageResource(R.drawable.icon_verify_not)
            verifyState.text = "审核中"
            verifyState.textColor = Color.parseColor("#FFC5C6C8")
            verifyState.setBackgroundResource(R.drawable.shape_rectangle_gray_divider_11dp)
        } else {
            userVerify.setImageResource(R.drawable.icon_verify_not)
            verifyState.text = "立即认证"
            verifyState.textColor = Color.parseColor("#FFC5C6C8")
            verifyState.setBackgroundResource(R.drawable.shape_rectangle_gray_divider_11dp)
        }

    }

    //是否认证
    private fun checkVip() {
        //有门槛 普通会员  升级钻石会员(灰色)
        //有门槛 钻石会员  会员权益(灰色)
        //无门槛 会员 会员权益(灰色)
        //无门槛 非会员 开通会员(灰色)
        //有门槛 非会员 开通会员(黄色)
        userVip.isVisible =
            (userInfoBean?.threshold_btn == true && userInfoBean?.userinfo?.isvip == true)
                    || (userInfoBean?.threshold_btn == false && userInfoBean?.userinfo?.isplatinum == true)

        if (userInfoBean?.threshold_btn == true) {
            if (userInfoBean?.userinfo?.isvip == false) {
                isVipPowerBtn.text = "开通会员"
            } else if (userInfoBean?.userinfo?.isvip == true && userInfoBean?.userinfo?.isplatinum == false) {
                EventBus.getDefault().post(UpdateSameCityVipEvent())
                isVipPowerBtn.text = "升级钻石会员"
                userVip.setImageResource(R.drawable.icon_vip)
            } else {
                EventBus.getDefault().post(UpdateSameCityVipEvent())
                isVipPowerBtn.text = "会员权益"
                userVip.setImageResource(R.drawable.icon_pt_vip)
            }
        } else {
            if (userInfoBean?.userinfo?.isplatinum == false) {
                isVipPowerBtn.text = "开通会员"
                userVip.setImageResource(R.drawable.icon_vip)
            } else {
                EventBus.getDefault().post(UpdateSameCityVipEvent())
                isVipPowerBtn.text = "会员权益"
                userVip.setImageResource(R.drawable.icon_pt_vip)

            }
        }

        if (userInfoBean?.userinfo?.isvip == false && userInfoBean?.threshold_btn == true) {
            vipLevelLogo.setImageResource(R.drawable.icon_vip_me)
            isVipPowerBtn.setBackgroundResource(R.drawable.gradient_is_vip_quanyi_bg)
            vipPowerLl.setBackgroundResource(R.drawable.shape_rectangle_light_orange_10dp)
            for (data in userInfoBean?.vip_descr ?: mutableListOf<VipDescr>())
                marqueeVipPower.addView(getMarqueeView(data, false))

        } else {
            for (data in userInfoBean?.platinum_vip_descr ?: mutableListOf<VipDescr>())
                marqueeVipPower.addView(
                    getMarqueeView(
                        data,
                        userInfoBean?.userinfo?.isplatinum ?: false
                    )
                )
            vipLevelLogo.setImageResource(R.drawable.icon_pt_vip_me)
            isVipPowerBtn.setBackgroundResource(R.drawable.gradient_is_pt_vip_quanyi_bg)
            vipPowerLl.setBackgroundResource(R.drawable.shape_rectangle_light_gray_10dp)
        }

    }


    private fun getMarqueeView(content: VipDescr, isPtVip: Boolean): View {
        val view = layoutInflater.inflate(R.layout.item_marquee_power, null, false)
        if (isPtVip) {
            view.powerContent.setTextColor(Color.parseColor("#FF5E6473"))
        } else {
            view.powerContent.setTextColor(Color.parseColor("#FF936F3F"))
        }
        view.powerContent.text = content.title
        return view
    }


    private val guideContent by lazy { "完成认证获取更多曝光" }
    override fun onGetMyInfoResult(userinfo: UserInfoBean?) {
        multiStateView.viewState = MultiStateView.VIEW_STATE_CONTENT

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
            }
        }
    }


    override fun onLazyClick(view: View) {
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
                //有门槛 普通会员  升级钻石会员(灰色)
                //有门槛 钻石会员  会员权益(灰色)
                //无门槛 会员 会员权益(灰色)
                //无门槛 非会员 开通会员(灰色)
                //有门槛 非会员 开通会员(黄色)
                if (userInfoBean?.threshold_btn == true && userInfoBean?.userinfo?.isvip == false) {
                    ChargeVipDialog(ChargeVipDialog.MORE_EXPODE, activity!!).show()
                } else {
                    startActivity<VipPowerActivity>(
                        "type" to if (userInfoBean?.threshold_btn == true && userInfoBean?.userinfo?.isvip == false) {
                            VipPowerBean.TYPE_NORMAL_VIP
                        } else {
                            VipPowerBean.TYPE_PT_VIP
                        }
                    )
                }
            }
            //我的兴趣
            R.id.publishCl -> {
                startActivity<MyLabelActivity>()
            }
            //我的足迹
            R.id.userFoot -> {
                startActivity<MyFootPrintActivity>()
            }
            //我的来访
            R.id.userVisit -> {
                startActivity<MyVisitActivity>(
                    "isVip" to (userInfoBean?.userinfo?.isvip ?: false),
                    "today" to userInfoBean?.userinfo?.todayvisit,
                    "all" to userInfoBean?.userinfo?.allvisit,
                    "freeShow" to userInfoBean?.free_show
                )
            }
            //认证中心
            ////0 未认证且无视频 1 已认证的 2 认证中 3 认证被拒绝 需要更换头像认证
            R.id.userVerify, R.id.verifyState -> {
                when (userInfoBean?.userinfo?.isfaced) {
                    1 -> {
                        CommonFunction.toast("您已通过认证")
                    }
                    2, 3 -> {
                        CommonFunction.toast("认证审核中...")
                    }
                    else -> {
                        CommonFunction.startToFace(
                            activity!!, IDVerifyActivity.TYPE_ACCOUNT_NORMAL,
                            REQUEST_ID_VERIFY
                        )
//                        IDVerifyActivity.startActivityForResult(
//                            activity!!,
//                            requestCode = REQUEST_ID_VERIFY
//                        )
                    }

                }

            }
            //我的糖果
            R.id.candyCl -> {
                startActivity<MyCandyActivity>()
            }
            //女性权益页面
            R.id.femalePowerLl -> {
                startActivity<FemalePowerActivity>(
                    "contact" to userInfoBean?.userinfo?.contact_way,
                    "verify" to userInfoBean?.userinfo?.isfaced,
                    "video" to userInfoBean?.userinfo?.mv_faced,
                    "url" to userInfoBean?.power_url
                )
            }
            //我的糖果
            R.id.guideVerifyWindow -> {
                guideVerifyWindow.isVisible = false
                UserManager.saveShowGuideVerify(true)
                userVerify.viewTreeObserver.removeOnGlobalLayoutListener(this@UserCenterFragment)
            }
        }
    }


    //更新用户中心信息
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshEvent(event: UserCenterEvent) {
//        multiStateView.viewState = MultiStateView.VIEW_STATE_LOADING
        mPresenter.myInfoCandy()
    }


    //更新用户联系方式
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserCenterContactEvent(event: UserCenterContactEvent) {
        userInfoBean?.userinfo?.contact_way = event.contact_way
        setUserPower()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshMyCandyEvent(event: RefreshMyCandyEvent) {
        if ((userInfoBean?.userinfo?.my_candy_amount
                ?: 0) >= event.candyCount && event.candyCount >= 0
        ) {
            candyCount.text = "${(userInfoBean?.userinfo?.my_candy_amount ?: 0) - event.candyCount}"
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSetMyCandyEvent(event: SetMyCandyEvent) {
        candyCount.text = "${event.candyCount}"
    }


    override fun onPause() {
        super.onPause()
        Log.d("onpause", "onpause=====")
    }

    private var show = false
    override fun onGlobalLayout() {
        val width = userVerify.left
        if (width > 0) {
            if (userInfoBean?.userinfo?.isfaced != 1 && !show) {
                (guideVerifyWindow.layoutParams as ConstraintLayout.LayoutParams).leftMargin =
                    width + SizeUtils.dp2px(14F) - SizeUtils.dp2px(162F / 12 * guideContent.length)
                guideVerifyWindow.isVisible = true
                showAnimation()
                show = true

            } else {
                userVerify.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
    }


    fun showAnimation() {
        val animatorSet = AnimatorSet()
        animatorSet.duration = 300L
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(guideVerifyWindow, "scaleX", 0.45f, 1F),
            ObjectAnimator.ofFloat(guideVerifyWindow, "scaleY", 0.45f, 1F),
            ObjectAnimator.ofFloat(guideVerifyWindow, "alpha", 0F, 1F)
        )
        animatorSet.start()

        val trans = ObjectAnimator.ofFloat(
            guideVerifyWindow,
            "translationY",
            SizeUtils.dp2px(-5F).toFloat(),
            SizeUtils.dp2px(0F).toFloat(),
            SizeUtils.dp2px(-5F).toFloat()
        )
        trans.duration = 750
        trans.repeatCount = 4
        trans.interpolator = LinearInterpolator()
        trans.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                if (guideVerifyWindow != null)
                    guideVerifyWindow.isVisible = false
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })

        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                trans.start()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        guideVerifyWindow.clearAnimation()
    }
}

