package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.GiftBean
import com.sdy.jitangapplication.ui.adapter.SendGiftAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_chat_send_gift.*

/**
 *    author : ZFM
 *    date   : 2020/4/29:36
 *    desc   :聊天赠送礼物弹窗
 *    version: 1.0
 */
class ChatSendGiftDialog(context: Context) : Dialog(context, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_chat_send_gift)
        initWindow()
        initView()
        getGiftList()

    }

    private val giftAdapter by lazy { SendGiftAdapter() }
    private fun initView() {
        giftRv.layoutManager = GridLayoutManager(context, 4)
        giftRv.adapter = giftAdapter
        giftAdapter.setOnItemClickListener { _, view, position ->
            ConfirmSendGiftDialog(context,giftAdapter.data[position]).show()
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

    fun getGiftList() {
        RetrofitFactory.instance.create(Api::class.java)
            .getGiftList(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<MutableList<GiftBean>?>>(null) {
                override fun onNext(t: BaseResp<MutableList<GiftBean>?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        giftAdapter.addData(t.data ?: mutableListOf())
                    }
                }
            })
    }


}