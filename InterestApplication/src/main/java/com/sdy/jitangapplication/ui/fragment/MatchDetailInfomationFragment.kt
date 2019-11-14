package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.TimeUtils
import com.kotlin.base.ui.fragment.BaseFragment
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.DetailUserInfoBean
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.ui.adapter.MatchDetailInfoAdapter
import kotlinx.android.synthetic.main.match_detail_user_information.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 用户详情之个人资料
 *
 */
class MatchDetailInfomationFragment(val matchBean: MatchBean) : BaseFragment() {


    private val adapter by lazy { MatchDetailInfoAdapter() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.match_detail_user_information, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    private fun initData() {
        if (matchBean.birth != 0)
            adapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_birthday,
                    "生日",
                    "${TimeUtils.millis2String(
                        matchBean.birth!! * 1000L,
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    )}/${matchBean.constellation ?: ""}"
                )
            )

        if (matchBean.base_info.height != 0)
            adapter.addData(DetailUserInfoBean(R.drawable.icon_detail_height, "身高", "${matchBean.base_info.height}"))
        if (!matchBean.base_info.emotion_state.isNullOrEmpty())
            adapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_emotion,
                    "感情状态",
                    "${matchBean.base_info.emotion_state}"
                )
            )
        if (!matchBean.base_info.hometown.isNullOrEmpty())
            adapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_hometown,
                    "家乡",
                    "${matchBean.base_info.hometown}"
                )
            )
        if (!matchBean.base_info.present_address.isNullOrEmpty())
            adapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_living,
                    "现居地",
                    "${matchBean.base_info.present_address}"
                )
            )
        if (!matchBean.base_info.personal_job.isNullOrEmpty())
            adapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_living,
                    "职业",
                    "${matchBean.base_info.personal_job}"
                )
            )
        if (!matchBean.base_info.making_friends.isNullOrEmpty())
            adapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_ami,
                    "交友目的",
                    "${matchBean.base_info.making_friends}"
                )
            )
        if (!matchBean.base_info.personal_school.isNullOrEmpty())
            adapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_school,
                    "学校",
                    "${matchBean.base_info.personal_school}"
                )
            )
        if (!matchBean.base_info.personal_drink.isNullOrEmpty())
            adapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_drink,
                    "喝酒",
                    "${matchBean.base_info.personal_drink}"
                )
            )
        if (!matchBean.base_info.personal_smoke.isNullOrEmpty())
            adapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_smoke,
                    "抽烟",
                    "${matchBean.base_info.personal_smoke}"
                )
            )
        if (!matchBean.base_info.personal_schedule.isNullOrEmpty())
            adapter.addData(
                DetailUserInfoBean(
                    R.drawable.icon_detail_schedule,
                    "作息时间",
                    "${matchBean.base_info.personal_schedule}"
                )
            )
    }


    private fun initView() {
        rvInfomation.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        rvInfomation.adapter = adapter
    }


}
