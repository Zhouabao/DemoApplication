package com.sdy.jitangapplication.ui.adapter

import android.animation.ObjectAnimator
import android.graphics.Path
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.android.flexbox.*
import com.kotlin.base.ext.onClick
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MyLabelBean
import com.sdy.jitangapplication.ui.activity.AddLabelActivity
import com.sdy.jitangapplication.ui.activity.LabelQualityActivity
import kotlinx.android.synthetic.main.item_layout_my_label.view.*
import org.jetbrains.anko.startActivity


/**
 *    author : ZFM
 *    date   : 2019/11/117:36
 *    desc   :
 *    version: 1.0
 */
class MyLabelAdapter : BaseQuickAdapter<MyLabelBean, BaseViewHolder>(R.layout.item_layout_my_label) {
    var notify = false

    override fun convert(helper: BaseViewHolder, item: MyLabelBean) {
        if (notify)
            if (item.editMode) {
                val translate =
                    ObjectAnimator.ofFloat(helper.itemView.content, "translationX", -SizeUtils.dp2px(35F).toFloat())
                translate.duration = 100
                translate.start()
            } else {
                val translate =
                    ObjectAnimator.ofFloat(helper.itemView.content, "translationX", SizeUtils.dp2px(0F).toFloat())
                translate.duration = 100
                translate.start()

            }
        helper.itemView.labelName.text = item.title
        GlideUtil.loadRoundImgCenterCrop(mContext, item.icon, helper.itemView.labelIcon, SizeUtils.dp2px(10F))
        helper.addOnClickListener(R.id.labelDelete)
        helper.addOnClickListener(R.id.labelEdit)

        if (item.label_quality.isNullOrEmpty()) {
            helper.itemView.labelQualityAddBtn.isVisible = true
            if (!item.animated) {
                val path = Path()
                path.moveTo(0.8F, 0.8F)
                path.lineTo(1F, 1F)
                ObjectAnimator.ofFloat(helper.itemView.labelQualityAddBtn, "scaleX", "scaleY", path)
                    .setDuration(450L)
                    .start()
                item.animated = true
            }
            helper.itemView.labelQualityRv.isVisible = false
            helper.itemView.labelQualityAddBtn.onClick {
                mContext.startActivity<LabelQualityActivity>(
                    "aimData" to item,
                    "from" to AddLabelActivity.FROM_EDIT,
                    "mode" to LabelQualityActivity.MODE_EDIT
                )
            }
        } else {
            helper.itemView.labelQualityAddBtn.isVisible = false
            helper.itemView.labelQualityRv.isVisible = true
            val adapter = LabelQualityAdapter()

            val manager = FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP)
            manager.alignItems = AlignItems.STRETCH
            manager.justifyContent = JustifyContent.FLEX_START
            helper.itemView.labelQualityRv.layoutManager = manager
            helper.itemView.labelQualityRv.adapter = adapter
            for (data in item.label_quality) {
                data.isfuse = true
            }
            adapter.addData(item.label_quality)
        }


    }
}