package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
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
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.event.ReminderScoreEvent
import com.sdy.jitangapplication.event.UserCenterEvent
import com.sdy.jitangapplication.model.MyPhotoBean
import com.sdy.jitangapplication.model.NewJobBean
import com.sdy.jitangapplication.model.ProvinceBean
import com.sdy.jitangapplication.model.UserInfoSettingBean
import com.sdy.jitangapplication.presenter.UserInfoSettingsPresenter
import com.sdy.jitangapplication.presenter.view.UserInfoSettingsView
import com.sdy.jitangapplication.ui.adapter.UserPhotoAdapter
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.ui.dialog.ReminderScoreDialog
import com.sdy.jitangapplication.utils.GetJsonDataUtil
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import com.sdy.jitangapplication.widgets.OnRecyclerItemClickListener
import kotlinx.android.synthetic.main.activity_new_user_info_settings.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.dialog_delete_photo.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_add_score.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivityForResult
import org.json.JSONArray
import top.zibin.luban.OnCompressListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NewUserInfoSettingsActivity : BaseMvpActivity<UserInfoSettingsPresenter>(), UserInfoSettingsView,
    OnItemDragListener, View.OnClickListener {
    companion object {
        const val IMAGE_SIZE = 9
        const val REPLACE_REQUEST = 187
        private const val MSG_LOAD_SUCCESS = 100
        private const val MSG_LOAD_FAILED = -100
        private const val MSG_LOAD_DATA = 1
    }

    val dialog by lazy { DeleteDialog(this) }
    val params by lazy { hashMapOf("token" to UserManager.getToken(), "accid" to UserManager.getAccid()) }
    private var isChange = false
    private var photos: MutableList<MyPhotoBean?> = mutableListOf()
    private var originalPhotos: MutableList<MyPhotoBean?> = mutableListOf()//用于对比用户是否改变过相册信息
    private val adapter by lazy { UserPhotoAdapter(datas = mutableListOf()) }
    private var data: UserInfoSettingBean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_user_info_settings)
        initView()
        updateScoreLayout()

        mPresenter.personalInfo(params)
        setSwipeBackEnable(false)
//        adapter.addData(MyPhotoBean(type = MyPhotoBean.COVER))
//        mHandler.sendEmptyMessage(MSG_LOAD_DATA)
    }


    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = UserInfoSettingsPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        btnBack.setOnClickListener(this)
        userNickName.setOnClickListener(this)
//        userGender.setOnClickListener(this)
        userBirth.setOnClickListener(this)
        userHeight.setOnClickListener(this)
        userNickSign.setOnClickListener(this)
        userLoveStatus.setOnClickListener(this)
        userHomeTown.setOnClickListener(this)
        userLiveNow.setOnClickListener(this)
        userJob.setOnClickListener(this)
        userFriendsAim.setOnClickListener(this)
        userSchool.setOnClickListener(this)
        userDrink.setOnClickListener(this)
        userSmoke.setOnClickListener(this)
        userEat.setOnClickListener(this)
        saveBtn.setOnClickListener(this)
        //userScore20.setOnClickListener(this)
        userScore80.setOnClickListener(this)
        userScoreVip.setOnClickListener(this)


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
                                                        PictureMimeType.ofImage()
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
                            PictureMimeType.ofImage()
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
                userScore80.tvAddScoreSmile.text = "${data.score_rule.base_total + data.score_rule.base}"
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

            updateScoreStatus(userScoreAboutMe, data.score_rule?.about ?: 0, !data.sign.isNullOrEmpty())

            if (data.emotion_state > 0 && data.emotion_list.size > data.emotion_state - 1) {
                userLoveStatus.text = data.emotion_list[data.emotion_state - 1]
                userScoreEmotion.isVisible = false
            } else
                userScoreEmotion.isVisible = true
            updateScoreStatus(userScoreEmotion, data.score_rule?.emotion ?: 0, data.emotion_state > 0)
            if (data.height > 0) {
                userHeight.text = "${data.height}"
                userScoreHeight.isVisible = false
            } else
                userScoreHeight.isVisible = true

            updateScoreStatus(userScoreHeight, data.score_rule?.height ?: 0, data.height > 0)

            if (!data.hometown.isNullOrEmpty()) {
                userHomeTown.text = data.hometown
                userScoreHomeTown.isVisible = false
            } else
                userScoreHomeTown.isVisible = true
            updateScoreStatus(userScoreHomeTown, data.score_rule?.hometown ?: 0, !data.hometown.isNullOrEmpty())
            if (!data.present_address.isNullOrEmpty()) {
                userLiveNow.text = data.present_address
                userScoreLiveNow.isVisible = false
            } else
                userScoreLiveNow.isVisible = true
            updateScoreStatus(
                userScoreLiveNow,
                data.score_rule?.present_address ?: 0,
                !data.present_address.isNullOrEmpty()
            )


            if (!data.personal_job.isNullOrEmpty()) {
                userJob.text = data.personal_job
                userScoreJob.isVisible = false
            } else
                userScoreJob.isVisible = true
            updateScoreStatus(userScoreJob, data.score_rule?.personal_job ?: 0, !data.personal_job.isNullOrEmpty())

            if (data.making_friends > 0 && data.making_friends_list.size > data.making_friends - 1) {
                userFriendsAim.text = data.making_friends_list[data.making_friends - 1]
                userScoreFriendsAim.isVisible = false
            } else
                userScoreFriendsAim.isVisible = true
            updateScoreStatus(userScoreFriendsAim, data.score_rule?.making_friends ?: 0, data.making_friends > 0)

            if (!data.personal_school.isNullOrEmpty()) {
                userSchool.text = data.personal_school
                userScoreSchool.isVisible = false
            } else
                userScoreSchool.isVisible = true

            updateScoreStatus(
                userScoreSchool,
                data.score_rule?.personal_school ?: 0,
                !data.personal_school.isNullOrEmpty()
            )


            if (data.personal_drink > 0 && data.personal_drink_list.size > data.personal_drink - 1) {
                userDrink.text = data.personal_drink_list[data.personal_drink - 1]
                userScoreDrink.isVisible = false
            } else
                userScoreDrink.isVisible = true

            updateScoreStatus(userScoreDrink, data.score_rule?.personal_drink ?: 0, data.personal_drink > 0)



            if (data.personal_smoke > 0 && data.personal_smoke_list.size > data.personal_smoke - 1) {
                userSmoke.text = data.personal_smoke_list[data.personal_smoke - 1]
                userScoreSmoke.isVisible = false
            } else
                userScoreSmoke.isVisible = true
            updateScoreStatus(userScoreSmoke, data.score_rule?.personal_smoke ?: 0, data.personal_smoke > 0)

            if (data.personal_schedule > 0 && data.personal_schedule_list.size > data.personal_schedule - 1) {
                userEat.text = data.personal_schedule_list[data.personal_schedule - 1]
                userScoreEat.isVisible = false
            } else
                userScoreEat.isVisible = true
            updateScoreStatus(userScoreEat, data.score_rule?.personal_schedule ?: 0, data.personal_schedule > 0)


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
            adapter.addData(MyPhotoBean(type = MyPhotoBean.COVER, photoScore = data.score_rule?.photo ?: 0))

            refreshLayout()

            mHandler.sendEmptyMessage(MSG_LOAD_DATA)

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
                                                PictureMimeType.ofImage()
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
                    CommonFunction.onTakePhoto(this, 1, REPLACE_REQUEST, PictureMimeType.ofImage())
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
        if (hasChangePhotos) {
            val photosId = mutableListOf<Int?>()
            for (data in photos.withIndex()) {
                if (data.value?.type == MyPhotoBean.PHOTO) {
                    photosId.add(data.value?.id)
                }
            }
            mPresenter.addPhotoV2(savePersonalParams, UserManager.getToken(), UserManager.getAccid(), photosId, type)
        } else {
            mPresenter.addPhotoV2(
                savePersonalParams,
                UserManager.getToken(),
                UserManager.getAccid(),
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
            0 + (SizeUtils.dp2px(15F) + (16 / 9F * (ScreenUtils.getScreenWidth() - 4 * SizeUtils.dp2px(15F)) / 3).toInt()) * row


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

    private fun updateUserInfo(key: String, value: Any) {
        val params = hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            key to value
        )
        mPresenter.savePersonal(params)
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

    private fun showJobPicker() {
        val secondJobs = mutableListOf<MutableList<NewJobBean>>()
        for (job in newJobs) {
            secondJobs.add(job.son ?: mutableListOf())
        }
        showConditionPicker<NewJobBean>(userJob, userScoreJob, 2, "请选择您的职业", "personal_job", newJobs, secondJobs)
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
                104 -> {//学校
                    if (data?.getStringExtra("schoolBean") != null) {

                        if (data.getIntExtra(
                                "type",
                                ChooseSchoolActivity.CHOOSE_SCHOOL
                            ) == ChooseSchoolActivity.CHOOSE_SCHOOL
                        ) {
                            userSchool.text = data.getStringExtra("schoolBean")
                            savePersonalParams["personal_school"] = data.getStringExtra("schoolBean")
                            if (userScoreSchool.isVisible)
                                updateScoreStatus(
                                    userScoreSchool,
                                    this.data!!.score_rule?.personal_school ?: 0,
                                    update = true
                                )
                        } else {
                            userJob.text = data.getStringExtra("schoolBean")
                            savePersonalParams["personal_job"] = data.getStringExtra("schoolBean")
                            if (userScoreJob.isVisible)
                                updateScoreStatus(
                                    userScoreJob,
                                    this.data!!.score_rule?.personal_job ?: 0,
                                    update = true
                                )
                        }
                        isChange = true
                        checkSaveEnable()
                    }
                }
                105 -> {//关于我
                    userNickSign.text = data?.getStringExtra("content")
                    savePersonalParams["sign"] = data?.getStringExtra("content")

                    if (userScoreAboutMe.isVisible)
                        updateScoreStatus(userScoreAboutMe, this.data!!.score_rule?.about ?: 0, update = true)
                    isChange = true
                    checkSaveEnable()
                }
                REPLACE_REQUEST,//替换头像
                PictureConfig.CHOOSE_REQUEST//icon_verify_not_pass
                -> {
                    if (data != null) {
                        if (!PictureSelector.obtainMultipleResult(data).isNullOrEmpty()) {
                            loading.show()
                            uploadPicture(
                                requestCode == REPLACE_REQUEST, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    PictureSelector.obtainMultipleResult(data)[0].androidQToPath
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
                ReminderScoreDialog(this, ((data?.score_rule?.base_total ?: 0) + (data?.score_rule?.base ?: 0))).show()
//                userScore80.setImageResource(R.drawable.icon_eighty_click)
            }
            R.id.userNickName -> {//昵称
                startActivityForResult<NickNameActivity>(102, "type" to 1, "content" to userNickName.text.toString())
            }

            R.id.userGender -> {//性别
                showConditionPicker<String>(
                    userGender,
                    null,
                    0,
                    "请选择您的性别",
                    "gender",
                    mutableListOf("男", "女")
                )
            }
            R.id.userBirth -> { //生日
                showCalender(userBirth)
            }

            R.id.userHeight -> {//身高
                val heights = mutableListOf<Int>()
                for (i in 60 until 250) {
                    heights.add(i)
                }
                showConditionPicker(
                    userHeight,
                    userScoreHeight,
                    2,
                    "请选择您的身高",
                    "height",
                    heights
                )
            }
            R.id.userNickSign -> {//关于我
                startActivityForResult<AboutMeActivity>(105, "content" to data?.sign)

            }
            R.id.userLoveStatus -> {//情感状态
                showConditionPicker(
                    userLoveStatus,
                    userScoreEmotion,
                    2,
                    "请选择您的感情状态",
                    "emotion_state",
                    data?.emotion_list ?: mutableListOf()
                )
            }
            R.id.userHomeTown -> {//家乡
                showConditionPicker(
                    userHomeTown,
                    userScoreHomeTown,
                    2,
                    "请选择您的家乡",
                    "hometown",
                    provinceItems,
                    cityItems
                )
            }
            R.id.userLiveNow -> {//现居地
                showConditionPicker(
                    userLiveNow,
                    userScoreLiveNow,
                    2,
                    "请选择您的现居地",
                    "present_address",
                    provinceItems,
                    cityItems
                )
            }

            R.id.userJob -> {//职业
                startActivityForResult<ChooseSchoolActivity>(104, "type" to ChooseSchoolActivity.CHOOSE_JOB)
                // startActivityForResult<MyJobActivity>(103, "job" to userJob.text.toString())
            }

            R.id.userFriendsAim -> {//交友目的
                showConditionPicker(
                    userFriendsAim,
                    userScoreFriendsAim,
                    2,
                    "请选择您的交友目的",
                    "making_friends",
                    data?.making_friends_list ?: mutableListOf()
                )
            }
            R.id.userSchool -> {//学校
                startActivityForResult<ChooseSchoolActivity>(104, "type" to ChooseSchoolActivity.CHOOSE_SCHOOL)
            }
            R.id.userDrink -> {//饮酒
                showConditionPicker(
                    userDrink,
                    userScoreDrink,
                    2,
                    "请选择您的饮酒状态",
                    "personal_drink",
                    data?.personal_drink_list ?: mutableListOf()
                )
            }
            R.id.userSmoke -> {//抽烟
                showConditionPicker(
                    userSmoke,
                    userScoreSmoke,
                    2,
                    "请选择您的抽烟状态",
                    "personal_smoke",
                    data?.personal_smoke_list ?: mutableListOf()
                )
            }
            R.id.userEat -> {//饮食
                showConditionPicker(
                    userEat,
                    userScoreEat,
                    2,
                    "请选择您的作息习惯",
                    "personal_schedule",
                    data?.personal_schedule_list ?: mutableListOf()
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

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
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
        }


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onResetScoreEvent(dialogEvent: ReminderScoreEvent) {
        when (dialogEvent.score) {
            20 -> {
//                userScore20.setImageResource(R.drawable.icon_twenty_unclick)
            }
            80 -> {
//                userScore80.setImageResource(R.drawable.icon_eighty_unclick)
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

        val layoutmanager20 = userScore20.layoutParams as RelativeLayout.LayoutParams
        layoutmanager20.leftMargin = if (UserManager.isUserVip() && userFinishProgress.progress == 100) {
            ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) - SizeUtils.dp2px(50F)
        } else  //左边距+进度条宽度-自身宽度
            SizeUtils.dp2px(70F) +
                    ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(70F + 15))
                            * userFinishProgress.progress * 1.0f / 100).toInt() - if (progress >= 80 * 0.8F) {
                SizeUtils.dp2px(60F * 0.9F)
            } else {
                0
            }
        userScore20.layoutParams = layoutmanager20

        if (!UserManager.isUserVip())
            userScore80.isVisible = (progress != 80)

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

    private fun showUploadMorePhotos() {
        //使用AnimationUtils类的静态方法loadAnimation()来加载XML中的动画XML文件
        val animation = AnimationUtils.loadAnimation(this, R.anim.dialog_center_in)
        val animationOut = AnimationUtils.loadAnimation(this, R.anim.dialog_center_exit)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                addPhotoTip.isVisible = true
                Thread.sleep(200L)
                addPhotoTip.startAnimation(animationOut)
            }

            override fun onAnimationStart(p0: Animation?) {
            }

        })
        animationOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                addPhotoTip.isVisible = false
            }

            override fun onAnimationStart(p0: Animation?) {
            }

        })
        addPhotoTip.startAnimation(animation)
    }


    /*---------------------展示省市区等各种选择器----------------------*/
    private var newJobs = mutableListOf<NewJobBean>()
    private var provinceItems = mutableListOf<String>()
    private val cityItems = mutableListOf<MutableList<String>>()
    private val areaItems = mutableListOf<MutableList<MutableList<String>>>()
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_LOAD_DATA -> {
                    Thread(Runnable {
                        //子线程解析省市区数据
                        initJsonData()
                    }).start()
                }
            }
        }
    }

    private fun initJsonData() {//解析数据

        /**
         * 注意：assets 目录下的Json文件仅供参考，实际使用可自行替换文件
         * 关键逻辑在于循环体
         *
         */
        val provinceList = parseData(GetJsonDataUtil().getJson(this, "province.json"))//获取assets目录下的json文件,数据用Gson 转成实体

        /**
         * 添加省份数据
         *
         * 注意：如果是添加的JavaBean实体，则实体类需要实现 IPickerViewData 接口，
         * PickerView会通过getPickerViewText方法获取字符串显示出来。
         */
        for (i in provinceList.indices) {//遍历省份
            provinceItems.add(provinceList[i].name)
            val cityList = ArrayList<String>()//该省的城市列表（第二级）
            val provinceAreaList = ArrayList<ArrayList<String>>()//该省的所有地区列表（第三极）
            for (c in 0 until provinceList[i].city.size) {//遍历该省份的所有城市
                val cityName = provinceList[i].city[c].name
                cityList.add(cityName)//添加城市
                val cityAreaList = ArrayList<String>()//该城市的所有地区列表
                cityAreaList.addAll(provinceList[i].city[c].area)
                provinceAreaList.add(cityAreaList)//添加该省所有地区数据
            }

            /**
             * 添加城市数据
             */
            cityItems.add(cityList)

            /**
             * 添加地区数据
             */
            areaItems.add(provinceAreaList.toMutableList())
        }

        mHandler.sendEmptyMessage(MSG_LOAD_SUCCESS)

    }


    private fun parseData(result: String): MutableList<ProvinceBean> {//Gson 解析rovro
        val detail = mutableListOf<ProvinceBean>()
        try {
            val data = JSONArray(result)
            val gson = Gson()
            for (i in 0 until data.length()) {
                val entity = gson.fromJson<ProvinceBean>(data.optJSONObject(i).toString(), ProvinceBean::class.java)
                detail.add(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mHandler.sendEmptyMessage(MSG_LOAD_FAILED)
        }

        return detail
    }


    /**
     * 展示条件选择器
     */
    private fun <T> showConditionPicker(
        textview: TextView,
        scoreView: View?,
        score: Int,
        title: String,
        param: String,
        optionsItems1: MutableList<T>,
        optionsItems2: MutableList<MutableList<T>>? = mutableListOf(),
        optionsItems3: MutableList<MutableList<MutableList<T>>>? = mutableListOf()
    ) {
        //条件选择器
        val pvOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v ->
                /*
                nickname复制  [string]		更改昵称时传
                birth	[int]		生日时间戳（秒）
                face	[string]		人脸识别图片
                sign	[string]		更签名称时传
                personal_school	[string]	是	学校名(选择）

                hometown	    [string]	是	家乡地址(三级)
                presentaddress	[string]	是	现居住地址(三级)
                personal_job	[string]	是	职业(二级，对象)
                height	    [int]	是	身高（cm）
                emotionState	[int]	是	感情状态 0 没有选择 1单身 2热恋 3已婚 4保密
                making_friends	[int]	是	交友目的 0 未填写 1交朋友 2找对象 3接触感兴趣的
                personal_drink	[int]	是	喝酒状况 0 未填写 1偶尔 2 经常 3不喝
                personal_smoke	[int]	是	抽烟状况 0未填写 1偶尔 2 经常 3不喝
                personal_schedule	[int]	是	饮食喜好 0 未填写 1素食主义者*/
                if (optionsItems2.isNullOrEmpty()) {
                    if (param == "height") {
                        savePersonalParams[param] = optionsItems1[options1] as Int
                    } else {
                        savePersonalParams[param] = (options1 + 1)
                    }
                    textview.text = "${optionsItems1[options1]}"
                } else if (optionsItems3.isNullOrEmpty()) {
                    if (optionsItems2[options1][options2] is NewJobBean) {
                        savePersonalParams[param] =
                            "${(optionsItems1[options1] as NewJobBean).title}.${(optionsItems2[options1][options2] as NewJobBean).title}"
                    } else {
                        savePersonalParams[param] = "${optionsItems1[options1]}${optionsItems2[options1][options2]}"
                    }
                    textview.text = "${savePersonalParams[param]}"
                } else {
                    savePersonalParams[param] =
                        if (optionsItems1[options1] == optionsItems2[options1][options2]) { //直辖市，省市名称一样，就去重
                            "${optionsItems1[options1]} ${optionsItems3[options1][options2][options3]}"
                        } else {
                            "${optionsItems1[options1]} ${optionsItems2[options1][options2]} ${optionsItems3[options1][options2][options3]}"
                        }
                    textview.text = "${savePersonalParams[param]}"
                }
                isChange = true
                checkSaveEnable()

                if (scoreView != null && scoreView.isVisible)
                    updateScoreStatus(scoreView, score, update = true)
            })
            .setSubmitText("确定")
            .setTitleText(title)
            .setTitleColor(resources.getColor(R.color.colorBlack))
            .setTitleSize(16)
            .setDividerColor(resources.getColor(R.color.colorDivider))
            .setContentTextSize(20)
            .setDecorView(window.decorView.findViewById(android.R.id.content) as ViewGroup)
            .setSubmitColor(resources.getColor(R.color.colorBlueSky1))
            .build<T>()

        //身高默认选中，男170 女160
        if (param == "height")
            if (data?.gender == 1) //男
                pvOptions.setSelectOptions(170 - 60)
            else if (data?.gender == 2)//女
                pvOptions.setSelectOptions(160 - 60)

        if (optionsItems2.isNullOrEmpty()) {
            pvOptions.setPicker(optionsItems1)
        } else if (optionsItems3.isNullOrEmpty()) {
            pvOptions.setPicker(optionsItems1, optionsItems2)
        } else {
            pvOptions.setPicker(optionsItems1, optionsItems2, optionsItems3)
        }
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
        endDate.set(endDate.get(Calendar.YEAR) - 18, endDate.get(Calendar.MONTH), endDate.get(Calendar.DATE))
        val clOptions = TimePickerBuilder(this, OnTimeSelectListener { date, v ->
            //            getZodiac
            userBirth.text =
                "${TimeUtils.date2String(date, SimpleDateFormat("yyyy-MM-dd"))}/${TimeUtils.getZodiac(date)}"
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
