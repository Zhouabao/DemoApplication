package com.example.demoapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Video
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.*
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.model.LabelBean
import com.example.demoapplication.model.MediaBean
import com.example.demoapplication.presenter.PublishPresenter
import com.example.demoapplication.presenter.view.PublishView
import com.example.demoapplication.ui.adapter.ChoosePhotosAdapter
import com.example.demoapplication.ui.adapter.PublishLabelAdapter
import com.example.demoapplication.utils.UriUtils.getAllPhotoInfo
import com.example.demoapplication.utils.UriUtils.getAllVideoInfos
import com.example.demoapplication.utils.UserManager
import com.example.demoapplication.widgets.DividerItemDecoration
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_publish.*
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*


/**
 * 发布内容页面
 * //todo 此处要接入地图
 *
 */
class PublishActivity : BaseMvpActivity<PublishPresenter>(), PublishView, RadioGroup.OnCheckedChangeListener,
    View.OnClickListener, TextWatcher {

    companion object {
        const val AUTHORITY = "com.example.demoapplication.fileprovider" //FileProvider的签名 7.0以上要用
        const val REQUEST_CODE_CAPTURE_RAW = 6 //startActivityForResult时的请求码
        const val REQUEST_CODE_VIDEO = 10 //startActivityForResult时的请求码
        const val REQUEST_CODE_LABEL = 20 //startActivityForResult时的请求码
    }

    var imageFile: File? = null     //拍照后保存的照片
    var imgUri: Uri? = null         //拍照后保存的照片的uri

    private var pickedPhotos: MutableList<MediaBean> = mutableListOf()
    private var videoPath: String = ""
    private var audioPath: String = ""
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
        GlideUtil.loadAvatorImg(this, UserManager.getAvator(), publisherAvator)
        contentLength.text = SpanUtils.with(contentLength)
            .append(publishContent.length().toString())
            .setFontSize(14, true)
            .setForegroundColor(resources.getColor(R.color.colorOrange))
            .setBold()
            .append("/200")
            .setFontSize(10, true)
            .create()
    }

    //默认选中照片
    private var currentWayId: Int = R.id.publishPhotos

    private fun initView() {
        btnBack.onClick {
            finish()
        }

        //主动弹起键盘
        publishContent.postDelayed(
            { KeyboardUtils.showSoftInput(publishContent) }, 100L
        )


        mPresenter = PublishPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        tabPublishWay.setOnCheckedChangeListener(this)
        publishContent.addTextChangedListener(this)
        publishBtn.setOnClickListener(this)

        initTags()
        initPhotos()
        initVideos()
        initAudioLl()
        initPickedRv()
    }


    /***************设置选中的标签******************/
    private val publishLabelAdapter by lazy { PublishLabelAdapter() }

    /**
     * 移除子级标签
     *
     */
    private fun onRemoveSubLablesResult(label: LabelBean, parentPos: Int) {
        if (!label.son.isNullOrEmpty())
            for (tempLabel in label.son!!) {
                tempLabel.checked = false
                publishLabelAdapter.data.remove(tempLabel)
                checkTags.remove(tempLabel)
                onRemoveSubLablesResult(tempLabel, parentPos)
            }
        publishLabelAdapter.notifyDataSetChanged()
    }


    var checkTags = mutableListOf<LabelBean>()
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
        val checkedIds = SPUtils.getInstance(Constants.SPNAME).getString("globalLabelId")
        val myTags: MutableList<LabelBean> = UserManager.getSpLabels()
        val ids = checkedIds.split("-")

        for (id in ids) {
            for (tag in myTags) {
                if (id == tag.id.toString()) {
                    publishLabelAdapter.addData(tag)
                    checkTags.add(tag)
                }
            }
        }

        publishLabelAdapter.addData(0, LabelBean("添加标签"))
        publishLabelAdapter.setOnItemClickListener { adapter, view, position ->
            if (position == 0) {
                //todo 跳转到标签选择页面选新标签
                startActivityForResult<PublishChooseLabelsActivity>(
                    REQUEST_CODE_LABEL,
                    "checkedLabels" to checkTags as Serializable
                )
            } else {//其他时候点击就删除标签
                if (publishLabelAdapter.data.size <= 1) {
                    ToastUtils.showShort("至少要选择一个标签哦")
                    return@setOnItemClickListener
                } else {
                    onRemoveSubLablesResult(publishLabelAdapter.data[position], position)
                }
            }
        }
    }

    private val allPhotoAdapter by lazy { ChoosePhotosAdapter(0, pickedPhotos) }
    private val pickedPhotoAdapter by lazy { ChoosePhotosAdapter(1) }
    private val allVideoThumbAdapter by lazy { ChoosePhotosAdapter(2) }


    private fun initPickedRv() {
//        pickedPhotosRv.layoutManager = GridLayoutManager(this, 4, RecyclerView.VERTICAL, false)
        pickedPhotosRv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        //垂直+水平分割线
//        pickedPhotosRv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.BOTH_SET, SizeUtils.dp2px(4F), resources.getColor(R.color.colorWhite)))
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
        pickedPhotoAdapter.setOnItemClickListener { adapter, view, position ->
            val mediaBean = pickedPhotoAdapter.data[position]
            for (bean in allPhotoAdapter.data) {
                if (bean.id == mediaBean.id) {
                    bean.ischecked = false
                    break
                }
            }
            pickedPhotos.remove(mediaBean)
            allPhotoAdapter.notifyDataSetChanged()
            pickedPhotoAdapter.notifyItemRemoved(position)
            checkCompleteBtnEnable()
        }
    }

    /**
     * 读取相册中所有图片 时间顺序
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
        allPhotoAdapter.setOnItemClickListener { _, view, position ->
            if (position == 0) {
                //todo 去拍照
                gotoCaptureRaw(true)
            } else {
                //相册的选择与取消选择
                if (!allPhotoAdapter.data[position].ischecked) {
                    if (pickedPhotos.size == 9) {
                        ToastUtils.showShort("最多只能选9张图片")
                        return@setOnItemClickListener
                    }
                    allPhotoAdapter.data[position].ischecked = !(allPhotoAdapter.data[position].ischecked)
                    pickedPhotos.add(allPhotoAdapter.data[position])
                } else {
                    allPhotoAdapter.data[position].ischecked = !(allPhotoAdapter.data[position].ischecked)
                    for (photo in pickedPhotos) {
                        if (photo.id == allPhotoAdapter.data[position].id) {
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
                allPhotoAdapter.notifyDataSetChanged()
                checkCompleteBtnEnable()
            }
        }


    }

    /**
     * 读取相册中所有视频  时间顺序
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
        allVideoThumbAdapter.setOnItemClickListener { adapter, view, position ->
            if (position == 0) {
                go2TakeVideo()
//                gotoCaptureRaw(false)
            } else {
                //相册的选择与取消选择
                if (!allVideoThumbAdapter.data[position].ischecked) {
                    if (pickedPhotos.size == 1) {
                        ToastUtils.showShort("最多只能选择1个视频")
                        return@setOnItemClickListener
                    }
                    allVideoThumbAdapter.data[position].ischecked = !(allVideoThumbAdapter.data[position].ischecked)
                    pickedPhotos.add(allVideoThumbAdapter.data[position])
                } else {
                    allVideoThumbAdapter.data[position].ischecked = !(allVideoThumbAdapter.data[position].ischecked)
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
                checkCompleteBtnEnable()
            }
        }
    }

    /**
     * 初始化录音控件
     */
    private fun initAudioLl() {

    }

    override fun onCheckedChanged(radioGroup: RadioGroup, checkedId: Int) {
        when (checkedId) {
            R.id.publishPhotos -> {
                if (videoPath.isNotEmpty() || audioPath.isNotEmpty()) {
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
                if (pickedPhotos.isNotEmpty() || audioPath.isNotEmpty()) {
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
                    ToastUtils.showShort("不支持音频和其他媒体一起上传哦")
                    return
                }
                currentWayId = checkedId
                allPhotosRv.visibility = View.GONE
                videosRv.visibility = View.GONE
                audioLl.visibility = View.VISIBLE
            }
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
            var rootFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera")
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
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
            } else if (requestCode == REQUEST_CODE_VIDEO) {
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
                    cursor!!.close()

                    pickedPhotos.add(
                        0,
                        MediaBean(id, MediaBean.TYPE.VIDEO, filePath, displayName, thumbPath, duration, size, true)
                    )
                    //pickedPhotoAdapter.data.add(0,MediaBean(id, MediaBean.TYPE.VIDEO, filePath, displayName, thumbPath, duration, size, true))
                    allVideoThumbAdapter.data.add(
                        1,
                        MediaBean(id, MediaBean.TYPE.VIDEO, filePath, displayName, thumbPath, duration, size, true)
                    )
                    pickedPhotoAdapter.notifyDataSetChanged()
                    allVideoThumbAdapter.notifyDataSetChanged()
                    pickedPhotosRv.visibility = if (pickedPhotos.size > 0) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
                    checkCompleteBtnEnable()
                }
            } else if (requestCode == REQUEST_CODE_LABEL) {
                publishLabelAdapter.setNewData(data!!.getSerializableExtra("checkedLabels") as MutableList<LabelBean>)
                publishLabelAdapter.addData(0, LabelBean("添加标签"))
            }
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.publishBtn -> {
                //
                toast("发布")
            }
        }
    }

    /************编辑内容监听**************/
    override fun afterTextChanged(p0: Editable?) {
        checkCompleteBtnEnable()
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


    fun checkCompleteBtnEnable() {
        publishBtn.isEnabled = publishContent.text.isNotEmpty() || pickedPhotos.size > 0 || audioPath.isNotEmpty()
    }


}
