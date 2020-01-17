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
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.ui.activity.IDVerifyActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_match_filter.*
import org.greenrobot.eventbus.EventBus
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
     * //标签id
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
        filterAge.text = "${seekBarAge.leftSeekBar.progress.toInt()}-${seekBarAge.rightSeekBar.progress.toInt()}岁"

        rbSexAll.check(
            when (sp.getInt("filter_gender", 3)) {
                1 -> R.id.switchSexMan
                2 -> R.id.switchSexWoman
                else -> R.id.switchSexAll
            }
        )

        switchOnLine.isVisible = UserManager.isUserVip()
        switchOnLine.isChecked = sp.getInt("online_only", 1) == 2
        btnGoVip1.isVisible = !UserManager.isUserVip()
        switchSameCity.isVisible = UserManager.isUserVip()
        switchSameCity.isChecked = sp.getInt("local_only", 1) == 2
        btnGoVip.isVisible = !UserManager.isUserVip()

        if (UserManager.isUserVerify() == 1) {
            btnVerify.visibility = View.GONE
            switchShowVerify.visibility = View.VISIBLE
            switchShowVerify.isChecked = sp.getInt("audit_only", 1) == 2

        } else {
            if (UserManager.isUserVerify() == 2 || UserManager.isUserVerify() == 3) {
                btnVerify.text = "认证中"
            } else {
                btnVerify.text = "未认证"
            }
            btnVerify.visibility = View.VISIBLE
            switchShowVerify.visibility = View.GONE
        }

        btnGoVip.onClick {
            ChargeVipDialog(ChargeVipDialog.FILTER_LOCAL_CITY, context1).show()
        }
        btnGoVip1.onClick {
            ChargeVipDialog(ChargeVipDialog.FILTER_ONLINE, context1).show()
        }
        btnVerify.onClick {
            if (UserManager.isUserVerify() == 2 || UserManager.isUserVerify() == 3) {
                CommonFunction.toast("认证正在审核中，请耐心等待哦~")
            } else {
                context1.startActivity<IDVerifyActivity>()
            }
        }
        seekBarAge.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }

            override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
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
            if (switchSameCity.isChecked) {
                sp.put("local_only", 2)
                sp.put("city_code", UserManager.getCityCode())
            } else {
                sp.put("local_only", 1)
            }
            if (UserManager.isUserVerify() == 1)
                if (switchShowVerify.isChecked) {
                    sp.put("audit_only", 2)
                } else {
                    sp.put("audit_only", 1)
                }

            //添加在线用户筛选
            if (switchOnLine.isChecked) {
                sp.put("online_only", 2)
            } else {
                sp.put("online_only", 1)
            }
            EventBus.getDefault().post(RefreshEvent(true))
            dismiss()
        }

    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.TOP)
        val params = window?.attributes
//        params?.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) * 2
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT

        params?.windowAnimations = R.style.MyDialogTopAnimation
//        params?.y = SizeUtils.dp2px(20F)
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }
}