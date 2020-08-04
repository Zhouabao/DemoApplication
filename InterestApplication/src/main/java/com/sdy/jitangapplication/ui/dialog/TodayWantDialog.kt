package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.UpdateTodayWantEvent
import com.sdy.jitangapplication.model.CheckBean
import com.sdy.jitangapplication.model.NearBean
import com.sdy.jitangapplication.ui.adapter.TodayWantAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_today_want.*
import org.greenrobot.eventbus.EventBus

/**
 *    author : ZFM
 *    date   : 2020/4/29:36
 *    desc   :选择今日意向
 *    version: 1.0
 */
class TodayWantDialog(
    val myContext: Context,
    val nearBean: NearBean?
) : Dialog(myContext, R.style.MyDialog) {

    public var checkWantId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_today_want)
        initWindow()
        initView()
        getIntention()
    }


    private val adapter by lazy {
        TodayWantAdapter()
    }

    private fun initView() {
        t2.text = if (UserManager.getGender() == 1) {
            "添加意向将标明想要的约会且置顶卡片，有相同意向的女生会主动与你联系，请真诚选择"
        } else {
            "添加意向将标明想要的约会且置顶卡片，男生需要向你支付消息门槛才能和你获得联系，请真诚选择"
        }

        todayWantList.layoutManager = GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)
        todayWantList.adapter = adapter
        adapter.setOnItemClickListener { _, view, position ->
            checkPosi = position
            for (data in adapter.data) {
                data.checked = data == adapter.data[position]
            }
            adapter.notifyDataSetChanged()
        }

        confirmTodayWant.clickWithTrigger {
            addIntention()
        }

        resetTodayWant.clickWithTrigger {
            addIntention(true)
        }
        closeDialogBtn.clickWithTrigger {
            dismiss()
        }


    }


    /**
     * 获取今日意向
     */
    private fun getIntention() {
        RetrofitFactory.instance.create(Api::class.java)
            .getIntention(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<MutableList<CheckBean>?>>() {
                override fun onNext(t: BaseResp<MutableList<CheckBean>?>) {
                    super.onNext(t)
                    if (t.code == 200 && !t.data.isNullOrEmpty()) {
                        var hasCheck = false
                        if (checkWantId != -1) {
                            for (data in t.data!!.withIndex()) {
                                if (data.value.id == checkWantId) {
                                    data.value.checked = true
                                    checkPosi = data.index
                                    hasCheck = true
                                    break
                                }
                            }
                        }
                        if (!hasCheck) {
                            t.data!![0].checked = true
                            checkPosi = 0
                        }
                        adapter.setNewData(t.data)
                    }
                }
            })
    }

    /**
     * 添加今日意向
     */
    private var checkPosi = 0

    private fun addIntention(reset: Boolean = false) {
        val params = hashMapOf<String, Any>()
        if (reset) {
            params["id"] = 0
        } else if (adapter.data.size > checkPosi) {
            params["id"] = adapter.data[checkPosi].id
        } else return
        RetrofitFactory.instance.create(Api::class.java)
            .addIntention(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>() {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        EventBus.getDefault().post(
                            UpdateTodayWantEvent(
                                if (reset) {
                                    null
                                } else {
                                    adapter.data[checkPosi]
                                }
                            )
                        )
                        dismiss()
                    }
                }
            })
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


    override fun dismiss() {
        super.dismiss()
        if (nearBean?.today_pull_share == false && !UserManager.showCompleteUserCenterDialog) {
            //如果自己的完善度小于标准值的完善度，就弹出完善个人资料的弹窗
            InviteFriendDialog(myContext).show()
        }
    }

}