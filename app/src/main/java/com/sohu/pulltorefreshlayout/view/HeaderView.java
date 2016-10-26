package com.sohu.pulltorefreshlayout.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sohu.pulltorefreshlayout.CommonUtils;
import com.sohu.pulltorefreshlayout.R;

/**
 * Created by shaoyinghui on 2016/10/20.
 */

public class HeaderView extends LinearLayout {

    private Context mContext;
    private LinearLayout headerView;
    private ImageView arrowImageView;
    private ProgressBar headerProgressBar;
    private Bitmap arrowBitmap;
    private TextView tipsTextView;


    private int mState = -1;

    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;
    private final int ROTATE_ANIM_DURATION = 180;

    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_REFRESHING = 2;

    private int headerHeight;

    public HeaderView(Context context) {
        this(context, null);
    }

    public HeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }


    private void initView() {

        headerView = new LinearLayout(mContext);
        headerView.setOrientation(LinearLayout.HORIZONTAL);
        headerView.setGravity(Gravity.CENTER);
        headerView.setPadding(0, CommonUtils.dp2px(mContext, 10), 0, CommonUtils.dp2px(mContext, 10));//设置padding

        FrameLayout headImage = new FrameLayout(mContext);
        arrowBitmap = CommonUtils.getBitmapFromSrc(mContext, R.drawable.arrow);
        arrowImageView=new ImageView(mContext);
        arrowImageView.setImageBitmap(arrowBitmap);

        headerProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleSmall);
        headerProgressBar.setVisibility(View.GONE);

        LayoutParams imgLp = new LayoutParams(-2, -2);
        imgLp.gravity = Gravity.CENTER;
        imgLp.width = CommonUtils.dp2px(mContext, 40);
        imgLp.height = CommonUtils.dp2px(mContext, 40);
        headImage.addView(arrowImageView, imgLp);
        headImage.addView(headerProgressBar, imgLp);

        LinearLayout headTextLayout = new LinearLayout(mContext);
        headTextLayout.setOrientation(LinearLayout.VERTICAL);
        headTextLayout.setGravity(Gravity.CENTER);
        tipsTextView = new TextView(mContext);
        tipsTextView.setTextColor(Color.DKGRAY);
        tipsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
        LayoutParams textLp = new LayoutParams(-2, -2);
        headTextLayout.addView(tipsTextView, textLp);


        LayoutParams headLp = new LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headLp.gravity = Gravity.CENTER;
        headLp.rightMargin = CommonUtils.dp2px(mContext, 10);
        LinearLayout headerLayout = new LinearLayout(mContext);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setGravity(Gravity.CENTER);
        headerLayout.addView(headImage, headLp);
        headerLayout.addView(headTextLayout, headLp);

        LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.BOTTOM;
        headerView.addView(headerLayout, lp);

        this.addView(headerView, lp);
        CommonUtils.measureView(this);
        headerHeight = this.getMeasuredHeight();

        mRotateUpAnim = new RotateAnimation(0.0f, -180f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);

        mRotateDownAnim= new RotateAnimation(-180f,0.0f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);

    }


    public void setState(int state){
        if(state==mState){
            return ;
        }
        if(state==STATE_REFRESHING){
            arrowImageView.clearAnimation();
            arrowImageView.setVisibility(View.GONE);
            headerProgressBar.setVisibility(View.VISIBLE);
        }else{
            arrowImageView.setVisibility(View.VISIBLE);
            headerProgressBar.setVisibility(View.GONE);
        }
        switch (state){
            case STATE_NORMAL:
                if(mState==STATE_READY){
                    arrowImageView.startAnimation(mRotateDownAnim);
                }
                if(mState==STATE_REFRESHING){
                    arrowImageView.clearAnimation();
                }
                tipsTextView.setText("下拉刷新");
                break;
            case STATE_READY:
                if (mState != STATE_READY) {
                    arrowImageView.clearAnimation();
                    arrowImageView.startAnimation(mRotateUpAnim);
                    tipsTextView.setText("松开以刷新");
                }
                break;
            case STATE_REFRESHING:
                tipsTextView.setText("正在刷新...");
                break;
            default:
        }
        mState=state;
    }


    public int getHeaderHeight() {
        return headerHeight;
    }


    public int getState() {
        return mState;
    }
}
