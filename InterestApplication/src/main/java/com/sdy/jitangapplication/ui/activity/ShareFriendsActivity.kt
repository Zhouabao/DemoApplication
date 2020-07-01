package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nim.uikit.common.util.sys.ClipboardUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.model.VipDescr
import com.sdy.jitangapplication.presenter.ShareFriendsPresenter
import com.sdy.jitangapplication.presenter.view.ShareFriendsView
import com.sdy.jitangapplication.ui.adapter.ShareFriendsAdapter
import com.sdy.jitangapplication.ui.dialog.MoreActionNewDialog
import kotlinx.android.synthetic.main.activity_share_friends.*
import kotlinx.android.synthetic.main.dialog_more_action_new.*
import kotlinx.android.synthetic.main.item_marquee_vip_friends.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * todo 数据填充
 * 分享给好友
 */
class ShareFriendsActivity : BaseMvpActivity<ShareFriendsPresenter>(), ShareFriendsView,
    OnLazyClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_friends)
        initView()
    }

    private val adapter by lazy { ShareFriendsAdapter() }
    private fun initView() {
        mPresenter = ShareFriendsPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        (shareFriendsIv.layoutParams as ConstraintLayout.LayoutParams).width =
            ScreenUtils.getScreenWidth()
        (shareFriendsIv.layoutParams as ConstraintLayout.LayoutParams).height =
            (ScreenUtils.getScreenWidth() * 245F / 375).toInt()

        hotT1.text = "分享获取会员"
        btnBack.setOnClickListener(this)
        shareNowBtn.setOnClickListener(this)
        openVipBtn.setOnClickListener(this)

        shareFriendsRv.layoutManager = GridLayoutManager(this, 1)
        shareFriendsRv.adapter = adapter
        adapter.addData("")
//        adapter.addData("")
//        adapter.addData("")

        SpanUtils.with(shareFriendsTitle)
            .append("分享三位好友注册，获取免费黄金会员\n与积糖")
            .append("52398")
            .setForegroundColor(Color.parseColor("#FFFD4417"))
            .append("位超主动小姐姐产生无限可能")
            .create()
        SpanUtils.with(shareFriendsCount)
            .append("已邀请")
            .append("0")
            .setForegroundColor(Color.parseColor("#FFFD4417"))
            .append("位，再邀请")
            .append("3")
            .setForegroundColor(Color.parseColor("#FFFD4417"))
            .append("位好友注册即可成为会员")
            .create()

//        #FFFD4417
    }


    /**
     * 遍历循环到已经成为会员的
     */
    private fun getMarqueeView(content: VipDescr): View {
        val view = layoutInflater.inflate(R.layout.item_marquee_vip_friends, null, false)
        view.vipFriendsName.text = content.title
        return view
    }


    lateinit var moreActionDialog: MoreActionNewDialog

    /**
     * 展示更多操作对话框
     */
    private fun showShareDialog(url: String) {
        moreActionDialog =
            MoreActionNewDialog(this, url = url, type = MoreActionNewDialog.TYPE_SHARE_VIP_URL)
        moreActionDialog.show()

        moreActionDialog.collect.isVisible = true
        moreActionDialog.report.isVisible = false
        moreActionDialog.delete.isVisible = false
        moreActionDialog.transpondFriend.isVisible = false

        moreActionDialog.collect.text = "复制链接"
        val top = resources.getDrawable(R.drawable.icon_copy_url)
        moreActionDialog.collect.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)

        moreActionDialog.collect.onClick {
            ClipboardUtil.clipboardCopyText(this, url)
            CommonFunction.toast("分享链接已复制")
        }
    }

    override fun onLazyClick(v: View) {
        when (v.id) {
            //立即分享
            R.id.shareNowBtn -> {
                showShareDialog("http://www.baidu.com")
            }
            //直接开通会员
            R.id.openVipBtn -> {
            }
            R.id.btnBack -> {
                finish()
            }
        }
    }

}