package com.example.demoapplication.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demoapplication.R
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.presenter.SquarePlayDetaiPresenter
import com.example.demoapplication.presenter.view.SquarePlayDetailView
import com.example.demoapplication.ui.adapter.MultiListDetailPlayAdapter
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

    }

    private fun initView() {
        mPresenter = SquarePlayDetaiPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        val layoutmanager = object : LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) {
            override fun canScrollHorizontally(): Boolean {
                return false
            }
        }
        friendSquareList.layoutManager = layoutmanager
        friendSquareList.adapter = adapter

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
}
