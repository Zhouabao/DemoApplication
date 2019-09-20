package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import androidx.core.view.isVisible
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_guide.*

/**
 *    author : ZFM
 *    date   : 2019/9/1917:18
 *    desc   :
 *    version: 1.0
 */
class GuideDialog(context: Context) : Dialog(context, R.style.MyFullTransparentDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_guide)
        initWindow()
        initView()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = params
        setCanceledOnTouchOutside(false)
        setOnKeyListener { dialogInterface, keyCode, event ->
            keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0
        }
    }


    private fun initView() {
        guideLast.onClick {
            guideCl.isVisible = true
            guideLast.isVisible = false
            guideNext.isVisible = true
            guideDetail.isVisible = false
            guideHi.isVisible = false
            useCl.isVisible = false
        }

        guideNext.onClick {
            guideCl.isVisible = true
            guideLast.isVisible = false
            guideNext.isVisible = false
            guideDetail.isVisible = true
            guideHi.isVisible = false
            useCl.isVisible = false
        }

        guideDetail.onClick {
            guideCl.isVisible = true
            guideLast.isVisible = false
            guideNext.isVisible = false
            guideDetail.isVisible = false
            guideHi.isVisible = true
            useCl.isVisible = false
        }

        guideHi.onClick {
            guideCl.isVisible = true
            guideLast.isVisible = false
            guideNext.isVisible = false
            guideDetail.isVisible = false
            guideHi.isVisible = false
            guideCl.isVisible = false
            useCl.isVisible = true
        }

        onceAgain.onClick {
            guideCl.isVisible = true
            guideLast.isVisible = true
            guideNext.isVisible = false
            guideDetail.isVisible = false
            guideHi.isVisible = false
            useCl.isVisible = false
        }
        startUse.onClick {
            dismiss()
            UserManager.saveShowGuide(true)
        }
    }
}