package com.kotlin.base.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.bigkoo.alertview.AlertView
import com.bigkoo.alertview.OnItemClickListener
import com.jph.takephoto.app.TakePhoto
import com.jph.takephoto.app.TakePhotoImpl
import com.jph.takephoto.compress.CompressConfig
import com.jph.takephoto.model.CropOptions
import com.jph.takephoto.model.TResult
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.presenter.view.BaseView
import com.kotlin.base.utils.DateUtils
import org.jetbrains.anko.toast
import java.io.File


/*
    存在选择图片的Activity基础封装
 */
abstract open class BaseTakePhotoActivity<T : BasePresenter<*>> : BaseActivity(), BaseView,
    TakePhoto.TakeResultListener {

    lateinit var mTakePhoto: TakePhoto

    private lateinit var mTempFile: File

    lateinit var mPresenter: T


    //TODO(所有存在加载框的地方都要重新编写加载)
    //private lateinit var mLoadingDialog: ProgressLoading

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mTakePhoto = TakePhotoImpl(this, this)
        mTakePhoto.onCreate(savedInstanceState)

        //mLoadingDialog = ProgressLoading.create(this)
    }


    /*
        显示加载框，默认实现
     */
    override fun showLoading() {
        //mLoadingDialog.showLoading()
    }

    /*
        隐藏加载框，默认实现
     */
    override fun hideLoading() {
        // mLoadingDialog.hideLoading()
    }

    /*
        错误信息提示，默认实现
     */
    override fun onError(text: String) {
        toast(text)
    }

    /*
        弹出选择框，默认实现
        可根据实际情况，自行修改
     */
    protected fun showAlertView() {
        AlertView("选择图片", "", "取消", null, arrayOf("拍照", "相册"), this,
            AlertView.Style.ActionSheet, OnItemClickListener { o, position ->
                mTakePhoto.onEnableCompress(CompressConfig.ofDefaultConfig(), false)
                when (position) {
                    0 -> {
                        createTempFile()
                        mTakePhoto.onPickFromCaptureWithCrop(Uri.fromFile(mTempFile), getCropOptions())
                    }
                    1 -> {
                        createTempFile()
                        mTakePhoto.onPickFromGalleryWithCrop(Uri.fromFile(mTempFile), getCropOptions())
                    }
                }
            }

        ).show()

    }

    /*
        获取图片，成功回调
     */
    override fun takeSuccess(result: TResult?) {
        Log.d("TakePhoto", result?.image?.originalPath)
        Log.d("TakePhoto", result?.image?.compressPath)
    }

    /*
        获取图片，取消回调
     */
    override fun takeCancel() {
    }

    /*
        获取图片，失败回调
     */
    override fun takeFail(result: TResult?, msg: String?) {
        Log.e("takePhoto", msg)
    }

    /*
        TakePhoto默认实现
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mTakePhoto.onActivityResult(requestCode, resultCode, data)
    }


    /*
        新建临时文件
     */
    private fun createTempFile() {
        val tempFileName = "${DateUtils.curTime}.png"
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            this.mTempFile = File(Environment.getExternalStorageDirectory(), tempFileName)
            return
        }

        this.mTempFile = File(filesDir, tempFileName)
    }


    fun getCropOptions(): CropOptions {
        val builder = CropOptions.Builder()
        builder.setAspectX(1).setAspectY(1)
        builder.setWithOwnCrop(true)
        return builder.create()

    }
}
