package com.sdy.jitangapplication.nim.uikit.impl.customization;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.DemoCache;
import com.sdy.jitangapplication.nim.uikit.api.model.recent.RecentCustomization;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangjun on 2017/9/29.
 */

public class DefaultRecentCustomization extends RecentCustomization {

    /**
     * 最近联系人列表项文案定制
     *
     * @param recent 最近联系人
     * @return 默认文案
     */
    public String getDefaultDigest(RecentContact recent) {
        switch (recent.getMsgType()) {
            case text:
                return recent.getContent();
            case image:
                return "["+ DemoCache.getContext().getString(R.string.input_panel_photo) +"]";
            case video:
                return "["+ DemoCache.getContext().getString(R.string.input_panel_video) +"]";
            case audio:
                return "["+ DemoCache.getContext().getString(R.string.message_type_audio) +"]";
            case location:
                return "["+ DemoCache.getContext().getString(R.string.input_panel_location) +"]";
            case file:
                return "["+ DemoCache.getContext().getString(R.string.message_type_file) +"]";
            case tip:
                List<String> uuids = new ArrayList<>();
                uuids.add(recent.getRecentMessageId());
                List<IMMessage> messages = NIMClient.getService(MsgService.class).queryMessageListByUuidBlock(uuids);
                if (messages != null && messages.size() > 0) {
                    return messages.get(0).getContent();
                }
                return "["+ DemoCache.getContext().getString(R.string.message_type_notification) +"]";
            case robot:
                return "["+ DemoCache.getContext().getString(R.string.message_type_robot) +"]";
            default:
                return "["+ DemoCache.getContext().getString(R.string.message_type_custom) +"]";
        }
    }
}
