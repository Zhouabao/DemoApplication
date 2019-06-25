package com.example.demoapplication.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.example.demoapplication.R

/**
 * @author zfm
 */

class RoundImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ImageView(context, attrs, defStyleAttr) {

    private val mPath: Path
    private var mRectF: RectF? = null
    private val rids = FloatArray(8)
    private val paintFlagsDrawFilter: PaintFlagsDrawFilter

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView)
        val mRadius = array.getDimension(R.styleable.RoundImageView_radius, 10f)
        rids[0] = mRadius
        rids[1] = mRadius
        rids[2] = mRadius
        rids[3] = mRadius
        rids[4] = 0f
        rids[5] = 0f
        rids[6] = 0f
        rids[7] = 0f
        array.recycle()
        mPath = Path()
        paintFlagsDrawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        mPath.reset()
        mPath.addRoundRect(mRectF!!, rids, Path.Direction.CW)
        canvas.drawFilter = paintFlagsDrawFilter
        canvas.save()
        canvas.clipPath(mPath)
        super.onDraw(canvas)
        canvas.restore()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mRectF = RectF(0f, 0f, w.toFloat(), h.toFloat())
    }

}
