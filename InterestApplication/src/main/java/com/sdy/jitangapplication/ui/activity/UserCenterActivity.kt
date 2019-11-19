package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.gson.Gson
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.UserInfoBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.presenter.UserCenterPresenter
import com.sdy.jitangapplication.presenter.view.UserCenterView
import com.sdy.jitangapplication.ui.adapter.UserCenterCoverAdapter
import com.sdy.jitangapplication.ui.adapter.UserLabelAdapter
import com.sdy.jitangapplication.ui.adapter.VisitUserAvatorAdater
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_user_center.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.item_not_vip_viewpager.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

/**
 * 个人中心
 */
class UserCenterActivity : BaseMvpActivity<UserCenterPresenter>(), UserCenterView, View.OnClickListener {


    companion object {
        const val REQUEST_LABEL_CODE = 10
        const val REQUEST_INFO_SETTING = 11
        const val REQUEST_MY_SQUARE = 12
        const val REQUEST_ID_VERIFY = 13
        const val REQUEST_PUBLISH = 14
    }

    //用户标签adapter
    private val tagAdapter by lazy { UserLabelAdapter() }
    //动态封面适配器
    private val coverAdapter by lazy { UserCenterCoverAdapter() }
    //我的访客adapter
    private val visitsAdapter by lazy { VisitUserAvatorAdater() }


    private var userInfoBean: UserInfoBean? = null
    private lateinit var vipDialog: ChargeVipDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_center)
        EventBus.getDefault().register(this)
        initView()
        mPresenter.getMemberInfo(params)
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun initData() {
        //更新了信息之后更新本地缓存
        SPUtils.getInstance(Constants.SPNAME).put("avatar", userInfoBean!!.userinfo?.avatar)
        EventBus.getDefault().post(UpdateAvatorEvent(true))
        getTagData()
        coverAdapter.setNewData(userInfoBean?.squarelist?.list ?: mutableListOf())
        visitsAdapter.freeShow = userInfoBean?.free_show ?: false
        visitsAdapter.setNewData(userInfoBean?.visitlist ?: mutableListOf())
        GlideUtil.loadAvatorImg(this, userInfoBean?.userinfo?.avatar ?: "", userAvator)
        userName.text = userInfoBean?.userinfo?.nickname ?: ""
        userSquareCount.text = "我的动态 ${userInfoBean?.squarelist?.count}"
        UserManager.saveUserVip(userInfoBean?.userinfo?.isvip ?: 0)
        UserManager.saveUserVerify(userInfoBean?.userinfo?.isfaced ?: 0)
        userInfoSettingBtn.text = "完成度：${userInfoBean?.userinfo?.percent_complete}%"
        userVerifyScore.text = "+${userInfoBean?.userinfo?.identification}"

        // userVisitCount.text = "今日总来访${userInfoBean.userinfo?.todayvisit}\t\t总来访${userInfoBean.userinfo?.allvisit}"
        checkVerify()
        checkVip()
    }

    //从缓存中获取标签信息
    private fun getTagData() {
        tagAdapter.setNewData(UserManager.getSpLabels())
    }


    //是否认证 0 未认证 1通过 2机审中 3人审中 4被拒（弹框）
    private fun checkVerify() {
        if (userInfoBean?.userinfo?.isfaced == 1) {//已认证
            userVerify.setImageResource(R.drawable.icon_verify)
            userVerifyTipBtn.text = "已认证"
            userVerifyTipBtn.setTextColor(resources.getColor(R.color.colorOrange))
            userVerifyTipBtn.isEnabled = false
            userVerifyScore.isVisible = false
        } else if (userInfoBean?.userinfo?.isfaced == 2 || userInfoBean?.userinfo?.isfaced == 3) { //审核中
            userVerify.setImageResource(R.drawable.icon_verify_gray)
            userVerifyTipBtn.text = "认证审核中"
            userVerifyTipBtn.setTextColor(resources.getColor(R.color.colorGrayText))
            userVerifyTipBtn.isEnabled = false
            userVerifyScore.isVisible = false
        } else {
            userVerify.setImageResource(R.drawable.icon_verify_gray)
            userVerifyTipBtn.isVisible = true
            userVerifyTipBtn.isEnabled = true
            userVerifyTipBtn.setTextColor(resources.getColor(R.color.colorOrange))

            if (userInfoBean?.userinfo?.isfaced == 4) {//审核不通过
                userVerifyTipBtn.text = "重新认证"
                userVerifyScore.isVisible = true
            } else {//未认证
                userVerifyTipBtn.text = "立即认证"
                userVerifyScore.isVisible = true
            }
        }
    }

    //是否认证
    private fun checkVip() {
        //是否会员
        if (userInfoBean?.userinfo?.isvip == 1) {
            userVip.visibility = View.VISIBLE
            isVipCl.visibility = View.VISIBLE
            isVipTimeout.text = "到期时间\t\t${userInfoBean?.userinfo?.vip_express ?: ""}"
            notVipPowerLl.visibility = View.GONE
            isVipPowerBtn.isVisible = false
        } else {
            userVip.visibility = View.GONE
            isVipCl.visibility = View.GONE
            notVipPowerLl.visibility = View.VISIBLE
            isVipPowerBtn.isVisible = true
            isVipPowerBtn.text = "开通会员"
            initViewPager()
        }
    }

    private fun initViewPager() {
        /*生成indicator*/
        if (notVipPowerIndicator.childCount == 0)
            if ((userInfoBean?.vip_descr ?: mutableListOf<MatchBean>()).size > 1) {
                val size = (userInfoBean?.vip_descr ?: mutableListOf<MatchBean>()).size
                for (i in 0 until size) {
                    val indicator = RadioButton(this)
                    indicator.width = SizeUtils.dp2px(5F)
                    indicator.height = SizeUtils.dp2px(5F)
                    indicator.buttonDrawable = null
                    indicator.background = resources.getDrawable(R.drawable.selector_circle_indicator)

                    indicator.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    val layoutParams: LinearLayout.LayoutParams = indicator.layoutParams as LinearLayout.LayoutParams
                    layoutParams.setMargins(0, 0, SizeUtils.dp2px(6f), 0)
                    indicator.layoutParams = layoutParams
                    indicator.isEnabled = false
                    indicator.isChecked = i == 0
                    notVipPowerIndicator.addView(indicator)
                }
            }

        notVipPowerVp.adapter = object : PagerAdapter() {
            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view == `object`
            }

            override fun getCount(): Int {
                return (userInfoBean?.vip_descr ?: mutableListOf<MatchBean>()).size
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val view = layoutInflater.inflate(R.layout.item_not_vip_viewpager, null)
                view.vpTitle.text = userInfoBean?.vip_descr?.get(position)?.title ?: ""
                view.vpMsg.text = userInfoBean?.vip_descr?.get(position)?.rule ?: ""
                view.setOnClickListener {
                    vipDialog.show()
                }
                container.addView(view)
                return view
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

        }

        notVipPowerVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                for (child in 0 until notVipPowerIndicator.childCount)
                    (notVipPowerIndicator.getChildAt(child) as RadioButton).isChecked = position == child
            }

        })

    }


    private fun initView() {
        mPresenter = UserCenterPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        btnBack.setOnClickListener(this)
        settingBtn.setOnClickListener(this)
        userInfoSettingBtn.setOnClickListener(this)
        userAvator.setOnClickListener(this)
        isVipProblem.setOnClickListener(this)
        isVipPowerBtn.setOnClickListener(this)
        userTagsBtn.setOnClickListener(this)
        userSquareCount.setOnClickListener(this)
        userCollection.setOnClickListener(this)
        userQuestions.setOnClickListener(this)
        userRecommend.setOnClickListener(this)
        userVisit.setOnClickListener(this)
        userDianzan.setOnClickListener(this)
        userComment.setOnClickListener(this)
        userVerifyTipBtn.setOnClickListener(this)
        feedBack.setOnClickListener(this)


        multiStateView.retryBtn.onClick {
            multiStateView.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getMemberInfo(params)
        }

        //用户标签
        val manager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        userTagRv.layoutManager = manager
        userTagRv.adapter = tagAdapter

        //用户动态照片
        val squareManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        userSquaresRv.layoutManager = squareManager
        userSquaresRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL_LIST,
                SizeUtils.dp2px(10F),
                resources.getColor(R.color.colorWhite)
            )
        )
        userSquaresRv.adapter = coverAdapter
        val headView = LayoutInflater.from(this).inflate(R.layout.empty_cover_layout, userSquaresRv, false)
        coverAdapter.addHeaderView(headView, 0, LinearLayout.HORIZONTAL)
        coverAdapter.headerLayout.onClick {
            mPresenter.checkBlock(UserManager.getToken(), UserManager.getAccid())
            coverAdapter.headerLayout.isEnabled = false
        }

        //我的访客封面
        val visitLayoutmanager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        visitLayoutmanager.stackFromEnd = true
        userVisitRv.layoutManager = visitLayoutmanager
        userVisitRv.adapter = visitsAdapter


        //初始化vipDialog
        vipDialog = ChargeVipDialog(ChargeVipDialog.VIP_LOGO, this)
    }

    override fun onGetMyInfoResult(userinfo: UserInfoBean?) {
        multiStateView.viewState = MultiStateView.VIEW_STATE_CONTENT
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
                EventBus.getDefault().post(RePublishEvent(true, this))
//            if (UserManager.publishState == 1) {//正在发布中
//                CommonFunction.toast("还有动态正在发布哦~请稍候")
//                return
//            } else if (UserManager.publishState == -2) {//发布失败
//                CommonAlertDialog.Builder(this)
//                    .setTitle("发布提示")
//                    .setContent("您有一条内容未成功发布，是否重新发布？")
//                    .setConfirmText("重新上传")
//                    .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
//                        override fun onClick(dialog: Dialog) {
//                            dialog.cancel()
//                            retryPublish()
//                        }
//                    })
//                    .setCancelText("发布新内容")
//                    .setOnCancelListener(object : CommonAlertDialog.OnCancelListener {
//                        override fun onClick(dialog: Dialog) {
//                            dialog.cancel()
//                            UserManager.clearPublishParams()
//                            if (!ActivityUtils.isActivityExistsInStack(PublishActivity::class.java))
//                                startActivity<PublishActivity>("from" to 2)
//                        }
//                    })
//                    .create()
//                    .show()
//            } else if (UserManager.publishState == -1) { //400
//                SPUtils.getInstance(Constants.SPNAME).put("draft", UserManager.publishParams["descr"] as String)
//                UserManager.clearPublishParams()
//                if (!ActivityUtils.isActivityExistsInStack(PublishActivity::class.java))
//                    startActivity<PublishActivity>("from" to 2)
//
//            } else if (UserManager.publishState == 0) {
//                if (!ActivityUtils.isActivityExistsInStack(PublishActivity::class.java))
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionUtils.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                        startActivity<PublishActivity>("from" to 2)
//                    } else {
//                        startActivity<PublishActivity>("from" to 2)
//                    }
//            }

        }
        coverAdapter.headerLayout.isEnabled = true
    }

    override fun onError(text: String) {
        multiStateView.viewState = MultiStateView.VIEW_STATE_ERROR
        multiStateView.errorMsg.text = if (mPresenter.checkNetWork()) {
            getString(R.string.retry_load_error)
        } else {
            getString(R.string.retry_net_error)
        }
    }

    private val params by lazy {
        hashMapOf(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "_sign" to "",
            "_timestamp" to System.currentTimeMillis()
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_LABEL_CODE) {
                getTagData()
            } else if (requestCode == REQUEST_MY_SQUARE) {
                onRefreshEvent(UserCenterEvent(true))
            } else if (requestCode == REQUEST_ID_VERIFY) {
                userInfoBean?.userinfo?.isfaced = UserManager.isUserVerify()
                checkVerify()
            }
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnBack -> {
                onBackPressed()
            }
            //设置
            R.id.settingBtn -> {

                startActivity<SettingsActivity>()
            }
            //个人信息设置
            R.id.userAvator,
            R.id.userInfoSettingBtn -> {
                startActivityForResult<NewUserInfoSettingsActivity>(REQUEST_INFO_SETTING)

            }
            //会员问题
            R.id.isVipProblem -> {
                //todo 下标的popupwindow

            }
            //会员权益
            R.id.isVipPowerBtn -> {
                if (userInfoBean?.userinfo?.isvip != 1) {
                    vipDialog.show()
                }
            }
            //我的标签
            R.id.userTagsBtn -> {
                startActivityForResult<LabelsActivity>(REQUEST_LABEL_CODE, "from" to "usercenter")
            }
            //我的动态 1,我的所有动态 2我点过赞的 3 我收藏的
            R.id.userSquareCount -> {
                startActivityForResult<MyCollectionEtcActivity>(REQUEST_MY_SQUARE, "type" to 1)
            }
            //我的收藏
            R.id.userCollection -> {
                startActivity<MyCollectionEtcActivity>("type" to 3)
            }
            //我的点赞
            R.id.userDianzan -> {
                startActivity<MyCollectionEtcActivity>("type" to 2)
            }
            //我的评论
            R.id.userComment -> {
                startActivity<MyCommentActivity>()
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
            //我的问题
            R.id.userQuestions -> {
                startActivity<MyQuestionActivity>()
            }
            //推荐给朋友
            R.id.userRecommend -> {
            }
            //认证中心
            R.id.userVerifyTipBtn -> {
                startActivityForResult<IDVerifyActivity>(REQUEST_ID_VERIFY)
            }
            //意见反馈
            R.id.feedBack -> {
                ChatActivity.start(this, Constants.ASSISTANT_ACCID)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


    //发布成功后回来刷新界面数据
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onProgressEvent(event: UploadEvent) {
        if (event.from == 2) {
//            multiStateView.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getMemberInfo(params)
            EventBus.getDefault().post(RefreshEvent(true))
        }
    }


    //发布成功后回来刷新界面数据
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefreshEvent(event: UserCenterEvent) {
//        multiStateView.viewState = MultiStateView.VIEW_STATE_LOADING
        mPresenter.getMemberInfo(params)
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
            UserManager.checkIds,
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

}
