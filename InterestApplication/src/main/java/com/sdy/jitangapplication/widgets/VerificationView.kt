package com.sdy.jitangapplication.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.sdy.jitangapplication.R

/**
 *
 * 自定义View - 手机验证码输入框
 *
 */
class VerificationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr), TextWatcher, View.OnKeyListener {
    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DEL && event?.action == KeyEvent.ACTION_UP) {
            if (v?.tag == (etTextCount - 1)) {
                if (lastEtisEmpty) {
                    backFocus()
                }
                lastEtisEmpty = true
            } else {
                backFocus()
            }
        }
        return false
    }

    override fun afterTextChanged(s: Editable?) {
        synchronized(this) {
            focus()
        }

    }

    private var lastEtisEmpty = true
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        val lastETC = getChildAt(etTextCount - 1) as EditText
        lastEtisEmpty = lastETC.text.isEmpty()
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val middle = (sizeW - sizeH * etTextCount) / (etTextCount - 1)
        for (i in 0 until etTextCount) {
            val et = getChildAt(i) as EditText
            val lp = getChildAt(0).layoutParams
            lp.width = sizeH
            lp.height = sizeH
            getChildAt(i).layoutParams = lp

            val left = (middle + sizeH) * i
            et.layout(left, 0, left + sizeH, sizeH)
        }
        getChildAt(etTextCount).layout(0, 0, sizeW, sizeH)

    }

    private val density = resources.displayMetrics.density
    private var etTextSize = 18 * density
    private var etIsTextBold = false
    private var etTextCount = 4
    private var etBackGround = 0
    private var etBackGroundColor = Color.BLACK
    private var etTextColor = Color.BLACK
    private var etCursorDrawable = 0
    private var etAutoShow = false

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.VerificationView)
        etTextSize = array.getDimension(R.styleable.VerificationView_vTextSize, 18 * density)
        etTextCount = array.getInteger(R.styleable.VerificationView_vTextCount, 4)
        etIsTextBold = array.getBoolean(R.styleable.VerificationView_vIsTextBold, false)
        etBackGround = array.getResourceId(R.styleable.VerificationView_vBackgroundResource, 0)
        etBackGroundColor = array.getColor(R.styleable.VerificationView_vBackgroundColor, Color.BLACK)
        etTextColor = array.getColor(R.styleable.VerificationView_vTextColor, Color.BLACK)
        etCursorDrawable = array.getResourceId(R.styleable.VerificationView_vCursorDrawable, 0)
        etAutoShow = array.getBoolean(R.styleable.VerificationView_vAutoShowInputBoard, false)
        array.recycle()

    }

    private fun backFocus() {
        for (i in etTextCount - 1 downTo 0) {
            val editText = getChildAt(i) as EditText
            if (editText.text.length == 1) {
                editText.setText("")
                return
            }
        }
    }

    /**
     * 展示输入键盘
     */
    public fun showInputPad(editText: EditText) {
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(editText, 0)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (etAutoShow)
            postDelayed({
                focus()
            }, 200)

    }


    public fun focus() {

        val text = StringBuffer()
        for (i in 0 until etTextCount) {
            text.append((getChildAt(i) as EditText).text.toString())
        }
        listener?.invoke(text.toString(), text.length == etTextCount)

        for (i in 0 until etTextCount) {
            val editText = getChildAt(i) as EditText
            (getChildAt(i) as EditText).isEnabled = true
            if (editText.text.isEmpty()) {
                showInputPad(editText)
                editText.isCursorVisible = true
                return
            }
        }
        val et = getChildAt(etTextCount - 1) as EditText
        if (et.text.isNotEmpty()) {
            for (i in 0 until etTextCount) {
                (getChildAt(i) as EditText).isEnabled = false
            }
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                et.windowToken,
                0
            )
            et.isCursorVisible = false
            et.clearFocus()

        }
    }

    /**
     * 最后一个完成回调
     */
    @Deprecated("replace by listener")
    var finish: ((String) -> Unit)? = null

    var listener: ((String, Boolean) -> Unit)? = null

    init {
        for (i in 0 until etTextCount) {
            val et = EditText(context)
            et.gravity = Gravity.CENTER
            et.includeFontPadding = false
            if (etBackGroundColor != Color.BLACK)
                et.background = ColorDrawable(etBackGroundColor)
            et.includeFontPadding = false
            if (etBackGround != 0)
                et.setBackgroundResource(etBackGround)
            et.setEms(1)
            if (etCursorDrawable != 0)
                try {
                    val f = TextView::class.java.getDeclaredField("mCursorDrawableRes")
                    f.isAccessible = true
                    f.set(et, etCursorDrawable)
                } catch (e: Throwable) {
                }
            et.inputType = InputType.TYPE_CLASS_NUMBER
            et.setTextColor(etTextColor)
            et.paint.isFakeBoldText = etIsTextBold
            et.setTextSize(TypedValue.COMPLEX_UNIT_PX, etTextSize)
            et.addTextChangedListener(this)
            et.setOnKeyListener(this)
            et.tag = i
            et.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(1))
            addView(et)
        }
        val view = View(context)
        view.setOnClickListener {
            requestEditeFocus()
        }
        addView(view)

    }

    private var sizeH = 0
    private var sizeW = 0
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        sizeW = View.MeasureSpec.getSize(widthMeasureSpec)
        sizeH = View.MeasureSpec.getSize(heightMeasureSpec)
        measureChildren(widthMeasureSpec, heightMeasureSpec)
    }

    fun clear() {
        for (j in 0 until etTextCount) {
            val et = (getChildAt(j) as EditText)
            et.removeTextChangedListener(this)
            et.setText("")
            et.addTextChangedListener(this)
        }
        focus()
    }

    public fun requestEditeFocus() {
        val lastETC = getChildAt(etTextCount - 1) as EditText
        if (lastETC.text.isNotEmpty()) {
            lastETC.isEnabled = true
            showInputPad(lastETC)
            lastETC.isCursorVisible = true
        } else
            focus()
    }
}