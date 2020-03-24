package com.sdy.jitangapplication.ui.fragment


import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SquareTitleBean
import com.sdy.jitangapplication.presenter.IndexPresenter
import com.sdy.jitangapplication.presenter.view.IndexView
import com.sdy.jitangapplication.ui.adapter.IndexSwitchAdapter
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.dialog.FilterUserDialog
import kotlinx.android.synthetic.main.fragment_index.*
import java.util.*

/**
 * 首页fragment
 */
class IndexFragment : BaseMvpLazyLoadFragment<IndexPresenter>(), IndexView {

    private val fragments by lazy { Stack<Fragment>() }
    private val matchFragment by lazy { MatchFragment() }
    private val findByTagFragment by lazy { FindByTagFragment() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_index, container, false)
    }

    val titleAdapter by lazy { IndexSwitchAdapter() }

    override fun loadData() {

        initFragments()

        filterBtn.onClick {
            FilterUserDialog(activity!!).show()
        }
    }

    private fun initFragments() {
        titleIndex.layoutManager = LinearLayoutManager(activity!!, RecyclerView.HORIZONTAL, false)
        titleAdapter.addData(
            SquareTitleBean(
                "匹配",
                true,
                R.drawable.icon_tab_top_match_normal,
                R.drawable.icon_tab_top_match_checked
            )
        )
        titleAdapter.addData(
            SquareTitleBean(
                "兴趣",
                false,
                R.drawable.icon_tab_top_tag_normal,
                R.drawable.icon_tab_top_tag_checked
            )
        )
        titleIndex.adapter = titleAdapter
        titleAdapter.setOnItemClickListener { _, view, position ->
            for (data in titleAdapter.data) {
                data.checked = data == titleAdapter.data[position]
            }
            filterBtn.isVisible = position == 0
            titleAdapter.notifyDataSetChanged()
            vpIndex.currentItem = position
        }

        fragments.add(matchFragment)
        fragments.add(findByTagFragment)
        vpIndex.setScrollable(false)
        vpIndex.offscreenPageLimit = 2
        vpIndex.adapter = MainPagerAdapter(activity!!.supportFragmentManager, fragments)
        vpIndex.currentItem = 0
    }


}
