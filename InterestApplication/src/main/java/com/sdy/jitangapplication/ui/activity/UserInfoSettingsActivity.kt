package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ext.setVisible
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.UserCenterEvent
import com.sdy.jitangapplication.model.LabelBean
import com.sdy.jitangapplication.model.MyPhotoBean
import com.sdy.jitangapplication.model.UserInfoSettingBean
import com.sdy.jitangapplication.presenter.UserInfoSettingsPresenter
import com.sdy.jitangapplication.presenter.view.UserInfoSettingsView
import com.sdy.jitangapplication.ui.adapter.UserPhotoAdapter
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import com.sdy.jitangapplication.widgets.OnRecyclerItemClickListener
import kotlinx.android.synthetic.main.activity_user_center.btnBack
import kotlinx.android.synthetic.main.activity_user_info_settings.*
import kotlinx.android.synthetic.main.dialog_delete_photo.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivityForResult
import java.text.SimpleDateFormat
import java.util.*

/**
 * 个人信息设置界面
 *
 *
 */
class UserInfoSettingsActivity : BaseMvpActivity<UserInfoSettingsPresenter>(), UserInfoSettingsView,
    OnItemDragListener, View.OnClickListener {
    companion object {
        const val IMAGE_SIZE = 9
    }

    val params by lazy { hashMapOf("token" to UserManager.getToken(), "accid" to UserManager.getAccid()) }
    private var isChange = false
    private var photos: MutableList<MyPhotoBean> = mutableListOf()
    private val adapter by lazy { UserPhotoAdapter(datas = mutableListOf()) }
    private var data: UserInfoSettingBean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info_settings)
        initView()
        mPresenter.personalInfo(params)
    }

    private fun initView() {
        btnBack.setOnClickListener(this)
        userJob.setOnClickListener(this)
        userNickName.setOnClickListener(this)
        userNickSign.setOnClickListener(this)
        userBirth.setOnClickListener(this)

        mPresenter = UserInfoSettingsPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.personalInfo(params)
        }


        userPhotosRv.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        userPhotosRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.BOTH_SET,
                SizeUtils.dp2px(15F),
                resources.getColor(R.color.colorWhite)
            )
        )
        userPhotosRv.adapter = adapter
//        adapter.setEmptyView(R.layout.empty_user_photo_layout, userPhotosRv)
//        adapter.emptyView.onClick {
//            onTakePhoto(IMAGE_SIZE - photos.size + 1)
//        }
        val itemDragAndSwpieCallBack = ItemDragAndSwipeCallback(adapter)
        val itemTouchHelper = ItemTouchHelper(itemDragAndSwpieCallBack)
        itemTouchHelper.attachToRecyclerView(userPhotosRv)

        //开启拖拽
        adapter.enableDragItem(itemTouchHelper, R.id.userImg, true)
        adapter.setOnItemDragListener(this)
        userPhotosRv.addOnItemTouchListener(object : OnRecyclerItemClickListener(userPhotosRv) {
            override fun onItemClick(vh: RecyclerView.ViewHolder) {
                if (vh.itemViewType == MyPhotoBean.COVER) {
                    choosePosition = vh.layoutPosition
                    onTakePhoto(IMAGE_SIZE - photos.size + 1)
                } else {
                    if (adapter.data.size > 2)
                        showDeleteDialog(vh.layoutPosition)
                }
            }

            override fun onItemLongClick(holder: RecyclerView.ViewHolder) {
                if (holder.layoutPosition != adapter.data.size - 1) {
                    itemTouchHelper.startDrag(holder)
                }
            }
        })
    }

    private var choosePosition = -1
    /**
     * 填写数据
     */
    private fun setData(data: UserInfoSettingBean?) {
        if (data != null) {
            this.data = data
            //更新本地缓存
            SPUtils.getInstance(Constants.SPNAME).put("nickname", data.nickname!!)
            SPUtils.getInstance(Constants.SPNAME).put("gender", data.gender!!)
            SPUtils.getInstance(Constants.SPNAME).put("birth", data.birth!!)

            userNickName.text = "${data.nickname}"
            userNickSign.text = "${data.sign}"
            userGender.text = if (data.gender == 1) {
                "男"
            } else {
                "女"
            }
            userBirth.text = "${data.birth}"
            userJob.text = "${data.job}"

            adapter.domain = data.qiniu_domain
            for (url in data.photos ?: mutableListOf()) {
                photos.add(MyPhotoBean(MyPhotoBean.PHOTO, url))
            }
            adapter.setNewData(photos)
//            if ((data.photos ?: mutableListOf()).size < IMAGE_SIZE) {
            adapter.addData(MyPhotoBean(MyPhotoBean.COVER, ""))
//            }
            refreshLayout()
        }
    }


    override fun onError(text: String) {
        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
            getString(R.string.retry_load_error)
        } else {
            getString(R.string.retry_net_error)
        }
    }

    private fun showDeleteDialog(position: Int) {
        val dialog = Dialog(this, R.style.MyDialog)
        dialog.setContentView(R.layout.dialog_delete_photo)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        val window = dialog.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.y = SizeUtils.dp2px(40F)
        window?.attributes = params

        dialog.show()
        dialog.makeAvator.setVisible(position != 0)

        dialog.makeAvator.onClick {
            isChange = true
            val myPhotoBean0 = adapter.data[0]
            val myPhotoBeanP = adapter.data[position]
            photos.set(0, myPhotoBeanP)
            photos.set(position, myPhotoBean0)
            adapter.data.set(0, myPhotoBeanP)
            adapter.data.set(position, myPhotoBean0)
            adapter.notifyDataSetChanged()
//            Collections.swap(photos, position, 0)
//            Collections.swap(adapter.data, position, 0)

            dialog.dismiss()
        }


        dialog.lldelete.onClick {
            isChange = true

            adapter.remove(position)
            adapter.notifyDataSetChanged()
            refreshLayout()
            updatePhotos()
            dialog.dismiss()
        }

        dialog.cancel.onClick {
            dialog.dismiss()
        }
    }

    private var fromPos = -1
    private var toPos = -1
    override fun onItemDragMoving(hF: RecyclerView.ViewHolder?, pF: Int, hT: RecyclerView.ViewHolder?, pT: Int) {}

    override fun onItemDragStart(holder: RecyclerView.ViewHolder?, position: Int) {
        fromPos = position
    }

    override fun onItemDragEnd(holder: RecyclerView.ViewHolder?, position: Int) {
        toPos = position
        if (fromPos != toPos && fromPos != -1 && toPos != -1) {
            isChange = true
            Collections.swap(photos, fromPos, toPos)
            Collections.swap(adapter.data, fromPos, toPos)
            adapter.notifyDataSetChanged()
        } else {
            fromPos = -1
            toPos = -1
        }
        refreshLayout()
        updatePhotos()
    }

    private fun updatePhotos() {
        if (isChange) {
            val photos = arrayOfNulls<String>(10)
            for (data in adapter.data.withIndex()) {
                if (data.value.type == MyPhotoBean.PHOTO) {
                    photos[data.index] = data.value.url
                }
            }
            mPresenter.addPhotos(UserManager.getToken(), UserManager.getAccid(), photos)
        }
    }


    /**
     * 刷新布局位置
     *   val layoutParams = holder.itemView.layoutParams
    layoutParams.width =
    layoutParams.height = (16 / 9F * (ScreenUtils.getScreenWidth() - 4 * SizeUtils.dp2px(15F)) / 3).toInt()
     */
    private fun refreshLayout() {
        //判断提醒谁布局看是否需要下移
        var row = adapter.itemCount / 3
        row = if (0 == adapter.itemCount % 3) row else row + 1
//        row = if (4 == row) 4 else row//row最多为四行
        val marginTop =
            (SizeUtils.dp2px(15F) + (16 / 9F * (ScreenUtils.getScreenWidth() - 4 * SizeUtils.dp2px(15F)) / 3).toInt()) * row
        val params = mLinearLayout.layoutParams as RelativeLayout.LayoutParams
        params.setMargins(0, marginTop, 0, 0)
        mLinearLayout.layoutParams = params
    }


    private fun updateUserInfo(key: String, value: Any) {
        val params = hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            key to value
        )
        mPresenter.savePersonal(params)
    }


    /**
     * 拍照或者选取照片
     */
    private fun onTakePhoto(count: Int) {
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofImage())
            .maxSelectNum(count)
            .minSelectNum(0)
            .imageSpanCount(4)
            .selectionMode(PictureConfig.MULTIPLE)
            .previewImage(true)
            .isCamera(true)
            .enableCrop(true)
            .scaleEnabled(true)
            .showCropFrame(true)
            .rotateEnabled(false)
            .withAspectRatio(9, 16)
            .compress(true)
            .openClickSound(false)
            .forResult(PictureConfig.CHOOSE_REQUEST)
    }

    private val loading by lazy { LoadingDialog(this) }
    override fun uploadImgResult(b: Boolean, key: String) {
        chooseCount++
        if (b) {
            if (loading.isShowing)
                loading.dismiss()
            isChange = true
            adapter.addData(choosePosition, MyPhotoBean(MyPhotoBean.PHOTO, key))
//            if (adapter.data.size == IMAGE_SIZE + 1) {
//                adapter.remove(IMAGE_SIZE)
//            }
            adapter.notifyDataSetChanged()
            refreshLayout()
            if (chooseCount < selectList.size)
                uploadPicture()
            else {
                updatePhotos()
            }
        }
    }

    override fun onPersonalInfoResult(data: UserInfoSettingBean?) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        setData(data)
    }

    override fun onSavePersonalResult(result: Boolean, type: Int) {
        if (type == 2) {
            if (result) {
                EventBus.getDefault().postSticky(UserCenterEvent(true))
            }
        }
    }

    private var chooseCount = 0
    private var selectList: MutableList<LocalMedia> = mutableListOf()
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                100 -> {
                    if (data != null) {
                        val year = data.getStringExtra("year")
                        val monthDay = data.getStringExtra("month").substring(0, 2).plus("-")
                            .plus(data.getStringExtra("month").substring(2, 4))
                        userBirth.text = year.plus("-").plus(monthDay)
                        updateUserInfo(
                            "birth",
                            TimeUtils.date2Millis(
                                SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    Locale.getDefault()
                                ).parse(userBirth.text.toString())
                            ) / 1000L
                        )
                    }
                }
                102 -> {
                    if (data != null) {
                        if (data.getIntExtra("type", 0) == 1) {
                            userNickName.text = data.getStringExtra("content")
                            updateUserInfo("nickname", userNickName.text.toString())
                        } else if (data.getIntExtra("type", 0) == 2) {
                            userNickSign.text = data.getStringExtra("content")
                            updateUserInfo("sign", userNickSign.text.toString())
                        }
                    }
                }
                103 -> {
                    if (data?.getSerializableExtra("job") != null) {
                        val label = data.getSerializableExtra("job") as LabelBean
                        userJob.text = label.title
                        updateUserInfo("job", label.id)

                    }
                }
                PictureConfig.CHOOSE_REQUEST -> {
                    if (data != null) {
                        chooseCount = 0
                        selectList = PictureSelector.obtainMultipleResult(data)
                        loading.show()
                        uploadPicture()
                    }
                }
            }
        }
    }

    private fun uploadPicture() {
        val userProfile =
            "${Constants.FILE_NAME_INDEX}${Constants.USERCENTER}${SPUtils.getInstance(Constants.SPNAME).getString(
                "accid"
            )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                16
            )}.jpg"
        mPresenter.uploadProfile(selectList[chooseCount].compressPath, userProfile.toString())
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnBack -> {
                onBackPressed()
            }
            R.id.userNickName -> {
                startActivityForResult<NickNameActivity>(102, "type" to 1, "content" to userNickName.text.toString())
            }
            R.id.userNickSign -> {
                startActivityForResult<NickNameActivity>(102, "type" to 2, "content" to data?.sign)
            }
            R.id.userBirth -> {
                startActivityForResult<UserBirthActivity>(100)
            }
            R.id.userJob -> {
                startActivityForResult<MyJobActivity>(103, "job" to userJob.text.toString())
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
//        if (isChange) {
//            val photos = arrayOfNulls<String>(10)
//            for (data in adapter.data.withIndex()) {
//                if (data.value.type == MyPhotoBean.PHOTO) {
//                    photos[data.index] = data.value.url
//                }
//            }
//            mPresenter.addPhotos(UserManager.getToken(), UserManager.getAccid(), photos)
//        } else {
//            finish()
//        }

    }
}
