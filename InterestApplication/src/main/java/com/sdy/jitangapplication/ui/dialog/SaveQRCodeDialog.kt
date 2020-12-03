package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ImageUtils
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import kotlinx.android.synthetic.main.dialog_save_qrcode.*

/**
 *    author : ZFM
 *    date   : 2019/9/2316:45
 *    desc   : 保存二维码
 *    version: 1.0
 */
class SaveQRCodeDialog(
    val context1: Context,
    val qrCodeUrl: String = "https://www.qedev.com/res/ad/ad2.png"
) :
    Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_save_qrcode)
        initWindow()
        initView()
    }

    private fun initView() {
        GlideUtil.loadImg(context1, qrCodeUrl, qrCodeIv)

        saveAndToWechatBtn.clickWithTrigger {
            Thread().run {
                GlideUtil.downLoadImage(context1, qrCodeUrl) { bitmap ->
                    if (bitmap != null) {
                        ImageUtils.save2Album(bitmap, Bitmap.CompressFormat.JPEG)
                    }
                }
            }
            AppUtils.launchApp("com.tencent.mm")
            dismiss()
        }
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
//        params?.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) * 2
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT

        params?.windowAnimations = R.style.MyDialogBottomAnimation
//        params?.y = SizeUtils.dp2px(15F)
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)

    }
}