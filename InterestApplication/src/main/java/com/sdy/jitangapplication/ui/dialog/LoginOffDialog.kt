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
class LoginOffDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_login_off)
        initWindow()
        initView()
    }

    private val adapter by lazy { ReportResonAdapter() }
    private fun initView() {

        confirmLoginoffBtn.onClick {
            context1.startActivity<VerifyCodeActivity>(
                "type" to "${VerifyCodeActivity.TYPE_LOGIN_OFF}",
                "phone" to "13990811869"
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
                        data.value.checked = data.index == position
                    }
                    adapter.notifyDataSetChanged()
                    checkConfirmEnable()
                }
            }

        }


        setData()


    }

    private fun setData() {
        loginOffWarning.text = "您的账号已提交注销申请，将于X年X月X日注销成功。注销成功前，您可随时登录恢复账号正常使用，15天不登录则将自动注销账号及清空全部数据\n\n" +
                "您即将离开积糖，我们非常不舍，同时想知道您离开的原因，我们会努力提升自己，期待您再次使用"
        adapter.addData(ReportBean("产品不是想要的感觉", true))
        adapter.addData(ReportBean("在平台找不到想找的人", false))
        adapter.addData(ReportBean("产品不知道怎么玩或体验感差", false))
        adapter.addData(ReportBean("受到垃圾用户骚扰太多", false))
        adapter.addData(ReportBean("已在平台脱单或交到朋友", false))
        adapter.addData(ReportBean("其他", false))
        adapter.addData(ReportBean("其他", false))
        adapter.addData(ReportBean("其他", false))
        adapter.addData(ReportBean("其他", false))
        adapter.addData(ReportBean("其他", false))
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