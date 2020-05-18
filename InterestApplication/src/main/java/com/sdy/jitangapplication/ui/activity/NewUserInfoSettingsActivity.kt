package com.sdy.jitangapplication.ui.activity

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.*
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.*
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.google.gson.Gson
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
import com.sdy.jitangapplication.event.AccountDangerEvent
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.event.UserCenterEvent
import com.sdy.jitangapplication.model.FindTagBean
import com.sdy.jitangapplication.model.MyPhotoBean
import com.sdy.jitangapplication.model.UserInfoSettingBean
import com.sdy.jitangapplication.presenter.UserInfoSettingsPresenter
import com.sdy.jitangapplication.presenter.view.UserInfoSettingsView
import com.sdy.jitangapplication.ui.adapter.MoreInfoAdapter
import com.sdy.jitangapplication.ui.adapter.UserPhotoAdapter
import com.sdy.jitangapplication.ui.dialog.*
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import com.sdy.jitangapplication.widgets.OnRecyclerItemClickListener
import kotlinx.android.synthetic.main.activity_new_user_info_settings.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.dialog_delete_photo.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.item_more_info.view.*
import kotlinx.android.synthetic.main.layout_add_score.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivityForResult
import top.zibin.luban.OnCompressListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * 用户信息界面
 */
class NewUserInfoSettingsActivity : BaseMvpActivity<UserInfoSettingsPresenter>(),
    UserInfoSettingsView,
    OnItemDragListener, View.OnClickListener {
    companion object {
        const val IMAGE_SIZE = 9
        const val REPLACE_REQUEST = 187
        private const val MSG_LOAD_SUCCESS = 100
        private const val MSG_LOAD_FAILED = -100
        private const val MSG_LOAD_DATA = 1
    }

    val dialog by lazy { DeleteDialog(this) }
    val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid()
        )
    }
    private var isChange = false
    private var photos: MutableList<MyPhotoBean?> = mutableListOf()
    private var originalPhotos: MutableList<MyPhotoBean?> = mutableListOf()//用于对比用户是否改变过相册信息
    private val adapter by lazy { UserPhotoAdapter(datas = mutableListOf()) }
    private val moreInfoAdapter by lazy { MoreInfoAdapter() }
    private var data: UserInfoSettingBean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_user_info_settings)
        initView()
        updateScoreLayout()
        mPresenter.personalInfo(params)
        setSwipeBackEnable(false)
    }


    private fun initView() {
        mPresenter = UserInfoSettingsPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        btnBack.setOnClickListener(this)
        userNickName.setOnClickListener(this)
        userBirth.setOnClickListener(this)
        userNickSign.setOnClickListener(this)
        saveBtn.setOnClickListener(this)
        userScore80.setOnClickListener(this)
        userScoreVip.setOnClickListener(this)


        //更多信息
        rvMoreInfo.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvMoreInfo.adapter = moreInfoAdapter
        moreInfoAdapter.setOnItemClickListener { _, view, position ->
            val item = moreInfoAdapter.data[position]
            showConditionPicker(
                view.moreInfoContent,
                view.moreInfoAnimation,
                item.point,
                item.title,
                item.id.toString(),
                item.find_tag,
                if (item.child.isEmpty()) {
                    val heights = mutableListOf<FindTagBean>()
                    for (i in 60 until 250) {
                        heights.add(FindTagBean(-1, "$i"))
                    }
                    heights
                } else {
                    item.child
                }
            )
        }

        userScore80.setBackgroundResource(R.drawable.shape_rectangle_gray_white_11dp)
        userScore80.tvAddScoreSmile.setTextColor(Color.parseColor("#FFD1D1DB"))
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
                                                        this@NewUserInfoSettingsActivity,
                                                        1,
                                                        PictureConfig.CHOOSE_REQUEST,
                                                        PictureMimeType.ofImage(), cropEnable = true
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
                            this@NewUserInfoSettingsActivity,
                            1,
                            PictureConfig.CHOOSE_REQUEST,
                            PictureMimeType.ofImage(), cropEnable = true
                        )
                    }

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

//
//        scrollUser.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
//            val view = v?.getChildAt(0)
//            if (intent.getBooleanExtra("showGuide", false)
//                && view != null && view.measuredHeight == scrollY + v?.height
//                && !data?.answer_list.isNullOrEmpty()
//            ) {
//                RemindUpdateUserInfoDialog(this, data?.answer_list ?: mutableListOf()).show()
//                intent.removeExtra("showGuide")
//            }
//        }

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
            userGender.text = if (data.gender == 1) {
                "男"
            } else {
                "女"
            }
            userBirth.text = "${data.birth}/${data.constellation}"

            if (UserManager.isUserVip()) {
                userScore80.isVisible = false
                userScoreVip.isVisible = false
                userScoreVip.setImageResource(R.drawable.icon_vip_score_highlight)
                userScoreVip.isEnabled = false
            } else {
                userScore80.isVisible = true
                userScoreVip.isVisible = true
                userScoreVip.setImageResource(R.drawable.icon_vip_score)
                userScoreVip.isEnabled = true
            }

            if (data.score_rule != null) {
                userScore80.tvAddScoreSmile.text =
                    "${data.score_rule.base_total + data.score_rule.base}"
                userScore80.ivAddScoreSmile.setImageResource(R.drawable.icon_xiaolian_gray)
                var scorePhoto = 0
                if (!data.photos_wall.isNullOrEmpty()) {
                    scorePhoto = (data.photos_wall.size - 1) * data.score_rule.photo
                }
                setScroeProgress(scorePhoto)
            }

            if (!data.sign.isNullOrEmpty() && data.sign.trim().isNotEmpty()) {
                userNickSign.text = "${data.sign}"
                userScoreAboutMe.isVisible = false
            } else
                userScoreAboutMe.isVisible = true

            updateScoreStatus(
                userScoreAboutMe,
                data.score_rule?.about ?: 0,
                !data.sign.isNullOrEmpty() && data.sign.trim().isNotEmpty()
            )
            moreInfoAdapter.setNewData(data.answer_list)
            for (data in moreInfoAdapter.data) {
                updateScoreStatus(
                    null,
                    data.point,
                    data.find_tag != null && data.find_tag!!.id != 0
                )
            }

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
            originalPhotos.addAll(data.photos_wall ?: mutableListOf())
            adapter.addData(
                MyPhotoBean(
                    type = MyPhotoBean.COVER,
                    photoScore = data.score_rule?.photo ?: 0
                )
            )
            refreshLayout()
            if (intent.getBooleanExtra("showGuide", false)) {
                scrollUser.postDelayed({
                    scrollToPosition(0, scrollUser.getChildAt(0).height)
                }, 100L)
            }

        }
    }


    /**
     * 平滑移动到底部页面
     */
    fun scrollToPosition(x: Int, y: Int) {

        val xTranslate = ObjectAnimator.ofInt(scrollUser, "scrollX", x)
        val yTranslate = ObjectAnimator.ofInt(scrollUser, "scrollY", y)

        val animators = AnimatorSet()
        animators.duration = 1000L
        animators.playTogether(xTranslate, yTranslate)
        animators.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                if (intent.getBooleanExtra("showGuide", false)
                    && !data?.answer_list.isNullOrEmpty()
                ) {
                    RemindUpdateUserInfoDialog(
                        this@NewUserInfoSettingsActivity,
                        data?.answer_list ?: mutableListOf()
                    ).show()
                    intent.removeExtra("showGuide")
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })
        animators.start()
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


        //不能删除头像
        dialog.lldelete.isVisible = position != 0

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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    (!PermissionUtils.isGranted(PermissionConstants.CAMERA) ||
                            !PermissionUtils.isGranted(PermissionConstants.STORAGE))
                ) {
                    PermissionUtils.permission(PermissionConstants.CAMERA)
                        .callback(object : PermissionUtils.SimpleCallback {
                            override fun onGranted() {
                                if (!PermissionUtils.isGranted(PermissionConstants.STORAGE)) {
                                }
                                PermissionUtils.permission(PermissionConstants.STORAGE)
                                    .callback(object : PermissionUtils.SimpleCallback {
                                        override fun onGranted() {
                                            CommonFunction.onTakePhoto(
                                                this@NewUserInfoSettingsActivity,
                                                1,
                                                REPLACE_REQUEST,
                                                PictureMimeType.ofImage(), cropEnable = true
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
                        this,
                        1,
                        REPLACE_REQUEST,
                        PictureMimeType.ofImage(),
                        cropEnable = true
                    )
                }
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
                setScroeProgress(-data!!.score_rule!!.photo)
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
    override fun onItemDragMoving(
        hF: RecyclerView.ViewHolder?,
        pF: Int,
        hT: RecyclerView.ViewHolder?,
        pT: Int
    ) {

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


        var hasChangePhotos = false
        if (originalPhotos.size != photos.size) {
            hasChangePhotos = true
        } else {
            for (i in 0 until photos.size) {
                if (originalPhotos[i]?.id != photos[i]?.id) {
                    hasChangePhotos = true
                    break
                }
            }
        }
        loading.show()

        if (moreInfoAdapter.params.isNotEmpty()) {
            savePersonalParams["detail_json"] = Gson().toJson(moreInfoAdapter.params)
        }
        if (hasChangePhotos) {
            val photosId = mutableListOf<Int?>()
            for (data in photos.withIndex()) {
                if (data.value?.type == MyPhotoBean.PHOTO) {
                    photosId.add(data.value?.id)
                }
            }
            mPresenter.addPhotoV2(
                savePersonalParams,
                photosId,
                2
            )
        } else {
            mPresenter.addPhotoV2(
                savePersonalParams,
                mutableListOf(),
                type
            )
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
            0 + (SizeUtils.dp2px(15F) + (16 / 9F * (ScreenUtils.getScreenWidth() - 4 * SizeUtils.dp2px(
                15F
            )) / 3).toInt()) * row


//        val params = mLinearLayout.layoutParams as RelativeLayout.LayoutParams
//        params.setMargins(0, marginTop, 0, 0)
//        mLinearLayout.layoutParams = params
    }


    /**
     * 保存个人信息参数列表
     */
    private val savePersonalParams: HashMap<String, Any?> by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "gender" to data?.gender
        )
    }


    private val loading by lazy { LoadingDialog(this) }
    override fun uploadImgResult(b: Boolean, key: String, replaceAvator: Boolean) {
        if (b) {
            mPresenter.addPhotoWall(
                replaceAvator,
                UserManager.getToken(),
                UserManager.getAccid(),
                key
            )
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
            updateScoreStatus(score = data!!.score_rule!!.photo, update = true)
        }
    }


    override fun onPersonalInfoResult(data: UserInfoSettingBean?) {
        if (data != null) {
            stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
            setData(data)
        } else {
            stateview.viewState = MultiStateView.VIEW_STATE_ERROR

        }
    }


    /**
     * type  1 个人信息 2 头像
     */
    override fun onSavePersonalResult(result: Boolean, type: Int, from: Int) {
        if (type == 2) {
            if (loading.isShowing)
                loading.dismiss()
            if (result) {
                //发送通知更新匹配列表
                EventBus.getDefault().postSticky(RefreshEvent(true))
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                102 -> { //昵称
                    if (data != null) {
                        if (data.getIntExtra("type", 0) == 1) {
                            userNickName.text = data.getStringExtra("content")
                            savePersonalParams["nickname"] = userNickName.text.toString()
                            isChange = true
                            checkSaveEnable()
                        }
                    }
                }
                105 -> {//关于我
                    userNickSign.text = data?.getStringExtra("content")
                    savePersonalParams["sign"] = data?.getStringExtra("content")

                    if (userScoreAboutMe.isVisible)
                        updateScoreStatus(
                            userScoreAboutMe,
                            this.data!!.score_rule?.about ?: 0,
                            update = true
                        )
                    isChange = true
                    checkSaveEnable()
                }
                REPLACE_REQUEST,//替换头像
                PictureConfig.CHOOSE_REQUEST//icon_verify_account_not_pass
                -> {
                    if (data != null) {
                        if (!PictureSelector.obtainMultipleResult(data).isNullOrEmpty()) {
                            loading.show()
                            uploadPicture(
                                requestCode == REPLACE_REQUEST,
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    if (PictureSelector.obtainMultipleResult(data)[0].androidQToPath.isNullOrEmpty()) {
                                        PictureSelector.obtainMultipleResult(data)[0].path
                                    } else {
                                        PictureSelector.obtainMultipleResult(data)[0].androidQToPath
                                    }
                                } else {
                                    PictureSelector.obtainMultipleResult(data)[0].path
                                }
                            )
                        }
                    }
                }
            }
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnBack -> {
                onBackPressed()
            }

            R.id.userScore20 -> {
                ReminderScoreDialog(this, 20).show()
//                userScore20.setImageResource(R.drawable.icon_twenty_click)
            }
            R.id.userScoreVip -> {
                ChargeVipDialog(ChargeVipDialog.INFINITE_SLIDE, this).show()
            }
            R.id.userScore80 -> {
                ReminderScoreDialog(
                    this,
                    ((data?.score_rule?.base_total ?: 0) + (data?.score_rule?.base ?: 0))
                ).show()
//                userScore80.setImageResource(R.drawable.icon_eighty_click)
            }
            R.id.userNickName -> {//昵称
                startActivityForResult<NickNameActivity>(
                    102,
                    "type" to 1,
                    "content" to userNickName.text.toString()
                )
            }

            R.id.userBirth -> { //生日
                showCalender(userBirth)
            }
            R.id.userNickSign -> {//关于我
                startActivityForResult<UserIntroduceActivity>(
                    105,
                    "content" to "${userNickSign.text}",
                    "from" to UserIntroduceActivity.USERCENTER
                )
            }
            R.id.saveBtn -> {
                updatePhotos(0)
            }

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


        //如果已经换了头像,并且要求强制替换头像
        Log.d("OKhttp", "${UserManager.getAvator().contains(adapter.data[0].url)}")
        if (adapter.data.isNotEmpty() && !UserManager.getAvator().contains(Constants.DEFAULT_EMPTY_AVATAR)
            && !UserManager.getAvator().contains(adapter.data[0].url) && UserManager.isNeedChangeAvator()
        ) {
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
            dialog.title.text = "保存内容"
            dialog.tip.text = "是否保存此次编辑的内容？"
            dialog.cancel.text = "放弃"
            dialog.confirm.text = "保存"
            dialog.cancel.onClick {
                setResult(Activity.RESULT_OK)
                finish()
            }
            dialog.confirm.onClick {
                updatePhotos(1)
                dialog.dismiss()
//                setResult(Activity.RESULT_OK)
//                super.onBackPressed()
            }
        } else {
            setResult(Activity.RESULT_OK)
            finish()
            if (adapter.data.isNotEmpty() && !UserManager.getAvator().contains(Constants.DEFAULT_EMPTY_AVATAR)
                && !UserManager.getAvator().contains(adapter.data[0].url)
                && (UserManager.getAccountDanger() || UserManager.getAccountDangerAvatorNotPass())
            ) { //账号异常
                UserManager.saveUserVerify(2)
                EventBus.getDefault().postSticky(AccountDangerEvent(AccountDangerDialog.VERIFY_ING))
            }
        }


    }


    /*------------------更新分数--------------------*/
    /**
     * 更新添加分数的状态
     * 资料新增后就要改变状态实现动画
     */
    private fun updateScoreStatus(view: View? = null, score: Int, update: Boolean? = false) {
        //会员的时候不显示添加分数
        if (view != null) {
            view.tvAddScoreSmile.text = "+$score"
            //如果view处于可见状态，说明之前没有加过分数，那这时就实现动画效果
            if (view.isVisible && update == true) {
                val translateAnimationRight = TranslateAnimation(
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0f,
                    TranslateAnimation.ABSOLUTE,
                    (view.width - view.ivAddScoreSmile.width - SizeUtils.dp2px(8f)).toFloat(),
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0f,
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0F

                )
                translateAnimationRight.duration = 500
                translateAnimationRight.fillAfter = true
                translateAnimationRight.interpolator = LinearInterpolator()

                val translateAnimationTop = TranslateAnimation(
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0f,
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0F,
                    TranslateAnimation.RELATIVE_TO_SELF,
                    0f,
                    TranslateAnimation.RELATIVE_TO_PARENT,
                    -0.7F

                )
                translateAnimationTop.duration = 500
                translateAnimationTop.fillAfter = true
                translateAnimationTop.interpolator = DecelerateInterpolator()
                val scaleAnimation = ScaleAnimation(1f, 0f, 1f, 0f)
                scaleAnimation.duration = 500
                scaleAnimation.fillAfter = true
                val alphaAnimation = AlphaAnimation(0F, 1F)
                alphaAnimation.duration = 500
                alphaAnimation.fillAfter = true
                val animationSet = AnimationSet(true)
                animationSet.addAnimation(translateAnimationTop)
                animationSet.addAnimation(scaleAnimation)
                animationSet.addAnimation(alphaAnimation)
                animationSet.fillAfter = true

                translateAnimationRight.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(p0: Animation?) {

                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        view.postDelayed({
                            view.isVisible = false
                        }, 200)
                    }

                    override fun onAnimationStart(p0: Animation?) {
                        view.setBackgroundResource(R.drawable.shape_rectangle_orange_11dp)
                        view.tvAddScoreSmile.startAnimation(animationSet)
                    }
                })
                view.ivAddScoreSmile.startAnimation(translateAnimationRight)
            }
        }

        if (update == true)
            setScroeProgress(score)


    }


    private var totalGetScore = 0//用户资料目前总的得分
    private fun setScroeProgress(score: Int) {
        val params = userFinishProgress.layoutParams as RelativeLayout.LayoutParams
        params.leftMargin = SizeUtils.dp2px(70F)
        params.rightMargin = SizeUtils.dp2px(15F)
        userFinishProgress.layoutParams = params


        totalGetScore += score //汇总每次的得分
        var progress =
            (totalGetScore * 1.0F / (data!!.score_rule!!.base_total) * 100).toInt()
        userScore20.text = "${progress}%"

        if (!UserManager.isUserVip()) {
            progress = (progress * 0.8).toInt()
        }

        if (Build.VERSION.SDK_INT >= 24) {
            userFinishProgress.setProgress(progress, true)
        } else {
            userFinishProgress.progress = progress
        }

        Log.d("setScroeProgress", "${userFinishProgress.progress}")

        val translate = ObjectAnimator.ofFloat(
            userScore20, "translationX",
            ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(70F + 15) - SizeUtils.dp2px(45F)) * userFinishProgress.progress * 1.0f / 100)
        )
        translate.duration = 100
        translate.start()

        if (!UserManager.isUserVip()) {
            userScore80.isVisible = (progress < 80)
        }

    }


    /**
     * 更新分数的位置
     */
    private fun updateScoreLayout() {
        val layoutmanager80 = userScore80.layoutParams as RelativeLayout.LayoutParams
        layoutmanager80.rightMargin =
            (SizeUtils.dp2px(15F) + (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(110F)) * 0.2F).toInt()
//        userScore80.layoutParams = layoutmanager80


    }


    /**
     * 展示条件选择器
     */
    private fun showConditionPicker(
        textview: TextView,
        scoreView: View?,
        score: Int,
        title: String,
        param: String,
        findTagBean: FindTagBean? = null,
        optionsItems1: MutableList<FindTagBean>
    ) {
        //条件选择器
        val pvOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v ->
                if (optionsItems1[options1].id == -1)
                    moreInfoAdapter.params[param] = optionsItems1[options1].title
                else
                    moreInfoAdapter.params[param] = optionsItems1[options1].id
                textview.text = "${optionsItems1[options1].title}"
                if (scoreView != null && scoreView.isVisible)
                    updateScoreStatus(scoreView, score, update = true)
                saveBtn.isEnabled = true
            })
            .setSubmitText("确定")
            .setTitleText(title)
            .setTitleColor(resources.getColor(R.color.colorBlack))
            .setTitleSize(16)
            .setDividerColor(resources.getColor(R.color.colorDivider))
            .setContentTextSize(20)
            .setDecorView((window.decorView.findViewById(android.R.id.content)) as ViewGroup)
            .setSubmitColor(resources.getColor(R.color.colorBlueSky1))
            .build<FindTagBean>()

        //身高默认选中，男170 女160
        if (findTagBean != null && !findTagBean.title.isNullOrEmpty()) {
            for (data in optionsItems1.withIndex()) {
                if (title == "身高") {
                    if (data.value.title == findTagBean.title) {
                        pvOptions.setSelectOptions(data.index)
                        break
                    }
                } else {
                    if (data.value.id == findTagBean.id) {
                        pvOptions.setSelectOptions(data.index)
                        break
                    }
                }
            }
        } else if (title == "身高") {
            if (UserManager.getGender() == 1) { //男
                pvOptions.setSelectOptions(175 - 60)
            } else {
                pvOptions.setSelectOptions(165 - 60)
            } //女
        }
        pvOptions.setPicker(optionsItems1)
        pvOptions.show()
    }


    /**
     * 展示日历
     */
    //错误使用案例： startDate.set(2013,1,1);  endDate.set(2020,12,1);
    //正确使用案例： startDate.set(2013,0,1);  endDate.set(2020,11,1);
    private fun showCalender(userBirth: TextView) {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        startDate.set(endDate.get(Calendar.YEAR) - 50, 0, 1)
        endDate.set(
            endDate.get(Calendar.YEAR) - 18,
            endDate.get(Calendar.MONTH),
            endDate.get(Calendar.DATE)
        )
        val clOptions = TimePickerBuilder(this, OnTimeSelectListener { date, v ->
            //            getZodiac
            userBirth.text =
                "${TimeUtils.date2String(
                    date,
                    SimpleDateFormat("yyyy-MM-dd")
                )}/${TimeUtils.getZodiac(date)}"
            savePersonalParams["birth"] = TimeUtils.date2Millis(date) / 1000L
//            savePersonalParams["birth"] = TimeUtils.date2Millis(date)
            isChange = true
            checkSaveEnable()
        })
            .setRangDate(startDate, endDate)
            .setDate(endDate)
            .setTitleText("请选择您的生日")
            .setTitleColor(Color.BLACK)//标题文字颜色
            .build()
        clOptions.show()
    }

}
