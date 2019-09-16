package com.sdy.jitangapplication.widgets

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R

/**
 *    author : ZFM
 *    date   : 2019/9/1616:26
 *    desc   : 自定义通用的dialog
 *    version: 1.0
 */
class CommonAlertDialog : Dialog {

    //  var  ImageView ivDialogCancel? = null;
    var tvTitle: TextView? = null
    var ivDialogIcon: ImageView? = null
    var tvDialogContent: TextView? = null
    var btDialogConfirm: TextView? = null //确定按钮可通过外部自定义按钮内容
    var tvDialogCancel: TextView? = null //取消
    var viewDialog: View? = null //分割线


    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, themeStyle: Int) : super(context, themeStyle) {
        initView()
    }

    private fun initView() {
        setContentView(R.layout.customer_alert_dialog_layout)
        initWindow()
        setCanceledOnTouchOutside(false)
        //        ivDialogCancel = findViewById(R.id.iv_dialog_cancel);
        tvTitle = findViewById(R.id.title)
        ivDialogIcon = findViewById(R.id.icon)
        tvDialogContent = findViewById(R.id.message)
        btDialogConfirm = findViewById(R.id.confirm)
        tvDialogCancel = findViewById(R.id.cancel)
        viewDialog = findViewById(R.id.cancelView)
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
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }

    class Builder(val context: Context) {
        var confirmListener: OnConfirmListener? = null
        var cancelListener: OnCancelListener? = null
        var title: String? = null
        var icon: Any? = 0
        var iconVisble: Boolean = false
        var content: String? = null
        var btConfirmText: String? = null
        var tvCancelText: String? = null
        var cancelIsVisibility: Boolean? = true

        fun setOnConfirmListener(confirmListener: OnConfirmListener): Builder {
            this.confirmListener = confirmListener
            return this
        }

        fun setOnCancelListener(cancelListener: OnCancelListener): Builder {
            this.cancelListener = cancelListener
            return this
        }

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setIconVisible(visible: Boolean): Builder {
            this.iconVisble = visible
            return this
        }

        fun setIcon(icon: Any): Builder {
            this.icon = icon
            return this
        }

        fun setContent(content: String): Builder {
            this.content = content
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
            if (!TextUtils.isEmpty(title)) {
                dialog.tvTitle?.text = this.title
            } else {
                dialog.tvTitle?.visibility = View.GONE
            }

            dialog.tvDialogContent?.text = this.content
            if (icon != 0) {
                GlideUtil.loadImg(context,this.icon,dialog.ivDialogIcon)
//                dialog.ivDialogIcon?.setImageResource(this.icon!!)
            }
            dialog.ivDialogIcon?.isVisible = this.iconVisble

            dialog.btDialogConfirm?.text = this.btConfirmText ?: "确认"
            if (this.cancelIsVisibility!!) {
                dialog.tvDialogCancel?.text = this.tvCancelText ?: "取消"
            } else {
                dialog.tvDialogCancel?.visibility = View.GONE
                dialog.viewDialog?.visibility = View.GONE
            }

            if (cancelListener != null) {
                dialog.tvDialogCancel?.setOnClickListener { v -> cancelListener!!.onClick(dialog) }
            }
            if (confirmListener != null) {
                dialog.btDialogConfirm?.setOnClickListener { v -> confirmListener!!.onClick(dialog) }
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
}