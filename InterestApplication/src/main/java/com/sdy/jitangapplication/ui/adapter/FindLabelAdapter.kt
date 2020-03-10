package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.ext.onClick
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.NewLabel
import com.sdy.jitangapplication.ui.activity.FindByTagListActivity
import com.sdy.jitangapplication.ui.activity.MyLabelActivity
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.item_add_label.view.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/10/910:30
 *    desc   : 兴趣找人适配器
 *    version: 1.0
 */
class FindLabelAdapter : BaseQuickAdapter<NewLabel, BaseViewHolder>(R.layout.item_add_label) {

    override fun convert(helper: BaseViewHolder, item: NewLabel) {
        helper.addOnClickListener(R.id.labelManagerBtn)
        helper.itemView.labelTypeName.text = item.title
        GlideUtil.loadImg(mContext, item.icon, helper.itemView.labelTypeNameIv)
        helper.itemView.labelManagerBtn.isVisible = item.ismine
        helper.itemView.labelManagerBtn.onClick {
            mContext.startActivity<MyLabelActivity>()
        }
        val labelAdapter = AllNewLabelAdapter1(isIndex = true)
        if (item.ismine || item.ishot) {
            for (decoration in 0 until helper.itemView.labelTypeRv.itemDecorationCount) {
                helper.itemView.labelTypeRv.removeItemDecorationAt(decoration)
            }
            helper.itemView.labelTypeRv.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
            helper.itemView.labelTypeRv.addItemDecoration(
                DividerItemDecoration(
                    mContext,
                    DividerItemDecoration.VERTICAL_LIST,
                    SizeUtils.dp2px(12f),
                    mContext.resources.getColor(R.color.colorWhite)
                )
            )
        } else {
            helper.itemView.labelTypeRv.layoutManager = GridLayoutManager(mContext, 3, RecyclerView.VERTICAL, false)
        }
        helper.itemView.labelTypeRv.adapter = labelAdapter
        labelAdapter.setNewData(item.son)
        labelAdapter.setOnItemClickListener { _, view, position ->
            mContext.startActivity<FindByTagListActivity>("labelBean" to item.son[position])
        }
    }
}