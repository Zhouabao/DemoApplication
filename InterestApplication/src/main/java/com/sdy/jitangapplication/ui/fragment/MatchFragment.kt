package com.sdy.jitangapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.chad.library.adapter.base.BaseViewHolder
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.UpdateLabelEvent
import com.sdy.jitangapplication.model.GreetBean
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.MatchListBean
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.presenter.MatchPresenter
import com.sdy.jitangapplication.presenter.view.MatchView
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.adapter.MatchUserAdapter
import com.sdy.jitangapplication.ui.chat.MatchSucceedActivity
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.swipecard.OverLayCardLayoutManager
import com.sdy.jitangapplication.widgets.swipecard.RenRenCallback
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_match.*
import kotlinx.android.synthetic.main.item_match_user.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

/**
 * 匹配页面(新版)
 * //todo 探探是把用戶存在本地數據庫的
 */
class MatchFragment : BaseMvpFragment<MatchPresenter>(), MatchView, View.OnClickListener,
    RenRenCallback.OnSwipeListener {

    override fun onGreetStateResult(greetBean: GreetBean?, matchBean: MatchBean) {
    }

    override fun onGreetSResult(greetBean: Boolean, code: Int, matchBean: MatchBean) {
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
        return inflater.inflate(R.layout.fragment_match, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private val callback by lazy { RenRenCallback() }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = MatchPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        btnDislike.setOnClickListener(this)
        btnLike.setOnClickListener(this)
        btnChat.setOnClickListener(this)
        matchUserRv.layoutManager = OverLayCardLayoutManager(activity!!)
        matchUserRv.adapter = matchUserAdapter
        ItemTouchHelper(callback).attachToRecyclerView(matchUserRv)
        callback.setSwipeListener(this)
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


    override fun onSwiped(adapterPosition: Int, direction: Int) {
        if (direction == ItemTouchHelper.UP) {
            startActivity<MatchDetailActivity>("target_accid" to matchUserAdapter.data[adapterPosition].accid)
//            matchUserAdapter.data.add(adapterPosition, userList.removeAt(adapterPosition))
        } else if (direction == ItemTouchHelper.LEFT) {
            params["target_accid"] = matchUserAdapter.data[adapterPosition].accid ?: ""
            mPresenter.dislikeUser(params)

        } else if (direction == ItemTouchHelper.RIGHT) {
            params["target_accid"] = matchUserAdapter.data[adapterPosition].accid ?: ""
            mPresenter.likeUser(params, matchUserAdapter.data[matchUserAdapter.data.size - 1])
        }
    }

    val params by lazy {
        hashMapOf<String, Any>(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "target_accid" to ""
        )
    }

    override fun onSwipeTo(viewHolder: RecyclerView.ViewHolder?, offset: Float) {
        val holder = viewHolder as BaseViewHolder
        if (offset < -100) {
            holder.itemView.matchDislike.alpha = (Math.abs(offset) - 100) * 0.02f
            holder.itemView.matchLike.alpha = 0F
        } else if (offset > 100) {
            holder.itemView.matchLike.alpha = (Math.abs(offset) - 100) * 0.02f
            holder.itemView.matchDislike.alpha = 0F
        } else {
            holder.itemView.matchDislike.alpha = 0F
            holder.itemView.matchLike.alpha = 0F
        }

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnDislike -> {
                callback.toLeft(matchUserRv)
                params["target_accid"] = matchUserAdapter.data[matchUserAdapter.data.size - 1].accid ?: ""
                mPresenter.dislikeUser(params)
            }
            R.id.btnLike -> {
                callback.toRight(matchUserRv)
                params["target_accid"] = matchUserAdapter.data[matchUserAdapter.data.size - 1].accid ?: ""
                mPresenter.likeUser(params, matchUserAdapter.data[matchUserAdapter.data.size - 1])
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

    override fun onGetDislikeResult(success: Boolean, data: BaseResp<StatusBean?>) {
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
    override fun onGetLikeResult(success: Boolean, data: BaseResp<StatusBean?>, matchBean: MatchBean) {
        if (success) {
            matchUserAdapter.data.removeAt(matchUserAdapter.data.size - 1)
            if (data != null && data.data!!.status == 2) {
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateLabelEvent(event: UpdateLabelEvent) {
        matchParams["tagid"] = event.label.id
        //这个地方还要默认设置选中第一个标签来更新数据
        mPresenter.getMatchList(matchParams)
    }

}
