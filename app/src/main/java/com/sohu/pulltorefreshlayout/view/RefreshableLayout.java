package com.sohu.pulltorefreshlayout.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;


/**
 * Created by shaoyinghui on 2016/10/21.
 */
public class RefreshableLayout extends LinearLayout implements AbsListView.OnScrollListener {

    private Context mContext;
    private boolean mEnablePullRefresh=true;
    private boolean mEnableLoadMore=true;

    private int mLastMotionX;
    private int mLastMotionY;

    private HeaderView mHeaderView;
    private AdapterView<?>mAdapterView;
    private ScrollView mScrollerView;

    private int mHeaderViewHeight;
    private int mPullState;
    private static final int PULL_UP_STATE=0;
    private static final int PULL_DOWN_STATE=1;
    private int mCount=0;
    private boolean mPullRefreshing=false;
    private boolean mPullLoading=false;
    private IOnHeaderRefreshListener onHeaderRefreshListener;


    private FootView mFootView;
    private int mFootViewHeight;
    private IOnfootRefreshListener onfootRefreshListener;
    public RefreshableLayout(Context context) {
        this(context, null);
    }

    public RefreshableLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setOrientation(LinearLayout.VERTICAL);
        this.mContext=context;
        addHeaderView();
    }


    private void addHeaderView() {
        mHeaderView=new HeaderView(mContext);
        mHeaderViewHeight=mHeaderView.getHeaderHeight();
        mHeaderView.setGravity(Gravity.BOTTOM);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mHeaderViewHeight);
        params.topMargin = -mHeaderViewHeight;
        addView(mHeaderView, params);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addFootView();
        initContentAdapterView();
    }

    private void addFootView() {
        mFootView=new FootView(mContext);
        mFootViewHeight=mFootView.getFootViewHeight();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mFootViewHeight);
        mFootView.setGravity(Gravity.CENTER_HORIZONTAL);
        addView(mFootView, params);
    }

    private void initContentAdapterView() {
        int count=getChildCount();
        if(count<2){
            throw new IllegalArgumentException("this layout must contain 2 child views,and AdapterView or ScrollView must in the second position!");
        }
        View view=null;
        for (int i = 0; i < count; i++) {
            view=getChildAt(i);
            if(view instanceof AdapterView<?>){
                mAdapterView= (AdapterView<?>) view;
                ListView lv= (ListView) mAdapterView;
                lv.setOnScrollListener(this);
            }else if(view instanceof ScrollView){
                mScrollerView = (ScrollView) view;
            }
        }
        if (mAdapterView == null && mScrollerView == null) {
            throw new IllegalArgumentException("must contain a AdapterView or ScrollView in this layout!");
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int x= (int) ev.getX();
        int y= (int) ev.getY();
        int action=ev.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                mLastMotionX=x;
                mLastMotionY=y;
                break;
            case MotionEvent.ACTION_MOVE:
                int duraX=x-mLastMotionX;
                int duraY=y-mLastMotionY;
                if(Math.abs(duraX)<Math.abs(duraY)&&Math.abs(duraY)>10){
                    if(isRefreshScroll(duraY)){
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private boolean isRefreshScroll(int duraY) {
        if(mPullRefreshing||mPullLoading){
            return false;
        }
        if(mAdapterView!=null){
            if(duraY>0){
                if(!mEnablePullRefresh){
                    return false;
                }
                View child=mAdapterView.getChildAt(0);
                if(child==null){
                    return false;
                }
                int top=child.getTop();
                int padding=mAdapterView.getPaddingTop();
                if(mAdapterView.getFirstVisiblePosition()==0&&Math.abs(top - padding)<=11){
                    mPullState=PULL_DOWN_STATE;
                    return true;
                }
            }else if(duraY<0){
                View child = mAdapterView.getChildAt(mAdapterView.getChildCount() - 1);
                if(child!=null&&child.getBottom()<=getMeasuredHeight()&&mAdapterView.getLastVisiblePosition()==mAdapterView.getCount()-1){
                    mPullState=PULL_UP_STATE;
                    return true;
                }
            }
        }
        else if (mScrollerView != null) {
            if (duraY > 0) {
                if (!mEnablePullRefresh) {
                    return false;
                }
                if(mScrollerView.getScrollY()==0){
                    mPullState = PULL_DOWN_STATE;
                    return true;
                }
            } else if (duraY < 0) {
                View child = mScrollerView.getChildAt(0);
                if (child != null &&  child.getMeasuredHeight() <= mScrollerView.getScrollY() + mScrollerView.getHeight()) {
                    mPullState = PULL_UP_STATE;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y= (int) event.getY();
        int action=event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                int duraY=y-mLastMotionY;
                if(mPullState==PULL_DOWN_STATE){
                    headerPrepareToRefresh(duraY);
                }else if(mPullState==PULL_UP_STATE){
                    footPrepareToRefresh(duraY);
                }
                mLastMotionY=y;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                int topMargin=((LayoutParams)mHeaderView.getLayoutParams()).topMargin;
                if(mPullState==PULL_DOWN_STATE){
                    if(topMargin>0){
                        headerRefresh();
                    }else{
                        setHeaderTopMargin(-mHeaderViewHeight);
                    }
                }else if(mPullState==PULL_UP_STATE){
                    if(topMargin<=-(mHeaderViewHeight+mFootViewHeight)){
                        footRefreshing();
                    }else{
                        setHeaderTopMargin(-mHeaderViewHeight);
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }


    private void headerRefresh() {
        mPullRefreshing=true;
        mHeaderView.setState(HeaderView.STATE_REFRESHING);
        setHeaderTopMargin(0);
        if(onHeaderRefreshListener!=null){
            onHeaderRefreshListener.onHeaderRefresh(this);
        }
    }

    private void setHeaderTopMargin(int topMargin) {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        params.topMargin = topMargin;
        mHeaderView.setLayoutParams(params);
        invalidate();
    }

    private void headerPrepareToRefresh(int duraY) {
        if(mPullRefreshing||mPullLoading){
            return ;
        }

        int newTopMargin=updateHeaderViewTopMargin(duraY);

        if(newTopMargin>=0&&mHeaderView.getState()!= HeaderView.STATE_REFRESHING){
            mHeaderView.setState(HeaderView.STATE_READY);
        }else if(newTopMargin<0&&newTopMargin>-mHeaderViewHeight){
            mHeaderView.setState(HeaderView.STATE_NORMAL);
        }
    }


    private void footPrepareToRefresh(int duraY) {
        if(mPullRefreshing||mPullLoading){
            return ;
        }
        int newTopMargin=updateHeaderViewTopMargin(duraY);
        mFootView.setState(HeaderView.STATE_READY);
    }



    private int updateHeaderViewTopMargin(int duraY) {
        LayoutParams params= (LayoutParams) mHeaderView.getLayoutParams();

        float newTopMargin=params.topMargin+duraY*0.3f;
        if(duraY>0&&mPullState==PULL_UP_STATE&&Math.abs(params.topMargin)<=mHeaderViewHeight){
            return params.topMargin;
        }
        if(duraY<0&&mPullState==PULL_DOWN_STATE&&Math.abs(params.topMargin)>=mHeaderViewHeight){
            return params.topMargin;
        }
        params.topMargin= (int) newTopMargin;
        mHeaderView.setLayoutParams(params);
        invalidate();
        return params.topMargin;
    }

    public void refreshComplete(){
        mPullLoading=false;//是否上拉加载中置为false
        mPullRefreshing=false;//是否下拉加载置为false
        mHeaderView.setState(HeaderView.STATE_NORMAL);
        setHeaderTopMargin(-mHeaderViewHeight);
        mFootView.setState(HeaderView.STATE_NORMAL);
    }

    private int mScrollState;
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mScrollState=scrollState;
    }
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(mAdapterView!=null&&mAdapterView.getCount()>0){
            View child = mAdapterView.getChildAt(mAdapterView.getChildCount() - 1);
            if(child!=null&&child.getBottom()<=getMeasuredHeight()&&mAdapterView.getLastVisiblePosition()==mAdapterView.getCount()-1){
                if(mScrollState== AbsListView.OnScrollListener.SCROLL_STATE_FLING){
                    footRefreshing();
                }
            }
        }
    }


    private void footRefreshing() {
        if(mPullLoading||mPullRefreshing){
            return;
        }
        mPullLoading=true;
        mFootView.setState(HeaderView.STATE_REFRESHING);
        setHeaderTopMargin(-(mFootViewHeight + mHeaderViewHeight));
        if(onfootRefreshListener!=null){
            onfootRefreshListener.onFootRefresh(this);
        }
    }

    public interface IOnHeaderRefreshListener{
        void onHeaderRefresh(RefreshableLayout view);
    }

    public interface IOnfootRefreshListener{
        void onFootRefresh(RefreshableLayout view);
    }

    public void setHeaderRefreshListener(IOnHeaderRefreshListener onHeaderRefreshListener){
        this.onHeaderRefreshListener=onHeaderRefreshListener;
    }

    public void setOnfootRefreshListener(IOnfootRefreshListener onfootRefreshListener) {
        this.onfootRefreshListener = onfootRefreshListener;
    }
}
