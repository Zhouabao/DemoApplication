package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.CloseDialogEvent
import com.sdy.jitangapplication.model.GiftBeans
import com.sdy.jitangapplication.ui.adapter.AccostGiftAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_send_gift_be_friend.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *    author : ZFM
 *    date   : 2020/4/29:36
 *    desc   :赠送礼物达成好友关系
 *    version: 1.0
 */
class SendGiftBeFriendDialog(val account: String, val context1: Context) :
    Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_send_gift_be_friend)
        initWindow()
        initView()
        getGreetGiftList()

    }

    private var checkGiftPos = -1
    private val giftAdapter by lazy { AccostGiftAdapter() }
    private fun initView() {
        candyCount.typeface = Typeface.createFromAsset(context1.assets, "DIN_Alternate_Bold.ttf")

        giftRv.layoutManager = GridLayoutManager(context, 3)
        giftRv.adapter = giftAdapter
        giftAdapter.setOnItemClickListener { _, view, position ->
            for (data in giftAdapter.data) {
                data.checked = data == giftAdapter.data[position]
                checkGiftPos = position
            }
            giftAdapter.notifyDataSetChanged()
        }

        //糖果充值
        chargeBtn.onClick {
            CommonFunction.gotoCandyRecharge(context1)
        }

        //赠送礼物
        sendGiftBtn.clickWithTrigger {
            if (checkGiftPos != -1) {
                ConfirmSendGiftDialog(context, giftAdapter.data[checkGiftPos], account, true).show()
                dismiss()
            } else {
                CommonFunction.toast("请选择要赠送的礼物")
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
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }

    private var myCandyCount = 0
    fun getGreetGiftList() {
        RetrofitFactory.instance.create(Api::class.java)
            .getGreetGiftList(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<GiftBeans?>>(null) {
                override fun onNext(t: BaseResp<GiftBeans?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        myCandyCount = t.data?.candy_amount ?: 0
                        candyCount.text = "${t.data?.candy_amount}"
                        if ((t.data?.list ?: mutableListOf()).size > 0) {
                            t.data?.list!![0].checked = true
                            checkGiftPos = 0
                        }
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