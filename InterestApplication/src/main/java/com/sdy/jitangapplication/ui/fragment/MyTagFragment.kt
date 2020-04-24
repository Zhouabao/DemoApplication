package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.UpdateMyLabelEvent
import com.sdy.jitangapplication.model.LabelQuality
import com.sdy.jitangapplication.presenter.MyTagPresenter
import com.sdy.jitangapplication.presenter.view.MyTagView
import com.sdy.jitangapplication.ui.activity.MyLabelActivity
import com.sdy.jitangapplication.ui.adapter.UserCenteTagAdapter
import kotlinx.android.synthetic.main.empty_my_square_layout.view.*
import kotlinx.android.synthetic.main.fragment_my_tag.*
import kotlinx.android.synthetic.main.headerview_user_center_square.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity

/**
 * 我的标签特质
 */
class MyTagFragment : BaseMvpLazyLoadFragment<MyTagPresenter>(), MyTagView {
    private val tagAdapter by lazy { UserCenteTagAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_tag, container, false)
    }

    override fun loadData() {
        initView()
    }

    private fun initView() {
        EventBus.getDefault().register(this)


        //用户标签
        val tagManager = GridLayoutManager(activity!!, 2)
        rvMyTag.layoutManager = tagManager
        rvMyTag.adapter = tagAdapter
        //android 瀑布流
        tagAdapter.setHeaderAndEmpty(false)
        tagAdapter.setEmptyView(R.layout.empty_my_square_layout, rvMyTag)
        tagAdapter.emptyView.emptyPublishBtn.text = "添加兴趣特质"
        tagAdapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_my_label_quality)
        tagAdapter.emptyView.emptyMySquareTip.text = "您还没有添加兴趣\n快去添加一些你喜欢的兴趣吧"
        tagAdapter.emptyView.emptyPublishBtn.onClick {
            startActivity<MyLabelActivity>()
        }


        tagAdapter.setOnItemClickListener { _, view, position ->
            startActivity<MyLabelActivity>()
//            val intent = Intent()
//            intent.putExtra("aimData", tagAdapter.data[position])
//            intent.putExtra("mode", LabelQualityActivity.MODE_NEW)
//            intent.setClass(activity!!, LabelQualityActivity::class.java)
//            startActivity(intent)
        }
    }

    fun setTagData(datas: MutableList<LabelQuality>) {
        tagAdapter.setNewData(datas)
        if (tagAdapter.data.size == 0) {
            tagAdapter.isUseEmpty(true)
        } else {
            tagAdapter.removeAllHeaderView()
            tagAdapter.addHeaderView(initHeadPublish())
        }

    }


    /**
     *头部banner
     */
    private fun initHeadPublish(): View {
        val headPublish = LayoutInflater.from(activity!!)
            .inflate(R.layout.headerview_user_center_square, rvMyTag, false)
        headPublish.publishImg.setImageResource(R.drawable.icon_add_tag_me)
        headPublish.publishBtn.text = "添加新兴趣特质"
        headPublish.publishCl.onClick {
            startActivity<MyLabelActivity>()
        }
        return headPublish
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateMyLabelEvent(event: UpdateMyLabelEvent) {
//        if (!event.tags.isNullOrEmpty())
        setTagData(event.tags ?: mutableListOf<LabelQuality>())
    }
}
