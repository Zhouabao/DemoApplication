package com.sdy.jitangapplication.ui.adapter

import android.app.Activity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.TopicBean
import com.sdy.jitangapplication.ui.activity.SquareSamePersonActivity
import kotlinx.android.synthetic.main.item_title_more.view.*
import org.jetbrains.anko.startActivity

/**
 * 更多标题
 */
class AllTitleAdapter(var spancount: Int = 5) :
    BaseQuickAdapter<TopicBean, BaseViewHolder>(R.layout.item_title_more) {

    override fun convert(helper: BaseViewHolder, item: TopicBean) {
        if (spancount == 3) {
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).width = SizeUtils.dp2px(180F)
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).leftMargin =0
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).rightMargin = 0

            (helper.itemView.titlePicRv.layoutParams as ConstraintLayout.LayoutParams).height = SizeUtils.dp2px(48F)

        } else {
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).width = RecyclerView.LayoutParams.MATCH_PARENT
            (helper.itemView.titlePicRv.layoutParams as ConstraintLayout.LayoutParams).height = ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(70F)) / 5f).toInt()
        }
        helper.itemView.titleTv.text = item.title
        val adapter = AllTitlePicAdapter(spancount)
        helper.itemView.titlePicRv.layoutManager = GridLayoutManager(mContext, spancount)
        helper.itemView.titlePicRv.adapter = adapter
        adapter.setNewData(item.son)

        //点击跳转
        helper.itemView.onClick {
            helper.itemView.isEnabled = false
            (mContext as Activity).startActivity<SquareSamePersonActivity>("topicBean" to item)
            helper.itemView.postDelayed({
                helper.itemView.isEnabled = true
            }, 1000L)
        }
        //此处做跳转是为了避免recyclerview占据焦点无法点击
        helper.itemView.titlePicView.onClick {
            helper.itemView.titlePicView.isEnabled = false
            (mContext as Activity).startActivity<SquareSamePersonActivity>("topicBean" to item)
            helper.itemView.titlePicView.postDelayed({
                helper.itemView.titlePicView.isEnabled = true
            }, 1000L)
        }
    }
}