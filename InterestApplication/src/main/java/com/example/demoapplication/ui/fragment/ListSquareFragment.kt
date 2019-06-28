package com.example.demoapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapplication.R
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.ui.adapter.MultiListSquareAdapter
import kotlinx.android.synthetic.main.fragment_list_square.*


/**
 * 列表形式的广场列表
 */
class ListSquareFragment : Fragment() {
    private val listSquareAdapter by lazy { MultiListSquareAdapter(activity!!, userList) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_square, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val manager1 = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
//        val manager1 = MyLinearLayoutManager(activity, RecyclerView.VERTICAL, false)
//        manager1.setScrollEnabled(false)
        listSquareRv.isNestedScrollingEnabled = false
        listSquareRv.layoutManager = manager1
        listSquareRv.adapter = listSquareAdapter

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
                    R.drawable.img_avatar_01,
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
                ), 2
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
                ), 3
            )
        )
        userList.add(
            MatchBean(
                "Lily",
                23,
                1,
                mutableListOf(
                    R.drawable.img_avatar_03
                ), 1
            )
        )

        listSquareAdapter.addData(userList)

    }

}
