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
import com.google.gson.Gson
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
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
        if (!UserManager.isShowGuideVerify())
            userVerify.viewTreeObserver.addOnGlobalLayoutListener(this)



        multiStateView.retryBtn.onClick {
            multiStateView.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.myInfoCandy()
        }

        //我的访客封面
        val visitLayoutmanager = LinearLayoutManager(activity!!, RecyclerView.HORIZONTAL, false)
        visitLayoutmanager.stackFromEnd = true
        userVisitRv.layoutManager = visitLayoutmanager
        userVisitRv.adapter = visitsAdapter

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
                                userInfoBean?.mytags_list ?: mutableListOf()
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
                            userInfoBean?.mytags_list ?: mutableListOf()
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
        candyCount.text = "${userInfoBean?.userinfo?.my_candy_amount_str}"
        UserManager.saveUserVip(userInfoBean?.userinfo?.isvip ?: 0)
        UserManager.saveUserVerify(userInfoBean?.userinfo?.isfaced ?: 0)

        // userVisitCount.text = "今日总来访${userInfoBean.userinfo?.todayvisit}\t\t总来访${userInfoBean.userinfo?.allvisit}"
        checkVerify()
        checkVip()
        for (data in userInfoBean?.vip_descr ?: mutableListOf<VipDescr>())
            marqueeVipPower.addView(getMarqueeView(data))


    }


    //是否认证 0 未认证 1通过 2机审中 3人审中 4被拒（弹框）
    private fun checkVerify() {
        if (userInfoBean?.userinfo?.isfaced == 1) {//已认证
            userVerify.setImageResource(R.drawable.icon_verify_pass)
//            userVerifyTipBtn.text = "已认证"
//            userVerifyTipBtn.setTextColor(resources.getColor(R.color.colorWhite))
//            userVerifyTipBtn.isEnabled = false
//            userVerifyScore.isVisible = false
        } else if (userInfoBean?.userinfo?.isfaced == 2 || userInfoBean?.userinfo?.isfaced == 3) { //审核中
            userVerify.setImageResource(R.drawable.icon_verify_reject)
//            userVerifyTipBtn.text = "认证审核中"
//            userVerifyTipBtn.setTextColor(resources.getColor(R.color.colorGrayTextBF))
//            userVerifyTipBtn.isEnabled = false
//            userVerifyScore.isVisible = false
        } else {
            userVerify.setImageResource(R.drawable.icon_verify_reject)
//            userVerifyTipBtn.isVisible = true
//            userVerifyTipBtn.isEnabled = true
//            userVerifyTipBtn.setTextColor(resources.getColor(R.color.colorGrayTextBF))
//
//            if (userInfoBean?.userinfo?.isfaced == 4) {//审核不通过
//                userVerifyTipBtn.text = "重新认证"
//                userVerifyScore.isVisible = true
//            } else {//未认证
//                userVerifyTipBtn.text = "立即认证"
//                userVerifyScore.isVisible = true
//            }
        }
    }

    //是否认证
    private fun checkVip() {
        //是否会员
        if (userInfoBean?.userinfo?.isvip == 1) {
            userVip.visibility = View.VISIBLE
//            isVipCl.visibility = View.VISIBLE
//            isVipTimeout.text = "到期时间\t\t${userInfoBean?.userinfo?.vip_express ?: ""}"
//            notVipPowerLl.visibility = View.GONE
            isVipPowerBtn.isVisible = true
            isVipPowerBtn.text = "会员权益"
        } else {
            userVip.visibility = View.GONE
//            isVipCl.visibility = View.GONE
//            notVipPowerLl.visibility = View.VISIBLE
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

        multiStateView.postDelayed({
            guideVerifyWindow.dismiss()
            UserManager.saveShowGuideVerify(true)
            userVerify.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }, 3000L)
        if (userinfo != null) {
            userInfoBean = userinfo
            initData()
        }
    }


    override fun onCheckBlockResult(b: Boolean) {
        if (b) {
            if (UserManager.publishState == 0) {
                startActivity<PublishActivity>("from" to 2)
            } else
                EventBus.getDefault().post(
                    RePublishEvent(
                        true,
                        UserCenterFragment::class.java.simpleName
                    )
                )
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
                startActivity<MyLabelActivity>()
            }
            //我的足迹
            R.id.userFoot -> {
                startActivity<MyFootPrintActivity>()
            }
            //我的来访
            R.id.userVisit -> {
                startActivity<MyVisitActivity>(
                    "isVip" to (userInfoBean?.userinfo?.isvip == 1),
                    "today" to userInfoBean?.userinfo?.todayvisit,
                    "all" to userInfoBean?.userinfo?.allvisit,
                    "freeShow" to userInfoBean?.free_show
                )
            }
            //认证中心
            R.id.userVerify -> {
                startActivityForResult<IDVerifyActivity>(REQUEST_ID_VERIFY)
            }
            //我的糖果
            R.id.candyCl -> {
                startActivity<MyCandyActivity>()
            }
        }
    }


    //发布进度通知
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onProgressEvent(event: UploadEvent) {
        if (event.from == 2) {
//            multiStateView.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.myInfoCandy()
            EventBus.getDefault().post(RefreshEvent(true))
        }
    }


    //更新用户中心信息
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshEvent(event: UserCenterEvent) {
//        multiStateView.viewState = MultiStateView.VIEW_STATE_LOADING
        mPresenter.myInfoCandy()
    }


    /*-------------------------------------- 重新上传-----------------------------*/
    private var uploadCount = 0

    private fun retryPublish() {
        if (!mPresenter.checkNetWork()) {
            CommonFunction.toast("网络不可用,请检查网络设置")
            return
        }
        uploadCount = 0
        //发布消息的类型0,纯文本的 1，照片 2，视频 3，声音
        UserManager.publishState = 1
        when {
            UserManager.publishParams["type"] == 0 -> publish()
            UserManager.publishParams["type"] == 1 -> {
                UserManager.cancelUpload = false
                uploadPictures()
            }
            UserManager.publishParams["type"] == 2 -> {
                UserManager.cancelUpload = false
                //TODO上传视频
                val videoQnPath =
                    "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME).getString(
                        "accid"
                    )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                        16
                    )}"
                mPresenter.uploadFile(1, 1, UserManager.mediaBeans[0].url, videoQnPath, 2)
            }
            UserManager.publishParams["type"] == 3 -> {
                UserManager.cancelUpload = false
                //TODO上传音频
                val audioQnPath =
                    "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME).getString(
                        "accid"
                    )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                        16
                    )}"
                mPresenter.uploadFile(1, 1, UserManager.mediaBeans[0].url, audioQnPath, 3)
            }
        }
    }


    private fun uploadPictures() {
        //上传图片
        val imagePath =
            "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME).getString(
                "accid"
            )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                16
            )}"
        mPresenter.uploadFile(
            UserManager.mediaBeans.size,
            uploadCount + 1,
            UserManager.mediaBeans[uploadCount].url,
            imagePath,
            1
        )
    }

    private fun publish() {
        mPresenter.publishContent(
            UserManager.publishParams["type"] as Int,
            UserManager.publishParams,
            UserManager.keyList
        )
    }


    //发布消息的类型0,纯文本的 1，照片 2，视频 3，声音
    override fun onQnUploadResult(success: Boolean, type: Int, key: String?) {
        if (success) {
            when (type) {
                0 -> {
                    publish()
                }
                1 -> {
                    UserManager.mediaBeans[uploadCount].url = key ?: ""
                    UserManager.keyList.add(Gson().toJson(UserManager.mediaBeans[uploadCount]))
                    uploadCount++
                    if (uploadCount == UserManager.mediaBeans.size) {
                        publish()
                    } else {
                        uploadPictures()
                    }
                }
                2 -> {
                    UserManager.mediaBeans[uploadCount].url = key ?: ""
                    UserManager.keyList.add(Gson().toJson(UserManager.mediaBeans[0]))
                    publish()
                }
                3 -> {
                    UserManager.mediaBeans[uploadCount].url = key ?: ""
                    UserManager.keyList.add(Gson().toJson(UserManager.mediaBeans[0]))
                    publish()
                }
            }
        } else {
            onProgressEvent(UploadEvent(qnSuccess = false))
        }
    }

    override fun onSquareAnnounceResult(type: Int, success: Boolean, code: Int) {
        onAnnounceEvent(AnnounceEvent(success, code))
        EventBus.getDefault().postSticky(UploadEvent(1, 1, 1.0, from = 2))

    }


    fun onAnnounceEvent(event: AnnounceEvent) {
        if (event.serverSuccess) {
            UserManager.clearPublishParams()
            CommonFunction.toast("动态发布成功!")
        } else {
            UserManager.cancelUpload = true
            if (event.code == 402) { //内容违规重新去编辑
                UserManager.publishState = -1
                CommonFunction.toast("内容违规请重新编辑")
            } else { //发布失败重新发布
                UserManager.publishState = -2
                CommonFunction.toast("发布失败")
            }
        }
    }

    override fun onGlobalLayout() {
        val width = userVerify.left + userVerify.width
        if (width > 0) {
            guideVerifyWindow.showAtLocation(
                userVerify,
                Gravity.TOP and Gravity.LEFT,
                width - SizeUtils.dp2px(154F),
                SizeUtils.dp2px(-20F)
            )
        }
        Log.d("onGlobalLayout", "$width")

    }


}
