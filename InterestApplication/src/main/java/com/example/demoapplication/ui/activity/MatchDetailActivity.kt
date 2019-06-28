package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.view.size
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.example.demoapplication.R
import com.example.demoapplication.model.Label
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.presenter.MatchDetailPresenter
import com.example.demoapplication.presenter.view.MatchDetailView
import com.example.demoapplication.ui.adapter.DetailThumbAdapter
import com.example.demoapplication.ui.adapter.MatchDetailImgsAdapter
import com.example.demoapplication.ui.adapter.MatchDetailLabelAdapter
import com.example.demoapplication.ui.chat.MatchSucceedActivity
import com.example.demoapplication.ui.dialog.ChargeVipDialog
import com.example.demoapplication.ui.fragment.BlockSquareFragment
import com.example.demoapplication.ui.fragment.ListSquareFragment
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_match_detail.*
import kotlinx.android.synthetic.main.match_action_layout.*
import org.jetbrains.anko.startActivity
import java.util.*

/**
 * 匹配详情页
 */
class MatchDetailActivity : BaseMvpActivity<MatchDetailPresenter>(), MatchDetailView {
    private val matchBean by lazy { intent.getSerializableExtra("matchBean") as MatchBean }
    private val thumbAdapter by lazy { DetailThumbAdapter(this) }

    var photos: MutableList<Int> = mutableListOf()
    private val photosAdapter by lazy { MatchDetailImgsAdapter(this, photos) }

    private val labelsAdapter by lazy { MatchDetailLabelAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_detail1)

        initView()

        initData()
    }

    private fun initData() {
        detailUserName.text = matchBean.name
        detailUserInfo.text = "${matchBean.age} / ${if (matchBean.sex == 1) "男" else "女"} / android开发"
        detailUserSign.text =
            "Suppressing notification from package com.qihoo360.mobilesafe by user request, and Notification.FLAG = 98, and when = 1561516865824"

        detailThumbRv.adapter = thumbAdapter
        thumbAdapter.setData(
            mutableListOf(
                R.drawable.img_avatar_01,
                R.drawable.img_avatar_01,
                R.drawable.img_avatar_01,
                R.drawable.img_avatar_01,
                R.drawable.img_avatar_01
            )
        )

        detailPhotosVp.adapter = photosAdapter
        photos.addAll(matchBean.imgs)
        photosAdapter.notifyDataSetChanged()
        setViewpagerAndIndicator()


        detailLabelRv.adapter = labelsAdapter
        labelsAdapter.setData(
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


    }

    /**
     * 设置竖直滑动的vp2以及其滑动的indicator
     */
    private fun setViewpagerAndIndicator() {
        detailPhotosVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                for (i in 0 until detailPhotosIndicator.size) {
                    (detailPhotosIndicator[i] as RadioButton).isChecked = i == position
                }
            }
        })

        if (photos.size > 1) {
            for (i in 0 until photos.size) {
                val indicator = RadioButton(this)
                indicator.width = SizeUtils.dp2px(10F)
                indicator.height = SizeUtils.dp2px(10F)
                indicator.buttonDrawable = null
                indicator.background = resources.getDrawable(R.drawable.selector_circle_indicator)

                indicator.layoutParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val layoutParams: LinearLayout.LayoutParams = indicator.layoutParams as LinearLayout.LayoutParams
                layoutParams.setMargins(0, SizeUtils.dp2px(6f), 0, 0)
                indicator.layoutParams = layoutParams

                indicator.isChecked = i == 0
                detailPhotosIndicator.addView(indicator)
            }
        }
    }


    private fun initView() {
        backBtn.onClick { finish() }

        //设置图片的宽度占满屏幕，宽高比3:4
        val layoutParams = detailPhotosVp.layoutParams
        layoutParams.width = ScreenUtils.getScreenWidth()
        layoutParams.height = (4 / 3.0F * layoutParams.width).toInt()
        detailPhotosVp.layoutParams = layoutParams

        btnDislike.visibility = View.GONE
        btnLike.onClick {
            startActivity<MatchSucceedActivity>()
        }
        btnChat.onClick {
            val dialog = ChargeVipDialog(this)
            dialog.show()
        }



        //用户的广场预览界面
        detailThumbRv.layoutManager = LinearLayoutManager(this, LinearLayout.HORIZONTAL, false)
        //用户标签
        val manager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        detailLabelRv.layoutManager = manager

        initFragment()
        detailSquareSwitchRg.setOnCheckedChangeListener { radioGroup, checkedId ->

            if (checkedId == R.id.rbList) {
                changeFragment(1)
            } else if (checkedId == R.id.rbBlock) {
                changeFragment(0)
            }
        }

        //设置根布局的滑动事件监听，如果滑出屏幕高度的1/3，就将按钮浮动在底部
        detailScrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            Log.i("scrollviewTag", "x = $scrollX, y = $scrollY  ;  oldX = $oldScrollX, oldY = $oldScrollY")
            Log.i("scrollviewTag", "${ScreenUtils.getScreenHeight()}")
        }


    }

    //fragment栈管理
    private val mStack = Stack<Fragment>()
    //九宫格
    private val blockFragment by lazy { BlockSquareFragment() }
    //列表
    private val listFragment by lazy { ListSquareFragment() }

    /**
     * 初始化fragments
     */
    private fun initFragment() {
        val manager = supportFragmentManager.beginTransaction()
        manager.add(R.id.detail_content_fragment, blockFragment)
        manager.add(R.id.detail_content_fragment, listFragment)
        manager.commit()
        mStack.add(blockFragment)
        mStack.add(listFragment)
    }

    /**
     * 点击切换fragment
     */
    private fun changeFragment(position: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        for (fragment in mStack) {
            transaction.hide(fragment)
        }
        transaction.show(mStack[position])
        transaction.commit()
    }
}
