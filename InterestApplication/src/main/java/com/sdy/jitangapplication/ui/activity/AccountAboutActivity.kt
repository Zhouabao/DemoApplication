package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.view.View
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.presenter.AccountAboutPresenter
import com.sdy.jitangapplication.presenter.view.AccountAboutView
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.ui.dialog.LoginOffSuccessDialog
import kotlinx.android.synthetic.main.activity_account_about.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity

/**
 * 账号相关
 */
class AccountAboutActivity : BaseMvpActivity<AccountAboutPresenter>(), AccountAboutView, View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_about)

        initView()
    }

    private fun initView() {
        mPresenter = AccountAboutPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        hotT1.text = "账号相关"

        btnBack.setOnClickListener(this)
        telChangeBtn.setOnClickListener(this)
        wechatChangeBtn.setOnClickListener(this)
    }


    override fun onClick(view: View) {
        when (view) {
            btnBack -> {
                finish()
            }
            //更改号码
            telChangeBtn -> {
                startActivity<ChangeAccountActivity>()
            }
            //微信绑定
            //todo 如果已经绑定，显示微信号及解除绑定按钮,点击后显示二次确认弹窗
            //TODO 如果未绑定，显示未绑定及绑定按钮 ,点击后拉起微信、授权 ,完成后显示绑定成功弹窗
            wechatChangeBtn -> {
//                CorrectDialog(this).show()
//                showDissolveDialog()
                LoginOffSuccessDialog(this).show()
            }
        }

    }

    private fun showDissolveDialog() {
        val dissolveDialog = DeleteDialog(this)
        dissolveDialog.show()
        dissolveDialog.tip.text = getString(R.string.dissolve_wechat)
        dissolveDialog.confirm.text = "确定"
        dissolveDialog.cancel.onClick { dissolveDialog.dismiss() }
        dissolveDialog.confirm.onClick {
            dissolveDialog.dismiss()
        }
    }

}
