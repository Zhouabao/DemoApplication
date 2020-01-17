package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.ui.activity.NewUserInfoSettingsActivity
import kotlinx.android.synthetic.main.dialog_right_slide_out.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/11/49:45
 *    desc   : 右滑次数用尽dialog
 *    version: 1.0
 */
class RightSlideOutdDialog(val myContext: Context, var myCount: Int = 0, var maxCount: Int = 0) :
    Dialog(myContext, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_right_slide_out)
        initWindow()
        initView()
    }

    private fun initView() {
        slideOutBtn.onClick {
            myContext.startActivity<NewUserInfoSettingsActivity>()
        }
        slideOutClose.onClick {
            dismiss()
        }
        gotoPurchase.onClick {
            ChargeVipDialog(ChargeVipDialog.INFINITE_SLIDE, myContext, ChargeVipDialog.PURCHASE_VIP).show()
        }


        slideOutMin.typeface = Typeface.createFromAsset(myContext.assets, "DIN_Alternate_Bold.ttf")
        slideOutMin.text = "${myCount}"
        slideOutMax.typeface = Typeface.createFromAsset(myContext.assets, "DIN_Alternate_Bold.ttf")
        slideOutMax.text = "${maxCount}"
        slideOutContent.text = SpanUtils.with(slideOutContent)
            .append("现在去完善资料免费右滑次数将从每天${myCount}次增至每天")
            .append("$maxCount")
            .setForegroundColor(myContext.resources.getColor(R.color.colorOrange))
            .append("次")
            .setForegroundColor(Color.parseColor("#B6BABF"))
            .create()

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