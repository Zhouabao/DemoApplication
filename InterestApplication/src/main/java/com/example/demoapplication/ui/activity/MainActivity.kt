package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.demoapplication.R
import com.example.demoapplication.ui.fragment.MatchFragment
import com.example.demoapplication.ui.fragment.SquareFragment
import com.google.android.material.tabs.TabLayout
import com.kotlin.base.ui.activity.BaseActivity
import com.luck.picture.lib.rxbus2.RxBus
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

//在支持路由的页面上添加注解（必选）
//这里的路径需要注意的是至少需要两级,/xx/xx
//路径标签个人建议写在一个类里面，方便统一管理和维护

class MainActivity : BaseActivity(), View.OnClickListener {


    //获取Arouter传递过来的参数
//    @Autowired(name = "key")
//    lateinit var value:String

    //fragment栈管理
    private val mStack = Stack<Fragment>()
    //匹配
    private val matchFragment by lazy { MatchFragment() }
    //广场
    private val squareFragment by lazy { SquareFragment() }

    var fragments: MutableList<Fragment> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RxBus.getDefault().register(this)
        initView()
        initFragment()
        filterBtn.setOnClickListener(this)
    }

    private fun initView() {
        filterBtn.setOnClickListener(this)
        notificationBtn.setOnClickListener(this)
    }


    /*
      初始化Fragment栈管理
   */
    private fun initFragment() {
        val manager = supportFragmentManager.beginTransaction()
        manager.add(R.id.content, matchFragment)
        manager.add(R.id.content, squareFragment)
        manager.commit()
        mStack.add(matchFragment)
        mStack.add(squareFragment)

        tabMain.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                changeFragment(tab.position)
            }

        })
        tabMain.addTab(tabMain.newTab().setText("匹配"), true)
        tabMain.addTab(tabMain.newTab().setText("广场"))

    }


    /*
      切换Tab，切换对应的Fragment
   */
    private fun changeFragment(position: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        for (fragment in mStack) {
            transaction.hide(fragment)
        }
        transaction.show(mStack[position])
        transaction.commit()
    }


    /**
     * 方法为改变fragment之后要同步更新右上角的图标
     */
    fun changeMainStatus() {

    }


    //TODO("联动") https://blog.csdn.net/YANGWEIQIAO/article/details/78959968
    override fun onClick(view: View) {
        when (view.id) {
            R.id.filterBtn -> {
                    matchFragment.showFilterDialog()
            }
            R.id.notificationBtn -> {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RxBus.getDefault().unregister(this)
    }

}
