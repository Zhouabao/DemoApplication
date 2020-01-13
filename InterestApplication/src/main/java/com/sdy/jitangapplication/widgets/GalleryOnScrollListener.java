package com.sdy.jitangapplication.widgets;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.blankj.utilcode.util.SizeUtils;
import com.sdy.jitangapplication.R;

public class GalleryOnScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        slideInAnimation(recyclerView);
    }

    /**
     * 滑入时的动画
     */
    private void slideInAnimation(RecyclerView recyclerView) {
        float percent = getScrollPercent(recyclerView);
        if (percent > 0 && percent < 1) {
            //屏幕中只能同时显示两个item，所以直接找到第二个可见item就是需要实现动画效果的item
            View targetView = getTargetView(recyclerView, 1);
            if (targetView == null) {
                return;
            }
            //根据滑动的距离改变item的大小
            ViewGroup.MarginLayoutParams lp = ((ViewGroup.MarginLayoutParams) targetView.getLayoutParams());
            lp.width = (int) (SizeUtils.dp2px(280F) * (0.89 + 0.11 * percent));
            lp.height = (int) (SizeUtils.dp2px(280F) * (0.89 + 0.11 * percent));
            targetView.setLayoutParams(lp);
        }
    }

    /**
     * 计算滑动的百分比
     */
    private float getScrollPercent(RecyclerView recyclerView) {
        View firstItem = recyclerView.getChildAt(0);
        if (firstItem == null) {
            return 0;
        }
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int scrollDistance = firstItem.getWidth() - layoutManager.getDecoratedRight(firstItem);
        return scrollDistance * 1.0f / firstItem.getWidth();
    }

    private View getTargetView(RecyclerView recyclerView, int index) {
        View view = recyclerView.getChildAt(index);
        if (view == null) {
            return null;
        }
        return view.findViewById(R.id.ivUser);
    }
}
