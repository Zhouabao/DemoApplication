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
                normalContent.text="上传清晰的手持身份证正面照片"
            }
            SweetHeartVerifyUploadActivity.TYPE_IDFACE -> {
                normalImage.setImageResource(R.drawable.icon_sweet_normal_faceid)
                normalContent.text="上传清晰的身份证正面照片"
            }
            SweetHeartVerifyUploadActivity.TYPE_CAR -> {
                normalImage.setImageResource(R.drawable.icon_sweet_normal_drivingid)
                normalContent.text="上传与身份证姓名一致的车辆行驶证\n要求车价大于50万"
            }
            SweetHeartVerifyUploadActivity.TYPE_WEALTH -> {
                normalImage.setImageResource(R.drawable.icon_sweet_normal_houseid)
                normalContent.text="上传与身份证姓名一致的房产证\n要求房产大于200平米"
            }
            SweetHeartVerifyUploadActivity.TYPE_FIGURE -> {
                normalImage.setImageResource(R.drawable.icon_sweet_normal_chest)
                normalContent.text="上传的测量图片能清晰看到卷尺标"
            }
            SweetHeartVerifyUploadActivity.TYPE_PROFESSION -> {
                normalImage.setImageResource(R.drawable.icon_sweet_normal_workid)
                normalContent.text="上传与身份证姓名一致的工作证明信息"
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