package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import kotlinx.android.synthetic.main.dialog_choose_choiceness.*
import java.util.*

/**
 *    author : ZFM
 *    date   : 2020/6/179:53
 *    desc   :选择成为精选置顶日期
 *    version: 1.0
 */
class ChooseChoicenessDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_choose_choiceness)
        initWindow()
        initView()
    }

    private fun initView() {
        initCalendarView()

        //选择当日
        chooseCalendarBtn.clickWithTrigger {
            if (calendarView.selectedDate != null) {
                CommonFunction.toast("${calendarView.selectedDate!!.year}${calendarView.selectedDate!!.month}${calendarView.selectedDate!!.day}")
                calendarCl.isVisible = false
                completeCalendarLl.isVisible = true
                val params = window?.attributes
                params?.y = SizeUtils.dp2px(10F)
                window?.attributes = params
            }
        }


        //好的
        okBtn.clickWithTrigger {
            dismiss()
        }
    }


    private fun initCalendarView() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 4)
        calendarView.state().edit()
            .setMinimumDate(
                CalendarDay.from(
                    CalendarDay.today().year,
                    CalendarDay.today().month,
                    1
                )
            )
            .setMaximumDate(
                CalendarDay.from(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            )
            .commit()

        calendarView.addDecorator(object : DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay): Boolean {
                return (day.month == CalendarDay.today().month && day.day < CalendarDay.today().day)
            }

            override fun decorate(view: DayViewFacade) {
                view.setDaysDisabled(true)
            }

        })
        calendarView.addDecorator(object : DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay): Boolean {
                return !(day.month == CalendarDay.today().month && day.day < CalendarDay.today().day)
            }

            override fun decorate(view: DayViewFacade) {
                view.setDaysDisabled(false)
            }
        })
        calendarView.selectedDate = CalendarDay.today()
        calendarView.setTitleFormatter(object :
            MonthArrayTitleFormatter(context1.resources.getTextArray(R.array.custom_months)) {
            override fun format(day: CalendarDay): CharSequence {
                return SpannableStringBuilder()
                    .append(day.year.toString())
                    .append("年")
                    .append(context1.resources.getTextArray(R.array.custom_months)[day.month - 1])
                    .append("月")
            }
        });
        calendarView.setOnDateChangedListener { widget, date, selected ->
            //todo 此处进行请求 看日期是否可选

        }
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
//        params?.y = SizeUtils.dp2px(20F)

        window?.attributes = params
    }
}