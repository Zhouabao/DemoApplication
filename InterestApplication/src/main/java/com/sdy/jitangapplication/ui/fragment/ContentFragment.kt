package com.sdy.jitangapplication.ui.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SquareTitleBean
import com.sdy.jitangapplication.presenter.ContentPresenter
import com.sdy.jitangapplication.presenter.view.ContentView
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.adapter.SquareSwitchAdapter
import kotlinx.android.synthetic.main.fragment_content.*
import java.util.*

/**
 * 内容页面
 */
class ContentFragment : BaseMvpLazyLoadFragment<ContentPresenter>(), ContentView {
    override fun loadData() {
        initView()

    }

    private fun initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val param = customStatusBar.layoutParams as LinearLayout.LayoutParams
            param.height = BarUtils.getStatusBarHeight()
        } else {
            customStatusBar.isVisible = false
        }

        initFragments()
        initViewpager()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_content, container, false)
    }


    //fragment栈管理
    private val mStack = Stack<Fragment>()
    val titleAdapter by lazy { SquareSwitchAdapter() }


    private fun initFragments() {
        titleAdapter.addData(SquareTitleBean("好友", true))
        titleAdapter.addData(SquareTitleBean("推荐", false))
        titleAdapter.addData(SquareTitleBean("兴趣", false))
        titleAdapter.addData(SquareTitleBean("附近", false))

        mStack.add(SquareFragment())
        mStack.add(RecommendSquareFragment())
        mStack.add(TagSquareFragment())
        mStack.add(RecommendSquareFragment())
    }

    private fun initViewpager() {
        squareVp.setScrollable(false)
        squareVp.adapter = MainPagerAdapter(activity!!.supportFragmentManager, mStack)
        squareVp.currentItem = 0
        squareVp.offscreenPageLimit = 4

        rgSquare.layoutManager = LinearLayoutManager(activity!!, RecyclerView.HORIZONTAL, false)
        rgSquare.adapter = titleAdapter
        titleAdapter.setOnItemClickListener { _, view, position ->
            for (data in titleAdapter.data) {
                data.checked = data == titleAdapter.data[position]
                squareVp.currentItem = position
            }
            filterGenderBtn.isVisible = position != 2
            titleAdapter.notifyDataSetChanged()
        }

    }

}
