package com.example.demoapplication.ui.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.KeyboardUtils
import com.example.demoapplication.R
import com.example.demoapplication.model.CommentBean
import com.example.demoapplication.presenter.MyCommentPresenter
import com.example.demoapplication.presenter.view.MyCommentView
import com.example.demoapplication.ui.adapter.MultiListCommentAdapter
import com.example.demoapplication.ui.adapter.MyCommentAdapter
import com.example.demoapplication.ui.dialog.CommentActionDialog
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import kotlinx.android.synthetic.main.activity_my_comment.*
import kotlinx.android.synthetic.main.activity_square_comment_detail.*
import kotlinx.android.synthetic.main.activity_square_comment_detail.btnBack
import kotlinx.android.synthetic.main.activity_square_comment_detail.refreshLayout
import kotlinx.android.synthetic.main.dialog_comment_action.*
import org.jetbrains.anko.toast

/**
 * 我的评论
 */
class MyCommentActivity : BaseMvpActivity<MyCommentPresenter>(), MyCommentView, OnRefreshListener, OnLoadMoreListener {
    private val adapter: MyCommentAdapter by lazy { MyCommentAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_comment)
        initView()
    }

    private fun initView() {
        mPresenter = MyCommentPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)

        commentRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        commentRv.adapter = adapter

        btnBack.onClick {
            finish()
        }





        adapter.setOnItemLongClickListener { adapter, view, position ->
            showCommentDialog(position)
            true
        }

        adapter.setOnItemClickListener { _, view, position ->
            showCommentEt.isFocusable = true
            showCommentEt.hint = "『回复\t${adapter.data[position].nickname}：』"
            KeyboardUtils.showSoftInput(showCommentEt)
        }
    }


    var commentActionDialog: CommentActionDialog? = null
    //判断当前是添加评论还是回复评论
    private var reply = false
    private var reply_id = 0
    private fun showCommentDialog(position: Int) {
        if (commentActionDialog == null) {
            //判断该条评论是不是自己发的
            if (adapter.data[position].member_accid!! == UserManager.getAccid())
                commentActionDialog = CommentActionDialog(this, "self")
            else
                commentActionDialog = CommentActionDialog(this, "others")

        }
        commentActionDialog!!.show()

        commentActionDialog!!.copyComment.onClick {
            copyText(position)
            commentActionDialog!!.dismiss()
        }

        commentActionDialog!!.replyComment.onClick {
            reply = true
            reply_id = adapter.data[position].id!!
            showCommentEt.isFocusable = true
            showCommentEt.hint = "『回复\t${adapter.data[position].nickname}：』"
            showCommentEt.postDelayed({ KeyboardUtils.showSoftInput(showCommentEt) }, 100L)

            commentActionDialog!!.dismiss()
        }

        commentActionDialog!!.jubaoComment.onClick {
            //todo 举报
//            mPresenter.commentReport(
//                hashMapOf(
//                    "token" to UserManager.getToken(),
//                    "accid" to UserManager.getAccid(),
//                    "id" to adapter.data[position].id!!
//                )
//                , position
//            )
            commentActionDialog!!.dismiss()

        }

        commentActionDialog!!.deleteComment.onClick {
//            mPresenter.deleteComment(
//                hashMapOf(
//                    "token" to UserManager.getToken(),
//                    "accid" to UserManager.getAccid(),
//                    "id" to adapter.data[position].id!!
//                )
//                , position
//            )
            commentActionDialog!!.dismiss()
        }


        commentActionDialog!!.setOnDismissListener {
            commentActionDialog = null
        }
    }


    private fun copyText(position: Int) {
        //获取剪贴板管理器
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        //创建普通字符串clipData
        val clipData = ClipData.newPlainText("label", "${adapter.data[position].content}")
        //将clipdata内容放到系统剪贴板里
        cm.setPrimaryClip(clipData)
        toast("已复制内容到剪贴板")
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {


    }

    override fun onRefresh(refreshLayout: RefreshLayout) {


    }
}
