package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.OtherLabelsBean
import com.sdy.jitangapplication.presenter.MatchDetailLabelPresenter
import com.sdy.jitangapplication.presenter.view.MatchDetailLabelView
import com.sdy.jitangapplication.ui.adapter.MatchDetailUserLabelAdapter
import kotlinx.android.synthetic.main.match_detail_user_label_fragment.*

/**
 * 用户详情之兴趣
 *
 */
class MatchDetailLabelFragment(val target_accid: String) : BaseMvpLazyLoadFragment<MatchDetailLabelPresenter>(),
    MatchDetailLabelView {
    override fun loadData() {
        initView()
        mPresenter.getOtherTags(target_accid)
    }

    private val adapter by lazy { MatchDetailUserLabelAdapter() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.match_detail_user_label_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    private fun initView() {
        mPresenter = MatchDetailLabelPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!
        rvLabel.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        rvLabel.adapter = adapter
        adapter.bindToRecyclerView(rvLabel)
        adapter.setEmptyView(R.layout.empty_layout_block, rvLabel)
    }

    override fun getOtherTagsResult(result: Boolean, data: OtherLabelsBean?) {
        if (result) {
            if (data != null) {
                for (my in data.my) {
                    for (other in data.other) {
                        if (my.id == other.id) {
                            other.same_label = true
                        }
                        for (myQuality in my.label_quality) {
                            for (otherQuality in other.label_quality) {
                                if (myQuality.id == otherQuality.id) {
                                    otherQuality.checked = true
                                    other.same_quality_count.plus(1)
                                }
                            }
                        }
                    }
                }
                adapter.setNewData(data.other)
            }
        }
    }

}
