package com.sdy.jitangapplication.wxapi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.blankj.utilcode.util.ActivityUtils
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.PayLabelResultEvent
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.event.UpdateMyLabelEvent
import com.sdy.jitangapplication.event.UserCenterEvent
import com.sdy.jitangapplication.ui.activity.AddLabelActivity
import com.sdy.jitangapplication.ui.activity.MainActivity
import com.sdy.jitangapplication.ui.activity.MyLabelActivity
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
        AlertDialog.Builder(ctx)
            .setTitle("支付结果")
            .setMessage(info)
            .setPositiveButton("确定") { p0, _ ->
                p0.cancel()
                finish()
                overridePendingTransition(0, 0)
                if (code == BaseResp.ErrCode.ERR_OK) {
                    if (ActivityUtils.getTopActivity() == AddLabelActivity::class.java) {
                        EventBus.getDefault().post(PayLabelResultEvent(code == BaseResp.ErrCode.ERR_OK))
                    } else if (ActivityUtils.getTopActivity() == MyLabelActivity::class.java) {
                        EventBus.getDefault().post(UpdateMyLabelEvent())
                    } else {
                        if (ActivityUtils.getTopActivity() != MainActivity::class.java) {
                            MainActivity.start(this, Intent())
                        }
                    }
                    EventBus.getDefault().postSticky(RefreshEvent(true))
                    EventBus.getDefault().postSticky(UserCenterEvent(true))
                }
            }
            .show()
    }

}
