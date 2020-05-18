package com.sdy.jitangapplication.ui.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Video
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.MediaController
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.services.core.PoiItem
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.*
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.google.gson.Gson
import com.kotlin.base.ext.onClick
import com.kotlin.base.ext.setVisible
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.baselibrary.emoj.EmojiSource
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.CustomClickListener
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.AnnounceEvent
import com.sdy.jitangapplication.event.UploadEvent
import com.sdy.jitangapplication.model.*
import com.sdy.jitangapplication.player.MediaPlayerHelper
import com.sdy.jitangapplication.player.MediaRecorderHelper
import com.sdy.jitangapplication.player.MediaRecorderHelper.*
import com.sdy.jitangapplication.player.UpdateVoiceTimeThread
import com.sdy.jitangapplication.presenter.PublishPresenter
import com.sdy.jitangapplication.presenter.view.PublishView
import com.sdy.jitangapplication.ui.adapter.ChoosePhotosAdapter
import com.sdy.jitangapplication.ui.adapter.ChooseTitleAdapter
import com.sdy.jitangapplication.ui.adapter.EmojAdapter
import com.sdy.jitangapplication.ui.adapter.PublishWayAdapter
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.utils.AMapManager
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UriUtils.getAllVideoInfos
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_publish.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import kotlinx.android.synthetic.main.layout_record_audio.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivityForResult
import top.zibin.luban.OnCompressListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * 发布内容页面
 *
 */
class PublishActivity : BaseMvpActivity<PublishPresenter>(), PublishView,
    View.OnClickListener, TextWatcher, LoaderManager.LoaderCallbacks<Cursor> {

    companion object {
        const val AUTHORITY = "com.sdy.jitangapplication.provider" //FileProvider的签名 7.0以上要用
        const val REQUEST_CODE_CAPTURE_RAW = 6 //拍照
        const val REQUEST_CODE_VIDEO = 10 //视频选择
        const val REQUEST_CODE_LABEL = 20 //兴趣选择
        const val REQUEST_CODE_MAP = 30 //地图选择
        const val REQUEST_CODE_TITILE = 40 //发布标题

        val IMAGE_PROJECTION = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATE_MODIFIED
        )

        val VIDEO_PROJECTION = arrayOf(
            Video.Thumbnails._ID,
            Video.Thumbnails.DATA,
            Video.Media.DURATION,
            Video.Media.SIZE,
            Video.Media.DISPLAY_NAME,
            Video.Media.DATE_ADDED
        )
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        if (id == 0) {
            return CursorLoader(
                this,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                IMAGE_PROJECTION,
                null,
                null,
                IMAGE_PROJECTION[2] + " DESC"
            )
        } else {
            return CursorLoader(
                this,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                IMAGE_PROJECTION,
                null,
                null,
                IMAGE_PROJECTION[2] + " DESC"
            )
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if (data != null && allPhotoAdapter.data.isNullOrEmpty()) {
            allPhotoAdapter.data.add(MediaBean(-1, MediaBean.TYPE.IMAGE))
            val count = data.count
            data.moveToFirst()
            if (count > 0) {
                do {
                    val imagePath = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0])) ?: ""
                    val size = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1])) / 1024L
                    val id = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]))
                    val displayName = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[3])) ?: ""
                    val width = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]))
                    val height = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[5]))
                    allPhotoAdapter.addData(
                        MediaBean(
                            id,
                            MediaBean.TYPE.IMAGE,
                            imagePath,
                            displayName,
                            size = size,
                            width = width,
                            height = height
                        )
                    )
                } while (data.moveToNext())
            }

        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        LoaderManager.getInstance(this).destroyLoader(0)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish)
        //获取所有的照片信息
        initView()

        //获取权限后加载相册信息
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //申请权限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        } else {
            LoaderManager.getInstance(this@PublishActivity).initLoader(0, null, this@PublishActivity)
            //获取所有的视频封面
            allVideoThumbAdapter.setNewData(getAllVideoInfos(this@PublishActivity))
            allVideoThumbAdapter.data.add(0, MediaBean(-1, MediaBean.TYPE.VIDEO))
        }

        initData()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty()) {
                for (i in 0 until grantResults.size) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        LoaderManager.getInstance(this@PublishActivity).initLoader(0, null, this@PublishActivity)
                        //获取所有的视频封面
                        allVideoThumbAdapter.setNewData(getAllVideoInfos(this@PublishActivity))
                        allVideoThumbAdapter.data.add(0, MediaBean(-1, MediaBean.TYPE.VIDEO))
                        return
                    }
                }
                CommonFunction.toast("请重新进入页面,并开启多媒体权限.")
            }
        }
    }

    private fun initData() {
        UserManager.saveShowGuidePublish(true)
        //从其他地方进入发布,自主选择的兴趣
        if (intent.getIntExtra("tag_id", -1) != -1) {
            checkTags.clear()
            checkTags.add(SquareLabelBean(id = intent.getIntExtra("tag_id", -1)))
            rightBtn1.text = "发布"
        } else {
            rightBtn1.text = "下一步"

        }

        //从广场标题引导进入发布，默认选中兴趣
        if (!intent.getStringExtra("title").isNullOrEmpty()) {
            choosedTitleAdapter.addData(LabelQualityBean(intent.getStringExtra("title"), isfuse = true))
            choosedTitleAdapter.notifyDataSetChanged()
            titleState.setImageResource(R.drawable.icon_topic_blue)
            chooseTitle.isVisible = false
            chooseTitleRv.isVisible = true
        }
        locationCity.text = UserManager.getCity()
        contentLength.text = SpanUtils.with(contentLength)
            .append(publishContent.length().toString())
            .setFontSize(14, true)
            .setForegroundColor(resources.getColor(R.color.colorOrange))
            .setBold()
            .append("/200")
            .setFontSize(10, true)
            .create()

        //进入页面判断是否启用草稿箱，不管启用不启用，最后都删除内容，只保留一次。
        if (SPUtils.getInstance(Constants.SPNAME).getString("draft", "").isNotEmpty()) {
            CommonAlertDialog.Builder(this)
                .setTitle("草稿箱")
                .setContent("是否启用草稿箱")
                .setConfirmText("是")
                .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                    override fun onClick(dialog: Dialog) {
                        publishContent.setText(SPUtils.getInstance(Constants.SPNAME).getString("draft", ""))
                        publishContent.setSelection(publishContent.length())
                        SPUtils.getInstance(Constants.SPNAME).remove("draft", true)
                        dialog.cancel()
                    }
                })
                .setCancelText("否")
                .setOnCancelListener(object : CommonAlertDialog.OnCancelListener {
                    override fun onClick(dialog: Dialog) {
                        SPUtils.getInstance(Constants.SPNAME).remove("draft", true)
                        dialog.cancel()
                    }
                })
                .create()
                .show()

        }
    }

    val publishAdapter by lazy { PublishWayAdapter() }
    val choosedTitleAdapter by lazy { ChooseTitleAdapter() }
    private fun initView() {
        UserManager.cancelUpload = false

        btnBack.onClick {
            onBackPressed()
        }

        //主动弹起键盘
//        publishContent.postDelayed(
//            { KeyboardUtils.showSoftInput(publishContent) }, 100L
//        )

        hotT1.text = "发布内容"
        rightBtn1.isVisible = true
        rightBtn1.isEnabled = true
        rightBtn1.setBackgroundResource(R.drawable.selector_confirm_btn_15dp)
        rightBtn1.setTextColor(resources.getColor(R.color.complete_btn_color_selector))


        mPresenter = PublishPresenter()
        mPresenter.mView = this
        mPresenter.context = applicationContext

        tabPublishWay.layoutManager = GridLayoutManager(this, 4)
        tabPublishWay.adapter = publishAdapter
        publishAdapter.addData(
            PublishWayBean(
                R.drawable.icon_publish_pic_normal,
                R.drawable.icon_publish_pic_checked,
                true
            )
        )
        publishAdapter.addData(
            PublishWayBean(
                R.drawable.icon_publish_audio_normal,
                R.drawable.icon_publish_audio_checked,
                false
            )
        )
        publishAdapter.addData(
            PublishWayBean(
                R.drawable.icon_publish_video_normal,
                R.drawable.icon_publish_video_checked,
                false
            )
        )
        publishAdapter.addData(
            PublishWayBean(
                R.drawable.icon_publish_emoj_normal,
                R.drawable.icon_publish_emoj_checked,
                false
            )
        )
        publishAdapter.setOnItemClickListener { _, view, position ->

            when (position) {
                0 -> {//图片
                    if (pickedPhotos.size > 0 && pickedPhotos[0].fileType == MediaBean.TYPE.VIDEO || !mMediaRecorderHelper.currentFilePath.isNullOrEmpty()) {
                        CommonFunction.toast("不支持图片和其他媒体一起上传哦")
                        return@setOnItemClickListener
                    }
                    allPhotosRv.isVisible = true
                    videosRv.isVisible = false
                    audioLl.isVisible = false
                    emojRv.isVisible = false
                }
                1 -> {//语音
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionUtils.isGranted(Manifest.permission.RECORD_AUDIO)) {
                        PermissionUtils.permission(PermissionConstants.MICROPHONE)
                            .callback(object : PermissionUtils.SimpleCallback {
                                override fun onGranted() {
                                    if (pickedPhotos.isNotEmpty() || videoPath.isNotEmpty()) {
                                        CommonFunction.toast("不支持语音和其他媒体一起上传哦")
                                        return
                                    }
                                    allPhotosRv.visibility = View.GONE
                                    videosRv.visibility = View.GONE
                                    audioLl.visibility = View.VISIBLE
                                }

                                override fun onDenied() {
                                    CommonFunction.toast("录音权限被拒,请开启后再录音.")
                                }
                            }).request()
                    } else {
                        if (pickedPhotos.isNotEmpty() || videoPath.isNotEmpty()) {
                            CommonFunction.toast("不支持语音和其他媒体一起上传哦")
                            return@setOnItemClickListener
                        }
                        allPhotosRv.isVisible = false
                        videosRv.isVisible = false
                        audioLl.isVisible = true
                        emojRv.isVisible = false
                    }
                }
                2 -> {//视频
                    if (pickedPhotos.size > 0 && pickedPhotos[0].fileType == MediaBean.TYPE.IMAGE || !mMediaRecorderHelper.currentFilePath.isNullOrEmpty()) {
                        CommonFunction.toast("不支持视频和其他媒体一起上传哦")
                        return@setOnItemClickListener
                    }
                    allPhotosRv.isVisible = false
                    videosRv.isVisible = true
                    audioLl.isVisible = false
                    emojRv.isVisible = false

                }
                3 -> {//表情
                    allPhotosRv.isVisible = false
                    videosRv.isVisible = false
                    audioLl.isVisible = false
                    emojRv.isVisible = true
                }
            }
            for (data in publishAdapter.data) {
                data.checked = (data == publishAdapter.data[position])
            }
            publishAdapter.notifyDataSetChanged()
        }
        publishContent.addTextChangedListener(this)
        locationCity.setOnClickListener(this)
        chooseTitleBtn.setOnClickListener(this)
        chooseTitle.setOnClickListener(this)
        chooseTitleRv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        chooseTitleRv.adapter = choosedTitleAdapter
        rightBtn1.onClick(object : CustomClickListener() {
            override fun onSingleClick(view: View) {
                if (pickedPhotos.size == 0 && mMediaRecorderHelper.currentFilePath.isNullOrEmpty() && publishContent.text.isNullOrEmpty()) {
                    CommonFunction.toast("文本内容和媒体内容至少要选择一种发布哦~")
                    return
                }

                if (!mMediaRecorderHelper.currentFilePath.isNullOrEmpty() && currentActionState != ACTION_DONE && !isTopPreview) {
                    CommonFunction.toast("请录制完语音再发布")
                    return
                }


                if (emojRv.visibility == View.VISIBLE) {
                    emojRv.visibility = View.GONE
                }

                if (intent.getIntExtra("tag_id", -1) != -1) {
                    startToUploadAndPublsih()
                } else
                    startActivityForResult<ChooseLabelActivity>(REQUEST_CODE_LABEL)

            }

        })

        initPhotos()
        initVideos()
        initAudioLl()
        initPickedRv()
        initEmojRv()
    }

    /**************设置表情包******************/
    private val emojAdapter by lazy { EmojAdapter() }

    private fun initEmojRv() {
        emojRv.layoutManager = GridLayoutManager(this, 8, RecyclerView.VERTICAL, false)
        emojRv.adapter = emojAdapter

        emojAdapter.addData(EmojiSource.people.toMutableList())
        emojAdapter.addData(EmojiSource.objects.toMutableList())
        emojAdapter.addData(EmojiSource.nature.toMutableList())
        emojAdapter.addData(EmojiSource.places.toMutableList())
        emojAdapter.addData(EmojiSource.symbol.toMutableList())
        emojAdapter.setOnItemClickListener { _, view, position ->
            publishContent.append(emojAdapter.data[position])
            publishContent.setSelection(publishContent.length())
        }

    }


    /***************设置选中的兴趣******************/
    private var checkTags = mutableListOf<SquareLabelBean>()


    /*****************设置相册和视频信息********************/
    private var imageFile: File? = null     //拍照后保存的照片
    private var imgUri: Uri? = null         //拍照后保存的照片的uri
    private var pickedPhotos: MutableList<MediaBean> = mutableListOf()
    private var videoPath: String = ""
    private var audioPath: String = ""

    private val allPhotoAdapter by lazy { ChoosePhotosAdapter(0, pickedPhotos) } //全部照片
    private val pickedPhotoAdapter by lazy { ChoosePhotosAdapter(1) }//选中的封面
    private val allVideoThumbAdapter by lazy { ChoosePhotosAdapter(2) }//全部视频封面
    private var videoCheckIndex = -1

    //开启拖拽
    private var fromPos = -1
    private var toPos = -1
    private fun initPickedRv() {
        pickedPhotosRv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        //垂直分割线
        pickedPhotosRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL_LIST,
                SizeUtils.dp2px(4F),
                resources.getColor(R.color.colorWhite)
            )
        )

        val itemDragAndSwpieCallBack = ItemDragAndSwipeCallback(pickedPhotoAdapter)
        val itemTouchHelper = ItemTouchHelper(itemDragAndSwpieCallBack)
        itemTouchHelper.attachToRecyclerView(pickedPhotosRv)

        pickedPhotoAdapter.enableDragItem(itemTouchHelper, R.id.choosePhoto, true)
        pickedPhotoAdapter.setOnItemDragListener(object : OnItemDragListener {
            override fun onItemDragMoving(
                p0: RecyclerView.ViewHolder?,
                p1: Int,
                p2: RecyclerView.ViewHolder?,
                p3: Int
            ) {
            }

            override fun onItemDragStart(p0: RecyclerView.ViewHolder?, position: Int) {
                fromPos = position
            }

            override fun onItemDragEnd(p0: RecyclerView.ViewHolder?, position: Int) {
                toPos = position
                if (fromPos != toPos && fromPos != -1 && toPos != -1) {
                    val data = pickedPhotos[fromPos]
                    pickedPhotos.removeAt(fromPos)
                    pickedPhotos.add(toPos, data)
                    pickedPhotoAdapter.notifyDataSetChanged()
                }
                fromPos = -1
                toPos = -1
            }

        })
        pickedPhotosRv.adapter = pickedPhotoAdapter
        pickedPhotoAdapter.setNewData(pickedPhotos)
        pickedPhotoAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.choosePhoto -> {
                    if (pickedPhotoAdapter.data[position].fileType == MediaBean.TYPE.IMAGE)
                        showBigImagePreview(pickedPhotoAdapter.data[position])
                    else
                        showVideoPreview(pickedPhotoAdapter.data[position])
                }
                R.id.choosePhotoDel -> {
                    val mediaBean = pickedPhotoAdapter.data[position]
                    val type = mediaBean.fileType

                    pickedPhotos.remove(mediaBean)
                    pickedPhotoAdapter.notifyDataSetChanged()
//                    pickedPhotoAdapter.notifyItemRemoved(position)
                    if (type == MediaBean.TYPE.IMAGE) {
                        for (bean in allPhotoAdapter.data) {
                            if (bean.id == mediaBean.id) {
                                bean.ischecked = false
                                break
                            }
                        }
                        allPhotoAdapter.notifyDataSetChanged()
                        checkCompleteBtnEnable()
                    } else {
                        allVideoThumbAdapter.data[videoCheckIndex].ischecked = false
                        allVideoThumbAdapter.notifyItemChanged(videoCheckIndex)
                    }

                }
            }
        }
    }

    private fun showBigImagePreview(mediaBean: MediaBean) {
        GlideUtil.loadImg(this, mediaBean.filePath, imageBigPreview)
        imageBigPreview.visibility = View.VISIBLE
        imageBigPreview.onClick {
            imageBigPreview.visibility = View.GONE
        }
    }

    /**
     * 读取相册中所有图片 时间顺序
     * 照片最多选择九张
     */
    private fun initPhotos() {
        allPhotosRv.layoutManager = GridLayoutManager(this, 4, RecyclerView.VERTICAL, false)
        //垂直+水平分割线
        allPhotosRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.BOTH_SET,
                SizeUtils.dp2px(4F),
                resources.getColor(R.color.colorWhite)
            )
        )
        allPhotosRv.adapter = allPhotoAdapter
        allPhotoAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                //点击查看大图
                R.id.choosePhoto -> {
                    showBigImagePreview(allPhotoAdapter.data[position])
                }
                //点击选中
                R.id.choosePhotoDel -> {
                    val data = (allPhotoAdapter.data[position])
                    if (!data.ischecked && pickedPhotos.size == 9) {
                        CommonFunction.toast("最多只能选9张图片")
                        return@setOnItemChildClickListener
                    }
                    allPhotoAdapter.data[position].ischecked = !(allPhotoAdapter.data[position].ischecked)
                    if (!allPhotoAdapter.data[position].ischecked) {
                        for (photo in pickedPhotos) {
                            if (photo.id == allPhotoAdapter.data[position].id) {
                                pickedPhotos.remove(photo)
                                break
                            }
                        }
                    } else {
                        //相册的选择与取消选择
                        pickedPhotos.add(allPhotoAdapter.data[position])
                    }
                    pickedPhotosRv.visibility = if (pickedPhotos.size > 0) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
                    pickedPhotoAdapter.notifyDataSetChanged()
                    allPhotoAdapter.notifyDataSetChanged()
                    checkCompleteBtnEnable()
                }
                R.id.chooseCamera -> {
                    //相册的选择与取消选择
                    if (pickedPhotos.size == 9) {
                        CommonFunction.toast("最多只能选9张图片")
                        return@setOnItemChildClickListener
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionUtils.isGranted(PermissionConstants.CAMERA))
                        PermissionUtils.permission(PermissionConstants.CAMERA)
                            .callback(object : PermissionUtils.SimpleCallback {
                                override fun onGranted() {
                                    gotoCaptureRaw(true)
                                }

                                override fun onDenied() {
                                    CommonFunction.toast("相机权限被拒,请允许权限后再进行拍照.")
                                }
                            })
                            .request()
                    else
                        gotoCaptureRaw(true)
                }
            }
        }

    }

    /**
     * 读取相册中所有视频  时间顺序
     * 视频的时间是3~120s以内
     */
    private fun initVideos() {
        videosRv.layoutManager = GridLayoutManager(this, 4, RecyclerView.VERTICAL, false)
        //垂直+水平分割线
        videosRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.BOTH_SET,
                SizeUtils.dp2px(4F),
                resources.getColor(R.color.colorWhite)
            )
        )
        videosRv.adapter = allVideoThumbAdapter

        allVideoThumbAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.choosePhoto -> {
                    showVideoPreview(allVideoThumbAdapter.data[position])
                }
                R.id.chooseCamera -> {
                    if (pickedPhotos.size == 1) {
                        CommonFunction.toast("最多只能选择1个视频")
                        return@setOnItemChildClickListener
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionUtils.isGranted(PermissionConstants.CAMERA))
                        PermissionUtils.permission(PermissionConstants.CAMERA)
                            .callback(object : PermissionUtils.SimpleCallback {
                                override fun onGranted() {
                                    go2TakeVideo()
                                }

                                override fun onDenied() {
                                    CommonFunction.toast("相机权限被拒,请允许权限后再进行视频录制.")
                                }
                            })
                            .request()
                    else
                        go2TakeVideo()
                }
                R.id.choosePhotoDel -> {
                    //视频的选择与取消选择
                    if (!allVideoThumbAdapter.data[position].ischecked) {
                        if (pickedPhotos.size == 1) {
                            CommonFunction.toast("最多只能选择1个视频")
                            return@setOnItemChildClickListener
                        }
                        if (allVideoThumbAdapter.data[position].duration / 1000 < 3) {
                            CommonFunction.toast("视频时长过短")
                            return@setOnItemChildClickListener
                        } else if (allVideoThumbAdapter.data[position].duration / 1000 > 120) {
                            CommonFunction.toast("视频时长过长")
                            return@setOnItemChildClickListener
                        }
                        allVideoThumbAdapter.data[position].ischecked =
                            !(allVideoThumbAdapter.data[position].ischecked)
                        pickedPhotos.add(allVideoThumbAdapter.data[position])
                        videoCheckIndex = position
                    } else {
                        allVideoThumbAdapter.data[position].ischecked =
                            !(allVideoThumbAdapter.data[position].ischecked)
                        for (photo in pickedPhotos) {
                            if (photo.id == allVideoThumbAdapter.data[position].id) {
                                pickedPhotos.remove(photo)
                                break
                            }
                        }
                    }
                    pickedPhotosRv.visibility = if (pickedPhotos.size > 0) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
                    pickedPhotoAdapter.notifyDataSetChanged()
                    allVideoThumbAdapter.notifyDataSetChanged()
//                    checkCompleteBtnEnable()
                }
            }
        }
    }

    /**
     * 视频预览
     */
    private fun showVideoPreview(mediaBean: MediaBean) {
        previewRl.visibility = View.VISIBLE
        videoPreviewStart.visibility = View.VISIBLE

        videoPreview.setMediaController(MediaController(this))
        videoPreview.setVideoURI(Uri.parse(mediaBean.filePath))
        videoPreview.setOnCompletionListener {
            videoPreviewStart.visibility = View.VISIBLE
            videoPreview.stopPlayback()
        }
        videoPreview.onClick {
            previewRl.visibility = View.GONE
            videoPreview.stopPlayback()
        }
        previewRl.onClick {
            previewRl.visibility = View.GONE
            videoPreview.stopPlayback()
        }
        videoPreviewStart.onClick {
            videoPreviewStart.visibility = View.GONE
            videoPreview.start()

        }
    }


    /**
     * @param isCapture 是否是拍照 true为拍照 false为视频
     */
    private fun gotoCaptureRaw(isCapture: Boolean) {
        imageFile = createImageFile(isCamera = isCapture)
        imageFile?.let {
            var intent: Intent
            if (isCapture) {
                intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG)
            } else {
                intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
                // 录制视频最大时长2min
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 120)
            }



            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  //如果是7.0以上，使用FileProvider，否则会报错
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                imgUri = FileProvider.getUriForFile(this, AUTHORITY, it)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri) //设置拍照后图片保存的位置
            } else {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(it)) //设置拍照后图片保存的位置
            }
            intent.resolveActivity(packageManager)?.let {
                if (isCapture)
                    startActivityForResult(intent, REQUEST_CODE_CAPTURE_RAW) //调起系统相机
                else
                    startActivityForResult(intent, REQUEST_CODE_VIDEO) //调起系统相机录制

            }
        }
    }

    //拍摄视频并返回
    private fun go2TakeVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
        // 录制视频最大时长2min
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 120)
        startActivityForResult(intent, REQUEST_CODE_VIDEO)
    }


    /**
     * 创建文件夹来保存照片
     */
    private fun createImageFile(isCrop: Boolean = false, isCamera: Boolean = true): File? {
        return try {
            var rootFile: File? = null

            if (!isCamera) {
                rootFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    "jitangapplicaiton/video"
                )
            } else {
                rootFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    "jitangapplicaiton/camera"
                )
            }
            if (!rootFile.exists())
                rootFile.mkdirs()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val fileName = if (isCamera) {
                if (isCrop) "IMG_${timeStamp}_CROP.jpg" else "IMG_$timeStamp.jpg"
            } else {
                "VID_$timeStamp.mp4"
            }
            File(rootFile.absolutePath + File.separator + fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    /**************** * 初始化录音控件 录音时间在5S~3M之间********************/
    private var mIsRecorder = false
    private var mIsPreview = false
    //是否显示顶部预览
    private var isTopPreview = false
    private var countTimeThread: CountDownTimer? = null
    private var mPreviewTimeThread: UpdateVoiceTimeThread? = null
    private lateinit var mMediaRecorderHelper: MediaRecorderHelper
    private var totalSecond = 0
    private var currentActionState = ACTION_NORMAL
    //判断是否是第一次点击上部分预览界面的播放按钮
    private var click = false

    private fun initAudioLl() {
        startRecordBtn.setOnClickListener(this)
        deleteRecord.setOnClickListener(this)
        finishRecord.setOnClickListener(this)
        //上面预览状态变更
        recordDelete.setOnClickListener(this)
        audioPlayBtn.setOnClickListener(this)

        deleteRecord.setVisible(false)
        finishRecord.setVisible(false)
        mMediaRecorderHelper = MediaRecorderHelper(this)

        //开启录音计时线程
        countTimeThread = object : CountDownTimer(3 * 60 * 1000, 1000) {
            override fun onFinish() {

                switchActionState()
            }

            override fun onTick(millisUntilFinished: Long) {
                totalSecond++
                if (!mIsRecorder) {
                    countTimeThread?.cancel()
                }

                if (totalSecond == 170) {
                    CommonFunction.toast("还可以录制10秒钟哦，抓紧时间")
                }
                recordTime.text = UriUtils.getShowTime(totalSecond)
                recordTime.setTextColor(resources.getColor(R.color.colorOrange))
                recordProgress.update(totalSecond, 100)


//                }

            }
        }

    }

    /**
     * 切换录音ACTION状态
     */
    private fun switchActionState() {
        mIsRecorder = false
        if (currentActionState == ACTION_NORMAL) {
            currentActionState = ACTION_RECORDING
            startRecordBtn.setImageResource(R.drawable.icon_record_stop)
            //开始录音
            mMediaRecorderHelper.startRecord()
            mIsRecorder = true
            recordTv.text = "正在录音"
            countTimeThread?.start()

        } else if (currentActionState == ACTION_RECORDING) {//录制中
            currentActionState = ACTION_COMMPLETE
            startRecordBtn.setImageResource(R.drawable.icon_record_start)
            //停止录音
            recordTv.text = "点击试听"
            mMediaRecorderHelper.stopAndRelease()
            deleteRecord.visibility = View.VISIBLE
            finishRecord.visibility = View.VISIBLE
        } else if (currentActionState == ACTION_COMMPLETE) {//录制完成
            currentActionState = ACTION_PLAYING
            //开启预览倒计时
            if (isTopPreview) {
                mPreviewTimeThread = UpdateVoiceTimeThread.getInstance(UriUtils.getShowTime(totalSecond), audioTime)
                audioPlayBtn.setImageResource(R.drawable.icon_pause_audio)
                voicePlayView.playAnimation()
            } else {
                //预览播放录音
                recordTv.text = "播放中.."
                recordProgress.update(0, 100)
                mPreviewTimeThread = UpdateVoiceTimeThread.getInstance(UriUtils.getShowTime(totalSecond), recordTime)
                startRecordBtn.setImageResource(R.drawable.icon_record_pause)
            }
            mPreviewTimeThread?.start()
            MediaPlayerHelper.playSound(mMediaRecorderHelper.currentFilePath) {
                //当播放完了之后切换到录制完成的状态
                mPreviewTimeThread?.stop()
//                recordTime.setTextColor(resources.getColor(R.color.colorBlack22))
                recordProgress.update(0, 100)
                if (isTopPreview) {
                    currentActionState = ACTION_COMMPLETE
                    audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                    voicePlayView.cancelAnimation()
                } else {
                    currentActionState = ACTION_COMMPLETE
                    recordTime.text = UriUtils.getShowTime(totalSecond)
                    recordTv.text = "点击试听"
                    startRecordBtn.setImageResource(R.drawable.icon_record_start)
                }
            }
        } else if (currentActionState == ACTION_PLAYING) {//播放中
            currentActionState = ACTION_PAUSE
            mPreviewTimeThread?.pause()
            if (isTopPreview) {
                voicePlayView.cancelAnimation()
                audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
            } else {
                recordTv.text = "暂停"
                startRecordBtn.setImageResource(R.drawable.icon_record_start)
            }
            //暂停播放
            MediaPlayerHelper.pause()
        } else if (currentActionState == ACTION_PAUSE) {//暂停
            currentActionState = ACTION_PLAYING
            //开启预览计时线程
            mPreviewTimeThread?.start()
            if (isTopPreview) {
                audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                voicePlayView.playAnimation()
            } else {
                recordTv.text = "播放中.."
                startRecordBtn.setImageResource(R.drawable.icon_record_pause)
            }
            //继续播放
            MediaPlayerHelper.resume()
        } else if (currentActionState == ACTION_DONE) {
            mMediaRecorderHelper.cancel()
            changeToNormalState()


        }
    }


    //恢复成未录制状态
    private fun changeToNormalState() {
        click = false
        audioRecordLl.visibility = View.GONE
        recordTime.visibility = View.VISIBLE
        recordProgress.visibility = View.VISIBLE
        voicePlayView.cancelAnimation()
        mIsPreview = false
        isTopPreview = false
        mIsRecorder = false
        MediaPlayerHelper.realese()
        currentActionState = ACTION_NORMAL
        startRecordBtn.setImageResource(R.drawable.icon_record_normal)
        audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
        totalSecond = 0
        mPreviewTimeThread?.stop()
        recordTv.text = "点击录音"
        recordTime.text = "00:00"
        recordTime.setTextColor(resources.getColor(R.color.colorBlack22))
        recordProgress.update(0, 100)
        deleteRecord.visibility = View.GONE
        finishRecord.visibility = View.GONE
    }


    /************编辑内容监听**************/
    override fun afterTextChanged(p0: Editable) {
        if (p0.length > 200) {
            publishContent.setText(publishContent.text.subSequence(0, 200))
            publishContent.setSelection(publishContent.text.length)
            CommonFunction.toast("超出字数限制")
            KeyboardUtils.hideSoftInput(publishContent)
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        contentLength.text = SpanUtils.with(contentLength)
            .append(publishContent.length().toString())
            .setFontSize(14, true)
            .setForegroundColor(resources.getColor(R.color.colorOrange))
            .setBold()
            .append("/200")
            .setFontSize(10, true)
            .create()
    }


    /**
     * 检查发布按钮是否可用
     */
    private fun checkCompleteBtnEnable() {
//        publishBtn.isEnabled = ((publishContent.text.isNotEmpty() || publishContent.text.length <= 200 || pickedPhotos.size > 0 || !mMediaRecorderHelper.currentFilePath.isNullOrEmpty()) && checkTags.size > 0)
    }

    override fun onResume() {
        super.onResume()

        AMapManager.initLocation(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        supportLoaderManager.destroyLoader(0)
        if (videoPreview.isPlaying) videoPreview.stopPlayback()
        countTimeThread?.cancel()
        mPreviewTimeThread?.stop()
        if (currentActionState == ACTION_RECORDING) {
            mMediaRecorderHelper.cancel()
            changeToNormalState()
        }
        if (KeyboardUtils.isSoftInputVisible(this)) {
            KeyboardUtils.hideSoftInput(publishContent)
        }
    }

    override fun onPause() {
        super.onPause()
        if (videoPreview.isPlaying) videoPreview.stopPlayback()

    }

    override fun onStop() {
        super.onStop()
        if (videoPreview.isPlaying) videoPreview.stopPlayback()
    }

    override fun onBackPressed() {
        if (publishContent.text.toString().isNotEmpty()) {
            SPUtils.getInstance(Constants.SPNAME).put("draft", publishContent.text.toString())
        }
        if (imageBigPreview.visibility == View.VISIBLE) {
            imageBigPreview.visibility = View.GONE
        } else if (previewRl.visibility == View.VISIBLE) {
            previewRl.visibility = View.GONE
            videoPreview.stopPlayback()
        } else
            super.onBackPressed()
    }

    private var positionItem: PoiItem? = null
    override fun onClick(view: View) {
        when (view.id) {
            R.id.locationCity -> {
                if (emojRv.visibility == View.VISIBLE)
                    emojRv.visibility = View.GONE
                startActivityForResult<LocationActivity>(REQUEST_CODE_MAP)
            }
            R.id.startRecordBtn -> {
                if (emojRv.visibility == View.VISIBLE)
                    emojRv.visibility = View.GONE
                if (currentActionState == ACTION_RECORDING) {
                    if (totalSecond < 5) {
                        CommonFunction.toast("再录制长一点吧")
                        return
                    }
                }
                if (isTopPreview) {
                    currentActionState = ACTION_DONE
                }
                switchActionState()
            }
            R.id.deleteRecord -> {
                val dialog = DeleteDialog(this)
                dialog.show()
                dialog.title.text = "重新录制"
                dialog.tip.text = "确定重新录制？"
                dialog.confirm.onClick {
                    if (emojRv.visibility == View.VISIBLE)
                        emojRv.visibility = View.GONE
                    mMediaRecorderHelper.cancel()
                    changeToNormalState()
                    dialog.dismiss()
                }

                dialog.cancel.onClick {
                    dialog.dismiss()
                }


            }
            R.id.finishRecord -> { //录制完成
                if (emojRv.visibility == View.VISIBLE)
                    emojRv.visibility = View.GONE
                //如果下面在预览播放，那么就先释放资源，停止播放
                MediaPlayerHelper.realese()
                mPreviewTimeThread?.stop()
                //检查发布按钮是否可以使用
                checkCompleteBtnEnable()
                //重置状态为完成
                currentActionState = ACTION_DONE
                recordTv.text = "删除并重录"
                deleteRecord.visibility = View.GONE
                recordTime.visibility = View.GONE
                finishRecord.visibility = View.GONE
                recordProgress.visibility = View.GONE
                startRecordBtn.setImageResource(R.drawable.icon_record_delete)

                audioTime.text = UriUtils.getShowTime(totalSecond)
                audioRecordLl.visibility = View.VISIBLE
                recordDelete.isVisible = true

                //changeToNormalState()
            }
            R.id.recordDelete -> {
                val dialog = DeleteDialog(this)
                dialog.show()
                dialog.title.text = "重新录制"
                dialog.tip.text = "确定重新录制？"
                dialog.confirm.onClick {
                    isTopPreview = false
                    mMediaRecorderHelper.cancel()
                    changeToNormalState()
                    dialog.dismiss()
                }

                dialog.cancel.onClick {
                    dialog.dismiss()
                }

            }
            R.id.audioPlayBtn -> {
                if (!click) {
                    currentActionState = ACTION_COMMPLETE
                    click = true
                }
                isTopPreview = true
                switchActionState()
            }
            R.id.chooseTitle, R.id.chooseTitleBtn -> {
//                val topics = ArrayList<String>()
//                for (data in choosedTitleAdapter.data) {
//                    if (data.isfuse) {
//                        topics.add(data.content)
//                    }
//                }
//                startActivityForResult<ChooseTitleActivity>(REQUEST_CODE_TITILE,"title" to topics)
                startActivityForResult<ChooseTitleActivity>(REQUEST_CODE_TITILE)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //拍照返回
            if (requestCode == REQUEST_CODE_CAPTURE_RAW) {
                //用于展示相册初始化界面
                //插入全部照片的第二个 第一个为拍照
                //UriUtils.updateMedia(this, imageFile?.absolutePath ?: "")
                val imageBean = MediaBean(
                    imageFile?.length()?.toInt() ?: 0,
                    MediaBean.TYPE.IMAGE,
                    imageFile?.absolutePath ?: "",
                    imageFile?.name ?: "",
                    "",
                    0,
                    imageFile?.length() ?: 0L,
                    true,
                    width = ImageUtils.getSize(imageFile?.absolutePath ?: "")[0],
                    height = ImageUtils.getSize(imageFile?.absolutePath ?: "")[0]
                )
                //插入选中照片的第一个
                pickedPhotoAdapter.addData(0, imageBean)

                allPhotoAdapter.addData(1, imageBean)
                pickedPhotosRv.visibility = if (pickedPhotos.size > 0) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
                checkCompleteBtnEnable()
            }
            //视频返回
            else if (requestCode == REQUEST_CODE_VIDEO) {
                val uri = data!!.data!!
//                val uri = FileProvider.getUriForFile(this, AUTHORITY, imageFile!!)
                val cursor = contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndex(Video.VideoColumns._ID))
                    val filePath = cursor.getString(cursor.getColumnIndex(Video.VideoColumns.DATA))
                    val duration = cursor.getInt(cursor.getColumnIndex(Video.Media.DURATION))

                    var size = cursor.getLong(cursor.getColumnIndex(Video.Media.SIZE)) / 1024 //单位kb
                    if (size < 0) {
                        //某些设备获取size<0，直接计算
                        Log.e("dml", "this video size < 0 $filePath")
                        size = File(filePath).length() / 1024
                    }
                    val displayName = cursor.getString(cursor.getColumnIndex(Video.Media.DISPLAY_NAME))
                    //提前生成缩略图，再获取：http://stackoverflow.com/questions/27903264/how-to-get-the-video-thumbnail-path-and-not-the-bitmap
                    Video.Thumbnails.getThumbnail(contentResolver, id.toLong(), Video.Thumbnails.MICRO_KIND, null)
                    val projection = arrayOf(
                        Video.Thumbnails._ID,
                        Video.Thumbnails.DATA,
                        Video.Thumbnails.WIDTH,
                        Video.Thumbnails.HEIGHT
                    )
                    val thumCursor = contentResolver.query(
                        Video.Thumbnails.EXTERNAL_CONTENT_URI,
                        projection,
                        Video.Thumbnails.VIDEO_ID + "=?",
                        arrayOf(id.toString() + ""),
                        null
                    )
                    var thumbPath = ""
                    var width = 0
                    var height = 0
                    while (thumCursor!!.moveToNext()) {
                        thumbPath = thumCursor.getString(thumCursor.getColumnIndex(Video.Thumbnails.DATA))
                        width = cursor.getInt(cursor.getColumnIndex(Video.Thumbnails.WIDTH))
                        height = cursor.getInt(cursor.getColumnIndex(Video.Thumbnails.HEIGHT))
                    }
                    thumCursor.close()
                    cursor.close()

                    if (duration >= 3000) {
                        pickedPhotos.add(
                            0, MediaBean(
                                id, MediaBean.TYPE.VIDEO, filePath, displayName, thumbPath,
                                duration, size, true, width, height
                            )
                        )
                        //pickedPhotoAdapter.data.add(0,MediaBean(id, MediaBean.TYPE.VIDEO, filePath, displayName, thumbPath, duration, size, true))
                        allVideoThumbAdapter.data.add(
                            1,
                            MediaBean(id, MediaBean.TYPE.VIDEO, filePath, displayName, thumbPath, duration, size, true)
                        )
                        videoCheckIndex = 1
                        pickedPhotoAdapter.notifyDataSetChanged()
                        allVideoThumbAdapter.notifyDataSetChanged()
                        pickedPhotosRv.visibility = if (pickedPhotos.size > 0) {
                            View.VISIBLE
                        } else {
                            View.INVISIBLE
                        }
                        checkCompleteBtnEnable()
                    } else {
                        CommonFunction.toast("视频拍摄最短为3S")
                    }
                }
            }
            //地图返回
            else if (requestCode == REQUEST_CODE_MAP) {
                if (data?.getParcelableExtra<PoiItem>("poiItem") != null) {
                    positionItem = data!!.getParcelableExtra("poiItem") as PoiItem
                    locationCity.text =
                        (positionItem!!.cityName ?: "") + if (!positionItem!!.cityName.isNullOrEmpty()) {
                            "·"
                        } else {
                            ""
                        } + positionItem!!.title
//                    +  + (positionItem!!.adName ?: "") + (positionItem!!.businessArea ?: "") + (positionItem!!.snippet ?: "")

                    locationCity.ellipsize = TextUtils.TruncateAt.MARQUEE
                    locationCity.isSingleLine = true
                    locationCity.isSelected = true
                    locationCity.isFocusable = true
                    locationCity.isFocusableInTouchMode = true
                }
            }
            //发布标题返回
            else if (requestCode == REQUEST_CODE_TITILE) {
                choosedTitleAdapter.data.clear()
                choosedTitleAdapter.notifyDataSetChanged()
                if (data?.getStringArrayListExtra("title") != null) {
                    val topics = data?.getStringArrayListExtra("title")
                    for (topic in topics) {
                        choosedTitleAdapter.addData(LabelQualityBean(topic, isfuse = true))
                    }
                    titleState.setImageResource(R.drawable.icon_topic_blue)
                    chooseTitle.isVisible = false
                    chooseTitleRv.isVisible = true

                }
            }
            //兴趣选择返回
            else if (requestCode == REQUEST_CODE_LABEL) {
                checkTags.clear()
                if (data?.getSerializableExtra("label") != null)
                    checkTags.add((data?.getSerializableExtra("label") as SquareLabelBean))

                startToUploadAndPublsih()
            }
        }
    }

    private fun startToUploadAndPublsih() {
        if (!mPresenter.checkNetWork()) {
            CommonFunction.toast("网络不可用,请检查网络设置")
            return
        }


        // 此处要存下所有的数据信息
        val checkIds = mutableListOf<Int>()
        for (i in 0 until checkTags.size) {
            checkIds.add(checkTags[i].id)
        }
        val type = if (pickedPhotos.isNullOrEmpty() && mMediaRecorderHelper.currentFilePath.isNullOrEmpty()) {
            0
        } else if (!mMediaRecorderHelper.currentFilePath.isNullOrEmpty()) {
            3
        } else if (pickedPhotos.isNotEmpty() && pickedPhotos.size > 0 && pickedPhotos[0].fileType == MediaBean.TYPE.IMAGE) {
            1
        } else {
            2
        }

        val titles = mutableListOf<String>()
        for (data in choosedTitleAdapter.data) {
            titles.add(data.content)
        }
        val param = hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "descr" to "${publishContent.text}",
            "title" to "${chooseTitleBtn.text}",
            "lat" to if (positionItem == null) {
                UserManager.getlatitude()
            } else {
                positionItem!!.latLonPoint?.latitude ?: 0.0
            },
            "lng" to if (positionItem == null) {
                UserManager.getlongtitude()
            } else {
                positionItem!!.latLonPoint?.longitude ?: 0.0
            },
            "province_name" to if (positionItem == null) {
                UserManager.getProvince()
            } else {
                positionItem!!.provinceName ?: ""
            },
            "city_name" to if (positionItem == null) {
                UserManager.getCity()
            } else {
                positionItem!!.cityName ?: ""
            },
            "city_code" to (if (positionItem == null) {
                UserManager.getCityCode()
            } else {
                positionItem!!.cityCode ?: ""
            }),
            "puber_address" to if (locationCity.text.toString() == "不显示位置") {
                ""
            } else {
                locationCity.text.toString()
            },
            //发布消息的类型0,纯文本的 1，照片 2，视频 3，声音
            "type" to type,
            "titles" to Gson().toJson(titles)
        )

        if (checkIds.isNotEmpty()) {
            param["tag_id"] = checkTags[0].id
        }
        UserManager.publishParams = param
        UserManager.publishState = 1

        if (pickedPhotos.isNullOrEmpty() && mMediaRecorderHelper.currentFilePath.isNullOrEmpty()) {//文本
            publish()
        } else if (!mMediaRecorderHelper.currentFilePath.isNullOrEmpty()) {//音频
            //保存音频数据
            UserManager.mediaBeans.add(
                MediaParamBean(
                    url = mMediaRecorderHelper.currentFilePath,
                    duration = totalSecond
                )
            )
            //TODO上传音频
            val audioQnPath =
                "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME).getString(
                    "accid"
                )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                    16
                )}"
            mPresenter.uploadFile(1, 1, mMediaRecorderHelper.currentFilePath, audioQnPath, 3)
        } else if (pickedPhotos.isNotEmpty() && pickedPhotos.size > 0 && pickedPhotos[0].fileType == MediaBean.TYPE.IMAGE) { //图片
            //压缩图片并保存图片数据
            var pickedPaths = mutableListOf<String>()
            var index = 0
            for (photo in pickedPhotos) {
                pickedPaths.add(photo.filePath)
            }
            UriUtils.getLubanBuilder(this@PublishActivity)
                .load(pickedPaths)
                .setCompressListener(object : OnCompressListener {
                    override fun onSuccess(file: File?) {
                        if (file != null) {
                            Log.d(TAG1, "original[$index] = ${pickedPhotos[index].filePath}")
                            Log.d(TAG1, "crop[$index] = ${file.absolutePath}")
                            UserManager.mediaBeans.add(
                                MediaParamBean(
                                    url = file.absolutePath,
                                    width = pickedPhotos[index].width,
                                    height = pickedPhotos[index].height
                                )
                            )
                            pickedPhotos[index].filePath = file.absolutePath
                            index++
                        }
                        if (index == pickedPhotos.size)
                            uploadPictures()
                    }

                    override fun onError(e: Throwable?) {
                        for (photo in pickedPhotos) {
                            UserManager.mediaBeans.add(
                                MediaParamBean(
                                    url = photo.filePath,
                                    width = photo.width,
                                    height = photo.height
                                )
                            )
                        }

                        uploadPictures()

                    }

                    override fun onStart() {
                    }

                })
                .launch()

        } else {//视频
            //保存视频数据
            UserManager.mediaBeans.add(
                MediaParamBean(
                    url = pickedPhotos[0].filePath,
                    duration = pickedPhotos[0].duration,
                    width = pickedPhotos[0].width,
                    height = pickedPhotos[0].height
                )
            )
            //TODO上传视频
            val videoQnPath =
                "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME).getString(
                    "accid"
                )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                    16
                )}"
            mPresenter.uploadFile(1, 1, pickedPhotos[0].filePath, videoQnPath, 2)
        }
        if (ActivityUtils.isActivityAlive(ChooseLabelActivity::class.java.newInstance())) {
            ActivityUtils.finishActivity(ChooseLabelActivity::class.java)
        }
        finish()
    }


    //msg.what  0代表是文本，就上传文本    1：代表上传多个图片     2代表上传视频  3代表上传录音文件成功
    private var uploadCount = 0
    /**
     * 设置发布的参数
     */
    private val keyList: MutableList<String> = mutableListOf()

    /**
     * 发布的话题
     */
    private fun publish() {
        // 此处要存下所有的数据信息
        val checkIds = mutableListOf<Int>()
        for (i in 0 until checkTags.size) {
            checkIds.add(checkTags[i].id)
        }
        val type = if (pickedPhotos.isNullOrEmpty() && mMediaRecorderHelper.currentFilePath.isNullOrEmpty()) {
            0
        } else if (!mMediaRecorderHelper.currentFilePath.isNullOrEmpty()) {
            3
        } else if (pickedPhotos.isNotEmpty() && pickedPhotos.size > 0 && pickedPhotos[0].fileType == MediaBean.TYPE.IMAGE) {
            1
        } else {
            2
        }

        val titles = mutableListOf<String>()
        for (data in choosedTitleAdapter.data) {
            titles.add(data.content)
        }
        val param = hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "descr" to "${publishContent.text}",
            "title" to "${chooseTitleBtn.text}",
            "lat" to if (positionItem == null) {
                UserManager.getlatitude()
            } else {
                positionItem!!.latLonPoint?.latitude ?: 0.0
            },
            "lng" to if (positionItem == null) {
                UserManager.getlongtitude()
            } else {
                positionItem!!.latLonPoint?.longitude ?: 0.0
            },
            "province_name" to if (positionItem == null) {
                UserManager.getProvince()
            } else {
                positionItem!!.provinceName ?: ""
            },
            "city_name" to if (positionItem == null) {
                UserManager.getCity()
            } else {
                positionItem!!.cityName ?: ""
            },
            "city_code" to (if (positionItem == null) {
                UserManager.getCityCode()
            } else {
                positionItem!!.cityCode ?: ""
            }),
            "puber_address" to if (locationCity.text.toString() == "不显示位置") {
                ""
            } else {
                locationCity.text.toString()
            },
            //发布消息的类型0,纯文本的 1，照片 2，视频 3，声音
            "type" to type,
            "titles" to Gson().toJson(titles)
        )

        if (checkIds.isNotEmpty()) {
            param["tag_id"] = checkTags[0].id
        }
        UserManager.publishParams = param

        mPresenter.publishContent(
            type, UserManager.publishParams, keyList = keyList
        )
    }


    private fun uploadPictures() {
        //上传图片
        val imagePath =
            "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME).getString(
                "accid"
            )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                16
            )}"
        Log.d("uploadPictures", "${imagePath}")
        mPresenter.uploadFile(pickedPhotos.size, uploadCount + 1, pickedPhotos[uploadCount].filePath, imagePath, 1)
    }


    //发布消息的类型0,纯文本的 1，照片 2，视频 3，声音
    override fun onQnUploadResult(success: Boolean, type: Int, key: String) {
        if (success) {
            when (type) {
                0 -> {
                    publish()
                }
                1 -> {
                    keyList.add(
                        uploadCount,
                        Gson().toJson(
                            MediaParamBean(
                                key,
                                pickedPhotos[uploadCount].duration,
                                pickedPhotos[uploadCount].width,
                                pickedPhotos[uploadCount].height
                            )
                        )
                    )
                    uploadCount++
                    if (uploadCount == pickedPhotos.size) {
                        publish()
                    } else {
                        uploadPictures()
                    }
                }
                2 -> {
                    keyList.add(
                        uploadCount,
                        Gson().toJson(
                            MediaParamBean(
                                key,
                                pickedPhotos[uploadCount].duration,
                                pickedPhotos[uploadCount].width,
                                pickedPhotos[uploadCount].height
                            )
                        )
                    )
                    publish()
                }
                3 -> {
                    keyList.add(uploadCount, Gson().toJson(MediaParamBean(key, totalSecond, 0, 0)))
                    publish()
                }
            }
        } else {
            EventBus.getDefault().postSticky(UploadEvent(qnSuccess = false))
        }

    }


    /**
     * 广场发布结果回调
     *   //发布消息的类型0,纯文本的 1，照片 2，视频 3，声音
     */
    override fun onSquareAnnounceResult(type: Int, success: Boolean, code: Int) {
        EventBus.getDefault().postSticky(AnnounceEvent(success, code))

        if (success) {
            if (intent.getIntExtra("from", 1) == 2) {
                EventBus.getDefault().postSticky(UploadEvent(1, 1, 1.0, from = UploadEvent.FROM_USERCENTER))
            }
            if (!this.isFinishing)
                finish()

        }
    }

}
