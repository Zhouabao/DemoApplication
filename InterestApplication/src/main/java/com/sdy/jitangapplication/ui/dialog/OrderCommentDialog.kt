package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.CommentPicEvent
import com.sdy.jitangapplication.ui.adapter.OrderCommentPicAdapter
import kotlinx.android.synthetic.main.dialog_order_comment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *    author : ZFM
 *    date   : 2019/8/1513:59
 *    desc   : 收到货评价商品
 *    version: 1.0
 */
class OrderCommentDialog(var context1: Context) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_order_comment)
        initWindow()
        initview()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 0.5f
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }

    private val picAdapter by lazy { OrderCommentPicAdapter() }

    fun initview() {
        orderCommentPicRv.layoutManager =
            LinearLayoutManager(context1, RecyclerView.HORIZONTAL, false)
        orderCommentPicRv.adapter = picAdapter
        picAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.addPicComment -> {
                    CommonFunction.onTakePhoto(context1, 9 - (picAdapter.data.size - 1), 100)
                }

                R.id.cancelPic->{
                    picAdapter.remove(position)
                    picAdapter.notifyDataSetChanged()
                }
            }
        }

        picAdapter.addData("")
    }


    override fun show() {
        super.show()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentPicEvent(event: CommentPicEvent) {
        if (!event.imgs.isNullOrEmpty()) {
            picAdapter.addData(event.imgs)
            picAdapter.notifyDataSetChanged()
        }
    }


}