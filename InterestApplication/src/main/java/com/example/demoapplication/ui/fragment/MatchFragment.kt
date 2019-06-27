package com.example.demoapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapplication.R
import com.example.demoapplication.model.Label
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.presenter.MatchPresenter
import com.example.demoapplication.presenter.view.MatchView
import com.example.demoapplication.ui.activity.LabelsActivity
import com.example.demoapplication.ui.activity.MatchDetailActivity
import com.example.demoapplication.ui.adapter.MatchLabelAdapter
import com.example.demoapplication.ui.adapter.MatchUserAdapter
import com.example.demoapplication.widgets.cardswipelayout.CardConfig
import com.example.demoapplication.widgets.cardswipelayout.CardItemTouchHelperCallback
import com.example.demoapplication.widgets.cardswipelayout.CardLayoutManager
import com.example.demoapplication.widgets.cardswipelayout.OnSwipeListener
import com.kotlin.base.ui.adapter.BaseRecyclerViewAdapter
import com.kotlin.base.ui.fragment.BaseMvpFragment
import kotlinx.android.synthetic.main.fragment_match.*
import kotlinx.android.synthetic.main.item_match_user.view.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

/**
 * 匹配页面
 */
class MatchFragment : BaseMvpFragment<MatchPresenter>(), MatchView {


    //标签适配器
    private val labelAdapter: MatchLabelAdapter by lazy { MatchLabelAdapter(context!!) }
    //用户适配器
    private val userAdapter: MatchUserAdapter by lazy { MatchUserAdapter(context!!) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_match, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initData()
    }

    private fun initView() {

        //标签管理
        val labelManager = LinearLayoutManager(activity?.applicationContext, LinearLayoutManager.HORIZONTAL, false)
        matchLabelRv.layoutManager = labelManager
        matchLabelRv.adapter = labelAdapter
        labelAdapter.dataList = labelList
        labelAdapter.setOnItemClickListener(object : MatchLabelAdapter.OnItemClickListener {
            override fun onItemClick(itemView: View, position: Int) {
                if (position == 0) {
                    //todo("跳转到标签选择页面")
                    startActivity<LabelsActivity>()
                } else {
                    //TODO("修改标签的选中状态并且要更新此时的用户数据")
                    for (i in 0 until labelAdapter.dataList.size) {
                        labelAdapter.dataList[i].checked = i == position - 1
                    }
                    labelAdapter.notifyDataSetChanged()
                }
            }
        })

        //用户匹配
        matchUserRv.adapter = userAdapter
        val cardCallback = CardItemTouchHelperCallback(matchUserRv.adapter!!, userList)
        val touchHelper = ItemTouchHelper(cardCallback)
        val cardLayoutManager = CardLayoutManager(matchUserRv, touchHelper)
        matchUserRv.layoutManager = cardLayoutManager
        touchHelper.attachToRecyclerView(matchUserRv)
        cardCallback.setOnSwipedListener(object : OnSwipeListener<MatchBean> {
            override fun onSwiping(viewHolder: RecyclerView.ViewHolder, ratio: Float, direction: Int) {
                val holder = viewHolder as MatchUserAdapter.ViewHolder
                viewHolder.itemView.alpha = 1 - Math.abs(ratio) * 0.2f
                if (direction == CardConfig.SWIPING_LEFT) {
                    holder.itemView.iv_dislike.alpha = Math.abs(ratio)
                } else if (direction == CardConfig.SWIPING_RIGHT) {
                    holder.itemView.iv_like.alpha = Math.abs(ratio)
                } else {
                    holder.itemView.iv_dislike.alpha = 0f
                    holder.itemView.iv_like.alpha = 0f
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, t: MatchBean, direction: Int) {
                val myHolder = viewHolder as MatchUserAdapter.ViewHolder
                viewHolder.itemView.alpha = 1f
                myHolder.itemView.iv_dislike.alpha = 0f
                myHolder.itemView.iv_like.alpha = 0f
                toast(if (direction == CardConfig.SWIPED_LEFT) "left" else "right")
            }

            override fun onSwipedClear() {
                toast("data clear")
                matchUserRv.postDelayed({
                    // initData()
                }, 3000L)
            }

        })

        userAdapter.setOnItemClickListener(object : BaseRecyclerViewAdapter.OnItemClickListener<MatchBean> {
            override fun onItemClick(item: MatchBean, position: Int) {
                toast("$position")
                startActivity<MatchDetailActivity>("matchBean" to item)
            }
        })
    }


    //用户数据源
    var userList: MutableList<MatchBean> = mutableListOf()
    //标签数据源
    var labelList: MutableList<Label> = mutableListOf()

    private fun initData() {
        userList.add(
            MatchBean(
                "Lily",
                23,
                1,
                mutableListOf(
                    R.drawable.img_avatar_01,
                    R.drawable.img_avatar_02,
                    R.drawable.img_avatar_03,
                    R.drawable.img_avatar_04,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06,
                    R.drawable.img_avatar_07,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06
                )
            )
        )
        userList.add(
            MatchBean(
                "Username",
                28,
                2,
                mutableListOf(
                    R.drawable.img_avatar_02,
                    R.drawable.img_avatar_03,
                    R.drawable.img_avatar_04,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06,
                    R.drawable.img_avatar_07,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06
                )
            )
        )
        userList.add(
            MatchBean(
                "Shirly",
                24,
                2,
                mutableListOf(
                    R.drawable.img_avatar_03,
                    R.drawable.img_avatar_04,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06,
                    R.drawable.img_avatar_07,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06
                )
            )
        )
        userList.add(
            MatchBean(
                "爱的魔力圈",
                19,
                1,
                mutableListOf(
                    R.drawable.img_avatar_04,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06,
                    R.drawable.img_avatar_07,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06
                )
            )
        )
        userList.add(
            MatchBean(
                "Lily",
                23,
                1,
                mutableListOf(
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06,
                    R.drawable.img_avatar_07,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06
                )
            )
        )

        userAdapter.setData(userList)

        labelList.addAll(
            mutableListOf(
                Label("精选", checked = true, parId = 0),
                Label("PlayStation", checked = false, parId = 1),
                Label("游戏", checked = false, parId = 2),
                Label("精选", checked = false, parId = 0),
                Label("PlayStation", checked = false, parId = 1),
                Label("游戏", checked = false, parId = 2),
                Label("精选", checked = false, parId = 0),
                Label("PlayStation", checked = false, parId = 1),
                Label("游戏", checked = false, parId = 2),
                Label("精选", checked = false, parId = 0),
                Label("PlayStation", checked = false, parId = 1),
                Label("游戏", checked = false, parId = 2),
                Label("精选", checked = false, parId = 0),
                Label("PlayStation", checked = false, parId = 1),
                Label("游戏", checked = false, parId = 2)
            )
        )
        labelAdapter.setData(labelList)
    }


    override fun onGetNewUserFromLabelResult(users: String) {

    }


}
