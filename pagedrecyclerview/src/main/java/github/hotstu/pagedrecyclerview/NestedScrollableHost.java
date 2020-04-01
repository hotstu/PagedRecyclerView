package github.hotstu.pagedrecyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author hglf [hglf](https://github.com/hotstu)
 * @desc 用于viewpager2嵌套recyclerview, see
 * @since 4/1/20
 */
public class NestedScrollableHost extends FrameLayout {
    private float initialX;
    private float initialY;
    private float touchSlop;
    private int orientation;
    public NestedScrollableHost(@NonNull Context context) {
        super(context);
        init(context);
    }

    public NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        orientation = RecyclerView.VERTICAL;

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        handleInterceptTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    private View insureChild() {
        return getChildAt(0);
    }

    private boolean canChildScroll(int orientation, float delta) {
        int direction = -((int) Math.signum(delta));
        if (orientation == RecyclerView.HORIZONTAL) {
            return insureChild().canScrollHorizontally(direction);
        } else if (orientation == RecyclerView.VERTICAL) {
            return insureChild().canScrollVertically(direction);
        } else {
            throw new IllegalArgumentException();
        }
    }


    private void handleInterceptTouchEvent(MotionEvent e) {
        // Early return if child can't scroll in same direction as parent
        if (!canChildScroll(orientation, -1f) && !canChildScroll(orientation, 1f)) {
            return;
        }

        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            initialX = e.getX();
            initialY = e.getY();
            getParent().requestDisallowInterceptTouchEvent(true);
        } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = e.getX() - initialX;
            float dy = e.getY() - initialY;
            boolean isVpHorizontal = orientation == RecyclerView.HORIZONTAL;

            // assuming ViewPager2 touch-slop is 2x touch-slop of child
            float scaledDx = dx;
            float scaledDy = dy;

            if (isVpHorizontal) {
                scaledDx *= .5f;
                scaledDy *= 1f;
            } else {
                scaledDx *= 1f;
                scaledDy *= .5f;
            }

            if (scaledDx > touchSlop || scaledDy > touchSlop) {
                if (isVpHorizontal == (scaledDy > scaledDx)) {
                    // Gesture is perpendicular, allow all parents to intercept
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    // Gesture is parallel, query child if movement in that direction is possible
                    if (canChildScroll(orientation, (isVpHorizontal) ? dx : dy)) {
                        // Child can scroll, disallow all parents to intercept
                        getParent().requestDisallowInterceptTouchEvent(true);
                    } else {
                        // Child cannot scroll, allow all parents to intercept
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
            }
        }
    }
}
