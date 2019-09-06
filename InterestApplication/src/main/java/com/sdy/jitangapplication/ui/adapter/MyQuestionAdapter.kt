package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.QuestionBean
import kotlinx.android.synthetic.main.item_my_question.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2615:22
 *    desc   : 个人中心的动态封面适配器
 *    version: 1.0
 */
class MyQuestionAdapter : BaseQuickAdapter<QuestionBean, BaseViewHolder>(R.layout.item_my_question) {

    override fun convert(holder: BaseViewHolder, item: QuestionBean) {
        holder.itemView.questionNum.text = "Q${holder.layoutPosition}"
        holder.itemView.questionMsg.text = item.title ?: ""
    }

}