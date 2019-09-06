package com.sdy.jitangapplication.wxapi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.sdy.jitangapplication.common.Constants
import com.kotlin.base.ui.activity.BaseActivity
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
                    Toast.makeText(this, "支付成功！", Toast.LENGTH_LONG).show()
                    finish()
                }
                -2 -> {
                    Toast.makeText(this, "支付取消！", Toast.LENGTH_LONG).show()
                    finish()
                }
                -1 -> {
                    Toast.makeText(this, "支付失败！", Toast.LENGTH_LONG).show()
                    finish()
                }
                else -> {
                    Toast.makeText(this, "支付出错！", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    override fun onReq(p0: BaseReq?) {

    }


}
