package com.sdy.jitangapplication.nim.attachment;

import com.alibaba.fastjson.JSONObject;
import com.sdy.jitangapplication.nim.extension.CustomAttachmentType;

/**
 * author : ZFM
 * date   : 2019/8/129:32
 * desc   : 广场分享的attachment
 * version: 1.0
 */
public class ChatDatingAttachment extends CustomAttachment {
    private final String KEY_DATING_CONTENT = "content";// 约会内容
    private final String KEY_DATING_ICON = "icon";// 约会类型图
    private final String KEY_DATING_ID = "datingId";// 约会id

    private String content;// 分享的文本内容
    private String img;// 分享的图片
    private int datingId;// 分享的广场id

    public ChatDatingAttachment() {
        super(CustomAttachmentType.ChatDating);
    }

    public ChatDatingAttachment(String content, String img, int datingId) {
        super(CustomAttachmentType.ChatDating);
        this.content = content;
        this.img = img;
        this.datingId = datingId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getId() {
        return datingId;
    }

    public void setId(int id) {
        this.datingId = id;
    }

    @Override
    protected void parseData(JSONObject data) {
        content = data.getString(KEY_DATING_CONTENT);
        img = data.getString(KEY_DATING_ICON);
        datingId = data.getInteger(KEY_DATING_ID);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_DATING_CONTENT, content);
        data.put(KEY_DATING_ICON, img);
        data.put(KEY_DATING_ID, datingId);
        return data;
    }
}
