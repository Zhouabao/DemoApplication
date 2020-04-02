package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.marginTop
import androidx.core.view.size
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.google.android.material.appbar.AppBarLayout
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nim.uikit.business.session.module.Container
import com.netease.nim.uikit.business.session.module.ModuleProxy
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.friend.FriendService
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.DetailUserInfoBean
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.RecommendSquareListBean
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.presenter.MatchDetailPresenter
import com.sdy.jitangapplication.presenter.view.MatchDetailView
import com.sdy.jitangapplication.ui.adapter.*
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.ui.dialog.MoreActionDialog
import com.sdy.jitangapplication.ui.dialog.RightSlideOutdDialog
import com.sdy.jitangapplication.ui.dialog.SayHiDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_match_detail.*
import kotlinx.android.synthetic.main.dialog_more_action.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.sdk27.coroutines.onTouch
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

/**
 * 匹配详情页
 */
class MatchDetailActivity : BaseMvpActivity<MatchDetailPresenter>(), MatchDetailView,
    View.OnClickListener,
    ModuleProxy {

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


    private val userTagAdapter by lazy { UserCenteTagAdapter() }
    private val usergiftAdapter by lazy { UserCenteGiftAdapter() }
    private val adapter by lazy { RecommendSquareAdapter() }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = MatchDetailPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        //设置图片的宽度占满屏幕，宽高比9:16
        val layoutParams = clPhotos.layoutParams
        layoutParams.width = ScreenUtils.getScreenWidth()
        layoutParams.height = ScreenUtils.getScreenWidth()
//        layoutParams.height = ScreenUtils.getScreenHeight() - SizeUtils.dp2px(197F)
        clPhotos.layoutParams = layoutParams

        //设置个人信息距离顶部的距离
//        val paramsClUserInfo = clUserInfo.layoutParams as LinearLayout.LayoutParams
        val paramsClUserInfo = clUserInfo.layoutParams as FrameLayout.LayoutParams
        paramsClUserInfo.topMargin = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(26F)
        paramsClUserInfo.height = LinearLayout.LayoutParams.WRAP_CONTENT
        clUserInfo.layoutParams = paramsClUserInfo

//        vpUserDetail.setScrollable(false)
        moreBtn.setOnClickListener(this)
        moreBtn1.setOnClickListener(this)
        detailUserLikeBtn.setOnClickListener(this)
        detailUserDislikeBtn.setOnClickListener(this)
        detailUserChatBtn.setOnClickListener(this)
        detailUserGreetBtn.setOnClickListener(this)
        giftAll.setOnClickListener(this)
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
        detailUserInformationRv.adapter = detailUserInformationAdapter
        //用户动态
        listSquareRv.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        listSquareRv.adapter = adapter
        adapter.setEmptyView(R.layout.empty_layout_block, listSquareRv)
        adapter.isUseEmpty(false)
        adapter.bindToRecyclerView(listSquareRv)
        //用户兴趣
        detailRvTag.layoutManager = GridLayoutManager(this, 2)
        detailRvTag.adapter = userTagAdapter
        //用户礼物墙
        rvGift.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rvGift.adapter = usergiftAdapter


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

    }


    /**
     * 切换tab
     */

    //fragment栈管理
//    private val mStack = Stack<Fragment>()

    /*
      初始化Fragment栈管理
   */
    private fun initFragment() {
//        mStack.add(ListSquareFragment(targetAccid))
//        mStack.add(BlockSquareFragment(targetAccid))
//        mStack.add(MatchDetailLabelFragment(targetAccid))
//        vpUserDetail.offscreenPageLimit = 3
//        vpUserDetail.adapter = MainPagerAdapter(supportFragmentManager, mStack)
//        vpUserDetail.currentItem = 1
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
        usergiftAdapter.setNewData(matchBean!!.gift_list)

        initFragment()//初始化vp
        initUserInfomationData()//初始化个人信息数据
//        EventBus.getDefault().post(UpdateSquareEvent())
        gift.text = "礼物墙 ${matchBean!!.candy_cnt ?: 0}"
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
        updateLightCount(matchBean!!.lightningcnt ?: 0)

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
    private var downX = 0F
    private var upX = 0F
    private fun setViewpagerAndIndicator() {
        detailPhotosVp.onTouch { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                }
                MotionEvent.ACTION_UP -> {
                    upX = event.x
                    if (Math.abs(upX - downX) < 10) {
                        if (upX < ScreenUtils.getScreenWidth() / 2F) {
                            if (detailPhotosVp.currentItem > 0) {
                                detailPhotosVp.setCurrentItem(detailPhotosVp.currentItem - 1, true)
                            }
                        } else {
                            if (detailPhotosVp.currentItem < photos.size - 1) {
                                detailPhotosVp.setCurrentItem(detailPhotosVp.currentItem + 1, true)
                            }
                        }
                    }
                    downX = 0F
                }
            }
        }


        detailPhotosVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
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
                val layoutParams: LinearLayout.LayoutParams =
                    indicator.layoutParams as LinearLayout.LayoutParams
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
    }


    /**
     * 获取用户详情结果
     */
    //1 互相没有拉黑  2 我拉黑了他  3  ta拉黑了我   4 互相拉黑
    private var page = 1
    private val params1 by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "page" to page,
            "target_accid" to targetAccid
        )
    }

    override fun onGetMatchDetailResult(success: Boolean, matchUserDetailBean: MatchBean?) {
        if (success) {
            matchBean = matchUserDetailBean
            updateBlockStatus()
            mPresenter.getSomeoneSquare(params1)
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


    override fun onGetLikeResult(
        success: Boolean,
        statusBean: BaseResp<StatusBean?>?,
        islike: Boolean
    ) {

        if (islike) {
            if (statusBean != null)
                if (statusBean.code == 200) {
                    if (ActivityUtils.isActivityAlive(LikeMeReceivedActivity::class.java.newInstance())) {
                        EventBus.getDefault().post(UpdateLikeMeReceivedEvent())
                    }

                    if (statusBean.data?.residue == 0) {
                        ChargeVipDialog(ChargeVipDialog.INFINITE_SLIDE, this).show()
                    } else {
                        EventBus.getDefault().post(RefreshEvent(true))
                        if (statusBean.data?.status == 1) {  //喜欢成功
                            matchBean!!.isliked = 1
                            updateLightCount(-1)
                        } else if (statusBean.data?.status == 2) {//匹配成功
                            //如果是来自喜欢我的界面， 就刷新
                            if (intent.getIntExtra("parPos", -1) != -1) {
                                EventBus.getDefault().post(
                                    UpdateLikemeOnePosEvent(
                                        intent.getIntExtra("parPos", -1),
                                        intent.getIntExtra("childPos", -1)
                                    )
                                )
                            }
                            //喜欢过
                            matchBean!!.isfriend = 1
                            updateLightCount(-1)
                            sendChatHiMessage(ChatHiAttachment.CHATHI_MATCH)
                        }
                    }
                } else if (statusBean.code == 201) {
                    if (matchBean!!.my_percent_complete <= matchBean!!.normal_percent_complete)
                        RightSlideOutdDialog(
                            this,
                            matchBean!!.my_like_times,
                            matchBean!!.total_like_times
                        ).show()
                    else
                        ChargeVipDialog(ChargeVipDialog.INFINITE_SLIDE, this).show()
                }
        } else {
            if (statusBean != null)
                if (statusBean.code == 200) {
                    if (ActivityUtils.isActivityAlive(LikeMeReceivedActivity::class.java.newInstance())) {
                        EventBus.getDefault().post(UpdateLikeMeReceivedEvent())
                    }

                    if (statusBean.data != null) {
                        EventBus.getDefault().post(RefreshEvent(true))
                        finish()
                    } else {
                        CommonFunction.toast(statusBean.msg)
                    }
                }
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

    override fun onGetSquareListResult(
        data: RecommendSquareListBean?,
        result: Boolean,
        isRefresh: Boolean
    ) {
        if (result) {
            stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (data == null || data.list == null || data!!.list!!.size == 0) {
                if (adapter.data.isNullOrEmpty()) {
                    adapter.isUseEmpty(true)
                }
                listRefresh.finishLoadMoreWithNoMoreData()
            } else {
                if (data?.list.size > 0) {
                    for (data in data?.list) {
                        data.originalLike = data.isliked
                        data.originalLikeCount = data.like_cnt
                    }
                    adapter.addData(data?.list)
                }
                listRefresh.finishLoadMore(true)
            }
        } else {
            if (listRefresh != null && page > 1)
                listRefresh.finishLoadMore(false)
            else
                stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        }

    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.moreBtn,
            R.id.moreBtn1 -> {//更多
                showMoreActionDialog()
            }
            R.id.detailUserDislikeBtn -> {//不感兴趣
                mPresenter.dislikeUser(params)
            }
            R.id.detailUserLikeBtn -> {//感兴趣
                mPresenter.likeUser(params)
            }
            //这里要判断是不是VIP用户 如果是VIP 直接进入聊天界面
            //1.首先判断是否有次数，
            // 若有 就打招呼
            // 若无 就弹充值
            R.id.detailUserChatBtn -> {
                if (matchBean != null)
                    CommonFunction.commonGreet(
                        this,
                        matchBean!!.accid,
                        detailUserChatBtn,
                        targetAvator = matchBean!!.avatar ?: "", needSwipe = true
                    )
            }

            R.id.detailUserGreetBtn -> {
                if (matchBean != null)
                    CommonFunction.commonGreet(
                        this,
                        matchBean!!.accid,
                        detailUserGreetBtn,
                        targetAvator = matchBean!!.avatar ?: "", needSwipe = true
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
            R.id.giftAll -> { //查看全部礼物
                startActivity<SomeoneGetGiftActivity>("target_accid" to targetAccid)
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
    private fun updateLightCount(lightningcnt: Int) {
        if (lightningcnt != -1) {
            UserManager.saveLightingCount(lightningcnt)
            EventBus.getDefault().postSticky(UpdateHiCountEvent())
        }

        //已感兴趣或者是好友不做操作
        if (matchBean!!.isliked == 1 || matchBean!!.isfriend == 1) {
            detailUserDislikeBtn.visibility = View.INVISIBLE
            detailUserLikeBtn.isVisible = false
            detailUserGreetBtn.isVisible = true
        } else {
            detailUserDislikeBtn.visibility = View.VISIBLE
            detailUserLikeBtn.isVisible = true
            detailUserGreetBtn.isVisible = true
        }

        //判断是否为好友 是：显示聊天
        //              否: 判断是否开启招呼,是否喜欢过
        if (matchBean!!.isfriend == 1) {//是好友就显示聊天
            detailUserChatBtn.text = "聊  天"
            detailUserChatBtn.isVisible = true
            detailUserGreetBtn.isVisible = false
        } else {
            detailUserChatBtn.isVisible = false
            detailUserGreetBtn.isVisible = true
            detailUserLikeBtn.isVisible = matchBean!!.isliked != 1//喜欢过就不显示“感兴趣”
            if (!matchBean!!.greet_switch) {//招呼未开启不显示打招呼
                detailUserChatBtn.isVisible = false
                detailUserGreetBtn.isVisible = false
            } else {//招呼开启,   招呼有效 与 招呼无效
                if (matchBean!!.isgreeted) {
                    detailUserGreetBtn.isVisible = false
                    detailUserChatBtn.isVisible = true
                    detailUserChatBtn.text = "继续聊天"
                } else {
                    detailUserGreetBtn.isVisible = true
                    detailUserChatBtn.isVisible = false
                }
            }


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
    fun onGreetDetailSuccessEvent(event: GreetDetailSuccessEvent) {
        if (event.success) {
            matchBean!!.isgreeted = true
            updateLightCount(UserManager.getLightingCount() - 1)
        }
    }


    /*--------------------------消息代理------------------------*/
    private fun sendChatHiMessage(type: Int) {
        Log.d("OkHttp", matchBean?.accid ?: "")
        val container = Container(this, matchBean?.accid, SessionTypeEnum.P2P, this, true)
        val chatHiAttachment = ChatHiAttachment(type)
        val config = CustomMessageConfig()
        config.enablePush = false
        val message = MessageBuilder.createCustomMessage(
            matchBean?.accid,
            SessionTypeEnum.P2P,
            "",
            chatHiAttachment,
            config
        )
        container.proxy.sendMessage(message)
    }

    override fun sendMessage(msg: IMMessage): Boolean {
        NIMClient.getService(MsgService::class.java).sendMessage(msg, false).setCallback(object :
            RequestCallback<Void?> {
            override fun onSuccess(param: Void?) {
                if (msg.attachment is ChatHiAttachment) {
                    if ((msg.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_HI) {
                        SayHiDialog(
                            matchBean!!.accid,
                            matchBean!!.nickname ?: "",
                            this@MatchDetailActivity
                        ).show()
                    } else {
                        startActivity<MatchSucceedActivity>(
                            "avator" to matchBean!!.avatar,
                            "nickname" to matchBean!!.nickname,
                            "accid" to matchBean!!.accid
                        )
                    }
                }

            }

            override fun onFailed(code: Int) {
            }

            override fun onException(exception: Throwable) {
            }
        })
        return true
    }

    override fun onInputPanelExpand() {

    }

    override fun shouldCollapseInputPanel() {

    }

    override fun isLongClickEnabled(): Boolean {
        return false
    }

    override fun onItemFooterClick(message: IMMessage?) {

    }

}
