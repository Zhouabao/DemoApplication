package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.WindowManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.RefreshGoodsMessageEvent
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_add_and_message_layout.*
import org.greenrobot.eventbus.EventBus

/**
 *    author : ZFM
 *    date   : 2019/8/1513:59
 *    desc   : 已加入心愿单，留言
 *    version: 1.0
 */
class AddAndMessageDialog(var context1: Context, val id: Int) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWindow()
        setContentView(R.layout.dialog_add_and_message_layout)
        initview()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 0.5f
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(false)
    }

    fun initview() {
        closeBtn.onClick {
            dismiss()
        }
        publishMessage.onClick {
            goodsAddMsg()
        }

        etMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                publishMessage.isEnabled = !etMessage.text.trim().isNullOrEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })


    }

    /**
     * 添加商品留言
     */
    fun goodsAddMsg() {
        val params = hashMapOf(
            "goods_id" to id,
            "content" to etMessage.text.trim().toString()
        )
        RetrofitFactory.instance.create(Api::class.java)
            .goodsAddMsg(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    CommonFunction.toast(t.msg)
                    if (t.code == 200) {
                        EventBus.getDefault().post(RefreshGoodsMessageEvent())
                        dismiss()
                    }
                }
            })

    }

}