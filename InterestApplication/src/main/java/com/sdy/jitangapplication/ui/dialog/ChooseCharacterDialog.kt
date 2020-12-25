package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.RefreshSweetEvent
import com.sdy.jitangapplication.event.UpdateManTapBtnEvent
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.model.MyTapsBean
import com.sdy.jitangapplication.ui.adapter.ManTapAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_choose_character.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *    author : ZFM
 *    date   : 2019/6/259:44
 *    desc   :男性落地选择自己的特质
 *    version: 1.0
 */
class ChooseCharacterDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_choose_character)
        initWindow()
        initView()
        getManTaps()
    }

    private fun initView() {
        btnCompleteCharacter.clickWithTrigger {
            addWant()
        }
        manTapRv.layoutManager = LinearLayoutManager(context1, RecyclerView.VERTICAL, false)
        manTapRv.adapter = adapter
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
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }


    private val adapter by lazy { ManTapAdapter() }
    private fun getManTaps() {
        RetrofitFactory.instance.create(Api::class.java)
            .getManTaps(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<MutableList<MyTapsBean>?>>() {
                override fun onStart() {
                    super.onStart()
                }

                override fun onCompleted() {
                    super.onCompleted()
                }

                override fun onNext(t: BaseResp<MutableList<MyTapsBean>?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        adapter.setNewData(t.data ?: mutableListOf<MyTapsBean>())
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog.getInstance(context).show()
                    }
                }
            })
    }


    override fun show() {
        super.show()
        EventBus.getDefault().register(this)
    }

    override fun dismiss() {
        super.dismiss()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun checkConfirmEnable(event: UpdateManTapBtnEvent) {
        var checkCnt = 0
        for (data in adapter.data) {
            for (tdata in data.child) {
                if (tdata.checked) {
                    checkCnt += 1
                    break
                }
            }
        }

        btnCompleteCharacter.isEnabled = checkCnt == 3
    }


    /**
     * 添加心愿
     */
    private fun addWant() {
        val ids = arrayListOf<Int>()
        for (data in adapter.data) {
            for (tdta in data.child) {
                if (tdta.checked) {
                    ids.add(tdta.id)
                }
            }
        }

        RetrofitFactory.instance.create(Api::class.java)
            .addWant(UserManager.getSignParams(hashMapOf("find_id" to Gson().toJson(ids))))
            .excute(object : BaseSubscriber<BaseResp<MoreMatchBean?>>() {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<MoreMatchBean?>) {
                    super.onNext(t)
                    EventBus.getDefault().post(RefreshSweetEvent())
                    dismiss()
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog.getInstance(context).show()
                    }
                }
            })
    }
}