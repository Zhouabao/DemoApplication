package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.UpdateAccountEvent
import com.sdy.jitangapplication.model.AccountBean
import com.sdy.jitangapplication.presenter.AccountAboutPresenter
import com.sdy.jitangapplication.presenter.view.AccountAboutView
import com.sdy.jitangapplication.ui.dialog.CorrectDialog
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.activity_account_about.*
import kotlinx.android.synthetic.main.correct_dialog_layout.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivityForResult

/**
 * 账号相关
 */
class AccountAboutActivity : BaseMvpActivity<AccountAboutPresenter>(), AccountAboutView, View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_about)

        initView()

        mPresenter.getAccountInfo()
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = AccountAboutPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        hotT1.text = "账号相关"
        btnBack.setOnClickListener(this)
        telChangeBtn.setOnClickListener(this)
        wechatChangeBtn.setOnClickListener(this)

        stateAccount.retryBtn.onClick {
            mPresenter.getAccountInfo()
        }
    }


    override fun onClick(view: View) {
        when (view) {
            btnBack -> {
                finish()
            }
            //更改号码
            telChangeBtn -> {
                startActivityForResult<ChangeAccountActivity>(100, "phone" to phone)
            }
            //微信绑定
            wechatChangeBtn -> {
//                CorrectDialog(this).show()
//                showDissolveDialog()
                if (wechat.isNotEmpty())//如果已经绑定，显示微信号及解除绑定按钮,点击后显示二次确认弹窗
//                    LoginOffSuccessDialog(this).show()
                    showDissolveDialog()
                else {//如果未绑定，显示未绑定及绑定按钮 ,点击后拉起微信、授权 ,完成后显示绑定成功弹窗
                    CommonFunction.wechatLogin(this, WXEntryActivity.WECHAT_AUTH)
                }
            }
        }

    }

    private fun showDissolveDialog() {
        val dissolveDialog = DeleteDialog(this)
        dissolveDialog.show()
        dissolveDialog.title.text = "解除绑定"
        dissolveDialog.tip.text = getString(R.string.dissolve_wechat)
        dissolveDialog.confirm.text = "确定"
        dissolveDialog.cancel.onClick {

            dissolveDialog.dismiss()
        }
        dissolveDialog.confirm.onClick {
            loadingDialog.show()
            mPresenter.unbundWeChat()
            dissolveDialog.dismiss()
        }
    }


    private val loadingDialog by lazy { LoadingDialog(this) }

    override fun showLoading() {
        stateAccount.viewState = MultiStateView.VIEW_STATE_LOADING
    }

    override fun hideLoading() {
        stateAccount.viewState = MultiStateView.VIEW_STATE_CONTENT
    }

    override fun onError(text: String) {
        stateAccount.viewState = MultiStateView.VIEW_STATE_ERROR
        stateAccount.errorMsg.text = text
    }


    private var phone = ""
    private var wechat = ""
    override fun getAccountResult(accountBean: AccountBean) {
        phone = accountBean.phone
        telNumber.text = accountBean.phone.replaceRange(3, 7, "****")
        if (accountBean.wechat.isNotEmpty()) {
            wechat = accountBean.wechat
            wechatNumber.text = accountBean.wechat
            wechatChangeBtn.text = "解除绑定"
        } else {
            wechatNumber.text = "未绑定"
            wechatChangeBtn.text = "绑定微信"
        }
    }


    override fun unbundWeChatResult(result: Boolean) {
        loadingDialog.dismiss()
        if (result) {
            wechat = ""
            wechatNumber.text = "未绑定"
            wechatChangeBtn.text = "绑定微信"
            val unbundDialog = CorrectDialog(this)
            unbundDialog.show()
            unbundDialog.correctTip.text = "解绑成功"
            wechatChangeBtn.postDelayed({
                unbundDialog.dismiss()
            }, 1000L)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateAccountEvent(event: UpdateAccountEvent) {
        wechat = event.account.nickname
        wechatNumber.text = wechat
        wechatChangeBtn.text = "解除绑定"
        val unbundDialog = CorrectDialog(this)
        unbundDialog.show()
        unbundDialog.correctTip.text = "绑定成功"
        wechatChangeBtn.postDelayed({
            unbundDialog.dismiss()
        }, 1000L)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                phone = data?.getStringExtra("phone") ?: ""
                telNumber.text = phone.replaceRange(3, 7, "****")
            }
        }
    }
}
