package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapplication.R
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.presenter.SquarePlayDetaiPresenter
import com.example.demoapplication.presenter.view.SquarePlayDetailView
import com.example.demoapplication.ui.adapter.MultiListDetailPlayAdapter
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_square_play_detail.*

/**
 * 点击图片、视频、录音进入详情页面，并且支持点击左右切换好友动态
 */
class SquarePlayDetailActivity : BaseMvpActivity<SquarePlayDetaiPresenter>(), SquarePlayDetailView {
    //广场列表内容适配器
    private val adapter by lazy { MultiListDetailPlayAdapter(this, mutableListOf()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_square_play_detail)

        initView()
        initData()
        currentIndex = userList.size / 2
        moveToPosition(layoutmanager, friendSquareList, currentIndex)
    }

    val layoutmanager by lazy {
        object : LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) {
            override fun canScrollHorizontally(): Boolean {
                return false
            }
        }
    }

    private fun initView() {
        mPresenter = SquarePlayDetaiPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        btnBack.onClick { finish() }

        friendSquareList.layoutManager = layoutmanager
        friendSquareList.adapter = adapter


        rvLast.onClick {
            if (currentIndex > 0) {
                currentIndex--
                Log.i("squareplaydetail", "$currentIndex")
            }
            if (currentIndex >= 0) {
                moveToPosition(layoutmanager, friendSquareList, currentIndex)
            }
        }

        rvNext.onClick {
            if (currentIndex < adapter.data.size) {
                currentIndex++
                Log.i("squareplaydetail", "$currentIndex")
            }
            if (currentIndex < adapter.data.size) {
                moveToPosition(layoutmanager, friendSquareList, currentIndex)
            }
        }


    }

    //好友信息用户数据源
    var userList: MutableList<MatchBean> = mutableListOf()

    private fun initData() {
        userList.add(
            MatchBean(
                "Lily",
                23,
                1,
                mutableListOf(
                    R.drawable.img_avatar_01,
                    R.drawable.img_avatar_02,
                    R.drawable.img_avatar_03,
                    R.drawable.img_avatar_04,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06,
                    R.drawable.img_avatar_07,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06
                ), 1, true
            )
        )
        userList.add(
            MatchBean(
                "Username",
                28,
                2,
                mutableListOf(
                    R.drawable.img_avatar_02,
                    R.drawable.img_avatar_03,
                    R.drawable.img_avatar_04,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06,
                    R.drawable.img_avatar_07,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06
                ), 3
            )
        )
        userList.add(
            MatchBean(
                "Shirly",
                24,
                2,
                mutableListOf(
                    R.drawable.img_avatar_03,
                    R.drawable.img_avatar_04,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06,
                    R.drawable.img_avatar_07,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06
                ), 3
            )
        )
        userList.add(
            MatchBean(
                "爱的魔力圈",
                19,
                1,
                mutableListOf(
                    R.drawable.img_avatar_04,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06,
                    R.drawable.img_avatar_07,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06
                ), 2
            )
        )
        userList.add(
            MatchBean(
                "Lily",
                23,
                1,
                mutableListOf(
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06,
                    R.drawable.img_avatar_07,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06
                ), 1
            )
        )
        userList.add(
            MatchBean(
                "Lily",
                23,
                1,
                mutableListOf(
                    R.drawable.img_avatar_01,
                    R.drawable.img_avatar_02,
                    R.drawable.img_avatar_03,
                    R.drawable.img_avatar_04,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06,
                    R.drawable.img_avatar_07,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06
                ), 3
            )
        )
        userList.add(
            MatchBean(
                "Username",
                28,
                2,
                mutableListOf(
                    R.drawable.img_avatar_02,
                    R.drawable.img_avatar_03,
                    R.drawable.img_avatar_04,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06,
                    R.drawable.img_avatar_07,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06
                ), 2
            )
        )
        userList.add(
            MatchBean(
                "Shirly",
                24,
                2,
                mutableListOf(
                    R.drawable.img_avatar_03,
                    R.drawable.img_avatar_04,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06,
                    R.drawable.img_avatar_07,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06
                ), 1
            )
        )
        userList.add(
            MatchBean(
                "爱的魔力圈",
                19,
                1,
                mutableListOf(
                    R.drawable.img_avatar_04,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06,
                    R.drawable.img_avatar_07,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06
                ), 2
            )
        )
        userList.add(
            MatchBean(
                "Lily",
                23,
                1,
                mutableListOf(
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06,
                    R.drawable.img_avatar_07,
                    R.drawable.img_avatar_05,
                    R.drawable.img_avatar_06
                ), 3
            )
        )
        adapter.addData(userList)

    }

    private var currentIndex = 0
    fun moveToPosition(manager: LinearLayoutManager, mRecyclerView: RecyclerView, n: Int) {
        val firstItem = manager.findFirstVisibleItemPosition()
        val lastItem = manager.findLastVisibleItemPosition()
        if (n <= firstItem) {
            mRecyclerView.scrollToPosition(n)
        } else if (n <= lastItem) {
            val top = mRecyclerView.getChildAt(n - firstItem).getTop()
            mRecyclerView.scrollBy(0, top)
        } else {
            mRecyclerView.scrollToPosition(n)
        }
    }


    /**
     * 目标项是否在最后一个可见项之后
     */
    private var mShouldScroll: Boolean = false
    /**
     * 记录目标项位置
     */
    private var mToPosition: Int = 0

    /**
     * 滑动到指定位置
     *
     * @param mRecyclerView
     * @param position
     */
    private fun smoothMoveToPosition(mRecyclerView: RecyclerView, position: Int) {
        // 第一个可见位置
        val firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0))
        // 最后一个可见位置
        val lastItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(mRecyclerView.childCount - 1))

        if (position < firstItem) {
            // 如果跳转位置在第一个可见位置之前，就smoothScrollToPosition可以直接跳转
            mRecyclerView.smoothScrollToPosition(position)
        } else if (position <= lastItem) {
            // 跳转位置在第一个可见项之后，最后一个可见项之前
            // smoothScrollToPosition根本不会动，此时调用smoothScrollBy来滑动到指定位置
            val movePosition = position - firstItem
            if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
                val top = mRecyclerView.getChildAt(movePosition).top
                mRecyclerView.smoothScrollBy(0, top)
            }
        } else {
            // 如果要跳转的位置在最后可见项之后，则先调用smoothScrollToPosition将要跳转的位置滚动到可见位置
            // 再通过onScrollStateChanged控制再次调用smoothMoveToPosition，执行上一个判断中的方法
            mRecyclerView.smoothScrollToPosition(position)
            mToPosition = position
            mShouldScroll = true
        }
    }


}
