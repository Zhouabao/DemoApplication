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
import com.example.demoapplication.model.MatchListBean
import com.example.demoapplication.presenter.MatchPresenter
import com.example.demoapplication.presenter.view.MatchView
import com.example.demoapplication.ui.activity.MatchDetailActivity
import com.example.demoapplication.ui.adapter.MatchUserAdapter
import com.example.demoapplication.widgets.cardswipelayout.CardItemTouchHelperCallback
import com.example.demoapplication.widgets.cardswipelayout.CardLayoutManager
import com.example.demoapplication.widgets.cardswipelayout.OnSwipeListener
import com.kotlin.base.ui.fragment.BaseMvpFragment
import kotlinx.android.synthetic.main.fragment_match.*
import kotlinx.android.synthetic.main.item_match_user.view.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

/**
 * 匹配页面
 */
class MatchFragment : BaseMvpFragment<MatchPresenter>(), MatchView {
    override fun onGetMatchListResult(success: Boolean, matchBeans: MatchListBean?) {


    }

    //用户适配器
    private val matchUserAdapter: MatchUserAdapter by lazy { MatchUserAdapter(mutableListOf()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_match, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {
        //用户匹配
        matchUserRv.adapter = matchUserAdapter
        val cardCallback = CardItemTouchHelperCallback(matchUserRv.adapter!!, userList)
        val touchHelper = ItemTouchHelper(cardCallback)
        val cardLayoutManager = CardLayoutManager(matchUserRv, touchHelper)
        matchUserRv.layoutManager = cardLayoutManager
        touchHelper.attachToRecyclerView(matchUserRv)
        cardCallback.setOnSwipedListener(object : OnSwipeListener<MatchBean> {
            override fun onSwiping(viewHolder: RecyclerView.ViewHolder, ratio: Float, direction: Int) {
                val holder = viewHolder as BaseViewHolder
                viewHolder.itemView.alpha = 1 - Math.abs(ratio) * 0.2f
                if (direction == com.example.demoapplication.widgets.cardswipelayout.CardConfig.SWIPING_LEFT) {
                    holder.itemView.matchDislike.alpha = Math.abs(ratio)
                } else if (direction == com.example.demoapplication.widgets.cardswipelayout.CardConfig.SWIPING_RIGHT) {
                    holder.itemView.matchLike.alpha = Math.abs(ratio)
                } else {
                    holder.itemView.matchDislike.alpha = 0f
                    holder.itemView.matchLike.alpha = 0f
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, t: MatchBean, direction: Int) {
                val myHolder = viewHolder as BaseViewHolder
                viewHolder.itemView.alpha = 1f
                myHolder.itemView.matchDislike.alpha = 0f
                myHolder.itemView.matchLike.alpha = 0f
                toast(if (direction == com.example.demoapplication.widgets.cardswipelayout.CardConfig.SWIPED_LEFT) "left" else "right")
            }

            override fun onSwipedClear() {
                toast("data clear")
                matchUserRv.postDelayed({
                     initData()
                }, 3000L)
            }

        })

        matchUserAdapter.setOnItemClickListener { adapter, view, position ->
            toast("$position")
            startActivity<MatchDetailActivity>("matchBean" to adapter.data[position])
        }
        initData()
    }


    //用户数据源
    var userList: MutableList<MatchBean> = mutableListOf()



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
    }

}
