package com.sdy.jitangapplication.widgets

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.customer_alert_dialog_layout.*

/**
 *    author : ZFM
 *    date   : 2019/9/1616:26
 *    desc   : 自定义通用的dialog
 *    version: 1.0
 */
class CommonAlertDialog : Dialog {


    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, themeStyle: Int) : super(context, themeStyle) {
        initView()
    }

    private fun initView() {
        setContentView(R.layout.customer_alert_dialog_layout)
        initWindow()
        //点击外部可取消
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 0.5f
//         android:layout_marginLeft="15dp"
//        android:layout_marginRight="15dp"
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = params
    }

    class Builder(val context: Context) {
        var confirmListener: OnConfirmListener? = null
        var cancelListener: OnCancelListener? = null
        var title1: String? = null
        var icon: Any? = 0
        var iconVisble: Boolean = false
        var content1: String? = null
        var btConfirmText: String? = null
        var tvCancelText: String? = null
        var cancelIsVisibility: Boolean? = true
        var cancelAble1: Boolean = true

        fun setOnConfirmListener(confirmListener: OnConfirmListener): Builder {
            this.confirmListener = confirmListener
            return this
        }

        fun setOnCancelListener(cancelListener: OnCancelListener): Builder {
            this.cancelListener = cancelListener
            return this
        }

        fun setTitle(title: String): Builder {
            this.title1 = title
            return this
        }

        fun setIconVisible(visible: Boolean): Builder {
            this.iconVisble = visible
            return this
        }

        fun setCancelAble(cancelAble: Boolean): Builder {
            this.cancelAble1 = cancelAble
            return this
        }

        fun setIcon(icon: Any): Builder {
            this.icon = icon
            return this
        }

        fun setContent(content: String): Builder {
            this.content1 = content
            return this
        }

        // 点击确定按钮的文字
        fun setConfirmText(btConfirmText: String): Builder {
            this.btConfirmText = btConfirmText
            return this
        }

        //取消按钮的文字
        fun setCancelText(tvCancelText: String): Builder {
            this.tvCancelText = tvCancelText
            return this
        }

        fun setCancelIconIsVisibility(cancelIsVisibility: Boolean): Builder {
            this.cancelIsVisibility = cancelIsVisibility
            return this
        }

        fun create(): CommonAlertDialog {
            val dialog = CommonAlertDialog(context, R.style.MyDialog)
            if (!TextUtils.isEmpty(title1)) {
                dialog.title.text = this.title1
            } else {
                dialog.title.visibility = View.GONE
            }

            dialog.message.text = this.content1
            if (icon != 0) {
                GlideUtil.loadImg(context, this.icon, dialog.icon)
//                dialog.icon .setImageResource(this.icon!!)
            }
            dialog.icon.isVisible = this.iconVisble

            dialog.confirm.text = this.btConfirmText ?: context.getString(R.string.ok)
            if (this.cancelIsVisibility!!) {
                dialog.cancel.text = this.tvCancelText ?: context.getString(R.string.cancel)
            } else {
                dialog.cancel.visibility = View.GONE
                dialog.cancelView.visibility = View.GONE
            }
            dialog.setCancelable(this.cancelAble1)


            if (cancelListener != null) {
                dialog.cancel.setOnClickListener { v -> cancelListener!!.onClick(dialog) }
            }
            if (confirmListener != null) {
                dialog.confirm.setOnClickListener { v -> confirmListener!!.onClick(dialog) }
            }
            return dialog
        }

    }

    // 点击弹窗取消按钮回调
    interface OnCancelListener {
        fun onClick(dialog: Dialog)
    }

    // 点击弹窗跳转回调
    interface OnConfirmListener {
        fun onClick(dialog: Dialog)
    }

    fun setCancelBtnable(cancelIsVisibility: Boolean) {
        if (cancelIsVisibility) {
            cancel.text = context.getString(R.string.cancel)
        } else {
            cancel.visibility = View.GONE
            cancelView.visibility = View.GONE
        }
    }
}