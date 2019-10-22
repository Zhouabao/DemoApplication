package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.ui.activity.IDVerifyActivity
import com.sdy.jitangapplication.ui.activity.SettingsActivity
import kotlinx.android.synthetic.main.dialog_harassment.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   :开启招呼提示
 *    version: 1.0
 */
class HarassmentDialog(val context1: Context, var from: Int) : Dialog(context1, R.style.MyDialog) {

    companion object {
        const val CHATHI = 1  //打招呼方
        const val CHATEDHI = 2//被打招呼方
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_harassment)
        initWindow()
        initView()
    }

    private fun initView() {
        when (from) {
            CHATHI -> {
                harassmentIcon.setImageResource(R.drawable.icon_open_verify)
                harassmentTitle.text = "实名认证"
                harassmentContent.text = "对方已开启认证招呼，请先进行实名认证"
                harassmentBtn.text = "前往认证"
                harassmentBtn.onClick {
                    context1.startActivity<IDVerifyActivity>()
                }

            }
            CHATEDHI -> {
                harassmentIcon.setImageResource(R.drawable.icon_open_settings)
                harassmentTitle.text = "认证招呼"
                harassmentContent.text = "当前收到的招呼过多，是否开启认证招呼\n你可以在设置中手动开启"
                harassmentBtn.text = "立即开启"
                harassmentBtn.onClick {
                    context1.startActivity<SettingsActivity>()
                }
            }
        }

        harassmentClose.onClick {
            dismiss()
        }
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogCenterAnimation
        window?.attributes = params
        setCanceledOnTouchOutside(true)

    }


}