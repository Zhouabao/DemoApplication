package com.sdy.jitangapplication.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.chuanglan.shanyan_sdk.tool.ShanYanUIConfig;
import com.kotlin.base.common.BaseConstant;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.common.CommonFunction;
import com.sdy.jitangapplication.common.Constants;
import com.sdy.jitangapplication.ui.activity.PhoneActivity;
import com.sdy.jitangapplication.wxapi.WXEntryActivity;


public class ConfigUtils {
    /**
     * 闪验三网运营商授权页配置类
     *
     * @param context
     * @return
     */

    //沉浸式竖屏样式
    public static ShanYanUIConfig getCJSConfig(final Context context) {
        /************************************************自定义控件**************************************************************/
        Drawable logBtnImgPath = context.getResources().getDrawable(R.drawable.shape_rectangle_orange_27dp);
        Drawable backgruond = context.getResources().getDrawable(R.drawable.shanyan_demo_auth_no_bg);
        Drawable returnBg = context.getResources().getDrawable(R.drawable.icon_back_white);
        //loading自定义加载框
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout view_dialog = (LinearLayout) inflater.inflate(R.layout.loading_layout, null);
        RelativeLayout.LayoutParams mLayoutParams3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        view_dialog.setLayoutParams(mLayoutParams3);
        view_dialog.setVisibility(View.GONE);

        //号码栏背景
        LayoutInflater numberinflater = LayoutInflater.from(context);
        RelativeLayout numberLayout = (RelativeLayout) numberinflater.inflate(R.layout.shanyan_demo_phobackground, null);
        RelativeLayout.LayoutParams numberParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numberParams.setMargins(0, 0, 0, SizeUtils.dp2px(250));
        numberParams.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(50);
        numberParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        numberParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        numberLayout.setLayoutParams(numberParams);

        LayoutInflater inflater1 = LayoutInflater.from(context);
        RelativeLayout relativeLayout = (RelativeLayout) inflater1.inflate(R.layout.shanyan_demo_other_login_item, null);
        RelativeLayout.LayoutParams layoutParamsOther = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsOther.setMargins(0, 0, 0, SizeUtils.dp2px(80));
        layoutParamsOther.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParamsOther.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        relativeLayout.setLayoutParams(layoutParamsOther);
        otherLogin(context, relativeLayout);
        ShanYanUIConfig uiConfig = new ShanYanUIConfig.Builder()
                .setActivityTranslateAnim("shanyan_demo_fade_in_anim", "shanyan_dmeo_fade_out_anim")
                //授权页导航栏：
                .setNavColor(Color.parseColor("#ffffff"))  //设置导航栏颜色
                .setNavText("")  //设置导航栏标题文字
                .setNavReturnBtnWidth(24)
                .setNavReturnBtnHeight(24)
                .setAuthBGImgPath(backgruond)
                .setLogoHidden(true)   //是否隐藏logo
                .setDialogDimAmount(0f)
                .setNavReturnImgPath(returnBg)
                .setNavReturnImgHidden(true)
                .setFullScreen(true)
                .setStatusBarHidden(true)


                //授权页号码栏：
                .setNumberColor(Color.WHITE)  //设置手机号码字体颜色
                .setNumFieldOffsetBottomY(409)    //设置号码栏相对于标题栏下边缘y偏移
                .setNumberSize(26)
                .setNumberBold(true)
                .setNumFieldHeight(40)


                //授权页登录按钮：
                .setLogBtnText("本机号码一键登录")  //设置登录按钮文字
                .setLogBtnTextColor(0xffffffff)   //设置登录按钮文字颜色
                .setLogBtnImgPath(logBtnImgPath)   //设置登录按钮图片
                .setLogBtnTextSize(18)
                .setLogBtnTextBold(true)
                .setLogBtnHeight(54)
                .setLogBtnOffsetBottomY(295)
                .setLogBtnWidth(298)

                //授权页隐私栏：
                .setAppPrivacyOne(" 积糖用户协议 ", BaseConstant.SERVER_ADDRESS + "protocol/userProtocol" + Constants.END_BASE_URL)  //设置开发者隐私条款1名称和URL(名称，url)
                .setAppPrivacyTwo(" 积糖隐私协议 ", BaseConstant.SERVER_ADDRESS + "protocol/privacyProtocol" + Constants.END_BASE_URL)  //设置开发者隐私条款2名称和URL(名称，url)
                .setAppPrivacyColor(Color.parseColor("#ffffff"), Color.parseColor("#FF6796FA"))    //	设置隐私条款名称颜色(基础文字颜色，协议文字颜色)
                .setPrivacyText("登录注册代表你已同意", "和", " 以及 ", "、", "")
                .setPrivacyOffsetBottomY(10)//设置隐私条款相对于屏幕下边缘y偏
                .setPrivacyState(true)
                .setPrivacyTextSize(12)
                .setPrivacyOffsetX(20)
                .setOperatorPrivacyAtLast(true)
                .setPrivacySmhHidden(true)
                .setCheckBoxHidden(true)
                .setPrivacyState(true)

                .setSloganHidden(false)
                .setSloganOffsetBottomY(263)
                .setSloganTextColor(Color.WHITE)
                .setSloganTextSize(12)

                .setShanYanSloganHidden(true)
                .setShanYanSloganTextColor(Color.parseColor("#ffffff"))

                // .addCustomView(numberLayout, false, false, null)

                .setLoadingView(view_dialog)
                // 添加自定义控件:
                .addCustomView(relativeLayout, false, false, null)
                //标题栏下划线，可以不写
                .build();

        return uiConfig;

    }

    private static void otherLogin(final Context context, RelativeLayout relativeLayout) {
        LinearLayout weixin = relativeLayout.findViewById(R.id.shanyan_dmeo_weixin);
        LinearLayout phone = relativeLayout.findViewById(R.id.shanyan_dmeo_phone);
        weixin.setOnClickListener(v -> {
            phone.setEnabled(false);
            CommonFunction.INSTANCE.wechatLogin(context, WXEntryActivity.WECHAT_LOGIN);
            phone.postDelayed(() -> {
                phone.setEnabled(true);
            }, 1000L);
        });
        phone.setOnClickListener(v -> {
            weixin.setEnabled(false);
            Intent intent = new Intent(context, PhoneActivity.class);
            intent.putExtra("type", "1");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            weixin.postDelayed(() -> {
                weixin.setEnabled(true);
            }, 1000L);
        });

    }
}