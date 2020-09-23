package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.fragment.MyTodayVisitFragment
import com.sdy.jitangapplication.ui.fragment.MyVisitFragment
import kotlinx.android.synthetic.main.activity_my_visit.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import java.util.*

/**
 * 我的访客
 */
class MyVisitActivity : BaseActivity() {
    private val from by lazy { intent.getIntExtra("from", FROM_ME) }

    companion object {
        const val FROM_ME = 1
        const val FROM_TOP_RECOMMEND = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_visit)
        initView()
        initFragment()
    }

    private fun initView() {
        hotT1.text = "看过我的"
        btnBack.onClick {
            finish()
        }
        divider.isVisible = false
    }


    //fragment栈管理
    private val mStack = Stack<Fragment>()
    private val titles by lazy {
        if (from == FROM_TOP_RECOMMEND) arrayOf(
            "今天来过${intent.getIntExtra(
                "today",
                0
            )}", "所有访客"
        ) else {
            arrayOf("所有访客")
        }
    }


    /*
      初始化Fragment栈管理
      view.visitTodayCount.text = "今日来访：${intent.getIntExtra("today", 0)}"
view.visitAllCount.text = "总来访：${intent.getIntExtra("all", 0)}"
intent.getBooleanExtra("freeShow", false)
   */
    private fun initFragment() {

        if (from == FROM_TOP_RECOMMEND)
            mStack.add(
                MyTodayVisitFragment(
                    intent.getIntExtra("todayExplosure", 0),
                    intent.getBooleanExtra("freeShow", false)
                )
            )
        //今日来访
        mStack.add(
            MyVisitFragment(
                intent.getBooleanExtra("freeShow", false),
                from,
                intent.getIntExtra("today", 0),
                intent.getIntExtra("all", 0)
            )
        )
        //所有来访
        vpMyVisit.adapter = MainPagerAdapter(supportFragmentManager, mStack, titles)
        if (from == FROM_TOP_RECOMMEND) {
            vpMyVisit.offscreenPageLimit = 2
            tabMyVisit.setViewPager(vpMyVisit,titles)
            tabMyVisit.isVisible = true
        } else {
            vpMyVisit.offscreenPageLimit = 1
            tabMyVisit.isVisible = false
        }
        vpMyVisit.currentItem = 0
    }


}

