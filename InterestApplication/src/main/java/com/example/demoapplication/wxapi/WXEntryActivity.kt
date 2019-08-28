package com.example.demoapplication.wxapi

import android.os.Bundle
import com.example.demoapplication.R
import com.example.demoapplication.ui.dialog.LoadingDialog
import com.tencent.mm.opensdk.constants.ConstantsAPI.COMMAND_SENDAUTH
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.umeng.socialize.weixin.view.WXCallbackActivity
import org.jetbrains.anko.toast

/**
 * 微信回调界面
 */
class WXEntryActivity : WXCallbackActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wxentry)
    }


    override fun onResp(resp: BaseResp) {
        if (resp.type == COMMAND_SENDAUTH) {
            if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
                val code = (resp as SendAuth.Resp).code


                loading.show()
                toast("${code}")
            }
        } else {
            super.onResp(resp)//一定要加super，实现我们的方法，否则不能回调
        }
    }


    fun loginWithWechat() {

    }

    private val loading by lazy { LoadingDialog(this) }
}
