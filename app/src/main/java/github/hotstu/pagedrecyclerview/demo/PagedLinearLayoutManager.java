package github.hotstu.pagedrecyclerview.demo;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author hglf [hglf](https://github.com/hotstu)
 * @desc
 * @since 3/31/20
 */
public class PagedLinearLayoutManager extends LinearLayoutManager {
    public PagedLinearLayoutManager(Context context) {
        super(context);
        init();
    }

    public PagedLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        init();
    }

    public PagedLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {

    }

    public void forwardOrBackPage(RecyclerView view, @BouncingFrameLayout.EventType int type) {
        int target = findFirstVisibleItemPosition();
        if (type == BouncingFrameLayout.FLAG_REACH_BOTTOM) {
            target += 1;
        } else if (type == BouncingFrameLayout.FLAG_REACH_TOP) {
            target -= 1;
        }

        if (target < 0) {
            target = 0;
        }
        smoothScrollToPosition(view, null, target);
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        view.setLayoutFrozen(true);
    }


}
