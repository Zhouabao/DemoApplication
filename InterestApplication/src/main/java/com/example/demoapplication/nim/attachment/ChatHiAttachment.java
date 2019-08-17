package com.example.demoapplication.nim.attachment;

import com.alibaba.fastjson.JSONObject;
import com.example.demoapplication.nim.extension.CustomAttachmentType;

/**
 * author : ZFM
 * date   : 2019/8/1014:48
 * desc   : 匹配的attachment
 * version: 1.0
 */
public class ChatHiAttachment extends CustomAttachment {
    private String matchTag;//匹配下的标签
    private int showType;//是什么方式  1.匹配  2.招呼 3.好友
    public static int CHATHI_MATCH = 1;
    public static int CHATHI_HI = 2;
    public static int CHATHI_RFIEND = 3;

    private final String KEY_TAG = "matchTag"; //匹配下的标签
    private final String KEY_SHOWTYPE = "showType";//是什么方式  1.匹配  2.招呼 3.好友

    public ChatHiAttachment() {
        super(CustomAttachmentType.ChatHi);
    }

    public ChatHiAttachment(String tag, int showType) {
        super(CustomAttachmentType.ChatHi);
        this.matchTag = tag;
        this.showType = showType;
    }

    @Override
    protected void parseData(JSONObject data) {
        matchTag = data.getString(KEY_TAG);
        showType = data.getInteger(KEY_SHOWTYPE);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_TAG, matchTag);
        data.put(KEY_SHOWTYPE, showType);
        return data;
    }

    public String getTag() {
        return matchTag;
    }

    public void setTag(String tag) {
        this.matchTag = tag;
    }

    public int getShowType() {
        return showType;
    }

    public void setShowType(int showType) {
        this.showType = showType;
    }
}
