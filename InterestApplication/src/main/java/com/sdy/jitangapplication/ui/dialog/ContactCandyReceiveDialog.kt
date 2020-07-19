package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_contact_candy_receive.*

/**
 *    author : ZFM
 *    date   : 2020/4/29:36
 *    desc   : 联系方式糖果获取
 *    version: 1.0
 */
class ContactCandyReceiveDialog(
    var target_accid: String,
    var get_help_amount: Int, context: Context
) :
    Dialog(context, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_contact_candy_receive)
        initWindow()
        initView()
    }

    private fun initView() {
        t2.text = "获得${get_help_amount}糖果"
        okBtn.clickWithTrigger { dismiss() }
    }

    override fun show() {
        super.show()
        tagUnlockPopup()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.y = SizeUtils.dp2px(10F)

        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(false)
        setCancelable(true)
    }


    /**
     * 标准是否解锁联系方式弹窗
     */
    fun tagUnlockPopup() {
        RetrofitFactory.instance.create(Api::class.java)
            .tagUnlockPopup(UserManager.getSignParams(hashMapOf("target_accid" to target_accid)))
            .excute(object : BaseSubscriber<BaseResp<Any?>>() {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                }
            })
    }

}