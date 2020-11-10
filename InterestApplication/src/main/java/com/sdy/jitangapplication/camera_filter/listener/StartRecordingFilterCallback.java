package com.sdy.jitangapplication.camera_filter.listener;

import android.app.Activity;
import android.widget.Toast;

import com.sdy.jitangapplication.R;

import org.wysaid.view.CameraRecordGLSurfaceView;

/**
 * Created by dingmouren
 * email: naildingmouren@gmail.com
 * github: https://github.com/DingMouRen
 */

public class StartRecordingFilterCallback implements CameraRecordGLSurfaceView.StartRecordingCallback {

    private Activity mActivity;

    public StartRecordingFilterCallback(Activity activity){
        this.mActivity = activity;
    }

    @Override
    public void startRecordingOver(final boolean success) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (success){
                    Toast.makeText(mActivity,mActivity.getString(R.string.start_record),Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(mActivity,mActivity.getString(R.string.record_fail),Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
