package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.SpanUtils
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
            hotT1.text = getString(R.string.sweet_wealth_title)

            sweetVerifyWay1.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.icon_sweet_verify_wealth_unchecked),
                null,
                null
            )
            sweetVerifyWay1.text = getString(R.string.sweet_big_house_title)
            sweetVerifyWay1.setTextColor(resources.getColor(R.color.colorBlack19))

            sweetVerifyWay2.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.icon_sweet_verify_car_unchecked),
                null,
                null
            )
            sweetVerifyWay2.text = getString(R.string.sweet_luxury_car_title)
            sweetVerifyWay2.setTextColor(resources.getColor(R.color.colorBlack19))
        } else {
            hotT1.text = getString(R.string.sweet_girl_title)

            sweetVerifyWay1.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.icon_sweet_verify_education_unchecked),
                null,
                null
            )
            sweetVerifyWay1.text = getString(R.string.sweet_figure_title)
            sweetVerifyWay1.setTextColor(resources.getColor(R.color.colorBlack19))

            sweetVerifyWay2.setCompoundDrawablesWithIntrinsicBounds(
                null,
                getDrawable(R.drawable.icon_sweet_verify_profession_unchecked),
                null,
                null
            )
            sweetVerifyWay2.text = getString(R.string.sweet_job_title)
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
                    type = SweetHeartVerifyUploadActivity.TYPE_WEALTH

                    SpanUtils.with(sweetVerifyTip)
                        .append(getString(R.string.need_commit_house))
                        .append(getString(R.string.bigger_than_200))
                        .setForegroundColor(Color.parseColor("#FF5A85F6"))
                        .setBold()
                        .append(getString(R.string.approve__ability))
                        .create()

                } else {
                    sweetVerifyWay1.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        getDrawable(R.drawable.icon_sweet_verify_education_checked),
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
                    type = SweetHeartVerifyUploadActivity.TYPE_EDUCATION
//学历认证仅通过981/211在读或毕业生
//学历是受教和成长环境的最佳体现
//认证后将获得全局额外推荐
                    SpanUtils.with(sweetVerifyTip)
                        .append(getString(R.string.need_commit1))
                        .append(getString(R.string.approve_education))
                        .setForegroundColor(Color.parseColor("#FF5A85F6"))
                        .setBold()
                        .append(getString(R.string.s_photo))
                        .append(getString(R.string.only_important_college))
                        .create()
//                    sweetVerifyWay1.setCompoundDrawablesWithIntrinsicBounds(
//                        null,
//                        getDrawable(R.drawable.icon_sweet_verify_figure_checked),
//                        null,
//                        null
//                    )
//                    sweetVerifyWay1.setTextColor(resources.getColor(R.color.colorOrange))
//                    sweetVerifyWay2.setCompoundDrawablesWithIntrinsicBounds(
//                        null,
//                        getDrawable(R.drawable.icon_sweet_verify_profession_unchecked),
//                        null,
//                        null
//                    )
//                    sweetVerifyWay2.setTextColor(resources.getColor(R.color.colorBlack19))
//                    type = TYPE_FIGURE
//
//                    SpanUtils.with(sweetVerifyTip)
//                        .append(getString(R.string.chest_size))
//                        .append(getString(R.string.bigger_than_c))
//                        .setForegroundColor(Color.parseColor("#FF5A85F6"))
//                        .setBold()
//                        .append(getString(R.string.to_show_magic))
//                        .create()


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

                    type = SweetHeartVerifyUploadActivity.TYPE_CAR
                    SpanUtils.with(sweetVerifyTip)
                        .append(getString(R.string.need_commit))
                        .append(getString(R.string.driving_license_price_more_than_50))
                        .setForegroundColor(Color.parseColor("#FF5A85F6"))
                        .setBold()
                        .append(getString(R.string.to_show_your_car))
                        .create()
                } else {
                    sweetVerifyWay1.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        getDrawable(R.drawable.icon_sweet_verify_education_unchecked),
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
                    type = SweetHeartVerifyUploadActivity.TYPE_PROFESSION

//                    要求提交能证明职业的照片
                    SpanUtils.with(sweetVerifyTip)
                        .append(getString(R.string.must_commit))
                        .append(getString(R.string.job_prove))
                        .setForegroundColor(Color.parseColor("#FF5A85F6"))
                        .setBold()
                        .append(getString(R.string.pic_like_model))
                        .create()
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