package com.sdy.jitangapplication.ui.adapter

import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MyLabelBean
import kotlinx.android.synthetic.main.item_layout_my_label.view.*


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
                val animationLeft = TranslateAnimation(
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0F,
                    TranslateAnimation.ABSOLUTE,
                    -SizeUtils.dp2px(35F).toFloat(),
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0F,
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0F
                )
                animationLeft.fillAfter = true
                animationLeft.duration = 100L
                animationLeft.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(p0: Animation?) {

                    }

                    override fun onAnimationEnd(p0: Animation?) {
//                        val params = helper.itemView.content.layoutParams as FrameLayout.LayoutParams
//                        params.rightMargin = SizeUtils.dp2px(50F)
//                        helper.itemView.content.layoutParams = params
                    }

                    override fun onAnimationStart(p0: Animation?) {
                    }

                })
                helper.itemView.content.startAnimation(animationLeft)
            } else {
                val animationLeft = TranslateAnimation(
                    TranslateAnimation.ABSOLUTE,
                    -SizeUtils.dp2px(35F).toFloat(),
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0F,
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0F,
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0F
                )
                animationLeft.fillAfter = true
                animationLeft.duration = 100L
                animationLeft.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(p0: Animation?) {

                    }

                    override fun onAnimationEnd(p0: Animation?) {
//                        val params = helper.itemView.content.layoutParams as FrameLayout.LayoutParams
//                        params.rightMargin = SizeUtils.dp2px(15F)
//                        helper.itemView.content.layoutParams = params
                    }

                    override fun onAnimationStart(p0: Animation?) {
                    }

                })
                helper.itemView.content.startAnimation(animationLeft)
            }
        helper.itemView.labelName.text = item.title
        GlideUtil.loadRoundImgCenterCrop(mContext, item.icon, helper.itemView.labelIcon, SizeUtils.dp2px(10F))
        helper.addOnClickListener(R.id.labelDelete)
        helper.addOnClickListener(R.id.labelEdit)
        val adapter = LabelQualityAdapter()
        helper.itemView.labelQualityRv.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        helper.itemView.labelQualityRv.adapter = adapter
        for (data in item.label_quality) {
            data.checked = true
        }
        adapter.addData(item.label_quality)
    }
}