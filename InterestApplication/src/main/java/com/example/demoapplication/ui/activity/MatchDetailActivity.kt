package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.view.size
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
import com.example.demoapplication.ui.adapter.MatchDetailLabelAdapter
import com.example.demoapplication.ui.adapter.MatchImgsAdapter
import com.example.demoapplication.widgets.ObservableScrollView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_match_detail.*
import kotlinx.android.synthetic.main.match_action_layout.*
import org.jetbrains.anko.toast

class MatchDetailActivity : BaseMvpActivity<MatchDetailPresenter>(), MatchDetailView {
    private val matchBean by lazy { intent.getSerializableExtra("matchBean") as MatchBean }
    private val thumbAdapter by lazy { DetailThumbAdapter(this) }

    var photos: MutableList<Int> = mutableListOf()
    private val photosAdapter by lazy { MatchImgsAdapter(this, photos) }

    private val labelsAdapter by lazy { MatchDetailLabelAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_detail)

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
        btnDislike.visibility = View.GONE

        //用户的广场预览界面
        detailThumbRv.layoutManager = LinearLayoutManager(this, LinearLayout.HORIZONTAL, false)
        //用户标签
        val manager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        detailLabelRv.layoutManager = manager


        detailSquareSwitchRg.setOnCheckedChangeListener { radioGroup, checkedId ->

            if (checkedId == R.id.rbList) {
                toast("列表形式")
            } else if (checkedId == R.id.rbBlock) {
                toast("九宫格形式")
            }
        }

        //设置根布局的滑动事件监听，如果滑出屏幕高度的1/3，就将按钮浮动在底部
        detailScrollView.setOnScrollViewChangedListener(object : ObservableScrollView.OnScrollChangedListener {
            override fun onScrollChanged(scrollView: ObservableScrollView, x: Int, y: Int, oldX: Int, oldY: Int) {
                Log.i("scrollviewTag", "x = $x, y = $y  ;  oldX = $oldX, oldY = $oldY")
                Log.i("scrollviewTag", "${ScreenUtils.getScreenHeight()}")
            }
        })
    }
}
