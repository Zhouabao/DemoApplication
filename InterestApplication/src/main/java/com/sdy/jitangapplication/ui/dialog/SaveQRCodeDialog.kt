package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ImageUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.QRCodeBean
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_save_qrcode.*

/**
 *    author : ZFM
 *    date   : 2019/9/2316:45
 *    desc   : 保存二维码
 *    version: 1.0
 */
class SaveQRCodeDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {
    var qrCodeUrl: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_save_qrcode)
        initWindow()
        initView()

        getQRCode()

    }

    private fun initView() {
        saveAndToWechatBtn.clickWithTrigger {
            if (qrCodeUrl.isNotEmpty()) {
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

    fun getQRCode() {
        RetrofitFactory.instance.create(Api::class.java)
            .getQrCode(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<QRCodeBean>>() {
                override fun onNext(t: BaseResp<QRCodeBean>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        qrCodeUrl = t.data.url
                        GlideUtil.loadImg(context1, qrCodeUrl, qrCodeIv)
                    }
                }
            })
    }
}