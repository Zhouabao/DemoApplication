package com.sdy.jitangapplication.ui.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.intention_matching_dialog.*
import kotlinx.android.synthetic.main.layout_marker_bg.view.userAvator
import kotlinx.android.synthetic.main.layout_marker_white_bg.view.*
import java.io.IOException
import java.io.InputStream

/**
 * 意向匹配成功dialog
 */
class IntentionMatchingDialog(val myContext: Context) : Dialog(myContext, R.style.MyDialog) {

    private var showMap: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intention_matching_dialog)
        initWindow()
        initView(savedInstanceState)
    }

    private fun initView(savedInstanceState: Bundle?) {
        if (showMap) {
            intentionLocationMap.isVisible = true
            intentionMatchCloseLocationBg.isVisible = false
            initMap(savedInstanceState)
            initLocation()
        } else {
            intentionLocationMap.isVisible = false
            intentionMatchCloseLocationBg.isVisible = true
        }

        iconClose.onClick {
            dismiss()
            intentionLocationMap.onDestroy()
        }
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        params?.windowAnimations = R.style.MyDialogCenterAnimation
//        params?.y = SizeUtils.dp2px(20F)

        window?.attributes = params

    }


    private lateinit var aMap: AMap
    private fun initMap(savedInstanceState: Bundle?) {
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        intentionLocationMap.onCreate(savedInstanceState)
        //初始化地图控制器对象
        aMap = intentionLocationMap.map
        aMap.mapType = AMap.MAP_TYPE_NORMAL
        aMap.moveCamera(CameraUpdateFactory.zoomTo(zoom))
        aMap.uiSettings.isZoomControlsEnabled = false
        var buffer1: ByteArray = byteArrayOf()
        var buffer2: ByteArray = byteArrayOf()
        var is1: InputStream? = null
        var is2: InputStream? = null
        try {
            is1 = context.assets.open("style.data")
            val lenght1 = is1.available()
            buffer1 = ByteArray(lenght1)
            is1.read(buffer1)
            is2 = context.assets.open("style_extra.data")
            val lenght2 = is2.available()
            buffer2 = ByteArray(lenght2)
            is2.read(buffer2)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                is1?.close()
                is2?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val customMapStyleOptions = CustomMapStyleOptions()
        customMapStyleOptions.isEnable = true
        customMapStyleOptions.styleData = buffer1
        customMapStyleOptions.styleExtraData = buffer2
        aMap.setCustomMapStyle(customMapStyleOptions)
    }


    private var mLocationClient: AMapLocationClient? = null
    //创建AMapLocationClientOption对象
    private val mLocationOption by lazy { AMapLocationClientOption() }

    private fun initLocation() {
        //设置定位模式为高精度模式
        mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        //获取近3S精度最高的一次定位结果
        mLocationOption.isOnceLocationLatest = true
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.isNeedAddress = true
        //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.httpTimeOut = 20000
        //关闭缓存机制
        mLocationOption.isLocationCacheEnable = false
        // 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
        mLocationOption.locationPurpose = AMapLocationClientOption.AMapLocationPurpose.SignIn

        mLocationClient = AMapLocationClient(myContext)
        //获取定位结果
        mLocationClient?.setLocationListener {
            if (it != null) {
                if (mLocationClient != null) mLocationClient!!.stopLocation()
                if (it.errorCode == 0) {
                    moveMapCamera(it!!.latitude, it!!.longitude)
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                }
            }
        }

        if (null != mLocationClient) {
            mLocationClient!!.setLocationOption(mLocationOption)
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            mLocationClient!!.stopLocation()
            mLocationClient!!.startLocation()
        }
    }

    private var markerMine: Marker? = null
    private var markerOther: Marker? = null
    private val zoom = 18f//地图缩放级别

    private fun moveMapCamera(latitude: Double, longitude: Double) {
        Glide.with(myContext)
            .asBitmap()
            .load(UserManager.getAvator())
            .thumbnail(0.2f)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .centerCrop()
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val markerBg = View.inflate(myContext, R.layout.layout_marker_white_bg, null)
                    markerBg.userBg.setBackgroundResource(R.drawable.shape_oval_orange_dash)
                    markerBg.userAvator.setImageBitmap(resource)
                    val bitmap = convertViewToBitmap(markerBg)
                    markerMine = aMap.addMarker(
                        MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                            .draggable(false)
                            .position(LatLng(latitude, longitude))
                            .anchor(0.5F, 0.5F)
                    )
                }
            })

        Glide.with(myContext)
            .asBitmap()
            .load(UserManager.getAvator())
            .thumbnail(0.2f)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .centerCrop()
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val markerBg = View.inflate(myContext, R.layout.layout_marker_white_bg, null)
                    markerBg.userAvator.setImageBitmap(resource)
                    markerBg.userBg.setBackgroundResource(R.drawable.shape_oval_meired_dash)
                    val bitmap = convertViewToBitmap(markerBg)
                    markerOther = aMap.addMarker(
                        MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                            .draggable(false)
                            .position(LatLng(34.7466, 113.625367))
                            .anchor(0.5F, 0.5F)
                    )
                }
            })

        // 设置所有maker显示在当前可视区域地图中
        val bounds = LatLngBounds.Builder()
            .include(LatLng(UserManager.getlatitude().toDouble(), UserManager.getlongtitude().toDouble()))
            .include(LatLng(34.7466, 113.625367)).build()
        if (aMap != null)
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))

        if (aMap != null) {
//            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoom))
        }

    }

    fun convertViewToBitmap(view: View): Bitmap {

        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        view.buildDrawingCache()

        return view.drawingCache

    }
}
