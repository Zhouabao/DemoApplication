package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.WindowManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_pay_problem.*


/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 支付遇到问题
 *    version: 1.0
 */
class PayProblemDialog(val myContext: Context) : Dialog(myContext, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_pay_problem)
        initWindow()
        initView()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params
        setCanceledOnTouchOutside(false)
    }


    private fun initView() {
        feedbackNotice.text = Html.fromHtml(myContext.getString(R.string.add_jitang_wechat))
        feedbackBtn.clickWithTrigger {
            if (feedbackEt.text.trim().isEmpty()) {
                dismiss()
            } else {
                payFeedback()
            }
        }
    }


    //pay_id 	    是	支付方式id	展开
    //product_id 	是	购买产品id	展开
    //order_id		是	非必串参数。例如同一商品切换支付方式就需要传
    //payment_type 支付类型 1支付宝 2微信支付 3余额支付
    private val loadingDialog by lazy { LoadingDialog(myContext) }
    private fun payFeedback() {
        val params = hashMapOf<String, Any>()
        params["comment"] = feedbackEt.text.trim()
        RetrofitFactory.instance.create(Api::class.java)
            .payFeedback(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any>>(null) {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onNext(t: BaseResp<Any>) {
                    loadingDialog.dismiss()
                    dismiss()
                }

                override fun onError(e: Throwable?) {
                    loadingDialog.dismiss()
                    CommonFunction.toast(CommonFunction.getErrorMsg(myContext))
                }
            })
    }

}