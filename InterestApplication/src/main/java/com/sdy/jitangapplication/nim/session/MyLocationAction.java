package com.sdy.jitangapplication.nim.session;

import com.sdy.jitangapplication.R;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.sdy.jitangapplication.nim.uikit.api.model.location.LocationProvider;
import com.sdy.jitangapplication.nim.uikit.impl.NimUIKitImpl;

/**
 * Created by hzxuwen on 2015/6/12.
 */
public class MyLocationAction extends ChatBaseAction {
    private final static String TAG = "LocationAction";

    public MyLocationAction() {
        super(R.drawable.send_location_normal, R.drawable.send_location_uncheck,R.string.input_panel_location);
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
