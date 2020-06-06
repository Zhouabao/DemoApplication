package com.sdy.jitangapplication.ui.activity

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
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
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.RecommendSquareListBean
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.presenter.MatchDetailPresenter
import com.sdy.jitangapplication.presenter.view.MatchDetailView
import com.sdy.jitangapplication.ui.adapter.*
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.ui.dialog.HelpWishDialog
import com.sdy.jitangapplication.ui.dialog.MoreActionDialog
import com.sdy.jitangapplication.ui.dialog.RightSlideOutdDialog
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CustomPagerSnapHelper
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_match_detail.*
import kotlinx.android.synthetic.main.dialog_more_action.*
import kotlinx.android.synthetic.main.empty_gift.view.emptyTip
import kotlinx.android.synthetic.main.empty_layout_block.view.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.footer_tag_quality.view.*
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
    OnLazyClickListener, ModuleProxy, ViewTreeObserver.OnGlobalLayoutListener {

    private val targetAccid by lazy { intent.getStringExtra("target_accid") }
    private var matchBean: MatchBean? = null
    var photos: MutableList<String> = mutableListOf()
    private val photosAdapter by lazy { MatchImgsPagerAdapter(this, photos) }
    private val params by lazy {
        hashMapOf<String, Any>(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "target_accid" to targetAccid
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

    private var setPadding = false
    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = MatchDetailPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        //设置图片的宽度占满屏幕，宽高比9:16
        val layoutParams = clPhotos.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.width = ScreenUtils.getScreenWidth()
        layoutParams.height = ScreenUtils.getScreenWidth()
        clPhotos.layoutParams = layoutParams


        val contactNumberparams1 = (contactNumber.layoutParams as FrameLayout.LayoutParams)
        contactNumberparams1.topMargin =
            layoutParams.height - SizeUtils.dp2px(49F) - SizeUtils.dp2px(38F)
        contactNumber.layoutParams = contactNumberparams1


        val contactNumberparams2 = (videoIntroduce.layoutParams as FrameLayout.LayoutParams)
        contactNumberparams2.topMargin =
            contactNumberparams1.topMargin - SizeUtils.dp2px(38F) - SizeUtils.dp2px(15F)
        videoIntroduce.layoutParams = contactNumberparams2


        moreBtn.setOnClickListener(this)
        moreBtn1.setOnClickListener(this)
        detailUserLikeBtn.setOnClickListener(this)
        detailUserDislikeBtn.setOnClickListener(this)
        detailUserChatBtn.setOnClickListener(this)
        detailUserGreetBtn.setOnClickListener(this)
        guidelWishHelp.setOnClickListener(this)
        giftAll.setOnClickListener(this)
        cancelBlack.setOnClickListener(this)
        backBtn.setOnClickListener(this)
        backBtn1.setOnClickListener(this)
        btnBack2.setOnClickListener(this)
        notifyAddTagBtn.setOnClickListener(this)
        contactNumber.setOnClickListener(this)
        videoIntroduce.setOnClickListener(this)
        clUserInfoTop.viewTreeObserver.addOnGlobalLayoutListener(this)


        //用户心愿列表
//        detailUserWishRv.viewTreeObserver.addOnGlobalLayoutListener(this)
        detailUserWishRv.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        CustomPagerSnapHelper().attachToRecyclerView(detailUserWishRv)
        detailUserWishRv.adapter = wishGiftAdapter
        wishGiftAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.helpWishBtn -> {
                    HelpWishDialog(
                        matchBean!!.mycandy_amount,
                        matchBean!!.accid,
                        matchBean!!.nickname ?: "",
                        wishGiftAdapter.data[position],
                        this,
                        matchBean!!.isfriend == 1
                    ).show()
                }
            }
        }
        detailUserWishIndicator.typeface =
            Typeface.createFromAsset(assets, "DIN_Alternate_Bold.ttf")
        detailUserWishRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                detailUserWishIndicator.text = SpanUtils.with(detailUserWishIndicator)
                    .append("${(detailUserWishRv.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() + 1}")
                    .setFontSize(14, true)
                    .setBold()
                    .append(" / ${matchBean!!.wish_cnt}")
                    .setFontSize(10, true)
                    .create()
            }
        })


        //用户详细信息列表
        detailUserInformationRv.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        detailUserInformationRv.adapter = detailUserInformationAdapter
        //用户动态
        listSquareRv.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        listSquareRv.adapter = adapter
        adapter.setEmptyView(R.layout.empty_layout_block, listSquareRv)
        adapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_other_square)

        adapter.isUseEmpty(false)
        adapter.bindToRecyclerView(listSquareRv)
        //用户兴趣
        detailRvTag.layoutManager = GridLayoutManager(this, 2)
        detailRvTag.adapter = userTagAdapter
        userTagAdapter.setEmptyView(R.layout.empty_gift, detailRvTag)
        userTagAdapter.emptyView.emptyTip.text = "暂时还没有添加兴趣"
        userTagAdapter.isUseEmpty(false)

        //用户礼物墙
        rvGift.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rvGift.adapter = usergiftAdapter
        usergiftAdapter.setEmptyView(R.layout.empty_gift, rvGift)
        usergiftAdapter.isUseEmpty(false)

        adapter.setPreLoadNumber(Constants.PAGESIZE * page - 5)
        adapter.setOnLoadMoreListener({
            if (adapter.data.size < page * Constants.PAGESIZE) {
                adapter.loadMoreEnd()
            } else {
                page += 1
                params1["page"] = page
                mPresenter.getSomeoneSquare(params1)
                adapter.setPreLoadNumber(Constants.PAGESIZE * page - 5)
            }
        }, listSquareRv)

        scrollDetail.setZoomView(detailPhotosVp)
        scrollDetail.setOnScrollListener { scrollX, scrollY, oldScrollX, oldScrollY ->
            if (clTop in 1..scrollY) {
                detailActionbar.alpha = 1F
                if (!setPadding) {
                    StatusBarUtil.setPaddingSmart(this, detailActionbar)
                    setPadding = true
                }
            } else {
                detailActionbar.alpha = 0F
            }
        }
        //重试
        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getUserDetailInfo(params)

        }

    }

    private var expand = false
    private fun initTagFooterView(): View {
        val view = layoutInflater.inflate(R.layout.footer_tag_quality, detailRvTag, false)
        view.expandAll.onClick {
            if (expand) {
                view.expandAll.text = "展开全部"
                view.expandAll.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    resources.getDrawable(R.drawable.icon_down_gray),
                    null
                )
                userTagAdapter.data.clear()
                val data = matchBean!!.label_quality
                userTagAdapter.addData(data.subList(0, 6))
                userTagAdapter.notifyDataSetChanged()

            } else {
                view.expandAll.text = "收起展示"
                view.expandAll.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    resources.getDrawable(R.drawable.icon_up_gray),
                    null
                )
                userTagAdapter.data.clear()

                val data = matchBean!!.label_quality
                userTagAdapter.addData(data)
                userTagAdapter.notifyDataSetChanged()

            }
            expand = !expand
        }
        return view
    }


    /**
     * 初始化个人信息数据
     */
    private val detailUserInformationAdapter by lazy { MatchDetailInfoAdapter() }

    /**
     *心愿礼物适配器
     */
    private val wishGiftAdapter by lazy { WishGiftAdapter(this) }


    @SuppressLint("SetTextI18n")
    private fun initData() {
        val data = matchBean!!.label_quality
        if (!matchBean!!.label_quality.isNullOrEmpty() && matchBean!!.label_quality.size > 6) {
            userTagAdapter.setFooterView(initTagFooterView())
            notifyAddTagBtn.isVisible = false
            userTagAdapter.addData(data.subList(0, 6))
        } else if (matchBean!!.label_quality.isNullOrEmpty()) {
            notifyAddTagBtn.isVisible = true
            if (matchBean!!.need_notice) {
                notifyAddTagBtn.text = "希望${if (matchBean!!.gender == 1) {
                    "他"
                } else {
                    "她"
                }}添加"
                notifyAddTagBtn.setTextColor(Color.parseColor("#789EFF"))
                notifyAddTagBtn.isEnabled = true
            } else {
                notifyAddTagBtn.text = "已通知"
                notifyAddTagBtn.setTextColor(Color.parseColor("#A5A5A5"))
                notifyAddTagBtn.isEnabled = false
            }
            userTagAdapter.isUseEmpty(true)
        } else {
            notifyAddTagBtn.isVisible = false
            userTagAdapter.addData(data)
        }

        guidelWishHelp.isVisible =
            !UserManager.isShowGuideHelpWish() && !matchBean!!.wish_list.isNullOrEmpty()
        val scaleAni = ObjectAnimator.ofFloat(guidelWishHelp, "scaleX", 0.9F, 1F, 0.9F)
        scaleAni.repeatCount = 2
        scaleAni.duration = 1500L
        val scaleAni1 = ObjectAnimator.ofFloat(guidelWishHelp, "scaleY", 0.9F, 1F, 0.9F)
        scaleAni1.repeatCount = 2
        scaleAni1.duration = 1500L
        val set = AnimatorSet()
        set.playTogether(scaleAni, scaleAni1)
        set.start()
        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                guidelWishHelp.isVisible = false
                UserManager.saveShowGuideHelpWish(true)
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })


        if (matchBean!!.gift_list.isNullOrEmpty()) {
            usergiftAdapter.isUseEmpty(true)
        } else
            usergiftAdapter.setNewData(matchBean!!.gift_list)

        detailUserInformationAdapter.setNewData(matchBean!!.personal_info)

        if (matchBean!!.wish_list.isNullOrEmpty()) {
            detailUserWishIndicator.isVisible = false
            detailUserWishRv.isVisible = false
        } else {
            detailUserWishRv.isVisible = true
            detailUserWishIndicator.isVisible = true
            wishGiftAdapter.setNewData(matchBean!!.wish_list)
            detailUserWishIndicator.text = SpanUtils.with(detailUserWishIndicator)
                .append("1")
                .setFontSize(14, true)
                .setBold()
                .append(" / ${matchBean!!.wish_cnt}")
                .setFontSize(10, true)
                .create()
        }

//        EventBus.getDefault().post(UpdateSquareEvent())

        detailUserName.text = matchBean!!.nickname ?: ""
        detailUserName.textSize = if ((matchBean!!.nickname ?: "").length > 6) {
            22F
        } else {
            25F
        }
        titleUsername.text = matchBean!!.nickname ?: ""

        //	0没有留下联系方式 1 电话 2 微信 3 qq 99隐藏
        when (matchBean!!.contact_way) {
            1 -> {
                contactNumber.isVisible = true
                contactNumber.setCompoundDrawablesWithIntrinsicBounds(
                    resources.getDrawable(R.drawable.icon_phone_reg),
                    null,
                    null,
                    null
                )
            }
            2 -> {
                contactNumber.isVisible = true
                contactNumber.setCompoundDrawablesWithIntrinsicBounds(
                    resources.getDrawable(R.drawable.icon_wechat_reg),
                    null,
                    null,
                    null
                )
            }
            3 -> {
                contactNumber.isVisible = true
                contactNumber.setCompoundDrawablesWithIntrinsicBounds(
                    resources.getDrawable(R.drawable.icon_qq_reg),
                    null,
                    null,
                    null
                )
            }
            else -> {
                contactNumber.isVisible = false
            }
        }

        videoIntroduce.isVisible = matchBean!!.mv_btn

        if (matchBean!!.intention_title.isNullOrEmpty()) {
            detailUserIntention1.isVisible = false
        } else {
            detailUserIntention1.isVisible = true
            detailUserIntention.text = matchBean!!.intention_title
            matchAimIv.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    matchAimIv.postDelayed({
                        matchAimIv.playAnimation()
                    }, 1000L)
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }
            })
            matchAimIv.playAnimation()
        }
        if (matchBean!!.online_time.isNullOrEmpty()) {
            detailUserOnline.isVisible = false
        } else {
            detailUserOnline.text = matchBean!!.online_time
            detailUserOnline.isVisible = true
        }
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
        detailUserDistance.isVisible = !matchBean!!.distance.isNullOrEmpty()
        detailUserDistance.text = "${matchBean!!.distance}"
        detailUserOnline.isVisible = !matchBean!!.online_time.isNullOrEmpty()
        detailUserOnline.text = "${matchBean!!.online_time}"
        detailUserSign.apply {
            setContent("${matchBean!!.sign}")
            isVisible = !(matchBean!!.sign.isNullOrBlank())
        }

        detailUserVip.visibility = if (matchBean!!.isvip == 1 || matchBean!!.isplatinumvip) {
            View.VISIBLE
        } else {
            View.GONE
        }
        if (matchBean!!.isplatinumvip) {
            detailUserVip.setImageResource(R.drawable.icon_pt_vip)
        } else {
            detailUserVip.setImageResource(R.drawable.icon_vip)
        }
        detailUserVerify.isVisible = matchBean!!.isfaced == 1

        //更新打招呼次数和状态
        updateLightCount()


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
            "pagesize" to Constants.PAGESIZE,
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
                //状态栏透明和间距处理
                StatusBarUtil.immersive(this)
//                StatusBarUtil.setMargin(this, clPhotos)
            }
            2 -> {
                llBlackContent.isVisible = true
                userContent.isVisible = false
                cancelBlack.isVisible = true
                blackContent.text = "拉黑状态下双方主页互相不可见\n取消拉黑可恢复好友权益"
            }
            3 -> {
                llBlackContent.isVisible = true
                userContent.isVisible = false
                cancelBlack.isVisible = false
                blackContent.text = "对方已将你拉黑并限制访问其主页\n去看看其它感兴趣的人吧"
            }
            4 -> {
                llBlackContent.isVisible = true
                userContent.isVisible = false
                cancelBlack.isVisible = true
                blackContent.text = "拉黑状态下双方主页互相不可见\n取消拉黑可恢复好友权益"
            }
            else -> {
                userContent.isVisible = true
                llBlackContent.isVisible = false
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
                            showGreetAnimatioon()
//                            updateLightCount(-1)
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
                            showGreetAnimatioon()
//                            updateLightCount(-1)
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

    private fun showGreetAnimatioon() {
        detailUserGreetBtn.playAnimation()
        val translateDislike = ObjectAnimator.ofFloat(
            detailUserDislikeBtn,
            "translationX",
            SizeUtils.dp2px(-150F).toFloat()
        )
        translateDislike.duration = 250L
        translateDislike.interpolator = LinearInterpolator()
        translateDislike.start()

        val translateLike = ObjectAnimator.ofFloat(
            detailUserLikeBtn,
            "translationX",
            SizeUtils.dp2px(150F).toFloat()
        )
        translateLike.duration = 250L
        translateLike.interpolator = LinearInterpolator()
        translateLike.start()
        detailUserGreetBtn.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                updateLightCount()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })
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

    override fun onNeedNoticeResult(success: Boolean) {
        if (success) {
            matchBean!!.need_notice = false
            notifyAddTagBtn.text = "已通知"
            notifyAddTagBtn.setTextColor(Color.parseColor("#A5A5A5"))
            CommonFunction.toast("已提示对方更新兴趣标签")
            notifyAddTagBtn.isEnabled = false
        }
    }

    override fun onGetSquareListResult(
        data: RecommendSquareListBean?,
        result: Boolean,
        isRefresh: Boolean
    ) {
        if (result) {
            stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (data?.list == null || data!!.list!!.size == 0) {
                if (adapter.data.isNullOrEmpty()) {
                    adapter.isUseEmpty(true)
                    adapter.loadMoreEnd()
                }
                adapter.notifyDataSetChanged()
            } else {
                if (data?.list.size > 0) {
                    for (data in data?.list) {
                        data.originalLike = data.isliked
                        data.originalLikeCount = data.like_cnt
                    }
                    adapter.addData(data?.list)
                }
                adapter.loadMoreComplete()
            }
        } else {
            if (page > 1)
                adapter.loadMoreFail()
            else
                stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        }

    }

    override fun onLazyClick(view: View) {
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
                    CommonFunction.checkSendGift(this, matchBean!!.accid)
            }

            R.id.detailUserGreetBtn -> {
                if (matchBean != null)
                    CommonFunction.checkSendGift(this, matchBean!!.accid)
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
            R.id.guidelWishHelp -> {//查看全部礼物
                guidelWishHelp.isVisible = false
                UserManager.saveShowGuideHelpWish(true)
            }
            R.id.notifyAddTagBtn -> {//通知对方添加特质
                mPresenter.needNotice(hashMapOf<String, Any>("target_accid" to matchBean!!.accid))
                notifyAddTagBtn.isEnabled = false
            }
            R.id.contactNumber -> {//获取联系方式
                CommonFunction.checkUnlockContact(
                    this,
                    matchBean!!.accid,
                    matchBean!!.gender ?: 1
                )
            }
            R.id.videoIntroduce -> {//获取认证视频
                CommonFunction.checkUnlockIntroduceVideo(
                    this,
                    matchBean!!.accid,
                    matchBean!!.gender ?: 1
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
            mPresenter.shieldingFriend(params)
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
            mPresenter.dissolutionFriend(params)
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
        //已感兴趣或者是好友不做操作
        //1.是好友
        //2.打过招呼
        //3.喜欢过
        //4.不喜欢
        //5.normal状态

        if (matchBean!!.isfriend == 1) {//是好友
            detailUserChatBtn.setImageResource(R.drawable.icon_matchdetail_chat)
            detailUserChatBtn.isVisible = true
            detailUserGreetBtn.isVisible = false
            detailUserLikeBtn.isVisible = false
            detailUserDislikeBtn.visibility = View.INVISIBLE
        } else {//不是好友,判断打请求过私聊没
            if (matchBean!!.isgreeted) {
                detailUserGreetBtn.setImageResource(R.drawable.icon_matchdetail_hi)
            } else {
                detailUserGreetBtn.imageAssetsFolder = "images_greet_match_detail"
                detailUserGreetBtn.setAnimation("data_greet_match_detail.json")
            }
            detailUserChatBtn.isVisible = false
            //喜欢或者不喜欢都关闭喜欢按钮
            if (matchBean!!.isliked == 1 || matchBean!!.isdisliked == 1) {
                detailUserLikeBtn.isVisible = false
                detailUserDislikeBtn.visibility = View.INVISIBLE
            } else {
                detailUserLikeBtn.isVisible = true
                detailUserDislikeBtn.visibility = View.VISIBLE
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
    fun onGreetDetailSuccessEvent(event: GreetDetailSuccessEvent) {
        if (event.success) {
            matchBean!!.isgreeted = true
            updateLightCount()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMatchByWishHelpEvent(event: MatchByWishHelpEvent) {
        if (event.isFirend) {
            matchBean!!.isfriend = 1
            showGreetAnimatioon()
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateMyCandyAmountEvent(event: UpdateMyCandyAmountEvent) {
        matchBean!!.mycandy_amount = matchBean!!.mycandy_amount - event.reduceAmout
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
                    startActivity<MatchSucceedActivity>(
                        "avator" to matchBean!!.avatar,
                        "nickname" to matchBean!!.nickname,
                        "accid" to matchBean!!.accid
                    )
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

    var clTop = 0
    override fun onGlobalLayout() {
        clTop = clUserInfoTop.top
        if (clTop > 0) {
            clUserInfoTop.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    }


}
