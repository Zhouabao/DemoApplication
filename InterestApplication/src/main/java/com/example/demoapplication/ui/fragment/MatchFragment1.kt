package com.example.demoapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseViewHolder
import com.example.demoapplication.R
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.presenter.MatchPresenter
import com.example.demoapplication.presenter.view.MatchView
import com.example.demoapplication.ui.activity.MatchDetailActivity
import com.example.demoapplication.ui.adapter.MatchUserAdapter1
import com.example.demoapplication.widgets.stackview.StackCardsView
import com.example.demoapplication.widgets.swipecard.CardConfig
import com.example.demoapplication.widgets.swipecard.OverLayCardLayoutManager
import com.example.demoapplication.widgets.swipecard.RenRenCallback
import com.kotlin.base.ui.fragment.BaseMvpFragment
import kotlinx.android.synthetic.main.fragment_match1.*
import kotlinx.android.synthetic.main.item_match_user1.view.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

/**
 * 匹配页面
 */
class MatchFragment1 : BaseMvpFragment<MatchPresenter>(), MatchView, View.OnClickListener,
    StackCardsView.OnCardSwipedListener, RenRenCallback.OnSwipeListener {


    //用户适配器
    private val matchUserAdapter: MatchUserAdapter1 by lazy { MatchUserAdapter1(userList) }

    //    private val matchUserAdapter by lazy { CardAdapter() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_match1, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {
        btnDislike.setOnClickListener(this)
        btnLike.setOnClickListener(this)
        btnChat.setOnClickListener(this)
        matchUserRv.layoutManager = OverLayCardLayoutManager()
        matchUserRv.adapter = matchUserAdapter
        CardConfig.initConfig(activity!!)
//        val callback = RenRenCallback(matchUserRv, matchUserAdapter, userList)
        val callback = RenRenCallback()
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(matchUserRv)
        matchUserAdapter.setOnItemClickListener { adapter, view, position ->
            toast("${position}")
        }
        callback.setSwipeListener(this)
//        matchUserStackView.setAdapter(matchUserAdapter)
//        matchUserStackView.addOnCardSwipedListener(this)
        initData()
    }

    override fun onSwiped(adapterPosition: Int, direction: Int) {
        if (direction == ItemTouchHelper.UP) {
            userList.add(0, userList.removeAt(adapterPosition))
            startActivity<MatchDetailActivity>("matchBean" to userList[adapterPosition])
        } else {
            userList.removeAt(adapterPosition)
        }
        matchUserAdapter.notifyDataSetChanged()
    }

    override fun onSwipeTo(viewHolder: RecyclerView.ViewHolder?, offset: Float) {
        val holder = viewHolder as BaseViewHolder
        if (offset < -50) {
            holder.itemView.matchDislike.visibility = View.VISIBLE
            holder.itemView.matchLike.visibility = View.INVISIBLE
        } else if (offset > 50) {
            holder.itemView.matchLike.visibility = View.VISIBLE
            holder.itemView.matchDislike.visibility = View.INVISIBLE
        } else {
            holder.itemView.matchLike.visibility = View.INVISIBLE
            holder.itemView.matchDislike.visibility = View.INVISIBLE
        }
    }

    //用户数据源
    var userList: MutableList<MatchBean> = mutableListOf()

    override fun onGetNewUserFromLabelResult(users: String) {

    }

    private fun initData() {
        userList.add(
            MatchBean(
                "Lily",
                23,
                1,
                mutableListOf(
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/0ca42c0d253ebee3f2bb197fbfcc5527/1562759634820/1pw367w0qfwtuwm0.jpg",
                    "http://rsrc1.futrueredland.com.cn/ppns/headImage/0ca42c0d253ebee3f2bb197fbfcc5527/1562740286/0uBnhoxs4yRnWl39",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg"
                )
            )
        )
        userList.add(
            MatchBean(
                "Username",
                28,
                2,
                mutableListOf(
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg",
                    "http://rsrc1.futrueredland.com.cn/ppns/headImage/0ca42c0d253ebee3f2bb197fbfcc5527/1562740286/0uBnhoxs4yRnWl39",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/0ca42c0d253ebee3f2bb197fbfcc5527/1562759634820/1pw367w0qfwtuwm0.jpg"
                )
            )
        )
        userList.add(
            MatchBean(
                "Shirly",
                24,
                2,
                mutableListOf(
                    "http://rsrc1.futrueredland.com.cn/ppns/headImage/0ca42c0d253ebee3f2bb197fbfcc5527/1562740286/0uBnhoxs4yRnWl39",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/0ca42c0d253ebee3f2bb197fbfcc5527/1562759634820/1pw367w0qfwtuwm0.jpg",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg"
                )
            )
        )
        userList.add(
            MatchBean(
                "爱的魔力圈",
                19,
                1,
                mutableListOf(
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/0ca42c0d253ebee3f2bb197fbfcc5527/1562759634820/1pw367w0qfwtuwm0.jpg",
                    "http://rsrc1.futrueredland.com.cn/ppns/headImage/0ca42c0d253ebee3f2bb197fbfcc5527/1562740286/0uBnhoxs4yRnWl39",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg"
                )
            )
        )
        userList.add(
            MatchBean(
                "Lily",
                23,
                1,
                mutableListOf(
                    "http://rsrc1.futrueredland.com.cn/ppns/headImage/0ca42c0d253ebee3f2bb197fbfcc5527/1562740286/0uBnhoxs4yRnWl39",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/0ca42c0d253ebee3f2bb197fbfcc5527/1562759634820/1pw367w0qfwtuwm0.jpg",
                    "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg"
                )
            )
        )
        matchUserAdapter.addData(userList)
//        matchUserAdapter.notifyDataSetChanged()

//        val items = mutableListOf<MatchCardItem>()
//        for (i in 0 until userList.size) {
//            val matchItem = MatchCardItem(activity!!, userList[i])
//            items.add(matchItem)
//        }
//        matchUserAdapter.appendItems(items as List<BaseCardItem>?)
//        matchUserAdapter.addData(userList)
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnDislike -> {
                toast("不喜欢")
                matchUserAdapter.remove(0)
                //
//                    matchUserAdapter.remove(0)
//                matchUserStackView.removeCover(StackCardsView.SWIPE_LEFT)
            }
            R.id.btnLike -> {
                toast("喜欢")
//                matchUserAdapter.remove(0)
//                matchUserAdapter.remove(0)
//                matchUserStackView.removeCover(StackCardsView.SWIPE_RIGHT)

            }
            R.id.btnChat -> {
            }
        }

    }

    override fun onCardDismiss(direction: Int) {
        when (direction) {
            StackCardsView.SWIPE_LEFT -> {
                toast("不喜欢")
                matchUserAdapter.remove(0)
            }
            StackCardsView.SWIPE_RIGHT -> {
                toast("喜欢")
                matchUserAdapter.remove(0)
            }
            StackCardsView.SWIPE_UP -> {
                //永远是最后一个在最上面
                startActivity<MatchDetailActivity>("matchBean" to userList[userList.size - 1])
            }
        }

//        if (matchUserAdapter.count < 3) {
//            initData()
//        }
    }

    override fun onCardScrolled(view: View?, progress: Float, direction: Int) {
        if (progress == 1F)
            when (direction) {
                StackCardsView.SWIPE_LEFT -> {
                    toast("不喜欢")
                }
                StackCardsView.SWIPE_RIGHT -> {
                    toast("喜欢")
                }
                StackCardsView.SWIPE_UP -> {
                    //永远是最后一个在最上面
                    startActivity<MatchDetailActivity>("matchBean" to userList[userList.size - 1])
                }
            }
    }


}
