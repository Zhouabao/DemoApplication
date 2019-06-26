package com.example.demoapplication.ui.activity

import android.animation.Animator
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.demoapplication.R
import com.example.demoapplication.event.NewMsgEvent
import com.example.demoapplication.presenter.MainPresenter
import com.example.demoapplication.presenter.view.MainView
import com.example.demoapplication.ui.fragment.MatchFragment
import com.example.demoapplication.ui.fragment.SquareFragment
import com.example.demoapplication.widgets.FilterUserDialog
import com.google.android.material.tabs.TabLayout
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_match_filter.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

//在支持路由的页面上添加注解（必选）
//这里的路径需要注意的是至少需要两级,/xx/xx
//路径标签个人建议写在一个类里面，方便统一管理和维护

class MainActivity : BaseMvpActivity<MainPresenter>(), MainView, View.OnClickListener {

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

        EventBus.getDefault().register(this)
        initView()
        initFragment()
        filterBtn.setOnClickListener(this)
    }

    private fun initView() {
        filterBtn.setOnClickListener(this)
        notificationBtn.setOnClickListener(this)
        mPresenter = MainPresenter()
        mPresenter.mView = this
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

    //筛选对话框
    private val filterUserDialog: FilterUserDialog by lazy { FilterUserDialog(this) }


    /**
     * 展示筛选条件对话框
     */
    private fun showFilterDialog() {
        filterUserDialog.show()
        filterUserDialog.btnCompleteFilter.onClick {
            //TODO("发起网络请求，并关闭对话框")

        }
        filterUserDialog.seekBarAge.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

            override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
                filterUserDialog.filterAge.text = "${leftValue.toInt()}-${rightValue.toInt()}岁"
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }

        })

    }


    //TODO("联动") https://blog.csdn.net/YANGWEIQIAO/article/details/78959968
    override fun onClick(view: View) {
        when (view.id) {
            R.id.filterBtn -> {
                showFilterDialog()
            }
            R.id.notificationBtn -> {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().post(NewMsgEvent(21, 6, 3, 2))
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onUpdateFilterResult() {

        if (filterUserDialog.isShowing)
            filterUserDialog.dismiss()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewMsgEvent(event: NewMsgEvent) {
        msgLike.text = event.likeCount.toString()
        msgHi.text = event.HiCount.toString()
        msgChat.text = event.chatCount.toString()
        msgSquare.text = event.squareCount.toString()
        llMsgCount.visibility = View.VISIBLE
        YoYo.with(Techniques.Bounce)
            .duration(2000)
            .withListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    llMsgCount.visibility = View.GONE
                }

            })
            .delay(1000)
            .playOn(llMsgCount)


    }
}
