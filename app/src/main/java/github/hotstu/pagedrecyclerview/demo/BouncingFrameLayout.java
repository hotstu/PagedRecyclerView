package github.hotstu.pagedrecyclerview.demo;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author hglf [hglf](https://github.com/hotstu)
 * @desc
 * @since 10/31/19
 */
public class BouncingFrameLayout extends FrameLayout {
    public BouncingFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public BouncingFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public BouncingFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }


    private void init() {

    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        //TODO
        return new LayoutParams(lp.width, lp.height);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {
        public static final int HEADER = 0;
        public static final int Footer = 1;
        public static final int CONTNENT = 2;

        @IntDef({HEADER, Footer, CONTNENT})
        @Retention(RetentionPolicy.SOURCE)
        @interface LayoutRole {

        }

        private @LayoutRole
        int mLayoutRole;

        public LayoutParams(@NonNull Context c, @Nullable AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.BouncingFrameLayout_Layout);
            mLayoutRole = a.getInt(R.styleable.BouncingFrameLayout_Layout_layout_role, CONTNENT);
            a.recycle();

        }

        public LayoutParams(int width, int height) {
            super(width, height);
            mLayoutRole = CONTNENT;
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
