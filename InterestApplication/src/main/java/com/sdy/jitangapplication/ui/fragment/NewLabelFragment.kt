package com.sdy.jitangapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.base.ui.fragment.BaseFragment
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.ChooseLabelCountEvent
import com.sdy.jitangapplication.event.UpdateAllNewLabelEvent
import com.sdy.jitangapplication.event.UpdateChooseAllLabelEvent
import com.sdy.jitangapplication.event.UpdateChooseLabelEvent
import com.sdy.jitangapplication.model.NewLabel
import com.sdy.jitangapplication.ui.adapter.AllNewLabelAdapter
import kotlinx.android.synthetic.main.activity_new_labels.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewLabelFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class NewLabelFragment : BaseFragment() {
    //所有标签的adapter
    private val allLabekAdapter by lazy { AllNewLabelAdapter() }
    //选中的标签的数量
    private var checkLabelsSize = 0

    //所有的标签数据源
    private val newLabels: MutableList<NewLabel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_label, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //所有标签
        allLabelsRv.layoutManager = GridLayoutManager(activity!!, 3, RecyclerView.VERTICAL, false)
        allLabelsRv.adapter = allLabekAdapter
        allLabekAdapter.setOnItemClickListener { _, view, position ->
            if (!allLabekAdapter.data[position].checked && checkLabelsSize == Constants.LABEL_MAX_COUNT + 1) {
                CommonFunction.toast("最多只能选${Constants.LABEL_MAX_COUNT}个标签")
                return@setOnItemClickListener
            }

            allLabekAdapter.data[position].checked = !allLabekAdapter.data[position].checked
            //todo 发送通知更新选中状态
            EventBus.getDefault().post(UpdateChooseLabelEvent(allLabekAdapter.data[position]))
            allLabekAdapter.notifyItemChanged(position)

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onUpdateNewLabelEvent(event: UpdateAllNewLabelEvent) {
        allLabekAdapter.setNewData(event.labels)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateChooseAllLabelEvent(event: UpdateChooseAllLabelEvent) {
        for (label in allLabekAdapter.data.withIndex()) {
            if (label.value.id == event.label.id) {
                label.value.checked = false
                allLabekAdapter.notifyItemChanged(label.index)
                break
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun onChooseLabelCountEvent(event: ChooseLabelCountEvent) {
        checkLabelsSize = event.count
    }
}
