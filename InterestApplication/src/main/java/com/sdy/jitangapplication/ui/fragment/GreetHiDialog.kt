package com.sdy.jitangapplication.ui.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.CustomerMsgBean
import com.sdy.jitangapplication.ui.dialog.VisitorsPayChatDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.GreetHiView
import kotlinx.android.synthetic.main.dialog_greet_hi.*

/**
 * 顶级通知fragment
 */
class GreetHiDialog(val msgBean: CustomerMsgBean) :
    Dialog(ActivityUtils.getTopActivity(), R.style.MyFullTransparentDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_greet_hi)
        initWindow()

        media.start()
        initNewMessages(greetHiView, msgBean)

    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.START)

        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        val leftMargin =
            (SizeUtils.dp2px(15f)..ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F + 56F)).random()
        if (leftMargin > ScreenUtils.getScreenWidth() / 2) {
            greetHiView.setDirection(GreetHiView.DIRECTION_LEFT)
        } else {
            greetHiView.setDirection(GreetHiView.DIRECTION_RIGHT)
        }
        params?.x = leftMargin
        params?.y = marginBottom[(0..1).random()]
        window?.attributes = params
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    private val media by lazy { MediaPlayer.create(context, R.raw.bubble) }
    override fun show() {
        super.show()
    }

    private val marginBottom = arrayOf(SizeUtils.dp2px(9F), SizeUtils.dp2px(89F + 9F))

    private fun initNewMessages(view1: GreetHiView, customerMsgBean: CustomerMsgBean) {
        view1.loadImg(customerMsgBean.avatar, customerMsgBean.accid)
        view1.isVisible = true
        view1.clickWithTrigger {
            if (UserManager.isUserFoot()) {
                //男性解锁聊天
                CommonFunction.checkChat(context, customerMsgBean.accid)
            } else {
                VisitorsPayChatDialog(context).show()
            }
            dismiss()

        }
        val animatorSet = AnimatorSet().apply {
            val scaleX = ObjectAnimator.ofFloat(view1, "scaleX", 0.8F, 1f, 0.8F)
            scaleX.repeatCount = 5
            scaleX.duration = 1000
            val scaleY = ObjectAnimator.ofFloat(view1, "scaleY", 0.8F, 1f, 0.8F)
            scaleY.repeatCount = 5
            scaleY.duration = 1000
            interpolator = AccelerateInterpolator()
            playTogether(scaleX, scaleY)
        }
        animatorSet.start()

        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                if (ActivityUtils.isActivityAlive(ActivityUtils.getTopActivity()) && isShowing)
                    dismiss()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {

            }

        })
    }

    override fun dismiss() {
        if (media.isPlaying) {
            media.stop()
            media.release()
        }
        super.dismiss()

    }

}
