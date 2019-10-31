package github.hotstu.pagedrecyclerview.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ListViewCompat;

public class MySwipeRefreshLayout extends ViewGroup  {

    private static final String TAG = "MySwipeRefreshLayout";
    private NestedScrollingParentHelper mParentHelper;
    private View mTarget;
    private View mTopView;
    private View mBottomView;
    private float mInitialDownY = 0f;
    private int mTouchSlop;
    private float mInitialMotionY = 0f;
    private NestedScrollingChildHelper mNestedScrollingChildHelper;

    public MySwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public MySwipeRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySwipeRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        final View child = mTarget;
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        //TODO layout top and bottom view
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        mTarget.measure(MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY
                ),
                MeasureSpec.makeMeasureSpec(
                        getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY
                ));
        //TODO mesure top and bottom view
    }
    private static final int INVALID_POINTER = -1;
    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsBeingDragged = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = ev.getActionMasked();
        int pointerIndex;

        if (hasFlag(FLAG_RETURNINGTOSTART) && action == MotionEvent.ACTION_DOWN) {
            removeFlag(FLAG_RETURNINGTOSTART);
        }

        if (!isEnabled() || flagkeep> 0 ) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCircleView.getTop());
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownY = ev.getY(pointerIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e(TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                final float y = ev.getY(pointerIndex);
                startDragging(y);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }



    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // if this is a List < L or another view that doesn't support nested
        // scrolling, ignore this request so that the vertical scroll event
        // isn't stolen
        if ((android.os.Build.VERSION.SDK_INT < 21 && mTarget instanceof AbsListView)
                || (mTarget != null && !ViewCompat.isNestedScrollingEnabled(mTarget))) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    //TODO use interloper
    private static final float DRAG_RATE = .5f;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        int pointerIndex = -1;

        if (hasFlag(FLAG_RETURNINGTOSTART) && action == MotionEvent.ACTION_DOWN) {
            removeFlag(FLAG_RETURNINGTOSTART);
        }

        if (!isEnabled() || flagkeep > 0 ) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;
                break;

            case MotionEvent.ACTION_MOVE: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float y = ev.getY(pointerIndex);
                startDragging(y);

                if (mIsBeingDragged) {
                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    if (overscrollTop > 0) {
                        moveSpinner(overscrollTop);
                    } else {
                        return false;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                pointerIndex = ev.getActionIndex();
                if (pointerIndex < 0) {
                    Log.e(TAG,
                            "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                mActivePointerId = ev.getPointerId(pointerIndex);
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                if (mIsBeingDragged) {
                    final float y = ev.getY(pointerIndex);
                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    mIsBeingDragged = false;
                    finishSpinner(overscrollTop);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                return false;
        }

        return true;
    }

    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (yDiff > mTouchSlop && !mIsBeingDragged && !canChildScrollUp()) {
            mInitialMotionY = mInitialDownY + mTouchSlop;
            mIsBeingDragged = true;
            //开始下拉刷新 --> onTouchEvent()
            //mProgress.setAlpha(STARTING_PROGRESS_ALPHA);
        } else if(yDiff < -mTouchSlop && !mIsBeingDragged && !canChildScrollDown()) {
            //开始上拉加载更多 --> onTouchEvent()
            mInitialMotionY = mInitialDownY - mTouchSlop;
            mIsBeingDragged = true;
        }
    }

    private void moveSpinner(float overscrollTop) {
        Log.d(TAG, "moveSpinner-->" + overscrollTop);
        mTarget.setTranslationY(overscrollTop);
        //mTarget.offsetTopAndBottom(((int) overscrollTop));
    }

    final int refreshTriggerDistance = 400;
    final int refreshStopDistance = 300;
    private void finishSpinner(float overscrollTop) {
        Log.d(TAG, "finishSpinner-->" + overscrollTop);
        if (overscrollTop > refreshTriggerDistance) {
            addFlag(FLAG_REFRESHING);
            //TODO animate to refreshStopDistance
            mTarget.setTranslationY(refreshStopDistance);
        } else {
            addFlag(FLAG_RETURNINGTOSTART);
            mTarget.setTranslationY(0);
        }
    }

    private final static int FLAG_RETURNINGTOSTART = 1;
    private final static int FLAG_REFRESHING = 1 << 1;
    private final static int FLAG_NESTEDSCROLLINPROGRESS = 1 << 2;
    private int flagkeep = 0;

    private void clearFlags() {
        flagkeep = 0;
    }

    private void addFlag(int flag) {
        flagkeep |= flag;
    }

    private void removeFlag(int flag) {
        flagkeep &= ~flag;
    }

    private boolean hasFlag(int flag) {
        return (flagkeep & flag) != 0;
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     *         scroll up. Override this if the child view is a custom view.
     */
    public boolean canChildScrollUp() {
        if (mTarget instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) mTarget, -1);
        }
        return mTarget.canScrollVertically(-1);
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     *         scroll down. Override this if the child view is a custom view.
     */
    public boolean canChildScrollDown() {
        if (mTarget instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) mTarget, 1);
        }
        return mTarget.canScrollVertically(1);
    }

    private void ensureTarget() {
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(mTopView) && !child.equals(mBottomView)) {
                    mTarget = child;
                    break;
                }
            }
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

//    int mTotalUnconsumed = 0;
//    int[] mParentOffsetInWindow = new int[2];
//    int[] mParentScrollConsumed = new int[2];
//    @Override
//    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
//        return isEnabled() && !hasFlag(FLAG_RETURNINGTOSTART) && !hasFlag(FLAG_REFRESHING)
//                && (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;    }
//
//    @Override
//    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
//        // Reset the counter of how much leftover scroll needs to be consumed.
//        mParentHelper.onNestedScrollAccepted(child, target, axes);
//        // Dispatch up to the nested parent
//        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL, type);
//        mTotalUnconsumed = 0;
//        addFlag(FLAG_NESTEDSCROLLINPROGRESS);
//    }
//
//    @Override
//    public void onStopNestedScroll(@NonNull View target, int type) {
//        mParentHelper.onStopNestedScroll(target);
//        removeFlag(FLAG_NESTEDSCROLLINPROGRESS);
//        // Finish the spinner for nested scrolling if we ever consumed any
//        // unconsumed nested scroll
//        if (mTotalUnconsumed > 0) {
//            finishSpinner(mTotalUnconsumed);
//            mTotalUnconsumed = 0;
//        }
//        // Dispatch up our nested parent
//        stopNestedScroll();
//    }
//
//    @Override
//    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
//        // Dispatch up to the nested parent first
//        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow, type);
//
//        // This is a bit of a hack. Nested scrolling works from the bottom up, and as we are
//        // sometimes between two nested scrolling views, we need a way to be able to know when any
//        // nested scrolling parent has stopped handling events. We do that by using the
//        // 'offset in window 'functionality to see if we have been moved from the event.
//        // This is a decent indication of whether we should take over the event stream or not.
//        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
//        if (dy < 0 && !canChildScrollUp()) {
//            mTotalUnconsumed += Math.abs(dy);
//            moveSpinner(mTotalUnconsumed);
//        }
//    }
//
//    @Override
//    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
//        // If we are in the middle of consuming, a scroll, then we want to move the spinner back up
//        // before allowing the list to scroll
//        if (dy > 0 && mTotalUnconsumed > 0) {
//            if (dy > mTotalUnconsumed) {
//                consumed[1] = dy - (int) mTotalUnconsumed;
//                mTotalUnconsumed = 0;
//            } else {
//                mTotalUnconsumed -= dy;
//                consumed[1] = dy;
//            }
//            moveSpinner(mTotalUnconsumed);
//        }
//
//
//        // Now let our nested parent consume the leftovers
//        final int[] parentConsumed = mParentScrollConsumed;
//        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
//            consumed[0] += parentConsumed[0];
//            consumed[1] += parentConsumed[1];
//        }
//    }
//
//
//    //nestedScrollChild2
//    @Override
//    public boolean startNestedScroll(int axes, int type) {
//        return mNestedScrollingChildHelper.startNestedScroll(axes, type);
//    }
//
//    @Override
//    public void stopNestedScroll(int type) {
//        mNestedScrollingChildHelper.stopNestedScroll(type);
//    }
//
//    @Override
//    public boolean hasNestedScrollingParent(int type) {
//        return mNestedScrollingChildHelper.hasNestedScrollingParent(type);
//    }
//
//    @Override
//    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
//        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed,dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
//    }
//
//    @Override
//    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
//        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
//    }
}