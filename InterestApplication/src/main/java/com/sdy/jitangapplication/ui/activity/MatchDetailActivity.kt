package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.core.widget.NestedScrollView
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.flexbox.*
import com.kennyc.view.MultiStateView
import com.kotlin.base.common.AppManager
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nim.uikit.business.session.module.Container
import com.netease.nim.uikit.business.session.module.ModuleProxy
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
import com.sdy.jitangapplication.event.BlockDataEvent
import com.sdy.jitangapplication.event.ListDataEvent
import com.sdy.jitangapplication.event.NotifyEvent
import com.sdy.jitangapplication.event.UpdateHiCountEvent
import com.sdy.jitangapplication.model.GreetBean
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.presenter.MatchDetailPresenter
import com.sdy.jitangapplication.presenter.view.MatchDetailView
import com.sdy.jitangapplication.ui.adapter.DetailThumbAdapter
import com.sdy.jitangapplication.ui.adapter.MatchDetailLabelAdapter
import com.sdy.jitangapplication.ui.adapter.MatchImgsPagerAdapter
import com.sdy.jitangapplication.ui.chat.MatchSucceedActivity
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.ui.dialog.CountDownChatHiDialog
import com.sdy.jitangapplication.ui.dialog.MoreActionDialog
import com.sdy.jitangapplication.ui.fragment.BlockSquareFragment
import com.sdy.jitangapplication.ui.fragment.ListSquareFragment
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.BounceScrollView
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_match_detail1.*
import kotlinx.android.synthetic.main.dialog_more_action.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import java.util.*

/**
 * 匹配详情页
 */
class MatchDetailActivity : BaseMvpActivity<MatchDetailPresenter>(), MatchDetailView,
    View.OnClickListener, ModuleProxy {


    private val targetAccid by lazy { intent.getStringExtra("target_accid") }
    private var matchBean: MatchBean? = null
    private val thumbAdapter by lazy { DetailThumbAdapter(this) }

    var photos: MutableList<String> = mutableListOf()
    private val photosAdapter by lazy { MatchImgsPagerAdapter(this, photos) }

    private val labelsAdapter by lazy { MatchDetailLabelAdapter(this) }

    private val params by lazy {
        hashMapOf(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "target_accid" to targetAccid,
            "_sign" to "",
            "_timestamp" to System.currentTimeMillis()
        )
    }
    private val targetUserPramas by lazy {
        hashMapOf(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "target_accid" to targetAccid,
            "_sign" to "",
            "_timestamp" to System.currentTimeMillis()
        )
    }

    companion object {
        @JvmStatic
        fun start(context: Context, fromAccount: String) {
            context.startActivity<MatchDetailActivity>("target_accid" to fromAccount)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_detail1)

        initView()

        mPresenter.getUserDetailInfo(params)
    }


    private fun initView() {
        ScreenUtils.setFullScreen(this)

        mPresenter = MatchDetailPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        detailScrollView.setOnBounceListener {
            finish()
        }
        //向下拉
        detailScrollView.bounceType = BounceScrollView.ENABLED_TOP

        //刚度 默认1200 值越大回弹的速度越快
        springAnim.spring.stiffness = 100.0f
        //阻尼 默认0.5 值越小，回弹之后来回的次数越多
        springAnim.spring.dampingRatio = 0.80f
        detailScrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY < 0) {
                springAnim.start()
            } else if (scrollY < -100) {
                springAnim.cancel()
                finish()
            } else {
                springAnim.cancel()
            }
        }


        //设置图片的宽度占满屏幕，宽高比9:16
        val layoutParams = detailPhotosVp.layoutParams
        layoutParams.width = ScreenUtils.getScreenWidth()
        layoutParams.height = (16 / 9.0F * layoutParams.width).toInt()
        detailPhotosVp.layoutParams = layoutParams



        moreBtn.setOnClickListener(this)
        detailUserLikeBtn.setOnClickListener(this)
        detailUserChatBtn.setOnClickListener(this)
        backBtn.setOnClickListener(this)


        //用户的广场预览界面
        detailThumbRv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        //用户标签
        val manager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        manager.justifyContent = JustifyContent.CENTER
        detailLabelRv.layoutManager = manager

        initFragment()

        detailSquareSwitchRg.setOnCheckedChangeListener { radioGroup, checkedId ->
            if (checkedId == R.id.rbList) {
                changeFragment(1)
                if (!loadList) {
                    EventBus.getDefault().post(ListDataEvent(targetAccid, true))
                }
                loadList = true
                currIndex = 1
            } else if (checkedId == R.id.rbBlock) {
                changeFragment(0)
                if (!loadBlock) {
                    EventBus.getDefault().post(BlockDataEvent(targetAccid, true))
                }
                loadBlock = true
                currIndex = 0
            }
        }

        //重试
        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getUserDetailInfo(params)
            if (currIndex == 0) {
                EventBus.getDefault().post(BlockDataEvent(targetAccid, true))
            } else {
                EventBus.getDefault().post(ListDataEvent(targetAccid, true))
            }
        }
    }

    private fun initData() {
        detailUserName.text = matchBean!!.nickname
        detailUserInfo.text =
            "${matchBean!!.age} / ${if (matchBean!!.gender == 1) "男" else "女"} / ${matchBean!!.constellation} / ${matchBean!!.distance}"
        detailUserJob.text = "${matchBean!!.jobname}"
        detailUserSign.text = "${matchBean!!.sign}"
        updateLightCount(matchBean!!.lightningcnt ?: 0, matchBean!!.countdown)
        detailUserJob.visibility = if (matchBean!!.jobname.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
        detailUserVip.visibility = if (matchBean!!.isvip == 1) {
            View.VISIBLE
        } else {
            View.GONE
        }
        detailUserVerify.isVisible = matchBean!!.isfaced == 1


        //已感兴趣或者是好友不做操作
        if (matchBean!!.isliked == 1 || matchBean!!.isfriend == 1) {
            detailUserLikeBtn.visibility = View.GONE
            detailUserLikeBtn.isEnabled = false
            detailUserLikeBtn.setBackgroundResource(R.drawable.shape_rectangle_solid_gray)
        } else {
            detailUserLikeBtn.visibility = View.VISIBLE
            detailUserLikeBtn.isEnabled = true
            detailUserLikeBtn.setBackgroundResource(R.drawable.shape_rectangle_solid_blue)
        }


        //喜欢过
        if (matchBean!!.isfriend == 1) {//是好友就显示聊天
            detailUserChatBtn.setBackgroundResource(R.drawable.shape_rectangle_solid_orange)
            detailUserChatIv.setImageResource(R.drawable.icon_chat_white)
            detailUserLeftChatCount.isVisible = false
            detailUserChatTv.text = "聊天"
        } else {//不是好友就显示打招呼
            detailUserChatBtn.setBackgroundResource(R.drawable.gradient_match_detail_red_bg)
            detailUserChatIv.setImageResource(R.drawable.icon_flash_white)
            detailUserLeftChatCount.isVisible = true
            detailUserChatTv.text = "打招呼"

        }

//        用户动态封面图片
        if (matchBean!!.square == null || matchBean!!.square!!.size == 0) {
            detailThumbRv.visibility = View.GONE
        } else {
            detailThumbRv.adapter = thumbAdapter
            thumbAdapter.setData(matchBean!!.square ?: mutableListOf())
        }

        //用户照片
        detailPhotosVp.adapter = photosAdapter

        if (matchBean!!.photos == null || matchBean!!.photos!!.isEmpty())
            photos.add(matchBean!!.avatar ?: "")
        photos.addAll(matchBean!!.photos ?: mutableListOf())
        photosAdapter.notifyDataSetChanged()
        setViewpagerAndIndicator()

        //用户标签
        detailLabelRv.adapter = labelsAdapter
        labelsAdapter.setData(matchBean!!.tags ?: mutableListOf())

    }

    /**
     * 设置竖直滑动的vp2以及其滑动的indicator
     */
    private fun setViewpagerAndIndicator() {
        detailPhotosVp.setScrollable(false)
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
                val width = ((ScreenUtils.getScreenWidth()
                        - SizeUtils.dp2px(15F) * 4
                        - (SizeUtils.dp2px(6F) * (photos.size - 1))) * 1F / photos.size).toInt()
                val height = SizeUtils.dp2px(5F)

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

    //fragment栈管理
    private val mStack = Stack<Fragment>()
    //九宫格
    private val blockFragment by lazy { BlockSquareFragment() }
    //列表
    private val listFragment by lazy { ListSquareFragment() }
    //标识 来确认是否已经加载过数据
    private var loadBlock = false
    private var loadList = false
    private var currIndex = 1

    /**
     * 初始化fragments
     */
    private fun initFragment() {
        val manager = supportFragmentManager.beginTransaction()
        manager.add(R.id.detail_content_fragment, blockFragment) //九宫格照片模式
        manager.add(R.id.detail_content_fragment, listFragment) //列表
        manager.commit()
        mStack.add(blockFragment)
        mStack.add(listFragment)
    }

    /**
     * 点击切换fragment
     */
    private fun changeFragment(position: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        for (fragment in mStack) {
            transaction.hide(fragment)
        }
        transaction.show(mStack[position])
        transaction.commit()
    }

    /**
     * 获取用户详情结果
     */
    override fun onGetMatchDetailResult(success: Boolean, matchUserDetailBean: MatchBean?) {
        if (success) {
            stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
            matchBean = matchUserDetailBean
            //本地对标签进行过滤筛选
            val labels = UserManager.getSpLabels()
            for (label in labels) {
                for (tag in matchBean!!.tags ?: mutableListOf()) {
                    if (label.id == tag.id && tag.id != Constants.RECOMMEND_TAG_ID) {
                        tag.sameLabel = true
                    }
                }
            }
            initData()

            //请求成功了请求列表广场
            detailSquareSwitchRg.check(R.id.rbList)
        } else {
            stateview.viewState = MultiStateView.VIEW_STATE_ERROR
            stateview.errorMsg.text = CommonFunction.getErrorMsg(this)
        }
    }

    override fun onGetUserActionResult(success: Boolean, result: String?) {
        if (success) {
            if (result == "解除成功!") {
//                matchBean!!.isfriend = 0
                //更新数据
//                initData()
            } else if (result == "拉黑成功!") {
                NIMClient.getService(MsgService::class.java)
                    .deleteRecentContact2(matchBean!!.accid, SessionTypeEnum.P2P)
                AppManager.instance.finishAllActivity()
                startActivity<MainActivity>()
//                EventBus.getDefault().post(UpdateLabelEvent(LabelBean(id = UserManager.getGlobalLabelId())))

            }
            ToastUtils.showShort(result)
        }
    }


    override fun onGetLikeResult(success: Boolean, statusBean: BaseResp<StatusBean?>?) {
        if (statusBean != null) {
            if (statusBean.data?.residue == 0) {
                ChargeVipDialog(this).show()
            } else {
                if (statusBean.data?.status == 1) {  //喜欢成功
                    detailUserLikeBtn.isEnabled = false
                    detailUserLikeBtn.visibility = View.GONE
                    detailUserLikeBtn.setBackgroundResource(R.drawable.shape_rectangle_solid_gray)
                } else if (statusBean.data?.status == 2) {//匹配成功
                    //喜欢过
                    matchBean!!.isfriend = 1
                    if (matchBean!!.isfriend == 1) {//是好友就显示聊天
                        detailUserChatBtn.setBackgroundResource(R.drawable.shape_rectangle_solid_orange)
                        detailUserChatIv.setImageResource(R.drawable.icon_chat_white)
                        detailUserLeftChatCount.isVisible = false
                        detailUserChatTv.text = "聊天"
                        detailUserLikeBtn.visibility = View.GONE
                        detailUserLikeBtn.isEnabled = false
                        detailUserLikeBtn.setBackgroundResource(R.drawable.shape_rectangle_solid_gray)
                    }
                    sendChatHiMessage(ChatHiAttachment.CHATHI_MATCH)
                }
            }
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.moreBtn -> {//更多
                showMoreActionDialog()
            }
            R.id.detailUserLikeBtn -> {//感兴趣
                mPresenter.likeUser(params)
            }
            //todo  这里要判断是不是VIP用户 如果是VIP 直接进入聊天界面
            //1.首先判断是否有次数，
            // 若有 就打招呼
            // 若无 就弹充值
            R.id.detailUserChatBtn -> {//打个招呼
                if (matchBean != null)
                    if (matchBean!!.isfriend == 1) {
                        ChatActivity.start(this, matchBean?.accid ?: "")
                    } else
                        mPresenter.greetState(UserManager.getToken(), UserManager.getAccid(), matchBean?.accid ?: "")
            }

            R.id.backBtn -> {
                finish()
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
            mPresenter.reportUser(targetUserPramas)
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
     *  点击聊天
     *
     *  判断是否是好友，如果是好友，直接聊天，
     *                 若不是好友，判断剩余次数
     *                                有次数 直接打招呼
     *                                无次数 其他操作--如:请求充值会员
     */
    override fun onGreetStateResult(data: GreetBean?) {
        if (data != null) {
            updateLightCount(data.lightningcnt, data.countdown)
            if (data.isfriend || data.isgreet) {
                ChatActivity.start(this, matchBean?.accid ?: "")
            } else {
                if (data.lightningcnt > 0) {
                    mPresenter.greet(
                        UserManager.getToken(),
                        UserManager.getAccid(),
                        (matchBean?.accid ?: ""),
                        UserManager.getGlobalLabelId()
                    )
                } else {
                    if (UserManager.isUserVip()) {
                        CountDownChatHiDialog(this).show()
                    } else {
                        ChargeVipDialog(this).show()
                    }
                }
            }
        } else {
            ToastUtils.showShort("请求失败，请重试")
        }
    }

    /**
     * 更新本地的招呼次数
     */
    private fun updateLightCount(lightningcnt: Int, countdown: Int) {
        UserManager.saveLightingCount(lightningcnt)
        if (countdown != -1)
            UserManager.saveCountDownTime(countdown)

        EventBus.getDefault().postSticky(UpdateHiCountEvent())
        detailUserLeftChatCount.text = "${UserManager.getLightingCount()}"
    }

    /**
     * 打招呼结果（先请求服务器）
     */
    override fun onGreetSResult(success: Boolean) {
        if (success) {
            updateLightCount(UserManager.getLightingCount() - 1, -1)
            sendChatHiMessage(ChatHiAttachment.CHATHI_HI)
        }
    }


    private val springAnim: SpringAnimation by lazy {
        SpringAnimation(
            detailScrollView,
            SpringAnimation.TRANSLATION_Y,
            0.0f
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        detailScrollView.destroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == SquarePlayDetailActivity.REQUEST_CODE) {
                EventBus.getDefault().post(NotifyEvent(data!!.getIntExtra("position", -1)))
            }
    }


    /*--------------------------消息代理------------------------*/
    private fun sendChatHiMessage(type: Int) {
        Log.d("OkHttp", matchBean?.accid ?: "")
        val container = Container(this, matchBean?.accid, SessionTypeEnum.P2P, this, true)
        val chatHiAttachment = ChatHiAttachment(UserManager.getGlobalLabelName(), type)
        val message = MessageBuilder.createCustomMessage(
            matchBean?.accid,
            SessionTypeEnum.P2P,
            "",
            chatHiAttachment,
            CustomMessageConfig()
        )
        container.proxy.sendMessage(message)
    }

    override fun sendMessage(msg: IMMessage): Boolean {
        NIMClient.getService(MsgService::class.java).sendMessage(msg, false).setCallback(object :
            RequestCallback<Void?> {
            override fun onSuccess(param: Void?) {
                if (msg.attachment is ChatHiAttachment) {
                    if ((msg.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_HI)
                        ChatActivity.start(this@MatchDetailActivity, matchBean?.accid ?: "")
                    else {
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
