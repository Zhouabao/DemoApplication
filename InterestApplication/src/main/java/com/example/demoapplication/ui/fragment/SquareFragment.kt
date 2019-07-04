package com.example.demoapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapplication.R
import com.example.demoapplication.model.Label
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.presenter.SquarePresenter
import com.example.demoapplication.presenter.view.SquareView
import com.example.demoapplication.ui.activity.SquareDetailActivity
import com.example.demoapplication.ui.adapter.MatchLabelAdapter
import com.example.demoapplication.ui.adapter.MultiListSquareAdapter
import com.example.demoapplication.ui.adapter.SquareFriendsAdapter
import com.kotlin.base.ui.fragment.BaseMvpFragment
import kotlinx.android.synthetic.main.fragment_square.*
import kotlinx.android.synthetic.main.headerview_label.view.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

/**
 * 广场列表
 *
 */
class SquareFragment : BaseMvpFragment<SquarePresenter>(), SquareView {
    //广场列表内容适配器
    private val adapter by lazy { MultiListSquareAdapter(activity!!, mutableListOf()) }
    //标签适配器
    private val labelAdapter: MatchLabelAdapter by lazy { MatchLabelAdapter(context!!) }
    //广场好友适配器
    private val friendsAdapter: SquareFriendsAdapter by lazy { SquareFriendsAdapter(userList) }
    //好友信息用户数据源
    var userList: MutableList<MatchBean> = mutableListOf()
    //标签数据源
    var labelList: MutableList<Label> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_square, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initData()
//
//        tv1.onClick {
//            RetrofitFactory.instance.create(Api::class.java)
//                .getFileFromNet("http://rsrc1.futrueredland.com.cn/safty/ppsns")
//                .excute(object : BaseSubscriber<ResponseBody>(mPresenter.mView) {
//                    override fun onNext(t: ResponseBody) {
//                        super.onNext(t)
//                        Log.i("tag", t.string())
//                    }
//                })
//        }
//    }

        squareDynamicRv.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
//                if (GSYVideoManager.instance().playPosition >= 0) {
//                    val position = GSYVideoManager.instance().playPosition
//
//                }
            }
        })

    }


    private fun initView() {
        mPresenter = SquarePresenter()
        mPresenter.mView = this
        squareDynamicRv.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        squareDynamicRv.adapter = adapter
        adapter.addHeaderView(initLabelView(), 0)
        adapter.addHeaderView(initFriendsView(), 1)
        adapter.setOnItemClickListener { adapter, view, position ->
            startActivity<SquareDetailActivity>("matchbean" to adapter.data[position])
        }


    }


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
                ), 1,true
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
                ), 3
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
                ), 3
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
                ), 2
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
                ), 1
            )
        )
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
                ), 3
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
                ), 2
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
                ), 1
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
                ), 2
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
                ), 3
            )
        )
        adapter.addData(userList)

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


    //创建标签布局
    fun initLabelView(): View {
        val labelView = LayoutInflater.from(activity!!).inflate(R.layout.headerview_label, squareDynamicRv, false)
        labelView.friendTv.visibility = View.GONE

        val labelManager = LinearLayoutManager(activity?.applicationContext, LinearLayoutManager.HORIZONTAL, false)
        labelView.headRv.layoutManager = labelManager
        labelView.headRv.adapter = labelAdapter
        labelAdapter.dataList = labelList
        labelAdapter.setOnItemClickListener(object : MatchLabelAdapter.OnItemClickListener {
            override fun onItemClick(item: View, position: Int) {
                toast("$position")
            }

        })

        return labelView
    }

    //创建好友布局
    fun initFriendsView(): View {
        val friendsView = LayoutInflater.from(activity!!).inflate(R.layout.headerview_label, squareDynamicRv, false)

        val linearLayoutManager =
            LinearLayoutManager(activity?.applicationContext, LinearLayoutManager.HORIZONTAL, false)
        friendsView.headRv.layoutManager = linearLayoutManager
        friendsView.headRv.adapter = friendsAdapter
        friendsAdapter.addData(userList)
        friendsAdapter.setOnItemClickListener { adapter, view, position ->

            toast("${adapter.data[position]}")
        }

        return friendsView
    }


    override fun onPause() {
        super.onPause()
    }
}

