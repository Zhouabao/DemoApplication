package com.sdy.jitangapplication.widgets.cardstackview.internal;

import android.util.Log;
import androidx.recyclerview.widget.RecyclerView;
import com.blankj.utilcode.util.ScreenUtils;
import com.sdy.jitangapplication.widgets.cardstackview.Direction;


public class CardStackState {
    public Status status = Status.Idle;
    public int width = 0;
    public int height = 0;
    public int dx = 0;
    public int dy = 0;
    public int firstDx = 0;
    public int firstDy = 0;
    public int topPosition = 0;
    public int targetPosition = RecyclerView.NO_POSITION;
    public float proportion = 0.0f;
    public static float LEFTRATIO = 0.2f;

    public enum Status {
        Idle,
        Dragging,
        RewindAnimating,
        AutomaticSwipeAnimating,
        AutomaticSwipeAnimated,
        ManualSwipeAnimating,
        ManualSwipeAnimated;

        public boolean isBusy() {
            return this != Idle;
        }

        public boolean isDragging() {
            return this == Dragging;
        }

        public boolean isSwipeAnimating() {
            return this == ManualSwipeAnimating || this == AutomaticSwipeAnimating;
        }

        public Status toAnimatedStatus() {
            switch (this) {
                case ManualSwipeAnimating:
                    return ManualSwipeAnimated;
                case AutomaticSwipeAnimating:
                    return AutomaticSwipeAnimated;
                default:
                    return Idle;
            }
        }
    }

    public void next(Status state) {
        this.status = state;
    }

    public Direction getDirection() {
        if (firstDx == 0 || firstDy == 0) {
            firstDx = dx;
            firstDy = dy;
        }

        Log.d("getDirection", firstDx + "," + firstDy);

//        if (Math.abs(firstDx) > Math.abs(firstDy)) {
//            if (dx < 0.0f) {
//                return Direction.Left;
//            } else {
//                return Direction.Right;
//            }
//        } else {
//            if (dy < 0.0f) {
//                return Direction.Top;
//            } else {
//                return Direction.Bottom;
//            }
//        }

        if (Math.abs(dy) < Math.abs(dx)) {
            if (dx < 0.0f) {
                return Direction.Left;
            } else {
                return Direction.Right;
            }
        } else {
            if (Math.abs(dx) > ScreenUtils.getScreenWidth() * LEFTRATIO) {
                if (dx < 0.0f) {
                    return Direction.Left;
                } else {
                    return Direction.Right;
                }
            } else {
                if (dy < 0.0f) {
                    return Direction.Top;
                } else {
                    return Direction.Bottom;
                }
            }
        }
    }

    public float getRatio() {
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);
        float ratio;
        if (absDx < absDy) {
            if (Math.abs(dx) > ScreenUtils.getScreenWidth() * LEFTRATIO) {
                ratio = absDx / (width / 2.0f);
            } else
                ratio = absDy / (height / 2.0f);
        } else {
            ratio = absDx / (width / 2.0f);
        }
        return Math.min(ratio, 1.0f);
    }

    public boolean isSwipeCompleted() {
        if (status.isSwipeAnimating()) {
            if (topPosition < targetPosition) {
                if (width < Math.abs(dx) || height < Math.abs(dy)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canScrollToPosition(int position, int itemCount) {
        if (position == topPosition) {
            return false;
        }
        if (position < 0) {
            return false;
        }
        if (itemCount < position) {
            return false;
        }
        if (status.isBusy()) {
            return false;
        }
        return true;
    }

}
