package com.sdy.jitangapplication.ui.adapter

import android.animation.ObjectAnimator
import android.graphics.Color
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.ext.onClick
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MyLabelBean
import com.sdy.jitangapplication.ui.activity.LabelQualityActivity
import kotlinx.android.synthetic.main.item_layout_my_label.view.*
import org.jetbrains.anko.startActivity


/**
 *    author : ZFM
 *    date   : 2019/11/117:36
 *    desc   :
 *    version: 1.0
 */
class MyLabelAdapter(var fromMine: Boolean = true) :
    BaseQuickAdapter<MyLabelBean, BaseViewHolder>(R.layout.item_layout_my_label) {
    var notify = false

    override fun convert(helper: BaseViewHolder, item: MyLabelBean) {
        if (notify)
            if (item.editMode) {
                val translate =
                    ObjectAnimator.ofFloat(helper.itemView.content, "translationX", SizeUtils.dp2px(35F).toFloat())
                translate.duration = 100
                translate.start()
                helper.itemView.labelEdit.isVisible = false
                helper.itemView.labelPurchase.isVisible = false
            } else {
                val translate =
                    ObjectAnimator.ofFloat(helper.itemView.content, "translationX", SizeUtils.dp2px(0F).toFloat())
                translate.duration = 100
                translate.start()
                if (fromMine) {
                    if (item.is_expire) {
                        helper.itemView.labelEdit.isVisible = false
                        helper.itemView.labelPurchase.isVisible = true
                    } else {
                        helper.itemView.labelEdit.isVisible = true
                        helper.itemView.labelPurchase.isVisible = false
                    }
                } else {
                    helper.itemView.labelEdit.isVisible = false
                    helper.itemView.labelOuttime.isVisible = false
                }
            }
        else
            if (fromMine) {
                if (item.is_expire) {
                    helper.itemView.labelEdit.isVisible = false
                    helper.itemView.labelPurchase.isVisible = true
                } else {
                    helper.itemView.labelEdit.isVisible = true
                    helper.itemView.labelPurchase.isVisible = false
                }
            } else {
                helper.itemView.labelEdit.isVisible = false
                helper.itemView.labelOuttime.isVisible = false
            }

        helper.itemView.labelName.text = item.title
        GlideUtil.loadRoundImgCenterCrop(mContext, item.icon, helper.itemView.labelIcon, SizeUtils.dp2px(8F))
        helper.addOnClickListener(R.id.labelDelete)
        helper.addOnClickListener(R.id.labelQualityAddBtn)
        helper.addOnClickListener(R.id.labelPurchase)
        helper.addOnClickListener(R.id.labelEdit)

        if (fromMine) {
            if (item.is_expire) {
                helper.itemView.labelOuttime.isVisible = true
                helper.itemView.labelName.setTextColor(Color.parseColor("#FFC5C6C8"))
                helper.itemView.labelQualityAddBtn.setTextColor(Color.parseColor("#FFC5C6C8"))
                helper.itemView.labelQualityAddBtn.setBackgroundResource(R.drawable.shape_rectangle_fafafa_8dp)
                helper.itemView.labelQualityAddBtn.setCompoundDrawablesWithIntrinsicBounds(
                    mContext.resources.getDrawable(R.drawable.icon_add_tag_gray),
                    null,
                    null,
                    null
                )
            } else {
                helper.itemView.labelOuttime.isVisible = false
                helper.itemView.labelName.setTextColor(Color.parseColor("#ff191919"))
                helper.itemView.labelQualityAddBtn.setTextColor(Color.parseColor("#FF787C7F"))
                helper.itemView.labelQualityAddBtn.setBackgroundResource(R.drawable.shape_rectangle_gray_white_8dp)
                helper.itemView.labelQualityAddBtn.setCompoundDrawablesWithIntrinsicBounds(
                    mContext.resources.getDrawable(R.drawable.icon_add_tag),
                    null,
                    null,
                    null
                )
            }
        }

        if (item.label_quality.isNullOrEmpty()) {
            helper.itemView.labelQualityAddBtn.isVisible = fromMine
            helper.itemView.labelQualityRv.isVisible = false
            helper.itemView.labelQualityAddBtn.onClick {
                mContext.startActivity<LabelQualityActivity>(
                    "aimData" to item,
                    "mode" to LabelQualityActivity.MODE_NEW
                )
            }
        } else {
            helper.itemView.labelQualityAddBtn.isVisible = false
            helper.itemView.labelQualityRv.isVisible = true
            val adapter = LabelQualityAdapter()
            val manager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
            helper.itemView.labelQualityRv.layoutManager = manager
            helper.itemView.labelQualityRv.adapter = adapter
            if (fromMine)
                for (data in item.label_quality) {
                    if (item.is_expire) {
                        data.outtime = true
                        data.isfuse = false
                    } else {
                        data.outtime = false
                        data.isfuse = true
                    }
                }
            adapter.addData(item.label_quality)
        }
    }
}