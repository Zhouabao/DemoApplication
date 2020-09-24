package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.blankj.utilcode.util.SPUtils
import com.google.gson.Gson
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.RecommendSquareListBean
import com.sdy.jitangapplication.presenter.MySquarePresenter
import com.sdy.jitangapplication.presenter.view.MySquareView
import com.sdy.jitangapplication.ui.activity.PublishActivity
import com.sdy.jitangapplication.ui.adapter.RecommendSquareAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.empty_my_square_layout.view.*
import kotlinx.android.synthetic.main.fragment_my_square.*
import kotlinx.android.synthetic.main.headerview_user_center_square.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity

/**
 * 我的广场
 */
class MySquareFragment : BaseMvpFragment<MySquarePresenter>(), MySquareView,
    OnRefreshListener, OnLoadMoreListener {

    private var page = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_square, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    //请求广场的参数 TODO要更新tagid
    private val params by lazy {
        hashMapOf<String, Any>(
            "page" to page,
            "type" to MyCollectionAndLikeFragment.TYPE_MINE,
            "pagesize" to Constants.PAGESIZE
        )
    }
    private val adapter by lazy { RecommendSquareAdapter() }
     fun loadData() {
        initView()
        if (!UserManager.touristMode)
            mPresenter.aboutMeSquareCandy(params)
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = MySquarePresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        refreshMySquare.setOnRefreshListener(this)
        refreshMySquare.setOnLoadMoreListener(this)
        rvMySquare.setHasFixedSize(true)
        val manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        manager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        rvMySquare.layoutManager = manager
        rvMySquare.adapter = adapter
        //android 瀑布流
        adapter.setHeaderAndEmpty(false)
        adapter.setEmptyView(R.layout.empty_my_square_layout, rvMySquare)
        adapter.emptyView.emptyPublishBtn.text = "发布动态"
        adapter.emptyView.emptyPublishBtn.onClick {
            mPresenter.checkBlock()
        }
        adapter.isUseEmpty(false)
    }


    /**
     *头部banner
     */
    private fun initHeadPublish(): View {
        val headPublish = LayoutInflater.from(activity!!)
            .inflate(R.layout.headerview_user_center_square, rvMySquare, false)
        headPublish.publishImg.setImageResource(R.drawable.icon_edit_me)
        headPublish.publishBtn.text = "发布动态"
        headPublish.publishCl.onClick {

            mPresenter.checkBlock()
        }
        return headPublish
    }


    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.aboutMeSquareCandy(params)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        mPresenter.aboutMeSquareCandy(params)
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshLikeEvent(event: RefreshLikeEvent) {
        if (event.position != -1 && event.squareId == adapter.data[event.position].id) {
            adapter.data[event.position].originalLike = event.isLike == 1
            adapter.data[event.position].isliked = event.isLike == 1
            adapter.data[event.position].like_cnt =
                if (event.likeCount >= 0) {
                    event.likeCount
                } else {
                    if (event.isLike == 1) {
                        adapter.data[event.position].like_cnt + 1
                    } else {
                        adapter.data[event.position].like_cnt - 1
                    }
                }

            adapter.refreshNotifyItemChanged(event.position)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshDeleteSquareEvent(event: RefreshDeleteSquareEvent) {
        for (data in adapter.data.withIndex()) {
            if (data.value.id == event.squareId) {
                adapter.remove(data.index)
                break
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshSquareEvent(event: RefreshSquareEvent) {
        refreshMySquare.autoRefresh()
    }


    //发布进度通知
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onProgressEvent(event: UploadEvent) {
        if (event.from == UploadEvent.FROM_USERCENTER) {
            page = 1
            params["page"] = page
            mPresenter.aboutMeSquareCandy(params)
            EventBus.getDefault().post(RefreshEvent(true))
        }
    }


    override fun onGetSquareListResult(data: RecommendSquareListBean?, b: Boolean) {
        if (refreshMySquare.state == RefreshState.Loading) {
            if (data?.list.isNullOrEmpty() || (data?.list
                    ?: mutableListOf()).size < Constants.PAGESIZE
            )
                refreshMySquare.finishLoadMoreWithNoMoreData()
            else
                refreshMySquare.finishLoadMore(b)

        } else {
            if (data?.list.isNullOrEmpty()) {
                adapter.isUseEmpty(true)
            }
            adapter.data.clear()
            adapter.notifyDataSetChanged()
            rvMySquare.scrollToPosition(0)
            refreshMySquare.finishRefresh(b)
        }

        if ((data?.list ?: mutableListOf()).size > 0) {
            for (data in data?.list ?: mutableListOf()) {
                data.originalLike = data.isliked
                data.originalLikeCount = data.like_cnt
            }
            adapter.addData(data?.list ?: mutableListOf())
            adapter.addData(mutableListOf())
        }


        if (adapter.data.size == 0) {
            adapter.isUseEmpty(true)
            refreshMySquare.finishLoadMoreWithNoMoreData()
        } else {
            adapter.setHeaderView(initHeadPublish())
            adapter.isUseEmpty(false)
        }

    }

    override fun onCheckBlockResult(b: Boolean) {
        if (b) {
            if (UserManager.publishState == 0) {
                startActivity<PublishActivity>("from" to 2)
            } else
                EventBus.getDefault().post(
                    RePublishEvent(
                        true,
                        MySquareFragment::class.java.simpleName
                    )
                )
        }
    }


    /*-------------------------------------- 重新上传-----------------------------*/
//    private var uploadCount = 0
//
//    private fun retryPublish() {
//        if (!mPresenter.checkNetWork()) {
//            CommonFunction.toast("网络不可用,请检查网络设置")
//            return
//        }
//        uploadCount = 0
//        //发布消息的类型0,纯文本的 1，照片 2，视频 3，声音
//        UserManager.publishState = 1
//        when {
//            UserManager.publishParams["type"] == 0 -> publish()
//            UserManager.publishParams["type"] == 1 -> {
//                UserManager.cancelUpload = false
//                uploadPictures()
//            }
//            UserManager.publishParams["type"] == 2 -> {
//                UserManager.cancelUpload = false
//                //TODO上传视频
//                val videoQnPath =
//                    "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME).getString(
//                        "accid"
//                    )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
//                        16
//                    )}"
//                mPresenter.uploadFile(1, 1, UserManager.mediaBeans[0].url, videoQnPath, 2)
//            }
//            UserManager.publishParams["type"] == 3 -> {
//                UserManager.cancelUpload = false
//                //TODO上传音频
//                val audioQnPath =
//                    "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME).getString(
//                        "accid"
//                    )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
//                        16
//                    )}"
//                mPresenter.uploadFile(1, 1, UserManager.mediaBeans[0].url, audioQnPath, 3)
//            }
//        }
//    }
//
//
//    private fun uploadPictures() {
//        //上传图片
//        val imagePath =
//            "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME).getString(
//                "accid"
//            )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
//                16
//            )}"
//        mPresenter.uploadFile(
//            UserManager.mediaBeans.size,
//            uploadCount + 1,
//            UserManager.mediaBeans[uploadCount].url,
//            imagePath,
//            1
//        )
//    }
//
//    private fun publish() {
//        mPresenter.publishContent(
//            UserManager.publishParams["type"] as Int,
//            UserManager.publishParams,
//            UserManager.keyList
//        )
//    }
//
//
//    //发布消息的类型0,纯文本的 1，照片 2，视频 3，声音
//    override fun onQnUploadResult(success: Boolean, type: Int, key: String?) {
//        if (success) {
//            when (type) {
//                0 -> {
//                    publish()
//                }
//                1 -> {
//                    UserManager.mediaBeans[uploadCount].url = key ?: ""
//                    UserManager.keyList.add(Gson().toJson(UserManager.mediaBeans[uploadCount]))
//                    uploadCount++
//                    if (uploadCount == UserManager.mediaBeans.size) {
//                        publish()
//                    } else {
//                        uploadPictures()
//                    }
//                }
//                2 -> {
//                    UserManager.mediaBeans[uploadCount].url = key ?: ""
//                    UserManager.keyList.add(Gson().toJson(UserManager.mediaBeans[0]))
//                    publish()
//                }
//                3 -> {
//                    UserManager.mediaBeans[uploadCount].url = key ?: ""
//                    UserManager.keyList.add(Gson().toJson(UserManager.mediaBeans[0]))
//                    publish()
//                }
//            }
//        } else {
//            onProgressEvent(UploadEvent(qnSuccess = false))
//        }
//    }
//
//    override fun onSquareAnnounceResult(type: Int, success: Boolean, code: Int) {
//        onAnnounceEvent(AnnounceEvent(success, code))
//        EventBus.getDefault().postSticky(UploadEvent(1, 1, 1.0, from = UploadEvent.FROM_USERCENTER))
//
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
//    fun onAnnounceEvent(event: AnnounceEvent) {
////        if (event.serverSuccess) {
////            UserManager.clearPublishParams()
////            CommonFunction.toast("动态发布成功!")
////        } else {
////            UserManager.cancelUpload = true
////            if (event.code == 402) { //内容违规重新去编辑
////                UserManager.publishState = -1
////                CommonFunction.toast("内容违规请重新编辑")
////            } else { //发布失败重新发布
////                UserManager.publishState = -2
////                CommonFunction.toast("发布失败")
////            }
////        }
//    }
}
