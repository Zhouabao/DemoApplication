package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.TimeUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.presenter.UserNickNamePresenter
import com.sdy.jitangapplication.presenter.view.UserNickNameView
import com.sdy.jitangapplication.widgets.VerificationCodeInput
import kotlinx.android.synthetic.main.activity_user_birth.*
import org.jetbrains.anko.startActivity
import java.text.SimpleDateFormat
import java.util.*


/**
 * 填写用户生日界面（计算年龄和星座）
 */
class UserBirthActivity : BaseMvpActivity<UserNickNamePresenter>(), UserNickNameView {

    private var year: String? = null
    private var month: String? = null
    private var day: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_birth)

        initView()
    }

    private fun initView() {
        mPresenter = UserNickNamePresenter()
        mPresenter.mView = this
        mPresenter.context = this
        btnBack.onClick { finish() }

        userBirthYear.setOnCompleteListener(object : VerificationCodeInput.Listener {
            override fun onComplete(complete: Boolean, content: String?) {
                if (complete) {
                    //年份输入完成后，让月日获取焦点
                    userBirthMonth.postDelayed({ userBirthMonth.requestEditeFocus() }, 50L)
                }
                year = content
                calculateAgeAndAstro()
            }
        })

        userBirthMonth.setOnCompleteListener(object : VerificationCodeInput.Listener {
            override fun onComplete(complete: Boolean, content: String?) {
                if (complete) {
                    //月份输入完成后，让日期获取焦点
                    userBirthDay.postDelayed({ userBirthDay.requestEditeFocus() }, 50L)
                } else {
                    if (content.isNullOrEmpty()) {
                        userBirthYear.postDelayed({ userBirthYear.requestEditeFocus() }, 50L)
                    }
                }
                month = content
                calculateAgeAndAstro()
            }

        })

        userBirthDay.setOnCompleteListener(object : VerificationCodeInput.Listener {
            override fun onComplete(complete: Boolean, content: String?) {
                if (content.isNullOrEmpty()) {
                    userBirthMonth.postDelayed({ userBirthMonth.requestEditeFocus() }, 50L)
                }
                day = content
                calculateAgeAndAstro()
            }

        })

        btnNextStep.onClick {
            mPresenter.uploadUserInfo(
                2, hashMapOf(
                    "birth" to "${TimeUtils.date2Millis(
                        SimpleDateFormat(
                            "yyyyMMdd",
                            Locale.getDefault()
                        ).parse("${year!!}${month!!}${day!!}")
                    ) / 1000L}"
                )
            )
        }
    }

    /**
     * itemType :1年 2月
     */
    private fun calculateAgeAndAstro() {
        if (year == null || year!!.isEmpty()) {
            userCollesationNotify.text = "让我看看好看的人都是什么星座？"
            btnNextStep.isEnabled = false
            return
        }
        if (month == null || month!!.isEmpty() || month!!.length != 2) {
            userCollesationNotify.text = "让我看看好看的人都是什么星座？"
            btnNextStep.isEnabled = false
            return
        }

        if (day == null || day!!.isEmpty() || day!!.length != 2) {
            userCollesationNotify.text = "让我看看好看的人都是什么星座？"
            btnNextStep.isEnabled = false
            return
        }

        if (!judgeYear(year!!.toInt())) { //年份不正确
            userCollesationNotify.text = "让我看看好看的人都是什么星座？"
            btnNextStep.isEnabled = false
            userBirthYear.clear()
            return
        }

        if (!judgeBirth(month!!.toInt(), day!!.toInt())) { //月份不正确
            userCollesationNotify.text = "让我看看好看的人都是什么星座？"
            btnNextStep.isEnabled = false
            return
        }

        //年月日都正确的情况下 计算年龄
        btnNextStep.isEnabled = true
        KeyboardUtils.hideSoftInput(birthRootView)
        userCollesationNotify.text = calculateAstro(month!!.toInt(), day!!.toInt())

    }


    /**
     * 判断输入的年是否正确（18到35）
     */
    private fun judgeYear(year: Int): Boolean {
        if (Calendar.getInstance().get(Calendar.YEAR) - year < 18) {
            CommonFunction.toast("年龄必须大于等于18岁哦")
            return false
        } else if (Calendar.getInstance().get(Calendar.YEAR) - year > 50) {
            CommonFunction.toast("年龄必须在50岁以内哦")
            return false
        }
        return true
    }

    /**
     * 判断输入的生日是否正确
     */
    private fun judgeBirth(month: Int, day: Int): Boolean {
        if (month > 12 || month <= 0) {
            CommonFunction.toast("请输入正确的月份！")
            userBirthMonth.clear()
            return false
        }
        when (month) {
            1, 3, 5, 7, 8, 10, 12 ->
                if (day > 31 || day == 0) {
                    CommonFunction.toast("请输入正确的日期！")
                    userBirthDay.clear()
                    return false
                }
            4, 6, 9, 11 -> {
                if (day > 30 || day == 0) {
                    CommonFunction.toast("请输入正确的日期！")
                    userBirthDay.clear()
                    return false
                }
            }
            2 -> {
                if (year!!.toInt() % 4 == 0 && year!!.toInt() % 100 != 0 || year!!.toInt() % 400 == 0) {
                    if (day > 29 || day == 0) {
                        CommonFunction.toast("请输入正确的日期！")
                        userBirthDay.clear()
                        return false
                    }
                } else if (day > 28 || day == 0) {
                    CommonFunction.toast("请输入正确的日期！")
                    userBirthDay.clear()
                    return false
                }
            }
        }
        return true
    }

    /**
     *计算星座
     */
    private fun calculateAstro(month: Int, day: Int): String {
        val astro = arrayOf(
            "摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座",
            "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"
        )
        val arr = arrayOf(20, 19, 21, 21, 21, 22, 23, 23, 23, 23, 22, 22)
        var index = month
        if (day < arr[month - 1]) {
            index -= 1
        }
        return astro[index]
    }

    override fun onBackPressed() {

    }


    override fun onUploadUserInfoResult(uploadResult: Boolean, msg: String?) {
        mPresenter.loadingDialg.dismiss()
        if (uploadResult) {
            startActivity<UserGenderActivity>()
            finish()
        } else {
            CommonFunction.toast("$msg")
        }
    }

}
