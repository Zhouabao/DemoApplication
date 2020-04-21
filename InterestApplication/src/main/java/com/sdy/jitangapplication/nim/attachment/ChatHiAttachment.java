package com.sdy.jitangapplication.nim.attachment;

import com.alibaba.fastjson.JSONObject;
import com.sdy.jitangapplication.nim.extension.CustomAttachmentType;

/**
 * author : ZFM
 * date   : 2019/8/1014:48
 * desc   : 匹配的attachment
 * version: 1.0
 */
public class ChatHiAttachment extends CustomAttachment {
    private int showType;//是什么方式  1.匹配  2.招呼 3.好友 4.过期
    public static int CHATHI_MATCH = 1;
    public static int CHATHI_HI = 2;
    public static int CHATHI_RFIEND = 3;
    public static int CHATHI_OUTTIME = 4;
    public static int CHATHI_WISH_HELP = 5;

    private final String KEY_TAG = "matchTag"; //匹配下的兴趣
    private final String KEY_SHOWTYPE = "showType";//是什么方式  1.匹配  2.招呼 3.好友 4.过期

    public ChatHiAttachment() {
        super(CustomAttachmentType.ChatHi);
    }

    public ChatHiAttachment(int showType) {
        super(CustomAttachmentType.ChatHi);
        this.showType = showType;
    }

    @Override
    protected void parseData(JSONObject data) {
        showType = data.getInteger(KEY_SHOWTYPE);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_SHOWTYPE, showType);
        return data;
    }

    public int getShowType() {
        return showType;
    }

    public void setShowType(int showType) {
        this.showType = showType;
    }
}
