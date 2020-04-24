package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.GetNewMsgEvent
import com.sdy.jitangapplication.event.UpdateHiEvent
import com.sdy.jitangapplication.model.GreetedListBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.nim.attachment.SendCustomTipAttachment
import com.sdy.jitangapplication.presenter.GreetReceivedPresenter
import com.sdy.jitangapplication.presenter.view.GreetReceivedView
import com.sdy.jitangapplication.ui.adapter.GreetUserAdapter
import com.sdy.jitangapplication.ui.dialog.GuideGreetDialog
import com.sdy.jitangapplication.utils.UserManager
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.activity_greet_received.*
import kotlinx.android.synthetic.main.empty_friend_layout.view.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivityForResult

/**
 * 收到的招呼列表
 */
class GreetReceivedActivity : BaseMvpActivity<GreetReceivedPresenter>(), GreetReceivedView,
    CardStackListener {

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
        setContentView(R.layout.activity_greet_received)
        initView()
        registerObservers(true)
        mPresenter.getRecentContacts()
    }

    override fun onDestroy() {
        super.onDestroy()
        registerObservers(false)
    }


    private fun initView() {
        BarUtils.setStatusBarLightMode(this, false)

        mPresenter = GreetReceivedPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        btnBack.setImageResource(R.drawable.icon_back_white)
        btnBack.onClick {
            finish()
        }
        hotT1.setTextColor(Color.WHITE)
        hotT1.text = "招呼列表"
        rightBtn.isVisible = true
        rightBtn.setTextColor(Color.WHITE)
        rightBtn.text = "全部招呼"
        rightBtn.setOnClickListener {
            startActivityForResult<MessageHiPastActivity>(100)
        }
        divider.setBackgroundColor(Color.TRANSPARENT)
        llTitle.setBackgroundColor(Color.TRANSPARENT)



        stateGreet.emptyImg.setImageResource(R.drawable.icon_hi_past_empty)
        stateGreet.emptyFriendTitle.text = "这里什么都没有"
        stateGreet.emptyFriendTitle.setTextColor(Color.WHITE)
        stateGreet.emptyFriendTip.text = "看到心仪的TA记得主动打个招呼\n丰富资料还能为你赢得更多招呼"
        stateGreet.emptyFriendTip.setTextColor(Color.parseColor("#FFB5B7B9"))
        stateGreet.retryBtn.onClick {
            stateGreet.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.greatLists(params)
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.v1 -> {
                    MatchDetailActivity.start(this, adapter.data[position].accid)
                }
            }
        }


        //初始化卡片布局
        initialize()

    }

    private var hasMore = true
    override fun onGreatListResult(t: BaseResp<MutableList<GreetedListBean>?>) {
        if (t != null && t.code == 200) {
            if (page == 1) {
                EventBus.getDefault().postSticky(UpdateHiEvent())
                EventBus.getDefault().post(GetNewMsgEvent())
            }

            if (t.data.isNullOrEmpty() || (page == 1 && (t.data
                    ?: mutableListOf()).size < Constants.PAGESIZE)
            ) {
                hasMore = false
            }
            stateGreet.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (page == 1 && t.data.isNullOrEmpty()) {
                stateGreet.viewState = MultiStateView.VIEW_STATE_EMPTY
            } else {
                if (!UserManager.isShowGuideGreet()) {
                    GuideGreetDialog(this).show()
                }
            }

            for (data in t.data ?: mutableListOf()) {
                for (contact in recentContacts) {
                    if (data.accid == contact.fromAccount) {
                        if (data.send_msg.isNullOrEmpty()) {
                            if (!contact.content.isNullOrEmpty()) {
                                when {
                                    contact.attachment is ChatHiAttachment -> data.send_msg =
                                        "对方向你打了个招呼"
                                    contact.attachment is SendCustomTipAttachment ->
                                        ((contact.attachment as SendCustomTipAttachment).content)
                                    else -> data.send_msg = contact.content
                                }
                            } else {
                                data.send_msg = "对方向你打了个招呼"
                            }
                        }
                        NIMClient.getService(MsgService::class.java)
                            .clearUnreadCount(data.accid, SessionTypeEnum.P2P)
                        break
                    }
                }
            }

            adapter.addData(t.data ?: mutableListOf())

        } else {
            stateGreet.viewState = MultiStateView.VIEW_STATE_ERROR
        }

    }


    //1 右滑招呼 2左滑失效
    override fun onLikeOrGreetStateResult(result: Boolean, type: Int) {
        if (!result) {
            greetRv.rewind()
        } else {
            EventBus.getDefault().postSticky(UpdateHiEvent())
            if (type == 1) {
                ChatActivity.start(this, adapter.data[manager.topPosition - 1].accid)
            }
        }
    }


    private var recentContacts = mutableListOf<RecentContact>()
    override fun onGetRecentContactResults(result: MutableList<RecentContact>) {
        recentContacts.addAll(result)
        mPresenter.greatLists(params)
    }


    fun updatGreetReceivedEvent() {
        page = 1
        hasMore = true
        adapter.data.clear()
        stateGreet.viewState = MultiStateView.VIEW_STATE_LOADING
        mPresenter.greatLists(params)
    }


    /*---------------------卡片参数和方法------------------------------*/
    private val manager by lazy { CardStackLayoutManager(this, this) }
    //用户适配器
    private val adapter: GreetUserAdapter by lazy { GreetUserAdapter() }

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
                params.leftMargin =
                    ((ScreenUtils.getScreenWidth() / 2F * ratio) - params.width / 2F).toInt()
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
                params.rightMargin =
                    ((ScreenUtils.getScreenWidth() / 2F * ratio) - params.width / 2F).toInt()
                animation_like.layoutParams = params
            }
        }
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    override fun onCardSwiped(direction: Direction) {
        resetAnimation()
        //清空此人的未读消息数量
        NIMClient.getService(MsgService::class.java)
            .clearUnreadCount(adapter.data[manager.topPosition - 1].accid, SessionTypeEnum.P2P)
        //1 右滑接受 2左滑失效
        mPresenter.likeOrGreetState(
            adapter.data[manager.topPosition - 1].greet_id, if (direction == Direction.Left) {
                2
            } else {
                1
            }
        )
        if (hasMore && manager.topPosition == adapter.itemCount - 5) {
            page++
            mPresenter.greatLists(params)
        } else if (!hasMore && manager.topPosition == adapter.itemCount) {
            stateGreet.viewState = MultiStateView.VIEW_STATE_EMPTY
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


    /**
     * ********************** 收消息，处理状态变化 ************************
     */

    private fun registerObservers(register: Boolean) {
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeReceiveMessage(messageReceiverObserver, register)
    }


    //监听在线消息的获取
    private val messageReceiverObserver =
        Observer<List<IMMessage>> { imMessages ->
            if (imMessages != null) {
                for (contact in adapter.data) {
                    for (imMessage in imMessages) {
                        if (contact.accid == imMessage.fromAccount) {
                            if (imMessage.attachment is SendCustomTipAttachment)
                                contact.send_msg =
                                    ((imMessage.attachment as SendCustomTipAttachment).content)
                            else
                                contact.send_msg = imMessage.content ?: ""
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                updatGreetReceivedEvent()
            }
        }
    }
}
