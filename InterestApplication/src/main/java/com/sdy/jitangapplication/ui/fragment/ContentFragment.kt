package com.sdy.jitangapplication.ui.fragment

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.PopupWindow
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
import com.google.gson.Gson
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.presenter.ContentPresenter
import com.sdy.jitangapplication.presenter.view.ContentView
import com.sdy.jitangapplication.ui.activity.PublishActivity
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.dialog.TouristDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import kotlinx.android.synthetic.main.fragment_content.*
import kotlinx.android.synthetic.main.popupwindow_square_filter_gender.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import java.util.*

/**
 * 内容页面
 */
class ContentFragment : BaseMvpFragment<ContentPresenter>(), ContentView {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    private val sp by lazy { SPUtils.getInstance(Constants.SPNAME) }
    private val filterPopupWindow by lazy {
        PopupWindow(activity!!).apply {
            contentView =
                LayoutInflater.from(activity!!)
                    .inflate(R.layout.popupwindow_square_filter_gender, null, false)
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            setBackgroundDrawable(null)
            isOutsideTouchable = true

            contentView.genderAll.onClick {
                contentView.genderAll.setTextColor(activity!!.resources.getColor(R.color.colorOrange))
                contentView.genderMan.setTextColor(Color.parseColor("#191919"))
                contentView.genderWoman.setTextColor(Color.parseColor("#191919"))
                filterGenderBtn.setImageResource(R.drawable.icon_square_filter_gender)
                sp.put("filter_square_gender", 3)
                EventBus.getDefault().post(RefreshSquareByGenderEvent())
                dismiss()
            }
            contentView.genderMan.onClick {
                contentView.genderMan.setTextColor(activity!!.resources.getColor(R.color.colorOrange))
                contentView.genderAll.setTextColor(Color.parseColor("#191919"))
                contentView.genderWoman.setTextColor(Color.parseColor("#191919"))
                filterGenderBtn.setImageResource(R.drawable.icon_square_filter_man)
                sp.put("filter_square_gender", 1)
                EventBus.getDefault().post(RefreshSquareByGenderEvent())
                dismiss()

            }
            contentView.genderWoman.onClick {
                contentView.genderWoman.setTextColor(activity!!.resources.getColor(R.color.colorOrange))
                contentView.genderAll.setTextColor(Color.parseColor("#191919"))
                contentView.genderMan.setTextColor(Color.parseColor("#191919"))
                filterGenderBtn.setImageResource(R.drawable.icon_square_filter_woman)
                sp.put("filter_square_gender", 2)
                EventBus.getDefault().post(RefreshSquareByGenderEvent())
                dismiss()
            }
        }
    }


    fun loadData() {
        initView()

    }

    private fun initView() {
        EventBus.getDefault().register(this)
        mPresenter = ContentPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        initFragments()
        initViewpager()

        publishSquareBtn.clickWithTrigger {
            //游客模式则提醒登录
            if (UserManager.touristMode) {
                TouristDialog(activity!!).show()
            } else
                mPresenter.checkBlock()
        }


        /**
         *性别筛选
         */
        if (sp.getInt("filter_square_gender", 3) == 3) {
            filterGenderBtn.setImageResource(R.drawable.icon_square_filter_gender)
        } else if (sp.getInt("filter_square_gender", 3) == 1) {
            filterGenderBtn.setImageResource(R.drawable.icon_square_filter_man)
        } else if (sp.getInt("filter_square_gender", 3) == 2) {
            filterGenderBtn.setImageResource(R.drawable.icon_square_filter_woman)
        }
        filterGenderBtn.onClick {
            if (filterPopupWindow.isShowing) {
                filterPopupWindow.dismiss()
            } else {
                filterPopupWindow.showAsDropDown(filterGenderBtn, 0, SizeUtils.dp2px(-15F))
                if (sp.getInt("filter_square_gender", 3) == 3) {
                    filterPopupWindow.contentView.genderAll.setTextColor(
                        activity!!.resources.getColor(
                            R.color.colorOrange
                        )
                    )
                    filterPopupWindow.contentView.genderMan.setTextColor(Color.parseColor("#191919"))
                    filterPopupWindow.contentView.genderWoman.setTextColor(Color.parseColor("#191919"))
                    filterGenderBtn.setImageResource(R.drawable.icon_square_filter_gender)

                } else if (sp.getInt("filter_square_gender", 3) == 1) {
                    filterPopupWindow.contentView.genderMan.setTextColor(
                        activity!!.resources.getColor(
                            R.color.colorOrange
                        )
                    )
                    filterPopupWindow.contentView.genderAll.setTextColor(Color.parseColor("#191919"))
                    filterPopupWindow.contentView.genderWoman.setTextColor(Color.parseColor("#191919"))
                    filterGenderBtn.setImageResource(R.drawable.icon_square_filter_man)

                } else if (sp.getInt("filter_square_gender", 3) == 2) {
                    filterPopupWindow.contentView.genderWoman.setTextColor(
                        activity!!.resources.getColor(
                            R.color.colorOrange
                        )
                    )
                    filterPopupWindow.contentView.genderAll.setTextColor(Color.parseColor("#191919"))
                    filterPopupWindow.contentView.genderMan.setTextColor(Color.parseColor("#191919"))
                    filterGenderBtn.setImageResource(R.drawable.icon_square_filter_woman)
                }

            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_content, container, false)
    }


    //fragment栈管理
    private val mStack = Stack<Fragment>()
    val titles by lazy { arrayOf("最新", "推荐", "兴趣", "附近") }


    private fun initFragments() {

        mStack.add(SquareNewestFragment())
        mStack.add(SquareRecommendFragment())
        mStack.add(TagSquareFragment())
        mStack.add(SquareNearbyFragment())
    }

    private fun initViewpager() {
        squareVp.setScrollable(true)
        squareVp.adapter = MainPagerAdapter(childFragmentManager, mStack)
        squareVp.offscreenPageLimit = 4

        squareVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                filterGenderBtn.isVisible = position != 2
            }

        })

        rgSquare.setViewPager(squareVp,titles)
        squareVp.currentItem = 1
    }



    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onProgressEvent(event: UploadEvent) {
        if (event.from == UploadEvent.FROM_SQUARE)
            if (event.qnSuccess) {
                llRetry.isVisible = false
                btnClose.isVisible = false
                uploadProgressBar.progress =
                    (((event.currentFileIndex - 1) * 1.0F / event.totalFileCount + (1.0F / event.totalFileCount * event.progress)) * 100).toInt()
                uploadProgressTv.text = "正在发布    ${uploadProgressBar.progress}%"
                uploadFl.isVisible = true
            } else {
                UserManager.cancelUpload = true
                UserManager.publishState = -2
            }

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAnnounceEvent(event: AnnounceEvent) {
        if (event.serverSuccess) {
            UserManager.clearPublishParams()
            uploadProgressTv.text = "动态发布成功!"
            uploadFl.postDelayed({
                uploadFl.isVisible = false
            }, 500)

            EventBus.getDefault().post(RefreshSquareEvent(true))
            SPUtils.getInstance(Constants.SPNAME).remove("draft", true)
        } else {
            UserManager.cancelUpload = true
            uploadFl.isVisible = true
            uploadProgressBar.progress = 0
            llRetry.isVisible = true
            btnClose.isVisible = true

            if (event.code == 402) { //内容违规重新去编辑
                UserManager.publishState = -1
                uploadProgressTv.text = "内容违规请重新编辑"
                iconRetry.setImageResource(R.drawable.icon_edit_retry)
                editRetry.text = "编辑"
                llRetry.onClick {
                    SPUtils.getInstance(Constants.SPNAME)
                        .put("draft", UserManager.publishParams["descr"] as String)
                    UserManager.clearPublishParams()

                    startActivity<PublishActivity>()
                    UserManager.publishState = 0
                    uploadFl.isVisible = false
                }
            } else { //发布失败重新发布
                UserManager.publishState = -2
                uploadProgressTv.text = "发布失败"
                iconRetry.setImageResource(R.drawable.icon_retry)
                editRetry.text = "重试"
                llRetry.onClick {
                    retryPublish()
                }
            }
            //取消重新发布，清除本地所存下的发布的数据
            btnClose.onClick {
                uploadFl.isVisible = false
                UserManager.clearPublishParams()
            }
        }
    }


    private var from = 1

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRePublishEvent(event: RePublishEvent) {
        from = if (event.context == MySquareFragment::class.java.simpleName) {
            2
        } else {
            1
        }
        if (UserManager.publishState == 1) {//正在发布中
            CommonFunction.toast("还有动态正在发布哦~请稍候")
            return
        } else if (UserManager.publishState == -2) {//发布失败
            CommonAlertDialog.Builder(activity!!)
                .setTitle("发布提示")
                .setContent("您有一条内容未成功发布，是否重新发布？")
                .setConfirmText("重新上传")
                .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                    override fun onClick(dialog: Dialog) {
                        dialog.cancel()
                        retryPublish()
                    }
                })
                .setCancelText("发布新内容")
                .setOnCancelListener(object : CommonAlertDialog.OnCancelListener {
                    override fun onClick(dialog: Dialog) {
                        dialog.cancel()
                        uploadFl.isVisible = false
                        UserManager.clearPublishParams()
                        if (!ActivityUtils.isActivityExistsInStack(PublishActivity::class.java))
                            if (event.context == MySquareFragment::class.java.simpleName) {
                                startActivity<PublishActivity>("from" to 2)
                            } else {
                                startActivity<PublishActivity>()
                            }
                    }
                })
                .create()
                .show()
        } else if (UserManager.publishState == -1) { //400
            SPUtils.getInstance(Constants.SPNAME)
                .put("draft", UserManager.publishParams["descr"] as String)
            UserManager.clearPublishParams()
            if (!ActivityUtils.isActivityExistsInStack(PublishActivity::class.java))
                if (event.context == MySquareFragment::class.java.simpleName) {
                    startActivity<PublishActivity>("from" to 2)
                } else {
                    startActivity<PublishActivity>()
                }
            uploadFl.isVisible = false
        } else if (UserManager.publishState == 0) {
            if (event.context == MySquareFragment::class.java.simpleName) {
                startActivity<PublishActivity>("from" to 2)
            } else {
                activity!!.intent.setClass(activity!!, PublishActivity::class.java)
                startActivity(activity!!.intent)
            }
        }
    }


    /*-------------------------------------- 重新上传-----------------------------*/
    private var uploadCount = 0

    private fun retryPublish() {
        if (!mPresenter.checkNetWork()) {
            uploadProgressTv.text = "网络不可用,请检查网络设置"
            return
        } else {
            uploadProgressTv.text = ""
        }
        uploadCount = 0
        llRetry.isVisible = false
        btnClose.isVisible = false
        //发布消息的类型0,纯文本的 1，照片 2，视频 3，声音
        UserManager.publishState = 1
        when {
            UserManager.publishParams["type"] == 0 -> publish()
            UserManager.publishParams["type"] == 1 -> {
                UserManager.cancelUpload = false
                uploadPictures()
            }
            UserManager.publishParams["type"] == 2 -> {
                UserManager.cancelUpload = false
                //TODO上传视频
                val videoQnPath =
                    "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME)
                        .getString(
                            "accid"
                        )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                        16
                    )}"
                mPresenter.uploadFile(1, 1, UserManager.mediaBeans[0].url, videoQnPath, 2)
            }
            UserManager.publishParams["type"] == 3 -> {
                UserManager.cancelUpload = false
                //TODO上传音频
                val audioQnPath =
                    "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME)
                        .getString(
                            "accid"
                        )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                        16
                    )}"
                mPresenter.uploadFile(1, 1, UserManager.mediaBeans[0].url, audioQnPath, 3)
            }
        }
    }


    //发布消息的类型0,纯文本的 1，照片 2，视频 3，声音
    override fun onQnUploadResult(success: Boolean, type: Int, key: String?) {
        if (success) {
            when (type) {
                0 -> {
                    publish()
                }
                1 -> {
                    UserManager.mediaBeans[uploadCount].url = key ?: ""
                    UserManager.keyList.add(Gson().toJson(UserManager.mediaBeans[uploadCount]))
                    uploadCount++
                    if (uploadCount == UserManager.mediaBeans.size) {
                        publish()
                    } else {
                        uploadPictures()
                    }
                }
                2 -> {
                    UserManager.mediaBeans[uploadCount].url = key ?: ""
                    UserManager.keyList.add(Gson().toJson(UserManager.mediaBeans[0]))
                    publish()
                }
                3 -> {
                    UserManager.mediaBeans[uploadCount].url = key ?: ""
                    UserManager.keyList.add(Gson().toJson(UserManager.mediaBeans[0]))
                    publish()
                }
            }
        } else {
            onProgressEvent(UploadEvent(qnSuccess = false))
        }
    }

    override fun onSquareAnnounceResult(type: Int, success: Boolean, code: Int) {
        onAnnounceEvent(AnnounceEvent(success, code))
        if (from == 2) {
            EventBus.getDefault()
                .postSticky(UploadEvent(1, 1, 1.0, from = UploadEvent.FROM_USERCENTER))
        }
        from = 1
    }


    //验证用户是否被封禁结果
    override fun onCheckBlockResult(result: Boolean) {
        if (result) {
            onRePublishEvent(RePublishEvent(true, ContentFragment::class.java.simpleName))
        }
    }


    private fun uploadPictures() {
        //上传图片
        val imagePath =
            "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME)
                .getString(
                    "accid"
                )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                16
            )}"
        mPresenter.uploadFile(
            UserManager.mediaBeans.size,
            uploadCount + 1,
            UserManager.mediaBeans[uploadCount].url,
            imagePath,
            1
        )
    }

    private fun publish() {
        mPresenter.publishContent(
            UserManager.publishParams["type"] as Int,
            UserManager.publishParams,
            UserManager.keyList
        )
    }

}
