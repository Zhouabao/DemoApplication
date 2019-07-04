package com.example.demoapplication.videorecord;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.example.demoapplication.R;
import com.example.demoapplication.videorecord.utils.TCConstants;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXUGCRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * UGC拍照界面
 */
public class TCCameraActivity extends AppCompatActivity implements
        View.OnClickListener, BeautySettingPannel.IOnBeautyParamsChangeListener
        , View.OnTouchListener,
        GestureDetector.OnGestureListener,
        ScaleGestureDetector.OnScaleGestureListener {

    private static final String TAG = "TCCameraActivity";
    private int mRecordType = TCConstants.VIDEO_RECORD_TYPE_UGC_RECORD;
    private boolean mRecording = false;
    private boolean mStartPreview = false;
    private boolean mFront = true;
    private TXUGCRecord mTXCameraRecord;

    private BeautySettingPannel.BeautyParams mBeautyParams = new BeautySettingPannel.BeautyParams();
    private TXCloudVideoView mVideoView;
    private ImageView backLL;
    private ComposeRecordBtn mComposeRecordBtn;

    private BeautySettingPannel mBeautyPannelView;
    private boolean mPause = false;

    private int mCurrentAspectRatio = TXRecordCommon.VIDEO_ASPECT_RATIO_3_4; // 视频比例;
    private FrameLayout mMaskLayout;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor;
    private float mLastScaleFactor;

    private int mHomeOrientation = TXLiveConstants.VIDEO_ANGLE_HOME_DOWN; // 录制方向
    private int mRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT; // 渲染方向


    private MediaPlayer mShootMediaPlayer = null;
    private MyHandler mMyHandler = new MyHandler();
    private ImageView mIvSnapshotPhoto;

    //标记是否在拍照中，包括拍照，存储，动画的整个过程之前把该值设为真，过程结束后设为假
    //在进行拍照之前，如果该值为真，则不进入拍照过程，以防止多个拍照过程重叠而产生的异常
    private boolean mIsTakingPhoto = false;

    /**
     * ------------------------ 滑动滤镜相关 ------------------------
     */
    private TextView mTvFilter;
    private int mCurrentIndex = 0; // 当前滤镜Index
    private int mLeftIndex = 0, mRightIndex = 1;// 左右滤镜的Index
    private int mLastLeftIndex = -1, mLastRightIndex = -1; // 之前左右滤镜的Index
    private float mLeftBitmapRatio;      // 左侧滤镜的比例
    private float mMoveRatio;      // 滑动的比例大小
    private boolean mStartScroll;  // 已经开始滑动了标记
    private boolean mMoveRight;    // 滑动是否往右
    private boolean mIsNeedChange;    // 滤镜的是否需要发生改变
    private ValueAnimator mFilterAnimator;
    private boolean mIsDoingAnimator;

    private Bitmap mLeftBitmap;
    private Bitmap mRightBitmap;
    //--------------------------------------------------------------
    private boolean mTouchFocus = true; // 开启手动聚焦；自动聚焦设置为false

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_record);
        initViews();
    }

    private static final int MSG_TAKE_PHOTO_SUCCESS = 1;
    private static final int MSG_TAKE_PHOTO_FAIL = 2;

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TAKE_PHOTO_SUCCESS: {
                    setResult(Activity.RESULT_OK, new Intent().putExtra("filePath", filePath));
                    finish();
                    break;
                }
                case MSG_TAKE_PHOTO_FAIL: {
                    Toast.makeText(TCCameraActivity.this, getResources().getString(R.string.activity_video_record_take_photo_fail), Toast.LENGTH_SHORT).show();
                    mIsTakingPhoto = true;
                    break;
                }
                default:
                    break;

            }
        }
    }


    private void startCameraPreview() {
        if (mStartPreview) return;
        mStartPreview = true;

        mTXCameraRecord = TXUGCRecord.getInstance(this.getApplicationContext());
//        mTXCameraRecord.setVideoRecordListener(this);
        // 推荐配置
        TXRecordCommon.TXUGCCustomConfig customConfig = new TXRecordCommon.TXUGCCustomConfig();
        customConfig.isFront = mFront;

        mTXCameraRecord.setMute(false);

        mTXCameraRecord.setHomeOrientation(mHomeOrientation);
        mTXCameraRecord.setRenderRotation(mRenderRotation);
        mTXCameraRecord.startCameraCustomPreview(customConfig, mVideoView);
        mTXCameraRecord.setAspectRatio(mCurrentAspectRatio);

        mTXCameraRecord.setFilter(mBeautyParams.mFilterBmp);
    }


    private String filePath;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initViews() {
        backLL = (ImageView) findViewById(R.id.back_ll);
        backLL.setOnClickListener(this);

        mMaskLayout = (FrameLayout) findViewById(R.id.mask);
        mMaskLayout.setOnTouchListener(this);

        mTvFilter = (TextView) findViewById(R.id.record_tv_filter);

        mBeautyPannelView = (BeautySettingPannel) findViewById(R.id.beauty_pannel);
        mBeautyPannelView.setBeautyParamsChangeListener(this);

        mVideoView = (TXCloudVideoView) findViewById(R.id.video_view);
        mGestureDetector = new GestureDetector(this, this);
        mScaleGestureDetector = new ScaleGestureDetector(this, this);

        mIvSnapshotPhoto = (ImageView) findViewById(R.id.iv_snapshot_photo);
        mComposeRecordBtn = (ComposeRecordBtn) findViewById(R.id.compose_record_btn);
        mComposeRecordBtn.setRecordMode(ComposeRecordBtn.RECORD_MODE_TAKE_PHOTO);
        mComposeRecordBtn.setOnRecordButtonListener(new ComposeRecordBtn.IRecordButtonListener() {
            @Override
            public void onRecordStart() {
            }

            @Override
            public void onRecordPause() {
            }

            @Override
            public void onTakePhotoStart() {
                if (mIsTakingPhoto) {
                    return;
                }
                mIsTakingPhoto = true;
                AudioManager meng = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                int volume = meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

                if (volume != 0) {
                    if (mShootMediaPlayer == null) {
                        mShootMediaPlayer = MediaPlayer.create(TCCameraActivity.this, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
                    }
                    if (mShootMediaPlayer != null) {
                        mShootMediaPlayer.start();
                    }
                }


            }

            @Override
            public void onTakePhotoFinish() {
                mTXCameraRecord.snapshot(new TXRecordCommon.ITXSnapshotListener() {
                    @Override
                    public void onSnapshot(Bitmap bitmap) {
                        String fileName = System.currentTimeMillis() + ".jpg";
                        String bitPath = MediaStore.Images.Media.insertImage(TCCameraActivity.this.getContentResolver(), bitmap, fileName, null);
                        filePath = getRealPathFromURI(Uri.parse(bitPath));
                        Log.i("TCCameraActivity", fileName + "," + filePath);
                        Message message = new Message();
                        message.what = MSG_TAKE_PHOTO_SUCCESS;
                        message.obj = bitmap;
                        mMyHandler.sendMessage(message);
                    }
                });


            }
        });


//        showEffectPannel();
    }

    private void showSnapshotPhoto(final Bitmap bitmap, final String filePath) {
        mIvSnapshotPhoto.setTranslationX(0);
        mIvSnapshotPhoto.setTranslationY(0);
        mIvSnapshotPhoto.setScaleX(1);
        mIvSnapshotPhoto.setScaleY(1);
        mIvSnapshotPhoto.setPivotX(0);
        mIvSnapshotPhoto.setPivotY(0);
        mIvSnapshotPhoto.setAlpha(1.0f);
        mIvSnapshotPhoto.setImageBitmap(bitmap);
        mIvSnapshotPhoto.setVisibility(View.VISIBLE);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        float screenWidth = dm.widthPixels;

        float vWidth = mIvSnapshotPhoto.getWidth();

        float density = getResources().getDisplayMetrics().density;

        float targetWidthInDP = 80;

        float targetWidth = targetWidthInDP * density;

        float scale = targetWidth / vWidth;

        float targetLocalX = screenWidth - 40 * density - targetWidth;
        float targetLocalY = 40 * density;

        float translationX = targetLocalX - 0;
        float translationY = targetLocalY - 0;

        ObjectAnimator animatorScaleX = ObjectAnimator.ofFloat(mIvSnapshotPhoto, "scaleX", 1, scale);
        ObjectAnimator animatorScaleY = ObjectAnimator.ofFloat(mIvSnapshotPhoto, "scaleY", 1, scale);
        ObjectAnimator animatorTranslationX = ObjectAnimator.ofFloat(mIvSnapshotPhoto, "translationX", 0, translationX);
        ObjectAnimator animatorTranslationY = ObjectAnimator.ofFloat(mIvSnapshotPhoto, "translationY", 0, translationY);

        AnimatorSet animatorSet1 = new AnimatorSet();
        animatorSet1.setDuration(2000);
        animatorSet1.setInterpolator(new DecelerateInterpolator());
        animatorSet1.play(animatorScaleX).with(animatorScaleY).with(animatorTranslationX).with(animatorTranslationY);

        ObjectAnimator animatorFadeOut = ObjectAnimator.ofFloat(mIvSnapshotPhoto, "alpha", 1.0f, 1.0f, 0.0f);


        AnimatorSet animatorSet2 = new AnimatorSet();
        animatorSet2.setDuration(2000);
        animatorSet2.setInterpolator(new LinearInterpolator());
        animatorSet2.play(animatorFadeOut);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animatorSet1);
        animatorSet.play(animatorSet2).after(animatorSet1);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIvSnapshotPhoto.setVisibility(View.VISIBLE);
//                Toast.makeText(TCCameraActivity.this, getResources().getString(R.string.activity_video_record_take_photo_success), Toast.LENGTH_SHORT).show();
                mIsTakingPhoto = false;

//                setResult(Activity.RESULT_OK, getIntent().putExtra("fileName", bitmap));
//                TCCameraActivity.this.finish();

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animatorSet.start();

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (hasPermission()) {
            startCameraPreview();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mTXCameraRecord.stopCameraPreview();

        if (mTXCameraRecord != null) {
            mTXCameraRecord.pauseBGM();
        }

        mStartPreview = false;
        startCameraPreview();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_ll:
                finish();
                break;
            case R.id.btn_switch_camera:
                mFront = !mFront;
                if (mTXCameraRecord != null) {
                    TXCLog.i(TAG, "switchCamera = " + mFront);
                    mTXCameraRecord.switchCamera(mFront);
                }
                break;
        }
    }


    @Override
    public void onBeautyParamsChange(BeautySettingPannel.BeautyParams params, int key) {
        if (key == BeautySettingPannel.BEAUTYPARAM_FILTER) {
            mBeautyParams.mFilterBmp = params.mFilterBmp;
            mCurrentIndex = params.filterIndex;
            if (mTXCameraRecord != null) {
                mTXCameraRecord.setSpecialRatio(mBeautyPannelView.getFilterProgress(mCurrentIndex) / 10.f);
                mTXCameraRecord.setFilter(params.mFilterBmp);
            }
            doTextAnimator();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                for (int ret : grantResults) {
                    if (ret != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.tc_video_record_activity_on_request_permissions_result_failed_to_get_permission),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                startCameraPreview();
                break;
            default:
                break;
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 100);
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view == mMaskLayout) {
            if (motionEvent.getPointerCount() >= 2) {
                mScaleGestureDetector.onTouchEvent(motionEvent);
            } else if (motionEvent.getPointerCount() == 1) {
                mGestureDetector.onTouchEvent(motionEvent);
                // 说明是滤镜滑动后结束
                if (mStartScroll && motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    doFilterAnimator();
                }
            }
        }
        return true;
    }

    private void doFilterAnimator() {
        if (mMoveRatio >= 0.2f) { //当滑动距离达到0.2比例的时候，则说明要切换
            mIsNeedChange = true;
            if (mMoveRight) { //说明是右滑动
                mCurrentIndex--;
                mFilterAnimator = generateValueAnimator(mLeftBitmapRatio, 1);
            } else { //左滑动
                mCurrentIndex++;
                mFilterAnimator = generateValueAnimator(mLeftBitmapRatio, 0);
            }
        } else {
            if (mCurrentIndex == mLeftIndex) {//说明用户向左侧滑动
                mFilterAnimator = generateValueAnimator(mLeftBitmapRatio, 1);
            } else {
                mFilterAnimator = generateValueAnimator(mLeftBitmapRatio, 0);
            }
        }
        mFilterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mIsDoingAnimator = true;
                if (mTXCameraRecord == null) return;
                float leftRatio = (float) valueAnimator.getAnimatedValue();
                // 动画结束
                if (leftRatio == 0 || leftRatio == 1) {
                    mLeftBitmapRatio = leftRatio;
                    if (mIsNeedChange) {
                        mIsNeedChange = false;
                        doTextAnimator();
                    } else {
                        mIsDoingAnimator = false;
                    }
                    mBeautyPannelView.setCurrentFilterIndex(mCurrentIndex);

                    // 保存到params 以便程序切换后恢复滤镜
                    if (mCurrentIndex == mLeftIndex) {
                        mBeautyParams.mFilterBmp = mLeftBitmap;
                    } else {
                        mBeautyParams.mFilterBmp = mRightBitmap;
                    }
                    mBeautyParams.mFilterMixLevel = mBeautyPannelView.getFilterProgress(mCurrentIndex);
                }
                float leftSpecialRatio = mBeautyPannelView.getFilterProgress(mLeftIndex) / 10.f;
                float rightSpecialRatio = mBeautyPannelView.getFilterProgress(mRightIndex) / 10.f;
                mTXCameraRecord.setFilter(
                        mLeftBitmap,
                        leftSpecialRatio,
                        mRightBitmap,
                        rightSpecialRatio, leftRatio
                );
            }


        });
        mFilterAnimator.start();
    }

    private ValueAnimator generateValueAnimator(float start, float end) {
        ValueAnimator animator = ValueAnimator.ofFloat(start, end);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(400);
        return animator;
    }

    private void doTextAnimator() {
        // 设置当前滤镜的名字
        mTvFilter.setText(mBeautyPannelView.getBeautyFilterArr()[mCurrentIndex]);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(400);
        alphaAnimation.setInterpolator(new LinearInterpolator());
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mTvFilter.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTvFilter.setVisibility(View.GONE);
                mIsDoingAnimator = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mTvFilter.startAnimation(alphaAnimation);
    }

    // OnGestureListener回调start
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        mStartScroll = false;
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        if (mTXCameraRecord != null && mTouchFocus) {
            mTXCameraRecord.setFocusPosition(motionEvent.getX(), motionEvent.getY());
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
        if (mIsDoingAnimator) {
            return true;
        }
        boolean moveRight = moveEvent.getX() > downEvent.getX();
        if (moveRight && mCurrentIndex == 0) {
            //  Toast.makeText(TCCameraActivity.this, "已经是第一个啦~", Toast.LENGTH_SHORT).show();
            return true;
        } else if (!moveRight && mCurrentIndex == mBeautyPannelView.getBeautyFilterArr().length - 1) {
            //Toast.makeText(TCCameraActivity.this, "已经是最后一个啦~", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            mStartScroll = true;
            if (moveRight) {//往右滑动
                mLeftIndex = mCurrentIndex - 1;
                mRightIndex = mCurrentIndex;
            } else {// 往左滑动
                mLeftIndex = mCurrentIndex;
                mRightIndex = mCurrentIndex + 1;
            }

            if (mLastLeftIndex != mLeftIndex) { //如果不一样，才加载bitmap出来；避免滑动过程中重复加载
                mLeftBitmap = mBeautyPannelView.getFilterBitmapByIndex(mLeftIndex);
                mLastLeftIndex = mLeftIndex;
            }

            if (mLastRightIndex != mRightIndex) {//如果不一样，才加载bitmap出来；避免滑动过程中重复加载
                mRightBitmap = mBeautyPannelView.getFilterBitmapByIndex(mRightIndex);
                mLastRightIndex = mRightIndex;
            }

            int width = mMaskLayout.getWidth();
            float dis = moveEvent.getX() - downEvent.getX();
            float leftBitmapRatio = Math.abs(dis) / (width * 1.0f);

            float leftSpecialRatio = mBeautyPannelView.getFilterProgress(mLeftIndex) / 10.0f;
            float rightSpecialRatio = mBeautyPannelView.getFilterProgress(mRightIndex) / 10.0f;
            mMoveRatio = leftBitmapRatio;
            if (moveRight) {
                leftBitmapRatio = leftBitmapRatio;
            } else {
                leftBitmapRatio = 1 - leftBitmapRatio;
            }
            this.mMoveRight = moveRight;
            mLeftBitmapRatio = leftBitmapRatio;
            if (mTXCameraRecord != null)
                mTXCameraRecord.setFilter(mLeftBitmap, leftSpecialRatio, mRightBitmap, rightSpecialRatio, leftBitmapRatio);
            return true;
        }
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return true;
    }
    // OnGestureListener回调end

    // OnScaleGestureListener回调start
    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        int maxZoom = mTXCameraRecord.getMaxZoom();
        if (maxZoom == 0) {
            TXCLog.i(TAG, "camera not support zoom");
            return false;
        }

        float factorOffset = scaleGestureDetector.getScaleFactor() - mLastScaleFactor;

        mScaleFactor += factorOffset;
        mLastScaleFactor = scaleGestureDetector.getScaleFactor();
        if (mScaleFactor < 0) {
            mScaleFactor = 0;
        }
        if (mScaleFactor > 1) {
            mScaleFactor = 1;
        }

        int zoomValue = Math.round(mScaleFactor * maxZoom);
        mTXCameraRecord.setZoom(zoomValue);
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        mLastScaleFactor = scaleGestureDetector.getScaleFactor();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

    }

    public String getRealPathFromURI(Uri contentUri) {//通过本地路经 content://得到URI路径
        Cursor cursor = null;
        String locationPath = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            locationPath = cursor.getString(column_index);
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return locationPath;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
