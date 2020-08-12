package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.TextView
import androidx.core.view.isVisible
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.DatingOptionsBean
import com.sdy.jitangapplication.presenter.CompleteDatingInfoPresenter
import com.sdy.jitangapplication.presenter.view.CompleteDatingInfoView
import kotlinx.android.synthetic.main.activity_complete_dating_info.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity


/**
 * 完善约会信息
 */
class CompleteDatingInfoActivity : BaseMvpActivity<CompleteDatingInfoPresenter>(),
    CompleteDatingInfoView, OnLazyClickListener {
    private val dating_type by lazy { intent.getIntExtra("dating_type", -1) }
    private var typeText = true //文本限制

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_dating_info)

        initView()
        playSmallAnimation()

        mPresenter.datingOptions()
    }

    private fun initView() {
        mPresenter = CompleteDatingInfoPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        hotT1.text = "完善约会信息"
        btnBack.clickWithTrigger {
            finish()
        }
        rightBtn1.text = "发布"
        rightBtn1.isVisible = true

        chooseDatingPlaceBtn.setOnClickListener(this)
        chooseDatingPlanBtn.setOnClickListener(this)
        chooseDatingObjectBtn.setOnClickListener(this)
        chooseDatingPayBtn.setOnClickListener(this)
        rightBtn1.setOnClickListener(this)

    }

    private fun initAudioCl(){

    }

    private fun playBigAnimation() {
        val animation = AnimationSet(true)
        //缩放动画，以中心从原始放大到1.4倍
        val scaleAnimation = ScaleAnimation(
            1.5f, 2.16f, 1.5f, 2.16f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        )
        //渐变动画
        val alphaAnimation = AlphaAnimation(1.0f, 0.5f)
        scaleAnimation.duration = 1000
        scaleAnimation.repeatCount = Animation.INFINITE
        alphaAnimation.repeatCount = Animation.INFINITE
        animation.duration = 1000
        animation.addAnimation(scaleAnimation)
        animation.addAnimation(alphaAnimation)
        recordAnimaBig.startAnimation(animation)

    }

    private fun playSmallAnimation() {
        val animation = AnimationSet(true)
        //缩放动画，以中心从1.4倍放大到1.8倍
        val scaleAnimation =
            ScaleAnimation(
                1.0f, 1.5f, 1.0f, 1.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f
            )
        //渐变动画
        val alphaAnimation = AlphaAnimation(0.5f, 0.1f)
        scaleAnimation.duration = 1000
        scaleAnimation.repeatCount = Animation.INFINITE
        alphaAnimation.repeatCount = Animation.INFINITE
        animation.duration = 1000
        animation.addAnimation(scaleAnimation)
        animation.addAnimation(alphaAnimation)
        recordAnimaSmall.startAnimation(animation)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
            }

            override fun onAnimationStart(animation: Animation?) {
                recordAnimaSmall.postDelayed({ playBigAnimation() }, 200L)

            }

        })
    }


    /**
     * 展示条件选择器
     */
    private val params by lazy { hashMapOf<String, Any>("dating_type" to dating_type) }

    private fun showConditionPicker(
        textview: TextView,
        params1: String,
        title: String,
        optionsItems1: MutableList<String>,
        optionsItems2: MutableList<MutableList<String>> = mutableListOf(),
        params2: String = ""
    ) {
        //条件选择器
        val pvOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v ->
                if (optionsItems2.isEmpty()) {
                    textview.text = optionsItems1[options1]
                    params[params1] = optionsItems1[options1]
                } else {
                    textview.text =
                        "${optionsItems1[options1]}·${optionsItems2[options1][options2]}"
                    params[params1] = optionsItems1[options1]
                    params[params2] = optionsItems2[options1][options2]
                }
            })
            .setSubmitText("确定")
            .setTitleText(title)
            .setTitleColor(resources.getColor(R.color.colorBlack))
            .setTitleSize(16)
            .setDividerColor(resources.getColor(R.color.colorDivider))
            .setContentTextSize(20)
            .setDecorView((window.decorView.findViewById(android.R.id.content)) as ViewGroup)
            .setSubmitColor(resources.getColor(R.color.colorBlueSky1))
            .build<String>()

        if (optionsItems2.isNotEmpty()) {
            pvOptions.setPicker(optionsItems1, optionsItems2)
        } else {
            pvOptions.setPicker(optionsItems1)
        }
        pvOptions.show()
    }

    private val datingCostMoneyCondition = mutableListOf<MutableList<String>>()
    private val datingCostTypeCondition = mutableListOf<String>()
    private val datingTargetCondition = mutableListOf<String>()
    private val datingPlanCondition = mutableListOf<String>()
    override fun onLazyClick(v: View) {
        when (v.id) {
            R.id.chooseDatingPlaceBtn -> { //约会地点
                startActivity<LocationActivity>()
            }
            R.id.chooseDatingObjectBtn -> {//约会对象
                showConditionPicker(
                    chooseDatingObjectBtn,
                    "dating_target",
                    "选择约会对象",
                    datingTargetCondition
                )
            }
            R.id.chooseDatingPayBtn -> {//费用开支
                showConditionPicker(
                    chooseDatingPayBtn,
                    "cost_type",
                    "费用开支预估",
                    datingCostTypeCondition,
                    datingCostMoneyCondition,
                    "cost_money"
                )
            }
            R.id.chooseDatingPlanBtn -> {//后续活动
                showConditionPicker(
                    chooseDatingPlanBtn,
                    "follow_up",
                    "后续活动",
                    datingPlanCondition
                )

            }
            R.id.switchContentTypeBtn -> {//切换文字描述
                typeText = !typeText
                datingTextCl.isVisible = typeText
                datingAudioCl.isVisible = !typeText
                if (typeText) {
                    myDatingAudioView.releaseAudio()
                    switchContentTypeBtn.text = "切换语音描述"
                } else {
                    datingDescrEt.setText("")
                    switchContentTypeBtn.text = "切换文字描述"
                }

            }
            R.id.rightBtn1 -> {
                //dating_type [int]	是	约会类型
                //title [string]	是	约会标题
                //place [string]		约会地点（）
                //dating_target [string]	是	约会要求
                //cost_type [int]	是	费用类型AA....
                //content_type [int]	是	1 文本 2语音
                //content [string]	是	文本 或 语音地址
                //cost_money [string]	是	费用标准
                //follow_up [int]	是	后续约会类型
                //province_name [string]	是	省
                //city_name [string]	是	市
                //lat [string]	是
                //lng [string]	是

                mPresenter.releaseDate()
            }
        }


    }

    override fun onDatingOptionsResult(data: DatingOptionsBean?) {
        if (data != null) {
            datingTargetCondition.addAll(data.dating_target)
            datingPlanCondition.addAll(data.follow_up)
            datingCostTypeCondition.addAll(data.cost_type)
            for (index in 0 until datingCostTypeCondition.size) {
                datingCostMoneyCondition.add(data.cost_money)
            }
        }
    }


}