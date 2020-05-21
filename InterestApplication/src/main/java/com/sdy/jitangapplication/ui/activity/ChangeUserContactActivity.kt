package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.blankj.utilcode.util.KeyboardUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.model.ContactWayBean
import com.sdy.jitangapplication.presenter.ChangeUserContactPresenter
import com.sdy.jitangapplication.presenter.view.ChangeUserContactView
import kotlinx.android.synthetic.main.activity_change_user_contact.*
import kotlinx.android.synthetic.main.layout_actionbar.*


/**
 * 更改用户联系方式
 */
class ChangeUserContactActivity : BaseMvpActivity<ChangeUserContactPresenter>(),
    ChangeUserContactView, OnLazyClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_user_contact)

        initView()
        mPresenter.getContact()
    }

    private fun initView() {
        mPresenter = ChangeUserContactPresenter()
        mPresenter.mView = this
        mPresenter.context = this


        rightBtn1.isVisible = true
        hotT1.text = "更改联系方式"
        rightBtn1.text = "保存"

        contactImg.setOnClickListener(this)
        contactImgMore.setOnClickListener(this)
        btnBack.setOnClickListener(this)
        rightBtn1.setOnClickListener(this)

        contactEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                rightBtn1.isEnabled = !s.toString().isNullOrEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

    }

    override fun onLazyClick(v: View) {
        when (v.id) {
            R.id.contactImgMore, R.id.contactImg -> {
                KeyboardUtils.hideSoftInput(this)
                showContactPicker()
            }
            R.id.rightBtn1 -> {
                mPresenter.setContact(
                    contactWay,
                    contactEt.text.trim().toString(),
                    if (switchShowContact.isChecked) {
                        2
                    } else {
                        1
                    }
                )
            }
            R.id.btnBack -> {
                finish()
            }
        }

    }


    /**
     * 展示联系方式
     * 1 电话 2 微信 3 qq 99隐藏
     */
    private var contactWay = 1 //1 电话 2 微信 3 qq 99隐藏
    private val contactWays by lazy { mutableListOf("手机号", "微信", "QQ") }
    private val contactWaysIcon by lazy {
        mutableListOf(
            R.drawable.icon_phone_circle,
            R.drawable.icon_wechat_circle,
            R.drawable.icon_qq_circle
        )
    }

    private fun showContactPicker() {
        //条件选择器
        val pvOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v ->
                contactImg.setImageResource(contactWaysIcon[options1])
                contactWay = options1 + 1
            })
            .setSubmitText("确定")
            .setTitleText("联系方式")
            .setTitleColor(resources.getColor(R.color.colorBlack))
            .setTitleSize(16)
            .setDividerColor(resources.getColor(R.color.colorDivider))
            .setContentTextSize(20)
            .setDecorView(window.decorView.findViewById(android.R.id.content) as ViewGroup)
            .setSubmitColor(resources.getColor(R.color.colorBlueSky1))
            .build<String>()

        pvOptions.setPicker(contactWays)
        pvOptions.setSelectOptions(1)
        pvOptions.show()
    }

    override fun onGetContactResult(data: ContactWayBean?) {
        if (data != null) {
            //	1 显示 2隐藏
            if (data.contact_way != 0 && data.contact_way != 99) {
                contactWay = data.contact_way
                contactImg.setImageResource(contactWaysIcon[data.contact_way - 1])
                contactEt.setText(data.contact_way_content)
                contactEt.setSelection(contactEt.text.length)
            }

//            是隐藏就开，默认不隐藏
            switchShowContact.isChecked = data.contact_way_hide == 2
        }

    }

    override fun onSetContactResult(success: Boolean) {
        if (success) {
            CommonFunction.toast("修改成功！")
        }
    }
}
