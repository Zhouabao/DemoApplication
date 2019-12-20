package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.*
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.OtherLabelsBean
import com.sdy.jitangapplication.presenter.MatchDetailLabelPresenter
import com.sdy.jitangapplication.presenter.view.MatchDetailLabelView
import com.sdy.jitangapplication.ui.adapter.MatchDetailUserInterestLabelAdapter
import com.sdy.jitangapplication.ui.adapter.MatchDetailUserLabelAdapter
import kotlinx.android.synthetic.main.empty_layout_block.view.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.footer__label_match_detail_user.view.*
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
    private val interestAdapter by lazy { MatchDetailUserInterestLabelAdapter() }
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

        stateLabel.retryBtn.onClick {
            stateLabel.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getOtherTags(target_accid)
        }

        rvLabel.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        rvLabel.adapter = adapter
        adapter.addFooterView(initFooterView())
        adapter.setEmptyView(R.layout.empty_layout_block, rvLabel)
        adapter.isUseEmpty(false)
        adapter.emptyView.emptyTip.text = "暂无标签"

        adapter.bindToRecyclerView(rvLabel)
    }

    override fun getOtherTagsResult(result: Boolean, data: OtherLabelsBean?) {
        if (result) {
            stateLabel.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (data != null && !data.other_interest.isNullOrEmpty() && !data.other_tags.isNullOrEmpty()) {
                adapter.setNewData(data.other_tags)
                if (!data.other_interest.isNullOrEmpty()) {
                    interestAdapter.setNewData(data.other_interest)
                }
            } else {
                stateLabel.viewState = MultiStateView.VIEW_STATE_EMPTY
            }
        } else {
            stateLabel.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }

    private fun initFooterView(): View? {
        val footerView = LayoutInflater.from(activity!!)
            .inflate(R.layout.footer__label_match_detail_user, rvLabel, false)
        val manager = FlexboxLayoutManager(activity!!, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        manager.justifyContent = JustifyContent.FLEX_START
        footerView.labelInterestRv.layoutManager = manager
        footerView.labelInterestRv.adapter = interestAdapter
        return footerView
    }

}