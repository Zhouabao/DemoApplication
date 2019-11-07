package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.presenter.UserNickNamePresenter
import com.sdy.jitangapplication.presenter.view.UserNickNameView
import kotlinx.android.synthetic.main.activity_user_nick_name.*
import org.jetbrains.anko.startActivity

/**
 * 用户昵称
 * 必传参数 1昵称 2生日 3性别 4头像
 */
class UserNickNameActivity : BaseMvpActivity<UserNickNamePresenter>(), UserNickNameView, View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_nick_name)
        initView()
    }

    private fun initView() {
        mPresenter = UserNickNamePresenter()
        mPresenter.context = this
        mPresenter.mView = this

        btnNextStep.setOnClickListener(this)
        nickNameClean.setOnClickListener(this)


        nickNameEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable) {
                if (p0.isEmpty()) {
                    nickNameNotify.setTextColor(resources.getColor(R.color.colorBlack4C))
                    nickNameBg.setBackgroundColor(resources.getColor(R.color.colorDividerC4))
                    nickNameNotify.text = "现在为自己取一个有趣的名字吧"
                    nickNameClean.isVisible = false
                } else {
                    nickNameBg.setBackgroundColor(resources.getColor(R.color.colorOrange))
                    nickNameClean.isVisible = true
                }
                btnNextStep.isEnabled = p0.isNotEmpty()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })

    }


    override fun onUploadUserInfoResult(uploadResult: Boolean, msg: String?) {
        mPresenter.loadingDialg.dismiss()
        if (uploadResult) {
            startActivity<UserBirthActivity>()
            finish()
        } else {
            if (msg != null) {
                nickNameNotify.setTextColor(resources.getColor(R.color.colorRed27))
                nickNameNotify.text = msg
            }
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnNextStep -> {
                mPresenter.loadingDialg.show()
                mPresenter.uploadUserInfo(1, hashMapOf("nickname" to nickNameEt.text.toString()))
            }
            R.id.nickNameClean -> {
                nickNameEt.setText("")
            }
        }

    }

    override fun onBackPressed() {

    }

}
