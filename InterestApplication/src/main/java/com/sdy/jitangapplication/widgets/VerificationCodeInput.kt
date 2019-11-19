package com.sdy.jitangapplication.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.View.OnKeyListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import com.blankj.utilcode.util.ScreenUtils
import com.sdy.jitangapplication.R


class VerificationCodeInput @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr), TextWatcher, OnKeyListener {



    private var lastEtisEmpty = true

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        val lastETC = getChildAt(boxCount - 1) as EditText
        lastEtisEmpty = lastETC.text.isEmpty()
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {


    }

    override fun afterTextChanged(s: Editable) {

        focus()
        checkAndCommit()

    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_UP) {
            if (v?.tag == (boxCount - 1)) {
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


    companion object {
        private val TYPE_NUMBER = "number"
        private val TYPE_TEXT = "text"
        private val TYPE_PASSWORD = "password"
        private val TYPE_PHONE = "phone"
        private val TAG = "VerificationCodeInput"
    }

    private var boxCount = 6
    private var boxWidth = 120
    private var boxHeight = 120
    private var childHPadding = 14
    private var childVPadding = 14
    private var child_hint = ""
    private var etAutoShow = false
    private var inputType = TYPE_NUMBER
    private var boxBgFocus: Drawable? = null
    private var boxBgNormal: Drawable? = null
    private var etTextSize: Float = 16F
    private var listener: Listener? = null

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.VerificationCodeInput)
        boxCount = array.getInt(R.styleable.VerificationCodeInput_box, 6)
        childHPadding = array.getDimension(R.styleable.VerificationCodeInput_child_h_padding, 0f).toInt()
        childVPadding = array.getDimension(R.styleable.VerificationCodeInput_child_v_padding, 0f).toInt()
        boxBgFocus = array.getDrawable(R.styleable.VerificationCodeInput_box_bg_focus)
        boxBgNormal = array.getDrawable(R.styleable.VerificationCodeInput_box_bg_normal)
        inputType = array.getString(R.styleable.VerificationCodeInput_inputType) ?: TYPE_NUMBER
        child_hint = array.getString(R.styleable.VerificationCodeInput_child_hint) ?: ""
        boxWidth = array.getDimension(R.styleable.VerificationCodeInput_child_width, boxWidth.toFloat()).toInt()
        boxHeight = array.getDimension(R.styleable.VerificationCodeInput_child_height, boxHeight.toFloat()).toInt()
//        etTextSize = array.getDimension(R.styleable.VerificationCodeInput_etTextSize, etTextSize)
        etAutoShow = array.getBoolean(R.styleable.VerificationCodeInput_autoShowInputBoard, false)
        array.recycle()
        initViews()

    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return LinearLayout.LayoutParams(context, attrs)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var parentWidth = measuredWidth
        if (parentWidth == ViewGroup.LayoutParams.MATCH_PARENT) {
            parentWidth = ScreenUtils.getScreenWidth()
        }
        Log.d(javaClass.name, "onMeasure width $parentWidth")

        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            this.measureChild(child, widthMeasureSpec, heightMeasureSpec)
        }
        if (count > 0) {
            val child = getChildAt(0)
            val cWidth = child.measuredWidth
            if (parentWidth != ViewGroup.LayoutParams.WRAP_CONTENT) {
                // 重新计算padding
                childHPadding = (parentWidth - cWidth * count) / (count + 1)
            }

            val cHeight = child.measuredHeight

            val maxH = cHeight + 2 * childVPadding
            val maxW = cWidth * count + childHPadding * (count + 1)
            setMeasuredDimension(
                View.resolveSize(maxW, widthMeasureSpec),
                View.resolveSize(maxH, heightMeasureSpec)
            )
        }


    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        Log.d(javaClass.name, "onLayout width = $measuredWidth")

        val childCount = childCount

        for (i in 0 until childCount) {
            val child = getChildAt(i)

            child.visibility = View.VISIBLE
            val cWidth = child.measuredWidth
            val cHeight = child.measuredHeight
            val cl = childHPadding + i * (cWidth + childHPadding)
            val cr = cl + cWidth
            val ct = childVPadding
            val cb = ct + cHeight
            child.layout(cl, ct, cr, cb)
        }


    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (etAutoShow)
            postDelayed({
                focus()
            }, 200)


    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()


    }


    private fun initViews() {
        for (i in 0 until boxCount) {
            val editText = EditText(context)
            val layoutParams = LinearLayout.LayoutParams(boxWidth, boxHeight)
            layoutParams.bottomMargin = childVPadding
            layoutParams.topMargin = childVPadding
            layoutParams.leftMargin = childHPadding
            layoutParams.rightMargin = childHPadding
            layoutParams.gravity = Gravity.CENTER

            if (child_hint.isNotEmpty()) {
                editText.hint = child_hint
            }

            editText.setOnKeyListener(this)
            editText.tag = i
            setBg(editText, false)
            editText.setTextColor(Color.BLACK)
            editText.setTextSize(etTextSize)
            editText.paint.isFakeBoldText = true
            editText.layoutParams = layoutParams
            editText.gravity = Gravity.CENTER
            editText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(1))

            if (TYPE_NUMBER == inputType) {
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            } else if (TYPE_PASSWORD == inputType) {
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
            } else if (TYPE_TEXT == inputType) {
                editText.inputType = InputType.TYPE_CLASS_TEXT
            } else if (TYPE_PHONE == inputType) {
                editText.inputType = InputType.TYPE_CLASS_PHONE

            }
            editText.id = i
            editText.setEms(1)
            editText.addTextChangedListener(this)
            addView(editText, i)


        }


    }

    private fun backFocus() {
        for (i in boxCount - 1 downTo 0) {
            val editText = getChildAt(i) as EditText
            if (editText.text.length == 1) {
                editText.setText("")
//                editText.requestFocus()
//                editText.setSelection(1)
                return
            }
        }
    }

    private fun focus() {
        val count = childCount
        var editText: EditText
        for (i in 0 until count) {
            editText = getChildAt(i) as EditText
            if (editText.text.isEmpty()) {
                editText.isFocusable = true
                editText.isFocusableInTouchMode = true
                editText.requestFocus()
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
                    editText,
                    0
                )
                return
            }
        }
    }

    private fun setBg(editText: EditText, focus: Boolean) {
        if (boxBgNormal != null && !focus) {
            editText.background = boxBgNormal
        } else if (boxBgFocus != null && focus) {
            editText.background = boxBgFocus
        }
    }

    private fun checkAndCommit() {
        val stringBuilder = StringBuilder()
        var full = true
        for (i in 0 until boxCount) {
            val editText = getChildAt(i) as EditText
            val content = editText.text.toString()
            if (content.isEmpty()) {
                full = false
            } else {
                stringBuilder.append(content)
            }
        }
        Log.d(TAG, "checkAndCommit:$stringBuilder")
        if (listener != null) {
            listener!!.onComplete(full, stringBuilder.toString())
//                isEnabled = false
        }
    }

    override fun setEnabled(enabled: Boolean) {
        val childCount = childCount

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.isEnabled = enabled
        }
    }

    fun setOnCompleteListener(listener: Listener) {
        this.listener = listener
    }


    interface Listener {
        fun onComplete(complete: Boolean, content: String?)
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

    public fun requestEditeFocus() {
        val lastETC = getChildAt(boxCount - 1) as EditText
        if (lastETC.text.isNotEmpty()) {
            lastETC.isEnabled = true
            showInputPad(lastETC)
            lastETC.isCursorVisible = true
        } else
            focus()
    }


    fun clear() {
        for (j in 0 until boxCount) {
            val et = (getChildAt(j) as EditText)
            et.removeTextChangedListener(this)
            et.setText("")
            et.addTextChangedListener(this)
        }
        focus()
    }
}

