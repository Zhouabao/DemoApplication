package com.example.baselibrary.glide;

import android.graphics.*;
import android.util.TypedValue;
import com.blankj.utilcode.util.SizeUtils;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

/**
 * @author GuZhC
 * @create 2018/4/20
 * @Describe
 */
public class CornersTranform extends BitmapTransformation {
    private float radius;

    public CornersTranform() {
        super();
        radius = SizeUtils.applyDimension(10, TypedValue.COMPLEX_UNIT_DIP);
    }

    public CornersTranform(float radius) {
        super();
        this.radius = radius;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return cornersCrop(pool, toTransform);
    }

    private Bitmap cornersCrop(BitmapPool pool, Bitmap source) {
        if (source == null) return null;

        Bitmap result = pool.get(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);

        if (result != null && !result.isRecycled()) {
            result.recycle();
            result = null;
        }

        if (result == null) {
            result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        RectF rectF = new RectF(0f, 0f, source.getWidth(), source.getHeight());
        canvas.drawRoundRect(rectF, SizeUtils.applyDimension(radius, TypedValue.COMPLEX_UNIT_DIP), SizeUtils.applyDimension(radius, TypedValue.COMPLEX_UNIT_DIP), paint);
        return result;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {

    }
}
