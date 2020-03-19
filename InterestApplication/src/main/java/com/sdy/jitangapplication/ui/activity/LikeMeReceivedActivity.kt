package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.UpdateHiEvent
import com.sdy.jitangapplication.event.UpdateLikeMeReceivedEvent
import com.sdy.jitangapplication.event.UpdateSlideCountEvent
import com.sdy.jitangapplication.model.NewLikeMeBean
import com.sdy.jitangapplication.model.PositiveLikeBean
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.presenter.LikeMeReceivedPresenter
import com.sdy.jitangapplication.presenter.view.LikeMeReceivedView
import com.sdy.jitangapplication.ui.adapter.LikeMeUserAdapter
import com.sdy.jitangapplication.ui.dialog.GuideLikeDialog
import com.sdy.jitangapplication.utils.UserManager
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.activity_like_me_received.*
import kotlinx.android.synthetic.main.empty_friend_layout.view.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 * 对我感兴趣未读
 */
class LikeMeReceivedActivity : BaseMvpActivity<LikeMeReceivedPresenter>(), LikeMeReceivedView, CardStackListener {
    //我的资料完整度
    private var my_percent_complete: Int = 0//（我的资料完整度）
    //标准完整度
    private var normal_percent_complete: Int = 0//（标准完整度）
    private var myCount: Int = 0//当前滑动次数
    private var maxCount: Int = 0//最大滑动次数
    private var page = 1
    private val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "pagesize" to Constants.PAGESIZE,
            "page" to page
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like_me_received)
        initView()
        mPresenter.likeListsV2(params)

    }


    private fun initView() {
        EventBus.getDefault().register(this)

        BarUtils.setStatusBarLightMode(this, false)

        mPresenter = LikeMeReceivedPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        btnBack.setImageResource(R.drawable.icon_back_white)
        btnBack.onClick {
            finish()
        }
        hotT1.setTextColor(Color.WHITE)
        hotT1.text = "新的喜欢"
        rightBtn.isVisible = true
        rightBtn.setTextColor(Color.WHITE)
        rightBtn.text = "全部喜欢"
        rightBtn.setOnClickListener {
            startActivity<MessageLikeMeActivity>()
        }
        divider.setBackgroundColor(Color.TRANSPARENT)
        llTitle.setBackgroundColor(Color.TRANSPARENT)

        stateLikeReceived.emptyImg.setImageResource(R.drawable.icon_empty_like_user)
        stateLikeReceived.emptyFriendTitle.text = "这里没有内容了"
        stateLikeReceived.emptyFriendTitle.setTextColor(Color.WHITE)
        stateLikeReceived.emptyFriendTip.text = "进入全部喜欢查看更多喜欢你的用户"
        stateLikeReceived.emptyFriendTip.setTextColor(Color.parseColor("#FFB5B7B9"))

        stateLikeReceived.retryBtn.onClick {
            stateLikeReceived.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.likeListsV2(params)
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.likeBtn -> {
                    val setting = SwipeAnimationSetting.Builder()
                        .setDirection(Direction.Right)
                        .setDuration(Duration.Normal.duration)
                        .setInterpolator(AccelerateInterpolator())
                        .build()
                    manager.setSwipeAnimationSetting(setting)
                    greetRv.swipe()
                }
                R.id.v1 -> {
                    MatchDetailActivity.start(this, adapter.data[position].accid)
                }
            }
        }


        //初始化卡片布局
        initialize()
    }

    private var hasMore = true
    private var likeCount = 0
    override fun onGreatListResult(t: BaseResp<NewLikeMeBean?>) = if (t.data != null && t.code == 200) {
        if (t.data!!.list.isNullOrEmpty() || t.data!!.list.size < Constants.PAGESIZE) {
            hasMore = false
        }
        stateLikeReceived.viewState = MultiStateView.VIEW_STATE_CONTENT
        adapter.addData(t.data!!.list)
        if ((page == 1 && t.data!!.list.isNullOrEmpty()) || (page > 1 && !hasMore)) {
            stateLikeReceived.viewState = MultiStateView.VIEW_STATE_EMPTY
        } else {
            if (!UserManager.isShowGuideLike()) {
                GuideLikeDialog(this).show()
            }
        }
        if (page == 1) {
            mPresenter.markLikeRead()
        }

        my_percent_complete = t.data!!.my_percent_complete
        normal_percent_complete = t.data!!.normal_percent_complete
        myCount = t.data!!.my_like_times
        maxCount = t.data!!.total_like_times

        likeCount = t.data!!.count
        if (t.data!!.count > 0) {
            likeLeftCount.isVisible = true
            likeLeftCount.text = SpanUtils.with(likeLeftCount)
                .append("有")
                .append(" ${t.data!!.count} ")
                .setForegroundColor(resources.getColor(R.color.colorOrange))
                .setBold()
                .append("个人喜欢你")
                .setForegroundColor(Color.parseColor("#FFC5C6C8"))
                .create()
        } else {
            likeLeftCount.isVisible = false
        }
    } else {
        stateLikeReceived.viewState = MultiStateView.VIEW_STATE_ERROR
    }


    //     * type:1 dianji  2 youhua
    override fun onLikeOrGreetStateResult(data: BaseResp<StatusBean?>, type: Int) {
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)

        if (data.code == 200) {
            if (likeCount > 0 && likeLeftCount.isVisible) {
                likeCount -= 1
                likeLeftCount.text = SpanUtils.with(likeLeftCount)
                    .append("有")
                    .append(" $likeCount ")
                    .setForegroundColor(resources.getColor(R.color.colorOrange))
                    .setBold()
                    .append("个人喜欢你")
                    .setForegroundColor(Color.parseColor("#FFC5C6C8"))
                    .create()
            }

            if (data.data != null) {
                if (data.data!!.status == 2) {//status :1.喜欢成功  2.匹配成功
                    sendChatHiMessage(
                        ChatHiAttachment.CHATHI_MATCH, if (type == 1) {
                            adapter.data[manager.topPosition]
                        } else {
                            adapter.data[manager.topPosition - 1]
                        }
                    )
                }
                if (manager.topPosition == adapter.itemCount && !hasMore) {
                    stateLikeReceived.viewState = MultiStateView.VIEW_STATE_EMPTY
                }
                EventBus.getDefault().post(UpdateHiEvent())
            } else {
                CommonFunction.toast(data.msg)
                greetRv.rewind()
            }

        } else if (data.code == 405) {
            CommonFunction.toast(data.msg)
            greetRv.rewind()
        }


        if (hasMore && manager.topPosition == adapter.itemCount) {
            page++
            stateLikeReceived.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.likeListsV2(params)
        }

    }


    override fun onGetDislikeResult(b: Boolean) {
        if (b) {
            if (likeCount > 0 && likeLeftCount.isVisible) {
                likeCount -= 1
                likeLeftCount.text = SpanUtils.with(likeLeftCount)
                    .append("有")
                    .append(" $likeCount ")
                    .setForegroundColor(resources.getColor(R.color.colorOrange))
                    .setBold()
                    .append("个人喜欢你")
                    .setForegroundColor(Color.parseColor("#FFC5C6C8"))
                    .create()
            }

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateLikeMeReceivedEvent(event: UpdateLikeMeReceivedEvent) {
        page = 1
        hasMore = true
        adapter.data.clear()
        stateLikeReceived.viewState = MultiStateView.VIEW_STATE_LOADING
        mPresenter.likeListsV2(params)
    }


    /*---------------------卡片参数和方法------------------------------*/
    private val manager by lazy { CardStackLayoutManager(this, this) }
    //用户适配器
    private val adapter: LikeMeUserAdapter by lazy { LikeMeUserAdapter() }

    private fun initialize() {
        //卡片排列方式
        manager.setStackFrom(StackFrom.Bottom)
        //最大可见数量
        manager.setVisibleCount(3)
        //两个卡片之间的间隔
        manager.setTranslationInterval(0f)
        //卡片滑出飞阈值
        manager.setSwipeThreshold(0.3f)
        //横向纵向的旋转角度
        manager.setMaxDegree(20F)
        //滑动的方向
        manager.setDirections(Direction.HORIZONTAL)
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(false)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())

        //撤回的动画设置
        val setting = RewindAnimationSetting.Builder()
            .setDirection(Direction.Top)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(DecelerateInterpolator())
            .build()
        manager.setRewindAnimationSetting(setting)

        //右滑飞出效果
        val setting1 = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Right)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        manager.setSwipeAnimationSetting(setting1)

        greetRv.layoutManager = manager
        greetRv.adapter = adapter
        adapter.bindToRecyclerView(greetRv)

        greetRv.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }

    override fun onCardDisappeared(view: View?, position: Int) {
        resetAnimation()
        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")
    }

    override fun onCardDragging(direction: Direction, ratio: Float) {
        Log.d("CardStackView", "onCardDragging: ($direction)")
        //向上超级喜欢(会员就超级喜欢 否则弹起收费窗)
        when (direction) {
            //左滑时加载动画
            Direction.Left -> {
                //重置右边、上边的距离
                animation_like.alpha = 0F
                val paramsLike = animation_like.layoutParams as ConstraintLayout.LayoutParams
                paramsLike.width = 0
                paramsLike.height = 0
                animation_like.layoutParams = paramsLike

                animation_dislike.alpha = ratio
                val params = animation_dislike.layoutParams as ConstraintLayout.LayoutParams
                params.width = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.height = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.leftMargin = ((ScreenUtils.getScreenWidth() / 2F * ratio) - params.width / 2F).toInt()
                animation_dislike.layoutParams = params

            }
            //右滑时加载动画
            Direction.Right -> {
                //重置左边、上边的距离
                val paramsLike = animation_dislike.layoutParams as ConstraintLayout.LayoutParams
                paramsLike.width = 0
                paramsLike.height = 0
                animation_dislike.layoutParams = paramsLike
                animation_dislike.alpha = 0F

                animation_like.alpha = ratio
                val params = animation_like.layoutParams as ConstraintLayout.LayoutParams
                params.width = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.height = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.rightMargin = ((ScreenUtils.getScreenWidth() / 2F * ratio) - params.width / 2F).toInt()
                animation_like.layoutParams = params
            }
        }
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    override fun onCardSwiped(direction: Direction) {
        resetAnimation()
        when (direction) {
            Direction.Left -> {//左滑不喜欢
                mPresenter.bindMemberHandle(adapter.data[manager.topPosition - 1].accid)
            }
            else -> {//右滑喜欢
                //保存剩余滑动次数
                if (UserManager.isUserVip() || UserManager.getLeftSlideCount() > 0) {
                    if (!UserManager.isUserVip() && UserManager.getLeftSlideCount() > 0) {
                        UserManager.saveLeftSlideCount(UserManager.getLeftSlideCount().minus(1))
                        EventBus.getDefault().post(UpdateSlideCountEvent())
                    }
                    mPresenter.addLike(
                        adapter.data[manager.topPosition - 1].accid,
                        adapter.data[manager.topPosition - 1].tag_id,
                        2
                    )
                    manager.setSwipeableMethod(SwipeableMethod.None)
                } else {
                    greetRv.postDelayed({ greetRv.rewind() }, 100)
                    greetRv.isEnabled = false
                }
            }
        }


    }

    private fun resetAnimation() {
        val params1 = animation_like.layoutParams
        params1.width = 0
        params1.height = 0
        animation_like.alpha = 0F
        animation_like.layoutParams = params1

        val params2 = animation_dislike.layoutParams
        params2.width = 0
        params2.height = 0
        animation_dislike.alpha = 0F
        animation_dislike.layoutParams = params2
    }

    override fun onCardCanceled() {
        resetAnimation()
        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")
    }

    override fun onCardAppeared(view: View?, position: Int) {
    }

    override fun onCardRewound() {

    }


    /*--------------------------消息代理------------------------*/

    private fun sendChatHiMessage(type: Int, matchBean: PositiveLikeBean) {
//        val matchBean = adapter.data[manager.topPosition - 1]
        Log.d("OkHttp", matchBean.accid ?: "")
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
        sendMessage(message, matchBean)
    }


    fun sendMessage(msg: IMMessage, matchBean: PositiveLikeBean): Boolean {
        NIMClient.getService(MsgService::class.java).sendMessage(msg, false)
            .setCallback(object : RequestCallback<Void?> {
                override fun onSuccess(param: Void?) {
                    if (msg.attachment is ChatHiAttachment && (msg.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_MATCH) { //匹配成功跳转到飞卡片
                        startActivity<MatchSucceedActivity>(
                            "avator" to matchBean.avatar,
                            "nickname" to matchBean.nickname,
                            "accid" to matchBean.accid
                        )
                    }

                }

                override fun onFailed(code: Int) {
                    greetRv.rewind()
                }

                override fun onException(exception: Throwable) {
                    greetRv.rewind()
                }
            })
        return true
    }


}
