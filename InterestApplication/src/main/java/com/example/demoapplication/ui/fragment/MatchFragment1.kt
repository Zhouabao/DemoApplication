package com.example.demoapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.chad.library.adapter.base.BaseViewHolder
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.model.MatchBean1
import com.example.demoapplication.model.MatchListBean
import com.example.demoapplication.model.StatusBean
import com.example.demoapplication.presenter.MatchPresenter
import com.example.demoapplication.presenter.view.MatchView
import com.example.demoapplication.ui.activity.MatchDetailActivity
import com.example.demoapplication.ui.adapter.MatchUserAdapter1
import com.example.demoapplication.ui.chat.MatchSucceedActivity
import com.example.demoapplication.utils.UserManager
import com.example.demoapplication.widgets.swipecard.CardConfig
import com.example.demoapplication.widgets.swipecard.OverLayCardLayoutManager
import com.example.demoapplication.widgets.swipecard.RenRenCallback
import com.kotlin.base.ui.fragment.BaseMvpFragment
import kotlinx.android.synthetic.main.fragment_match1.*
import kotlinx.android.synthetic.main.item_match_user1.view.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

/**
 * 匹配页面(新版)
 * //todo 探探是把用戶存在本地數據庫的
 */
class MatchFragment1 : BaseMvpFragment<MatchPresenter>(), MatchView, View.OnClickListener,
    RenRenCallback.OnSwipeListener {


    //用户适配器
    private val matchUserAdapter: MatchUserAdapter1 by lazy { MatchUserAdapter1(mutableListOf()) }


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
            "tagid" to 1
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

    private fun initView() {
        mPresenter = MatchPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        btnDislike.setOnClickListener(this)
        btnLike.setOnClickListener(this)
        btnChat.setOnClickListener(this)
        matchUserRv.layoutManager = OverLayCardLayoutManager()
        matchUserRv.adapter = matchUserAdapter
        CardConfig.initConfig(activity!!)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(matchUserRv)
        callback.setSwipeListener(this)
        mPresenter.getMatchList(matchParams)
        matchUserAdapter.setOnItemClickListener { _, view, position ->
            //保持始终是顶部item被点击到
            if (position == matchUserAdapter.data.size - 1) {
                toast("$$$$$$$$$$$${position}")
            }
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
            mPresenter.likeUser(params)
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
                mPresenter.likeUser(params)
            }
            R.id.btnChat -> {
            }
        }

    }


    override fun onGetMatchListResult(success: Boolean, matchBeans: MatchListBean?) {
        if (success) {
            matchUserAdapter.addData(matchBeans!!.list ?: mutableListOf<MatchBean1>())
            tvLeftChatTime.text = "${matchBeans.lightningcnt}"
        } else {
            mPresenter.mView.onError("加载失败")
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
            matchUserAdapter.data.add(matchUserAdapter.data.size - 1, matchUserAdapter.data.removeAt(matchUserAdapter.data.size - 1))
        }
        matchUserAdapter.notifyDataSetChanged()

    }

}
