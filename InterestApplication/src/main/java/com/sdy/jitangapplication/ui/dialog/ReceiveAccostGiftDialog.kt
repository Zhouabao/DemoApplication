package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.GiftBean
import kotlinx.android.synthetic.main.dialog_receive_accost_gift.*

/**
 *    author : ZFM
 *    date   : 2020/6/914:40
 *    desc   :
 *    version: 1.0
 */
class ReceiveAccostGiftDialog(val context1: Context, val giftBean: GiftBean) :
    Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_receive_accost_gift)
        initWindow()
        initView()
    }

    private fun initView() {
        GlideUtil.loadImg(context1, giftBean.icon, accostGiftIv)
        accostGiftName.text = giftBean.title

        contentView.clickWithTrigger {
            dismiss()
        }
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT

        params?.windowAnimations = R.style.MyDialogCenterAnimation
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
        setCancelable(true)
    }


}