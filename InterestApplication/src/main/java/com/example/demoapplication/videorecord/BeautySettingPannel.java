package com.example.demoapplication.videorecord;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.blankj.utilcode.util.SPUtils;
import com.example.demoapplication.R;
import com.example.demoapplication.common.Constants;
import com.example.demoapplication.videorecord.widgets.CustomProgressDialog;
import com.example.demoapplication.videorecord.widgets.TXHorizontalPickerView;

import java.util.ArrayList;

public class BeautySettingPannel extends FrameLayout implements SeekBar.OnSeekBarChangeListener {
    private final String TAG = "BeautySettingPannel";
    public static final int ITEM_TYPE_BEAUTY_STYLE = 0;
    public static final int ITEM_TYPE_FILTTER = 0;
    public static final int ITEM_TYPE_BEAUTY = 1;
//    public static final int ITEM_TYPE_MOTION = 2;
//    public static final int ITEM_TYPE_KOUBEI = 3;
//    public static final int ITEM_TYPE_GREEN = 4;

    private int mSencodGradleType = ITEM_TYPE_FILTTER;
    private ArrayList<String> mFirstGradleArrayString = new ArrayList<String>();
    private ArrayList<String> mSencodeGradleArrayString = new ArrayList<String>();
    private int mThirdGradleIndex = 0;
    private int[][] mSzSeekBarValue = null;
    private int[] mSzSecondGradleIndex = new int[16];

    public static final int BEAUTYPARAM_EXPOSURE = 0;
    public static final int BEAUTYPARAM_BEAUTY = 1;
    public static final int BEAUTYPARAM_WHITE = 2;
    public static final int BEAUTYPARAM_FACE_LIFT = 3;
    public static final int BEAUTYPARAM_BIG_EYE = 4;
    public static final int BEAUTYPARAM_FILTER = 5;
    public static final int BEAUTYPARAM_FILTER_MIX_LEVEL = 6;
    public static final int BEAUTYPARAM_MOTION_TMPL = 7;
    public static final int BEAUTYPARAM_GREEN = 8;
//    public static final int BEAUTYPARAM_BEAUTY_STYLE = 9;
    public static final int BEAUTYPARAM_RUDDY = 10;
    public static final int BEAUTYPARAM_NOSESCALE = 11;
    public static final int BEAUTYPARAM_CHINSLIME = 12;
    public static final int BEAUTYPARAM_FACEV = 13;
    public static final int BEAUTYPARAM_FACESHORT = 14;
    public static final int BEAUTYPARAM_SHARPEN = 15;
    public static final int BEAUTYPARAM_CAPTURE_MODE = 16;

    public static final int BEAUTYPARAM_BEAUTY_STYLE_SMOOTH    = 0; // 光滑
    public static final int BEAUTYPARAM_BEAUTY_STYLE_NATURAL   = 1; // 自然
    public static final int BEAUTYPARAM_BEAUTY_STYLE_HAZY      = 2; // 天天P图(朦胧)

    static public class BeautyParams{
        public float mExposure = 0;
        public int mBeautyLevel = 4;
        public int mWhiteLevel = 1;
        public int mRuddyLevel = 0;
        public int mSharpenLevel = 3;
        public int mBeautyStyle = 0;
        public int mFilterMixLevel = 0;
        public int mBigEyeLevel;
        public int mFaceSlimLevel;
        public int mNoseScaleLevel;
        public int mChinSlimLevel;
        public int mFaceVLevel;
        public int mFaceShortLevel;
        public Bitmap mFilterBmp;
        public String mMotionTmplPath;
        public String mGreenFile;
        public int mCaptureMode = 0;
        public int filterIndex;
    }

    private String[] mFirstGradleString = {
            getResources().getString(R.string.beauty_setting_pannel_filter),
//            getResources().getString(R.string.beauty_setting_pannel_beauty),
//            getResources().getString(R.string.beauty_setting_pannel_dynamic_effect),
//            getResources().getString(R.string.beauty_setting_pannel_key),
//            getResources().getString(R.string.beauty_setting_pannel_green_screen),
    };

    private String[] mBeautyString = {
            getResources().getString(R.string.beauty_setting_pannel_style_smooth),
            getResources().getString(R.string.beauty_setting_pannel_style_natural),
            getResources().getString(R.string.beauty_setting_pannel_style_hazy),
            getResources().getString(R.string.beauty_setting_pannel_beauty_whitening),
            getResources().getString(R.string.beauty_setting_pannel_beauty_ruddy),
            getResources().getString(R.string.beauty_setting_pannel_beauty_big_eye),
            getResources().getString(R.string.beauty_setting_pannel_beauty_thin_face),
            getResources().getString(R.string.beauty_setting_pannel_beauty_v_face),
            getResources().getString(R.string.beauty_setting_pannel_beauty_chin),
            getResources().getString(R.string.beauty_setting_pannel_beauty_short_face),
            getResources().getString(R.string.beauty_setting_pannel_beauty_small_nose),
    };
    private String[] mBeautyFilterTypeString = {
            getResources().getString(R.string.beauty_setting_pannel_filter_none),
            getResources().getString(R.string.beauty_setting_pannel_filter_standard),
            getResources().getString(R.string.beauty_setting_pannel_filter_cheery),
            getResources().getString(R.string.beauty_setting_pannel_filter_cloud),
            getResources().getString(R.string.beauty_setting_pannel_filter_pure),
            getResources().getString(R.string.beauty_setting_pannel_filter_orchid),
            getResources().getString(R.string.beauty_setting_pannel_filter_vitality),
            getResources().getString(R.string.beauty_setting_pannel_filter_super),
            getResources().getString(R.string.beauty_setting_pannel_filter_fragrance),
            getResources().getString(R.string.beauty_setting_pannel_filter_romantic),
            getResources().getString(R.string.beauty_setting_pannel_filter_fresh),
            getResources().getString(R.string.beauty_setting_pannel_filter_beautiful),
            getResources().getString(R.string.beauty_setting_pannel_filter_pink),
            getResources().getString(R.string.beauty_setting_pannel_filter_reminiscence),
            getResources().getString(R.string.beauty_setting_pannel_filter_blues),
            getResources().getString(R.string.beauty_setting_pannel_filter_cool),
            getResources().getString(R.string.beauty_setting_pannel_filter_Japanese),
    };


    private SPUtils mPrefs = SPUtils.getInstance(Constants.SPNAME);

    public interface IOnBeautyParamsChangeListener{
        void onBeautyParamsChange(BeautyParams params, int key);
    }
    // 新界面
    TXHorizontalPickerView mFirstGradlePicker;
    ArrayAdapter<String> mFirstGradleAdapter;
    private final int mFilterBasicLevel = 5;

    private final int mBeautyBasicLevel = 4;
    private final int mWhiteBasicLevel = 1;
    private final int mRuddyBasicLevel = 0;
    private int mExposureLevel = -1;
    private final int mSharpenLevel = 3;

    TXHorizontalPickerView mSecondGradlePicker;
    ArrayAdapter<String> mSecondGradleAdapter;

    LinearLayout mSeekBarLL = null;
    TextView mSeekBarValue = null;
    CustomProgressDialog mCustomProgressDialog;

    private SeekBar mThirdGradleSeekBar;

    private Context mContext;

    private IOnBeautyParamsChangeListener mBeautyChangeListener;

    public BeautySettingPannel(Context context, AttributeSet attrs) {
        super(context, attrs);

        View view = LayoutInflater.from(context).inflate(R.layout.beauty_pannel, this);
        mContext = context;
        initView(view);
    }

    public void setBeautyParamsChangeListener(IOnBeautyParamsChangeListener listener) {
        mBeautyChangeListener = listener;
    }

    private void initView(View view) {
        mThirdGradleSeekBar = (SeekBar) view.findViewById(R.id.ThirdGradle_seekbar);
        mThirdGradleSeekBar.setOnSeekBarChangeListener(this);

        mFirstGradlePicker = (TXHorizontalPickerView) view.findViewById(R.id.FirstGradePicker);
        mSecondGradlePicker = (TXHorizontalPickerView) view.findViewById(R.id.secondGradePicker);

        mSeekBarLL = (LinearLayout) view.findViewById(R.id.layoutSeekBar);

        mSeekBarValue = (TextView) view.findViewById(R.id.TextSeekBarValue);

//        setFirstPickerType(view);
        setSecondPickerType(0);
    }

    private void setFirstPickerType(View view){
        mFirstGradleArrayString.clear();
        for (int i = 0; i < mFirstGradleString.length; i++){
            mFirstGradleArrayString.add(mFirstGradleString[i]);
        }
        mFirstGradleAdapter = new ArrayAdapter<String>(mContext, 0, mFirstGradleArrayString){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                String value = getItem(position);
                if (convertView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(android.R.layout.simple_list_item_1,null);
                }
                TextView view = (TextView) convertView.findViewById(android.R.id.text1);
                view.setTag(position);
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                view.setText(value);
                view.setPadding(15, 5, 30, 5);
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int index = (int) view.getTag();
                        ViewGroup group = (ViewGroup)mFirstGradlePicker.getChildAt(0);
                        for (int i = 0; i < mFirstGradleAdapter.getCount(); i++) {
                            View v = group.getChildAt(i);
                            if (v instanceof TextView) {
                                if (i == index) {
                                    ((TextView) v).setTextColor(Color.parseColor("#FF584C"));
                                } else {
                                    ((TextView) v).setTextColor(Color.WHITE);
                                }
                            }
                        }
                        setSecondPickerType(index);
                    }
                });
                return convertView;

            }
        };
        mFirstGradlePicker.setAdapter(mFirstGradleAdapter);
        mFirstGradlePicker.setClicked(ITEM_TYPE_FILTTER);
    }

    private void setSecondPickerType(int type){
        mSencodeGradleArrayString.clear();
        mSencodGradleType = type;

        String[] typeString = null;
        switch (type){
//            case ITEM_TYPE_BEAUTY_STYLE:
//                typeString = mBeautyStyleString;
//                break;
            case ITEM_TYPE_BEAUTY:
                typeString = mBeautyString;
                break;
            case ITEM_TYPE_FILTTER:
                typeString = mBeautyFilterTypeString;
                break;
            default:
                break;
        }
        for (int i = 0; i < typeString.length; i++){
            mSencodeGradleArrayString.add(typeString[i]);
        }
        mSecondGradleAdapter = new ArrayAdapter<String>(mContext, 0, mSencodeGradleArrayString){
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                String value = getItem(position);
                if (convertView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(android.R.layout.simple_list_item_1,null);
                }
                TextView view = (TextView) convertView.findViewById(android.R.id.text1);
                view.setTag(position);
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                view.setText(value);
                view.setPadding(15, 5, 30, 5);
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int index = (int) view.getTag();
                        ViewGroup group = (ViewGroup)mSecondGradlePicker.getChildAt(0);
                        for (int i = 0; i < mSecondGradleAdapter.getCount(); i++) {
                            View v = group.getChildAt(i);
                            if (v instanceof TextView) {
                                if (i == index) {
                                    ((TextView) v).setTextColor(Color.parseColor("#FF584C"));
                                } else {
                                    ((TextView) v).setTextColor(Color.WHITE);
                                }
                            }
                        }
                            setPickerEffect(mSencodGradleType, index);

                    }
                });
                return convertView;
            }
        };
        mSecondGradlePicker.setAdapter(mSecondGradleAdapter);
        mSecondGradlePicker.setClicked(mSzSecondGradleIndex[mSencodGradleType]);
    }

    private void setPickerEffect(int type, int index){
        initSeekBarValue();
        mSzSecondGradleIndex[type] = index;
        mThirdGradleIndex = index;

        switch (type){
//            case ITEM_TYPE_BEAUTY_STYLE:
//                mThirdGradleSeekBar.setVisibility(View.GONE);
//                mSeekBarValue.setVisibility(View.GONE);
//                setBeautyStyle(index);
//                break;
            case ITEM_TYPE_BEAUTY:
                mThirdGradleSeekBar.setVisibility(View.VISIBLE);
                mSeekBarValue.setVisibility(View.VISIBLE);
                mThirdGradleSeekBar.setProgress(mSzSeekBarValue[type][index]);
                setBeautyStyle(index, mSzSeekBarValue[type][index]);
                break;
            case ITEM_TYPE_FILTTER:
                setFilter(index);
                mThirdGradleSeekBar.setVisibility(View.VISIBLE);
                mSeekBarValue.setVisibility(View.VISIBLE);
                mThirdGradleSeekBar.setProgress(mSzSeekBarValue[type][index]);
                break;
            default:
                break;
        }

    }

    public void initProgressValue(int type, int index, int progress){
        switch (type){
            case ITEM_TYPE_BEAUTY:
            case ITEM_TYPE_FILTTER:
                mSzSeekBarValue[type][index] = progress;
                setPickerEffect(type, index);
                // 复位
                setPickerEffect(type, 0);
                break;
        }
    }

    private Bitmap decodeResource(Resources resources, int id) {
        TypedValue value = new TypedValue();
        resources.openRawResource(id, value);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTargetDensity = value.density;
        return BitmapFactory.decodeResource(resources, id, opts);
    }

    //设置滤镜
    private void setFilter(int index) {
        Bitmap bmp = getFilterBitmapByIndex(index);
        if (mBeautyChangeListener != null) {
            BeautyParams params = new BeautyParams();
            params.mFilterBmp = bmp;
            params.filterIndex = index;
            mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_FILTER);
        }
    }

    public Bitmap getFilterBitmapByIndex(int index) {
        Bitmap bmp = null;
        switch (index) {
            case 1:
                bmp = decodeResource(getResources(), R.drawable.filter_biaozhun);
                break;
            case 2:
                bmp = decodeResource(getResources(), R.drawable.filter_yinghong);
                break;
            case 3:
                bmp = decodeResource(getResources(), R.drawable.filter_yunshang);
                break;
            case 4:
                bmp = decodeResource(getResources(), R.drawable.filter_chunzhen);
                break;
            case 5:
                bmp = decodeResource(getResources(), R.drawable.filter_bailan);
                break;
            case 6:
                bmp = decodeResource(getResources(), R.drawable.filter_yuanqi);
                break;
            case 7:
                bmp = decodeResource(getResources(), R.drawable.filter_chaotuo);
                break;
            case 8:
                bmp = decodeResource(getResources(), R.drawable.filter_xiangfen);
                break;
            case 9:
                bmp = decodeResource(getResources(), R.drawable.filter_langman);
                break;
            case 10:
                bmp = decodeResource(getResources(), R.drawable.filter_qingxin);
                break;
            case 11:
                bmp = decodeResource(getResources(), R.drawable.filter_weimei);
                break;
            case 12:
                bmp = decodeResource(getResources(), R.drawable.filter_fennen);
                break;
            case 13:
                bmp = decodeResource(getResources(), R.drawable.filter_huaijiu);
                break;
            case 14:
                bmp = decodeResource(getResources(), R.drawable.filter_landiao);
                break;
            case 15:
                bmp = decodeResource(getResources(), R.drawable.filter_qingliang);
                break;
            case 16:
                bmp = decodeResource(getResources(), R.drawable.filter_rixi);
                break;
            default:
                bmp = null;
                break;
        }
        return bmp;
    }


    //切换采集模式
    private void setCaptureMode(int index) {
        if (mBeautyChangeListener != null) {
            BeautyParams params = new BeautyParams();
            params.mCaptureMode = index;
            mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_CAPTURE_MODE);
        }
    }
    //设置绿幕
    private void setGreenScreen(int index) {
        String file = "";
        switch (index) {
            case 1:
                file = "green_1.mp4";
                break;
            default:
                break;
        }
        if (mBeautyChangeListener != null) {
            BeautyParams params = new BeautyParams();
            params.mGreenFile = file;
            mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_GREEN);
        }
    }


    // 设置美颜类型
    private void setBeautyStyle(int index, int beautyLevel){
        int style = index;
        if (index >= 3){
            return;
        }
        if (mBeautyChangeListener != null) {
            BeautyParams params = new BeautyParams();
            params.mBeautyStyle = style;
            params.mBeautyLevel = beautyLevel;
            mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_BEAUTY);
        }
    }

    public void setViewVisibility(int id, int visible) {
        LinearLayout contentLayout = (LinearLayout)getChildAt(0);
        int count = contentLayout.getChildCount();
        for (int i=0; i<count; ++i) {
            View view = contentLayout.getChildAt(i);
            if (view.getId() == id) {
                view.setVisibility(visible);
                return;
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        initSeekBarValue();
        mSzSeekBarValue[mSencodGradleType][mThirdGradleIndex] = progress;   // 记录设置的值
        mSeekBarValue.setText(String.valueOf(progress));

        if (seekBar.getId() == R.id.ThirdGradle_seekbar){
            if (mSencodGradleType == ITEM_TYPE_BEAUTY) {
                String beautyType = mSencodeGradleArrayString.get(mThirdGradleIndex);
                if(beautyType.equals(getResources().getString(R.string.beauty_setting_pannel_style_smooth))){
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mBeautyLevel = progress;
                        params.mBeautyStyle = BEAUTYPARAM_BEAUTY_STYLE_SMOOTH;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_BEAUTY);
                    }
                }else if(beautyType.equals(getResources().getString(R.string.beauty_setting_pannel_style_natural))){
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mBeautyLevel = progress;
                        params.mBeautyStyle = BEAUTYPARAM_BEAUTY_STYLE_NATURAL;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_BEAUTY);
                    }
                }else if(beautyType.equals(getResources().getString(R.string.beauty_setting_pannel_style_hazy))){
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mBeautyLevel = progress;
                        params.mBeautyStyle = BEAUTYPARAM_BEAUTY_STYLE_HAZY;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_BEAUTY);
                    }
                }else if(beautyType.equals(getResources().getString(R.string.beauty_setting_pannel_beauty_whitening))){
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mWhiteLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_WHITE);
                    }
                }else if(beautyType.equals(getResources().getString(R.string.beauty_setting_pannel_beauty_ruddy))){
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mRuddyLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_RUDDY);
                    }
                }else if(beautyType.equals(getResources().getString(R.string.beauty_setting_pannel_beauty_big_eye))){
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mBigEyeLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_BIG_EYE);
                    }
                }else if(beautyType.equals(getResources().getString(R.string.beauty_setting_pannel_beauty_thin_face))){
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mFaceSlimLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_FACE_LIFT);
                    }
                }else if(beautyType.equals(getResources().getString(R.string.beauty_setting_pannel_beauty_v_face))){
                        if (mBeautyChangeListener != null) {
                            BeautyParams params = new BeautyParams();
                            params.mFaceVLevel = progress;
                            mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_FACEV);
                        }
                }else if(beautyType.equals(getResources().getString(R.string.beauty_setting_pannel_beauty_chin))){
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mChinSlimLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_CHINSLIME);
                    }
                }else if(beautyType.equals(getResources().getString(R.string.beauty_setting_pannel_beauty_short_face))){
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mFaceShortLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_FACESHORT);
                    }
                }else if(beautyType.equals(getResources().getString(R.string.beauty_setting_pannel_beauty_small_nose))){
                    if (mBeautyChangeListener != null) {
                        BeautyParams params = new BeautyParams();
                        params.mNoseScaleLevel = progress;
                        mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_NOSESCALE);
                    }
                }
            } else if (mSencodGradleType == ITEM_TYPE_FILTTER){
                if (mBeautyChangeListener != null) {
                    BeautyParams params = new BeautyParams();
                    params.mFilterMixLevel = progress;
                    mBeautyChangeListener.onBeautyParamsChange(params, BEAUTYPARAM_FILTER_MIX_LEVEL);
                }
            }

        }

    }

    public void setCurrentFilterIndex(int index) {
        mSzSecondGradleIndex[ITEM_TYPE_FILTTER] = index;
        if (mSencodGradleType == ITEM_TYPE_FILTTER) { //当前就是这个Type
            ViewGroup group = (ViewGroup) mSecondGradlePicker.getChildAt(0);
            for (int i = 0; i < mSecondGradleAdapter.getCount(); i++) {
                View v = group.getChildAt(i);
                if (v instanceof TextView) {
                    if (i == index) {
                        ((TextView) v).setTextColor(Color.parseColor("#FF584C"));
                    } else {
                        ((TextView) v).setTextColor(Color.WHITE);
                    }
                }
            }

            mThirdGradleIndex = index;
            mThirdGradleSeekBar.setVisibility(View.VISIBLE);
            mSeekBarValue.setVisibility(View.VISIBLE);
            mThirdGradleSeekBar.setProgress(mSzSeekBarValue[ITEM_TYPE_FILTTER][index]);
        }
    }

    private void initSeekBarValue(){
        if (null == mSzSeekBarValue){
            mSzSeekBarValue = new int[16][24];
            for (int i = 1; i < mSzSeekBarValue[ITEM_TYPE_FILTTER].length; i++){
                mSzSeekBarValue[ITEM_TYPE_FILTTER][i] = mFilterBasicLevel;
            }
            // 前八个滤镜的推荐值 （其他默认为5）
            mSzSeekBarValue[ITEM_TYPE_FILTTER][1] = 4;
            mSzSeekBarValue[ITEM_TYPE_FILTTER][2] = 8;
            mSzSeekBarValue[ITEM_TYPE_FILTTER][3] = 8;
            mSzSeekBarValue[ITEM_TYPE_FILTTER][4] = 8;
            mSzSeekBarValue[ITEM_TYPE_FILTTER][5] = 10;
            mSzSeekBarValue[ITEM_TYPE_FILTTER][6] = 8;
            mSzSeekBarValue[ITEM_TYPE_FILTTER][7] = 10;
            mSzSeekBarValue[ITEM_TYPE_FILTTER][8] = 5;

            for (int i = 0; i < mSzSeekBarValue[ITEM_TYPE_BEAUTY].length; i++){
                if (i >= mSencodeGradleArrayString.size()){
                    break;
                }
                String beautyType = mSencodeGradleArrayString.get(i);
                if(beautyType.equals(getResources().getString(R.string.beauty_setting_pannel_style_smooth))){
                    mSzSeekBarValue[ITEM_TYPE_BEAUTY][i] = mBeautyBasicLevel;
                }else if(beautyType.equals(getResources().getString(R.string.beauty_setting_pannel_style_natural))){
                    mSzSeekBarValue[ITEM_TYPE_BEAUTY][i] = mBeautyBasicLevel;
                }else if(beautyType.equals(getResources().getString(R.string.beauty_setting_pannel_style_hazy))){
                    mSzSeekBarValue[ITEM_TYPE_BEAUTY][i] = mBeautyBasicLevel;
                }else if(beautyType.equals(getResources().getString(R.string.beauty_setting_pannel_beauty_whitening))){
                    mSzSeekBarValue[ITEM_TYPE_BEAUTY][i] = mWhiteBasicLevel;
                }else if(beautyType.equals(getResources().getString(R.string.beauty_setting_pannel_beauty_ruddy))){
                    mSzSeekBarValue[ITEM_TYPE_BEAUTY][i] = mRuddyBasicLevel;
                }

            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public int getFilterProgress(int index) {
        return mSzSeekBarValue[ITEM_TYPE_FILTTER][index];
    }

    public String[] getBeautyFilterArr() {
        return mBeautyFilterTypeString;
    }


}
