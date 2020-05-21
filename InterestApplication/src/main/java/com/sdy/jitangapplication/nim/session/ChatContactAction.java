package com.sdy.jitangapplication.nim.session;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.common.CommonFunction;

/**
 * 联系方式
 */
public class ChatContactAction extends ChatBaseAction {

    public ChatContactAction() {
        super(R.drawable.icon_contact_panel, R.drawable.icon_contact_panel, R.string.input_contact_panel);
    }

    @Override
    public void onClick() {
        int gender;
        if (((NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(getAccount())).getGenderEnum() == GenderEnum.FEMALE) {
            gender = 2;
        } else {
            gender = 1;
        }
        CommonFunction.INSTANCE.checkUnlockContact(getActivity(), getAccount(), gender);
    }

}
