package com.example.demoapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.demoapplication.R
import com.example.demoapplication.model.QuestionBean
import kotlinx.android.synthetic.main.item_all_question.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2615:22
 *    desc   : 个人中心的动态封面适配器
 *    version: 1.0
 */
class AllQuestionAdapter : BaseQuickAdapter<QuestionBean, BaseViewHolder>(R.layout.item_all_question) {

    override fun convert(holder: BaseViewHolder, item: QuestionBean) {
        holder.itemView.allQuestionMsg.text = item.title ?: ""
        holder.addOnClickListener(R.id.allQuestionAdd)
    }

}