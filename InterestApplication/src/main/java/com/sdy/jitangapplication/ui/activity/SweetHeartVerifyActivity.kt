package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ClickUtils
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_sweet_heart_verify.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity

/**
 * 资产认证
 */
class SweetHeartVerifyActivity : BaseActivity(), View.OnClickListener {
    companion object {
        const val TYPE_WEALTH = 1
        const val TYPE_CAR = 2
        const val TYPE_FIGURE = 3
        const val TYPE_PROFESSION = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sweet_heart_verify)

        initView()
    }


    private fun initView() {
        BarUtils.setStatusBarColor(this, Color.WHITE)
        ClickUtils.applySingleDebouncing(
            arrayOf<View>(applyForSweetBtn, sweetVerifyWay1, sweetVerifyWay2, btnBack),
            this
        )

        if (UserManager.getGender() == 1) {
            hotT1.text = "资产认证"

            sweetVerifyWay1.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.icon_sweet_verify_wealth_unchecked),
                null,
                null
            )
            sweetVerifyWay1.text = "资产认证"
            sweetVerifyWay1.setTextColor(resources.getColor(R.color.colorBlack19))

            sweetVerifyWay2.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.icon_sweet_verify_car_unchecked),
                null,
                null
            )
            sweetVerifyWay2.text = "豪车认证"
            sweetVerifyWay2.setTextColor(resources.getColor(R.color.colorBlack19))
        } else {
            hotT1.text = "甜心圈认证"

            sweetVerifyWay1.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.icon_sweet_verify_figure_unchecked),
                null,
                null
            )
            sweetVerifyWay1.text = "身材认证"
            sweetVerifyWay1.setTextColor(resources.getColor(R.color.colorBlack19))

            sweetVerifyWay2.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.icon_sweet_verify_profession_unchecked),
                null,
                null
            )
            sweetVerifyWay2.text = "职业认证"
            sweetVerifyWay2.setTextColor(resources.getColor(R.color.colorBlack19))
        }

    }


    private var type = 0
    override fun onClick(view: View) {
        when (view) {
            sweetVerifyWay1 -> {
                applyForSweetBtn.isEnabled = true
                sweetVerifyTip.isVisible = true
                if (UserManager.getGender() == 1) {
                    sweetVerifyWay1.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        getDrawable(R.drawable.icon_sweet_verify_wealth_checked),
                        null,
                        null
                    )
                    sweetVerifyWay1.setTextColor(resources.getColor(R.color.colorOrange))
                    sweetVerifyWay2.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        getDrawable(R.drawable.icon_sweet_verify_car_unchecked),
                        null,
                        null
                    )
                    sweetVerifyWay2.setTextColor(resources.getColor(R.color.colorBlack19))
                    type = TYPE_WEALTH
                    sweetVerifyTip.text = "资产认证是个人实力的表现\n" +
                            "可提交房产、存款等证明资产大于200万的凭证\n" +
                            "认证成功后可直接加入甜心圈，提交交友效率"

                } else {
                    sweetVerifyWay1.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        getDrawable(R.drawable.icon_sweet_verify_figure_checked),
                        null,
                        null
                    )
                    sweetVerifyWay1.setTextColor(resources.getColor(R.color.colorOrange))
                    sweetVerifyWay2.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        getDrawable(R.drawable.icon_sweet_verify_profession_unchecked),
                        null,
                        null
                    )
                    sweetVerifyWay2.setTextColor(resources.getColor(R.color.colorBlack19))
                    type = TYPE_FIGURE
                    sweetVerifyTip.text = "身材认证仅接受比基尼/私房写真\n" +
                            "身材是您个人魅力的提现\n" +
                            "认证后关注度会大幅提高"

                }
            }
            sweetVerifyWay2 -> {
                applyForSweetBtn.isEnabled = true
                sweetVerifyTip.isVisible = true
                if (UserManager.getGender() == 1) {
                    sweetVerifyWay1.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        getDrawable(R.drawable.icon_sweet_verify_wealth_unchecked),
                        null,
                        null
                    )
                    sweetVerifyWay1.setTextColor(resources.getColor(R.color.colorBlack19))

                    sweetVerifyWay2.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        getDrawable(R.drawable.icon_sweet_verify_car_checked),
                        null,
                        null
                    )
                    sweetVerifyWay2.setTextColor(resources.getColor(R.color.colorOrange))

                    type = TYPE_CAR
                    sweetVerifyTip.text = "豪车认证需要提交车辆或车内带人图片\n" +
                            "认证后对外展示您的车辆图片真实度\n" +
                            "提高您的交友效率，使您信息更真实可靠"
                } else {
                    sweetVerifyWay1.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        getDrawable(R.drawable.icon_sweet_verify_figure_unchecked),
                        null,
                        null
                    )
                    sweetVerifyWay2.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        getDrawable(R.drawable.icon_sweet_verify_profession_checked),
                        null,
                        null
                    )
                    sweetVerifyWay1.setTextColor(resources.getColor(R.color.colorBlack19))
                    sweetVerifyWay2.setTextColor(resources.getColor(R.color.colorOrange))
                    type = TYPE_PROFESSION
                    sweetVerifyTip.text = "职业认证部分限定职业如：\n" +
                            "空乘、护士、教师、舞蹈老师、瑜伽教练、白领、幼师、演员、模特、在校大学生"
                }

            }
            btnBack -> {
                finish()
            }

            applyForSweetBtn -> {
                startActivity<SweetHeartVerifyUploadActivity>("type" to type)
            }
        }
    }
}