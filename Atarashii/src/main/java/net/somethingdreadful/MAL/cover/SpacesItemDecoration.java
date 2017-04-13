package net.somethingdreadful.MAL.cover;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.Theme;

class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private final int spaceTop;
    private final int spaceRight;
    private final int columns;

    SpacesItemDecoration(int columns) {
        if (PrefManager.getTraditionalListEnabled()) {
            this.spaceTop = Theme.convert(2);
            this.spaceRight = 0;
        } else {
            this.spaceTop = Theme.convert(1);
            this.spaceRight = Theme.convert(1);
        }
        this.columns = columns;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int pos = parent.getChildLayoutPosition(view);
        float side = pos / columns;
        // Add top margin for the first item to avoid multiple spaces
        if (pos > columns) {
            outRect.top = spaceTop;
        }
        if ((float) (pos + 1) / columns != side) {
            outRect.right = spaceRight;
        }
    }
}
