package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import kotlinx.android.synthetic.main.dialog_purchase_index_choiceness.*

/**
 *    author : ZFM
 *    date   : 2020/6/1614:41
 *    desc   :购买置换首页精选券
 *    version: 1.0
 */
class PurchaseIndexChoicenessDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_purchase_index_choiceness)
        initWindow()
        initView()
    }

    private fun initView() {
        exchangeChoicenessBtn.clickWithTrigger {

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
}