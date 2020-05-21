package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.model.MyTapsBean
import com.sdy.jitangapplication.presenter.GetRelationshipPresenter
import com.sdy.jitangapplication.presenter.view.GetRelationshipView
import com.sdy.jitangapplication.ui.adapter.GetRelationshipVpAdapter
import com.sdy.jitangapplication.ui.dialog.OpenVipDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_get_relationship.*
import org.jetbrains.anko.startActivity

/**
 * 寻求什么关系
 */
class GetRelationshipActivity : BaseMvpActivity<GetRelationshipPresenter>(), GetRelationshipView,
    OnLazyClickListener {

    private val getRelationshipVpAdapter by lazy {
        GetRelationshipVpAdapter(
            completeProgress
        )
    }
    private var currentPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_relationship)

        mPresenter = GetRelationshipPresenter()
        mPresenter.context = this
        mPresenter.mView = this
        setSwipeBackEnable(false)

        vpRelationship.adapter = getRelationshipVpAdapter
        vpRelationship.isUserInputEnabled = false

        nextStep.setOnClickListener(this)

        mPresenter.getMyTaps()
    }

    override fun onLazyClick(v: View) {
        when (v.id) {
            R.id.nextStep -> {
                if (vpRelationship.currentItem == 0 && getRelationshipVpAdapter.channel_string.isNullOrEmpty()) {
                    CommonFunction.toast("请先填写了解的渠道奥")
                    return
                }

                if (vpRelationship.currentItem != 0 && (getRelationshipVpAdapter.checkList.isNullOrEmpty() || getRelationshipVpAdapter.checkList[currentPos] == -1)) {
                    CommonFunction.toast("请先勾选相应项")
                    return
                }
                when (currentPos) {
                    0 -> {
                        step1.setTextColor(Color.WHITE)
                        step1.setBackgroundResource(R.drawable.shape_oval_orange)

                        step2.setTextColor(Color.WHITE)
                        step2.setBackgroundResource(R.drawable.shape_oval_orange)

                        step3.setTextColor(Color.parseColor("#C5C6C8"))
                        step3.setBackgroundResource(R.drawable.shape_oval_1dp_c5c6c8)

                        step4.setTextColor(Color.parseColor("#C5C6C8"))
                        step4.setBackgroundResource(R.drawable.shape_oval_1dp_c5c6c8)
                    }
                    1 -> {

                        step1.setTextColor(Color.WHITE)
                        step1.setBackgroundResource(R.drawable.shape_oval_orange)

                        step2.setTextColor(Color.WHITE)
                        step2.setBackgroundResource(R.drawable.shape_oval_orange)

                        step3.setTextColor(Color.WHITE)
                        step3.setBackgroundResource(R.drawable.shape_oval_orange)

                        step4.setTextColor(Color.parseColor("#C5C6C8"))
                        step4.setBackgroundResource(R.drawable.shape_oval_1dp_c5c6c8)


                    }

                    2, 3 -> {
                        step1.setTextColor(Color.WHITE)
                        step1.setBackgroundResource(R.drawable.shape_oval_orange)

                        step2.setTextColor(Color.WHITE)
                        step2.setBackgroundResource(R.drawable.shape_oval_orange)

                        step3.setTextColor(Color.WHITE)
                        step3.setBackgroundResource(R.drawable.shape_oval_orange)

                        step4.setTextColor(Color.WHITE)
                        step4.setBackgroundResource(R.drawable.shape_oval_orange)

                    }
                }

                if (currentPos < getRelationshipVpAdapter.data.size - 1) {
                    currentPos += 1
                    vpRelationship.setCurrentItem(currentPos, true)
                    watingMatchCount.text = "${getRelationshipVpAdapter.data[currentPos].use_cnt}"
                    watingMatchCount.dance(getRelationshipVpAdapter.data[currentPos - 1].use_cnt * 1f / getRelationshipVpAdapter.data[currentPos].use_cnt)
                } else { //
                    mPresenter.addWant(
                        hashMapOf(
                            "channel_string" to getRelationshipVpAdapter.channel_string,
                            "find_id" to Gson().toJson(
                                getRelationshipVpAdapter.checkList.subList(1, 4)
                            )
                        )
                    )
                }
            }
        }

    }

    override fun onAddWant(b: Boolean, data: MoreMatchBean?) {
        if (b) {
            if (data?.isvip != true) {
                OpenVipDialog(
                    this,
                    data,
                    OpenVipDialog.FROM_REGISTER_OPEN_VIP,
                    force_vip = UserManager.isForceOpenVip()
                ).show()
//                finish()
            } else startActivity<MainActivity>()
        }
    }

    override fun onGetMyTaps(data: MutableList<MyTapsBean>) {
        getRelationshipVpAdapter.addData(
            MyTapsBean(
                title = "首先，您在哪里得知的积糖呢",
                type = MyTapsBean.TYPE_INVESTIGATION,
                use_cnt = data[0].use_cnt
            )
        )
        for (tdata in data) {
            tdata.type = MyTapsBean.TYPE_MYTAP
        }
        getRelationshipVpAdapter.addData(data)
        if (!data.isNullOrEmpty() && data.size > 0) {
            watingMatchCount.text = "${data[0].use_cnt}"
        } else {
            watingMatchCount.text =
                "${(intent.getSerializableExtra("moreMatch") as MoreMatchBean?)?.people_amount
                    ?: 0}"
        }
        watingMatchCount.dance()
    }

    override fun onBackPressed() {

    }

    override fun scrollToFinishActivity() {

    }
}
