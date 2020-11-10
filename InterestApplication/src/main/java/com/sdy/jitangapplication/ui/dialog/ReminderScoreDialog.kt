package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.dialog_reminder_score.*
import org.greenrobot.eventbus.EventBus

/**
 *    author : ZFM
 *    date   : 2019/11/110:19
 *    desc   :
 *    version: 1.0
 */



class ReminderScoreDialog(val context1: Context, var score: Int) : Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_reminder_score)

        initWindow()
        initView()
    }

    private fun initView() {

//        when (score) {
//            20 -> {
//
//                score20.isVisible = true
//                score80.isVisible = false
//                val params = reminderArrow.layoutParams as android.widget.LinearLayout.LayoutParams
//                params.leftMargin = (SizeUtils.dp2px(70F) + SizeUtils.dp2px(40f) / 2f - SizeUtils.dp2px(9F)).toInt()
//                reminderArrow.layoutParams = params
//
//                reminderContent.text = SpanUtils.with(reminderContent)
//                    .append("每天")
//                    .setFontSize(13, true)
//                    .setForegroundColor(context1.resources.getColor(R.color.colorBlack))
//                    .append("20")
//                    .setBold()
//                    .setFontSize(14, true)
//                    .setForegroundColor(context1.resources.getColor(R.color.colorOrange))
//                    .append("次免费右滑奥\n快去使用开始交友吧")
//                    .setFontSize(13, true)
//                    .setForegroundColor(context1.resources.getColor(R.color.colorBlack))
//                    .create()
//            }
//            80 -> {
        score20.isVisible = false
        score80.isVisible = true
        score80.text = "$score"
        val layoutmanager80 = score80.layoutParams as RelativeLayout.LayoutParams
        layoutmanager80.rightMargin =
            (SizeUtils.dp2px(15F) + (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(110F)) * 0.2F).toInt()
        //score80.layoutParams = layoutmanager80


        val params = reminderArrow.layoutParams as android.widget.LinearLayout.LayoutParams
        params.leftMargin =
            (SizeUtils.dp2px(70F) + (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(70F)
                    - SizeUtils.dp2px(15F)) * 0.8F - SizeUtils.dp2px(40f) / 2f - SizeUtils.dp2px(3f)).toInt()
        reminderArrow.layoutParams = params

        val params1 = reminderContent.layoutParams as android.widget.LinearLayout.LayoutParams
        params1.gravity = Gravity.RIGHT
        params1.rightMargin = SizeUtils.dp2px(15F)
//                params1.leftMargin = (SizeUtils.dp2px(95F) + (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(95F) - SizeUtils.dp2px(15F)) * 0.8F - SizeUtils.dp2px(40f) / 2f - SizeUtils.dp2px(103F)).toInt() //左边距+0.8的进度条-笑脸一半
        reminderContent.layoutParams = params1

        reminderContent.text = SpanUtils.with(reminderContent)
            .append(context1.getString(R.string.full_your_info_to_100))
            .setFontSize(13, true)
            .setForegroundColor(context1.resources.getColor(R.color.colorBlack))
            .append(context1.getString(R.string.free_right_tme_left))
            .setFontSize(13, true)
            .setForegroundColor(context1.resources.getColor(R.color.colorBlack))
            .append("$score")
            .setFontSize(14, true)
            .setBold()
            .setForegroundColor(context1.resources.getColor(R.color.colorOrange))
            .append(context1.getString(R.string.free_right_tme_right))
            .setFontSize(13, true)
            .setForegroundColor(context1.resources.getColor(R.color.colorBlack))
            .create()
//            }
//        }

        llRoot.onClick {
            dismiss()
        }
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
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = params
        setCanceledOnTouchOutside(true)
    }

}