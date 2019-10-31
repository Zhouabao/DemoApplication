package com.sdy.jitangapplication.widgets.cardstackview.internal;

import android.view.animation.Interpolator;
import com.sdy.jitangapplication.widgets.cardstackview.Direction;

public interface AnimationSetting {
    Direction getDirection();
    int getDuration();
    Interpolator getInterpolator();
}
