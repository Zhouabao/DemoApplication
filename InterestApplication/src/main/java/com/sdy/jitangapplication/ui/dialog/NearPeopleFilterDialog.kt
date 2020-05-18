package com.sdy.jitangapplication.ui.dialog

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.UpdateNearPeopleParamsEvent
import com.sdy.jitangapplication.model.CheckBean
import com.sdy.jitangapplication.ui.adapter.FilterSortAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_near_people_filter.*
import org.greenrobot.eventbus.EventBus

/**
 *    author : ZFM
 *    date   : 2019/8/1513:59
 *    desc   : 附近的人筛选
 *    version: 1.0
 */
class NearPeopleFilterDialog(var context1: Context) :
    BottomSheetDialog(context1, R.style.BottomSheetDialog) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_near_people_filter)
        initWindow()
        initview()
        initData()
    }

    private fun initData() {
//             params["limit_age_low_nearly"] = seekBarAge.leftSeekBar.progress.toInt()
//            params["limit_age_high_nearly"] = seekBarAge.rightSeekBar.progress.toInt()
//            params["rank_type_nearly"] = rankType
//            params["gender_nearly"]
        if (UserManager.getNearFilterParams().isNotEmpty()) {
            seekBarAge.setProgress(
                (UserManager.getNearFilterParams()["limit_age_low_nearly"] ?: 18).toFloat(),
                (UserManager.getNearFilterParams()["limit_age_high_nearly"] ?: 35).toFloat()
            )

            rbSexAll.check(
                when ((UserManager.getNearFilterParams()["gender_nearly"] ?: 0).toInt()) {
                    1 -> R.id.switchSexMan
                    2 -> R.id.switchSexWoman
                    else -> R.id.switchSexAll
                }
            )

            rankType = UserManager.getNearFilterParams()["rank_type_nearly"] ?: 3
            for (data in adapter.data) {
                data.checked = data == adapter.data[rankType - 1]
            }
            adapter.notifyDataSetChanged()

        }
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 0.5f
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }


    private val adapter by lazy { FilterSortAdapter() }
    private val params by lazy { hashMapOf<String, Int>() }
    private var rankType = 1
    fun initview() {
        fliterAge.typeface = Typeface.createFromAsset(context1.assets, "DIN_Alternate_Bold.ttf")
        rvSort.layoutManager = LinearLayoutManager(context1, RecyclerView.HORIZONTAL, false)
        rvSort.adapter = adapter
        adapter.addData(
            CheckBean(
                R.drawable.icon_recommend_auto_uncheck,
                R.drawable.icon_recommend_auto_check,
                "智能推荐",
                true
            )
        )
        adapter.addData(
            CheckBean(
                R.drawable.icon_distance_uncheck,
                R.drawable.icon_distance_check,
                "距离优先",
                false
            )
        )
        adapter.addData(
            CheckBean(
                R.drawable.icon_online_uncheck,
                R.drawable.icon_online_check,
                "在线优先",
                false
            )
        )


        adapter.setOnItemClickListener { _, view, position ->
            for (data in adapter.data) {
                data.checked = data == adapter.data[position]
            }
            rankType = position + 1
            adapter.notifyDataSetChanged()
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
                fliterAge.text = "${leftValue.toInt()}-${rightValue.toInt()}"
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }

        })

        //重置筛选条件
        resetBtn.clickWithTrigger {
            seekBarAge.setProgress(18F, 35F)
            rankType = 1
            for (data in adapter.data) {
                data.checked = data == adapter.data[rankType - 1]
            }
            adapter.notifyDataSetChanged()
            rbSexAll.check(R.id.switchSexAll)

            params.clear()
            EventBus.getDefault().post(UpdateNearPeopleParamsEvent(params))
            UserManager.clearNearFilterParams()
            dismiss()
        }

        //limit_age_low
        //limit_age_high
        //gender
        //rank_type  1智能 2距离 3在线
        //确认筛选
        confirmFilterBtn.clickWithTrigger {
            params["limit_age_low_nearly"] = seekBarAge.leftSeekBar.progress.toInt()
            params["limit_age_high_nearly"] = seekBarAge.rightSeekBar.progress.toInt()
            params["rank_type_nearly"] = rankType
            params["gender_nearly"] = when (rbSexAll.checkedRadioButtonId) {
                R.id.switchSexMan -> {
                    1
                }
                R.id.switchSexWoman -> {
                    2
                }
                else -> {
                    3
                }
            }

            UserManager.saveNearFilterParams(params)
            EventBus.getDefault().post(UpdateNearPeopleParamsEvent(params))
            dismiss()
        }

    }

    override fun show() {
        super.show()
//        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
//        EventBus.getDefault().unregister(this)/
    }


}