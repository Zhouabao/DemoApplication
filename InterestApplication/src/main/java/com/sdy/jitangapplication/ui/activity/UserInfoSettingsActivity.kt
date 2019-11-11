package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.core.view.isVisible
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
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.event.UserCenterEvent
import com.sdy.jitangapplication.model.LabelBean
import com.sdy.jitangapplication.model.MyPhotoBean
import com.sdy.jitangapplication.model.NewJobBean
import com.sdy.jitangapplication.model.UserInfoSettingBean
import com.sdy.jitangapplication.presenter.UserInfoSettingsPresenter
import com.sdy.jitangapplication.presenter.view.UserInfoSettingsView
import com.sdy.jitangapplication.ui.adapter.UserPhotoAdapter
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.GotoVerifyDialog
import com.sdy.jitangapplication.widgets.OnRecyclerItemClickListener
import kotlinx.android.synthetic.main.activity_user_center.btnBack
import kotlinx.android.synthetic.main.activity_user_info_settings.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.dialog_delete_photo.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivityForResult
import top.zibin.luban.OnCompressListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 个人信息设置界面
 *
 *
 */
class UserInfoSettingsActivity : BaseMvpActivity<UserInfoSettingsPresenter>(), UserInfoSettingsView,
    OnItemDragListener, View.OnClickListener {
    override fun onGetJobListResult(mutableList: MutableList<NewJobBean>?) {

    }


    companion object {
        const val IMAGE_SIZE = 9
        const val REPLACE_REQUEST = 187
    }

    val params by lazy { hashMapOf("token" to UserManager.getToken(), "accid" to UserManager.getAccid()) }
    private var isChange = false
    private var photos: MutableList<MyPhotoBean?> = mutableListOf()
    private val adapter by lazy { UserPhotoAdapter(datas = mutableListOf()) }
    private var data: UserInfoSettingBean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info_settings)
        initView()
        mPresenter.personalInfo(params)
        setSwipeBackEnable(false)
    }

    private fun initView() {
        when (intent.getIntExtra("type", -1)) {
            GotoVerifyDialog.TYPE_CHANGE_AVATOR_PASS -> {
                llGuide.isVisible = true
                guideContent.text = "上传真实头像获取更多配对机会"
                guideDelete.onClick {
                    llGuide.isVisible = false
                    refreshLayout()
                }
            }
            GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS -> {
                llGuide.isVisible = true
                guideContent.text = "请替换当前头像"
                guideDelete.onClick {
                    llGuide.isVisible = false
                    refreshLayout()
                }
            }
            GotoVerifyDialog.TYPE_CHANGE_ABLUM -> {
                llGuide.isVisible = true
                guideContent.text = "完善相册，获取更多展示"
                guideDelete.onClick {
                    llGuide.isVisible = false
                    refreshLayout()
                }
            }
            else -> {
                llGuide.isVisible = false
            }
        }


        btnBack.setOnClickListener(this)
        userJob.setOnClickListener(this)
        userNickName.setOnClickListener(this)
        userNickSign.setOnClickListener(this)
        userBirth.setOnClickListener(this)
        saveBtn.setOnClickListener(this)

        mPresenter = UserInfoSettingsPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.personalInfo(params)
        }


        userPhotosRv.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
//        userPhotosRv.addItemDecoration(
//            DividerItemDecoration(
//                this,
//                DividerItemDecoration.BOTH_SET,
//                SizeUtils.dp2px(15F),
//                resources.getColor(R.color.colorWhite)
//            )
//        )
        userPhotosRv.adapter = adapter
        val itemDragAndSwpieCallBack = ItemDragAndSwipeCallback(adapter)
        val itemTouchHelper = ItemTouchHelper(itemDragAndSwpieCallBack)
        itemTouchHelper.attachToRecyclerView(userPhotosRv)

        //开启拖拽
        adapter.enableDragItem(itemTouchHelper, R.id.userImg, true)
        adapter.setOnItemDragListener(this)
        userPhotosRv.addOnItemTouchListener(object : OnRecyclerItemClickListener(userPhotosRv) {
            override fun onItemClick(vh: RecyclerView.ViewHolder) {
                if (vh.itemViewType == MyPhotoBean.COVER) {
                    if (adapter.data.size == IMAGE_SIZE + 1) {
                        CommonFunction.toast("最多只能上传9张，请删除后上传")
                        return
                    }
                    choosePosition = vh.layoutPosition
                    onTakePhoto(1)
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
            SPUtils.getInstance(Constants.SPNAME).put("avatar", data.avatar!!)

            userNickName.text = "${data.nickname}"
            userNickSign.text = "${data.sign}"
            userGender.text = if (data.gender == 1) {
                "男"
            } else {
                "女"
            }
            userBirth.text = "${data.birth}"
            //userJob.text = "${data.job}"

            for (photoWallBean in (data.photos_wall ?: mutableListOf()).withIndex()) {
                photoWallBean.value?.type = MyPhotoBean.PHOTO
                if (photoWallBean.index == 0) {
                    originalAvator = photoWallBean.value?.url ?: ""
                    if (UserManager.isNeedChangeAvator() && !UserManager.getAvator().contains(
                            photoWallBean.value?.url ?: ""
                        )
                    ) {
                        isChange = true
                        checkSaveEnable()
                    }
                }
            }

            adapter.setNewData(data.photos_wall ?: mutableListOf())
            photos.addAll(data.photos_wall ?: mutableListOf())
            adapter.addData(MyPhotoBean(type = MyPhotoBean.COVER))
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
        dialog.makeAvatorTip.text = if (position == 0) {
            "替换头像"
        } else {
            "设为头像"
        }


        dialog.makeAvator.onClick {
            if (position != 0) {
                if (adapter.data[position].has_face != 2) {
                    CommonFunction.toast(getString(R.string.real_avator_tip))
                    dialog.dismiss()
                    return@onClick
                }
                Collections.swap(photos, position, 0)
                Collections.swap(adapter.data, position, 0)
                adapter.notifyDataSetChanged()
                isChange = true
                checkSaveEnable()
                dialog.dismiss()
            } else {
                choosePosition = 0
                onTakePhoto(1, true)
                dialog.dismiss()
            }
        }


        dialog.lldelete.onClick {
            if ((position == 0 && adapter.data[1].has_face == 2) || position != 0) {
                isChange = true
                checkSaveEnable()
                adapter.data.removeAt(position)
                photos.removeAt(position)
                adapter.notifyDataSetChanged()
                refreshLayout()
                dialog.dismiss()
            } else {
                CommonFunction.toast(getString(R.string.real_avator_tip))
                dialog.dismiss()
            }
        }

        dialog.cancelDelete.onClick {
            dialog.dismiss()
        }
    }

    private var fromPos = -1
    private var toPos = -1
    override fun onItemDragMoving(hF: RecyclerView.ViewHolder?, pF: Int, hT: RecyclerView.ViewHolder?, pT: Int) {

    }

    override fun onItemDragStart(holder: RecyclerView.ViewHolder?, position: Int) {
        fromPos = position
    }

    override fun onItemDragEnd(holder: RecyclerView.ViewHolder?, position: Int) {
        toPos = position
        if (toPos == 0 || fromPos == 0) {//from 大于 to
            if (adapter.data[if (toPos == 0) {
                    toPos
                } else {
                    fromPos
                }].has_face != 2
            ) {
                val data = adapter.data[toPos]
                adapter.data.removeAt(toPos)
                adapter.data.add(fromPos, data)
                adapter.notifyDataSetChanged()

//                Collections.swap(adapter.data, toPos, fromPos)
//                adapter.notifyDataSetChanged()
                CommonFunction.toast(getString(R.string.real_avator_tip))
            } else {
                isChange = true
                val data = photos[fromPos]
                photos.removeAt(fromPos)
                photos.add(toPos, data)
                adapter.notifyDataSetChanged()
            }
        } else if (fromPos != toPos && fromPos != -1 && toPos != -1) {
            isChange = true
            val data = photos[fromPos]
            photos.removeAt(fromPos)
            photos.add(toPos, data)
            adapter.notifyDataSetChanged()
        }
        fromPos = -1
        toPos = -1
        checkSaveEnable()
        refreshLayout()
    }

    private fun checkSaveEnable() {
        saveBtn.isEnabled = isChange
    }

    /**
     * type 为1表示点击返回上传信息
     */
    private fun updatePhotos(type: Int = 0) {
        if (photos.isNullOrEmpty()) {
            CommonFunction.toast("请至少上传一张照片")
            return
        }


        val photosId = mutableListOf<Int?>()
        for (data in photos.withIndex()) {
            if (data.value?.type == MyPhotoBean.PHOTO) {
                photosId.add(data.value?.id)
            }
        }
        loading.show()
        mPresenter.addPhotoV2(
            token = UserManager.getToken(),
            accid = UserManager.getAccid(),
            photos = photosId,
            type = type
        )
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
            if (llGuide.isVisible) {
                SizeUtils.dp2px(50F) + (SizeUtils.dp2px(15F) + (16 / 9F * (ScreenUtils.getScreenWidth() - 4 * SizeUtils.dp2px(
                    15F
                )) / 3).toInt()) * row
            } else {
                0 + (SizeUtils.dp2px(15F) + (16 / 9F * (ScreenUtils.getScreenWidth() - 4 * SizeUtils.dp2px(15F)) / 3).toInt()) * row
            }

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
    private fun onTakePhoto(count: Int, replaceAvator: Boolean = false) {
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofImage())
            .maxSelectNum(count)
            .minSelectNum(0)
            .imageSpanCount(4)
            .selectionMode(PictureConfig.SINGLE)
            .previewImage(true)
            .isCamera(true)
            .enableCrop(false)
            .compressSavePath(UriUtils.getCacheDir(this))
            .compress(false)
            .scaleEnabled(true)
            .showCropFrame(true)
            .rotateEnabled(false)
            .withAspectRatio(9, 16)
            .compressSavePath(UriUtils.getCacheDir(this))
            .openClickSound(false)
            .forResult(
                if (replaceAvator) {
                    REPLACE_REQUEST
                } else {
                    PictureConfig.CHOOSE_REQUEST
                }
            )
    }

    private val loading by lazy { LoadingDialog(this) }
    override fun uploadImgResult(b: Boolean, key: String, replaceAvator: Boolean) {
        if (b) {
            mPresenter.addPhotoWall(replaceAvator, UserManager.getToken(), UserManager.getAccid(), key)
        }
    }


    /**
     * 添加单张照片回调结果
     */
    override fun onAddPhotoWallResult(replaceAvator: Boolean, result: MyPhotoBean) {
        if (loading.isShowing)
            loading.dismiss()
        isChange = true
        checkSaveEnable()
        result.type = MyPhotoBean.PHOTO
        if (replaceAvator) {
            if (result.has_face == 2) {//有人脸
                adapter.setData(0, result)
                photos[0] = result
                refreshLayout()
            } else {
                CommonFunction.toast(getString(R.string.real_avator_tip))
            }
        } else {
            adapter.data.add(adapter.data.size - 1, result)
            photos.add(result)
            adapter.notifyDataSetChanged()
            refreshLayout()
        }
    }


    override fun onPersonalInfoResult(data: UserInfoSettingBean?) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        setData(data)
    }


    /**
     * type  1 个人信息 2 头像
     */
    override fun onSavePersonalResult(result: Boolean, type: Int, from: Int) {
        if (type == 2) {
            if (loading.isShowing)
                loading.dismiss()
            if (result) {
                isChange = false
                checkSaveEnable()
                EventBus.getDefault().postSticky(UserCenterEvent(true))
                if (from == 1) {
                    if (dialog.isShowing)
                        dialog.dismiss()
                    setResult(Activity.RESULT_OK)
                    super.onBackPressed()
                }
            } else {
                isChange = true
                checkSaveEnable()
            }
        }
    }

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

                REPLACE_REQUEST,//替换头像
                PictureConfig.CHOOSE_REQUEST//选中
                -> {
                    if (data != null) {
                        if (!PictureSelector.obtainMultipleResult(data).isNullOrEmpty()) {
                            loading.show()
                            uploadPicture(
                                requestCode == REPLACE_REQUEST, PictureSelector.obtainMultipleResult(data)[0].path
                            )

                        }
                    }
                }
            }
        }
    }

    /**
     * 七牛上传图片
     */
    private fun uploadPicture(replaceAvator: Boolean = false, path: String) {
        val userProfile =
            "${Constants.FILE_NAME_INDEX}${Constants.USERCENTER}${SPUtils.getInstance(Constants.SPNAME).getString(
                "accid"
            )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                16
            )}"
        UriUtils.getLubanBuilder(this)
            .load(path)
            .setCompressListener(object : OnCompressListener {
                override fun onSuccess(file: File?) {
                    mPresenter.uploadProfile(
                        if (file != null) {
                            file.absolutePath
                        } else {
                            path
                        }, userProfile
                        , replaceAvator
                    )
                }

                override fun onError(e: Throwable?) {
                    mPresenter.uploadProfile(path, userProfile)
                }

                override fun onStart() {
                }

            })
            .launch()

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
            R.id.saveBtn -> {
                updatePhotos(0)
            }
        }

    }


    val dialog by lazy { DeleteDialog(this) }

    override fun onBackPressed() {
        checkIsForceChangeAvator()


    }

    override fun scrollToFinishActivity() {
        checkIsForceChangeAvator()
        super.scrollToFinishActivity()
    }


    private var originalAvator = ""
    private fun checkIsForceChangeAvator() {
        if (adapter.data.isNullOrEmpty()) {
            CommonFunction.toast("请至少上传一张照片")
            return
        }


        //强制替换头像下,如果已经换了头像
        if (adapter.data.isNotEmpty() && !UserManager.getAvator().contains(adapter.data[0].url) && !isChange && UserManager.isNeedChangeAvator()) {
            UserManager.saveForceChangeAvator(true)
        }

        //如果修改了信息 更新本地筛选信息
        if (SPUtils.getInstance(Constants.SPNAME).getInt("audit_only", -1) != -1) {
            if (!UserManager.getAvator().contains(adapter.data[0].url)) {
                UserManager.saveUserVerify(2)
                SPUtils.getInstance(Constants.SPNAME).remove("audit_only")
                //发送通知更新内容
                EventBus.getDefault().postSticky(RefreshEvent(true))
            }
        }

        //如果更改过相册信息并且没有是强制替换头像,就新增
        if (isChange && !UserManager.isNeedChangeAvator()) {
            dialog.show()
            dialog.tip.text = "是否保存此次编辑的内容？"
            dialog.cancel.text = "放弃"
            dialog.confirm.text = "保存"
            dialog.cancel.onClick {
                setResult(Activity.RESULT_OK)
                super.onBackPressed()
            }
            dialog.confirm.onClick {
                updatePhotos(1)
                dialog.dismiss()
//                setResult(Activity.RESULT_OK)
//                super.onBackPressed()
            }
        } else {
            setResult(Activity.RESULT_OK)
            super.onBackPressed()
        }


    }
}
