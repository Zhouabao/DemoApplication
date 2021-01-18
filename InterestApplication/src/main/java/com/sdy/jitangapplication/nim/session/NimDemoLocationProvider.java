package com.sdy.jitangapplication.nim.session;

import android.content.Context;
import android.content.Intent;

import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.location.activity.LocationAmapActivity;
import com.sdy.jitangapplication.nim.location.activity.LocationExtras;
import com.sdy.jitangapplication.nim.location.activity.NavigationAmapActivity;
import com.sdy.jitangapplication.nim.location.helper.NimLocationManager;
import com.sdy.jitangapplication.nim.uikit.api.model.location.LocationProvider;
import com.sdy.jitangapplication.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.sdy.jitangapplication.nim.uikit.common.util.log.LogUtil;


/**
 * Created by zhoujianghua on 2015/8/11.
 */
public class NimDemoLocationProvider implements LocationProvider {
    @Override
    public void requestLocation(final Context context, Callback callback) {
        if (!NimLocationManager.isLocationEnable(context)) {
            final EasyAlertDialog alertDialog = new EasyAlertDialog(context);
            alertDialog.setMessage(context.getString(R.string.unopen_location));
            alertDialog.addNegativeButton(context.getString(R.string.cancel), EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                    v -> alertDialog.dismiss());
            alertDialog.addPositiveButton(context.getString(R.string.setting), EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                    v -> {
                        alertDialog.dismiss();
                        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        try {
                            context.startActivity(intent);
                        } catch (Exception e) {
                            LogUtil.e("LOC", "start ACTION_LOCATION_SOURCE_SETTINGS error");
                        }

                    });
            alertDialog.show();
            return;
        }

        LocationAmapActivity.start(context, callback);
    }

    @Override
    public void openMap(Context context, double longitude, double latitude, String address) {
        Intent intent = new Intent(context, NavigationAmapActivity.class);
        intent.putExtra(LocationExtras.LONGITUDE, longitude);
        intent.putExtra(LocationExtras.LATITUDE, latitude);
        intent.putExtra(LocationExtras.ADDRESS, address);
        context.startActivity(intent);
    }
}
