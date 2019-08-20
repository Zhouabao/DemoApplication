package com.example.demoapplication.wxapi

import android.os.Bundle
import android.widget.Toast
import com.example.demoapplication.R
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.umeng.socialize.weixin.view.WXCallbackActivity

/**
 * 微信回调界面
 */
class WXEntryActivity : WXCallbackActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wxentry)
    }

    override fun onResp(resp: BaseResp) {
        if (resp.type == ConstantsAPI.COMMAND_PAY_BY_WX) {
            when (resp.errCode) {
                0 -> Toast.makeText(this, "支付成功！", Toast.LENGTH_LONG).show()
                -2 -> Toast.makeText(this, "支付取消！", Toast.LENGTH_LONG).show()
                -1 -> Toast.makeText(this, "支付失败！", Toast.LENGTH_LONG).show()
                else -> Toast.makeText(this, "支付出错！", Toast.LENGTH_LONG).show()
            }
        } else {
            super.onResp(resp)//一定要加super，实现我们的方法，否则不能回调
        }

    }
}
