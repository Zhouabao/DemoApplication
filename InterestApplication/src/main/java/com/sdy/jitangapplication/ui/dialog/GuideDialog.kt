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
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.ui.activity.MyCandyActivity
import com.sdy.jitangapplication.ui.activity.ProtocolActivity
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
        completeGuide()
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

        startUse.onClick {
            last.clearAnimation()
            next.clearAnimation()
            detail.clearAnimation()
            guide_dislike_hand_left.clearAnimation()
            guide_like_hand_right.clearAnimation()
            dismiss()
        }
        useOfCandy.onClick {
            if (UserManager.getGender() == 1) {
                context.startActivity<ProtocolActivity>("type" to ProtocolActivity.TYPE_CANDY_USAGE)
            } else {
                context.startActivity<MyCandyActivity>()
            }
            last.clearAnimation()
            next.clearAnimation()
            detail.clearAnimation()
            guide_dislike_hand_left.clearAnimation()
            guide_like_hand_right.clearAnimation()
            completeGuide()
            dismiss()
        }
    }

    private fun completeGuide() {
        RetrofitFactory.instance.create(Api::class.java)
            .completeGuide(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {

            })
    }

    override fun dismiss() {
        super.dismiss()

    }
}
