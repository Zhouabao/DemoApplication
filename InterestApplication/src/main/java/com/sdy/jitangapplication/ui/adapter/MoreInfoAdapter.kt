package com.sdy.jitangapplication.ui.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.TextView
import androidx.core.view.isVisible
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.AnswerBean
import com.sdy.jitangapplication.model.FindTagBean
import kotlinx.android.synthetic.main.item_more_info.view.*
import kotlinx.android.synthetic.main.layout_add_score.view.*

/**
 *    author : ZFM
 *    date   : 2020/5/1110:10
 *    desc   : 更多个人信息
 *    version: 1.0
 */
class MoreInfoAdapter() :
    BaseQuickAdapter<AnswerBean, BaseViewHolder>(R.layout.item_more_info) {
    //改变的参数
    public val params by lazy { hashMapOf<String, Any>() }

    override fun convert(helper: BaseViewHolder, item: AnswerBean) {
        val itemView = helper.itemView
        itemView.moreInfoTitle.text = item.title
        itemView.moreInfoContent.hint = item.descr
        if (item.title == "身高" && !item.find_tag!!.title.isNullOrEmpty() && item.find_tag!!.title.toInt() > 0) {
            itemView.moreInfoContent.text = item.find_tag!!.title
            itemView.moreInfoAnimation.isVisible = false
        } else if (item.find_tag != null && item.child.contains(item.find_tag!!) && item.find_tag!!.id != 0) {
            itemView.moreInfoContent.text = item.find_tag!!.title
            itemView.moreInfoAnimation.isVisible = false
        } else {
            itemView.moreInfoAnimation.isVisible = true
        }
        itemView.moreInfoAnimation.tvAddScoreSmile.text = "+${item.point}"

    }


    /**
     * 更新添加分数的状态
     * 资料新增后就要改变状态实现动画
     */
    private fun updateScoreStatus(view: View? = null, score: Int, update: Boolean? = false) {
        //会员的时候不显示添加分数
        if (view != null) {
            view.tvAddScoreSmile.text = "+$score"
            //如果view处于可见状态，说明之前没有加过分数，那这时就实现动画效果
            if (view.isVisible && update == true) {
                val translateAnimationRight = TranslateAnimation(
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0f,
                    TranslateAnimation.ABSOLUTE,
                    (view.width - view.ivAddScoreSmile.width - SizeUtils.dp2px(8f)).toFloat(),
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0f,
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0F

                )
                translateAnimationRight.duration = 500
                translateAnimationRight.fillAfter = true
                translateAnimationRight.interpolator = LinearInterpolator()

                val translateAnimationTop = TranslateAnimation(
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0f,
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0F,
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0f,
                    TranslateAnimation.RELATIVE_TO_PARENT,
                    -0.7F

                )
                translateAnimationTop.duration = 500
                translateAnimationTop.fillAfter = true
                translateAnimationTop.interpolator = DecelerateInterpolator()
                val scaleAnimation = ScaleAnimation(1f, 0f, 1f, 0f)
                scaleAnimation.duration = 500
                scaleAnimation.fillAfter = true
                val alphaAnimation = AlphaAnimation(0F, 1F)
                alphaAnimation.duration = 500
                alphaAnimation.fillAfter = true
                val animationSet = AnimationSet(true)
                animationSet.addAnimation(translateAnimationTop)
                animationSet.addAnimation(scaleAnimation)
                animationSet.addAnimation(alphaAnimation)
                animationSet.fillAfter = true

                translateAnimationRight.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(p0: Animation?) {

                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        view.postDelayed({
                            view.isVisible = false
                        }, 200)
                    }

                    override fun onAnimationStart(p0: Animation?) {
                        view.setBackgroundResource(R.drawable.shape_rectangle_orange_11dp)
                        view.tvAddScoreSmile.startAnimation(animationSet)
                    }
                })
                view.ivAddScoreSmile.startAnimation(translateAnimationRight)
            }
        }

//        if (update == true)
//            setScroeProgress(score)


    }


    /**
     * 展示条件选择器
     */
    private fun showConditionPicker(
        textview: TextView,
        scoreView: View?,
        score: Int,
        title: String,
        param: String,
        defaultCheckId: FindTagBean? = null,
        optionsItems1: MutableList<FindTagBean>
    ) {
        //条件选择器
        val pvOptions = OptionsPickerBuilder(mContext,
            OnOptionsSelectListener { options1, options2, options3, v ->
                params[param] = optionsItems1[options1].id
                textview.text = "${optionsItems1[options1].title}"
                if (scoreView != null && scoreView.isVisible)
                    updateScoreStatus(scoreView, score, update = true)
            })
            .setSubmitText("确定")
            .setTitleText(title)
            .setTitleColor(mContext.resources.getColor(R.color.colorBlack))
            .setTitleSize(16)
            .setDividerColor(mContext.resources.getColor(R.color.colorDivider))
            .setContentTextSize(20)
            .setDecorView((mContext as Activity).window.decorView.findViewById(android.R.id.content) as ViewGroup)
            .setSubmitColor(mContext.resources.getColor(R.color.colorBlueSky1))
            .build<FindTagBean>()

        //身高默认选中，男170 女160
//        if (title == "身高") {
//            if (UserManager.getGender() == 1) { //男
//                pvOptions.setSelectOptions(170 - 60)
//            } else {
//                pvOptions.setSelectOptions(160 - 60)
//            } //女
//        } else {
        if (defaultCheckId != null) {
            for (data in optionsItems1.withIndex()) {
                if (data.value.id == defaultCheckId.id) {
                    pvOptions.setSelectOptions(data.index)
                    break
                }
            }
        }
//        }
        pvOptions.setPicker(optionsItems1)
        pvOptions.show()
    }

}