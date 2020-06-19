package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
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
import com.sdy.jitangapplication.event.UpdateMyTicketEvent
import com.sdy.jitangapplication.model.Ticket
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_purchase_index_choiceness.*
import org.greenrobot.eventbus.EventBus

/**
 *    author : ZFM
 *    date   : 2020/6/1614:41
 *    desc   :购买置换首页精选券
 *    version: 1.0
 */
class PurchaseIndexChoicenessDialog(val context1: Context, val ticket: Ticket?) :
    Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_purchase_index_choiceness)
        initWindow()
        initView()
    }

    private fun initView() {
        choicenessTitle.text = ticket?.title ?: ""
        choicenessDate.text = ticket?.descr ?: ""
        choicenessCandy.text = "${ticket?.amount ?: 0}"

        exchangeChoicenessBtn.clickWithTrigger {
            buyTicket()
        }

        closeBtn.clickWithTrigger {
            dismiss()
        }


    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 1f
        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(false)
    }


    /**
     * 购买置顶券
     *
     * code 码                201  冲门槛会员   419  糖果余额不足  200成功
     */
    fun buyTicket() {
        val loadingDialog = LoadingDialog(context1)
        RetrofitFactory.instance
            .create(Api::class.java)
            .buyTicket(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    loadingDialog.dismiss()
                    if (t.code == 200) {
                        CommonFunction.toast(t.msg)
                        dismiss()
                        EventBus.getDefault().post(UpdateMyTicketEvent(1))
                    } else if (t.code == 201) {
                        OpenVipDialog(context1).show()
                    } else if (t.code == 419) {
                        AlertCandyEnoughDialog(
                            context1,
                            AlertCandyEnoughDialog.FROM_SEND_GIFT
                        ).show()
                    }
                }

                override fun onCompleted() {
                    super.onCompleted()
                    loadingDialog.dismiss()
                }

                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }
            })
    }
}