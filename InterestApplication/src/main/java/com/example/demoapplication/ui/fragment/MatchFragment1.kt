package com.example.demoapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import com.blankj.utilcode.util.SPUtils
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.event.UpdateLabelEvent
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.model.MatchListBean
import com.example.demoapplication.model.StatusBean
import com.example.demoapplication.presenter.MatchPresenter
import com.example.demoapplication.presenter.view.MatchView
import com.example.demoapplication.ui.activity.MatchDetailActivity
import com.example.demoapplication.ui.adapter.MatchUserAdapter
import com.example.demoapplication.ui.chat.MatchSucceedActivity
import com.example.demoapplication.utils.UserManager
import com.example.demoapplication.widgets.swipecard.RenRenCallback
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_match.btnChat
import kotlinx.android.synthetic.main.fragment_match.btnDislike
import kotlinx.android.synthetic.main.fragment_match.btnLike
import kotlinx.android.synthetic.main.fragment_match.stateview
import kotlinx.android.synthetic.main.fragment_match.tvLeftChatTime
import kotlinx.android.synthetic.main.fragment_match1.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

/**
 * 匹配页面(新版)
 * //todo 探探是把用戶存在本地數據庫的
 */
class MatchFragment1 : BaseMvpFragment<MatchPresenter>(), MatchView, View.OnClickListener, CardStackListener {
    private var switch = false

    override fun onCardDisappeared(view: View?, position: Int) {
    }

    override fun onCardDragging(direction: Direction?, ratio: Float) {
        if (direction == Direction.Top && ratio > 0.7F && !switch) {
            startActivity<MatchDetailActivity>("target_accid" to matchUserAdapter.data[manager.topPosition].accid)
            switch = true
        }
    }

    override fun onCardSwiped(direction: Direction?) {
        if (direction == Direction.Left) {
            params["target_accid"] = matchUserAdapter.data[manager.topPosition].accid ?: ""
            mPresenter.dislikeUser(params)
        } else if (direction == Direction.Right) {
            params["target_accid"] = matchUserAdapter.data[manager.topPosition].accid ?: ""
            mPresenter.likeUser(params)
        }

        //如果已经只剩5张了就请求数据
        if (manager.topPosition == matchUserAdapter.data.size - 5) {
            mPresenter.getMatchList(matchParams)
        }
    }

    override fun onCardCanceled() {
    }

    override fun onCardAppeared(view: View?, position: Int) {
    }

    override fun onCardRewound() {
    }


    //用户适配器
    private val matchUserAdapter: MatchUserAdapter by lazy { MatchUserAdapter(mutableListOf()) }


    //当前请求页
    var page = 1
    //请求广场的参数 TODO要更新tagid
    private val matchParams by lazy {
        hashMapOf(
            "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
            "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
            "page" to page,
            "pagesize" to Constants.PAGESIZE,
            "_timestamp" to System.currentTimeMillis(),
            "tagid" to SPUtils.getInstance(Constants.SPNAME).getInt("globalLabelId")
        )
    }


    //    private val matchUserAdapter by lazy { CardAdapter() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_match1, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private val callback by lazy { RenRenCallback() }
    private val manager by lazy { CardStackLayoutManager(activity!!, this) }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = MatchPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        btnDislike.setOnClickListener(this)
        btnLike.setOnClickListener(this)
        btnChat.setOnClickListener(this)

        initialize()
        mPresenter.getMatchList(matchParams)
        matchUserAdapter.setOnItemClickListener { _, view, position ->
            //保持始终是顶部item被点击到
            if (position == matchUserAdapter.data.size - 1) {
                toast("$$$$$$$$$$$${position}")
            }
        }

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getMatchList(matchParams)
        }
    }

    val params by lazy {
        hashMapOf<String, Any>(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "target_accid" to ""
        )
    }

    private fun initialize() {
        //卡片排列方式
        manager.setStackFrom(StackFrom.Bottom)
        //最大可见数量
        manager.setVisibleCount(3)
        //两个卡片之间的间隔
        manager.setTranslationInterval(13.0f)
        //最大的缩放间隔
        manager.setScaleInterval(0.95f)
        //卡片滑出飞阈值
        manager.setSwipeThreshold(0.3f)
        //横向纵向的旋转角度
        manager.setMaxDegree(20.0f)
        //滑动的方向
        manager.setDirections(Direction.HORIZONTAL)
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(true)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())
        card_stack_view.layoutManager = manager
        card_stack_view.adapter = matchUserAdapter
        card_stack_view.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnDislike -> {
                callback.toLeft(card_stack_view)
                params["target_accid"] = matchUserAdapter.data[matchUserAdapter.data.size - 1].accid ?: ""
                mPresenter.dislikeUser(params)
            }
            R.id.btnLike -> {
                callback.toRight(card_stack_view)
                params["target_accid"] = matchUserAdapter.data[matchUserAdapter.data.size - 1].accid ?: ""
                mPresenter.likeUser(params)
            }
            R.id.btnChat -> {

            }
        }

    }


    override fun onGetMatchListResult(success: Boolean, matchBeans: MatchListBean?) {
        if (success) {
            if (matchBeans!!.list.isNullOrEmpty() && matchUserAdapter.data.isNullOrEmpty()) {
                stateview.viewState = MultiStateView.VIEW_STATE_EMPTY
            } else {
                stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
            }
            matchUserAdapter.addData(matchBeans!!.list ?: mutableListOf<MatchBean>())
            tvLeftChatTime.text = "${matchBeans.lightningcnt}"
        } else {
            stateview.viewState = MultiStateView.VIEW_STATE_ERROR
            stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
                activity!!.getString(R.string.retry_load_error)
            } else {
                activity!!.getString(R.string.retry_net_error)
            }
        }
    }

    override fun onGetDislikeResult(success: Boolean) {
        if (success) {
            matchUserAdapter.data.removeAt(matchUserAdapter.data.size - 1)
        } else {
            matchUserAdapter.data.add(
                matchUserAdapter.data.size - 1,
                matchUserAdapter.data.removeAt(matchUserAdapter.data.size - 1)
            )
        }
        matchUserAdapter.notifyDataSetChanged()
    }

    //todo  这里应该还要传参数
    override fun onGetLikeResult(success: Boolean, data: StatusBean?) {
        if (success) {
            matchUserAdapter.data.removeAt(matchUserAdapter.data.size - 1)
            if (data != null && data.status == 2) {
                startActivity<MatchSucceedActivity>()
            }
        } else {
            matchUserAdapter.data.add(
                matchUserAdapter.data.size - 1,
                matchUserAdapter.data.removeAt(matchUserAdapter.data.size - 1)
            )
        }
        matchUserAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        switch = false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateLabelEvent(event: UpdateLabelEvent) {
        matchParams["tagid"] = event.label.id
        //这个地方还要默认设置选中第一个标签来更新数据
        mPresenter.getMatchList(matchParams)
    }
}
