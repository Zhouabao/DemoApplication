package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.text.TextUtils
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LocationBean
import com.sdy.jitangapplication.nim.uikit.api.model.location.LocationProvider
import com.sdy.jitangapplication.ui.adapter.LocationAdapter
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_location.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity
import java.util.*
import kotlin.concurrent.thread

/**
 * 选择定位页面
 */
class LocationActivity : BaseActivity(), View.OnClickListener, OnMapReadyCallback,
    OnFailureListener, OnSuccessListener<FindAutocompletePredictionsResponse>,
    GoogleMap.OnMapClickListener {
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mLastLocation: Location
    private lateinit var mLocationRequest: LocationRequest
    private val adapter by lazy { LocationAdapter() }


    companion object {
        lateinit var callback: LocationProvider.Callback
        fun start(context: Context, callback: LocationProvider.Callback? = null) {
            if (callback != null)
                LocationActivity.callback = callback
            context.startActivity<LocationActivity>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        initView()
        initMap(savedInstanceState)

    }

    private val bottomSheetBehavior by lazy { BottomSheetBehavior.from(scrollLocationSv) }
    private var expand = false
    private fun initView() {
        btnBack.onClick {
            finish()
        }
        hotT1.text = getString(R.string.choose_location)
        rightBtn1.text = getString(R.string.ok)
        rightBtn1.isVisible = true
        rightBtn1.setOnClickListener(this)
        backToMyLocationBtn.setOnClickListener(this)

        bottomSheetBehavior.peekHeight = SizeUtils.dp2px(263F)
        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                Log.d("slideOffset", "slideOffset=====${slideOffset}")
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    KeyboardUtils.hideSoftInput(this@LocationActivity)
                    expandBtn.setImageResource(R.drawable.icon_search_expand)
                } else {
                    expandBtn.setImageResource(R.drawable.icon_search_collapsed)
                }
//                val params = bottomSheet.layoutParams
//                if (params.height > SizeUtils.dp2px(432F)) {
//                    params.height = SizeUtils.dp2px(432F)
//                    bottomSheet.layoutParams = params
//                }
//                Log.d("slideOffset", "slideOffset=====${params.height}")
            }

        })



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
//            if (adapter.checkPosition != position) {
            adapter.checkPosition = position
            adapter.notifyDataSetChanged()
            if (position != 0 && adapter.data[position].latitude != 0.0) {
                isTouch = false
                latLng = LatLng(adapter.data[position].latitude, adapter.data[position].longitude)
                moveMapCamera(
                    adapter.data[position].latitude,
                    adapter.data[position].longitude
                )
            }
            searchLocation.clearFocus()
//            }
        }


        searchLocation.setOnQueryTextFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                KeyboardUtils.hideSoftInput(this)
            }
        }


        searchLocation.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                keyWordSearch(query ?: "")
                searchLocation.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                keyWordSearch(newText ?: "")
                return true
            }

        })
    }

    private var isTouch = false
    private lateinit var placeClient: PlacesClient
    private fun initMap(savedInstanceState: Bundle?) {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
//        locationMap.onCreate(savedInstanceState)
        mapFragment!!.getMapAsync(this)

        Places.initialize(this, getString(R.string.google_map_api_key))
        placeClient = Places.createClient(this)
    }


    fun findPlaceById(placeId: String, placeName: String) {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)
        placeClient.fetchPlace(request)
            .addOnSuccessListener {
                if (it != null) {
                    LogUtils.e(it)
                    adapter.addData(
                        LocationBean(
                            it.place.address ?: placeName,
                            0.0, 0.0,
                            it.place.id
                        )
                    )
                }
            }
    }

    private val zoom = 19f//地图缩放级别
    private fun moveMapCamera(latitude: Double, longitude: Double) {
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        addScreenMoveMarker()
    }


    private val poiCode by lazy { getString(R.string.poi_name) }

    private fun doWhenLocationSuccess(p0: LatLng, islocated: Boolean = true) {
        moveMapCamera(p0.latitude, p0.longitude)
        thread {
            val addresses = Geocoder(
                ActivityUtils.getTopActivity(),
                Locale.getDefault()
            ).getFromLocation(
                p0.latitude,
                p0.longitude,
                20
            )
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                if (islocated) {
                    UserManager.saveLocation(
                        "${address.latitude}",
                        "${address.longitude}",
                        address.adminArea,
                        address.locality,
                        address.thoroughfare,
                        address.countryCode
                    )
                }
                runOnUiThread {
                    adapter.data.clear()
                    adapter.notifyDataSetChanged()
                    if (!adapter.data.contains(
                            LocationBean(
                                getString(R.string.no_yaoqiu),
                                0.0, 0.0,
                                ""
                            )
                        )
                    ) {
                        adapter.addData(
                            LocationBean(
                                getString(R.string.no_yaoqiu),
                                0.0, 0.0,
                                ""
                            )
                        )
                    }
                    for (i in 0 until address.maxAddressLineIndex) {
                        adapter.addData(
                            LocationBean(
                                address.getAddressLine(i),
                                address.latitude, address.longitude, ""
                            )
                        )
                    }


                    rightBtn1.isEnabled = true
                    if (adapter.data.size > 1) {
                        adapter.checkPosition = 1
                    } else {
                        adapter.checkPosition = 0
                    }
                }
            }
        }.run()

//        val token = AutocompleteSessionToken.newInstance()
//        val request = FindAutocompletePredictionsRequest.builder()
//            .setQuery(UserManager.getCity() + search)
//            .setCountry(UserManager.getCountryCode())
//            .setSessionToken(token)
//            .build()
//
//        placeClient.findAutocompletePredictions(request)
//            .addOnSuccessListener(this)
//            .addOnFailureListener(this)

    }

    private fun keyWordSearch(keyword: String) {
        if (TextUtils.isEmpty(keyword)) {
            startLocationUpdates()
            return
        }

        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(UserManager.getCity() + keyword)
            .setCountry(UserManager.getCountryCode())
            .setOrigin(LatLng(mLastLocation.latitude, mLastLocation.longitude))
            .setTypeFilter(TypeFilter.ESTABLISHMENT)
            .setSessionToken(token)
            .build()

        placeClient.findAutocompletePredictions(request)
            .addOnSuccessListener(this)
            .addOnFailureListener(this)


        //120200楼宇 190107街道
//        地名地址信息|道路附属设施|公共设施
//        mQuery = PoiSearch.Query(keyword, poiCode, UserManager.getCity())
//        mQuery!!.pageSize = 100
//        mQuery!!.pageNum = 0//设置查询第一页
//        mPoiSearch = PoiSearch(this, mQuery)
//        mPoiSearch!!.setOnPoiSearchListener(this)
//        mPoiSearch!!.searchPOIAsyn()// 异步搜索
    }

    private var screenMoveMarker: Marker? = null
    private fun addScreenMoveMarker() {
        mGoogleMap.clear()
        mGoogleMap.addMarker(
            MarkerOptions()
                .position(
                    LatLng(
                        latLng.latitude,
                        latLng.longitude
                    )
                )
                .draggable(false)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_anchor))
        )

    }

    override fun onPause() {
        super.onPause()
//        locationMap.onPause()
//        locationUtil.stopLocation()
    }

    override fun onResume() {
        super.onResume()
//        locationMap.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
//        locationMap.onDestroy()
//        locationUtil.stopLocation()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.backToMyLocationBtn -> {
                stopLocationUpdates()
                startLocationUpdates()
            }
            R.id.rightBtn1 -> {
                setResult(
                    Activity.RESULT_OK,
                    intent.putExtra("poiItem", adapter.data[adapter.checkPosition])
                )
                finish()
            }
        }

    }


    private val mLocationCallback: LocationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                if (p0 != null) {
                    LogUtils.d(p0)
                    mLastLocation = p0.lastLocation
                    latLng = LatLng(mLastLocation.latitude, mLastLocation.longitude)
                    UserManager.saveLocation(
                        "${p0.lastLocation.latitude}",
                        "${p0.lastLocation.longitude}"
                    )
                    doWhenLocationSuccess(latLng, true)
                    stopLocationUpdates()

                }

            }
        }
    }


    //开始定位
    private fun startLocationUpdates() {
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )

    }

    //停止定位
    private fun stopLocationUpdates() {
        LocationServices.getFusedLocationProviderClient(this)
            .removeLocationUpdates(mLocationCallback)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mGoogleMap = googleMap ?: return
        setMapLocation()
        startLocationUpdates()
    }

    private var latLng = LatLng(
        UserManager.getlatitude().toDouble(),
        UserManager.getlongtitude().toDouble()
    )

    private fun setMapLocation() {
        with(mGoogleMap) {
            moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
            mapType = GoogleMap.MAP_TYPE_NORMAL
            isMyLocationEnabled = true
            isBuildingsEnabled = true
            isIndoorEnabled = true
            uiSettings.isZoomGesturesEnabled = true
//            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isScrollGesturesEnabled = true
            setOnMapClickListener(this@LocationActivity)
        }
        mLocationRequest = LocationRequest()
//        mLocationRequest.interval = 1000
//        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onSuccess(result: FindAutocompletePredictionsResponse?) {
        if (result != null && result.autocompletePredictions != null) {
            LogUtils.e(result.autocompletePredictions)
            adapter.data.clear()
            if (!adapter.data.contains(
                    LocationBean(
                        getString(R.string.no_yaoqiu),
                        0.0, 0.0,
                        ""
                    )
                )
            )
                adapter.addData(
                    LocationBean(
                        getString(R.string.no_yaoqiu),
                        0.0, 0.0,
                        ""
                    )
                )
            result.autocompletePredictions.forEach {
                findPlaceById(
                    it.placeId,
                    it.getFullText(null).toString()
                )
            }

            rightBtn1.isEnabled = true
            if (adapter.data.size > 1) {
                adapter.checkPosition = 1
            } else {
                adapter.checkPosition = 0
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onFailure(p0: Exception) {


    }

    override fun onMapClick(p0: LatLng) {
        latLng = p0
        doWhenLocationSuccess(p0)
    }


}
