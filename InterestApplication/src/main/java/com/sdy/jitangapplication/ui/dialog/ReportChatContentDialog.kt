package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.KeyboardUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.ReportBean
import com.sdy.jitangapplication.ui.adapter.ReportResonAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_report_chat_content.*
import kotlinx.android.synthetic.main.dialog_write_report_chat_content.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * 聊天内容举报
 */
class ReportChatContentDialog(val context1: Context, val msg: IMMessage) : Dialog(context1, R.style.MyDialog) {

    private val adapter by lazy { ReportResonAdapter() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_report_chat_content)
        initWindow()
        initView()

    }

    private fun initView() {
        reportContentRv.layoutManager = LinearLayoutManager(context1, RecyclerView.VERTICAL, false)
        reportContentRv.adapter = adapter
        adapter.addData(ReportBean(context1.getString(R.string.report_garbage), false))
        adapter.addData(ReportBean(context1.getString(R.string.report_yellow), false))
        adapter.addData(ReportBean(context1.getString(R.string.report_attack), false))
        adapter.addData(ReportBean(context1.getString(R.string.report_elegal), false))
        adapter.addData(ReportBean(context1.getString(R.string.report_mingan), false))
        adapter.addData(ReportBean(context1.getString(R.string.report_other), false))
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.reportReason -> {
                    if (position == adapter.data.size - 1) {
                        showWriteReportContentDialog()
                        dismiss()
                    } else {
                        reportContent(adapter.data[position].reason)
                    }
                }
            }
        }
    }

    private fun reportContent(content: String) {
        val params = hashMapOf<String, Any>()
        params["target_accid"] = msg.fromAccount
        params["report_reasons"] = content
        params["report_type"] = msg.msgType.value
        params["message_id"] = msg.uuid
        params["report_content"] = when {
            msg.msgType == MsgTypeEnum.image -> (msg.attachment as ImageAttachment).url
            msg.msgType == MsgTypeEnum.video -> (msg.attachment as VideoAttachment).url
            msg.msgType == MsgTypeEnum.audio -> (msg.attachment as AudioAttachment).url
            else -> msg.content
        }
        RetrofitFactory.instance.create(Api::class.java)
            .chatReport(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    CommonFunction.toast(t.msg)
                    dismiss()
                }

                override fun onStart() {
                    super.onStart()
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    dismiss()
                }
            })
    }


    private val writeReportContentDialog by lazy { WriteReportContentDialog(context1) }
    private fun showWriteReportContentDialog() {
        if (!writeReportContentDialog.isShowing)
            writeReportContentDialog.show()

        writeReportContentDialog.confirm.onClick {
            reportContent(writeReportContentDialog.content.text.toString())
            writeReportContentDialog.dismiss()
        }
        writeReportContentDialog.cancel.onClick {
            writeReportContentDialog.dismiss()
        }
        writeReportContentDialog.setOnDismissListener {
            KeyboardUtils.hideSoftInput(writeReportContentDialog.content)
        }
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }
}