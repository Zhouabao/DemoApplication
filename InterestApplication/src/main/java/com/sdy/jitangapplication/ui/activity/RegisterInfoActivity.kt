package com.sdy.jitangapplication.ui.activity

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.TimeUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.model.MyPhotoBean
import com.sdy.jitangapplication.presenter.UserNickNamePresenter
import com.sdy.jitangapplication.presenter.view.UserNickNameView
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import kotlinx.android.synthetic.main.activity_register_info.*
import org.jetbrains.anko.startActivity
import java.text.SimpleDateFormat
import java.util.*


/**
 * 介绍一下自己吧
 */
class RegisterInfoActivity : BaseMvpActivity<UserNickNamePresenter>(), UserNickNameView,
    OnLazyClickListener {

    private val genders by lazy { mutableListOf("男", "女") }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_info)

        initView()
    }

    private fun initView() {
        mPresenter = UserNickNamePresenter()
        mPresenter.mView = this
        mPresenter.context = this
        setSwipeBackEnable(false)


        userGender.setOnClickListener(this)
        userBirth.setOnClickListener(this)
        nextBtn.setOnClickListener(this)

        userNickName.setFilters(arrayOf<InputFilter>(InputFilter { source, start, end, dest, dstart, dend ->
            if (source.equals(" ") || source.toString().contentEquals("\n")) {
                ""
            } else {
                null
            }
        }))
        userNickName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkConfirmEnable()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
        userSign.addTextChangedListener {
            checkConfirmEnable()
        }

        CommonFunction.startAnimation(moreInfoTitle)
        t2.postDelayed({
            CommonFunction.startAnimation(t2)
        }, 50L)
        clName.postDelayed({
            CommonFunction.startAnimation(clName)
        }, 100L)

        clGender.postDelayed({
            CommonFunction.startAnimation(clGender)
        }, 150L)

        clBirth.postDelayed({
            CommonFunction.startAnimation(clBirth)
        }, 200L)

        t6.postDelayed({
            CommonFunction.startAnimation(t6)
        }, 250L)

        userSign.postDelayed({
            CommonFunction.startAnimation(userSign)
        }, 300L)
    }

    private fun checkConfirmEnable() {
        nextBtn.isEnabled = userGender.text.isNotEmpty()
                && userBirth.text.isNotEmpty()
                && userNickName.text.isNotEmpty()
                && userSign.text.isNotEmpty()
    }

    override fun onLazyClick(v: View) {
        when (v.id) {
            R.id.userGender -> {
                KeyboardUtils.hideSoftInput(this)
                showGenderPicker()
            }
            R.id.userBirth -> {
                KeyboardUtils.hideSoftInput(this)
                showBirthdayPicker()
            }
            R.id.nextBtn -> {
                if (!alertGender) {
                    CommonAlertDialog.Builder(this)
                        .setTitle("提示")
                        .setContent("性别确定了就不能更改了奥")
                        .setConfirmText("我知道了")
                        .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                            override fun onClick(dialog: Dialog) {
                                params["nickname"] = userNickName.text.trim().toString()
                                params["sign"] = userSign.text.trim().toString()
                                mPresenter.setProfileCandy(1, params)
                                dialog.dismiss()
                            }
                        })
                        .setOnCancelListener(object : CommonAlertDialog.OnCancelListener {
                            override fun onClick(dialog: Dialog) {
                                dialog.dismiss()
                                alertGender = true
                            }
                        })
                        .setCancelAble(true)
                        .setCancelText("取消")
                        .create()
                        .show()
                } else {
                    params["nickname"] = userNickName.text.trim().toString()
                    params["sign"] = userSign.text.trim().toString()
                    mPresenter.setProfileCandy(1, params)
                }

            }
        }

    }


    private val params by lazy { hashMapOf<String, Any>() }
    /**
     * 展示日历
     */
    //错误使用案例： startDate.set(2013,1,1);  endDate.set(2020,12,1);
    //正确使用案例： startDate.set(2013,0,1);  endDate.set(2020,11,1);
    private fun showBirthdayPicker() {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        startDate.set(endDate.get(Calendar.YEAR) - 50, 0, 1)
        endDate.set(
            endDate.get(Calendar.YEAR) - 18, endDate.get(Calendar.MONTH), endDate.get(
                Calendar.DATE
            )
        )
        val clOptions = TimePickerBuilder(this, OnTimeSelectListener { date, v ->
            //            getZodiac
            userBirth.text =
                "${TimeUtils.date2String(
                    date,
                    SimpleDateFormat("yyyy-MM-dd")
                )}/${TimeUtils.getZodiac(date)}"
            params["birth"] = TimeUtils.date2Millis(date) / 1000L
            checkConfirmEnable()
        })
            .setRangDate(startDate, endDate)
            .setDate(endDate)
            .setTitleText("生日")
            .setTitleColor(Color.BLACK)//标题文字颜色
            .build()
        clOptions.show()
    }

    private var alertGender = false
    /**
     * 展示条件选择器
     */
    private fun showGenderPicker() {
        //条件选择器
        val pvOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v ->
                userGender.text = genders[options1]
                params["gender"] = options1 + 1
                checkConfirmEnable()
            })
            .setSubmitText("确定")
            .setTitleText("性别")
            .setTitleColor(resources.getColor(R.color.colorBlack))
            .setTitleSize(16)
            .setDividerColor(resources.getColor(R.color.colorDivider))
            .setContentTextSize(20)
            .setDecorView(window.decorView.findViewById(android.R.id.content) as ViewGroup)
            .setSubmitColor(resources.getColor(R.color.colorBlueSky1))
            .build<String>()

        pvOptions.setPicker(genders)
        pvOptions.show()
    }

    override fun onUploadUserInfoResult(
        uploadResult: Boolean,
        msg: String?,
        moreMatchBean: MoreMatchBean?
    ) {
        if (uploadResult) {
            SPUtils.getInstance(Constants.SPNAME).put("gender", params["gender"] as Int)
            startActivity<UserAvatorActivity>()
        } else {
            CommonFunction.toast(msg ?: "")
        }

    }

    override fun uploadImgResult(ok: Boolean, key: String) {

    }

    override fun onAddPhotoWallResult(data: MyPhotoBean) {

    }

    override fun onRegisterAddPhoto(data: MoreMatchBean?) {

    }

    override fun onBackPressed() {

    }
}
