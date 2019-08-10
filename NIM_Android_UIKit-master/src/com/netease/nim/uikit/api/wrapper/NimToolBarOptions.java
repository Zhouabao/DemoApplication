package com.netease.nim.uikit.api.wrapper;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.ToolBarOptions;

/**
 * Created by hzxuwen on 2016/6/16.
 */
public class NimToolBarOptions extends ToolBarOptions {

    public NimToolBarOptions() {
        logoId = 0;
//        logoId = R.drawable.nim_actionbar_nest_dark_logo;
        navigateId = R.drawable.nim_actionbar_dark_back_icon;
        isNeedNavigate = true;
    }

    public NimToolBarOptions(int logoId) {
        this.logoId = logoId;
        navigateId = R.drawable.nim_actionbar_dark_back_icon;
        isNeedNavigate = true;
    }
}
