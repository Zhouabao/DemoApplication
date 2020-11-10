package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.ui.activity.SweetHeartVerifyUploadActivity
import kotlinx.android.synthetic.main.dialog_sweet_upload_normal.*

/**
 *    author : ZFM
 *    date   : 2019/9/2316:45
 *    desc   : 甜心圈上传模板
 *    version: 1.0
 */
class SweetUploadNormalDialog(val context1: Context, val position: Int, val type: Int) :
    Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_sweet_upload_normal)
        initWindow()
        initView()
    }

    private fun initView() {
        when (type) {
            SweetHeartVerifyUploadActivity.TYPE_IDHAND -> {
                normalImage.setImageResource(R.drawable.icon_sweet_normal_handid)
                normalContent.text=context1.getString(R.string.upload_clear_face_id_pic)
            }
            SweetHeartVerifyUploadActivity.TYPE_IDFACE -> {
                normalImage.setImageResource(R.drawable.icon_sweet_normal_faceid)
                normalContent.text=context1.getString(R.string.upload_clear_front_pic)
            }
            SweetHeartVerifyUploadActivity.TYPE_CAR -> {
                normalImage.setImageResource(R.drawable.icon_sweet_normal_drivingid)
                normalContent.text=context1.getString(R.string.upload_driving_more_50)
            }
            SweetHeartVerifyUploadActivity.TYPE_WEALTH -> {
                normalImage.setImageResource(R.drawable.icon_sweet_normal_houseid)
                normalContent.text=context1.getString(R.string.upload_house_licnese)
            }
            SweetHeartVerifyUploadActivity.TYPE_FIGURE -> {
                normalImage.setImageResource(R.drawable.icon_sweet_normal_chest)
                normalContent.text=context1.getString(R.string.upload_see_size)
            }
            SweetHeartVerifyUploadActivity.TYPE_PROFESSION -> {
                normalImage.setImageResource(R.drawable.icon_sweet_normal_workid)
                normalContent.text=context1.getString(R.string.upload_see_job)
            }
        }

        uploadBtn.clickWithTrigger {
            CommonFunction.onTakePhoto(context1, 1, position)
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
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }

}