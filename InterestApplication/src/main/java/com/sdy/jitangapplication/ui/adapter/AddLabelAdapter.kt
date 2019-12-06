package com.sdy.jitangapplication.ui.adapter

import android.app.Activity
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
import com.sdy.jitangapplication.model.MyLabelBean
import com.sdy.jitangapplication.model.NewLabel
import com.sdy.jitangapplication.ui.activity.AddLabelActivity
import com.sdy.jitangapplication.ui.activity.LabelQualityActivity
import com.sdy.jitangapplication.ui.activity.MyLabelQualityActivity
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.item_add_label.view.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/10/910:30
 *    desc   : 标签导航栏的数据
 *    version: 1.0
 */
class AddLabelAdapter : BaseQuickAdapter<NewLabel, BaseViewHolder>(R.layout.item_add_label) {
    public var from: Int = AddLabelActivity.FROM_EDIT
    public var removedLabels: MutableList<MyLabelBean> = mutableListOf()

    override fun convert(helper: BaseViewHolder, item: NewLabel) {
        helper.itemView.labelTypeName.text = item.title
        GlideUtil.loadImg(mContext, item.icon, helper.itemView.labelTypeNameIv)

        val labelAdapter = AllNewLabelAdapter1()
        if (helper.layoutPosition == 0) {
            helper.itemView.labelTypeRv.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
            helper.itemView.labelTypeRv.addItemDecoration(
                DividerItemDecoration(
                    mContext,
                    DividerItemDecoration.VERTICAL_LIST,
                    SizeUtils.dp2px(8F),
                    mContext.resources.getColor(R.color.colorWhite)
                )
            )
            labelAdapter.index = 0
        } else {
            helper.itemView.labelTypeRv.layoutManager = GridLayoutManager(mContext, 3, RecyclerView.VERTICAL, false)
            labelAdapter.index = 1
        }
        helper.itemView.labelTypeRv.adapter = labelAdapter
        labelAdapter.setNewData(item.son)
        labelAdapter.setOnItemClickListener { _, view, position ->
            if (labelAdapter.data[position].removed) {
                showDeleteDialog(labelAdapter.data[position])
            } else if (!labelAdapter.data[position].checked) {
                (mContext as Activity).startActivity<LabelQualityActivity>(
                    "data" to labelAdapter.data[position],
                    "from" to from
                )
            }
        }
    }


    private val deleteDialog by lazy { DeleteDialog(mContext) }
    private fun showDeleteDialog(position: NewLabel) {
        deleteDialog.show()
        deleteDialog.title.text = "完善兴趣"
        deleteDialog.title.isVisible = true
        deleteDialog.tip.text = "是否恢复「 ${position.title} 」删除前内容"
        deleteDialog.confirm.text = "重新完善"//要走推荐发布流程
        deleteDialog.confirm.onClick {
            (mContext as Activity).startActivity<LabelQualityActivity>(
                "data" to position,
                "from" to AddLabelActivity.FROM_ADD_NEW
            )
            // mPresenter.delMyTags(adapter.data[position].id, position)
            deleteDialog.dismiss()
        }
        deleteDialog.cancel.text = "使用原内容"
        deleteDialog.cancel.onClick {
            var tempLabel: MyLabelBean? = null
            for (data in removedLabels) {
                if (data.tag_id == position.id) {
                    tempLabel = data
                    break
                }
            }
            (mContext as Activity).startActivity<MyLabelQualityActivity>(
                "aimData" to tempLabel,
                "from" to AddLabelActivity.FROM_EDIT,
                "showDelete" to false
            )
            deleteDialog.dismiss()
        }

    }
}