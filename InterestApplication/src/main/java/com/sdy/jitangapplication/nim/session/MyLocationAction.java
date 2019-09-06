package com.sdy.jitangapplication.nim.session;

import com.sdy.jitangapplication.R;
import com.netease.nim.uikit.api.model.location.LocationProvider;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * Created by hzxuwen on 2015/6/12.
 */
public class MyLocationAction extends ChatBaseAction {
    private final static String TAG = "LocationAction";

    public MyLocationAction() {
        super(R.drawable.send_location_check, R.drawable.send_location_uncheck,R.string.input_panel_location);
    }

    @Override
    public void onClick() {
        if (NimUIKitImpl.getLocationProvider() != null) {
            NimUIKitImpl.getLocationProvider().requestLocation(getActivity(), new LocationProvider.Callback() {
                @Override
                public void onSuccess(double longitude, double latitude, String address) {
                    IMMessage message = MessageBuilder.createLocationMessage(getAccount(), getSessionType(), latitude, longitude,
                            address);
                    sendMessage(message);
                }
            });
            setCheck(false);
        }
    }
}
