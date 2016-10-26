package com.sohu.pulltorefreshlayout.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.sohu.pulltorefreshlayout.CommonUtils;

/**
 * Created by shaoyinghui on 2016/10/20.
 */

public class FootView extends LinearLayout {
    private Context mContext;
    private LinearLayout footView;
    private ProgressBar mProgressBar;
    private int footHeight;
    public FootView(Context context) {
        this(context,null);
    }

    public FootView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.mContext=context;
        this.setOrientation(LinearLayout.HORIZONTAL);
        footView=new LinearLayout(mContext);
        mProgressBar=new ProgressBar(mContext,null,android.R.attr.progressBarStyleSmall);
        LinearLayout.LayoutParams progressLp=new LinearLayout.LayoutParams(-2,-2);
        progressLp.gravity= Gravity.CENTER;
        progressLp.width= CommonUtils.dp2px(mContext,40);
        progressLp.height= CommonUtils.dp2px(mContext,40);
        footView.addView(mProgressBar,progressLp);
        LinearLayout.LayoutParams footLp=new LinearLayout.LayoutParams(-2,-2);
        footLp.gravity=Gravity.CENTER;
        footView.setGravity(Gravity.CENTER);
        this.addView(footView,footLp);
        CommonUtils.measureView(this);
        footHeight=this.getMeasuredHeight();
    }

    public int getFootViewHeight() {
        return footHeight;
    }
    private int mSate;
    public void setState(int state) {
        if(mSate==state){
            return;
        }
        if(state== HeaderView.STATE_REFRESHING||state== HeaderView.STATE_READY){
            this.setVisibility(View.VISIBLE);
        }else{
            this.setVisibility(View.GONE);
        }
        mSate=state;
    }
}
