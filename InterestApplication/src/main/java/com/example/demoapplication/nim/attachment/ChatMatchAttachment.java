package com.example.demoapplication.nim.attachment;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demoapplication.model.Tag;
import com.example.demoapplication.nim.extension.CustomAttachmentType;

import java.util.ArrayList;
import java.util.List;

/**
 * author : ZFM
 * date   : 2019/8/1014:48
 * desc   : 匹配的attachment
 * version: 1.0
 */
public class ChatMatchAttachment extends CustomAttachment {
    private final String KEY_MATCH_TAG = "matchtag";
    private final String KEY_TAGS = "tags";
    private final String KEY_AVATOR = "avator";

    private String matchTag;//匹配的标签
    private String avator;//头像
    private List<Tag> tags = new ArrayList<>();//用户所拥有的标签

    public ChatMatchAttachment() {
        super(CustomAttachmentType.ChatMatch);
    }

    public ChatMatchAttachment(String matchTag, List<Tag> tags, String avator) {
        super(CustomAttachmentType.ChatMatch);
        this.matchTag = matchTag;
        this.tags = tags;
        this.avator = avator;
    }

    public String getMatchTag() {
        return matchTag;
    }

    public void setMatchTag(String matchTag) {
        this.matchTag = matchTag;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String getAvator() {
        return avator;
    }

    public void setAvator(String avator) {
        this.avator = avator;
    }

    @Override
    protected void parseData(JSONObject data) {
        matchTag = data.getString(KEY_MATCH_TAG);
        avator = data.getString(KEY_AVATOR);
        JSONArray tages = data.getJSONArray(KEY_TAGS);
        for (int i = 0; i < tages.size(); i++) {
            JSONObject tagesJSONObject = tages.getJSONObject(i);
            Tag tag = new Tag(tagesJSONObject.getString("icon"), tagesJSONObject.getInteger("id"), tagesJSONObject.getString("title"), tagesJSONObject.getBoolean("sameLabel"));
            tags.add(tag);
        }
    }

    @Override
    protected JSONObject packData() {
        JSONArray tags1 = new JSONArray();
        for (int i = 0; i < this.tags.size(); i++) {
            JSONObject tag = new JSONObject();
            tag.put("icon", tags.get(i).getIcon());
            tag.put("id", tags.get(i).getId());
            tag.put("title", tags.get(i).getTitle());
            tag.put("sameLabel", tags.get(i).getSameLabel());
            tags1.add(tag);
        }
        JSONObject data = new JSONObject();
        data.put(KEY_MATCH_TAG, matchTag);
        data.put(KEY_AVATOR, avator);
        data.put(KEY_TAGS, tags1);
        return data;
    }
}
