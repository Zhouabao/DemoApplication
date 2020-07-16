package com.sdy.jitangapplication.nim.attachment;

import com.alibaba.fastjson.JSONObject;
import com.sdy.jitangapplication.nim.extension.CustomAttachmentType;

/**
 * author : ZFM
 * date   : 2019/8/129:32
 * desc   : 发送联系方式
 * version: 1.0
 */
public class ContactCandyAttachment extends CustomAttachment {
    private final String KEY_CONTACT_CANDY = "amount";// 获取联系方式赠送的糖果

    private int contactCandy;// 礼物的订单id

    public ContactCandyAttachment() {
        super(CustomAttachmentType.ChatContactCandy);
    }

    public ContactCandyAttachment(int contactCandy) {
        super(CustomAttachmentType.ChatContactCandy);
        this.contactCandy = contactCandy;
    }

    public int getContactCandy() {
        return contactCandy;
    }

    public void setContactCandy(int contactCandy) {
        this.contactCandy = contactCandy;
    }

    @Override
    protected void parseData(JSONObject data) {
        contactCandy = data.getInteger(KEY_CONTACT_CANDY);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_CONTACT_CANDY, contactCandy);
        return data;
    }
}
