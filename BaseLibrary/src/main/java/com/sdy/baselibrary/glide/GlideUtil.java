package com.sdy.baselibrary.glide;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.RemoteViews;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.sdy.baselibrary.R;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import java.io.File;

/**
 * @author Zhou Fengmei
 * @create 2018/4/20
 * @Describe glide模板
 */

public class GlideUtil {
    private static RequestOptions getOptions() {
        return new RequestOptions()
                .fitCenter()
                .priority(Priority.HIGH)
                .placeholder(R.drawable.default_image)
                .error(R.drawable.default_image)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
    }

    /**
     * 返回的图片路径为Object类型，由于不能确定你到底使用的那种图片加载器，
     * 传输的到的是什么格式，那么这种就使用Object接收和返回，你只需要强转成你传输的类型就行，
     * 切记不要胡乱强转！
     */

    public static void loadImg(Context context, Object url, ImageView targetImg) {
        Glide.with(context)
                .load(url)
                .apply(getOptions())
                .into(targetImg);
    }


    /**
     * 返回的图片路径为Object类型，由于不能确定你到底使用的那种图片加载器，
     * 传输的到的是什么格式，那么这种就使用Object接收和返回，你只需要强转成你传输的类型就行，
     * 切记不要胡乱强转！
     */

    public static void loadAvatorImg(Context context, Object url, ImageView targetImg) {
        Glide.with(context)
                .load(url)
                .apply(getOptions().placeholder(R.drawable.default_avator_image).error(R.drawable.default_avator_image))
                .into(targetImg);
    }


    /**
     * 返回的图片路径为Object类型，由于不能确定你到底使用的那种图片加载器，
     * 传输的到的是什么格式，那么这种就使用Object接收和返回，你只需要强转成你传输的类型就行，
     * 切记不要胡乱强转！
     */

    public static void loadImgWithError(Context context, Object url, ImageView targetImg) {
        Glide.with(context)
                .load(url)
                .apply(getOptions().error(R.drawable.icon_default_avator))
                .into(targetImg);
    }


    /**
     * 加载圆形图片
     *
     * @param context
     * @param url
     * @param targetImg
     */
    public static void loadCircleImg(Context context, Object url, ImageView targetImg) {
        Glide.with(context)
                .load(url)
                .apply(getOptions().circleCrop())
                .into(targetImg);
    }


    /**
     * 加载圆角图片内部居中
     *
     * @param context
     * @param url
     * @param tartgetImg
     * @param scale      是否缩放 0.0-1.0F
     */
    public static void loadRoundImgCenterinside(Context context, String url, ImageView tartgetImg, float scale, int radius) {
        MultiTransformation multiTransformation = new MultiTransformation(new CenterInside(), new RoundedCornersTransformation(radius, 0));
        Glide.with(context)
                .load(url)
                .priority(Priority.NORMAL)
                .thumbnail(scale)
                .transform(multiTransformation)
                .into(tartgetImg);

    }

    /**
     * 加载圆角图片铺满居中
     *
     * @param context
     * @param url
     * @param tartgetImg
     */
    public static void loadRoundImgCenterCrop(Context context, String url, ImageView tartgetImg, int radius) {
        MultiTransformation multiTransformation = new MultiTransformation(new CenterCrop(), new RoundedCornersTransformation(radius, 0));
        Glide.with(context)
                .load(url)
                .priority(Priority.NORMAL)
                .thumbnail(0.5F)
                .transform(multiTransformation)
                .into(tartgetImg);

    }

    /**
     * 加载圆角图片铺满居中
     *
     * @param context
     * @param url
     * @param tartgetImg
     */
    public static void loadImgCenterCrop(Context context, String url, ImageView tartgetImg) {
        Glide.with(context)
                .load(url)
                .priority(Priority.NORMAL)
                .thumbnail(0.1F)
                .transform(new CenterInside())
                .into(tartgetImg);

    }


    /**
     * 根据资源ID加载图片
     *
     * @param context
     * @param resourceId
     * @param target
     */
    public static void loadResourseImg(Context context, int resourceId, ImageView target) {
        Glide.with(context)
                .load(resourceId)
                .apply(getOptions())
                .into(target);
    }


    /**
     * 加载图片不需要缓存的
     *
     * @param context
     * @param url
     * @param target
     */
    public static void loadSourseImgWithNoCache(Context context, String url, ImageView target) {
        Glide.with(context)
                .load(url)
                .apply(getOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true))
                .into(target);
    }


    /**
     * 根据图片路径加载图片
     *
     * @param context
     * @param imgFile
     * @param target
     * @param defaultId
     */
    public static void loadFileImg(Context context, File imgFile, ImageView target, int defaultId) {
        Glide.with(context)
                .load(imgFile)
                .apply(getOptions())
                .into(target);
    }

    /**
     * 加载Gif为一张静态图片
     *
     * @param context
     * @param url
     */
    public static void LoadGiftAsBitmap(Context context, String url, ImageView imageView) {
        Glide.with(context).asBitmap().apply(getOptions()).load(url).into(imageView);
    }

    /**
     * 你想只有加载对象是Gif时才能加载成功
     *
     * @param context
     * @param url
     */
    public static void LoadGiftAsGist(Context context, String url, ImageView imageView, int erroId) {
        Glide.with(context).asGif().apply(getOptions()).load(url).into(imageView);
    }

    /**
     * 加载缩略图,会自动与传入的fragment绑定生命周期,加载请求现在会自动在onStop中暂停在，onStart中重新开始。
     * 需要保证 ScaleType 的设置是正确的。
     *
     * @param fragment
     * @param url
     * @param imageView
     */
    public static void LoadThumbNail(Fragment fragment, String url, ImageView imageView) {
        Glide.with(fragment).load(url).apply(getOptions()).thumbnail(0.1f).into(imageView);
    }

    /**
     * 上传一张大小为xPx*yPx像素的用户头像的图片bytes数据
     *
     * @param context
     * @param url
     * @param xPx
     * @param yPx
     */
    public static void decodeResorse(Context context, File url, int xPx, int yPx) {
        Glide.with(context)
                .load(url)
                .apply(getOptions())
                .into(new SimpleTarget<Drawable>(xPx, yPx) {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        //上传动作
                    }
                });
    }

    /**
     * 显示本地视频(网络视频无效)
     *
     * @param context
     * @param filePath
     * @param imageView
     */
    public static void LoadShowLocalVidio(Context context, String filePath, ImageView imageView) {
        Glide.with(context).load(Uri.fromFile(new File(filePath))).apply(getOptions()).into(imageView);
    }

    /**
     * 在通知栏中显示从网络上请求的图片
     *
     * @param context
     * @param remoteViews
     * @param viewId
     * @param notification
     * @param notificationId
     * @param url
     */
    public static void ShowImgInNotification(Context context, RemoteViews remoteViews, int viewId, Notification
            notification, int notificationId, String url) {
        NotificationTarget target = new NotificationTarget(context, viewId, remoteViews, notification, notificationId);
        Glide.with(context.getApplicationContext()).asBitmap().apply(getOptions()).load(url).into(target);
    }

    /**
     * 下载图片,耗时操作不能放在主线程中进行
     *
     * @param context
     * @param url
     */
    public static void downLoadImage(Context context, String url) {

        try {
            Glide.with(context).asBitmap().apply(getOptions()).load(url).listener(new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean
                        isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource
                        dataSource, boolean isFirstResource) {
                    return false;
                }
            }).submit().get();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 清除缓存
     *
     * @param context
     */
    public void clearCache(final Context context) {
        clearMemoryCache(context);
        new Thread(new Runnable() {
            @Override
            public void run() {
                clearDiskCache(context);
            }
        }).start();
    }

    /**
     * 清除内存缓存
     *
     * @param context
     */
    public void clearMemoryCache(Context context) {
        Glide.get(context).clearMemory();
    }

    /**
     * 清除磁盘缓存
     *
     * @param context
     */
    public void clearDiskCache(Context context) {
        Glide.get(context).clearDiskCache();
    }

}
