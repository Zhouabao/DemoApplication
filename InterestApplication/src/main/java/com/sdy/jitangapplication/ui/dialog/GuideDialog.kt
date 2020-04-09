package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import androidx.core.view.isVisible
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.ui.activity.MyCandyActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_guide.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/9/1917:18
 *    desc   :
 *    version: 1.0
 */
class GuideDialog(context: Context) : Dialog(context, R.style.MyDialog) {
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
        onceAgain.text = if (UserManager.getGender() == 1) {
            "没有看懂？再来一次"
        } else {
            "开启积糖之旅"
        }
        startUse.text = if (UserManager.getGender() == 1) {
            "开启积糖之旅"
        } else {
            "糖果有什么用？"
        }

        last.startAnimation(
            (AnimationUtils.loadAnimation(
                context,
                R.anim.anim_small_to_big
            ) as ScaleAnimation)
        )
        next.startAnimation(
            (AnimationUtils.loadAnimation(
                context,
                R.anim.anim_small_to_big
            ) as ScaleAnimation)
        )
        detail.startAnimation(
            (AnimationUtils.loadAnimation(
                context,
                R.anim.anim_small_to_big
            ) as ScaleAnimation)
        )

        guide_dislike_hand_left.startAnimation(
            (AnimationUtils.loadAnimation(
                context,
                R.anim.anim_left_to_right
            ) as TranslateAnimation)
        )
        guide_like_hand_right.startAnimation(
            (AnimationUtils.loadAnimation(
                context,
                R.anim.anim_right_to_left
            ) as TranslateAnimation)
        )




        guideLast.onClick {
            guideCl.isVisible = true
            guideLast.isVisible = false
            guideNext.isVisible = true
            guideDetail.isVisible = false
            chatTv.isVisible = false
            useCl.isVisible = false
            guideLike.isVisible = false
            guideDislike.isVisible = false
            intentionMatchingCl.isVisible = false
        }

        guideNext.onClick {
            guideCl.isVisible = true
            guideLast.isVisible = false
            guideNext.isVisible = false
            guideDetail.isVisible = true
            chatTv.isVisible = false
            useCl.isVisible = false
            guideLike.isVisible = false
            guideDislike.isVisible = false
            intentionMatchingCl.isVisible = false
        }

        guideDetail.onClick {
            guideCl.isVisible = true
            guideLast.isVisible = false
            guideNext.isVisible = false
            guideDetail.isVisible = false
            chatTv.isVisible = true
            btnChat.isVisible = true
            useCl.isVisible = false
            guideLike.isVisible = false
            guideDislike.isVisible = false
            intentionMatchingCl.isVisible = false

        }

        chatTv.onClick {
            guideCl.isVisible = true
            guideLast.isVisible = false
            guideNext.isVisible = false
            guideDetail.isVisible = false
            chatTv.isVisible = false
            btnChat.isVisible = false
            guideCl.isVisible = false
            guideLike.isVisible = true
            guideDislike.isVisible = false
            useCl.isVisible = false
            intentionMatchingCl.isVisible = false
        }

        btnChat.onClick {
            guideCl.isVisible = true
            guideLast.isVisible = false
            guideNext.isVisible = false
            guideDetail.isVisible = false
            chatTv.isVisible = false
            btnChat.isVisible = false
            guideCl.isVisible = false
            guideLike.isVisible = true
            guideDislike.isVisible = false
            useCl.isVisible = false
            intentionMatchingCl.isVisible = false
        }

        guideLike.onClick {
            guideCl.isVisible = false
            guideLike.isVisible = false
            guideDislike.isVisible = true
            useCl.isVisible = false
            intentionMatchingCl.isVisible = false

        }
        guideDislike.onClick {
            guideCl.isVisible = false
            guideLike.isVisible = false
            guideDislike.isVisible = false
//            useCl.isVisible = false
            useCl.isVisible = true
            welldone.playAnimation()

            intentionMatchingCl.isVisible = false
//            intentionMatchingCl.isVisible = true
        }


        intentionMatchingCl.onClick {
            guideCl.isVisible = false
            guideLike.isVisible = false
            guideDislike.isVisible = false
            intentionMatchingCl.isVisible = false
            useCl.isVisible = true
        }

        onceAgain.onClick {
            if (UserManager.getGender() == 1) { //1-男 2-女
                guideCl.isVisible = true
                guideLast.isVisible = true
                guideNext.isVisible = false
                guideDetail.isVisible = false
                guideLike.isVisible = false
                guideDislike.isVisible = false
                useCl.isVisible = false
                intentionMatchingCl.isVisible = false
            } else {
                last.clearAnimation()
                next.clearAnimation()
                detail.clearAnimation()
                guide_dislike_hand_left.clearAnimation()
                guide_like_hand_right.clearAnimation()
                UserManager.saveShowGuideIndex(true)
                dismiss()
            }

        }
        startUse.onClick {
            if (UserManager.getGender() == 1) {
                last.clearAnimation()
                next.clearAnimation()
                detail.clearAnimation()
                guide_dislike_hand_left.clearAnimation()
                guide_like_hand_right.clearAnimation()
                UserManager.saveShowGuideIndex(true)
                dismiss()
            } else {
                context.startActivity<MyCandyActivity>()
                last.clearAnimation()
                next.clearAnimation()
                detail.clearAnimation()
                guide_dislike_hand_left.clearAnimation()
                guide_like_hand_right.clearAnimation()
                UserManager.saveShowGuideIndex(true)
                dismiss()
            }
        }
    }
}
