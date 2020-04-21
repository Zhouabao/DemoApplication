package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.SPUtils
import com.google.gson.Gson
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.luck.picture.lib.config.PictureConfig
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.CommentPicEvent
import com.sdy.jitangapplication.event.RefreshOrderStateEvent
import com.sdy.jitangapplication.ui.adapter.OrderCommentPicAdapter
import com.sdy.jitangapplication.utils.QNUploadManager
import com.sdy.jitangapplication.utils.UserManager
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
class OrderCommentDialog(var context1: Context, val position: Int, val goods_id: Int) :
    Dialog(context1, R.style.MyDialog) {
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
        orderStar.setOnRatingBarChangeListener { _, rating, fromUser ->
            publishComment.isEnabled = rating > 0
        }

        orderCommentPicRv.layoutManager =
            LinearLayoutManager(context1, RecyclerView.HORIZONTAL, false)
        orderCommentPicRv.adapter = picAdapter
        picAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.addPicComment -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        (!PermissionUtils.isGranted(PermissionConstants.CAMERA) ||
                                !PermissionUtils.isGranted(PermissionConstants.STORAGE))
                    ) {
                        PermissionUtils.permission(PermissionConstants.CAMERA)
                            .callback(object : PermissionUtils.SimpleCallback {
                                override fun onGranted() {
                                    if (!PermissionUtils.isGranted(PermissionConstants.STORAGE))
                                        PermissionUtils.permission(PermissionConstants.STORAGE)
                                            .callback(object : PermissionUtils.SimpleCallback {
                                                override fun onGranted() {
                                                    CommonFunction.onTakePhoto(
                                                        context1,
                                                        9 - (picAdapter.data.size - 1),
                                                        PictureConfig.CHOOSE_REQUEST
                                                    )
                                                }

                                                override fun onDenied() {
                                                    CommonFunction.toast("文件存储权限被拒,请允许权限后再上传照片.")
                                                }

                                            })
                                            .request()
                                }

                                override fun onDenied() {
                                    CommonFunction.toast("相机权限被拒,请允许权限后再上传照片.")
                                }
                            })
                            .request()
                    } else {
                        CommonFunction.onTakePhoto(
                            context1,
                            9 - (picAdapter.data.size - 1),
                            PictureConfig.CHOOSE_REQUEST
                        )
                    }
                }
                R.id.cancelPic -> {
                    picAdapter.remove(position)
                    picAdapter.notifyDataSetChanged()
                }
            }
        }
        picAdapter.addData("")

        publishComment.onClick {
            publishComment.isEnabled = false
            if (picAdapter.data.size > 1) {
                if (picAdapter.data[photosNum] == "") {
                    photosNum++
                }
                val imageName =
                    "${Constants.FILE_NAME_INDEX}${Constants.CANDYPRODUCT}${UserManager.getAccid()}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                        16
                    )}"
                uploadProfile(picAdapter.data[photosNum], imageName)
            } else {
                goodsAddcomments()
            }
        }
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

    fun goodsAddcomments() {
        val params = hashMapOf<String, Any>()
        params["stars"] = orderStar.numStars
        params["photo"] = Gson().toJson(photosNameArray)
        params["comments"] = orderComment.text.trim().toString()
        params["order_id"] = goods_id
        RetrofitFactory.instance.create(Api::class.java)
            .goodsAddcomments(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    publishComment.isEnabled = true
                    super.onNext(t)
                    if (t.code == 200) {
                        CommonFunction.toast(t.msg)
                        EventBus.getDefault().post(RefreshOrderStateEvent(position, 4))
                        dismiss()
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    publishComment.isEnabled = true
                }
            })


    }


    /**
     * 上传照片
     * imageName 文件名格式： ppns/文件类型名/用户ID/当前时间戳/16位随机字符串
     */
    private val photosNameArray by lazy { mutableListOf<String>() }
    private var photosNum = 0 //图片上传的index

    fun uploadProfile(filePath: String, imageName: String) {
        QNUploadManager.getInstance().put(
            filePath, imageName, SPUtils.getInstance(Constants.SPNAME).getString("qntoken"),
            { key, info, response ->
                if (info != null) {
                    if (info.isOK) {
                        photosNameArray.add(imageName)
                        if ((photosNum + 1) == picAdapter.data.size) {
                            goodsAddcomments()
                        } else {
                            photosNum++
                            if (picAdapter.data[photosNum] != "") {
                                val imageName =
                                    "${Constants.FILE_NAME_INDEX}${Constants.REPORTUSER}${UserManager.getAccid()}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                                        16
                                    )}"
                                uploadProfile(
                                    picAdapter.data[photosNum],
                                    imageName
                                )
                            }
                        }
                    }
                } else {
                    publishComment.isEnabled = true
                }
            }, null
        )
    }

}