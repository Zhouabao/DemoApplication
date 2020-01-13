package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.ReportBean
import com.sdy.jitangapplication.model.loginOffCauseBean
import com.sdy.jitangapplication.ui.activity.VerifyCodeActivity
import com.sdy.jitangapplication.ui.adapter.ReportResonAdapter
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.dialog_login_off.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/11/2014:06
 *    desc   :账号注销dialog
 *    version: 1.0
 */
class LoginOffDialog(val context1: Context, val phone: String, val loginOffCauseBean: loginOffCauseBean) :
    Dialog(context1, R.style.MyDialog) {
    private var checkReason = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_login_off)
        initWindow()
        initView()
        setData()
    }

    private val adapter by lazy { ReportResonAdapter() }
    private fun initView() {
        confirmLoginoffBtn.onClick {
            context1.startActivity<VerifyCodeActivity>(
                "type" to "${VerifyCodeActivity.TYPE_LOGIN_OFF}",
                "descr" to checkReason,
                "phone" to phone
            )
            dismiss()
        }

        loginOffReasonRv.layoutManager = LinearLayoutManager(context1, RecyclerView.VERTICAL, false)
        loginOffReasonRv.addItemDecoration(
            DividerItemDecoration(
                context1,
                DividerItemDecoration.HORIZONTAL_LIST,
                SizeUtils.dp2px(1f),
                context1.resources.getColor(R.color.colorDivider)
            )
        )
        loginOffReasonRv.adapter = adapter
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.reportReason -> {
                    for (data in adapter.data.withIndex()) {
                        if (data.index == position)
                            checkReason = data.value.reason
                        data.value.checked = data.index == position
                    }
                    adapter.notifyDataSetChanged()
                    checkConfirmEnable()
                }
            }
        }
    }

    private fun setData() {
        loginOffWarning.text = loginOffCauseBean.descr
        for (cause in loginOffCauseBean.list.withIndex()) {
            if (cause.index == 0)
                checkReason = cause.value
            adapter.addData(ReportBean(cause.value, cause.index == 0))
        }
        checkConfirmEnable()
    }

    private fun checkConfirmEnable() {
        for (data in adapter.data) {
            if (data.checked) {
                confirmLoginoffBtn.isEnabled = true
                break
            } else {
                confirmLoginoffBtn.isEnabled = false
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
//        params?.y = SizeUtils.dp2px(20F)
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }

}