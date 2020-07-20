package com.sdy.jitangapplication.ui.activity

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.presenter.RegisterGenderPresenter
import com.sdy.jitangapplication.presenter.view.RegisterGenderView
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import kotlinx.android.synthetic.main.activity_rigister_gender.*
import org.jetbrains.anko.startActivity

//注册选择性别
class RigisterGenderActivity : BaseMvpActivity<RegisterGenderPresenter>(), RegisterGenderView,
    OnLazyClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rigister_gender)

        initView()

    }

    private fun initView() {
        mPresenter = RegisterGenderPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        genderMan.setOnClickListener(this)
        genderWoman.setOnClickListener(this)
        nextBtn.setOnClickListener(this)

    }

    private var gender = 0//默认是男性
    private var alertGender = false
    override fun onLazyClick(v: View) {
        when (v.id) {
            R.id.genderMan -> {
                genderCheckedMan.isVisible = true
                genderCheckedWoman.isVisible = false
                gender = 1
                checkNextBtnEnable()
            }
            R.id.genderWoman -> {
                genderCheckedWoman.isVisible = true
                genderCheckedMan.isVisible = false
                gender = 2
                checkNextBtnEnable()
            }
            R.id.nextBtn -> {
                if (!alertGender) {
                    CommonAlertDialog.Builder(this)
                        .setTitle("提示")
                        .setContent("性别确定了就不能更改了奥")
                        .setConfirmText("我知道了")
                        .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                            override fun onClick(dialog: Dialog) {
                                mPresenter.setProfileCandy(gender)
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
                    mPresenter.setProfileCandy(gender)
                }
            }
        }
    }

    fun checkNextBtnEnable() {
        nextBtn.isEnabled = gender != 0
    }


    override fun onUploadUserInfoResult(uploadResult: Boolean, moreMatchBean: MoreMatchBean?) {
        if (uploadResult) {
            UserManager.saveGender(gender)
            //男性跳转更多配对，女性跳转个人信息
            if (gender == 1) {
                startActivity<GetMoreMatchActivity>("morematchbean" to moreMatchBean)
            } else {
                startActivity<RegisterInfoActivity>()
            }
        }
    }
}