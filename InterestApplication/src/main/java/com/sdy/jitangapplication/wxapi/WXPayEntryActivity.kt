package com.sdy.jitangapplication.wxapi

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory

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
                    showAlert(0, this, "支付成功！")
                }
                BaseResp.ErrCode.ERR_USER_CANCEL -> {
                    showAlert(-2, this, "支付取消！")
                }
                BaseResp.ErrCode.ERR_COMM -> {
                    showAlert(-1, this, "支付失败！")
                }
                else -> {
                    showAlert(-3, this, "支付出错，请重新请求！")
                }
            }
        }
    }

    override fun onReq(p0: BaseReq?) {

    }


    private fun showAlert(code: Int, ctx: Context, info: String) {
        CommonAlertDialog.Builder(ctx)
            .setTitle("支付结果")
            .setContent(info)
            .setCancelIconIsVisibility(false)
            .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                override fun onClick(dialog: Dialog) {
                    dialog.cancel()
                    finish()
                    overridePendingTransition(0, 0)
                    if (code == BaseResp.ErrCode.ERR_OK) {
                        //TODO 刷新糖果相关界面 我的糖果 糖果商城 糖果详情
                        CommonFunction.payResultNotify(this@WXPayEntryActivity)
                    }
                }
            })
            .create()
            .show()
    }

}
