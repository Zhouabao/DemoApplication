package com.sdy.jitangapplication.nim.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.sdy.baselibrary.widgets.swipeback.SwipeBackLayout;
import com.sdy.baselibrary.widgets.swipeback.Utils;
import com.sdy.baselibrary.widgets.swipeback.app.SwipeBackActivityBase;
import com.sdy.baselibrary.widgets.swipeback.app.SwipeBackActivityHelper;
import com.sdy.jitangapplication.R;
import com.kotlin.base.common.AppManager;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.business.session.module.ModuleProxy;
import com.netease.nim.uikit.business.session.module.list.MessageListPanelEx;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * 搜索结果消息列表界面
 * <p>
 * Created by huangjun on 2017/1/11.
 */
public class DisplayMessageActivity extends UI implements ModuleProxy, SwipeBackActivityBase {

    private static String EXTRA_ANCHOR = "anchor";

    public static void start(Context context, IMMessage anchor) {
        Intent intent = new Intent();
        intent.setClass(context, DisplayMessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //search extra
        intent.putExtra(EXTRA_ANCHOR, anchor);

        context.startActivity(intent);
    }

    // context
    private SessionTypeEnum sessionType;
    private String account; // 对方帐号
    private IMMessage anchor;

    private MessageListPanelEx messageListPanel;

    private ImageView btnBack;
    private TextView llTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        AppManager.Companion.getInstance().addActivity(this);

        View rootView = LayoutInflater.from(this).inflate(R.layout.message_history_activity, null);
        setContentView(rootView);

        btnBack = rootView.findViewById(R.id.btnBack);
        llTitle = rootView.findViewById(R.id.hotT1);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        onParseIntent();

        Container container = new Container(this, account, sessionType, this);
        messageListPanel = new MessageListPanelEx(container, rootView, anchor, true, false);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        messageListPanel.onDestroy();
        AppManager.Companion.getInstance().finishActivity(this);
    }

    protected void onParseIntent() {
        anchor = (IMMessage) getIntent().getSerializableExtra(EXTRA_ANCHOR);
        account = anchor.getSessionId();
        sessionType = anchor.getSessionType();
        llTitle.setText(UserInfoHelper.getUserName(account));
    }

    @Override
    public boolean sendMessage(IMMessage msg) {
        return false;
    }

    @Override
    public void onInputPanelExpand() {

    }

    @Override
    public void onItemFooterClick(IMMessage message) {

    }

    @Override
    public void shouldCollapseInputPanel() {

    }

    @Override
    public boolean isLongClickEnabled() {
        return true;
    }



    /*------------------------侧滑退出-----------------*/

    private SwipeBackActivityHelper mHelper;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null)
            return mHelper.findViewById(id);
        return v;
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return mHelper.getSwipeBackLayout();
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this);
        getSwipeBackLayout().scrollToFinishActivity();
    }
}
