package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import com.blankj.utilcode.util.KeyboardUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.presenter.CustomChatUpContentPresenter
import com.sdy.jitangapplication.presenter.view.CustomChatUpContentView
import com.sdy.jitangapplication.ui.dialog.CorrectDialog
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import kotlinx.android.synthetic.main.activity_chat_up_content.*
import kotlinx.android.synthetic.main.correct_dialog_layout.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 个人介绍
 */
class CustomChatUpContentActivity : BaseMvpActivity<CustomChatUpContentPresenter>(),
    CustomChatUpContentView, View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_up_content)
        initView()

    }

    override fun onPause() {
        super.onPause()
        KeyboardUtils.hideSoftInputByToggle(this)
    }

    override fun onResume() {
        super.onResume()
        chatupEt.postDelayed(
            { KeyboardUtils.showSoftInput(chatupEt) },
            200L
        )
    }

    private fun initView() {
        mPresenter = CustomChatUpContentPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        hotT1.text = getString(R.string.custom_chatup)
        btnBack.setOnClickListener(this)
        rightBtn1.setOnClickListener(this)
        rightBtn1.isVisible = true
        rightBtn1.setBackgroundResource(R.drawable.selector_confirm_btn_25dp)
        rightBtn1.text = getString(R.string.save)
        rightBtn1.isEnabled = false


        chatupEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                checkSaveEnable()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

    }

    override fun onClick(view: View) {
        when (view) {
            btnBack -> {
                finish()
            }
            rightBtn1 -> {
                mPresenter.saveChatupMsg(chatupEt.text.trim().toString())
                KeyboardUtils.hideSoftInputByToggle(this)
            }

        }
    }


    fun checkSaveEnable() {
        rightBtn1.isEnabled = chatupEt.text.trim().isNotEmpty()
    }

    private val loading by lazy { LoadingDialog(this) }

    override fun showLoading() {
        chatupErrorTv.isVisible = false
        chatupErrorTv.text = ""
        loading.show()
    }

    override fun hideLoading() {
        loading.dismiss()
    }

    private val saveDialog by lazy { CorrectDialog(this) }
    override fun onSaveChatupMsg(success: Boolean, msg: String) {
        if (success) {
            saveDialog.show()
            saveDialog.correctTip.text = getString(R.string.save_success)
            chatupEt.postDelayed({
                saveDialog.dismiss()
            }, 500L)
        } else {
            chatupErrorTv.isVisible = true
            chatupErrorTv.text = msg
        }
    }

}
