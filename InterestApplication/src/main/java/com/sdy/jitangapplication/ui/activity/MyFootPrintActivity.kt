package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.NotifyEvent
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.fragment.MyCollectionAndLikeFragment
import com.sdy.jitangapplication.ui.fragment.MyCommentFragment
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_my_foot_print.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * 我的足迹：我的点赞 、评论、收藏
 */
class MyFootPrintActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_foot_print)
        initView()
        initFragment()
    }

    private fun initView() {
        hotT1.text = getString(R.string.my_foot_print)
        btnBack.onClick {
            finish()
        }
        divider.isVisible = false
    }


    //fragment栈管理
    private val mStack = Stack<Fragment>()
    private val titles by lazy { arrayOf(
        getString(R.string.tab_zan), getString(R.string.tab_comment), getString(
            R.string.tab_collect
        )
    ) }


    /*
      初始化Fragment栈管理
   */
    private fun initFragment() {
        mStack.add(MyCollectionAndLikeFragment(MyCollectionAndLikeFragment.TYPE_LIKE))   //我的点赞
        mStack.add(MyCommentFragment())   //我的评论
        mStack.add(MyCollectionAndLikeFragment(MyCollectionAndLikeFragment.TYPE_COLLECT))//我的收藏
//        mStack.add(MyLikedFragment())//我喜欢的
        vpMyFootPrint.adapter = MainPagerAdapter(supportFragmentManager, mStack, titles)
        vpMyFootPrint.offscreenPageLimit = 3

        tabMyFootprint.setViewPager(vpMyFootPrint, titles)
        vpMyFootPrint.currentItem = 0
    }


    override fun finish() {
        setResult(Activity.RESULT_OK, intent)
        super.finish()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == SquarePlayDetailActivity.REQUEST_CODE) {
                EventBus.getDefault()
                    .post(
                        NotifyEvent(
                            data!!.getIntExtra("position", -1),
                            data!!.getIntExtra("type", 0)
                        )
                    )
            }
    }

}
