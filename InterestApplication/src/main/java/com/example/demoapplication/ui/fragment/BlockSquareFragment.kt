package com.example.demoapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapplication.R
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.ui.adapter.BlockAdapter
import kotlinx.android.synthetic.main.fragment_block_square.*

/**
 * 九宫格的广场内容
 */
class BlockSquareFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_block_square, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    private val blockAdapter by lazy { BlockAdapter(activity?.applicationContext!!) }

    private fun initView() {
        blockRv.layoutManager = GridLayoutManager(activity, 3, RecyclerView.VERTICAL, false)
        blockRv.adapter = blockAdapter

    }


    //用户数据源
    var userList: MutableList<MatchBean> = mutableListOf()

    private fun initData() {
        blockAdapter.setData(
            mutableListOf(
                R.drawable.img_avatar_05,
                R.drawable.img_avatar_06,
                R.drawable.img_avatar_07,
                R.drawable.img_avatar_05,
                R.drawable.img_avatar_06
            )
        )
    }

}
