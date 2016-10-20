package com.sohu.pulltorefreshlayout;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * Created by shaoyinghui on 2016/10/19.
 */

public class RefreshableLayout extends ViewGroup implements GestureDetector.OnGestureListener {
    /*手势转换类*/
    private GestureDetector mGesture ;
    /*滚动动画实现类*/
    private Scroller mScroller ;
    /*下来刷新头部View*/
    private View mHeaderView ;
    /*内容View，需要刷新显示的内容*/
    private View mContentView ;
    /*Xml View加载处理类*/
    private LayoutInflater mInflater ;
    /*为了防止重复执行，这里判断是否是第一次*/
    private boolean isFirst = true ;
    /*按下时记录Y轴的坐标*/
    private int mDownY ;

    /*是否开始拖拽*/
    private boolean mIsBeginDrag ;
    /*是否停止拖拽*/
    private boolean mIsStopDrag ;

    private AdapterView mAdapter ;
    /*刷新时动画显示的View*/
    private View mTextView ;
    /*根据此下拉的距离，从而触发下拉操作*/
    private int mRefreshHeight ;
    /*刷新头部的高度*/
    private int mHeaderHeight ;

    private final int STATUS_NORMAL = 0 ;
    private final int STATUS_REFRESH = 1 ;
    private final int STATUS_HIDE = 0 ;

    private int STATUS = STATUS_NORMAL;

    private int mDuration = 500;

    private ImageView mAnimView ;

    private AnimationDrawable mAnimDrawable ;

    public RefreshableLayout(Context context) {
        this(context, null);
    }

    public RefreshableLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initRefreshView() ;
    }

    /**
     * 初始化ViewGroup的一些基本操作
     * 实例化GestureDetector
     * 实例化LayoutInflater
     * 添加头部View
     */
    private void initRefreshView(){
        mGesture = new GestureDetector(getContext(),this) ;
        mInflater = LayoutInflater.from(getContext()) ;
        mScroller = new Scroller(getContext());
        mHeaderView = mInflater.inflate(R.layout.pull_to_refresh,null) ;
        mTextView = mHeaderView.findViewById(R.id.id_txt_header) ;
        mAnimView = (ImageView) mHeaderView.findViewById(R.id.id_anim_header);
        mAnimDrawable = (AnimationDrawable) mAnimView.getDrawable();
        mHeaderHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,130,
                getResources().getDisplayMetrics()) ;
        addView(mHeaderView);
    }


    @Override
    public void addView(View child, int index, LayoutParams params) {
        mContentView = child ;
        if (mContentView instanceof AdapterView){
            mAdapter = (AdapterView)  mContentView;
        }
        super.addView(child, index, params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec) ;
        int height = MeasureSpec.getSize(heightMeasureSpec) ;
        measureChild(mHeaderView,widthMeasureSpec,MeasureSpec.makeMeasureSpec(mHeaderHeight ,MeasureSpec.EXACTLY));
        Log.v("zgy","==========mHeaderView============"+mHeaderView.getMeasuredHeight()) ;
        /*这里不懂的同学可以去参考我前面写的一篇blog 自定义View*/
        measureChild(mContentView, widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
        mRefreshHeight = mTextView.getMeasuredHeight() ;
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mHeaderView.layout(0,0,mHeaderView.getMeasuredWidth(),mHeaderView.getMeasuredHeight());
        mContentView.layout(0,mHeaderView.getMeasuredHeight(),mContentView.getMeasuredWidth(),
                mHeaderView.getMeasuredHeight()+mContentView.getMeasuredHeight());
        if (isFirst){
            scrollTo(0,mHeaderView.getMeasuredHeight());
        }
        isFirst = false ;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP||event.getAction() == MotionEvent.ACTION_CANCEL){
            mIsBeginDrag = false ;
            scrollNormal() ;
        }
        return mGesture.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.v("zgy","====onInterceptTouchEvent====");
        if (mIsBeginDrag){
            return true ;
        }
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            mDownY = (int) ev.getY();
        }
        if (ev.getAction() == MotionEvent.ACTION_MOVE){
            int currentY = (int) ev.getY();
            if (isIntercept(currentY-mDownY)){
                ev.setAction(MotionEvent.ACTION_DOWN);
                onTouchEvent(ev) ;
                requestDisallowInterceptTouchEvent(true);
                mIsBeginDrag = true ;
                return true ;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    private boolean isIntercept(int distance){
        if(distance > 0){
            Log.v("zgy","====mAdapter===="+mAdapter);
            if(mAdapter != null){
                Log.v("zgy","====mAdapter===="+mAdapter);
                View firstChild =  mAdapter.getChildAt(0);
                if(firstChild != null){
                    if (firstChild.getTop() == 0){
                        return true ;
                    }
                }
            }
        }
        return false ;
    }

    private void scrollNormal(){
        if (STATUS == STATUS_REFRESH){
            STATUS = STATUS_HIDE ;
            int scroll = mHeaderHeight - mRefreshHeight -getScrollY() ;
            int currentDuration = (int) (mDuration*0.6f* scroll/(mHeaderHeight - mRefreshHeight));
            mScroller.startScroll(0,getScrollY(),0,scroll,currentDuration);
            /*测试*/
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopRefresh() ;
                }
            },1000) ;
            mAnimView.setVisibility(VISIBLE);
            mAnimDrawable.start();
            invalidate();
        }else if(STATUS == STATUS_HIDE){
            STATUS = STATUS_NORMAL ;
            int scroll = mHeaderHeight - getScrollY() ;
            int currentDuration = mDuration* scroll/mHeaderHeight ;
            mScroller.startScroll(0,getScrollY(),0,scroll,currentDuration);
            mAnimView.setVisibility(View.INVISIBLE);
            mAnimDrawable.stop();
            invalidate();
        }
    }

    public void stopRefresh(){
        scrollNormal() ;
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            Log.v("zgy","======onDown====="+mScroller.getCurrY());
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
    }

    /*-----------------------------OnGestureListener----------------------------------------------*/

    @Override
    public boolean onDown(MotionEvent e) {
        /*根据我前面所讲的Android事件处理全面剖析可知，这里应该返回true*/
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        /*这里是让下拉的View 越拉越紧，给人的感觉时越要用力*/
        distanceY = distanceY *  (0.8f * (getScrollY() * 1.0f / mHeaderHeight));
        int scrollY = cling(0, mHeaderHeight, getScrollY()+(int) distanceY) ;
        Log.v("zgy","=======onScroll===="+distanceY+",scrollY=="+scrollY+",getScrollY()="+getScrollY());
        scrollTo(0,scrollY);
        if (scrollY < mHeaderHeight-mRefreshHeight){
            ((TextView)mTextView).setText("松开可以刷新");
            STATUS = STATUS_REFRESH ;
        }else{
            ((TextView)mTextView).setText("下拉可以刷新");
            STATUS = STATUS_HIDE ;
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    /*-----------------------------OnGestureListener----------------------------------------------*/

    private int cling(int min,int max, int value){
        return Math.min(Math.max(min, value), max) ;
    }


    private static final int STATE_NORMAL = 0;
    private static final int STATE_READY = 1;
    private static final int STATE_PULL = 2;
    private static final int STATE_UPDATING = 3;
    private static final int INVALID_POINTER_ID = -1;

    private static final int UP_STATE_READY = 4;
    private static final int UP_STATE_PULL = 5;

    private static final int MIN_UPDATE_TIME = 500;

    protected HeaderView mHeaderView;

    private int mActivePointerId;
    private float mLastY;

    private int mState;

    private boolean mPullUpRefreshEnabled = false;

    private OnUpdateTask mOnUpdateTask;
    private OnPullUpUpdateTask mOnPullUpUpdateTask;

    private int mTouchSlop;

    public RefreshableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public RefreshableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    /***
     * Set the Header Content View.
     *
     * @param id
     *            The view resource.
     */
    public void setContentView(int id) {
        final View view = LayoutInflater.from(getContext()).inflate(id,
                mListHeaderView, false);
        mListHeaderView.addView(view);
    }

    /**
     * Set the bootom content view. and open this feature.
     *
     * @param id
     */
    public void setBottomContentView(int id) {
        mPullUpRefreshEnabled = true;
        final View view = LayoutInflater.from(getContext()).inflate(id,
                mListBottomView, false);
        mListBottomView.addView(view);
        addFooterView(mListBottomView, null, false);
    }



    public ListHeaderView getListHeaderView() {
        return mListHeaderView;
    }

    /**
     * Setup the update task.
     *
     * @param task
     */
    public void setOnUpdateTask(OnUpdateTask task) {
        mOnUpdateTask = task;
    }

    public void setOnPullUpUpdateTask(OnPullUpUpdateTask task) {
        mOnPullUpUpdateTask = task;
    }

    /**
     * Update immediately.
     */
    public void startUpdateImmediate() {
        if (mState == STATE_UPDATING) {
            return;
        }
        setSelectionFromTop(0, 0);
        mListHeaderView.moveToUpdateHeight();
        update();
    }

    /**
     * Set the Header View change listener.
     *
     * @param listener
     */
    public void setOnHeaderViewChangedListener(
            OnHeaderViewChangedListener listener) {
        mListHeaderView.mOnHeaderViewChangedListener = listener;
    }

    public void setOnBottomViewChangedListener(
            OnBottomViewChangedListener listener) {
        mListBottomView.mOnHeaderViewChangedListener = listener;
    }

    private void initialize() {
        final Context context = getContext();
        mListHeaderView = new ListHeaderView(context, this);
        addHeaderView(mListHeaderView, null, false);
        mListBottomView = new ListBottomView(getContext(), this);
        mState = STATE_NORMAL;
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    private void pullUpUpdate() {
        if (mListBottomView.isUpdateNeeded()) {
            if (mOnPullUpUpdateTask != null) {
                mOnPullUpUpdateTask.onUpdateStart();
            }

            final int preAdapterCount = this.getAdapter().getCount();

            mListBottomView.startUpdate(new Runnable() {
                public void run() {
                    final long b = System.currentTimeMillis();
                    if (mOnPullUpUpdateTask != null) {
                        mOnPullUpUpdateTask.updateBackground();
                    }
                    final long delta = MIN_UPDATE_TIME
                            - (System.currentTimeMillis() - b);
                    if (delta > 0) {
                        try {
                            Thread.sleep(delta);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    post(new Runnable() {
                        public void run() {
                            int deltay = mListBottomView.close(STATE_NORMAL);
                            postDelayed(new Runnable() {
                                public void run() {

                                    if (getAdapter().getCount() != preAdapterCount) {
                                        throw new IllegalStateException(
                                                "You should change the adapter data in updateUI");
                                    }

                                    if (mOnPullUpUpdateTask != null) {
                                        mOnPullUpUpdateTask.updateUI();
                                    }
                                }
                            }, deltay);

                        }
                    });

                }
            });
            mState = STATE_UPDATING;
        } else {
            mListBottomView.close(STATE_NORMAL);
        }
    }

    private void update() {
        if (mListHeaderView.isUpdateNeeded()) {
            if (mOnUpdateTask != null) {
                mOnUpdateTask.onUpdateStart();
            }
            mListHeaderView.startUpdate(new Runnable() {
                public void run() {
                    final long b = System.currentTimeMillis();
                    if (mOnUpdateTask != null) {
                        mOnUpdateTask.updateBackground();
                    }
                    final long delta = MIN_UPDATE_TIME
                            - (System.currentTimeMillis() - b);
                    if (delta > 0) {
                        try {
                            Thread.sleep(delta);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    post(new Runnable() {
                        public void run() {
                            mListHeaderView.close(STATE_NORMAL);
                            if (mOnUpdateTask != null) {
                                mOnUpdateTask.updateUI();
                            }
                        }
                    });
                }
            });
            mState = STATE_UPDATING;
        } else {
            mListHeaderView.close(STATE_NORMAL);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mState == STATE_UPDATING) {
            return super.dispatchTouchEvent(ev);
        }
        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mLastY = ev.getY();
                isFirstViewTop();
                isLastViewBottom();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER_ID) {
                    break;
                }

                if (mState == STATE_NORMAL) {
                    isFirstViewTop();
                    isLastViewBottom();
                }

                if (mState == STATE_READY) {
                    final int activePointerId = mActivePointerId;
                    final int activePointerIndex = MotionEventCompat
                            .findPointerIndex(ev, activePointerId);
                    final float y = MotionEventCompat.getY(ev, activePointerIndex);
                    final int deltaY = (int) (y - mLastY);
                    mLastY = y;
                    if (deltaY <= 0 || Math.abs(y) < mTouchSlop) {
                        mState = STATE_NORMAL;
                    } else {
                        mState = STATE_PULL;
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(ev);
                    }
                } else if (mState == UP_STATE_READY) {
                    final int activePointerId = mActivePointerId;
                    final int activePointerIndex = MotionEventCompat
                            .findPointerIndex(ev, activePointerId);
                    final float y = MotionEventCompat.getY(ev, activePointerIndex);
                    final int deltaY = (int) (y - mLastY);
                    mLastY = y;
                    if (deltaY >= 0 || Math.abs(y) < mTouchSlop) {
                        mState = STATE_NORMAL;
                    } else {
                        mState = UP_STATE_PULL;
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(ev);
                    }
                }

                if (mState == STATE_PULL) {
                    final int activePointerId = mActivePointerId;
                    final int activePointerIndex = MotionEventCompat
                            .findPointerIndex(ev, activePointerId);
                    final float y = MotionEventCompat.getY(ev, activePointerIndex);
                    final int deltaY = (int) (y - mLastY);
                    mLastY = y;

                    final int headerHeight = mListHeaderView.getHeight();
                    setHeaderHeight(headerHeight + deltaY * 5 / 9);
                    return true;
                } else if (mState == UP_STATE_PULL) {
                    final int activePointerId = mActivePointerId;
                    final int activePointerIndex = MotionEventCompat
                            .findPointerIndex(ev, activePointerId);
                    final float y = MotionEventCompat.getY(ev, activePointerIndex);
                    final int deltaY = (int) (y - mLastY);
                    mLastY = y;
                    final int headerHeight = mListBottomView.getHeight();
                    setBottomHeight(headerHeight - deltaY * 5 / 9);
                    return true;
                }

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;
                if (mState == STATE_PULL) {
                    update();
                } else if (mState == UP_STATE_PULL) {
                    pullUpUpdate();
                }
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN:
                final int index = MotionEventCompat.getActionIndex(ev);
                final float y = MotionEventCompat.getY(ev, index);
                mLastY = y;
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastY = MotionEventCompat.getY(ev, newPointerIndex);
            mActivePointerId = MotionEventCompat.getPointerId(ev,
                    newPointerIndex);
        }
    }

    void setState(int state) {
        mState = state;
    }

    private void setHeaderHeight(int height) {
        mListHeaderView.setHeaderHeight(height);
    }

    private void setBottomHeight(int height) {
        mListBottomView.setBottomHeight(height);
    }

    private boolean isLastViewBottom() {
        final int count = getChildCount();
        if (count == 0 || !mPullUpRefreshEnabled) {
            return false;
        }

        final int lastVisiblePosition = getLastVisiblePosition();
        boolean needs = (lastVisiblePosition == (getAdapter().getCount() - getHeaderViewsCount()))
                && (getChildAt(getChildCount() - 1).getBottom() == (getBottom() - getTop()));
        if (needs) {
            mState = UP_STATE_READY;
        }
        return needs;
    }

    private boolean isFirstViewTop() {
        final int count = getChildCount();
        if (count == 0) {
            return true;
        }
        final int firstVisiblePosition = this.getFirstVisiblePosition();
        final View firstChildView = getChildAt(0);
        boolean needs = firstChildView.getTop() == 0
                && (firstVisiblePosition == 0);
        if (needs) {
            mState = STATE_READY;
        }

        return needs;
    }

    /** When use custom List header view */
    public static interface OnHeaderViewChangedListener {
        /**
         * When user pull the list view, we can change the header status here.
         * for example: the arrow rotate down or up.
         *
         * @param v
         *            : the list view header
         * @param canUpdate
         *            : if the list view can update.
         */
        void onViewChanged(View v, boolean canUpdate);

        /**
         * Change the header status when we really do the update task. for
         * example: display the progressbar.
         *
         * @param v
         *            the list view header
         */
        void onViewUpdating(View v);

        /**
         * Will called when the update task finished. for example: hide the
         * progressbar and show the arrow.
         *
         * @param v
         *            the list view header.
         */
        void onViewUpdateFinish(View v);
    }

    public static interface OnBottomViewChangedListener extends
            OnHeaderViewChangedListener {

    }

    public static interface OnPullUpUpdateTask extends OnUpdateTask {

    }

    /** The callback when the updata task begin, doing. or finish. */
    public static interface OnUpdateTask {

        /**
         * will called before the update task begin. Will Run in the UI thread.
         */
        public void onUpdateStart();

        /**
         * Will called doing the background task. Will Run in the background
         * thread.
         */
        public void updateBackground();

        /**
         * Will called when doing the background task. Will Run in the UI
         * thread.
         */
        public void updateUI();

    }
}
