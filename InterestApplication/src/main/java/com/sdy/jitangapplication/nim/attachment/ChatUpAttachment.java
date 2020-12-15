package com.sdy.jitangapplication.nim.attachment;

import com.alibaba.fastjson.JSONObject;
import com.sdy.jitangapplication.nim.extension.CustomAttachmentType;

/**
 * author : ZFM
 * date   : 2019/8/1014:48
 * desc   : 搭讪语消息
 * version: 1.0
 */
public class ChatUpAttachment extends CustomAttachment {
    private String chatUpContent;

    private final String KEY_CHAT_UP_CONTENT = "chatUpContent";

    public ChatUpAttachment() {
        super(CustomAttachmentType.ChatUp);
    }

    public ChatUpAttachment(String chatUpContent) {
        super(CustomAttachmentType.ChatUp);
        this.chatUpContent = chatUpContent;
    }

    @Override
    protected void parseData(JSONObject data) {
        chatUpContent = data.getString(KEY_CHAT_UP_CONTENT);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_CHAT_UP_CONTENT, chatUpContent);
        return data;
    }

    public String getChatUpContent() {
        return chatUpContent;
    }

    public void setChatUpContent(String chatUpContent) {
        this.chatUpContent = chatUpContent;
    }
}
