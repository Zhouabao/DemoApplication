package com.example.demoapplication.ui.activity

import android.os.Bundle
import com.example.demoapplication.R
import com.example.demoapplication.model.CommentDetailBean
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.presenter.SquareDetailPresenter
import com.example.demoapplication.presenter.view.SquareDetailView
import com.example.demoapplication.ui.CommentExpandAdapter
import com.example.demoapplication.ui.dialog.TranspondDialog
import com.kotlin.base.common.BaseApplication.Companion.context
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_square_detail.*

/**
 * 广场详情页 包含内容详情以及点赞评论信息
 */
class SquareDetailActivity : BaseMvpActivity<SquareDetailPresenter>(), SquareDetailView {
    //评论数据
    private var commentDatas: MutableList<CommentDetailBean> = mutableListOf()

    private val matchBean by lazy { intent.getSerializableExtra("matchbean") as MatchBean }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_square_detail)

        initView()
    }

    private fun initView() {
        mPresenter = SquareDetailPresenter()
        mPresenter.mView = this

        squareZhuanfaBtn.onClick {
            TranspondDialog(this).show()
        }

        val drawable1 =
            resources.getDrawable(if (matchBean.zan) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
        drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
        squareDianzanBtn.setCompoundDrawables(drawable1, null, null, null)
        squareDianzanBtn.onClick {
            matchBean.zan = !matchBean.zan
            val drawable1 =
                context.resources.getDrawable(if (matchBean.zan) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
            drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
            squareDianzanBtn.setCompoundDrawables(drawable1, null, null, null)
        }



        hotCommentList.setGroupIndicator(null)
        hotCommentList.setAdapter(CommentExpandAdapter(this, 2))
        for (i in 1..2) {
            hotCommentList.expandGroup(i)
        }

        allCommentList.setGroupIndicator(null)
        allCommentList.setAdapter(CommentExpandAdapter(this, 4))
        for (i in 1..4) {
            allCommentList.expandGroup(i)
        }
    }


}
