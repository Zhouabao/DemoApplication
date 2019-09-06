package com.example.demoapplication.ui.activity

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.CameraUpdateFactory
import com.amap.api.maps2d.model.BitmapDescriptorFactory
import com.amap.api.maps2d.model.LatLng
import com.amap.api.maps2d.model.Marker
import com.amap.api.maps2d.model.MarkerOptions
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.demoapplication.R
import com.example.demoapplication.ui.adapter.LocationAdapter
import com.example.demoapplication.utils.UserManager
import com.example.demoapplication.widgets.DividerItemDecoration
import com.kotlin.base.ext.onClick
import kotlinx.android.synthetic.main.activity_location.*

/**
 * 选择定位页面
 */
class LocationActivity : AppCompatActivity(), PoiSearch.OnPoiSearchListener, View.OnClickListener {
    override fun onClick(view: View) {
        when (view.id) {
            R.id.locationBtn -> {
                setResult(Activity.RESULT_OK, intent.putExtra("poiItem", adapter.data[adapter.checkPosition]))
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

        locationBtn.setOnClickListener(this)

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
            moveMapCamera(adapter.data[position].latLonPoint.latitude, adapter.data[position].latLonPoint.longitude)
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

        mLocationClient = AMapLocationClient(this)
        //获取定位结果
        mLocationClient?.setLocationListener {
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
                    doWhenLocationSuccess()
                    moveMapCamera(location!!.latitude, location!!.longitude)
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    ToastUtils.showShort("${it.errorCode},${it.errorInfo}")
                }
            }
        }

        if (null != mLocationClient) {
            mLocationClient!!.setLocationOption(mLocationOption)
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            mLocationClient!!.stopLocation()
            mLocationClient!!.startLocation()
        }
        mOnPoiSearchListener = this
    }

    private val zoom = 20f//地图缩放级别
    private fun moveMapCamera(latitude: Double, longitude: Double) {
        if (aMap != null) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoom))
        }
        if (marker == null) {
            marker = aMap.addMarker(
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

        marker!!.position = LatLng(latitude, longitude)
        aMap.invalidate()
    }


    private fun doWhenLocationSuccess() {
        //120200楼宇 190107街道
        mQuery = PoiSearch.Query("", "", UserManager.getCity())
        mQuery!!.pageSize = 100
        mQuery!!.pageNum = 0//设置查询第一页
        mPoiSearch = PoiSearch(this, mQuery)
        mPoiSearch!!.setOnPoiSearchListener(mOnPoiSearchListener)
        mPoiSearch!!.bound = PoiSearch.SearchBound(LatLonPoint(location!!.latitude, location!!.longitude), 100, true)
        mPoiSearch!!.searchPOIAsyn()// 异步搜索

    }

    private var marker: Marker? = null
    private lateinit var aMap: AMap
    private fun initMap(savedInstanceState: Bundle?) {
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        locationMap.onCreate(savedInstanceState)
        //初始化地图控制器对象
        aMap = locationMap.map
        aMap.mapType = AMap.MAP_TYPE_NORMAL
        aMap.moveCamera(CameraUpdateFactory.zoomTo(20F))
        aMap.uiSettings.isZoomControlsEnabled = false
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
                locationBtn.isEnabled = true
            }
        }
    }
}
