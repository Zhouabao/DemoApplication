package com.example.demoapplication.nim.extension;

import com.alibaba.fastjson.JSONObject;

/**
 * author : ZFM
 * date   : 2019/8/129:32
 * desc   : 广场分享的attachment
 * version: 1.0
 */
public class ShareSquareAttachment extends CustomAttachment {
    private final String KEY_DESC = "desc"; //分享的描述
    private final String KEY_CONTENT = "content";//分享的文本内容
    private final String KEY_TYPE = "shareType";//分享的类型
    private final String KEY_IMG = "img";//分享的图片
    private final String KEY_ID = "id";//分享的图片

    private String desc;//匹配的标签
    private String content;//分享的文本内容
    private int shareType;//分享的类型 1图片 2视频 3语音
    private String img;//分享的图片
    private int id;//分享的广场id

    ShareSquareAttachment() {
        super(CustomAttachmentType.ShareSquare);
    }

    public ShareSquareAttachment(String desc, String content, int shareType, String img, int id) {
        super(CustomAttachmentType.ShareSquare);
        this.desc = desc;
        this.content = content;
        this.shareType = shareType;
        this.img = img;
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getShareType() {
        return shareType;
    }

    public void setShareType(int shareType) {
        this.shareType = shareType;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    protected void parseData(JSONObject data) {
        desc = data.getString(KEY_DESC);
        content = data.getString(KEY_CONTENT);
        img = data.getString(KEY_IMG);
        shareType = data.getInteger(KEY_TYPE);
        id = data.getInteger(KEY_ID);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_DESC, desc);
        data.put(KEY_CONTENT, content);
        data.put(KEY_IMG, img);
        data.put(KEY_TYPE, shareType);
        data.put(KEY_ID, id);
        return data;
    }
}
