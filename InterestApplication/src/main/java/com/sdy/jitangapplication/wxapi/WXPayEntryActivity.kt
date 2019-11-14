package com.sdy.jitangapplication.wxapi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.blankj.utilcode.util.ActivityUtils
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.ui.activity.MainActivity
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
                0 -> {
                    showAlert(0, this, "支付成功！")
//                    Toast.makeText(this, "支付成功！", Toast.LENGTH_LONG).show()
//                    finish()
//                    MainActivity.start(this,intent)
                }
                -2 -> {
                    showAlert(-2, this, "支付取消！")
//                    Toast.makeText(this, "支付取消！", Toast.LENGTH_LONG).show()
                }
                -1 -> {
                    showAlert(-1, this, "支付失败！")
//                    Toast.makeText(this, "支付失败！", Toast.LENGTH_LONG).show()
                }
                else -> {
                    showAlert(-3, this, "支付出错，请重新请求！")
//                    Toast.makeText(this, "支付出错，请重新请求！！", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onReq(p0: BaseReq?) {

    }


    private fun showAlert(code: Int, ctx: Context, info: String) {
        AlertDialog.Builder(ctx)
            .setTitle("支付结果")
            .setMessage(info)
            .setPositiveButton("确定") { p0, _ ->
                p0.cancel()
                finish()
                overridePendingTransition(0, 0)
                if (code == 0) {
                    if (ActivityUtils.getTopActivity() != MainActivity::class.java)
                        MainActivity.start(this, Intent())
                    EventBus.getDefault().postSticky(RefreshEvent(true))
                }
            }
            .show()
    }

}
