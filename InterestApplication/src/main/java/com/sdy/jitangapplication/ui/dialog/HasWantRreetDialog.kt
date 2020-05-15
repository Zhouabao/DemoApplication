package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.GiftBean
import kotlinx.android.synthetic.main.dialog_has_want_greet.*

/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 对方设置了今日意向
 *    version: 1.0
 */
class HasWantRreetDialog(
    context: Context,
    val targetAccid: String,
    val targetAvator: String,
    val needSwipe: Boolean,
    val position: Int,
    val nickName: String,
    val greetAmount: Int,
    val myCandyAmount: Int,
    val goodWish: GiftBean? = null
) : Dialog(context, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_has_want_greet)
        initWindow()
        initView()
    }

    private fun initView() {
        cancel.onClick {
            dismiss()
        }

        candySendBtn.text = "${greetAmount}糖果"
        view1.isVisible = goodWish != null && goodWish.id != 0
        giftHelpChatBtn.isVisible = goodWish != null && goodWish.id != 0
        //礼物助力
        giftHelpChatBtn.clickWithTrigger {
            dismiss()
            HelpWishDialog(
                myCandyAmount,
                targetAccid,
                nickName,
                goodWish!!,
                context,
                false,
                true
            ).show()
        }

        //糖果助力
        candySendBtn.onClick {
            dismiss()
            CommonFunction.greet(
                targetAccid,
                null,
                context,
                needSwipe,
                position,
                targetAvator
            )
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
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }


}