package com.sdy.jitangapplication.wxapi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.ui.activity.MainActivity
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
                0 -> {
                    showAlert(this, "支付成功！")
                }
                -2 -> {
                    showAlert(this, "支付取消！")
                }
                -1 -> {
                    showAlert(this, "支付失败！")
                }
                else -> {
                    showAlert(this, "支付出错，请重新请求！")
                }
            }
        }
    }

    override fun onReq(p0: BaseReq?) {

    }


    private fun showAlert(ctx: Context, info: String) {
        AlertDialog.Builder(ctx)
            .setTitle("支付结果")
            .setMessage(info)
            .setPositiveButton("确定") { p0, _ ->
                p0.cancel()
                finish()
                MainActivity.start(this, Intent())
            }
            .show()
    }

}
