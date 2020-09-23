package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SPUtils
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.event.SetRoamingLocationEvent
import com.sdy.jitangapplication.event.UpdateNearPeopleParamsEvent
import com.sdy.jitangapplication.model.ChatUpBean
import com.sdy.jitangapplication.ui.activity.RoamingLocationActivity
import com.sdy.jitangapplication.ui.activity.VipPowerActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_match_filter.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/6/259:44
 *    desc   :年龄筛选器
 *    version: 1.0
 */
class FilterUserDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_match_filter)
        initWindow()
        initView()
    }


    /**
     * 展示筛选条件对话框
     * //最小年龄  limit_age_low
     * //最大年龄  limit_age_high
     * //兴趣id
     * //是否同城筛选 1否 2是 local_only
     * //选择了同城 传递城市id city_code
     * //是否筛选认证会员1不用 2需要筛选 audit_only
     * //1男 2女 3不限 gender
     * //toto  这里需要判断是否认证
     */
    private fun initView() {
        val sp = SPUtils.getInstance(Constants.SPNAME)
        seekBarAge.setProgress(
            sp.getInt("limit_age_low", 18).toFloat(),
            sp.getInt("limit_age_high", 35).toFloat()
        )
        filterAge.text =
            "${seekBarAge.leftSeekBar.progress.toInt()}-${seekBarAge.rightSeekBar.progress.toInt()}岁"

        rgOnlineTime.check(
            when (sp.getInt("online_type")) {
                1 -> {
                    R.id.timeOneDay
                }
                2 -> {
                    R.id.timeThreeDay
                }
                3 -> {
                    R.id.timeSevenDay
                }
                5 -> {
                    R.id.timeNoLimit
                }
                else -> {
                    R.id.timeFifteenDay
                }
            }
        )

        rbSexAll.check(
            when (sp.getInt("filter_gender", 3)) {
                1 -> R.id.switchSexMan
                2 -> R.id.switchSexWoman
                else -> R.id.switchSexAll
            }
        )

        if (sp.getString("roaming_city").isNotEmpty()) {
            currentLocation.setCompoundDrawablesWithIntrinsicBounds(
                context1.resources.getDrawable(R.drawable.icon_location_airplane),
                null,
                null,
                null
            )
            currentLocation.text = sp.getString("roaming_city")
        } else {
            currentLocation.setCompoundDrawablesWithIntrinsicBounds(
                context1.resources.getDrawable(R.drawable.icon_location_orange),
                null,
                null,
                null
            )
            currentLocation.text = "当前位置"
        }

        if (UserManager.isUserVerify() == 1) {
            btnVerify.visibility = View.GONE
            switchShowVerify.visibility = View.VISIBLE
            switchShowVerify.isChecked = sp.getInt("audit_only", 1) == 2

        } else {
            if (UserManager.isUserVerify() == 2) {
                btnVerify.text = "认证中"
            } else {
                btnVerify.text = "未认证"
            }
            btnVerify.visibility = View.VISIBLE
            switchShowVerify.visibility = View.GONE
        }

        currentLocation.clickWithTrigger {
            if (UserManager.isUserVip()) {
                context1.startActivity<RoamingLocationActivity>()
            } else {
                ChatUpOpenPtVipDialog(
                    context1,
                    "",
                    ChatUpOpenPtVipDialog.TYPE_ROAMING,
                    ChatUpBean()
                ).show()
                dismiss()
            }
        }


        btnVerify.onClick {
            if (UserManager.isUserVerify() == 2) {
                CommonFunction.toast("认证正在审核中，请耐心等待哦~")
            } else {
                CommonFunction.startToFace(context1)
            }
        }
        seekBarAge.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }

            override fun onRangeChanged(
                view: RangeSeekBar?,
                leftValue: Float,
                rightValue: Float,
                isFromUser: Boolean
            ) {
                filterAge.text = "${leftValue.toInt()}-${rightValue.toInt()}岁"
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }

        })

        btnCompleteFilter.onClick {
            sp.put("limit_age_high", seekBarAge.rightSeekBar.progress.toInt())
            sp.put("limit_age_low", seekBarAge.leftSeekBar.progress.toInt())
            when (rbSexAll.checkedRadioButtonId) {
                R.id.switchSexMan -> {
                    sp.put("filter_gender", 1)
                }
                R.id.switchSexWoman -> {
                    sp.put("filter_gender", 2)
                }
                R.id.switchSexAll -> {
                    sp.put("filter_gender", 3)
                }
            }

            //	1天内 2三天内 3七天内  4十五天内 5不限
            when (rgOnlineTime.checkedRadioButtonId) {
                R.id.timeOneDay -> {
                    sp.put("online_type", 1)
                }
                R.id.timeSevenDay -> {
                    sp.put("online_type", 2)
                }
                R.id.timeThreeDay -> {
                    sp.put("online_type", 3)
                }
                R.id.timeFifteenDay -> {
                    sp.put("online_type", 4)
                }
                R.id.timeNoLimit -> {
                    sp.put("online_type", 5)
                }
            }
            if (currentLocation.text != "当前位置") {
                sp.put("roaming_city", currentLocation.text.toString())
            } else {
                SPUtils.getInstance(Constants.SPNAME).remove("roaming_city")
            }

            if (UserManager.isUserVerify() == 1) {
            }
            if (switchShowVerify.isChecked) {
                sp.put("audit_only", 2)
            } else {
                sp.put("audit_only", 1)
            }

            EventBus.getDefault().post(RefreshEvent(true))
            EventBus.getDefault().post(UpdateNearPeopleParamsEvent())
            dismiss()
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

    override fun show() {
        super.show()
        EventBus.getDefault().register(this)
    }

    override fun dismiss() {
        super.dismiss()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSetRoamingLocationEvent(event: SetRoamingLocationEvent) {
        if (!event.cityBean.name.isNullOrEmpty()) {
            currentLocation.setCompoundDrawablesWithIntrinsicBounds(
                context1.resources.getDrawable(R.drawable.icon_location_airplane),
                null,
                null,
                null
            )
            currentLocation.text = "${event.cityBean.provinceName},${event.cityBean.name}"
        } else {
            currentLocation.setCompoundDrawablesWithIntrinsicBounds(
                context1.resources.getDrawable(R.drawable.icon_location_orange),
                null,
                null,
                null
            )
            currentLocation.text = "当前位置"
        }
    }

}