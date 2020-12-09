package com.sdy.jitangapplication.wxapi

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.ActivityUtils
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.CloseDialogEvent
import com.sdy.jitangapplication.event.CloseRegVipEvent
import com.sdy.jitangapplication.ui.dialog.OpenVipActivity
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import org.greenrobot.eventbus.EventBus

class WXPayEntryActivity : BaseActivity(), IWXAPIEventHandler {
    private val wxApi by lazy { WXAPIFactory.createWXAPI(this, Constants.WECHAT_APP_ID) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wxApi.handleIntent(intent, this)

    }

    override fun onNewIntent(paramIntent: Intent?) {
        super.onNewIntent(paramIntent)
        intent = paramIntent
        wxApi.handleIntent(intent, this)
    }

    override fun onResp(resp: BaseResp) {
        if (resp.type == ConstantsAPI.COMMAND_PAY_BY_WX) {
            when (resp.errCode) {
                BaseResp.ErrCode.ERR_OK -> {
                    showAlert(0, this, getString(R.string.pay_success))
                }
                BaseResp.ErrCode.ERR_USER_CANCEL -> {
                    if (UserManager.registerFileBean?.experience_state == true) {
                        finish()
                        overridePendingTransition(0, 0)
                        if (ActivityUtils.getTopActivity() is OpenVipActivity) {
                            EventBus.getDefault().post(CloseDialogEvent())
                            EventBus.getDefault().post(CloseRegVipEvent(false))
                        }
                    } else {
                        showAlert(-2, this, getString(R.string.pay_cancel))
                    }
                }
                BaseResp.ErrCode.ERR_COMM -> {
                    showAlert(-1, this, getString(R.string.pay_cancel))
                }
                else -> {
                    showAlert(-3, this, getString(R.string.pay_error))
                }
            }
        }
    }

    override fun onReq(p0: BaseReq?) {

    }


    private fun showAlert(code: Int, ctx: Context, info: String) {
        CommonAlertDialog.Builder(ctx)
            .setTitle(getString(R.string.pay_result))
            .setContent(info)
            .setCancelIconIsVisibility(false)
            .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                override fun onClick(dialog: Dialog) {
                    dialog.cancel()
                    finish()
                    overridePendingTransition(0, 0)
                    if (code == BaseResp.ErrCode.ERR_OK)
                        CommonFunction.payResultNotify(this@WXPayEntryActivity)
                }
            })
            .create()
            .show()
    }

}
