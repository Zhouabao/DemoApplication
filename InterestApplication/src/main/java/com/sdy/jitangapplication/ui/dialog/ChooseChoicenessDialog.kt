package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.UpdateMyTicketEvent
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_choose_choiceness.*
import org.greenrobot.eventbus.EventBus
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
            //todo 此处进行请求 看日期是否可选
            if (calendarView.selectedDate != null) {
                expendTicket("${calendarView.selectedDate?.year}-${calendarView.selectedDate?.month}-${calendarView.selectedDate?.day}")
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
                return (day.month == CalendarDay.today().month && day.day <= CalendarDay.today().day)
            }

            override fun decorate(view: DayViewFacade) {
                view.setDaysDisabled(true)
            }

        })
        calendarView.addDecorator(object : DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay): Boolean {
                return !(day.month == CalendarDay.today().month && day.day <= CalendarDay.today().day)
            }

            override fun decorate(view: DayViewFacade) {
                view.setDaysDisabled(false)
            }
        })
        calendarView.selectedDate = CalendarDay.from(
            CalendarDay.today().year,
            CalendarDay.today().month,
            CalendarDay.today().day + 1
        )
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


    //209 该日已满 200 使用成功 400 错误信息

    fun expendTicket(date: String) {
        val loadingDialog = LoadingDialog(context1)
        RetrofitFactory.instance.create(Api::class.java)
            .expendTicket(UserManager.getSignParams(hashMapOf("date" to date)))
            .excute(object : BaseSubscriber<BaseResp<Any?>>() {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onCompleted() {
                    super.onCompleted()
                    loadingDialog.dismiss()
                }

                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    when (t.code) {
                        200 -> {
                            //todo 使用成功
                            calendarCl.isVisible = false
                            completeCalendarLl.isVisible = true
                            val params = window?.attributes
                            params?.y = SizeUtils.dp2px(10F)
                            window?.attributes = params
                            EventBus.getDefault().post(UpdateMyTicketEvent(-1))
                        }
                        209 -> {
                            CommonFunction.toast("该日置顶购买已满")
                        }
                        400 -> {
                            CommonFunction.toast(t.msg)
                        }
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loadingDialog.dismiss()
                }

            })

    }
}