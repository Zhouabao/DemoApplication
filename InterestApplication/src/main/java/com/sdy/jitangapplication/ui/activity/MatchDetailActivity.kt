package com.sdy.jitangapplication.ui.activity

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
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
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.friend.FriendService
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.MatchByWishHelpEvent
import com.sdy.jitangapplication.event.NotifyEvent
import com.sdy.jitangapplication.event.UpdateBlackEvent
import com.sdy.jitangapplication.event.UpdateMyCandyAmountEvent
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.RecommendSquareListBean
import com.sdy.jitangapplication.presenter.MatchDetailPresenter
import com.sdy.jitangapplication.presenter.view.MatchDetailView
import com.sdy.jitangapplication.ui.adapter.*
import com.sdy.jitangapplication.ui.dialog.MoreActionDialog
import com.sdy.jitangapplication.utils.UserManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.umeng.socialize.UMShareAPI
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_match_detail.*
import kotlinx.android.synthetic.main.dialog_more_action.*
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
    OnLazyClickListener, ViewTreeObserver.OnGlobalLayoutListener, OnRefreshLoadMoreListener {

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

        refreshListSquare.setOnRefreshLoadMoreListener(this)


        //设置图片的宽度占满屏幕，宽高比4:5
        val layoutParams = clPhotos.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.width = ScreenUtils.getScreenWidth()
        layoutParams.height = ScreenUtils.getScreenWidth() / 4 * 5
        clPhotos.layoutParams = layoutParams


        moreBtn.setOnClickListener(this)
        moreBtn1.setOnClickListener(this)
        detailUserChatBtn.setOnClickListener(this)
        giftAll.setOnClickListener(this)
        cancelBlack.setOnClickListener(this)
        backBtn.setOnClickListener(this)
        backBtn1.setOnClickListener(this)
        btnBack2.setOnClickListener(this)
        notifyAddTagBtn.setOnClickListener(this)
        contactCl.setOnClickListener(this)
        clUserInfoTop.viewTreeObserver.addOnGlobalLayoutListener(this)


        //用户详细信息列表
        detailUserInformationRv.layoutManager = GridLayoutManager(this, 3)
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

        //点击播放视频
        userVideoPlayBtn.clickWithTrigger {
            CommonFunction.checkUnlockIntroduceVideo(this, matchBean!!.accid)
        }

        matchUserVideo.backButton.clickWithTrigger {
            matchUserVideo.isVisible = false
            GSYVideoManager.releaseAllVideos()
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



        if (matchBean!!.gift_list.isNullOrEmpty()) {
            usergiftAdapter.isUseEmpty(true)
        } else
            usergiftAdapter.setNewData(matchBean!!.gift_list)

        detailUserInformationAdapter.setNewData(matchBean!!.personal_info)

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
                contactCl.isVisible = true
                contactCl.setCompoundDrawablesWithIntrinsicBounds(
                    resources.getDrawable(R.drawable.icon_phone_white),
                    null,
                    null,
                    null
                )
                detailUserChatBtn.setImageResource(R.drawable.icon_match_chat_small)
            }
            2 -> {
                contactCl.isVisible = true
                contactCl.setCompoundDrawablesWithIntrinsicBounds(
                    resources.getDrawable(R.drawable.icon_wechat_white),
                    null,
                    null,
                    null
                )
                detailUserChatBtn.setImageResource(R.drawable.icon_match_chat_small)
            }
            3 -> {
                contactCl.isVisible = true
                contactCl.setCompoundDrawablesWithIntrinsicBounds(
                    resources.getDrawable(R.drawable.icon_qq_white),
                    null,
                    null,
                    null
                )
                detailUserChatBtn.setImageResource(R.drawable.icon_match_chat_small)
            }
            else -> {
                contactCl.isVisible = false
                detailUserChatBtn.setImageResource(R.drawable.icon_match_chat_big)
            }
        }

        userVideoCl.isVisible = matchBean!!.mv_btn
        //钻石或者女性可以免费看
        if (matchBean!!.myisplatinumvip )
            GlideUtil.loadImg(this, matchBean!!.mv_url, userVideoCover)
        else
            Glide.with(this)
                .load(matchBean!!.mv_url)
                .thumbnail(0.5F)
                .placeholder(R.drawable.default_image_10dp)
                .priority(Priority.NORMAL)
                .transform(BlurTransformation(25))
                .into(userVideoCover)
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

        detailUserGenderAndAge.text = "${matchBean!!.age}"
        if (matchBean!!.gender == 1) {
            detailUserGenderAndAge.setBackgroundResource(R.drawable.rectangle_orange_9dp)
            detailUserGenderAndAge.setCompoundDrawablesWithIntrinsicBounds(
                resources.getDrawable(R.drawable.icon_gender_boy_detail),
                null,
                null,
                null
            )
        } else {
            detailUserGenderAndAge.setBackgroundResource(R.drawable.rectangle_rosepink_9dp)
            detailUserGenderAndAge.setCompoundDrawablesWithIntrinsicBounds(
                resources.getDrawable(R.drawable.icon_gender_girl_detail),
                null,
                null,
                null
            )
        }
        detailUserDistance.isVisible = !matchBean!!.distance.isNullOrEmpty()
        detailUserDistance.text = "${matchBean!!.distance}"
        detailUserOnline.isVisible = !matchBean!!.online_time.isNullOrEmpty()
        detailUserOnline.text = "${matchBean!!.online_time}"
        detailUserSign.apply {
            setContent("${matchBean!!.sign}")
            isVisible = !(matchBean!!.sign.isNullOrBlank())
        }

        detailUserVip.visibility = if (matchBean!!.isplatinumvip) {
            View.VISIBLE
        } else {
            View.GONE
        }
        if (matchBean!!.isplatinumvip) {
            detailUserVip.setImageResource(R.drawable.icon_pt_vip)
        }


        if (matchBean!!.isfaced == 1) {
            detailUserVerify.isVisible = true
            detailUserVerify.setCompoundDrawablesWithIntrinsicBounds(
                resources.getDrawable(
                    if (matchBean!!.gender == 1) {
                        R.drawable.icon_gender_man_detail
                    } else {
                        R.drawable.icon_gender_woman_detail
                    }
                ), null, null, null
            )
            detailUserVerify.text = matchBean!!.face_str
        } else {
            detailUserVerify.isVisible = false
        }


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

//            stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (data?.list == null || data.list.size == 0) {
                if (adapter.data.isNullOrEmpty()) {
                    adapter.isUseEmpty(true)
                }
                adapter.notifyDataSetChanged()
            } else {
                if (data.list.size > 0) {
                    for (data in data.list) {
                        data.originalLike = data.isliked
                        data.originalLikeCount = data.like_cnt
                    }
                    adapter.addData(data.list)
                }
            }

            if (data?.list.isNullOrEmpty() || (data?.list
                    ?: mutableListOf()).size < Constants.PAGESIZE
            )
                refreshListSquare.finishLoadMoreWithNoMoreData()
            else
                refreshListSquare.finishLoadMore()
        } else {
            if (page == 1)
                stateview.viewState = MultiStateView.VIEW_STATE_ERROR
            else
                refreshListSquare.finishLoadMore(false)
        }

    }

    override fun onLazyClick(view: View) {
        when (view.id) {
            R.id.moreBtn,
            R.id.moreBtn1 -> {//更多
                showMoreActionDialog()
            }

            //这里要判断是不是VIP用户 如果是VIP 直接进入聊天界面
            //1.首先判断是否有次数，
            // 若有 就打招呼
            // 若无 就弹充值
            R.id.detailUserChatBtn -> {
                if (matchBean != null)
                    CommonFunction.checkSendGift(this, matchBean!!.accid)
            }

            R.id.backBtn1, R.id.btnBack2,
            R.id.backBtn -> {
                onBackPressed()
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

            R.id.notifyAddTagBtn -> {//通知对方添加特质
                mPresenter.needNotice(hashMapOf<String, Any>("target_accid" to matchBean!!.accid))
                notifyAddTagBtn.isEnabled = false
            }
            R.id.contactCl -> {//获取联系方式
                CommonFunction.checkUnlockContact(
                    this,
                    matchBean!!.accid,
                    matchBean!!.gender ?: 1
                )
            }
//            R.id.videoIntroduce -> {//获取认证视频
//                CommonFunction.checkUnlockIntroduceVideo(
//                    this,
//                    matchBean!!.accid,
//                    matchBean!!.gender ?: 1
//                )
//            }
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == SquarePlayDetailActivity.REQUEST_CODE) {
                EventBus.getDefault().post(NotifyEvent(data!!.getIntExtra("position", -1)))
            }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMatchByWishHelpEvent(event: MatchByWishHelpEvent) {
        if (event.isFirend) {
            matchBean!!.isfriend = 1
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateMyCandyAmountEvent(event: UpdateMyCandyAmountEvent) {
        matchBean!!.mycandy_amount = matchBean!!.mycandy_amount - event.reduceAmout
    }


    var clTop = 0
    override fun onGlobalLayout() {
        clTop = clUserInfoTop.top
        if (clTop > 0) {
            clUserInfoTop.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page += 1
        params1["page"] = page
        mPresenter.getSomeoneSquare(params1)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
    }

    override fun onBackPressed() {
        if (matchUserVideo.isVisible) {
            matchUserVideo.isVisible = false
            GSYVideoManager.releaseAllVideos()
        } else {
            super.onBackPressed()
        }
    }


}
