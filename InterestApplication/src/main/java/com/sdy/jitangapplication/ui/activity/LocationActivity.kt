package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.animation.Interpolator
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.TranslateAnimation
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.ui.adapter.LocationAdapter
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_location.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import kotlin.math.sqrt

/**
 * 选择定位页面
 */
class LocationActivity : BaseActivity(), PoiSearch.OnPoiSearchListener, View.OnClickListener,
    AMapLocationListener {
    override fun onClick(view: View) {
        when (view.id) {
            R.id.rightBtn1 -> {
                setResult(
                    Activity.RESULT_OK,
                    intent.putExtra("poiItem", adapter.data[adapter.checkPosition])
                )
                finish()
            }
        }

    }

    private var mLocationClient: AMapLocationClient? = null

    //创建AMapLocationClientOption对象
    private val mLocationOption by lazy { AMapLocationClientOption() }
    private var location: AMapLocation? = null

    //关键字搜素
    private var mQuery: PoiSearch.Query? = null
    private var mPoiSearch: PoiSearch? = null

    //Poi搜索监听器
    private var mOnPoiSearchListener: PoiSearch.OnPoiSearchListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        initView()
        initMap(savedInstanceState)
        initLocation()
    }

    private val adapter by lazy { LocationAdapter() }
    private fun initView() {
        btnBack.onClick {
            finish()
        }
        hotT1.text = "选择地址"
        rightBtn1.text = "确定"
        rightBtn1.isVisible = true
        rightBtn1.setOnClickListener(this)



        locationRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        locationRv.addItemDecoration(
            DividerItemDecoration(
                this, DividerItemDecoration.HORIZONTAL_LIST,
                SizeUtils.dp2px(1F),
                resources.getColor(R.color.colorDivider)
            )
        )
        locationRv.adapter = adapter
        adapter.setOnItemClickListener { _, view, position ->
            adapter.checkPosition = position
            adapter.notifyDataSetChanged()
            aMap.clear()
            moveMapCamera(
                adapter.data[position].latLonPoint.latitude,
                adapter.data[position].latLonPoint.longitude
            )
        }
    }


    private val myLocationStyle: MyLocationStyle by lazy {
        MyLocationStyle().apply {
            interval(2000)
        }
    }


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
        mLocationOption.isLocationCacheEnable = true
        // 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
        mLocationOption.locationPurpose = AMapLocationClientOption.AMapLocationPurpose.SignIn


        if (mLocationClient == null) {

            mLocationClient = AMapLocationClient(this)
            //获取定位结果
            mLocationClient?.setLocationListener(this)


            mLocationClient!!.setLocationOption(mLocationOption)
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            mLocationClient!!.stopLocation()
            mLocationClient!!.startLocation()
        }
        mOnPoiSearchListener = this

    }


    fun startJumpAnimation() {
        val latLng = screenMoveMarker.position
        val point = aMap.projection.toScreenLocation(latLng)
        point.y = point.y - SizeUtils.dp2px(125F)


        val target = aMap.projection.fromScreenLocation(point)
        val animation = TranslateAnimation(target)
        animation.setInterpolator(object : Interpolator {
            override fun getInterpolation(it: Float): Float {
                return (if (it <= 0.5f) {
                    0.5F - 2 * (0.5f - it) * (0.5f - it)
                } else {
                    0.5F - sqrt((it - 0.5F) * (1.5f - it))
                })
            }
        })
        animation.setDuration(600)
        screenMoveMarker.setAnimation(animation)
        screenMoveMarker.startAnimation()
    }

    private val zoom = 20f//地图缩放级别
    private fun moveMapCamera(latitude: Double, longitude: Double) {
        doWhenLocationSuccess(latitude, longitude)
        if (aMap != null) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoom))
        }
//        screenMoveMarker!!.position = LatLng(latitude, longitude)


    }


    private fun doWhenLocationSuccess(latitude: Double, longitude: Double) {
        //120200楼宇 190107街道
//        地名地址信息|道路附属设施|公共设施
        mQuery = PoiSearch.Query("地名地址信息|道路附属设施|公共设施", "", UserManager.getCity())
        mQuery!!.pageSize = 100
        mQuery!!.pageNum = 0//设置查询第一页
        mPoiSearch = PoiSearch(this, mQuery)
        mPoiSearch!!.setOnPoiSearchListener(mOnPoiSearchListener)
        mPoiSearch!!.bound =
            PoiSearch.SearchBound(LatLonPoint(latitude, longitude), 100, true)
        mPoiSearch!!.searchPOIAsyn()// 异步搜索

    }

    private val screenMoveMarker: Marker by lazy {
        aMap.addMarker(
            MarkerOptions()
                .icon(
                    BitmapDescriptorFactory.fromBitmap(
                        BitmapFactory.decodeResource(
                            resources,
                            R.drawable.icon_map
                        )
                    )
                )
                .draggable(false)
        )
    }


    fun addGrowMarker() {
        aMap.addMarker(
            MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .position(LatLng(location!!.latitude, location!!.longitude))
        )
    }

    private lateinit var aMap: AMap
    private fun initMap(savedInstanceState: Bundle?) {
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        locationMap.onCreate(savedInstanceState)
        //初始化地图控制器对象
        aMap = locationMap.map
        aMap.mapType = AMap.MAP_TYPE_NORMAL
        aMap.moveCamera(CameraUpdateFactory.zoomTo(20F))
        aMap.uiSettings.isZoomControlsEnabled = false
        aMap.uiSettings.setAllGesturesEnabled(true)
        aMap.setOnMarkerDragListener(object : AMap.OnMarkerDragListener {
            override fun onMarkerDragEnd(p0: Marker) {
                moveMapCamera(p0.position.latitude, p0.position.longitude)
            }

            override fun onMarkerDragStart(p0: Marker) {
            }

            override fun onMarkerDrag(p0: Marker) {
            }

        })

        aMap.uiSettings.isMyLocationButtonEnabled = true
        aMap.isMyLocationEnabled = true
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE)


        aMap.myLocationStyle = myLocationStyle//设置定位蓝点的Style
        aMap.isMyLocationEnabled = true //设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。


//        aMap.setOnMapClickListener(this) // 对amap添加单击地图事件监听器

//        aMap.setOnMapLongClickListener(this) // 对amap添加长按地图事件监听器
        // 对amap添加移动地图事件监听器
        aMap.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChangeFinish(p0: CameraPosition) {
                moveMapCamera(p0.target.latitude, p0.target.longitude)
                startJumpAnimation()
            }

            override fun onCameraChange(p0: CameraPosition) {

            }

        })

//        aMap.setOnMapTouchListener(this) // 对amap添加触摸地图事件监听器


    }

    override fun onPause() {
        super.onPause()
        locationMap.onPause()
    }

    override fun onResume() {
        super.onResume()
        locationMap.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationMap.onDestroy()
        if (null != mLocationClient) {
            mLocationClient!!.onDestroy();
        }
    }

    override fun onPoiItemSearched(p0: PoiItem?, p1: Int) {
    }

    override fun onPoiSearched(result: PoiResult?, rCode: Int) {
        if (rCode == 1000) {
            if (result != null && result.query != null) {
//                mList.clear()
//                mList.addAll(result.pois)
                adapter.setNewData(result.pois)
                //PoiItem(java.lang.String id, LatLonPoint point, java.lang.String title, java.lang.String snippet)
                adapter.addData(0, PoiItem("", LatLonPoint(0.0, 0.0), "不显示位置", ""))
                adapter.addData(
                    1,
                    PoiItem(
                        location!!.buildingId,
                        LatLonPoint(location!!.latitude, location!!.longitude),
                        location!!.city,
                        ""
                    )
                )
                rightBtn1.isEnabled = true
            }
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

    override fun onLocationChanged(it: AMapLocation?) {
        if (it != null) {
            if (mLocationClient != null) mLocationClient!!.stopLocation()
            if (it.errorCode == 0) {
                location = it
                //可在其中解析amapLocation获取相应内容。
                UserManager.saveLocation(
                    "${it.latitude}",
                    "${it.longitude}",
                    it.province,
                    it.city,
                    it.district,
                    it.cityCode
                )
                moveMapCamera(location!!.latitude, location!!.longitude)
                addGrowMarker()
            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
            }
        }
    }
}
