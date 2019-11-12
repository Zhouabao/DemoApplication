package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.widgets.Rotate3dAnimation
import kotlinx.android.synthetic.main.dialog_say_hi.*


/**
 *    author : ZFM
 *    date   : 2019/11/99:44
 *    desc   :打招呼dialog
 *    version: 1.0
 */
class SayHiDialog(
    val target_accid: String,
    val userName: String,
    val context1: Context
) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_say_hi)
        initWindow()

        initView()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        params?.windowAnimations = R.style.MyDialogLeftBottomAnimation
        window?.attributes = params
        setCanceledOnTouchOutside(true)
    }

    private fun initView() {
        sayHitargetName.text = userName
        sayHiClose.onClick {
            dismiss()
        }
        sayHiBtn.onClick {
            sayHiContent.clearFocus()
            KeyboardUtils.hideSoftInput(sayHiContent)
            //信封内容的缩放动画
            val scaleAnimation = ScaleAnimation(
                1f,
                0.6f,
                1F,
                0.6f,
                ScaleAnimation.RELATIVE_TO_SELF,
                0.5F,
                ScaleAnimation.RELATIVE_TO_SELF,
                0.5F
            )
            scaleAnimation.duration = 1000
            scaleAnimation.fillAfter = true
            scaleAnimation.fillBefore = true
            scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {

                }

                override fun onAnimationEnd(p0: Animation?) {
                    val par = contentHi.getLayoutParams() as ConstraintLayout.LayoutParams
                    par.height = (contentHi.getMeasuredHeight() * 0.8F).toInt()
                    par.width = (contentHi.getMeasuredWidth() * 0.8F).toInt()
                    contentHi.setLayoutParams(par)
                    contentHi.requestLayout()
                }

                override fun onAnimationStart(p0: Animation?) {
                }

            })

            //信封的平移动画
            val translateAniBottom = TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF,
                -1f,
                TranslateAnimation.ABSOLUTE,
                ScreenUtils.getAppScreenWidth() / 2F - (SizeUtils.dp2px(300F) / 2F * 0.8F),
                TranslateAnimation.RELATIVE_TO_SELF,
                0F,
                TranslateAnimation.RELATIVE_TO_SELF,
                0F
            )
            translateAniBottom.duration = 1000
            translateAniBottom.fillAfter = true


            //翻转动画
            val mOpenFlipAnimation = Rotate3dAnimation(
                0f,
                180f,
//                0f,
                letterCloseRight.width.toFloat()/2,
//                ScreenUtils.getAppScreenWidth() / 2F - (SizeUtils.dp2px(300F) / 2F * 0.8F),
//                0F,
                letterCloseRight.height.toFloat()/2,
                400F,
                false
            )
            mOpenFlipAnimation.duration = 200
            mOpenFlipAnimation.fillAfter = true
            mOpenFlipAnimation.repeatCount = 0
            mOpenFlipAnimation.interpolator = LinearInterpolator()


            //退场动画
            val translateAniContentOut = TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_PARENT,
                0F,
                TranslateAnimation.RELATIVE_TO_PARENT,
                1f,
                TranslateAnimation.RELATIVE_TO_PARENT,
                0F,
                TranslateAnimation.RELATIVE_TO_PARENT,
                0F
            )
            translateAniContentOut.duration = 1000
            translateAniContentOut.fillAfter = true


            letterBottomCl.isVisible = true
            letterTop.isVisible = true
            letterBottomCl.startAnimation(translateAniBottom)
            letterCloseLeft.startAnimation(translateAniBottom)
            letterTop.startAnimation(translateAniBottom)

            translateAniBottom.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {


                }

                override fun onAnimationEnd(p0: Animation?) {
                    letterCloseRight.isVisible = false
                    letterCloseLeft.isVisible = true
                    rootView.postDelayed({
                        rootView.startAnimation(translateAniContentOut)
                    }, 500)
//                    flipInYAnimator.start()
//                    letterCloseRight.startAnimation(mOpenFlipAnimation)

                }

                override fun onAnimationStart(p0: Animation?) {

                    contentHi.startAnimation(scaleAnimation)
                }


            })

            mOpenFlipAnimation.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation) {
//                    letterCloseLeft.postDelayed({
//                        letterCloseRight.isVisible = false
//                    }, 100)

                }

                override fun onAnimationRepeat(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    letterCloseRight.isVisible = false
                    letterCloseLeft.isVisible = true
                    rootView.postDelayed({
                        rootView.startAnimation(translateAniContentOut)
                    }, 500)

                }
            })

            translateAniContentOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {

                }

                override fun onAnimationEnd(p0: Animation?) {
                    this@SayHiDialog.hide()
                    this@SayHiDialog.dismiss()
                }

                override fun onAnimationStart(p0: Animation?) {

                }

            })

//            if (!sayHiContent.text.isNullOrBlank()) {
//                val msg = MessageBuilder.createTextMessage(
//                    target_accid,
//                    SessionTypeEnum.P2P,
//                    sayHiContent.text.toString().trim()
//                )
//
//                NIMClient.getService(MsgService::class.java).sendMessage(msg, false).setCallback(object :
//                    RequestCallback<Void?> {
//                    override fun onSuccess(p0: Void?) {
//                        val scaleAnimation = ScaleAnimation(
//                            1f,
//                            0.6f,
//                            1F,
//                            0.6f,
//                            ScaleAnimation.RELATIVE_TO_SELF,
//                            0.5F,
//                            ScaleAnimation.RELATIVE_TO_SELF,
//                            0.5F
//                        )
//                        scaleAnimation.duration = 500
//                        scaleAnimation.fillAfter = true
//
//                        val translateAniBottom = TranslateAnimation(
//                            TranslateAnimation.RELATIVE_TO_PARENT,
//                            0f,
//                            TranslateAnimation.RELATIVE_TO_PARENT,
//                            0.5f,
//                            TranslateAnimation.RELATIVE_TO_PARENT,
//                            0F,
//                            TranslateAnimation.RELATIVE_TO_PARENT,
//                            0.5F
//                        )
//                        translateAniBottom.duration = 500
//                        translateAniBottom.fillAfter = true
//                        letterBottomCl.startAnimation(translateAniBottom)
//                        letterTop.startAnimation(translateAniBottom)
//                        translateAniBottom.setAnimationListener(object : Animation.AnimationListener {
//                            override fun onAnimationRepeat(p0: Animation?) {
//
//
//                            }
//
//                            override fun onAnimationEnd(p0: Animation?) {
//                            }
//
//                            override fun onAnimationStart(p0: Animation?) {
////                                letterBottomCl.isVisible = true
////                                letterTop.isVisible = true
//                                contentHi.startAnimation(scaleAnimation)
//                            }
//
//
//                        })
//
//
////                        dismiss()
//                    }
//
//                    override fun onFailed(p0: Int) {
//                    }
//
//                    override fun onException(p0: Throwable?) {
//
//                    }
//
//                })
//            }
        }

        sayHiContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable) {
                sayHiBtn.isEnabled = p0.trim().isNotEmpty()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })


    }

}