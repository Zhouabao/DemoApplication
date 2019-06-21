package com.example.demoapplication.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.example.demoapplication.R
import com.example.demoapplication.ui.fragment.MatchFragment
import java.util.*

//在支持路由的页面上添加注解（必选）
//这里的路径需要注意的是至少需要两级,/xx/xx
//路径标签个人建议写在一个类里面，方便统一管理和维护

@Route(path = "/activity/MainActivity")
class MainActivity : AppCompatActivity() {
    //获取Arouter传递过来的参数
//    @Autowired(name = "key")
//    lateinit var value:String

    //fragment栈管理
    private val mStack = Stack<Fragment>()
    //匹配
    private val matchFragment by lazy { MatchFragment() }
    //广场
    private val squareFragment by lazy { MatchFragment() }

    var fragments: MutableList<Fragment> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ARouter.getInstance().inject(this)
        initFragment()
        changeFragment(0)
    }


    /*
      初始化Fragment栈管理
   */
    private fun initFragment() {
        val manager = supportFragmentManager.beginTransaction()
        manager.add(R.id.content,matchFragment)
        manager.add(R.id.content,squareFragment)
        manager.commit()
        mStack.add(matchFragment)
        mStack.add(squareFragment)
    }


    /*
      切换Tab，切换对应的Fragment
   */
    private fun changeFragment(position: Int) {
        val manager = supportFragmentManager.beginTransaction()
        for (fragment in mStack){
            manager.hide(fragment)
        }

        manager.show(mStack[position])
        manager.commit()
    }

}
