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
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.event.CloseDialogEvent
import com.sdy.jitangapplication.model.GiftBeans
import com.sdy.jitangapplication.ui.adapter.SendGiftAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_chat_send_gift.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *    author : ZFM
 *    date   : 2020/4/29:36
 *    desc   :聊天赠送礼物弹窗
 *    version: 1.0
 */
class ChatSendGiftDialog(
    val nickName: String,
    val avator: String,
    val account: String,
    context: Context
) :
    Dialog(context, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_chat_send_gift)
        initWindow()
        initView()
        getGiftList()

    }

    private val giftAdapter by lazy { SendGiftAdapter() }
    private fun initView() {
        targetNickname.text = nickName
        GlideUtil.loadCircleImg(context, avator, targetAvator)
        giftRv.layoutManager = GridLayoutManager(context, 4)
        giftRv.adapter = giftAdapter
        giftAdapter.setOnItemClickListener { _, view, position ->
            ConfirmSendGiftDialog(context, giftAdapter.data[position], account).show()
        }

        chargeBtn.onClick {
            RechargeCandyDialog(context).show()
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

    private var myCandyCount = 0
    fun getGiftList() {
        RetrofitFactory.instance.create(Api::class.java)
            .getGiftList(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<GiftBeans?>>(null) {
                override fun onNext(t: BaseResp<GiftBeans?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        myCandyCount = t.data?.candy_amount ?: 0
                        candyCount.text = "${t.data?.candy_amount}"
                        giftAdapter.addData(t.data?.list ?: mutableListOf())
                    }
                }
            })
    }

    override fun show() {
        super.show()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCloseDialogEvent(event: CloseDialogEvent) {
        if (isShowing) {
            dismiss()
        }
    }
}