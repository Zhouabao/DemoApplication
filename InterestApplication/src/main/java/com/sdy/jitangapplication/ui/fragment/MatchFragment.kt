package com.sdy.jitangapplication.ui.fragment


import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.viewpager.widget.ViewPager
import com.airbnb.lottie.LottieAnimationView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.MatchListBean
import com.sdy.jitangapplication.model.Newtag
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.presenter.MatchPresenter
import com.sdy.jitangapplication.presenter.view.MatchView
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.adapter.MatchUserAdapter
import com.sdy.jitangapplication.ui.dialog.*
import com.sdy.jitangapplication.utils.UserManager
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.empty_friend_layout.view.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.fragment_match.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 匹配页面(新版)
 */
class MatchFragment : BaseMvpLazyLoadFragment<MatchPresenter>(), MatchView, View.OnClickListener, CardStackListener {


    override fun loadData() {
        initView()
    }


    private var hasMore = false

    //用户适配器
    private val matchUserAdapter: MatchUserAdapter by lazy { MatchUserAdapter(mutableListOf()) }
    private var is_human: Boolean = false

    companion object {
        private const val LOADING = 0
        private const val CONTENT = 1
        private const val ERROR = 2
        private const val EMPTY = 3
        private const val PAGESIZE = 20

    }


    //请求广场的参数 TODO要更新tagid
    private val matchParams by lazy {
        hashMapOf(
            "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
            "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
            "_timestamp" to System.currentTimeMillis(),
            "lng" to UserManager.getlongtitude().toFloat(),
            "lat" to UserManager.getlatitude().toFloat(),
            "city_code" to UserManager.getCityCode(),
            "type" to 1
        )

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    //    private val matchUserAdapter by lazy { CardAdapter() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_match, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    private fun updateLocation() {
        //加入本地的筛选对话框的筛选条件
        if (matchParams["audit_only"] != null)
            matchParams.remove("audit_only")
        if (matchParams["local_only"] != null)
            matchParams.remove("local_only")
        val params = UserManager.getFilterConditions()
        params.forEach {
            matchParams[it.key] = it.value
        }
        if (matchParams["lng"].toString().toFloat() == 0.0F) {
            matchParams["lat"] = UserManager.getlongtitude().toFloat()
            matchParams["lng"] = UserManager.getlatitude().toFloat()
            matchParams["city_code"] = UserManager.getCityCode()
        }
    }

    //    CompleteLabelDialog
    private val manager by lazy { CardStackLayoutManager(activity!!, this) }

    private fun initView() {
        mPresenter = MatchPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        retryBtn.setOnClickListener(this)
        changeAvatorCloseBtn.setOnClickListener(this)
        changeAvatorBtn.setOnClickListener(this)

        initialize()

        updateLocation()

        mPresenter.getMatchList(matchParams)

        matchUserAdapter.setOnItemChildClickListener { _, view, position ->
            val item = matchUserAdapter.data[manager.topPosition]
            val itemView = manager.topView
            when (view.id) {
                R.id.v1 -> {
                    (itemView.findViewById<ConstraintLayout>(R.id.v1)).isEnabled = false
                    if ((matchUserAdapter.data[manager.topPosition].accid ?: "") != UserManager.getAccid())
                        MatchDetailActivity.start(activity!!, matchUserAdapter.data[manager.topPosition].accid)
                    (itemView.findViewById<ConstraintLayout>(R.id.v1)).isEnabled = true
                }
                R.id.btnHiLottieView,
                R.id.btnHi -> {
                    val setting = SwipeAnimationSetting.Builder()
                        .setDirection(Direction.Right)
                        .setDuration(Duration.Normal.duration)
                        .setInterpolator(AccelerateInterpolator())
                        .build()
                    manager.setSwipeAnimationSetting(setting)
                    card_stack_view.swipe()
                }
                R.id.nextImgBtn -> {
                    if (itemView != null) {
                        val vpPhotos = itemView.findViewById<ViewPager>(R.id.vpPhotos)
                        if (vpPhotos.currentItem < (item.photos ?: mutableListOf<MatchBean>()).size - 1) {
                            val index = vpPhotos.currentItem
                            vpPhotos.setCurrentItem(index + 1, true)
                        } else {
//                            EventBus.getDefault().post(ShakeEvent(true))
                            YoYo.with(Techniques.Shake)
                                .duration(300)
                                .repeat(0)
                                .playOn(itemView)
                        }

                    }
                }
                R.id.lastImgBtn -> {
                    val itemView = manager.topView
                    if (itemView != null) {
                        val vpPhotos = itemView.findViewById<ViewPager>(R.id.vpPhotos)
                        if (vpPhotos.currentItem > 0) {
                            val index = vpPhotos.currentItem
                            vpPhotos.setCurrentItem(index - 1, true)
                        } else {
                            YoYo.with(Techniques.Shake)
                                .duration(300)
                                .repeat(0)
                                .playOn(itemView)
//                            EventBus.getDefault().post(ShakeEvent(true))
                        }
                    }
                }
            }
        }

    }


    val params by lazy {
        hashMapOf<String, Any>(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "target_accid" to "",
            "type" to 1
        )
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.retryBtn -> {
                setViewState(LOADING)
                updateLocation()
                mPresenter.getMatchList(matchParams)
            }
            R.id.filterBtn -> {
                FilterUserDialog(activity!!).show()
            }
            R.id.changeAvatorCloseBtn -> {
                lieAvatorLl.isVisible = false
            }
            R.id.changeAvatorBtn -> { //强制替换头像
                lieAvatorLl.isVisible = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    /**
     * 匹配列表数据
     */
    private var paramsLastFiveIds = mutableListOf<Int>()

    override fun onGetMatchListResult(success: Boolean, matchBeans: MatchListBean?) {
        if (success) {
            if (matchBeans != null) {

                hasMore = (matchBeans!!.list ?: mutableListOf<MatchBean>()).size == PAGESIZE

                matchUserAdapter.addData(matchBeans.list ?: mutableListOf<MatchBean>())
                matchUserAdapter.my_tags_quality = matchBeans.mytags ?: mutableListOf<Newtag>()
                paramsLastFiveIds = matchBeans.exclude ?: mutableListOf()

                //保存 VIP信息
                UserManager.saveUserVip(matchBeans.isvip)
                //保存认证信息
                UserManager.saveUserVerify(matchBeans.isfaced)

                EventBus.getDefault().post(UpdateSlideCountEvent())
                //保存剩余招呼次数
                UserManager.saveLightingCount(matchBeans.lightningcnt ?: 0)

                //保存引导次数
                UserManager.motion = matchBeans.motion
                is_human = matchBeans.is_human

                if (!matchBeans.is_human) {
                    lieAvatorLl.isVisible = true
                    changeAvatorCloseBtn.isVisible = false
                } else if (!matchBeans.is_full) {
                    lieAvatorLl.isVisible = true
                    changeAvatorCloseBtn.isVisible = true
                    changeAvatorCloseBtn.onClick {
                        lieAvatorLl.isVisible = false
                    }
                } else {
                    lieAvatorLl.isVisible = false
                }

                when (matchBeans.motion) {
                    GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS -> {//7头像违规替换
                        EventBus.getDefault().postSticky(ReVerifyEvent(GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS))
                    }
                    GotoVerifyDialog.TYPE_CHANGE_AVATOR_PASS -> {//2//头像通过,但是不是真人
                        UserManager.replace_times = matchBeans.replace_times
                    }
                    GotoVerifyDialog.TYPE_CHANGE_ABLUM -> {//3//完善相册
                        UserManager.perfect_times = matchBeans.perfect_times
                    }
                    else -> {
                        UserManager.cleanVerifyData()
                    }
                }
            }

            if (matchBeans == null || (matchBeans!!.list.isNullOrEmpty() && matchUserAdapter.data.isNullOrEmpty())) {
                setViewState(EMPTY)
            } else {
                setViewState(CONTENT)
            }

        } else {
            setViewState(ERROR)
            errorMsg.text = if (mPresenter.checkNetWork()) {
                activity!!.getString(R.string.retry_load_error)
            } else {
                activity!!.getString(R.string.retry_net_error)
            }
        }
    }


    /**
     * 左滑不喜欢结果
     * //405 封禁
     * //201 会员提醒
     */
    override fun onGetDislikeResult(success: Boolean, data: BaseResp<StatusBean?>) {

        if (data.code == 200) {
            if (data.data != null) {
                if (UserManager.getCurrentSurveyVersion().isEmpty()) {
                    UserManager.saveSlideSurveyCount(UserManager.getSlideSurveyCount().plus(1))
                    EventBus.getDefault().post(ShowSurveyDialogEvent(UserManager.getSlideSurveyCount()))
                }

                if (data.data!!.residue == 0) {
                    card_stack_view.rewind()
                    ChargeVipDialog(ChargeVipDialog.INFINITE_SLIDE, activity!!).show()
                    return
                }
            } else {
                CommonFunction.toast(data.msg)
                card_stack_view.rewind()
            }
        }
    }
    override fun onError(text: String) {
        Log.d("error", text)
    }


    /**
     * 改变当前页面的状态
     */
    private fun setViewState(state: Int) {
        when (state) {
            LOADING -> {
                loadingLayout.isVisible = true
                contentLayout.isVisible = false
                errorLayout.isVisible = false
                emptyLayout.isVisible = false
            }
            CONTENT -> {
                contentLayout.isVisible = true
                loadingLayout.isVisible = false
                errorLayout.isVisible = false
                emptyLayout.isVisible = false
            }
            ERROR -> {
                errorLayout.isVisible = true
                contentLayout.isVisible = false
                loadingLayout.isVisible = false
                emptyLayout.isVisible = false
            }
            EMPTY -> {
                emptyLayout.isVisible = true
                emptyLayout.emptyFriendTitle.isVisible = true
                emptyLayout.emptyFriendGoBtn.isVisible = false
                emptyLayout.emptyImg.setImageResource(R.drawable.icon_empty_match)
                emptyLayout.emptyFriendTitle.text = "暂时没有人了"
                emptyLayout.emptyFriendTip.text = "一会儿再回来看看吧"
                contentLayout.isVisible = false
                errorLayout.isVisible = false
                loadingLayout.isVisible = false
            }
        }

    }


    /*---------------------事件总线--------------------------------*/
    /**
     * 通过本地的筛选条件类更新数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefreshEvent(event: RefreshEvent) {
        setViewState(LOADING)

        matchUserAdapter.data.clear()
        hasMore = false

        updateLocation()
        mPresenter.getMatchList(matchParams)
    }

    /**
     * 震动动画
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onShakeEvent(event: ShakeEvent) {
        YoYo.with(Techniques.Shake)
            .duration(400)
            .repeat(0)
            .playOn(card_stack_view)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateHiCountEvent(event: UpdateHiCountEvent) {
        if (UserManager.getLightingCount() < 0) {
            UserManager.saveLightingCount(0)
        }
        if (manager.topView != null)
            (manager.topView.findViewById<TextView>(R.id.btnHiLeftTime)).text = "${UserManager.getLightingCount()}"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateCardEvent(event: GreetEvent) {
        //请求失败了 卡片飞回来
        if (!event.success)
            card_stack_view.rewind()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun turnToLastLabelEvent(event: TurnToLastLabelEvent) {
        if (event.from == ChargeLabelDialog.FROM_INDEX) {
            onRefreshEvent(RefreshEvent(true))
        }
    }


    /*---------------------卡片参数和方法------------------------------*/
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
//        manager.setMaxDegree(5F)
        //滑动的方向
//        manager.setDirections(mutableListOf(Direction.Left, Direction.Right, Direction.Top))
        manager.setDirections(Direction.HORIZONTAL)
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(false)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())

        //撤回的动画设置
        val setting = RewindAnimationSetting.Builder()
            .setDirection(Direction.Right)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(DecelerateInterpolator())
            .build()
        manager.setRewindAnimationSetting(setting)
        card_stack_view.layoutManager = manager
        card_stack_view.adapter = matchUserAdapter
        matchUserAdapter.bindToRecyclerView(card_stack_view)
        card_stack_view.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }


    override fun onCardDisappeared(view: View?, position: Int) {
        Log.d("CardStackView", "onCardDisappeared: ($position)")
    }


    override fun onCardDragging(direction: Direction, ratio: Float) {
        Log.d("CardStackView", "onCardDragging: ($direction)")

        //向上超级喜欢(会员就超级喜欢 否则弹起收费窗)
        when (direction) {
            //左滑时加载动画
            Direction.Left -> {
                //重置右边、上边的距离
                animation_like.alpha = 0F
                val paramsLike = animation_like.layoutParams as RelativeLayout.LayoutParams
                paramsLike.width = 0
                paramsLike.height = 0
                animation_like.layoutParams = paramsLike

                animation_dislike.alpha = ratio
                val params = animation_dislike.layoutParams as RelativeLayout.LayoutParams
                params.width = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.height = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.leftMargin = ((ScreenUtils.getScreenWidth() / 2F * ratio) - params.width / 2F).toInt()
                animation_dislike.layoutParams = params

            }
            //右滑时加载动画
            Direction.Right -> {
                //重置左边、上边的距离
                val paramsLike = animation_dislike.layoutParams as RelativeLayout.LayoutParams
                paramsLike.width = 0
                paramsLike.height = 0
                animation_dislike.layoutParams = paramsLike
                animation_dislike.alpha = 0F

                animation_like.alpha = ratio
                val params = animation_like.layoutParams as RelativeLayout.LayoutParams
                params.width = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.height = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.rightMargin = ((ScreenUtils.getScreenWidth() / 2F * ratio) - params.width / 2F).toInt()
                animation_like.layoutParams = params
            }


        }
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    //此时已经飞出去了
    override fun onCardSwiped(direction: Direction?) {
        if (UserManager.slide_times != -1) {
            UserManager.slide_times++
            if (UserManager.motion == GotoVerifyDialog.TYPE_CHANGE_ABLUM && UserManager.slide_times == UserManager.perfect_times && !UserManager.getAlertChangeAlbum()) { //完善相册
                EventBus.getDefault().postSticky(ReVerifyEvent(GotoVerifyDialog.TYPE_CHANGE_ABLUM))
                UserManager.slide_times = 0
            }
        }



        resetAnimation()
        if (direction == Direction.Left) {//左滑不喜欢
            params["target_accid"] = matchUserAdapter.data[manager.topPosition - 1].accid ?: ""
            if (!matchUserAdapter.data[manager.topPosition - 1].newtags.isNullOrEmpty())
                params["tag_id"] = matchUserAdapter.data[manager.topPosition - 1].newtags!![0].id
            mPresenter.dislikeUser(params)
        } else if (direction == Direction.Right) {//右滑喜欢
            //非真人头像打招呼提示去修改头像
            if (!is_human && manager.topPosition - 1 == 0 && !UserManager.getAlertChangeRealMan()) {
                ChangeAvatarRealManDialog(
                    activity!!,
                    ChangeAvatarRealManDialog.VERIFY_NEED_REAL_MAN_GREET,
                    matchBean = matchUserAdapter.data[manager.topPosition - 1]
                ).show()
            } else {
//                保存剩余滑动次数
                CommonFunction.commonGreet(
                    activity!!,
                    matchUserAdapter.data[manager.topPosition - 1].accid,
                    targetAvator = matchUserAdapter.data[manager.topPosition - 1].avatar ?: ""
                )
            }
        }


        //如果已经只剩5张了就请求数据(预加载).
        if (hasMore && manager.topPosition == matchUserAdapter.itemCount - 5) {
            updateLocation()
            mPresenter.getMatchList(matchParams, paramsLastFiveIds)
        } else if (!hasMore && manager.topPosition == matchUserAdapter.itemCount) {
            setViewState(EMPTY)
        }
    }

    override fun onCardCanceled() {
        resetAnimation()
        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")

    }


    override fun onCardAppeared(view: View?, position: Int) {
        Log.d("CardStackView", "onCardAppeared: ($position)")
        if (view != null) {
            if (matchUserAdapter.data[manager.topPosition].greet_switch) {
                (view.findViewById<ConstraintLayout>(R.id.btnHi)).visibility = View.INVISIBLE
                (view.findViewById<LottieAnimationView>(R.id.btnHiLottieView)).isVisible = true
                (view.findViewById<TextView>(R.id.btnHiLeftTime)).text = "${UserManager.getLightingCount()}"

                (view.findViewById<LottieAnimationView>(R.id.btnHiLottieView)).addAnimatorListener(object :
                    Animator.AnimatorListener {
                    override fun onAnimationEnd(animation: Animator?) {
                        (view.findViewById<ConstraintLayout>(R.id.btnHi)).visibility = View.VISIBLE
                        (view.findViewById<LottieAnimationView>(R.id.btnHiLottieView)).visibility = View.INVISIBLE


                        //透明度起始为1，结束时为0
                        val animator =
                            ObjectAnimator.ofFloat(view.findViewById<ImageView>(R.id.btnHiIv), "alpha", 1f, 0f)
                        val animator1 =
                            ObjectAnimator.ofFloat(view.findViewById<TextView>(R.id.btnHiLeftTime), "alpha", 0f, 1f)
                        val animator2 =
                            ObjectAnimator.ofFloat(view.findViewById<ImageView>(R.id.btnHiIv), "alpha", 0f, 1f)
                        val animator3 =
                            ObjectAnimator.ofFloat(view.findViewById<TextView>(R.id.btnHiLeftTime), "alpha", 1f, 0f)
                        animator1.duration = animator.duration//时间1s
                        animator2.duration = animator.duration//时间1s
                        animator3.duration = animator.duration//时间1s
                        animator.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {

                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                animator1.start()
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationStart(animation: Animator?) {
                            }

                        })
                        animator1.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {

                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                view.postDelayed({
                                    animator2.start()
                                    animator3.start()
                                }, 1000L)
                            }

                            override fun onAnimationCancel(animation: Animator?) {

                            }

                            override fun onAnimationStart(animation: Animator?) {
                            }

                        })
                        animator2.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {

                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                (view.findViewById<ConstraintLayout>(R.id.btnHi)).visibility = View.INVISIBLE
                                (view.findViewById<LottieAnimationView>(R.id.btnHiLottieView)).isVisible = true

                                (view.findViewById<LottieAnimationView>(R.id.btnHiLottieView)).playAnimation()
                            }

                            override fun onAnimationCancel(animation: Animator?) {

                            }

                            override fun onAnimationStart(animation: Animator?) {
                            }

                        })

                        view.postDelayed({
                            animator.start()
                        }, 2000L)
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }

                    override fun onAnimationRepeat(animation: Animator?) {

                    }

                })
                (view.findViewById<LottieAnimationView>(R.id.btnHiLottieView)).playAnimation()
            }
        }
    }

    override fun onCardRewound() {
        if (UserManager.slide_times > 0) {
            UserManager.slide_times--
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


}
