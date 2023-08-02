package com.app.speak.ui.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class GridItemDecoration extends RecyclerView.ItemDecoration {
    private final int space;
    private boolean isBottomSpacing = false;

    public GridItemDecoration(int space) {
        this.space = space;
    }

    public GridItemDecoration(int space, boolean isBottomSpacing) {
        this.space = space;
        this.isBottomSpacing = isBottomSpacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;
        if (isBottomSpacing)
            outRect.bottom = space;

        // Add top margin only for the first item to avoid double space between items
//        if (parent.getChildLayoutPosition(view) == 0) {
//            outRect.top = space;
//        } else {
//            outRect.top = 0;
//        }
    }
}
