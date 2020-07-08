package com.sdy.jitangapplication.nim.session;

import android.app.Activity;
import android.content.Intent;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.sdy.jitangapplication.nim.uikit.business.session.module.Container;

import java.io.Serializable;
import java.lang.ref.WeakReference;

/**
 * Action基类。<br>
 * 注意：在子类中调用startActivityForResult时，requestCode必须用makeRequestCode封装一遍，否则不能再onActivityResult中收到结果。
 * requestCode仅能使用最低8位。
 */
public abstract class ChatBaseAction implements Serializable {

    private int iconResId;
    private int iconResIdChoose;
    private int iconResIdDisable;

    public int getIconResIdChoose() {
        return iconResIdChoose;
    }

    public int getIconResIdDisable() {
        return iconResIdDisable;
    }

    public void setIconResIdDisable(int iconResIdDisable) {
        this.iconResIdDisable = iconResIdDisable;
    }

    public void setIconResIdChoose(int iconResIdChoose) {
        this.iconResIdChoose = iconResIdChoose;
    }

    private boolean check;

    private boolean enable = true;

    private int titleId;

    private transient int index;

    // Container持有activity ， 防止内存泄露
    private transient WeakReference<Container> containerRef;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    /**
     * 构造函数
     *
     * @param iconResId 图标 res id
     */
    protected ChatBaseAction(int iconResIdChoose, int iconResId, int titleId) {
        this.iconResIdChoose = iconResIdChoose;
        this.iconResId = iconResId;
        this.titleId = titleId;
    }

    public Activity getActivity() {
        return getContainer().activity;
    }

    public String getAccount() {
        return getContainer().account;
    }

    public SessionTypeEnum getSessionType() {
        return getContainer().sessionType;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getTitleId() {
        return titleId;
    }

    public Container getContainer() {
        Container container = containerRef.get();
        if (container == null) {
            throw new RuntimeException("container be recycled by vm ");
        }
        return container;
    }

    public abstract void onClick();

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // default: empty
    }

    protected void sendMessage(IMMessage message) {
        getContainer().proxy.sendMessage(message);
    }

    protected int makeRequestCode(int requestCode) {
        if ((requestCode & 0xffffff00) != 0) {
            throw new IllegalArgumentException("Can only use lower 8 bits for requestCode");
        }
        return ((index + 1) << 8) + (requestCode & 0xff);
    }

    public void setContainer(Container container) {
        this.containerRef = new WeakReference<>(container);
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
