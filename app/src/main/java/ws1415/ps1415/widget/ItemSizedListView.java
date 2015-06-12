package ws1415.ps1415.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Erweitert die ListView um die Funktionalität ihre Höhe an die Höhe der Items anzupassen, sodass
 * immer alle Items angezeigt werden.
 * @author Richard Schulze
 */
public class ItemSizedListView extends ListView {
    private DataSetObserver observer = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            recalculateHeight();
        }
    };

    public ItemSizedListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public ItemSizedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ItemSizedListView(Context context) {
        super(context);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (getAdapter() != null) {
            getAdapter().unregisterDataSetObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerDataSetObserver(observer);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        recalculateHeight();
        super.onDraw(canvas);
    }

    private void recalculateHeight() {
        ListAdapter adapter = getAdapter();
        if (adapter != null) {
            int itemCount = adapter.getCount();
            int height = 0;
            View item;
            for (int i = 0; i < itemCount; i++) {
                item = adapter.getView(i, null, this);
                item.measure(0, 0);
                height += item.getMeasuredHeight();
            }
            height += (itemCount - 1) * getDividerHeight();

            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = height;
            setLayoutParams(params);
            requestLayout();
        }
    }
}
