package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
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
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
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
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.activity.MessageInfoActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.presenter.MatchDetailPresenter
import com.sdy.jitangapplication.presenter.view.MatchDetailView
import com.sdy.jitangapplication.ui.adapter.DetailThumbAdapter
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.adapter.MatchImgsPagerAdapter
import com.sdy.jitangapplication.ui.chat.MatchSucceedActivity
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.ui.dialog.MoreActionDialog
import com.sdy.jitangapplication.ui.dialog.RightSlideOutdDialog
import com.sdy.jitangapplication.ui.dialog.SayHiDialog
import com.sdy.jitangapplication.ui.fragment.MatchDetailInfomationFragment
import com.sdy.jitangapplication.ui.fragment.MatchDetailSquareFragment
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.ScaleTransitionPagerTitleView
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_match_detail.*
import kotlinx.android.synthetic.main.dialog_more_action.*
import kotlinx.android.synthetic.main.error_layout.view.*
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
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
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


    private val params by lazy {
        hashMapOf(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "tag_id" to UserManager.getGlobalLabelId(),
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

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = MatchDetailPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        //设置图片的宽度占满屏幕，宽高比9:16
        val layoutParams = detailPhotosVp.layoutParams
        layoutParams.width = ScreenUtils.getScreenWidth()
        layoutParams.height = (16 / 9.0F * layoutParams.width).toInt()
        detailPhotosVp.layoutParams = layoutParams

        //设置个人信息距离顶部的距离
        val paramsClUserInfo = clUserInfo.layoutParams as FrameLayout.LayoutParams
        paramsClUserInfo.topMargin = layoutParams.height - SizeUtils.dp2px(24f)
        clUserInfo.layoutParams = paramsClUserInfo

        vpUserDetail.setScrollable(true)
        detailPhotosVp.setScrollable(false)
        moreBtn.setOnClickListener(this)
        moreBtn1.setOnClickListener(this)
        detailUserLikeBtn.setOnClickListener(this)
        detailUserChatBtn.setOnClickListener(this)
        cancelBlack.setOnClickListener(this)
        backBtn.setOnClickListener(this)
        backBtn1.setOnClickListener(this)
        btnBack2.setOnClickListener(this)
        //用户的广场预览界面
        detailThumbRv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)


        userAppbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { p0, verticalOffset ->
            //            if (Math.abs(verticalOffset) >= clUserInfo.marginTop) {
//                if (ScreenUtils.isFullScreen(this))
//                    ScreenUtils.setNonFullScreen(this)
//            } else {
//                if (!ScreenUtils.isFullScreen(this))
//                    ScreenUtils.setFullScreen(this)
//            }
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


    //fragment栈管理
    private val mStack = Stack<Fragment>()
    private val titles = arrayOf("重叠兴趣 0", "个人信息")

    private fun initIndicator() {
        tabUserDettail.setBackgroundColor(Color.WHITE)
        val commonNavigator = CommonNavigator(this)
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return mStack.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val simplePagerTitleView = ScaleTransitionPagerTitleView(context)
                simplePagerTitleView.text = titles[index]
                simplePagerTitleView.minScale = 0.85F
                simplePagerTitleView.textSize = 20F
                simplePagerTitleView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                simplePagerTitleView.normalColor = resources.getColor(R.color.colorBlack53)
                simplePagerTitleView.selectedColor = resources.getColor(R.color.colorBlack)
                simplePagerTitleView.onClick {
                    vpUserDetail.currentItem = index
                }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineHeight = UIUtil.dip2px(context, 4.0).toFloat()
                indicator.lineWidth = UIUtil.dip2px(context, 32.0).toFloat()
                indicator.roundRadius = UIUtil.dip2px(context, 2.0).toFloat()
                indicator.startInterpolator = AccelerateInterpolator()
                indicator.endInterpolator = DecelerateInterpolator(1.0f)
                indicator.setColors(resources.getColor(R.color.colorOrange))
                return indicator
            }
        }
        tabUserDettail.navigator = commonNavigator
        ViewPagerHelper.bind(tabUserDettail, vpUserDetail)
    }

    /*
      初始化Fragment栈管理
   */
    private fun initFragment() {
        mStack.add(MatchDetailSquareFragment(matchBean!!, targetAccid))
        mStack.add(MatchDetailInfomationFragment(matchBean!!))
        vpUserDetail.adapter = MainPagerAdapter(supportFragmentManager, mStack, titles)
        initIndicator()
        vpUserDetail.currentItem = 0
    }


    private fun initData() {
        initFragment()
//        EventBus.getDefault().post(UpdateSquareEvent())

        detailUserName.text = matchBean!!.nickname
        titleUsername.text = matchBean!!.nickname
        detailUserInfo.text =
            "${matchBean!!.age} . ${if (matchBean!!.gender == 1) "男" else "女"} . ${matchBean!!.constellation} . ${matchBean!!.distance}"
        detailUserJob.text = "${matchBean!!.jobname}"
        detailUserSign.apply {
            text = "${matchBean!!.sign}"
            isVisible = !(matchBean!!.sign.isNullOrBlank())
        }
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

        //更新打招呼次数和状态
        updateLightCount(matchBean!!.lightningcnt ?: 0, matchBean!!.countdown)

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
                        - SizeUtils.dp2px(15F) * 2
                        - (SizeUtils.dp2px(6F) * (photos.size - 1))) * 1F / photos.size).toInt()
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
//            stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
            matchBean = matchUserDetailBean
            updateBlockStatus()

            //本地对标签进行过滤筛选
            val labels = UserManager.getSpLabels()
            var same = 0
            for (label in labels) {
                for (tag in matchBean!!.tags ?: mutableListOf()) {
                    if (label.id == tag.id && tag.id != Constants.RECOMMEND_TAG_ID) {
                        tag.sameLabel = true
                        same++
                    }
                }
            }
            titles[0] = "重叠兴趣 $same"
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
                NIMClient.getService(MsgService::class.java)
                    .deleteRecentContact2(matchBean!!.accid ?: "", SessionTypeEnum.P2P)
                // 删除与某个聊天对象的全部消息记录
                NIMClient.getService(MsgService::class.java)
                    .clearChattingHistory(matchBean!!.accid ?: "", SessionTypeEnum.P2P)

                EventBus.getDefault().post(UpdateContactBookEvent())
                if (AppUtils.isAppForeground() && ActivityUtils.isActivityAlive(MessageInfoActivity::class.java.newInstance()))
                    ActivityUtils.finishActivity(MessageInfoActivity::class.java)
                if (AppUtils.isAppForeground() && ActivityUtils.isActivityAlive(ChatActivity::class.java.newInstance()))
                    ActivityUtils.finishActivity(ChatActivity::class.java)
                EventBus.getDefault().postSticky(UpdateHiEvent())

                matchBean!!.isfriend = 0
            } else if (result == "拉黑成功!") {
                NIMClient.getService(FriendService::class.java).addToBlackList(matchBean!!.accid)
                NIMClient.getService(MsgService::class.java)
                    .deleteRecentContact2(matchBean!!.accid, SessionTypeEnum.P2P)
                NIMClient.getService(MsgService::class.java).clearServerHistory(matchBean!!.accid, SessionTypeEnum.P2P)
                matchBean!!.isblock = 2
                updateBlockStatus()
//                EventBus.getDefault().post(UpdateLabelEvent(LabelBean(id = UserManager.getGlobalLabelId())))

            }
            CommonFunction.toast("$result")
        }
    }


    override fun onGetLikeResult(success: Boolean, statusBean: BaseResp<StatusBean?>?) {
        if (statusBean != null)
            if (statusBean.code == 200) {
                if (statusBean.data?.residue == 0) {
                    ChargeVipDialog(ChargeVipDialog.INFINITE_SLIDE, this).show()
                } else {
                    EventBus.getDefault().post(RefreshEvent(true))
                    if (statusBean.data?.status == 1) {  //喜欢成功
                        matchBean!!.isliked = 1
                        updateLightCount(-1, -1)
                    } else if (statusBean.data?.status == 2) {//匹配成功
                        //如果是来自喜欢我的界面， 就刷新
                        if (intent.getIntExtra("parPos", -1) != -1) {
                            EventBus.getDefault().post(
                                UpdateLikemeOnePosEvent(
                                    intent.getIntExtra("parPos", -1), intent.getIntExtra("childPos", -1)
                                )
                            )
                        }
                        //喜欢过
                        matchBean!!.isfriend = 1
                        updateLightCount(-1, -1)

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
            } else if (statusBean.code == 201) {
                if (matchBean!!.my_percent_complete <= matchBean!!.normal_percent_complete)
                    RightSlideOutdDialog(this, matchBean!!.my_like_times, matchBean!!.total_like_times).show()
                else
                    ChargeVipDialog(ChargeVipDialog.INFINITE_SLIDE, this).show()
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
            R.id.detailUserLikeBtn -> {//感兴趣
                mPresenter.likeUser(params)
            }
            //todo  这里要判断是不是VIP用户 如果是VIP 直接进入聊天界面
            //1.首先判断是否有次数，
            // 若有 就打招呼
            // 若无 就弹充值
            R.id.detailUserChatBtn -> {
                if (matchBean != null)
                    CommonFunction.commonGreet(
                        this,
                        matchBean!!.isfriend == 1,
                        matchBean!!.greet_switch,
                        matchBean!!.greet_state,
                        matchBean!!.accid,
                        matchBean!!.nickname ?: "",
                        matchBean!!.isgreeted,
                        detailUserChatBtn
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
    private fun updateLightCount(lightningcnt: Int, countdown: Int) {
        if (lightningcnt != -1) {
            UserManager.saveLightingCount(lightningcnt)
            EventBus.getDefault().postSticky(UpdateHiCountEvent())
        }
        if (countdown != -1)
            UserManager.saveCountDownTime(countdown)

        detailUserLeftChatCount.text = "${UserManager.getLightingCount()}"
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

        //判断是否为好友 是：显示聊天
        //              否: 判断是否开启招呼,是否喜欢过
        if (matchBean!!.isfriend == 1) {//是好友就显示聊天
            detailUserChatBtn.setBackgroundResource(R.drawable.shape_rectangle_solid_orange)
            detailUserChatIv.setImageResource(R.drawable.icon_chat_white)
            detailUserLeftChatCount.isVisible = false
            detailUserChatTv.text = "聊天"
        } else {
            detailUserLikeBtn.isVisible = matchBean!!.isliked != 1//喜欢过就不显示“感兴趣”
            if (!matchBean!!.greet_switch) {//招呼未开启不显示打招呼
                detailUserChatBtn.isVisible = false
            } else {//招呼开启,   招呼有效 与 招呼无效
                if (matchBean!!.isgreeted) {
                    detailUserChatBtn.setBackgroundResource(R.drawable.gradient_match_detail_red_bg)
                    detailUserChatIv.setImageResource(R.drawable.icon_flash_white)
                    detailUserChatIv.isVisible = false
                    detailUserLeftChatCount.isVisible = false
                    detailUserChatTv.text = "继续聊天"
                } else {
                    detailUserChatBtn.setBackgroundResource(R.drawable.gradient_match_detail_red_bg)
                    detailUserChatIv.setImageResource(R.drawable.icon_flash_white)
                    detailUserLeftChatCount.isVisible = true
                    detailUserChatTv.text = "打招呼"
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
                    if ((msg.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_HI) {
                        SayHiDialog(matchBean!!.accid, matchBean!!.nickname ?: "", this@MatchDetailActivity).show()
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
