package net.somethingdreadful.MAL.cover

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import net.somethingdreadful.MAL.PrefManager
import net.somethingdreadful.MAL.Theme

internal class SpacesItemDecoration(private val columns: Int) : RecyclerView.ItemDecoration() {
    private val spaceTop: Int
    private val spaceRight: Int

    init {
        if (PrefManager.getTraditionalListEnabled()) {
            this.spaceTop = Theme.convert(2)
            this.spaceRight = 0
        } else {
            this.spaceTop = Theme.convert(1)
            this.spaceRight = Theme.convert(1)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        val pos = parent.getChildLayoutPosition(view)
        val side = (pos / columns).toFloat()
        // Add top margin for the first item to avoid multiple spaces
        if (pos > columns) {
            outRect.top = spaceTop
        }
        if ((pos + 1).toFloat() / columns != side) {
            outRect.right = spaceRight
        }
    }
}
