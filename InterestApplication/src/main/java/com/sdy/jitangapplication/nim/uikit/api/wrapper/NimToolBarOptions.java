package com.sdy.jitangapplication.nim.uikit.api.wrapper;

import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.uikit.common.activity.ToolBarOptions;

/**
 * Created by hzxuwen on 2016/6/16.
 */
public class NimToolBarOptions extends ToolBarOptions {

    public NimToolBarOptions() {
        logoId = 0;
//        logoId = R.drawable.nim_actionbar_nest_dark_logo;
        navigateId = R.drawable.icon_return;
        isNeedNavigate = true;
    }

    public NimToolBarOptions(int logoId) {
        this.logoId = logoId;
        navigateId = R.drawable.icon_return;
        isNeedNavigate = true;
    }
}
