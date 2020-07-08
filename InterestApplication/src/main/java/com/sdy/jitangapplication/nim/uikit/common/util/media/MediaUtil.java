package com.sdy.jitangapplication.nim.uikit.common.util.media;

import com.sdy.jitangapplication.nim.uikit.common.media.model.GLImage;
import com.sdy.jitangapplication.nim.uikit.common.util.sys.TimeUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 */
public class MediaUtil {
    public static Map<String, List<GLImage>> divideMedias(List<GLImage> images) {
        Map<String, List<GLImage>> imageItemMap = new LinkedHashMap<>();
        for (int i = 0; i < images.size(); i++) {
            GLImage GLImage = images.get(i);
            String date = TimeUtil.getDateString(GLImage.getAddTime());
            if (imageItemMap.get(date) != null) {
                List<com.sdy.jitangapplication.nim.uikit.common.media.model.GLImage> GLImageList = imageItemMap.get(date);
                GLImageList.add(GLImage);
                imageItemMap.put(date, GLImageList);
            } else {
                List<com.sdy.jitangapplication.nim.uikit.common.media.model.GLImage> GLImageList = new ArrayList<>(1);
                GLImageList.add(GLImage);
                imageItemMap.put(date, GLImageList);
            }
        }

        return imageItemMap;
    }
}