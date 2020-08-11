package com.aqueleangelo.myfirstandroidapp;

import android.util.Log;

public class ListItemCard {
    private String mTopText;
    private int mTime;
    private boolean mChecked;

    public ListItemCard(String topText, int time, Boolean checked){
        mTopText = topText;
        mTime = time;
        mChecked = checked;
    }

    public String getTopText() { return mTopText; }
    public int getTime() { return mTime; }
    public Boolean isChecked() { return mChecked; }

    public void changeTopText(String text){ mTopText = text; }
    public void changeTime(int time){ mTime = time; }
    public void changeChecked(Boolean chk) { mChecked = chk; }

}
