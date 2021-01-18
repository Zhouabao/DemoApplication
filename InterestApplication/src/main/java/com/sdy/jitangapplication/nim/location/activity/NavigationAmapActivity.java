package com.sdy.jitangapplication.nim.location.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.location.adapter.IconListAdapter;
import com.sdy.jitangapplication.nim.location.helper.MapHelper;
import com.sdy.jitangapplication.nim.location.model.NimLocation;
import com.sdy.jitangapplication.nim.uikit.api.wrapper.NimToolBarOptions;
import com.sdy.jitangapplication.nim.uikit.common.ToastHelper;
import com.sdy.jitangapplication.nim.uikit.common.activity.ToolBarOptions;
import com.sdy.jitangapplication.nim.uikit.common.activity.UI;
import com.sdy.jitangapplication.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.sdy.jitangapplication.nim.uikit.common.util.string.StringUtil;
import com.sdy.jitangapplication.utils.LocationUtil;
import com.sdy.jitangapplication.utils.MyLocationCallback;
import com.sdy.jitangapplication.utils.UserManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NavigationAmapActivity extends UI implements
        OnClickListener, LocationExtras,/* NimLocationManager.NimLocationListener,*/
        GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.InfoWindowAdapter, OnMapReadyCallback, MyLocationCallback {

    private TextView sendButton;
    private SupportMapFragment mapView;
    private LocationUtil locationUtil = null;


    private LatLng myLatLng;
    private LatLng desLatLng;

    private Marker myMaker;
    private Marker desMaker;

    private String myAddressInfo; // 对应的地址信息
    private String desAddressInfo; // 目的地址信息

    private boolean firstLocation = true;
    private boolean firstTipLocation = true;

    private String myLocationFormatText;

    GoogleMap googleMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_view_amap_navigation_layout);
        mapView = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.autonavi_mapView);
        mapView.getMapAsync(this);

        ToolBarOptions options = new NimToolBarOptions();
        setToolBar(R.id.toolbar, options);

        initView();

    }

    private void initView() {
        sendButton = findView(R.id.action_bar_right_clickable_textview);
        sendButton.setText(R.string.location_navigate);
        sendButton.setOnClickListener(this);
        sendButton.setVisibility(View.INVISIBLE);

        myLocationFormatText = getString(R.string.format_mylocation);
    }

    private void initAmap() {
        try {
            UiSettings uiSettings = googleMap.getUiSettings();
            uiSettings.setZoomControlsEnabled(true);
            // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            uiSettings.setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示

            googleMap.setOnMarkerClickListener(this); // 标记点击
            googleMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
            googleMap.setInfoWindowAdapter(this); // 必须 信息窗口显示

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initLocation() {
        locationUtil =new  LocationUtil();
        locationUtil.setMyLocationCallback(this);

        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra(LATITUDE, -100);
        double longitude = intent.getDoubleExtra(LONGITUDE, -100);
        desLatLng = new LatLng(latitude, longitude);

        desAddressInfo = intent.getStringExtra(ADDRESS);
        if (TextUtils.isEmpty(desAddressInfo)) {
            desAddressInfo = getString(R.string.location_address_unkown);
        }

        float zoomLevel = intent.getIntExtra(ZOOM_LEVEL, DEFAULT_ZOOM_LEVEL);

        if (UserManager.INSTANCE.getlatitude()=="0") {
            myLatLng = new LatLng(39.90923, 116.397428);
        } else {
            myLatLng = new LatLng(Double.parseDouble(UserManager.INSTANCE.getlatitude()), Double.parseDouble(UserManager.INSTANCE.getlongtitude()));
        }

        createNavigationMarker();
        startLocationTimeout();

        CameraUpdate camera = CameraUpdateFactory.newCameraPosition(new CameraPosition(desLatLng, zoomLevel, 0, 0));
        googleMap.moveCamera(camera);
    }

    private void startLocationTimeout() {
        Handler handler = getHandler();
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 20 * 1000);// 20s超时
    }

    private void updateSendStatus() {
        if (isFinishing()) {
            return;
        }
        if (TextUtils.isEmpty(myAddressInfo)) {
            setTitle(R.string.location_loading);
            sendButton.setVisibility(View.GONE);
        } else {
            setTitle(R.string.location_map);
            sendButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (locationUtil != null) {
            locationUtil.stopLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationUtil != null) {
            locationUtil.stopLocation();
        }
    }

    private void navigate() {
        NimLocation des = new NimLocation(desLatLng.latitude, desLatLng.longitude);
        NimLocation origin = new NimLocation(myLatLng.latitude, myLatLng.longitude);
        doNavigate(origin, des);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_bar_right_clickable_textview:
                navigate();
                break;
        }
    }

    private void updateMyMarkerLatLng() {
        myMaker.setPosition(myLatLng);
        myMaker.showInfoWindow();
    }

    private void showLocationFailTip() {
        if (firstLocation && firstTipLocation) {
            firstTipLocation = false;
            myAddressInfo = getString(R.string.location_address_unkown);
            ToastHelper.showToast(this, R.string.location_address_fail);
        }
    }

    private void clearTimeoutHandler() {
        Handler handler = getHandler();
        handler.removeCallbacks(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            showLocationFailTip();
            updateSendStatus();
        }
    };

    private MarkerOptions defaultMarkerOptions() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map));
        markerOptions.position(desLatLng);
        return markerOptions;
    }

    private void createNavigationMarker() {
        desMaker = googleMap.addMarker(defaultMarkerOptions());
        desMaker.setPosition(desLatLng);
        desMaker.setTitle(desAddressInfo);
        desMaker.showInfoWindow();

        myMaker = googleMap.addMarker(defaultMarkerOptions());
        myMaker.setPosition(myLatLng);
    }

    private void doNavigate(final NimLocation origin, final NimLocation des) {
        List<IconListAdapter.IconListItem> items = new ArrayList<IconListAdapter.IconListItem>();
        final IconListAdapter adapter = new IconListAdapter(this, items);

        List<PackageInfo> infos = MapHelper.getAvailableMaps(this);
        if (infos.size() >= 1) {
            for (PackageInfo info : infos) {
                String name = info.applicationInfo.loadLabel(getPackageManager()).toString();
                Drawable icon = info.applicationInfo.loadIcon(getPackageManager());
                IconListAdapter.IconListItem item = new IconListAdapter.IconListItem(name, icon, info);
                items.add(item);
            }
            CustomAlertDialog dialog = new CustomAlertDialog(this, items.size());
            dialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position) {
                    IconListAdapter.IconListItem item = adapter.getItem(position);
                    PackageInfo info = (PackageInfo) item.getAttach();
                    MapHelper.navigate(NavigationAmapActivity.this, info, origin, des);
                }
            });
            dialog.setTitle(getString(R.string.tools_selected));
            dialog.show();
        } else {
            IconListAdapter.IconListItem item = new IconListAdapter.IconListItem(getString(R.string.friends_map_navigation_web), null, null);
            items.add(item);
            CustomAlertDialog dialog = new CustomAlertDialog(this, items.size());
            dialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position) {
                    MapHelper.navigate(NavigationAmapActivity.this, null, origin, des);
                }
            });
            dialog.setTitle(getString(R.string.tools_selected));
            dialog.show();
        }
    }


    private View getMarkerInfoView(Marker pmarker) {
        String text = null;
        if (pmarker.equals(desMaker)) {
            text = desAddressInfo;
        } else if (pmarker.equals(myMaker)) {
            if (!StringUtil.isEmpty(myAddressInfo)) {
                text = String.format(myLocationFormatText, myAddressInfo);
            }
        }
        if (StringUtil.isEmpty(text)) {
            return null;
        }
        View view = getLayoutInflater().inflate(R.layout.amap_marker_window_info, null);
        TextView textView = (TextView) view.findViewById(R.id.title);
        textView.setText(text);
        return view;
    }

    @Override
    public View getInfoWindow(com.google.android.gms.maps.model.Marker marker) {
        return getMarkerInfoView(marker);
    }

    @Override
    public View getInfoContents(com.google.android.gms.maps.model.Marker marker) {
        return getMarkerInfoView(marker);
    }

    @Override
    public void onInfoWindowClick(com.google.android.gms.maps.model.Marker marker) {
        marker.hideInfoWindow();

    }

    @Override
    public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker) {

        if (marker == null) {
            return false;
        }
        String text = null;
        if (marker.equals(desMaker)) {
            text = desAddressInfo;
        } else if (marker.equals(myMaker)) {
            text = myAddressInfo;
        }
        if (!TextUtils.isEmpty(text)) {
            marker.setTitle(text);
            marker.showInfoWindow();
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        initAmap();
        initLocation();
        locationUtil.requestLocationUpdate(this);
        updateSendStatus();

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void locationFailure() {

    }

    @Override
    public void locationSuccess(@NotNull Location location) {
        if (location != null) {
            if (firstLocation) {
                firstLocation = false;
                new Thread(() -> {
                    try {
                        List<Address> address =    new Geocoder(this, Locale.getDefault()).getFromLocation(location.getLatitude(), location.getLongitude(),1);
                        if (!address.isEmpty() && address.get(0).getMaxAddressLineIndex()!=-1) {
                            myAddressInfo = address.get(0).getAddressLine(0);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).run();

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                myLatLng = new LatLng(latitude, longitude);
                // 缩放到可见区
                int boundPadding = getResources().getDimensionPixelSize(R.dimen.friend_map_bound_padding);
                LatLngBounds bounds = LatLngBounds.builder().include(myLatLng).include(desLatLng).build();
                CameraUpdate camera = CameraUpdateFactory.newLatLngBounds(bounds, boundPadding);
                googleMap.moveCamera(camera);
                updateMyMarkerLatLng();

                updateSendStatus();
            }
        } else {
            showLocationFailTip();
        }
        clearTimeoutHandler();
    }
}
