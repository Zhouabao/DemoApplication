package com.example.baselibrary.retrofit;

/**
 * author : ZFM
 * date   : 2019/8/309:57
 * desc   :
 * version: 1.0
 */
public interface UploadProgressListener {
    /**
     * 上传进度
     *
     * @param currentBytesCount
     * @param totalBytesCount
     */
    void onProgress(long currentBytesCount, long totalBytesCount);
}