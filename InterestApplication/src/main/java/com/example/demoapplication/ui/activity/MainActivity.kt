package com.example.demoapplication.ui.activity

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.SPUtils
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.event.NewMsgEvent
import com.example.demoapplication.event.UpdateLabelEvent
import com.example.demoapplication.model.LabelBean
import com.example.demoapplication.presenter.MainPresenter
import com.example.demoapplication.presenter.view.MainView
import com.example.demoapplication.ui.adapter.MainPagerAdapter
import com.example.demoapplication.ui.adapter.MatchLabelAdapter
import com.example.demoapplication.ui.dialog.FilterUserDialog
import com.example.demoapplication.ui.fragment.MatchFragment
import com.example.demoapplication.ui.fragment.SquareFragment
import com.example.demoapplication.utils.AMapManager
import com.example.demoapplication.utils.UserManager
import com.example.demoapplication.widgets.ScaleTransitionPagerTitleView
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_match_filter.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivityForResult
import java.util.*

//在支持路由的页面上添加注解（必选）
//这里的路径需要注意的是至少需要两级,/xx/xx
//路径标签个人建议写在一个类里面，方便统一管理和维护

class MainActivity : BaseMvpActivity<MainPresenter>(), MainView, View.OnClickListener {

    //fragment栈管理
    private val mStack = Stack<Fragment>()
    //匹配
    private val matchFragment by lazy { MatchFragment() }
    //广场
    private val squareFragment by lazy { SquareFragment() }
    private val titles = arrayOf("匹配", "发布")

    override fun onCreate(savedInstanceState: Bundle?) {
        // 设置一个exit transition
        /*window?.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window?.enterTransition = Explode()
        window?.exitTransition = Explode()*/
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)
        initView()


        //如果定位信息没有就重新定位
        if (UserManager.getlatitude().toDouble() == 0.0 || UserManager.getlongtitude().toDouble() == 0.0)
            AMapManager.initLocation(this)
        initFragment()
        filterBtn.setOnClickListener(this)
    }


    private fun initView() {
        filterBtn.setOnClickListener(this)
        notificationBtn.setOnClickListener(this)
        mPresenter = MainPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        GlideUtil.loadAvatorImg(this, SPUtils.getInstance(Constants.SPNAME).getString("avatar"), ivUserFace)
        initHeadView()

    }


    /*
      初始化Fragment栈管理
   */
    private fun initFragment() {
        mStack.add(matchFragment)
        mStack.add(squareFragment)
        vpMain.adapter = MainPagerAdapter(supportFragmentManager, mStack, titles)
        initIndicator()
        vpMain.currentItem = 0
        vpMain.setScrollable(false)
    }

    //筛选对话框
    private val filterUserDialog: FilterUserDialog by lazy {
        FilterUserDialog(
            this
        )
    }


    /**
     * 展示筛选条件对话框
     */
    private fun showFilterDialog() {
        filterUserDialog.show()
        filterUserDialog.btnCompleteFilter.onClick {
            //TODO("发起网络请求，并关闭对话框")

        }
        filterUserDialog.seekBarAge.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

            override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
                filterUserDialog.filterAge.text = "${leftValue.toInt()}-${rightValue.toInt()}岁"
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }

        })

    }


    //TODO("联动") https://blog.csdn.net/YANGWEIQIAO/article/details/78959968
    override fun onClick(view: View) {
        when (view.id) {
            R.id.filterBtn -> {
                showFilterDialog()
            }
            R.id.notificationBtn -> {
            }
        }
    }


    override fun onPause() {
        super.onPause()
        //        GSYVideoManager.onPause()

    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().post(NewMsgEvent(21, 6, 3, 2))
        //        GSYVideoManager.onResume(false)

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        //SPUtils.getInstance(Constants.SPNAME).remove("globalLabelId",true)
        //SPUtils.getInstance(Constants.SPNAME).remove("globalLabelPath",true)
        //        GSYVideoManager.releaseAllVideos()

    }


    override fun onUpdateFilterResult() {
        if (filterUserDialog.isShowing)
            filterUserDialog.dismiss()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewMsgEvent(event: NewMsgEvent) {
        msgLike.text = event.likeCount.toString()
        msgHi.text = event.HiCount.toString()
        msgChat.text = event.chatCount.toString()
        msgSquare.text = event.squareCount.toString()
        llMsgCount.visibility = View.VISIBLE
        YoYo.with(Techniques.Bounce)
            .duration(2000)
            .withListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    llMsgCount.visibility = View.GONE
                }

            })
            .delay(1000)
            .playOn(llMsgCount)


    }


    /**
     * 好友列表和标签列表
     * 设置头部数据一直居于最顶端
     */
    //标签适配器
    private val labelAdapter: MatchLabelAdapter by lazy { MatchLabelAdapter(this) }
    //标签数据源
    var labelList: MutableList<LabelBean> = mutableListOf()

    companion object {
        const val REQUEST_LABEL_CODE = 2000
    }

    private fun initHeadView() {
        val labelManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        headRvLabels.layoutManager = labelManager
        headRvLabels.adapter = labelAdapter
        labelAdapter.dataList = labelList
        labelAdapter.setOnItemClickListener(object : MatchLabelAdapter.OnItemClickListener {
            override fun onItemClick(item: View, position: Int) {
                if (position == 0) {
                    startActivityForResult<LabelsActivity>(
                        REQUEST_LABEL_CODE,
                        "from" to "mainactivity"
                    )
                } else {
                    for (index in 0 until labelAdapter.dataList.size) {
                        labelAdapter.dataList[index].checked = if (index == position - 1) {
                            SPUtils.getInstance(Constants.SPNAME).put("globalLabelId", labelAdapter.dataList[index].id)
                            true
                        } else {
                            false
                        }
                    }
                    labelAdapter.notifyDataSetChanged()
                    EventBus.getDefault().post(UpdateLabelEvent(labelList[position - 1]))
                }
            }

        })
        initData()
    }

    private fun initData() {
        labelList = UserManager.getSpLabels()
        if (labelList.size > 0) {
            labelList[0].checked = true
            SPUtils.getInstance(Constants.SPNAME).put("globalLabelId", labelList[0].id)
        }
        labelAdapter.setData(labelList)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_LABEL_CODE) {
                val list = UserManager.getSpLabels()
                for (i in 0 until labelList.size) {
                    for (j in 0 until list.size) {
                        if (labelList[i].id == list[j].id) {
                            list[j].checked = labelList[i].checked
                        }
                    }
                }
                labelList = list
                labelAdapter.setData(labelList)
            }
        }
    }


    private fun initIndicator() {
        tabMain.setBackgroundColor(Color.WHITE)
        val commonNavigator = CommonNavigator(this)
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return mStack.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val simplePagerTitleView = ScaleTransitionPagerTitleView(context)
                simplePagerTitleView.text = titles[index]
                simplePagerTitleView.textSize = 20f
                simplePagerTitleView.normalColor = resources.getColor(R.color.colorGrayText)
                simplePagerTitleView.selectedColor = resources.getColor(R.color.colorBlackTitle)
                simplePagerTitleView.onClick {
                    vpMain.currentItem = index
                }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineHeight = UIUtil.dip2px(context, 4.0).toFloat()
                indicator.lineWidth = UIUtil.dip2px(context, 35.0).toFloat()
                indicator.roundRadius = UIUtil.dip2px(context, 2.0).toFloat()
                indicator.startInterpolator = AccelerateInterpolator()
                indicator.endInterpolator = DecelerateInterpolator(2.0f)
                indicator.setColors(resources.getColor(R.color.colorOrange))
                return indicator
            }
        }
        tabMain.navigator = commonNavigator
        ViewPagerHelper.bind(tabMain, vpMain)
    }

}
