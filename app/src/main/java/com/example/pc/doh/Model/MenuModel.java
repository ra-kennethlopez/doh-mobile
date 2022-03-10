package com.example.pc.doh.Model;

import android.graphics.drawable.Drawable;

public class MenuModel {

    public String menuName, url;
    public boolean hasChildren, isGroup;
    public int drawable;
    public int index;
    public MenuModel(String menuName, boolean isGroup, boolean hasChildren,int drawable,int index) {

        this.menuName =" "+menuName;
        this.isGroup = isGroup;
        this.hasChildren = hasChildren;
        this.drawable = drawable;
        this.index = index;
    }




}