package com.sdy.jitangapplication.ui.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.NewLabel
import com.sdy.jitangapplication.ui.activity.AddLabelActivity
import kotlinx.android.synthetic.main.item_label_all1.view.*

/**
 *    author : ZFM
 *    date   : 2019/10/910:30
 *    desc   : 所有新标签的数据
 *    version: 1.0
 */
class AllNewLabelAdapter1(
    var index: Int = 1,
    var from: Int = AddLabelActivity.FROM_ADD_NEW,
    val isCross: Boolean = false
) :
    BaseQuickAdapter<NewLabel, BaseViewHolder>(R.layout.item_label_all1) {
    override fun convert(helper: BaseViewHolder, item: NewLabel) {
        //1.无需付费.未添加 2无需付费.已经添加  3.无需付费.已删除 4需要付费.付费进入
        //5.无需付费.男性付费 6.无需付费.女性付费  7.需要付费.已过期 8.需要付费.(已删除,未加入).未过期限
        //9.需要付费.已删除.过期 10.需要付费.已经添加
        helper.itemView.labelOutTime.isVisible = item.state == 7
        when (item.state) {
            1, 3 -> {//1.无需付费.未添加  3.无需付费.已删除
                helper.itemView.labelAdded.isVisible = false
            }
            2, 10 -> {//2无需付费.已经添加  10.需要付费.已经添加
                helper.itemView.labelAdded.isVisible = true
                helper.itemView.labelAdded.setImageResource(R.drawable.icon_label_added)
            }
            8 -> {//8.需要付费.(已删除,未加入).未过期限
                helper.itemView.labelAdded.isVisible = true
                helper.itemView.labelAdded.setImageResource(R.drawable.icon_label_purchased)
            }
            7 -> {//7.需要付费.已过期
                helper.itemView.labelAdded.isVisible = false
            }
            4, 9 -> {//4需要付费.付费进入   9.需要付费.已删除.过期
                helper.itemView.labelAdded.isVisible = true
                helper.itemView.labelAdded.setImageResource(R.drawable.icon_label_payed)
            }
            5 -> {//5.无需付费.男性付费
                helper.itemView.labelAdded.isVisible = true
                helper.itemView.labelAdded.setImageResource(R.drawable.icon_label_payed_man)
            }
            6 -> {//6.无需付费.女性付费
                helper.itemView.labelAdded.isVisible = true
                helper.itemView.labelAdded.setImageResource(R.drawable.icon_label_payed_woman)
            }
        }

        helper.itemView.labelChecked.isVisible = item.checked
        if (isCross) {
            val params = helper.itemView.labelRoot.layoutParams as RecyclerView.LayoutParams
            params.width = SizeUtils.dp2px(98F)
            params.height = RecyclerView.LayoutParams.WRAP_CONTENT
            params.rightMargin = if (helper.layoutPosition == mData.size - 1) {
                SizeUtils.dp2px(15F)
            } else {
                SizeUtils.dp2px(12F)
            }
            helper.itemView.layoutParams = params

            val params1 = helper.itemView.labelImg.layoutParams as ConstraintLayout.LayoutParams
            params1.width = params.width
            params1.height = params.width
            helper.itemView.labelImg.layoutParams = params1
        } else {
            val params = helper.itemView.labelRoot.layoutParams as RecyclerView.LayoutParams
            params.width = ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px((15 * 2 + 12 * 2).toFloat())) / 3F).toInt()
            params.height = RecyclerView.LayoutParams.WRAP_CONTENT
            params.bottomMargin = if (helper.layoutPosition / 3 != (mData.size - 1) / 3) {
                SizeUtils.dp2px(12F)
            } else {
                0
            }
            helper.itemView.layoutParams = params
            val params1 = helper.itemView.labelImg.layoutParams as ConstraintLayout.LayoutParams
            params1.width = params.width
            params1.height = params.width
            helper.itemView.labelImg.layoutParams = params1
        }
        helper.itemView.labelName.text = item.title
        helper.itemView.labelPersonNum.text = "${item.used_cnt}"
        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            item.icon,
            helper.itemView.labelImg,
            SizeUtils.dp2px(10F)
        )
    }
}