package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.NewLabel
import com.sdy.jitangapplication.ui.activity.AddLabelActivity
import com.sdy.jitangapplication.ui.dialog.CorrectDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.correct_dialog_layout.*
import kotlinx.android.synthetic.main.item_add_label.view.*

/**
 *    author : ZFM
 *    date   : 2019/10/910:30
 *    desc   : 兴趣导航栏的数据
 *    version: 1.0
 */
class AddLabelAdapter : BaseQuickAdapter<NewLabel, BaseViewHolder>(R.layout.item_add_label) {
    var from: Int = AddLabelActivity.FROM_ADD_NEW
    var parentPosition = -1  //购买兴趣的位置
    var childPosition = -1 //购买兴趣的子位置
    var purchaseId = -1 //购买兴趣的id

    override fun convert(helper: BaseViewHolder, item: NewLabel) {
        helper.addOnClickListener(R.id.labelManagerBtn)
        helper.itemView.labelTypeName.text = item.title
        GlideUtil.loadImg(mContext, item.icon, helper.itemView.labelTypeNameIv)
        helper.itemView.labelManagerBtn.isVisible = item.ismine
        val labelAdapter = AllNewLabelAdapter1(item.ismine || item.ishot)
        if (item.ismine || item.ishot) {
            for (decoration in 0 until helper.itemView.labelTypeRv.itemDecorationCount) {
                helper.itemView.labelTypeRv.removeItemDecorationAt(decoration)
            }
            helper.itemView.labelTypeRv.layoutManager =
                LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        } else {
            helper.itemView.labelTypeRv.layoutManager =
                GridLayoutManager(mContext, 3, RecyclerView.VERTICAL, false)
        }
        helper.itemView.labelTypeRv.adapter = labelAdapter
        labelAdapter.setNewData(item.son)
        labelAdapter.setOnItemClickListener { _, view, position ->
            //ChargeLabelDialog(mContext, labelAdapter.data[position]).show()
            val data = labelAdapter.data[position]
            //1.无需付费.未添加  3.无需付费.已删除
            //5.无需付费.男性付费
            //6.无需付费.女性付费
            //8.需要付费.(已删除,未加入).未过期限
            //2无需付费.已经添加  10.需要付费.已经添加
            //4需要付费.付费进入   9.需要付费.已删除.过期   7.需要付费.已过期
//            if (data.state == 2 || data.state == 10) {
//                YoYo.with(Techniques.Shake)
//                    .duration(100)
//                    .repeat(0)
//                    .playOn(view)
//                付费和免费已添加
//                return@setOnItemClickListener
//            }


            var checkedCount = 0
            for (index in 0 until mData.size) {
                if (!(mData[index].ishot || mData[index].ismine))
                    for (tdata1 in mData[index].son) {
                        if (tdata1.checked) {
                            checkedCount += 1
                        }
                    }
            }
            if (checkedCount >= UserManager.getMaxMyLabelCount() && !data.checked) {
                showWarningDialog("至多选择${UserManager.getMaxMyLabelCount()}个兴趣")
                return@setOnItemClickListener
            }

            //1 3 5 6 8  2 10
            labelAdapter.data[position].checked = !labelAdapter.data[position].checked
            for (index in 0 until mData.size) {
                for (tdata1 in mData[index].son) {
                    if (tdata1.id == labelAdapter.data[position].id) {
                        tdata1.checked = labelAdapter.data[position].checked
                    }
                }
                notifyDataSetChanged()
            }
        }
    }

    private val warningDialog by lazy { CorrectDialog(mContext) }
    private fun showWarningDialog(content: String) {
        warningDialog.show()
        warningDialog.correctLogo.setImageResource(R.drawable.icon_notice)
        warningDialog.correctTip.text = content
        warningDialog.correctTip.postDelayed({ warningDialog.dismiss() }, 1000L)
    }


    /*
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
                "from" to AddLabelActivity.FROM_ADD_NEW,
                "mode" to LabelQualityActivity.MODE_NEW
            )
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
            (mContext as Activity).startActivity<LabelQualityActivity>(
                "aimData" to tempLabel,
                "from" to AddLabelActivity.FROM_EDIT,
                "mode" to LabelQualityActivity.MODE_EDIT
            )
            deleteDialog.dismiss()
        }

    }*/

}