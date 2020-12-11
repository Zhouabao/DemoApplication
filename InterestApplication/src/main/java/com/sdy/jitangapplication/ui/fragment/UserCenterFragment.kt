package com.sdy.jitangapplication.ui.fragment

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
import com.google.android.material.appbar.AppBarLayout
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.UserInfoBean
import com.sdy.jitangapplication.presenter.UserCenterPresenter
import com.sdy.jitangapplication.presenter.view.UserCenterView
import com.sdy.jitangapplication.ui.activity.*
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.adapter.VisitUserAvatorAdater
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_user_center.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import java.util.*
import kotlin.math.abs

/**
 * 我的用户中心
 */
class UserCenterFragment : BaseMvpFragment<UserCenterPresenter>(), UserCenterView,
    OnLazyClickListener {

    companion object {
        const val REQUEST_INFO_SETTING = 11
        const val REQUEST_MY_SQUARE = 12
        const val REQUEST_ID_VERIFY = 13
        const val REQUEST_PUBLISH = 14
        const val REQUEST_INTENTION = 15

        const val POSITION_SQUARE = 0
        const val POSITION_DATING = 1
        const val POSITION_TAG = 2
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    fun loadData() {
        EventBus.getDefault().register(this)
        initView()
        if (!UserManager.touristMode)
            mPresenter.myInfoCandy()
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
        noticeSettingIv.setOnClickListener(this)
        femalePowerLl.setOnClickListener(this)
        shareRedBtn.setOnClickListener(this)
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
            userVerify.isVisible = true

        } else {
            femalePowerLl.isVisible = true
            userVerify.isVisible = false
        }

        initFragment()
    }

    private val myTagFragment by lazy { MyTagFragment() }
    private val myDatingFragment by lazy { MyDatingFragment() }
    private val mySquareFragment by lazy { MySquareFragment() }

    private fun initFragment() {
        mStack.add(mySquareFragment)  //我的广场
        mStack.add(myDatingFragment)  //我的约会
        mStack.add(myTagFragment)   //我的兴趣
        vpMySquareAndTag.adapter =
            MainPagerAdapter(childFragmentManager, mStack, titles)
        vpMySquareAndTag.offscreenPageLimit = 3
        tabMySquareAndTag.setViewPager(vpMySquareAndTag, titles)
        vpMySquareAndTag.currentItem = POSITION_SQUARE
    }


    //fragment栈管理
    private val mStack = Stack<Fragment>()
    private val titles by lazy { arrayOf(getString(R.string.tab_square), getString(R.string.tab_dating), getString(
        R.string.tab_label)) }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun initData() {
        if (UserManager.getGender() == 1) {
            femalePowerLl.isVisible = false
            malePowerLl.isVisible = true
        } else {
            femalePowerLl.isVisible = true
            malePowerLl.isVisible = false
        }

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
        if (!userInfoBean?.sign.isNullOrEmpty()) {
            userSign.isVisible = true
            userSign.text = userInfoBean?.sign ?: ""
        } else {
            userSign.visibility = View.INVISIBLE
        }
        candyCount.text = "${userInfoBean?.userinfo?.my_candy_amount}"
        UserManager.saveUserVip(userInfoBean?.userinfo?.isplatinum ?: false)
        UserManager.saveUserFoot(userInfoBean?.userinfo?.isvip ?: false)
        UserManager.saveUserVerify(userInfoBean?.userinfo?.isfaced ?: 0)

        checkVerify()
        checkVip()
        setUserPower()

        EventBus.getDefault()
            .post(UpdateMyLabelEvent(userInfoBean?.label_quality ?: mutableListOf()))
        EventBus.getDefault().post(userInfoBean?.userinfo?.isplatinum ?: false)

//        showWechatGuide()

    }

    private fun showWechatGuide() {
        if (!UserManager.isShowGuideWechat()) {
            noticeSettingIv.isVisible = true
            val trans = ObjectAnimator.ofFloat(
                noticeSettingIv,
                "translationY",
                SizeUtils.dp2px(-5F).toFloat(),
                SizeUtils.dp2px(0F).toFloat(),
                SizeUtils.dp2px(-5F).toFloat()
            )
            trans.duration = 500
            trans.repeatCount = 4
            trans.interpolator = LinearInterpolator()
            trans.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    noticeSettingIv.isVisible = false
                    UserManager.saveShowGuideWechat(true)
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }

            })
            trans.start()
        } else {
            noticeSettingIv.isVisible = false
        }
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
        contactWayMan.isVisible = UserManager.getGender() == 1
        if (userInfoBean?.userinfo?.contact_way == 0) {
            contactWayIv.setImageResource(R.drawable.icon_female_contact_no_small)
            contactWayMan.setImageResource(R.drawable.icon_female_contact_no_small)
        } else {
            contactWayMan.setImageResource(R.drawable.icon_female_contact_open_small)
            contactWayIv.setImageResource(R.drawable.icon_female_contact_open_small)
        }
    }


    //是否认证 0 未认证 1通过 2机审中 3人审中 4被拒（弹框）
    private fun checkVerify() {
        if (UserManager.getGender() == 2) {
            userVerify.isVisible = false
        } else {
            userVerify.isVisible = true
            if (userInfoBean?.userinfo?.isfaced == 1) {//已认证
                userVerify.playAnimation()
            } else if (userInfoBean?.userinfo?.isfaced == 2) { //审核中
                userVerify.progress = 0F
            } else {
                userVerify.progress = 0f
            }
        }

    }

    //是否认证
    private fun checkVip() {
        vipLevelSaveCount.text = "${userInfoBean?.platinum_vip_str}"
        if (userInfoBean?.userinfo?.isdirectvip == true) {
            isVipPowerBtn.setBackgroundResource(R.drawable.gradient_is_direct_vip_quanyi_bg)
            vipLevelSaveCount.setTextColor(Color.parseColor("#FF5E6473"))
            vipLevelLogo.setImageResource(R.drawable.icon_direct_vip_me)
            malePowerLl.setBackgroundResource(R.drawable.icon_direct_vip_bg)
        } else {
            isVipPowerBtn.setBackgroundResource(R.drawable.gradient_is_pt_vip_quanyi_bg)
            vipLevelSaveCount.setTextColor(Color.parseColor("#FF936F3F"))
            vipLevelLogo.setImageResource(R.drawable.icon_vip_me)
            malePowerLl.setBackgroundResource(R.drawable.icon_pt_vip_bg)
        }

    }


    private val guideContent by lazy { getString(R.string.verify_to_more_expode) }
    override fun onGetMyInfoResult(userinfo: UserInfoBean?) {

        if (userinfo != null) {
            multiStateView?.viewState = MultiStateView.VIEW_STATE_CONTENT
            userInfoBean = userinfo
            initData()
        } else {
            multiStateView?.viewState = MultiStateView.VIEW_STATE_ERROR
            multiStateView?.errorMsg?.text = if (mPresenter.checkNetWork()) {
                getString(R.string.retry_load_error)
            } else {
                getString(R.string.retry_net_error)
            }
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
                startActivity<NewUserInfoSettingsActivity>()

            }
            //会员权益
            R.id.isVipPowerBtn -> {
                //无门槛 非会员 开通会员(灰色)
                VipPowerActivity.start(activity!!, VipPowerActivity.SOURCE_SUPER_VIP_LOGO)
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
                    "isVip" to (userInfoBean?.userinfo?.isplatinum ?: false),
                    "today" to userInfoBean?.userinfo?.todayvisit,
                    "all" to userInfoBean?.userinfo?.allvisit,
                    "freeShow" to userInfoBean?.free_show,
                    "from" to MyVisitActivity.FROM_ME
                )
            }
            //认证中心
            ////0 未认证且无视频 1 已认证的 2 认证中 3 认证被拒绝 需要更换头像认证
            R.id.userVerify -> {
                when (userInfoBean?.userinfo?.isfaced) {
                    1 -> {
                        CommonFunction.toast(getString(R.string.verify_pass))
                    }
                    2, 3 -> {
                        CommonFunction.toast(getString(R.string.verify_checking))
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
            //微信推送设置提醒
            R.id.noticeSettingIv->{
                startActivity<NotificationActivity>()
            }

            //分享红包
            R.id.shareRedBtn -> {
                startActivity<InviteRewardsActivity>()
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
        }
    }


    //更新用户中心信息
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshEvent(event: UserCenterEvent) {
        showWechatGuide()

        if (!UserManager.touristMode)
            mPresenter.myInfoCandy()
    }


    //更新用户联系方式
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserCenterContactEvent(event: UserCenterContactEvent) {
        userInfoBean?.userinfo?.contact_way = event.contact_way
        contactWayMan.isVisible = UserManager.getGender() == 1
        if (userInfoBean?.userinfo?.contact_way == 0) {
            contactWayIv.setImageResource(R.drawable.icon_female_contact_no_small)
            contactWayMan.setImageResource(R.drawable.icon_female_contact_no_small)
        } else {
            contactWayMan.setImageResource(R.drawable.icon_female_contact_open_small)
            contactWayIv.setImageResource(R.drawable.icon_female_contact_open_small)
        }
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
    fun onFemaleVideoEvent(event: FemaleVideoEvent) {
        userInfoBean?.userinfo?.mv_faced = event.videoState
        if (userInfoBean?.userinfo?.mv_faced == 1) {
            videoIntroduceIv.setImageResource(R.drawable.icon_female_video_open_small)
        } else {
            videoIntroduceIv.setImageResource(R.drawable.icon_female_video_no_small)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFemaleVerifyEvent(event: FemaleVerifyEvent) {
        userInfoBean?.userinfo?.isfaced = event.verifyState
        if (userInfoBean?.userinfo?.isfaced == 1) {
            userVerifyIv.setImageResource(R.drawable.icon_female_verify_open_small)
        } else {
            userVerifyIv.setImageResource(R.drawable.icon_female_verify_no_small)
        }
    }


    override fun onPause() {
        super.onPause()
        Log.d("onpause", "onpause=====")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        noticeSettingIv.clearAnimation()

    }
}

