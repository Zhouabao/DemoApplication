package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ActivityUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.ui.activity.NewUserInfoSettingsActivity
import com.sdy.jitangapplication.utils.UserManager
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/9/1616:26
 *    desc   : 自定义通用的dialog
 *    version: 1.0
 */
class GotoVerifyDialog : Dialog {
    companion object {
        const val TYPE_VERIFY = 4//认证失败去认证
        const val TYPE_CHANGE_AVATOR_NOT_PASS = 7//头像违规替换
        const val TYPE_CHANGE_AVATOR_PASS = 2//头像通过,但是不是真人
        const val TYPE_CHANGE_ABLUM = 3//完善相册
    }

    //  var  ImageView ivDialogCancel? = null;
    var tvTitle: TextView? = null
    var ivDialogIcon: ImageView? = null
    var tvDialogContent: TextView? = null
    var btDialogConfirm: TextView? = null //确定按钮可通过外部自定义按钮内容
    var tvDialogCancel: TextView? = null //取消
    var viewDialog: View? = null //分割线
    var llVerify: LinearLayout? = null //认证的布局
    var verifyChange: TextView? = null //换头像
    var verifyHuman: TextView? = null //人工审核
    var verifyCancel: TextView? = null //取消
    var iconWarn: ImageView? = null //警告的图标
    var llConfirmOrCancel: LinearLayout? = null //确认或者取消


    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, themeStyle: Int) : super(context, themeStyle) {
        initView()
    }

    private fun initView() {
        setContentView(R.layout.dialog_goto_verify_layout)
        initWindow()
        //        ivDialogCancel = findViewById(R.id.iv_dialog_cancel);
        tvTitle = findViewById(R.id.title)
        ivDialogIcon = findViewById(R.id.icon)
        tvDialogContent = findViewById(R.id.message)
        btDialogConfirm = findViewById(R.id.confirm)
        tvDialogCancel = findViewById(R.id.cancel)
        viewDialog = findViewById(R.id.cancelView)
        llVerify = findViewById(R.id.llVerify)
        verifyHuman = findViewById(R.id.gotoHumanVerify)
        verifyChange = findViewById(R.id.changeAvator)
        verifyCancel = findViewById(R.id.cancelVerify)
        llConfirmOrCancel = findViewById(R.id.llConfirmOrCancel)
        iconWarn = findViewById(R.id.iconWarn)
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
        var title: String? = null
        var icon: Any? = 0
        var iconVisble: Boolean = false
        var content: String? = null
        var btConfirmText: String? = null
        var tvCancelText: String? = null
        var cancelIsVisibility: Boolean? = true
        var cancelable: Boolean = true
        var type: Int = 4//弹框的类型


        fun setOnCancelable(cancelable: Boolean): Builder {
            this.cancelable = cancelable
            return this
        }

        fun setType(type: Int): Builder {
            this.type = type
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

        fun create(): GotoVerifyDialog {
            val dialog = GotoVerifyDialog(context, R.style.MyDialog)
            if (!TextUtils.isEmpty(title)) {
                dialog.tvTitle?.text = this.title
            } else {
                dialog.tvTitle?.visibility = View.GONE
            }
            dialog.tvDialogContent?.text = this.content
            if (icon != 0) {
                GlideUtil.loadAvatorImg(context, this.icon, dialog.ivDialogIcon)
            }
            dialog.ivDialogIcon?.isVisible = this.iconVisble
            //点击外部可取消
            dialog.setCanceledOnTouchOutside(this.cancelable)
            dialog.setOnKeyListener { dialogInterface, keyCode, event ->
                if (this.cancelable) {
                    false
                } else
                    keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0
            }
            dialog.iconWarn?.isVisible = (this.type == TYPE_VERIFY || this.type == TYPE_CHANGE_AVATOR_NOT_PASS)


            if (this.type == TYPE_VERIFY) {
                dialog.llVerify?.isVisible = true
                dialog.llConfirmOrCancel?.isVisible = false
                dialog.verifyCancel?.onClick {
                    humanVerify(2)
                    dialog.cancel()
                }
                dialog.verifyHuman?.onClick {
                    // 人工审核
                    humanVerify(1)
                    if (this.cancelable)
                        dialog.cancel()
                }
                dialog.verifyChange?.onClick {
                    // 替换头像
                    humanVerify(2)
                    if (ActivityUtils.getTopActivity() != NewUserInfoSettingsActivity::class.java)
                        context.startActivity<NewUserInfoSettingsActivity>()
                    if (this.cancelable)
                        dialog.cancel()
                }
            } else {
                dialog.llVerify?.isVisible = false
                dialog.llConfirmOrCancel?.isVisible = true

                dialog.btDialogConfirm?.text = this.btConfirmText ?: "确认"
                if (this.cancelIsVisibility!!) {
                    dialog.tvDialogCancel?.text = this.tvCancelText ?: "取消"
                } else {
                    dialog.tvDialogCancel?.visibility = View.GONE
                    dialog.viewDialog?.visibility = View.GONE
                }
                dialog.tvDialogCancel?.onClick {
                    if (this.cancelable)
                        dialog.cancel()
                }
                dialog.btDialogConfirm?.onClick {
                    if (ActivityUtils.getTopActivity() != NewUserInfoSettingsActivity::class.java)
                        context.startActivity<NewUserInfoSettingsActivity>()
                    dialog.cancel()
                }
            }


            return dialog
        }


        /**
         * 人工审核
         * 1 人工认证 2重传头像或则取消
         */
        fun humanVerify(type: Int) {
            val params = UserManager.getBaseParams()
            params["type"] = type
            RetrofitFactory.instance.create(Api::class.java)
                .humanAduit(UserManager.getSignParams(params))
                .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                    override fun onNext(t: BaseResp<Any?>) {

                    }

                    override fun onError(e: Throwable?) {

                    }
                })

        }


    }

}