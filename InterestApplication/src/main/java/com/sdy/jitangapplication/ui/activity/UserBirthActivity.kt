package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.presenter.UserBirthPresenter
import com.sdy.jitangapplication.presenter.view.UserBirthView
import kotlinx.android.synthetic.main.activity_user_birth.*
import java.util.*


/**
 * 填写用户生日界面（计算年龄和星座）
 */
class UserBirthActivity : BaseMvpActivity<UserBirthPresenter>(), UserBirthView {
    private var year: String? = null
    private var monthAndDay: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_birth)

        initView()
    }

    private fun initView() {
        mPresenter = UserBirthPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        btnBack.onClick { finish() }
        userBirthYear.listener = { year, complete ->
            this.year = year
            if (complete) {
                calculateAgeAndAstro()
                //年份输入完成后，让月日获取焦点
                userBirthMonth.postDelayed({ userBirthMonth.requestEditeFocus() }, 200L)

            } else if (!complete && userAge.visibility == View.VISIBLE) {
                confirmBtn.isEnabled = false
                userAge.text = ""
                userAge.visibility = View.GONE
                userXingzuo.text = ""
                userXingzuo.visibility = View.GONE
            }
        }

        userBirthMonth.listener = { monthAndDay, complete ->
            this.monthAndDay = monthAndDay
            if (complete) {
                calculateAgeAndAstro()
            } else if (!complete && userAge.visibility == View.VISIBLE) {
                confirmBtn.isEnabled = false
                userAge.text = ""
                userAge.visibility = View.GONE
                userXingzuo.text = ""
                userXingzuo.visibility = View.GONE
            }
        }

        confirmBtn.onClick {
            setResult(Activity.RESULT_OK, intent.putExtra("year", "$year").putExtra("month", "$monthAndDay"))
            finish()
        }
    }

    /**
     * itemType :1年 2月
     */
    private fun calculateAgeAndAstro() {
        if (year == null || year!!.isEmpty()) {
            return
        }
        if (monthAndDay == null || monthAndDay!!.isEmpty() || monthAndDay!!.length != 4) {
            return
        }

        if (!judgeYear(year!!.toInt())) { //年份不正确
            userBirthYear.clear()
            userAge.text = ""
            userAge.visibility = View.GONE
            return
        }
        val month = monthAndDay!!.substring(0, 2).toInt()
        val day = monthAndDay!!.substring(2, 4).toInt()
        if (!judgeBirth(month, day)) { //月份不正确
            userBirthMonth.clear()
            userXingzuo.text = ""
            userXingzuo.visibility = View.GONE
            return
        }

        //年月日都正确的情况下 计算年龄
        userAge.text = "${Calendar.getInstance().get(Calendar.YEAR) - year!!.toInt()}"
        userAge.visibility = View.VISIBLE
        userXingzuo.text = calculateAstro(month, day)
        userXingzuo.visibility = View.VISIBLE
        confirmBtn.isEnabled = true


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
            return false
        }
        when (month) {
            1, 3, 5, 7, 8, 10, 12 ->
                if (day > 31 || day == 0) {
                    CommonFunction.toast("请输入正确的日期！")
                    return false
                }
            4, 6, 9, 11 -> {
                if (day > 30 || day == 0) {
                    CommonFunction.toast("请输入正确的日期！")
                    return false
                }
            }
            2 -> {
                if (year!!.toInt() % 4 == 0 && year!!.toInt() % 100 != 0 || year!!.toInt() % 400 == 0) {
                    if (day > 29 || day == 0) {
                        CommonFunction.toast("请输入正确的日期！")
                        return false
                    }
                } else if (day > 28 || day == 0) {
                    CommonFunction.toast("请输入正确的日期！")
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

}
