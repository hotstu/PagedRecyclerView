package github.hotstu.pagedrecyclerview.demo;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 一个类似CoordinateLayout的Layout，支持iOS风格的弹性over scroll
 * 使用的时候只需要在xml中指定每个子view的角色（app:layout_role=""）
 * 就可以指定header、content、footer
 * @author hglf [hglf](https://github.com/hotstu)
 * @desc
 * @since 10/31/19
 */
public class BouncingFrameLayout extends FrameLayout {
    public interface BouncingEventListener {
        void onChange(@EventType int type);
    }

    @IntDef({FLAG_REACH_TOP, FLAG_REACH_BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EventType {

    }

    private static final String TAG = BouncingFrameLayout.class.getSimpleName();
    private int mTouchSlop;
    private Scroller mScroller;
    private int mHeaderTriggerDistance = -1;
    private int mFooterTriggerDistance = -1;
    public  final static int FLAG_REACH_TOP = 1;
    public final static int FLAG_REACH_BOTTOM = 1 << 1;

    private int flags = 0;
    private BouncingEventListener mBouncingEventListener;

    public BouncingFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public BouncingFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BouncingFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }


    private void init(Context ctx, @Nullable AttributeSet attrs, int defStyleAttr) {
        mTouchSlop = ViewConfiguration.get(ctx).getScaledTouchSlop();
        mScroller = new Scroller(ctx);
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.BouncingFrameLayout);
        mHeaderTriggerDistance = a.getDimensionPixelSize(R.styleable.BouncingFrameLayout_headerActionHeight, -1);
        mFooterTriggerDistance = a.getDimensionPixelSize(R.styleable.BouncingFrameLayout_footerActionHeight, -1);
        a.recycle();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderTriggerDistance < 0 || mFooterTriggerDistance < 0) {
            int tempHeaderMax = -1;
            int tempFooterMax = -1;
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (layoutParams.getLayoutRole() == LayoutParams.HEADER) {
                    tempHeaderMax = Math.max(tempHeaderMax, childAt.getMeasuredHeight());
                }
                if (layoutParams.getLayoutRole() == LayoutParams.FOOTER) {
                    tempFooterMax = Math.max(tempFooterMax, childAt.getMeasuredHeight());
                }
            }
            if (mHeaderTriggerDistance < 0 && tempHeaderMax > 0) {
                mHeaderTriggerDistance = tempHeaderMax;
            }

            if (mFooterTriggerDistance < 0 && tempFooterMax > 0) {
                mFooterTriggerDistance = tempFooterMax;
            }
        }
    }


    private void addFlag(int flag) {
        flags |= flag;
    }

    private void removeFlag(int flag) {
        flags &= ~flag;
    }


    public void setBouncingEventListener(BouncingEventListener bouncingEventListener) {
        this.mBouncingEventListener = bouncingEventListener;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChildren(left, top, right, bottom, false);
    }

    void layoutChildren(int left, int top, int right, int bottom, boolean forceLeftGravity) {
        final int count = getChildCount();

        final int parentLeft = getPaddingLeft();
        final int parentRight = right - left - getPaddingRight();

        final int parentTop = getPaddingTop();
        final int parentBottom = bottom - top - getPaddingBottom();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final BouncingFrameLayout.LayoutParams lp = (BouncingFrameLayout.LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft;
                int childTop;

                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = Gravity.TOP | Gravity.START;
                }

                final int layoutDirection = getLayoutDirection();
                final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                                lp.leftMargin - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        if (!forceLeftGravity) {
                            childLeft = parentRight - width - lp.rightMargin;
                            break;
                        }
                    case Gravity.LEFT:
                    default:
                        childLeft = parentLeft + lp.leftMargin;
                }

                switch (verticalGravity) {
                    case Gravity.TOP:
                        childTop = parentTop + lp.topMargin;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                                lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = parentBottom - height - lp.bottomMargin;
                        break;
                    default:
                        childTop = parentTop + lp.topMargin;
                }
                if (lp.mLayoutRole == LayoutParams.HEADER) {
                    childTop = parentTop - height - lp.bottomMargin;
                } else if (lp.mLayoutRole == LayoutParams.FOOTER) {
                    childTop = parentBottom + lp.topMargin;
                }

                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }


    private float mInitialDownY;
    private boolean mIsBeingDragged;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
            return true;
        }
        if (action == MotionEvent.ACTION_DOWN) {
            mIsBeingDragged = false;
            mInitialDownY = ev.getY();
        } else if (action == MotionEvent.ACTION_MOVE) {
            float diff = ev.getY() - mInitialDownY;
            if (Math.abs(diff) >= mTouchSlop) {
                if (diff > 0 && !canContentScrollUp()) {
                    mIsBeingDragged = true;
                } else if (diff < 0 && !canContentScrollDown()) {
                    mIsBeingDragged = true;
                }
            }
        } else if (action == MotionEvent.ACTION_UP) {
            mIsBeingDragged = false;
        } else if (action == MotionEvent.ACTION_CANCEL) {
            mIsBeingDragged = false;
        }
        return mIsBeingDragged;
    }


    int mLastScrollerY = 0;
    final float PREVENT_FACTOR = .5f;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();

        if (!isEnabled()) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mInitialDownY = ev.getY();
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_MOVE: {
                final float y = ev.getY();
                float diff = (mInitialDownY - ev.getY());
                mInitialDownY = ev.getY();
                if (Math.abs(diff) >= mTouchSlop) {
                    mIsBeingDragged = true;
                }
                if (mIsBeingDragged) {
                    scrollBy(0, (int) (PREVENT_FACTOR * diff));
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (mIsBeingDragged) {
                    mLastScrollerY = getScrollY();
                    mScroller.startScroll(0, mLastScrollerY, 0, -mLastScrollerY);
                    postInvalidateOnAnimation();
                }
                mIsBeingDragged = false;
                flags = 0;
                if (mLastScrollerY < -mHeaderTriggerDistance) {
                    addFlag(FLAG_REACH_TOP);
                }
                if (mLastScrollerY > mFooterTriggerDistance) {
                    addFlag(FLAG_REACH_BOTTOM);
                }
            }

        }

        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int currY = mScroller.getCurrY();
            //Log.d(TAG, "currY: " + currY);
            scrollTo(0, currY);
            postInvalidateOnAnimation();
        } else {
            //Log.d(TAG, "" + flags + "," + mIsBeingDragged + "," + mScroller.isFinished());

        }
        if (flags > 0 && !mIsBeingDragged ) {
            if (flags != FLAG_REACH_BOTTOM && flags != FLAG_REACH_TOP) {
                throw new IllegalStateException("flags ==" + flags);
            }
            if (mBouncingEventListener != null) {
                mBouncingEventListener.onChange(flags);
            }
            flags = 0;
        }
    }

    public boolean canContentScrollUp() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            int layoutRole = layoutParams.getLayoutRole();
            if (layoutRole == LayoutParams.CONTENT && childAt.canScrollVertically(-1)) {
                return true;
            }
        }
        return false;
    }

    public boolean canContentScrollDown() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            int layoutRole = layoutParams.getLayoutRole();
            if (layoutRole == LayoutParams.CONTENT && childAt.canScrollVertically(1)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new LayoutParams(lp.width, lp.height);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {
        public static final int HEADER = 0;
        public static final int FOOTER = 1;
        public static final int CONTENT = 2;

        @IntDef({HEADER, FOOTER, CONTENT})
        @Retention(RetentionPolicy.SOURCE)
        @interface LayoutRole {

        }

        private @LayoutRole
        int mLayoutRole;

        public LayoutParams(@NonNull Context c, @Nullable AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.BouncingFrameLayout_Layout);
            mLayoutRole = a.getInt(R.styleable.BouncingFrameLayout_Layout_layout_role, CONTENT);
            a.recycle();

        }

        public LayoutParams(int width, int height) {
            super(width, height);
            mLayoutRole = CONTENT;
        }

        public void setLayoutRole(@LayoutRole int layoutRole) {
            this.mLayoutRole = layoutRole;
        }

        public @LayoutRole
        int getLayoutRole() {
            return mLayoutRole;
        }
    }
}
