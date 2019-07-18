package com.example.demoapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapplication.R
import com.example.demoapplication.model.SquareBean
import com.example.demoapplication.ui.activity.SquareCommentDetailActivity
import com.example.demoapplication.ui.adapter.MultiListSquareAdapter
import kotlinx.android.synthetic.main.fragment_list_square.*
import kotlinx.android.synthetic.main.item_list_square_pic.*
import org.jetbrains.anko.support.v4.startActivity


/**
 * 列表形式的广场列表
 */
class ListSquareFragment : Fragment() {
    private val listSquareAdapter by lazy { MultiListSquareAdapter( userList) }

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
        listSquareAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view == squareUserPics1)
                startActivity<SquareCommentDetailActivity>()
        }
        initData()
    }


    //用户数据源
    var userList: MutableList<SquareBean> = mutableListOf()

    private fun initData() {

        listSquareAdapter.addData(userList)

    }

}
