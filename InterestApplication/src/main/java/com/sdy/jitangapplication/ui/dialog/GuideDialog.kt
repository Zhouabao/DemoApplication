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
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_guide.*

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
        last.startAnimation((AnimationUtils.loadAnimation(context, R.anim.anim_small_to_big) as ScaleAnimation))
        next.startAnimation((AnimationUtils.loadAnimation(context, R.anim.anim_small_to_big) as ScaleAnimation))
        detail.startAnimation((AnimationUtils.loadAnimation(context, R.anim.anim_small_to_big) as ScaleAnimation))

        guide_dislike_hand_left.startAnimation((AnimationUtils.loadAnimation(context, R.anim.anim_left_to_right) as TranslateAnimation))
        guide_like_hand_right.startAnimation((AnimationUtils.loadAnimation(context, R.anim.anim_right_to_left) as TranslateAnimation))
        guide_hi_hand_top.startAnimation((AnimationUtils.loadAnimation(context, R.anim.anim_bottom_to_top) as TranslateAnimation))



        guideLast.onClick {
            guideCl.isVisible = true
            guideLast.isVisible = false
            guideNext.isVisible = true
            guideDetail.isVisible = false
            guideHi.isVisible = false
            useCl.isVisible = false
            guideLike.isVisible = false
            guideDislike.isVisible = false
            guideChathi.isVisible = false
        }

        guideNext.onClick {
            guideCl.isVisible = true
            guideLast.isVisible = false
            guideNext.isVisible = false
            guideDetail.isVisible = true
            guideHi.isVisible = false
            useCl.isVisible = false
            guideLike.isVisible = false
            guideDislike.isVisible = false
            guideChathi.isVisible = false
        }

        guideDetail.onClick {
            guideCl.isVisible = true
            guideLast.isVisible = false
            guideNext.isVisible = false
            guideDetail.isVisible = false
            guideHi.isVisible = true
            useCl.isVisible = false
            guideLike.isVisible = false
            guideDislike.isVisible = false
            guideChathi.isVisible = false
        }

        guideHi.onClick {
            guideCl.isVisible = true
            guideLast.isVisible = false
            guideNext.isVisible = false
            guideDetail.isVisible = false
            guideHi.isVisible = false
            guideCl.isVisible = false
            guideLike.isVisible = true
            guideDislike.isVisible = false
            guideChathi.isVisible = false
            useCl.isVisible = false
        }

        guideLike.onClick {
            guideCl.isVisible = false
            guideLike.isVisible = false
            guideDislike.isVisible = true
            guideChathi.isVisible = false
            useCl.isVisible = false
        }
        guideDislike.onClick {
            //关闭上滑
//            guideCl.isVisible = false
//            guideLike.isVisible = false
//            guideDislike.isVisible = false
//            guideChathi.isVisible = true
//            useCl.isVisible = false


            guideCl.isVisible = false
            guideLike.isVisible = false
            guideDislike.isVisible = false
            guideChathi.isVisible = false
            useCl.isVisible = true
        }

        guideChathi.onClick {
            guideCl.isVisible = false
            guideLike.isVisible = false
            guideDislike.isVisible = false
            guideChathi.isVisible = false
            useCl.isVisible = true
        }

        onceAgain.onClick {
            guideCl.isVisible = true
            guideLast.isVisible = true
            guideNext.isVisible = false
            guideDetail.isVisible = false
            guideHi.isVisible = false
            guideLike.isVisible = false
            guideDislike.isVisible = false
            guideChathi.isVisible = false
            useCl.isVisible = false
        }
        startUse.onClick {
            last.clearAnimation()
            next.clearAnimation()
            detail.clearAnimation()
            guide_dislike_hand_left.clearAnimation()
            guide_like_hand_right.clearAnimation()
            guide_hi_hand_top.clearAnimation()
            UserManager.saveShowGuide(true)
            dismiss()
        }
    }
}