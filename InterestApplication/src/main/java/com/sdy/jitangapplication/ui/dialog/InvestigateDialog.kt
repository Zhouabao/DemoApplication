package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.ChannelBean
import com.sdy.jitangapplication.model.InvestigateBean
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.dialog_investigate.*
import kotlinx.android.synthetic.main.item_investigate.view.*
import kotlinx.android.synthetic.main.item_investigate_edit.view.*
import org.jetbrains.anko.sdk27.coroutines.onFocusChange

/**
 *    author : ZFM
 *    date   : 2019/10/2111:41
 *    desc   : 调查问卷dialog
 *    version: 1.0
 */
class InvestigateDialog(private val contex1t: Context, private val investigateBean: InvestigateBean) :
    Dialog(contex1t, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_investigate)
        initWindow()
        initView()

    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogCenterAnimation
        window?.attributes = params
        setCanceledOnTouchOutside(true)

    }


    private val adapter: BaseMultiItemQuickAdapter<ChannelBean, BaseViewHolder> by lazy {
        object : BaseMultiItemQuickAdapter<ChannelBean, BaseViewHolder>(mutableListOf()) {
            init {
                addItemType(0, R.layout.item_investigate_edit)
                addItemType(1, R.layout.item_investigate)
            }

            override fun convert(helper: BaseViewHolder, item: ChannelBean) {
                when (helper.itemViewType) {
                    1 -> {
                        helper.itemView.titleInvestigate.text = item.title
                        helper.itemView.checkInvestigate.isVisible = item.check
                    }
                    0 -> {
                        helper.itemView.editInvestigate.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(p0: Editable?) {
                                inputContent = p0.toString()
                                checkConfirmBtn()
                            }

                            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            }

                            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            }

                        })

                        helper.itemView.editInvestigate.onFocusChange { _, hasFocus ->
                            if (hasFocus) {
                                for (data in adapter.data.withIndex()) {
                                    if (data.value.check) {
                                        data.value.check = false
                                        notifyItemChanged(data.index)
                                    }
                                    if (data.index == helper.layoutPosition) {
                                        data.value.check = true
                                    }
                                }
                                checkConfirmBtn()
                            }
                        }
                    }

                }
            }
        }
    }

    private fun initView() {
        rvWay.layoutManager = LinearLayoutManager(contex1t, RecyclerView.VERTICAL, false)
        rvWay.adapter = adapter
        rvWay.addItemDecoration(
            DividerItemDecoration(
                contex1t,
                DividerItemDecoration.HORIZONTAL_LIST,
                SizeUtils.dp2px(1F),
                contex1t.resources.getColor(R.color.colorDivider)
            )
        )
        adapter.setOnItemClickListener { _, view, position ->
            for (data in adapter.data.withIndex()) {
//                if (data.index != position) {
//                    if (data.value.check) {
//                        data.value.check = false
//                        adapter.notifyItemChanged(data.index)
//                    }
//                } else {
//                    data.value.check = !data.value.check
//                    data.value.check=  data.index == position
//                    adapter.notifyItemChanged(data.index)
//                }
                data.value.check = data.index == position
            }
            adapter.notifyDataSetChanged()
            if (adapter.data[position].check && adapter.data[position].itemType != 0) {
                inputContent = ""
            }
            checkConfirmBtn()
        }

        close.onClick {
            dismiss()
        }


        title.text = investigateBean.question_title
        content.text = investigateBean.question_content
        adapter.setNewData(investigateBean.item)

        completeBtn.onClick {
            answer()
        }
    }


    //检查当前完成按钮是否可用
    private var inputContent: String? = ""

    fun checkConfirmBtn() {
        var enable = false
        for (data in adapter.data) {
            if (data.check && data.itemType != 0) {
                enable = true
                break
            }
        }
        if (!inputContent.isNullOrBlank() || enable) {
            completeBtn.isEnabled = true
            completeBtn.text = "完成"
        } else {
            completeBtn.isEnabled = false
            completeBtn.text = "请选择"
        }
    }


    /**
     * 发起回答请求
     */
    private fun answer() {
        var answer_id = 0
        var answer = ""
        for (data in adapter.data) {
            if (data.check) {
                answer_id = data.id
                if (data.itemType == 0) {
                    answer = inputContent ?: ""
                }
                break
            }
        }
        val params = hashMapOf<String, Any>(
            "token" to UserManager.getToken(), "accid" to UserManager.getAccid(),
            "statistics_id" to investigateBean.statistics_id, "answer_id" to answer_id, "answer" to answer
        )
        RetrofitFactory.instance.create(Api::class.java)
            .answer(UserManager.getToken(), UserManager.getAccid(), investigateBean.statistics_id, answer_id, answer)
//            .answer(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        CommonFunction.toast("感谢您的支持！")
                    }
                    dismiss()
                }

                override fun onError(e: Throwable?) {
                    dismiss()
                }
            })
    }
}