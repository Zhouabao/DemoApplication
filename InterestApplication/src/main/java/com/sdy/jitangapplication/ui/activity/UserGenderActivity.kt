package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.presenter.UserNickNamePresenter
import com.sdy.jitangapplication.presenter.view.UserNickNameView
import kotlinx.android.synthetic.main.activity_user_gender.*
import org.jetbrains.anko.startActivity

/**
 * 用户性别  3
 */
class UserGenderActivity : BaseMvpActivity<UserNickNamePresenter>(), UserNickNameView, View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_gender)
        initView()
    }

    private fun initView() {
        setSwipeBackEnable(false)
        mPresenter = UserNickNamePresenter()
        mPresenter.mView = this
        mPresenter.context = this
        rlWoman.setOnClickListener(this)
        rlMan.setOnClickListener(this)
        btnNextStep.setOnClickListener(this)
        help.setOnClickListener(this)
    }


    override fun onUploadUserInfoResult(uploadResult: Boolean, msg: String?) {
        mPresenter.loadingDialg.dismiss()
        if (uploadResult) {
            startActivity<UserAvatorActivity>()
            finish()
        }
    }


    private var gender = -1
    override fun onClick(view: View) {
        when (view.id) {
            R.id.help -> {
                startActivity<LoginHelpActivity>()

            }
            R.id.rlWoman -> {
                womanIv.isVisible = true
                womanTv.setTextColor(resources.getColor(R.color.colorBlack))
                manIv.isVisible = false
                manTv.setTextColor(resources.getColor(R.color.colorGrayC5))
                gender = 2
                btnNextStep.isEnabled = true

            }
            R.id.rlMan -> {
                manIv.isVisible = true
                manTv.setTextColor(resources.getColor(R.color.colorBlack))
                womanIv.isVisible = false
                womanTv.setTextColor(resources.getColor(R.color.colorGrayC5))
                gender = 1
                btnNextStep.isEnabled = true

            }
            R.id.btnNextStep -> {
                mPresenter.loadingDialg.show()
                mPresenter.uploadUserInfo(3, hashMapOf("gender" to gender))

            }
        }
    }


    override fun onBackPressed() {

    }
}
