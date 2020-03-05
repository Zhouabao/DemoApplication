package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.marginTop
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.google.android.material.appbar.AppBarLayout
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.friend.FriendService
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.DetailUserInfoBean
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.presenter.MatchDetailPresenter
import com.sdy.jitangapplication.presenter.view.MatchDetailView
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.adapter.MatchDetailInfoAdapter
import com.sdy.jitangapplication.ui.adapter.MatchImgsPagerAdapter
import com.sdy.jitangapplication.ui.adapter.MyLabelAdapter
import com.sdy.jitangapplication.ui.dialog.MoreActionDialog
import com.sdy.jitangapplication.ui.fragment.BlockSquareFragment
import com.sdy.jitangapplication.ui.fragment.ListSquareFragment
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_match_detail.*
import kotlinx.android.synthetic.main.dialog_more_action.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import java.util.*

/**
 * 匹配详情页
 */
class MatchDetailActivity : BaseMvpActivity<MatchDetailPresenter>(), MatchDetailView, View.OnClickListener {

    private val targetAccid by lazy { intent.getStringExtra("target_accid") }
    private var matchBean: MatchBean? = null

    var photos: MutableList<String> = mutableListOf()
    private val photosAdapter by lazy { MatchImgsPagerAdapter(this, photos) }


    private val params by lazy {
        hashMapOf(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "target_accid" to targetAccid,
            "_timestamp" to System.currentTimeMillis()
        )
    }
    private val targetUserPramas by lazy {
        hashMapOf(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "target_accid" to targetAccid,
            "_timestamp" to System.currentTimeMillis()
        )
    }

    companion object {
        @JvmStatic
        fun start(context: Context, fromAccount: String, parPos: Int = -1, childPos: Int = -1) {
            context.startActivity<MatchDetailActivity>(
                "target_accid" to fromAccount,
                "parPos" to parPos,
                "childPos" to childPos
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_detail)

        initView()

        mPresenter.getUserDetailInfo(params)
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    private val userTagAdapter by lazy { MyLabelAdapter(false) }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = MatchDetailPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        //设置图片的宽度占满屏幕，宽高比9:16
        val layoutParams = clPhotos.layoutParams
        layoutParams.width = ScreenUtils.getScreenWidth()
//        layoutParams.height = (16 / 9.0F * layoutParams.width).toInt()
        layoutParams.height = ScreenUtils.getScreenHeight() - SizeUtils.dp2px(197F)
        clPhotos.layoutParams = layoutParams

        //设置个人信息距离顶部的距离
//        val paramsClUserInfo = clUserInfo.layoutParams as LinearLayout.LayoutParams
        val paramsClUserInfo = clUserInfo.layoutParams as FrameLayout.LayoutParams
        paramsClUserInfo.topMargin = ScreenUtils.getScreenHeight() - SizeUtils.dp2px(197F) - SizeUtils.dp2px(26F)
        paramsClUserInfo.height = LinearLayout.LayoutParams.WRAP_CONTENT
        clUserInfo.layoutParams = paramsClUserInfo

        vpUserDetail.setScrollable(false)
        moreBtn.setOnClickListener(this)
        moreBtn1.setOnClickListener(this)
        detailUserChatLl.setOnClickListener(this)
        cancelBlack.setOnClickListener(this)
        backBtn.setOnClickListener(this)
        backBtn1.setOnClickListener(this)
        btnBack2.setOnClickListener(this)
        //用户的广场预览界面
        detailThumbRv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        detailThumbRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL_LIST,
                SizeUtils.dp2px(3F),
                resources.getColor(R.color.colorTransparent)
            )
        )
        //用户详细信息列表
        detailUserInformationRv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)


        detailRvTag.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        detailRvTag.adapter = userTagAdapter


        userAppbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { p0, verticalOffset ->
            if (Math.abs(verticalOffset) >= clUserInfo.marginTop) {
                if (ScreenUtils.isFullScreen(this))
                    ScreenUtils.setNonFullScreen(this)
            } else {
                if (!ScreenUtils.isFullScreen(this))
                    ScreenUtils.setFullScreen(this)
            }
            //            detailActionbar.isVisible = Math.abs(verticalOffset) >= (userAppbar.totalScrollRange - SizeUtils.dp2px(60F))
            detailActionbar.isVisible = Math.abs(verticalOffset) >= clUserInfo.marginTop
        })

        //重试
        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getUserDetailInfo(params)
            EventBus.getDefault().post(UpdateSquareEvent())

        }

        detailSquareSwitchRg.setOnCheckedChangeListener { radioGroup, checkedId ->
            if (checkedId == R.id.rbList) {
                vpUserDetail.currentItem = 0
            } else if (checkedId == R.id.rbBlock) {
                vpUserDetail.currentItem = 1
            }
        }
        detailSquareSwitchRg.check(R.id.rbBlock)
    }


    /**
     * 切换tab
     */

    //fragment栈管理
    private val mStack = Stack<Fragment>()

    /*
      初始化Fragment栈管理
   */
    private fun initFragment() {
        mStack.add(ListSquareFragment(targetAccid))
        mStack.add(BlockSquareFragment(targetAccid))
//        mStack.add(MatchDetailLabelFragment(targetAccid))
        vpUserDetail.offscreenPageLimit = 3
        vpUserDetail.adapter = MainPagerAdapter(supportFragmentManager, mStack)
        vpUserDetail.currentItem = 1
    }


    /**
     * 初始化个人信息数据
     */
    private val detailUserInformationAdapter by lazy { MatchDetailInfoAdapter() }

    private fun initUserInfomationData() {
        if (matchBean!!.base_info.height != 0)
            detailUserInformationAdapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_height,
                    "身高",
                    "${matchBean!!.base_info.height}"
                )
            )
        if (!matchBean!!.base_info.emotion.isNullOrEmpty())
            detailUserInformationAdapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_emotion,
                    "感情状态",
                    "${matchBean!!.base_info.emotion}"
                )
            )
        if (!matchBean!!.base_info.hometown.isNullOrEmpty())
            detailUserInformationAdapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_hometown,
                    "家乡",
                    "${matchBean!!.base_info.hometown}"
                )
            )
        if (!matchBean!!.base_info.present_address.isNullOrEmpty())
            detailUserInformationAdapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_living,
                    "现居地",
                    "${matchBean!!.base_info.present_address}"
                )
            )
        if (!matchBean!!.base_info.personal_job.isNullOrEmpty())
            detailUserInformationAdapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_job,
                    "职业",
                    "${matchBean!!.base_info.personal_job}"
                )
            )
        if (!matchBean!!.base_info.making_friends.isNullOrEmpty())
            detailUserInformationAdapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_ami,
                    "交友目的",
                    "${matchBean!!.base_info.making_friends}"
                )
            )
        if (!matchBean!!.base_info.personal_school.isNullOrEmpty())
            detailUserInformationAdapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_school,
                    "学校",
                    "${matchBean!!.base_info.personal_school}"
                )
            )
        if (!matchBean!!.base_info.personal_drink.isNullOrEmpty())
            detailUserInformationAdapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_drink,
                    "喝酒",
                    "${matchBean!!.base_info.personal_drink}"
                )
            )
        if (!matchBean!!.base_info.personal_smoke.isNullOrEmpty())
            detailUserInformationAdapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_smoke,
                    "抽烟",
                    "${matchBean!!.base_info.personal_smoke}"
                )
            )
        if (!matchBean!!.base_info.personal_schedule.isNullOrEmpty())
            detailUserInformationAdapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_schedule,
                    "作息时间",
                    "${matchBean!!.base_info.personal_schedule}"
                )
            )
    }

    private fun initData() {
        userTagAdapter.setNewData(matchBean!!.other_tags)

        initFragment()//初始化vp
        initUserInfomationData()//初始化个人信息数据
        detailUserInformationRv.adapter = detailUserInformationAdapter
//        EventBus.getDefault().post(UpdateSquareEvent())
        squareCount.text = "动态 ${matchBean!!.square_cnt ?: 0}"
        detailUserName.text = matchBean!!.nickname
        titleUsername.text = matchBean!!.nickname
        val left = resources.getDrawable(
            if (matchBean!!.gender == 1) {
                R.drawable.icon_gender_man_gray_userdetail
            } else {
                R.drawable.icon_gender_woman_gray_userdetail
            }
        )
        detailUserInfoAge.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null)
        detailUserInfoAge.text = "${matchBean!!.age}"
        detailUserConstellation.text = "${matchBean!!.constellation}"
        detailUserDistance.text = "${matchBean!!.distance}"
        detailUserSign.apply {
            setContent("${matchBean!!.sign}")
            isVisible = !(matchBean!!.sign.isNullOrBlank())
        }

        detailUserVip.visibility = if (matchBean!!.isvip == 1) {
            View.VISIBLE
        } else {
            View.GONE
        }
        detailUserVerify.isVisible = matchBean!!.isfaced == 1

        //更新打招呼次数和状态
        updateLightCount()

//        用户动态封面图片（暂不展示）
        detailThumbRv.visibility = View.GONE
//        if (matchBean!!.square == null || matchBean!!.square!!.size == 0) {
//            detailThumbRv.visibility = View.GONE
//        } else {
//            detailThumbRv.adapter = thumbAdapter
//            thumbAdapter.setNewData(matchBean!!.square ?: mutableListOf())
//        }

        //用户照片
        detailPhotosVp.adapter = photosAdapter
        detailPhotosVp.setScrollable(true)

        if (matchBean!!.photos == null || matchBean!!.photos!!.isEmpty())
            photos.add(matchBean!!.avatar ?: "")
        photos.addAll(matchBean!!.photos ?: mutableListOf())
        photosAdapter.notifyDataSetChanged()
        setViewpagerAndIndicator()


    }

    /**
     * 设置竖直滑动的vp2以及其滑动的indicator
     */
    private fun setViewpagerAndIndicator() {
        detailPhotosVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                for (i in 0 until detailPhotosIndicator.size) {
                    (detailPhotosIndicator[i] as RadioButton).isChecked = i == position
                }
            }
        })

        if (photos.size > 1) {
            for (i in 0 until photos.size) {
//                val width = ((ScreenUtils.getScreenWidth()
//                        - SizeUtils.dp2px(15F) * 2
//                        - (SizeUtils.dp2px(6F) * (photos.size - 1))) * 1F / photos.size).toInt()
                val width = SizeUtils.dp2px(6F)
                val height = SizeUtils.dp2px(6F)

                val indicator = RadioButton(this)
                indicator.buttonDrawable = null
                indicator.background = resources.getDrawable(R.drawable.selector_round_indicator)

                indicator.layoutParams = LinearLayout.LayoutParams(width, height)
                val layoutParams: LinearLayout.LayoutParams = indicator.layoutParams as LinearLayout.LayoutParams
                layoutParams.setMargins(
                    if (i == 0) {
                        SizeUtils.dp2px(15F)
                    } else {
                        0
                    }, 0, if (i == photos.size - 1) {
                        SizeUtils.dp2px(15F)
                    } else {
                        SizeUtils.dp2px(6f)
                    }, 0
                )
                indicator.layoutParams = layoutParams
                indicator.isEnabled = false
                indicator.isChecked = i == 0
                detailPhotosIndicator.addView(indicator)
            }
        }

        //上一张
        btnLast.onClick {
            if (detailPhotosVp.currentItem > 0) {
                val index = detailPhotosVp.currentItem
                detailPhotosVp.setCurrentItem(index - 1, true)
            }
        }

        //下一张
        btnNext.onClick {
            if (detailPhotosVp.currentItem < photos.size - 1) {
                val index = detailPhotosVp.currentItem
                detailPhotosVp.setCurrentItem(index + 1, true)
            }
        }


    }


    /**
     * 获取用户详情结果
     */
    //1 互相没有拉黑  2 我拉黑了他  3  ta拉黑了我   4 互相拉黑
    override fun onGetMatchDetailResult(success: Boolean, matchUserDetailBean: MatchBean?) {
        if (success) {
            stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
            matchBean = matchUserDetailBean
            updateBlockStatus()
            initData()
        } else {
            stateview.viewState = MultiStateView.VIEW_STATE_ERROR
            stateview.errorMsg.text = CommonFunction.getErrorMsg(this)
        }
    }


    /**
     * 更新拉黑状态
     */
    //1 互相没有拉黑  2 我拉黑了他  3  ta拉黑了我   4 互相拉黑
    private fun updateBlockStatus() {
        when (matchBean!!.isblock) {
            1 -> {
                userContent.isVisible = true
                llBlackContent.isVisible = false
                ScreenUtils.setFullScreen(this)

            }
            2 -> {
                llBlackContent.isVisible = true
                userContent.isVisible = false
                cancelBlack.isVisible = true
                blackContent.text = "拉黑状态下双方主页互相不可见\n取消拉黑可恢复好友权益"
                ScreenUtils.setNonFullScreen(this)

            }
            3 -> {
                llBlackContent.isVisible = true
                userContent.isVisible = false
                cancelBlack.isVisible = false
                blackContent.text = "对方已将你拉黑并限制访问其主页\n去看看其它感兴趣的人吧"
                ScreenUtils.setNonFullScreen(this)
            }
            4 -> {
                llBlackContent.isVisible = true
                userContent.isVisible = false
                cancelBlack.isVisible = true
                blackContent.text = "拉黑状态下双方主页互相不可见\n取消拉黑可恢复好友权益"
                ScreenUtils.setNonFullScreen(this)
            }
            else -> {
                userContent.isVisible = true
                llBlackContent.isVisible = false
                ScreenUtils.setFullScreen(this)
            }
        }
    }

    override fun onGetUserActionResult(success: Boolean, result: String?) {
        if (success) {
            if (result == "解除成功!") {
//                finish()
                CommonFunction.dissolveRelationship(matchBean?.accid ?: "")

            } else if (result == "拉黑成功!") {
                NIMClient.getService(FriendService::class.java).addToBlackList(matchBean!!.accid)
                NIMClient.getService(MsgService::class.java)
                    .deleteRecentContact2(matchBean!!.accid, SessionTypeEnum.P2P)
                NIMClient.getService(MsgService::class.java)
                    .clearChattingHistory(matchBean!!.accid, SessionTypeEnum.P2P)
                matchBean!!.isblock = 2
                updateBlockStatus()
//                EventBus.getDefault().post(UpdateLabelEvent(NewLabel(id = UserManager.getGlobalLabelId())))

            }
            CommonFunction.toast("$result")
        }
    }


    override fun onRemoveBlockResult(success: Boolean) {
        if (success) {
            NIMClient.getService(FriendService::class.java).removeFromBlackList(matchBean!!.accid)
            //1 互相没有拉黑  2 我拉黑了他  3  ta拉黑了我   4 互相拉黑
            if (matchBean!!.isblock == 4) {
                matchBean!!.isblock = 3
            } else if (matchBean!!.isblock == 2) {
                matchBean!!.isblock = 1
            }
            EventBus.getDefault().post(UpdateBlackEvent())
            updateBlockStatus()
        }

    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.moreBtn,
            R.id.moreBtn1 -> {//更多
                showMoreActionDialog()
            }
            R.id.detailUserChatLl -> {
                if (matchBean != null)
                    CommonFunction.commonGreet(
                        this,
                        matchBean!!.accid,
                        detailUserChatLl,
                        targetAvator = matchBean!!.avatar ?: ""
                    )
            }

            R.id.backBtn1, R.id.btnBack2,
            R.id.backBtn -> {
                finish()
            }
            R.id.cancelBlack -> { //取消拉黑
                mPresenter.removeBlock(
                    hashMapOf(
                        "token" to UserManager.getToken(),
                        "accid" to UserManager.getAccid(),
                        "target_accid" to matchBean!!.accid
                    )
                )
            }
        }

    }


    /**
     * 拉黑、举报、取消配对（判断对方是否为好友）、取消
     */
    private fun showMoreActionDialog() {
        val dialog = MoreActionDialog(this, "matchDetail")
        dialog.show()
        dialog.llRemoveRelation.visibility = if (matchBean!!.isfriend == 1) {
            View.VISIBLE
        } else {
            View.GONE
        }

        //拉黑
        dialog.llLahei.onClick {
            mPresenter.shieldingFriend(targetUserPramas)
            dialog.dismiss()
        }
        //举报
        dialog.llJubao.onClick {
            startActivityForResult<ReportReasonActivity>(
                100,
                "target_accid" to matchBean!!.accid,
                "nickname" to matchBean!!.nickname
            )
            dialog.dismiss()
        }
        //解除配对
        dialog.llRemoveRelation.onClick {
            mPresenter.dissolutionFriend(targetUserPramas)
            dialog.dismiss()
        }
        dialog.cancel.onClick {
            dialog.dismiss()
        }
    }


    /**
     * 更新本地的招呼次数
     */
    private fun updateLightCount() {
        //判断是否为好友 是：显示聊天
        //              否: 判断是否开启招呼,是否喜欢过
        if (matchBean!!.isfriend == 1 || matchBean!!.isgreeted) {//是好友就显示聊天
            detailUserChatBtn.text = "聊天"
            detailChatTypeIcon.setImageResource(R.drawable.icon_chat_white)
        } else {
            detailUserChatBtn.text = "打招呼"
            detailChatTypeIcon.setImageResource(R.drawable.icon_hi_match)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == SquarePlayDetailActivity.REQUEST_CODE) {
                EventBus.getDefault().post(NotifyEvent(data!!.getIntExtra("position", -1)))
            }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserDetailViewStateEvent(event: UserDetailViewStateEvent) {
        if (stateview.viewState != MultiStateView.VIEW_STATE_CONTENT) {
            stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserRelationshipEvent(event: UserRelationshipEvent) {
        matchBean?.isgreeted = true
        //更新打招呼次数和状态
        updateLightCount()
    }

}
