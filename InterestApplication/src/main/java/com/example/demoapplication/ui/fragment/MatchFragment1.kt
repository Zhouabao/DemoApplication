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
import com.example.demoapplication.presenter.MatchPresenter
import com.example.demoapplication.presenter.view.MatchView
import com.example.demoapplication.ui.activity.MatchDetailActivity
import com.example.demoapplication.ui.adapter.MatchUserAdapter1
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
            userList.add(0, userList.removeAt(adapterPosition))
        } else {
            if (matchUserAdapter.data.size > 0) {
                matchUserAdapter.data.removeAt(adapterPosition)
            }
        }
        matchUserAdapter.notifyDataSetChanged()
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

    //用户数据源
    var userList: MutableList<MatchBean1> = mutableListOf()


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnDislike -> {
                toast("不喜欢")
                callback.toLeft(matchUserRv)

            }
            R.id.btnLike -> {
                toast("喜欢")
            }
            R.id.btnChat -> {
            }
        }

    }


    override fun onGetMatchListResult(success: Boolean, matchBeans: MatchListBean?) {
        if (success) {
//            for (match in matchBeans!!) {
//                match.photos = mutableListOf(
//                    "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg",
//                    "http://rsrc1.futrueredland.com.cn/ppns/headImage/0ca42c0d253ebee3f2bb197fbfcc5527/1562740286/0uBnhoxs4yRnWl39",
//                    "http://rsrc1.futrueredland.com.cn/ppns/avator/0ca42c0d253ebee3f2bb197fbfcc5527/1562759634820/1pw367w0qfwtuwm0.jpg"
//                )
//            }
            matchUserAdapter.addData(matchBeans!!.list ?: mutableListOf<MatchBean1>())
            tvLeftChatTime.text = "${matchBeans.lightningcnt}"
        } else {
            mPresenter.mView.onError("加载失败")
            initData()

        }
    }

    private fun initData() {
        userList.add(
            MatchBean1(
                nickname = "Lily",
                age = 23,
                gender = 1,
                photos = mutableListOf(
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/0ca42c0d253ebee3f2bb197fbfcc5527/1562759634820/1pw367w0qfwtuwm0.jpg",
                    "http://rsrc1.futrueredland.com.cn/ppns/headImage/0ca42c0d253ebee3f2bb197fbfcc5527/1562740286/0uBnhoxs4yRnWl39",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg"
                )
            )
        )
        userList.add(
            MatchBean1(
                nickname = "Username",
                age = 28,
                gender = 2,
                photos = mutableListOf(
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg",
                    "http://rsrc1.futrueredland.com.cn/ppns/headImage/0ca42c0d253ebee3f2bb197fbfcc5527/1562740286/0uBnhoxs4yRnWl39",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/0ca42c0d253ebee3f2bb197fbfcc5527/1562759634820/1pw367w0qfwtuwm0.jpg"
                )
            )
        )
        userList.add(
            MatchBean1(
                "Shirly",
                24,
                gender = 2,
                photos = mutableListOf(
                    "http://rsrc1.futrueredland.com.cn/ppns/headImage/0ca42c0d253ebee3f2bb197fbfcc5527/1562740286/0uBnhoxs4yRnWl39",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/0ca42c0d253ebee3f2bb197fbfcc5527/1562759634820/1pw367w0qfwtuwm0.jpg",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg"
                )
            )
        )
        userList.add(
            MatchBean1(
                "爱的魔力圈",
                19,
                gender = 1,
                avatar = "http://rsrc1.futrueredland.com.cn/ppns/avator/0ca42c0d253ebee3f2bb197fbfcc5527/1562759634820/1pw367w0qfwtuwm0.jpg"
            )
        )
        userList.add(
            MatchBean1(
                "Lily",
                23,
                gender = 1,
                photos = mutableListOf(
                    "http://rsrc1.futrueredland.com.cn/ppns/headImage/0ca42c0d253ebee3f2bb197fbfcc5527/1562740286/0uBnhoxs4yRnWl39",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/0ca42c0d253ebee3f2bb197fbfcc5527/1562759634820/1pw367w0qfwtuwm0.jpg",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg"
                )
            )
        )
        userList.add(
            MatchBean1(
                "爱的魔力圈",
                19,
                gender = 1,
                avatar = "http://rsrc1.futrueredland.com.cn/ppns/avator/0ca42c0d253ebee3f2bb197fbfcc5527/1562759634820/1pw367w0qfwtuwm0.jpg"
            )
        )
        userList.add(
            MatchBean1(
                "Lily",
                23,
                gender = 1,
                photos = mutableListOf(
                    "http://rsrc1.futrueredland.com.cn/ppns/headImage/0ca42c0d253ebee3f2bb197fbfcc5527/1562740286/0uBnhoxs4yRnWl39",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/0ca42c0d253ebee3f2bb197fbfcc5527/1562759634820/1pw367w0qfwtuwm0.jpg",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg"
                )
            )
        )
        userList.add(
            MatchBean1(
                "Lily",
                23,
                gender = 1,
                photos = mutableListOf(
                    "http://rsrc1.futrueredland.com.cn/ppns/headImage/0ca42c0d253ebee3f2bb197fbfcc5527/1562740286/0uBnhoxs4yRnWl39",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/0ca42c0d253ebee3f2bb197fbfcc5527/1562759634820/1pw367w0qfwtuwm0.jpg",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg"
                )
            )
        )
        matchUserAdapter.addData(userList)
    }


}
