package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SizeUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.UpdateFindByTagEvent
import com.sdy.jitangapplication.event.UpdateFindByTagListEvent
import com.sdy.jitangapplication.event.UpdateMyLabelEvent
import com.sdy.jitangapplication.event.UserCenterEvent
import com.sdy.jitangapplication.model.AddSinlgLabelBean
import com.sdy.jitangapplication.model.FindByTagBean
import com.sdy.jitangapplication.model.MyLabelBean
import com.sdy.jitangapplication.model.NewLabel
import com.sdy.jitangapplication.presenter.FindByTagListPresenter
import com.sdy.jitangapplication.presenter.view.FindByTagListView
import com.sdy.jitangapplication.ui.adapter.FindByTagListAdapter
import com.sdy.jitangapplication.ui.dialog.AddToLabelDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_find_by_tag_list.*
import kotlinx.android.synthetic.main.dialog_add_to_label.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.popupwindow_square_filter_distance.view.*
import kotlinx.android.synthetic.main.popupwindow_square_filter_gender.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 * 标签找人具体标签下的人
 */
class FindByTagListActivity : BaseMvpActivity<FindByTagListPresenter>(), FindByTagListView, OnRefreshListener,
    OnLoadMoreListener {

    private var page: Int = 1
    private val labelBean by lazy { intent.getSerializableExtra("labelBean") as NewLabel }
    private var isJoin = false //是否加入
    private var isFull = false //是否完善
    private var myLabelBean: MyLabelBean? = null

    private val adapter by lazy { FindByTagListAdapter() }
    //请求广场的参数 TODO要更新tagid
    private val params by lazy {
        hashMapOf(
            "tag_id" to labelBean.id,
            "lng" to UserManager.getlongtitude().toFloat(),
            "lat" to UserManager.getlatitude().toFloat(),
            "city_code" to UserManager.getCityCode(),
            "type" to 1,
            "page" to page,
            "pagesize" to Constants.PAGESIZE
        )

    }

    private val addLabelDialog: AddToLabelDialog by lazy { AddToLabelDialog(this, labelBean) }

    /**
     * 性别筛选
     */
    private val filterGenderPopupWindow by lazy {
        PopupWindow(this).apply {
            contentView =
                LayoutInflater.from(this@FindByTagListActivity)
                    .inflate(R.layout.popupwindow_square_filter_gender, null, false)
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            setBackgroundDrawable(null)
            isOutsideTouchable = true

            contentView.genderAll.onClick {
                filterByGender.text = "不限性别"
                contentView.genderAll.setTextColor(resources.getColor(R.color.colorOrange))
                contentView.genderMan.setTextColor(Color.parseColor("#191919"))
                contentView.genderWoman.setTextColor(Color.parseColor("#191919"))
                params["gender"] = 3
                refreshSamePerson.autoRefresh()
                dismiss()
            }
            contentView.genderMan.onClick {
                filterByGender.text = "只看男士"
                contentView.genderMan.setTextColor(resources.getColor(R.color.colorOrange))
                contentView.genderAll.setTextColor(Color.parseColor("#191919"))
                contentView.genderWoman.setTextColor(Color.parseColor("#191919"))
                params["gender"] = 1
                refreshSamePerson.autoRefresh()
                dismiss()
            }
            contentView.genderWoman.onClick {
                filterByGender.text = "只看女士"
                contentView.genderWoman.setTextColor(resources.getColor(R.color.colorOrange))
                contentView.genderAll.setTextColor(Color.parseColor("#191919"))
                contentView.genderMan.setTextColor(Color.parseColor("#191919"))
                params["gender"] = 2
                refreshSamePerson.autoRefresh()
                dismiss()
            }
        }
    }

    /**
     * 活跃度和距离筛选
     * rank:1活跃 2 距离
     */
    private val filterDistancePopupWindow by lazy {
        PopupWindow(this).apply {
            contentView = LayoutInflater.from(this@FindByTagListActivity)
                .inflate(R.layout.popupwindow_square_filter_distance, null, false)
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            setBackgroundDrawable(null)
            isOutsideTouchable = true

            contentView.filterActive.onClick {
                filterByDistance.text = "按活跃"
                contentView.filterActive.setTextColor(resources.getColor(R.color.colorOrange))
                contentView.filterDistance.setTextColor(Color.parseColor("#191919"))
                params["rank"] = 1
                refreshSamePerson.autoRefresh()
                dismiss()
            }
            contentView.filterDistance.onClick {
                filterByDistance.text = "按距离"
                contentView.filterDistance.setTextColor(resources.getColor(R.color.colorOrange))
                contentView.filterActive.setTextColor(Color.parseColor("#191919"))
                params["rank"] = 2
                refreshSamePerson.autoRefresh()
                dismiss()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_by_tag_list)
        initView()
        mPresenter.lookForPeopleTag(params)
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = FindByTagListPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        filterByDistance.onClick {
            filterDistancePopupWindow.showAsDropDown(filterByDistance, SizeUtils.dp2px(15F), 0)
        }

        filterByGender.onClick {
            filterGenderPopupWindow.showAsDropDown(filterByGender, SizeUtils.dp2px(-26F), 0)
        }

        refreshSamePerson.setOnRefreshListener(this)
        refreshSamePerson.setOnLoadMoreListener(this)
        refreshSamePerson.setPrimaryColorsId(R.color.colorTransparent)

        stateSamePerson.retryBtn.onClick {
            stateSamePerson.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.lookForPeopleTag(params)
        }

        btnBack.onClick {
            finish()
        }
        (statusView.layoutParams as RelativeLayout.LayoutParams).height = BarUtils.getStatusBarHeight()
        llTitle.setBackgroundColor(Color.TRANSPARENT)
        rightBtn1.isVisible = true
        btnBack.setImageResource(R.drawable.icon_back_white)


        samePersonRv.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        samePersonRv.adapter = adapter
        samePersonRv.setHasFixedSize(true)
        adapter.setOnItemClickListener { _, view, position ->
            view.isEnabled = false
            MatchDetailActivity.start(this, adapter.data[position].accid)
            view.postDelayed({
                view.isEnabled = true
            }, 1000L)
        }


        initData()
    }

    private fun initData() {
        labelName.text = labelBean.title
        labelUseCount.text = "${labelBean.used_cnt}"
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.lookForPeopleTag(params)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        adapter.hasmore = true
        adapter.data.clear()
        params["page"] = page
        mPresenter.lookForPeopleTag(params)
    }

    private var showAdd = false
    override fun onGetTitleInfoResult(b: Boolean, data: FindByTagBean?) {
        if (b) {
            if ((data?.list.isNullOrEmpty() || data?.list!!.size < Constants.PAGESIZE)) {
                adapter.hasmore = false
            }
            isJoin = data?.isjoin ?: false
            isFull = data?.is_full ?: false
            myLabelBean = data?.tag_info
            setLabelStateBtn()
            GlideUtil.loadImgCenterCrop(this, data?.tag_icon, samePersonBg)
            stateSamePerson.viewState = MultiStateView.VIEW_STATE_CONTENT
            adapter.addData(data?.list ?: mutableListOf())

            if (!isJoin && !showAdd) {
                showAdd = true
                addLabelDialog.show()
                addLabelDialog.addToLabelBtn.onClick {
                    //todo  加入自己的标签
                    mPresenter.addMyTagsSingle(labelBean.id)
                    addLabelDialog.dismiss()
                }
            }
        } else {
            stateSamePerson.viewState = MultiStateView.VIEW_STATE_ERROR
        }
        if (refreshSamePerson.state == RefreshState.Refreshing) {
            refreshSamePerson.finishRefresh(b)
            refreshSamePerson.resetNoMoreData()

        } else if (refreshSamePerson.state == RefreshState.Loading) {
            if (b && data?.list.isNullOrEmpty())
                refreshSamePerson.finishLoadMoreWithNoMoreData()
            else
                refreshSamePerson.finishLoadMore(b)
        }
    }

    private fun setLabelStateBtn() {
        if (isJoin) {
            if (!isFull) {
                rightBtn1.isVisible = true
                rightBtn1.text = "完善兴趣"
                rightBtn1.onClick {
                    startActivity<LabelQualityActivity>(
                        "aimData" to myLabelBean, "mode" to if (myLabelBean?.label_quality.isNullOrEmpty()) {
                            LabelQualityActivity.MODE_NEW
                        } else {
                            LabelQualityActivity.MODE_EDIT
                        }
                    )
                }
            } else {
                rightBtn1.text = "编辑兴趣"
                rightBtn1.onClick {
                    startActivity<LabelQualityActivity>(
                        "aimData" to myLabelBean, "mode" to if (myLabelBean?.label_quality.isNullOrEmpty()) {
                            LabelQualityActivity.MODE_NEW
                        } else {
                            LabelQualityActivity.MODE_EDIT
                        }
                    )
                }
            }
        } else {
            rightBtn1.isVisible = true
            rightBtn1.text = "加入兴趣"
            rightBtn1.onClick {
                //todo  加入自己的标签
                mPresenter.addMyTagsSingle(labelBean.id)
            }
        }
    }

    override fun onAddLabelResult(result: Boolean, data: AddSinlgLabelBean?) {
        if (result) {
            if (data?.tag_info != null)
                myLabelBean = data?.tag_info
            startActivity<LabelQualityActivity>(
                "aimData" to myLabelBean, "mode" to if (myLabelBean?.label_quality.isNullOrEmpty()) {
                    LabelQualityActivity.MODE_NEW
                } else {
                    LabelQualityActivity.MODE_EDIT
                }
            )

            isJoin = true
            setLabelStateBtn()

            //保存标签
            UserManager.saveLabels(data?.tag_list ?: mutableListOf())
            EventBus.getDefault().post(UpdateMyLabelEvent())
            EventBus.getDefault().post(UserCenterEvent(true))
            EventBus.getDefault().post(UpdateFindByTagEvent())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateFindByTagListEvent(event: UpdateFindByTagListEvent) {
        refreshSamePerson.autoRefresh()
    }
}
