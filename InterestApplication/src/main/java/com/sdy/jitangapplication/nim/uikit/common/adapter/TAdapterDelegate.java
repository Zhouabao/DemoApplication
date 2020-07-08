package com.sdy.jitangapplication.nim.uikit.common.adapter;

public interface TAdapterDelegate {

    public int getViewTypeCount();

    public Class<? extends TViewHolder> viewHolderAtPosition(int position);

    public boolean enabled(int position);
}