package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.demoapplication.R
import com.example.demoapplication.event.BlockDataEvent
import com.example.demoapplication.event.ListDataEvent
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.model.StatusBean
import com.example.demoapplication.presenter.MatchDetailPresenter
import com.example.demoapplication.presenter.view.MatchDetailView
import com.example.demoapplication.ui.adapter.DetailThumbAdapter
import com.example.demoapplication.ui.adapter.MatchDetailLabelAdapter
import com.example.demoapplication.ui.adapter.MatchImgsPagerAdapter
import com.example.demoapplication.ui.chat.MatchSucceedActivity
import com.example.demoapplication.ui.dialog.ChargeVipDialog
import com.example.demoapplication.ui.dialog.MoreActionDialog
import com.example.demoapplication.ui.fragment.BlockSquareFragment
import com.example.demoapplication.ui.fragment.ListSquareFragment
import com.example.demoapplication.utils.UserManager
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import kotlinx.android.synthetic.main.activity_match_detail1.*
import kotlinx.android.synthetic.main.dialog_more_action.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import java.util.*

/**
 * 匹配详情页
 */
class MatchDetailActivity : BaseMvpActivity<MatchDetailPresenter>(), MatchDetailView, OnRefreshListener,
    View.OnClickListener {

    private val targetAccid by lazy { intent.getStringExtra("target_accid") }
    private var matchBean: MatchBean? = null
    private val thumbAdapter by lazy { DetailThumbAdapter(this) }

    var photos: MutableList<String> = mutableListOf()
    private val photosAdapter by lazy { MatchImgsPagerAdapter(this, photos) }

    private val labelsAdapter by lazy { MatchDetailLabelAdapter(this) }

    private val params by lazy {
        hashMapOf(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "target_accid" to targetAccid,
            "_sign" to "",
            "_timestamp" to System.currentTimeMillis()
        )
    }
    private val targetUserPramas by lazy {
        hashMapOf(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "target_accid" to targetAccid,
            "_sign" to "",
            "_timestamp" to System.currentTimeMillis()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_detail1)

        initView()

        mPresenter.getUserDetailInfo(params)
    }


    private fun initView() {
        mPresenter = MatchDetailPresenter()
        mPresenter.mView = this
        mPresenter.context = this


        //设置图片的宽度占满屏幕，宽高比3:4
        val layoutParams = detailPhotosVp.layoutParams
        layoutParams.width = ScreenUtils.getScreenWidth()
        layoutParams.height = (4 / 3.0F * layoutParams.width).toInt()
        detailPhotosVp.layoutParams = layoutParams


        moreBtn.setOnClickListener(this)
        detailUserLikeBtn.setOnClickListener(this)
        detailUserChatBtn.setOnClickListener(this)
        backBtn.setOnClickListener(this)

        //用户的广场预览界面
        detailThumbRv.layoutManager = LinearLayoutManager(this, LinearLayout.HORIZONTAL, false)
        //用户标签
        val manager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        detailLabelRv.layoutManager = manager

        initFragment()

        detailSquareSwitchRg.setOnCheckedChangeListener { radioGroup, checkedId ->
            if (checkedId == R.id.rbList) {
                changeFragment(1)
                if (!loadList) {
                    EventBus.getDefault().post(ListDataEvent(targetAccid, true))
                }
                loadList = true
                currIndex = 1
            } else if (checkedId == R.id.rbBlock) {
                changeFragment(0)
                if (!loadBlock) {
                    EventBus.getDefault().post(BlockDataEvent(targetAccid, true))
                }
                loadBlock = true
                currIndex = 0
            }
        }


        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getUserDetailInfo(params)
        }
    }

    private fun initData() {
        detailUserName.text = matchBean!!.nickname
        detailUserInfo.text =
            "${matchBean!!.age} / ${if (matchBean!!.gender == 1) "男" else "女"} / ${matchBean!!.constellation} / ${matchBean!!.distance}"
        detailUserJob.text = "${matchBean!!.jobname}"
        detailUserSign.text = "${matchBean!!.sign}"
        detailUserLeftChatCount.text = "${matchBean!!.lightningcnt}"
        detailUserJob.visibility = if (matchBean!!.jobname.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
        detailUserVip.visibility = if (matchBean!!.isvip == 1) {
            View.VISIBLE
        } else {
            View.GONE
        }


        //已感兴趣不做操作
        if (matchBean!!.isliked == 1) {
            detailUserLikeBtn.isEnabled = false
            detailUserLikeTv.text = "已感兴趣"
            detailUserLikeBtn.setBackgroundResource(R.drawable.shape_rectangle_solid_gray)
        } else {
            detailUserLikeBtn.isEnabled = true
            detailUserLikeTv.text = "感兴趣"
            detailUserLikeBtn.setBackgroundResource(R.drawable.shape_rectangle_solid_blue)
        }

//        用户动态封面图片
        if (matchBean!!.square == null || matchBean!!.square!!.size == 0) {
            detailThumbRv.visibility = View.GONE
        } else {
            detailThumbRv.adapter = thumbAdapter
            thumbAdapter.setData(matchBean!!.square ?: mutableListOf())
        }

        //用户照片
        detailPhotosVp.adapter = photosAdapter

        //fixme 这里要删除加进去的照片内容
        if (matchBean!!.photos == null || matchBean!!.photos!!.isEmpty())
            photos.add(matchBean!!.avatar ?: "")
        photos.addAll(matchBean!!.photos ?: mutableListOf())
        photosAdapter.notifyDataSetChanged()
        setViewpagerAndIndicator()

        //用户标签
        detailLabelRv.adapter = labelsAdapter
        labelsAdapter.setData(matchBean!!.tags ?: mutableListOf())

    }

    /**
     * 设置竖直滑动的vp2以及其滑动的indicator
     */
    private fun setViewpagerAndIndicator() {
        detailPhotosVp.setScrollable(false)
        detailPhotosVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                for (i in 0 until detailPhotosIndicator.size) {
                    (detailPhotosIndicator[i] as RadioButton).isChecked = i == position
                }
            }
        })

        if (photos.size > 1) {
            for (i in 0 until photos.size) {
                val indicator = RadioButton(this)
                indicator.width = ((ScreenUtils.getScreenWidth()
                        - SizeUtils.applyDimension(15F, TypedValue.COMPLEX_UNIT_DIP) * 2
                        - (SizeUtils.applyDimension(
                    6F,
                    TypedValue.COMPLEX_UNIT_DIP
                ) * (photos.size - 1))) / photos.size).toInt()
                indicator.height = SizeUtils.dp2px(5F)
                indicator.buttonDrawable = null
                indicator.background = resources.getDrawable(R.drawable.selector_round_indicator)

                indicator.layoutParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val layoutParams: LinearLayout.LayoutParams = indicator.layoutParams as LinearLayout.LayoutParams
                layoutParams.setMargins(
                    if (i == 0) {
                        SizeUtils.dp2px(15F)
                    } else {
                        0
                    }, 0, if (i == photos.size - 1) {
                        SizeUtils.dp2px(15F)
                    } else {
                        SizeUtils.dp2px(6f)
                    }, 0
                )
                indicator.layoutParams = layoutParams
                indicator.isEnabled = false
                indicator.isChecked = i == 0
                detailPhotosIndicator.addView(indicator)
            }
        }

        //上一张
        btnLast.onClick {
            if (detailPhotosVp.currentItem > 0) {
                val index = detailPhotosVp.currentItem
                detailPhotosVp.setCurrentItem(index - 1, true)
            }
        }

        //下一张
        btnNext.onClick {
            if (detailPhotosVp.currentItem < photos.size - 1) {
                val index = detailPhotosVp.currentItem
                detailPhotosVp.setCurrentItem(index + 1, true)
            }
        }


    }

    //fragment栈管理
    private val mStack = Stack<Fragment>()
    //九宫格
    private val blockFragment by lazy { BlockSquareFragment() }
    //列表
    private val listFragment by lazy { ListSquareFragment() }
    //标识 来确认是否已经加载过数据
    private var loadBlock = false
    private var loadList = false
    private var currIndex = 1

    /**
     * 初始化fragments
     */
    private fun initFragment() {
        val manager = supportFragmentManager.beginTransaction()
        manager.add(R.id.detail_content_fragment, blockFragment) //九宫格照片模式
        manager.add(R.id.detail_content_fragment, listFragment) //列表
        manager.commit()
        mStack.add(blockFragment)
        mStack.add(listFragment)
    }

    /**
     * 点击切换fragment
     */
    private fun changeFragment(position: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        for (fragment in mStack) {
            transaction.hide(fragment)
        }
        transaction.show(mStack[position])
        transaction.commit()
    }

    override fun onGetMatchDetailResult(success: Boolean, matchUserDetailBean: MatchBean?) {
//        refreshLayout.finishRefresh(success)
        if (success) {
            stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
            matchBean = matchUserDetailBean
            //本地对标签进行过滤筛选
            val labels = UserManager.getSpLabels()
            for (label in labels) {
                for (tag in matchBean!!.tags ?: mutableListOf()) {
                    if (label.id == tag.id) {
                        tag.sameLabel = true
                    }
                }
            }
            initData()

            //请求成功了请求列表广场
            detailSquareSwitchRg.check(R.id.rbList)
        } else {
            stateview.viewState = MultiStateView.VIEW_STATE_ERROR
            stateview.errorMsg.text= if (!mPresenter.checkNetWork()){getString(R.string.retry_net_error)}else{getString(R.string.retry_load_error)}
        }
    }

    override fun onGetUserActionResult(success: Boolean, result: String?) {
        if (success) {
            ToastUtils.showShort(result)
        }
    }


    override fun onGetLikeResult(success: Boolean, statusBean: BaseResp<StatusBean?>?) {
        if (success && statusBean != null) {
            ToastUtils.showShort(statusBean.msg)
            if (statusBean!!.data?.status == 1) {  //喜欢成功
                detailUserLikeBtn.isEnabled = false
                detailUserLikeTv.text = "已感兴趣"
                detailUserLikeBtn.setBackgroundResource(R.drawable.shape_rectangle_solid_gray)
            } else if (statusBean!!.data?.status == 2) {//匹配成功
                startActivity<MatchSucceedActivity>("matchBean" to matchBean!!)
            }

        }
    }


    override fun onRefresh(refreshLayout: RefreshLayout) {
        mPresenter.getUserDetailInfo(params)
        if (currIndex == 0) {
            EventBus.getDefault().post(BlockDataEvent(targetAccid, true))
        } else {
            EventBus.getDefault().post(ListDataEvent(targetAccid, true))
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.moreBtn -> {
                showMoreActionDialog()
            }
            R.id.detailUserLikeBtn -> {
                mPresenter.likeUser(params)
            }
            //todo  这里要判断是不是VIP用户 如果是VIP 直接进入聊天界面
            R.id.detailUserChatBtn -> {
                if (matchBean!!.isvip != 1) {
                    val dialog = ChargeVipDialog(this)
                    dialog.show()
                }
            }

            R.id.backBtn -> {
                finish()
            }
        }

    }

    //拉黑、举报、取消配对（判断对方是否为好友）、取消
    private fun showMoreActionDialog() {
        val dialog = MoreActionDialog(this, "matchDetail")
        dialog.show()
        dialog.llRemoveRelation.visibility = if (matchBean!!.isfriend == 1) {
            View.VISIBLE
        } else {
            View.GONE
        }

        //拉黑
        dialog.llLahei.onClick {
            mPresenter.shieldingFriend(targetUserPramas)
            dialog.dismiss()
        }
        //举报
        dialog.llJubao.onClick {
            mPresenter.reportUser(targetUserPramas)
            dialog.dismiss()
        }
        //解除配对
        dialog.llRemoveRelation.onClick {
            mPresenter.dissolutionFriend(targetUserPramas)
            dialog.dismiss()
        }
        dialog.cancel.onClick {
            dialog.dismiss()
        }
    }


}
