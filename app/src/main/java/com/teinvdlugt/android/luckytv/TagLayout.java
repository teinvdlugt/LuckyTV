package com.teinvdlugt.android.luckytv;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class TagLayout extends ViewGroup {

    public TagLayout(Context context) {
        this(context, null, 0);
    }

    public TagLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();

        final int left = this.getPaddingLeft();
        final int top = this.getPaddingTop();
        final int right = this.getMeasuredWidth() - getPaddingRight();
        final int bottom = this.getMeasuredHeight() - getPaddingBottom();
        final int width = right - left;
        final int height = bottom - top;

        int curLeft = left, curTop = top;
        int childWidth, childHeight, maxHeight = 0;

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE)
                continue;

            child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();

            if (curLeft + childWidth > right) {
                // Go to next line
                curLeft = left;
                curTop += maxHeight;
                maxHeight = 0;
            }

            child.layout(curLeft, curTop, curLeft + childWidth, curTop + childHeight);
            if (maxHeight < childHeight) maxHeight = childHeight;
            curLeft += childWidth;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int maxWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();

        // The current x position in the current line
        int curLineWidth = 0;
        // The y position of the current line
        int curLineHeight = 0;
        // The ultimate measured width and height of the TagLayout
        int totalWidth = 0, totalHeight = 0;
        // The width and height of the current child (reused variables)
        int childWidth, childHeight;
        // The maximum height of the tags per line:
        int maxTagHeight = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE)
                continue;

            child.measure(MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();

            if (curLineWidth + childWidth >= maxWidth) {
                // Go to next line
                curLineWidth = 0;
                curLineHeight += maxTagHeight;
                maxTagHeight = 0;
            }

            maxTagHeight = Math.max(maxTagHeight, childHeight);
            curLineWidth += childWidth;

            totalWidth = Math.max(totalWidth, curLineWidth);
            totalHeight = Math.max(totalHeight, curLineHeight + maxTagHeight);
        }

        setMeasuredDimension(totalWidth + getPaddingLeft() + getPaddingRight(), totalHeight + getPaddingTop() + getPaddingBottom());
    }
}
