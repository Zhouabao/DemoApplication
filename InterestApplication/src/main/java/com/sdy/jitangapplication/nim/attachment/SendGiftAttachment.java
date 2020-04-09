package com.sdy.jitangapplication.nim.attachment;

import com.alibaba.fastjson.JSONObject;
import com.sdy.jitangapplication.nim.extension.CustomAttachmentType;

/**
 * author : ZFM
 * date   : 2019/8/129:32
 * desc   : 广场分享的attachment
 * version: 1.0
 */
public class SendGiftAttachment extends CustomAttachment {
    private final String KEY_TITLE = "title"; //分享的描述
    private final String KEY_IMG = "icon";//分享的图片
    private final String KEY_ID = "giftId";//分享的图片

    private String title;//匹配的兴趣
    private String img;//分享的图片
    private int giftId;//分享的广场id

    public SendGiftAttachment() {
        super(CustomAttachmentType.SendGift);
    }

    public SendGiftAttachment(String title, String img, int giftId) {
        super(CustomAttachmentType.SendGift);
        this.title = title;
        this.img = img;
        this.giftId = giftId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String desc) {
        this.title = title;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getId() {
        return giftId;
    }

    public void setId(int id) {
        this.giftId = id;
    }

    @Override
    protected void parseData(JSONObject data) {
        title = data.getString(KEY_TITLE);
        img = data.getString(KEY_IMG);
        giftId = data.getInteger(KEY_ID);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_TITLE, title);
        data.put(KEY_IMG, img);
        data.put(KEY_ID, giftId);
        return data;
    }
}
