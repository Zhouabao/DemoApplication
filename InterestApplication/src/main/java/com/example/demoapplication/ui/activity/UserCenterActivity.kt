package com.example.demoapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.event.RefreshEvent
import com.example.demoapplication.event.UpdateAvatorEvent
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.model.UserInfoBean
import com.example.demoapplication.presenter.UserCenterPresenter
import com.example.demoapplication.presenter.view.UserCenterView
import com.example.demoapplication.ui.adapter.UserCenterCoverAdapter
import com.example.demoapplication.ui.adapter.UserLabelAdapter
import com.example.demoapplication.ui.adapter.VisitUserAvatorAdater
import com.example.demoapplication.ui.dialog.ChargeVipDialog
import com.example.demoapplication.utils.UserManager
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_user_center.*
import kotlinx.android.synthetic.main.dialog_charge_vip.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.item_not_vip_viewpager.view.*
import org.greenrobot.eventbus.EventBus
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
    }

    //用户标签adapter
    private val tagAdapter by lazy { UserLabelAdapter() }
    //动态封面适配器
    private val coverAdapter by lazy { UserCenterCoverAdapter() }
    //我的访客adapter
    private val visitsAdapter by lazy { VisitUserAvatorAdater() }


    private lateinit var userInfoBean: UserInfoBean
    private lateinit var vipDialog: ChargeVipDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_center)
        initView()
        mPresenter.getMemberInfo(
            hashMapOf(
                "accid" to UserManager.getAccid(),
                "token" to UserManager.getToken(),
                "_sign" to "",
                "_timestamp" to System.currentTimeMillis()
            )
        )
    }

    override fun onResume() {
        super.onResume()

    }

    private fun initData() {
        //更新了信息之后更新本地缓存
        SPUtils.getInstance(Constants.SPNAME).put("avatar", userInfoBean.userinfo?.avatar)
        EventBus.getDefault().post(UpdateAvatorEvent(true))
        getTagData()
        coverAdapter.setNewData(userInfoBean.squarelist?.list ?: mutableListOf())
        visitsAdapter.setNewData(userInfoBean.visitlist ?: mutableListOf())
        GlideUtil.loadAvatorImg(this, userInfoBean.userinfo?.avatar ?: "", userAvator)
        userName.text = userInfoBean.userinfo?.nickname ?: ""
        userSquareCount.text = "我的动态 ${userInfoBean.squarelist?.count}"
        UserManager.saveUserVip(userInfoBean.userinfo?.isvip ?: 0)
        UserManager.saveUserVerify(userInfoBean.userinfo?.isfaced ?: 0)

        // userVisitCount.text = "今日总来访${userInfoBean.userinfo?.todayvisit}\t\t总来访${userInfoBean.userinfo?.allvisit}"
        checkVerify()
        checkVip()
    }

    //从缓存中获取标签信息
    private fun getTagData() {
        tagAdapter.setNewData(UserManager.getSpLabels())
    }


    //是否认证 1认证 2未认证
    private fun checkVerify() {
        if (userInfoBean.userinfo?.isfaced == 1) {
            userVerify.setImageResource(R.drawable.icon_verify)
            userVerifyTip.visibility = View.GONE
            userVerifyBtn.text = "已认证"
            userVerifyBtn.isEnabled = false
        } else if (userInfoBean.userinfo?.isfaced == 2) {
            userVerify.setImageResource(R.drawable.icon_verify_gray)
            userVerifyTip.visibility = View.VISIBLE
            userVerifyBtn.text = "认证中"
            userVerifyBtn.isEnabled = false
        } else {
            userVerify.setImageResource(R.drawable.icon_verify_gray)
            userVerifyTip.visibility = View.VISIBLE
            userVerifyBtn.text = "立即认证"
            userVerifyBtn.isEnabled = true
        }
    }

    //是否认证
    private fun checkVip() {
        //是否会员
        if (userInfoBean.userinfo?.isvip == 1) {
            userVip.visibility = View.VISIBLE
            isVipCl.visibility = View.VISIBLE
            isVipTimeout.text = "到期时间\t\t${userInfoBean.userinfo?.vip_express ?: ""}"
            notVipPowerLl.visibility = View.GONE
        } else {
            userVip.visibility = View.GONE
            isVipCl.visibility = View.GONE
            notVipPowerLl.visibility = View.VISIBLE
            initViewPager()
        }
    }

    private fun initViewPager() {
        /*生成indicator*/
        if ((userInfoBean.vip_descr ?: mutableListOf<MatchBean>()).size > 1) {
            val size = (userInfoBean.vip_descr ?: mutableListOf<MatchBean>()).size
            for (i in 0 until size) {
                val indicator = RadioButton(this)
                indicator.width = SizeUtils.dp2px(5F)
                indicator.height = SizeUtils.dp2px(5F)
                indicator.buttonDrawable = null
                indicator.background = resources.getDrawable(R.drawable.selector_circle_indicator)

                indicator.layoutParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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
                return (userInfoBean.vip_descr ?: mutableListOf<MatchBean>()).size
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val view = layoutInflater.inflate(R.layout.item_not_vip_viewpager, null)
                view.vpTitle.text = userInfoBean.vip_descr?.get(position)?.title ?: ""
                view.vpMsg.text = userInfoBean.vip_descr?.get(position)?.rule ?: ""
                view.setOnClickListener {
                    vipDialog.show()
                    if (vipDialog.bannerVip.childCount == 0) {
                        vipDialog.position = position
                    } else {
                        vipDialog.setCurrent(position)
                    }
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
        userVerifyBtn.setOnClickListener(this)

        //用户标签
        val manager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        userTagRv.layoutManager = manager
        userTagRv.adapter = tagAdapter

        //用户动态照片
        val squareManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        userSquaresRv.layoutManager = squareManager
        userSquaresRv.adapter = coverAdapter
        val headView = LayoutInflater.from(this).inflate(R.layout.empty_cover_layout, userSquaresRv, false)
        coverAdapter.addHeaderView(headView, 0, LinearLayout.HORIZONTAL)
        coverAdapter.headerLayout.onClick {
            startActivity<PublishActivity>()
        }

        //我的访客封面
        val visitLayoutmanager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        visitLayoutmanager.stackFromEnd = true
        userVisitRv.layoutManager = visitLayoutmanager
        userVisitRv.adapter = visitsAdapter


        //初始化vipDialog
        vipDialog = ChargeVipDialog(this)
    }

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
            if (requestCode == REQUEST_LABEL_CODE) {
                getTagData()
            } else if (requestCode == REQUEST_INFO_SETTING || requestCode == REQUEST_MY_SQUARE) {
                multiStateView.viewState = MultiStateView.VIEW_STATE_LOADING
                mPresenter.getMemberInfo(
                    hashMapOf(
                        "accid" to UserManager.getAccid(),
                        "token" to UserManager.getToken(),
                        "_sign" to "",
                        "_timestamp" to System.currentTimeMillis()
                    )
                )
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
            R.id.userInfoSettingBtn -> {
                startActivityForResult<UserInfoSettingsActivity>(REQUEST_INFO_SETTING)

            }
            //会员问题
            R.id.isVipProblem -> {
                //todo 下标的popupwindow

            }
            //会员权益
            R.id.isVipPowerBtn -> {
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
                    "isVip" to (userInfoBean.userinfo?.isvip == 1),
                    "today" to userInfoBean.userinfo?.todayvisit,
                    "all" to userInfoBean.userinfo?.allvisit
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
            R.id.userVerifyBtn -> {
                startActivity<IDVerifyActivity>()
            }
        }
    }

    override fun onBackPressed() {
        EventBus.getDefault().post(RefreshEvent(true))
        super.onBackPressed()
    }

}
