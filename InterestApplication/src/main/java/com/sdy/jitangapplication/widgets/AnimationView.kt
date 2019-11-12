package com.sdy.jitangapplication.widgets

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.View
import com.blankj.utilcode.util.SizeUtils
import com.sdy.jitangapplication.R

/**
 *    author : ZFM
 *    date   : 2019/11/1213:34
 *    desc   :
 *    version: 1.0
 */
class AnimationView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val PADDING = SizeUtils.dp2px(100f).toFloat()
    private val IMAGE_WIDTH = SizeUtils.dp2px(200f).toFloat()

    internal var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    internal var camera = Camera()

    internal var topFlip = 0f
    internal var bottomFlip = 0f
    internal var flipRotation = 0f
    var animatorSet = AnimatorSet()

    init {
        camera.setLocation(0f, 0f, SizeUtils.applyDimension(-6F, COMPLEX_UNIT_DIP)) // -8 = -8 * 72
        var bottomFlipAnimator = ObjectAnimator.ofFloat(this, "bottomFlip", 45f)
        bottomFlipAnimator.duration = 1500

        var flipRotationAnimator = ObjectAnimator.ofFloat(this, "flipRotation", 270f)
        flipRotationAnimator.duration = 1500

        var topFlipAnimator = ObjectAnimator.ofFloat(this, "topFlip", -45f)
        topFlipAnimator.duration = 1500

        animatorSet.playSequentially(bottomFlipAnimator, flipRotationAnimator, topFlipAnimator)
        animatorSet.startDelay = 500
    }

    fun getTopFlip(): Float {
        return topFlip
    }

    fun setTopFlip(topFlip: Float) {
        this.topFlip = topFlip
        invalidate()
    }

    fun getBottomFlip(): Float {
        return bottomFlip
    }

    fun setBottomFlip(bottomFlip: Float) {
        this.bottomFlip = bottomFlip
        invalidate()
    }

    fun getFlipRotation(): Float {
        return flipRotation
    }

    fun setFlipRotation(flipRotation: Float) {
        this.flipRotation = flipRotation
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制上半部分
        canvas.save()
        canvas.translate(PADDING + IMAGE_WIDTH / 2, PADDING + IMAGE_WIDTH / 2)
        canvas.rotate(-flipRotation)
        camera.save()
        camera.rotateX(topFlip)
        camera.applyToCanvas(canvas)
        camera.restore()
        canvas.clipRect(-IMAGE_WIDTH, -IMAGE_WIDTH, IMAGE_WIDTH, 0f)
        canvas.rotate(flipRotation)
        canvas.translate(-(PADDING + IMAGE_WIDTH / 2), -(PADDING + IMAGE_WIDTH / 2))
        canvas.drawBitmap(getAvatar(resources, IMAGE_WIDTH.toInt()), PADDING, PADDING, paint)
        canvas.restore()

        // 绘制下半部分
        canvas.save()
        canvas.translate(PADDING + IMAGE_WIDTH / 2, PADDING + IMAGE_WIDTH / 2)
        canvas.rotate(-flipRotation)
        camera.save()
        camera.rotateX(bottomFlip)
        camera.applyToCanvas(canvas)
        camera.restore()
        canvas.clipRect(-IMAGE_WIDTH, 0f, IMAGE_WIDTH, IMAGE_WIDTH)
        canvas.rotate(flipRotation)
        canvas.translate(-(PADDING + IMAGE_WIDTH / 2), -(PADDING + IMAGE_WIDTH / 2))
        canvas.drawBitmap(getAvatar(resources, IMAGE_WIDTH.toInt()), PADDING, PADDING, paint)
        canvas.restore()
    }

    fun getAvatar(res: Resources, width: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, R.drawable.icon_letter_close_right, options)
        options.inJustDecodeBounds = false
        options.inDensity = options.outWidth
        options.inTargetDensity = width.toInt()
        return BitmapFactory.decodeResource(res, R.drawable.icon_letter_close_right, options)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animatorSet.start()
    }
}