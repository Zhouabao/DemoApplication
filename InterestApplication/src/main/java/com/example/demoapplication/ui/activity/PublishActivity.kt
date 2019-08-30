package com.example.demoapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Video
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.MediaController
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.services.core.PoiItem
import com.blankj.utilcode.util.*
import com.example.baselibrary.emoj.EmojiSource
import com.example.baselibrary.glide.GlideUtil
import com.example.baselibrary.utils.RandomUtils
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.event.UpdateLabelEvent
import com.example.demoapplication.model.LabelBean
import com.example.demoapplication.model.MediaBean
import com.example.demoapplication.player.MediaPlayerHelper
import com.example.demoapplication.player.MediaRecorderHelper
import com.example.demoapplication.player.MediaRecorderHelper.*
import com.example.demoapplication.player.UpdateVoiceTimeThread
import com.example.demoapplication.presenter.PublishPresenter
import com.example.demoapplication.presenter.view.PublishView
import com.example.demoapplication.ui.adapter.ChoosePhotosAdapter
import com.example.demoapplication.ui.adapter.EmojAdapter
import com.example.demoapplication.ui.adapter.PublishLabelAdapter
import com.example.demoapplication.ui.dialog.DeleteDialog
import com.example.demoapplication.utils.AMapManager
import com.example.demoapplication.utils.UriUtils
import com.example.demoapplication.utils.UriUtils.getAllPhotoInfo
import com.example.demoapplication.utils.UriUtils.getAllVideoInfos
import com.example.demoapplication.utils.UserManager
import com.example.demoapplication.widgets.DividerItemDecoration
import com.kotlin.base.ext.onClick
import com.kotlin.base.ext.setVisible
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_publish.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.layout_record_audio.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*


/**
 * 发布内容页面
 *
 */
class PublishActivity : BaseMvpActivity<PublishPresenter>(), PublishView, RadioGroup.OnCheckedChangeListener,
    View.OnClickListener, TextWatcher {


    companion object {
        const val AUTHORITY = "com.example.demoapplication.fileprovider" //FileProvider的签名 7.0以上要用
        const val REQUEST_CODE_CAPTURE_RAW = 6 //startActivityForResult时的请求码
        const val REQUEST_CODE_VIDEO = 10 //startActivityForResult时的请求码
        const val REQUEST_CODE_LABEL = 20 //startActivityForResult时的请求码
        const val REQUEST_CODE_MAP = 30 //startActivityForResult时的请求码
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish)
        initView()

        //获取所有的照片信息
        allPhotoAdapter.setNewData(getAllPhotoInfo(this))
        //获取所有的视频封面
        allVideoThumbAdapter.setNewData(getAllVideoInfos(this))
        initData()
    }

    private fun initData() {
        locationCity.text = UserManager.getCity()
        GlideUtil.loadAvatorImg(this, UserManager.getAvator(), publisherAvator)
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
            AlertDialog.Builder(this)
                .setTitle("草稿箱")
                .setMessage("是否启用草稿箱？")
                .setPositiveButton("是") { _, _ ->
                    publishContent.setText(SPUtils.getInstance(Constants.SPNAME).getString("draft", ""))
                    publishContent.setSelection(publishContent.length())
                    SPUtils.getInstance(Constants.SPNAME).remove("draft", true)
                }
                .setNegativeButton("否") { _, _ ->
                    SPUtils.getInstance(Constants.SPNAME).remove("draft", true)
                }
                .setOnDismissListener {
                    SPUtils.getInstance(Constants.SPNAME).remove("draft", true)
                }
                .show()
        }
    }


    private fun initView() {
        btnBack.onClick {
            onBackPressed()
        }

        //主动弹起键盘
//        publishContent.postDelayed(
//            { KeyboardUtils.showSoftInput(publishContent) }, 100L
//        )


        mPresenter = PublishPresenter()
        mPresenter.mView = this
        mPresenter.context = applicationContext

        tabPublishWay.setOnCheckedChangeListener(this)
        tabPublishWay.check(currentWayId)//默认选中图片
        publishContent.addTextChangedListener(this)
        publishBtn.setOnClickListener(this)
        locationCity.setOnClickListener(this)
        btn_emo.setOnClickListener(this)

        initTags()
        initPhotos()
        initVideos()
        initAudioLl()
        initPickedRv()
        initEmojRv()
    }

    /**************设置表情包******************/
    private val emojAdapter by lazy { EmojAdapter() }

    private fun initEmojRv() {
        emojRv.layoutManager = GridLayoutManager(this, 10, RecyclerView.VERTICAL, false)
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


    /***************设置选中的标签******************/
    private val publishLabelAdapter by lazy { PublishLabelAdapter() }
    private var checkTags = mutableListOf<LabelBean>()
    private fun initTags() {
        tagLayoutRv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        tagLayoutRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL_LIST,
                SizeUtils.dp2px(8F),
                resources.getColor(R.color.colorWhite)
            )
        )
        tagLayoutRv.adapter = publishLabelAdapter

        //获取广场首页选中的标签id
        val checkedId = SPUtils.getInstance(Constants.SPNAME).getInt("globalLabelId")
        val myTags: MutableList<LabelBean> = UserManager.getSpLabels()
        for (tag in myTags) {
            if (checkedId == tag.id && checkedId != Constants.RECOMMEND_TAG_ID) {
                publishLabelAdapter.addData(tag)
                checkTags.add(tag)
            }
        }

        publishLabelAdapter.addData(0, LabelBean("添加标签"))
        publishLabelAdapter.setOnItemClickListener { adapter, view, position ->
            if (position == 0) {
                startActivityForResult<PublishChooseLabelsActivity>(
                    REQUEST_CODE_LABEL,
                    "checkedLabels" to checkTags as Serializable
                )
            } else {//其他时候点击就删除标签
                if (publishLabelAdapter.data.size <= 2) {
                    ToastUtils.showShort("至少要选择一个标签哦")
                    return@setOnItemClickListener
                } else {
                    val item = publishLabelAdapter.data[position]
                    checkTags.remove(item)
                    publishLabelAdapter.data.remove(item)
                    publishLabelAdapter.notifyItemRemoved(position)
                }
            }
        }
    }


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
        pickedPhotosRv.adapter = pickedPhotoAdapter
        pickedPhotoAdapter.setNewData(pickedPhotos)
        pickedPhotoAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.choosePhoto -> {
                    //showBigImagePreview(pickedPhotoAdapter.data[position])
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
//                    showBigImagePreview(allPhotoAdapter.data[position])
                }
                //点击选中
                R.id.choosePhotoDel -> {
                    //相册的选择与取消选择
                    if (pickedPhotos.size == 9) {
                        ToastUtils.showShort("最多只能选9张图片")
                        return@setOnItemChildClickListener
                    }
                    allPhotoAdapter.data[position].ischecked = !(allPhotoAdapter.data[position].ischecked)
                    pickedPhotos.add(allPhotoAdapter.data[position])
                    pickedPhotosRv.visibility = if (pickedPhotos.size > 0) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
                    pickedPhotoAdapter.notifyDataSetChanged()
                    allPhotoAdapter.notifyDataSetChanged()
                    checkCompleteBtnEnable()
                }
                //点击取消选择
                R.id.choosePhotoIndex -> {
                    allPhotoAdapter.data[position].ischecked = !(allPhotoAdapter.data[position].ischecked)
                    for (photo in pickedPhotos) {
                        if (photo.id == allPhotoAdapter.data[position].id) {
                            pickedPhotos.remove(photo)
                            break
                        }
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
//                    showVideoPreview(allVideoThumbAdapter.data[position])
                }
                R.id.chooseCamera -> {
                    go2TakeVideo()
                }
                R.id.choosePhotoDel -> {
                    //视频的选择与取消选择
                    if (!allVideoThumbAdapter.data[position].ischecked) {
                        if (pickedPhotos.size == 1) {
                            ToastUtils.showShort("最多只能选择1个视频")
                            return@setOnItemChildClickListener
                        }
                        if (allVideoThumbAdapter.data[position].duration < 3000) {
                            ToastUtils.showShort("视频时长过短")
                            return@setOnItemChildClickListener
                        } else if (allVideoThumbAdapter.data[position].duration > 120000) {
                            ToastUtils.showShort("视频时长过长")
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
        videoPreviewStart.onClick {
            videoPreviewStart.visibility = View.GONE
            videoPreview.start()

        }
    }


    /**
     * @param isCapture 是否是拍照 true为拍照 false为视频
     */
    private fun gotoCaptureRaw(isCapture: Boolean) {
        imageFile = createImageFile(isCapture = isCapture)
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
    private fun createImageFile(isCrop: Boolean = false, isCapture: Boolean = true): File? {
        return try {
            var rootFile: File? = null

            if (isCapture) {
                rootFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    "demoapplicaiton/video"
                )
            } else {
                rootFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    "demoapplicaiton/camera"
                )
            }
            if (!rootFile.exists())
                rootFile.mkdirs()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val fileName = if (isCapture) {
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
                    ToastUtils.showShort("还可以录制10秒钟哦，抓紧时间")
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


    //默认单选组按钮选中照片
    private var currentWayId: Int = R.id.publishPhotos

    override fun onCheckedChanged(radioGroup: RadioGroup, checkedId: Int) {
        if (emojRv.visibility == View.VISIBLE)
            emojRv.visibility = View.GONE
        when (checkedId) {
            R.id.publishPhotos -> {
                if (pickedPhotos.size > 0 && pickedPhotos[0].fileType == MediaBean.TYPE.VIDEO || !mMediaRecorderHelper.currentFilePath.isNullOrEmpty()) {
                    tabPublishWay.check(currentWayId)
                    ToastUtils.showShort("不支持图片和其他媒体一起上传哦")
                    return
                }
                currentWayId = checkedId
                allPhotosRv.visibility = View.VISIBLE
                videosRv.visibility = View.GONE
                audioLl.visibility = View.GONE
            }
            R.id.publishVideo -> {
                if (pickedPhotos.size > 0 && pickedPhotos[0].fileType == MediaBean.TYPE.IMAGE || !mMediaRecorderHelper.currentFilePath.isNullOrEmpty()) {
                    tabPublishWay.check(currentWayId)
                    ToastUtils.showShort("不支持视频和其他媒体一起上传哦")
                    return
                }
                currentWayId = checkedId
                allPhotosRv.visibility = View.GONE
                videosRv.visibility = View.VISIBLE
                audioLl.visibility = View.GONE
            }
            R.id.publishAudio -> {
                if (pickedPhotos.isNotEmpty() || videoPath.isNotEmpty()) {
                    tabPublishWay.check(currentWayId)
                    ToastUtils.showShort("不支持语音和其他媒体一起上传哦")
                    return
                }
                currentWayId = checkedId
                allPhotosRv.visibility = View.GONE
                videosRv.visibility = View.GONE
                audioLl.visibility = View.VISIBLE
            }
        }
    }

    /************编辑内容监听**************/
    override fun afterTextChanged(p0: Editable) {
        if (p0.length > 200) {
            publishContent.setText(publishContent.text.subSequence(0, 200))
            publishContent.setSelection(publishContent.text.length)
            ToastUtils.showShort("超出字数限制")
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
        if (videoPreview.isPlaying) videoPreview.stopPlayback()
        countTimeThread?.cancel()
        mPreviewTimeThread?.stop()
        if (currentActionState == ACTION_RECORDING) {
            mMediaRecorderHelper.cancel()
            changeToNormalState()
        }
        if (KeyboardUtils.isSoftInputVisible(this)) {
            KeyboardUtils.showSoftInput(publishContent)
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
            R.id.publishBtn -> {
                if (checkTags.size <= 0) {
                    ToastUtils.showShort("标签是必选项哦~")
                    return
                }

                if (publishContent.text.isNullOrEmpty()) {
                    ToastUtils.showShort("文本内容是必填的哦~")
                    return
                }

//                if (pickedPhotos.size == 0 && mMediaRecorderHelper.currentFilePath.isNullOrEmpty()) {
//                    ToastUtils.showShort("语音、图片、视频至少要选择一种发布哦~")
//                    return
//                }

                if (!mMediaRecorderHelper.currentFilePath.isNullOrEmpty() && currentActionState != ACTION_COMMPLETE) {
                    ToastUtils.showShort("请录制完语音再发布")
                    return
                }


                if (emojRv.visibility == View.VISIBLE) {
                    emojRv.visibility = View.GONE
                }
                if (pickedPhotos.isNullOrEmpty() && mMediaRecorderHelper.currentFilePath.isNullOrEmpty()) {//文本
                    publish()
                } else if (!mMediaRecorderHelper.currentFilePath.isNullOrEmpty()) {//音频
                    //TODO上传音频
                    val audioQnPath =
                        "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME).getString(
                            "accid"
                        )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                            16
                        )}.mp3"
                    mPresenter.uploadFile(mMediaRecorderHelper.currentFilePath, audioQnPath, 3)
                } else if (pickedPhotos.isNotEmpty() && pickedPhotos.size > 0 && pickedPhotos[0].fileType == MediaBean.TYPE.IMAGE) { //图片
                    uploadPictures()
                } else {//视频
                    //TODO上传视频
                    val videoQnPath =
                        "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME).getString(
                            "accid"
                        )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                            16
                        )}.mp4"
                    mPresenter.uploadFile(pickedPhotos[0].filePath, videoQnPath, 2)
                }
                finish()
            }
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
                        ToastUtils.showShort("再录制长一点吧")
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
                isTopPreview = false
                mMediaRecorderHelper.cancel()
                changeToNormalState()
            }
            R.id.audioPlayBtn -> {
                if (!click) {
                    currentActionState = ACTION_COMMPLETE
                    click = true
                }
                isTopPreview = true
                switchActionState()
            }
            R.id.btn_emo -> {
                if (emojRv.visibility == View.VISIBLE) {
                    emojRv.visibility = View.GONE
                } else {
//                    allPhotosRv.visibility = View.GONE
//                    videosRv.visibility = View.GONE
//                    audioLl.visibility = View.GONE
                    emojRv.visibility = View.VISIBLE
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //拍照返回
            if (requestCode == REQUEST_CODE_CAPTURE_RAW) {
                //用于展示相册初始化界面
                val imageBean = MediaBean(
                    imageFile?.length()?.toInt() ?: 0,
                    MediaBean.TYPE.IMAGE,
                    imageFile?.absolutePath ?: "",
                    imageFile?.name ?: "",
                    "",
                    0,
                    imageFile?.length() ?: 0L,
                    true
                )
                //插入选中照片的第一个
                pickedPhotos.add(0, imageBean)
                pickedPhotoAdapter.notifyDataSetChanged()
                //插入全部照片的第二个 第一个为拍照
                allPhotoAdapter.data.add(1, imageBean)
                allPhotoAdapter.notifyDataSetChanged()
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
                    val projection = arrayOf(Video.Thumbnails._ID, Video.Thumbnails.DATA)
                    val thumCursor = contentResolver.query(
                        Video.Thumbnails.EXTERNAL_CONTENT_URI,
                        projection,
                        Video.Thumbnails.VIDEO_ID + "=?",
                        arrayOf(id.toString() + ""),
                        null
                    )
                    var thumbPath = ""
                    while (thumCursor!!.moveToNext()) {
                        thumbPath = thumCursor.getString(thumCursor.getColumnIndex(Video.Thumbnails.DATA))
                    }
                    thumCursor.close()
                    cursor.close()

                    if (duration >= 3000) {
                        pickedPhotos.add(
                            0,
                            MediaBean(id, MediaBean.TYPE.VIDEO, filePath, displayName, thumbPath, duration, size, true)
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
                        ToastUtils.showShort("视频拍摄最短为3S")
                    }
                }
            }
            //标签返回
            else if (requestCode == REQUEST_CODE_LABEL) {
                checkTags = data!!.getSerializableExtra("checkedLabels") as MutableList<LabelBean>
                publishLabelAdapter.setNewData(data!!.getSerializableExtra("checkedLabels") as MutableList<LabelBean>)
                publishLabelAdapter.addData(0, LabelBean("添加标签"))
                checkCompleteBtnEnable()
            }
            //地图返回
            else if (requestCode == REQUEST_CODE_MAP) {
                if (data?.getParcelableExtra<PoiItem>("poiItem") != null) {
                    positionItem = data!!.getParcelableExtra("poiItem") as PoiItem
                    locationCity.text = positionItem!!.title + (positionItem!!.cityName ?: "") + (positionItem!!.adName
                        ?: "") + (positionItem!!.businessArea ?: "") + (positionItem!!.snippet ?: "")
                }
            }
        }
    }


    //msg.what  0代表是文本，就上传文本    1：代表上传多个图片     2代表上传视频  3代表上传录音文件成功
    private var uploadCount = 0
    /**
     * 设置发布的参数
     */
    private val keyList: Array<String?>? = arrayOfNulls<String>(10)

    private fun publish() {
        val checkIds = arrayOfNulls<Int>(10)
        for (i in 0 until checkTags.size) {
            checkIds[i] = checkTags[i].id
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

        val param = hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "tag_id" to SPUtils.getInstance(Constants.SPNAME).getInt("globalLabelId"),
            "descr" to "${publishContent.text}",
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
            //上传音频、视频的时间，精确到秒
            "duration" to if (pickedPhotos.isNotEmpty() && type == 2) {
                pickedPhotos[0].duration / 1000
            } else if (type == 3) {
                totalSecond
            } else {
                0
            }
            //	发布的图片/视频/声音 的json串（ios和android定义相同数据结构）
            //  "comment" to "",
            //发布图片/视频/声音 后加密的json串（ios和android定义相同数据结构）
            //  "md5_json" to ""
        )

        mPresenter.publishContent(param, checkIds, keyList)
    }


    private fun uploadPictures() {
        //上传图片
        val imagePath =
            "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME).getString(
                "accid"
            )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                16
            )}.jpg"
        mPresenter.uploadFile(pickedPhotos[uploadCount].filePath, imagePath, 1)
    }

    override fun onQnUploadResult(success: Boolean, type: Int, key: String) {
        if (success) {
            when (type) {
                0 -> {
                    publish()
                }
                1 -> {
                    keyList?.set(uploadCount, key)
                    uploadCount++
                    if (uploadCount == pickedPhotos.size) {
                        publish()
                    } else {
                        uploadPictures()
                    }
                }
                2 -> {
                    keyList?.set(uploadCount, key)
                    publish()
                }
                3 -> {
                    keyList?.set(uploadCount, key)
                    publish()
                }
            }
        }
    }


    /**
     * 广场发布结果回调
     */
    override fun onSquareAnnounceResult(success: Boolean) {
        if (success) {
            toast("动态发布成功！")
            EventBus.getDefault()
                .post(UpdateLabelEvent(LabelBean(id = SPUtils.getInstance(Constants.SPNAME).getInt("globalLabelId"))))
            if (!this.isFinishing)
                finish()
        }
    }

}
