package ws1415.veranstalterapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

/**
 * Created by pascalotto on 01.11.14.
 */
public class DynamicListView extends ListView implements View.OnTouchListener, AdapterView.OnItemLongClickListener, AbsListView.OnScrollListener {
    private static final String LOG_TAG = DynamicListView.class.getSimpleName();
    private static final int INVALID_POINTER_ID = -1;
    private static final int INVALID_ITEM_ID = -1;

    private int mobileCellId;
    private BitmapDrawable mobileCell;
    private int pointerId;

    public DynamicListView(Context context) {
        super(context);
        init(context);
    }

    public DynamicListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DynamicListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        pointerId = INVALID_POINTER_ID;
        mobileCellId = INVALID_ITEM_ID;
        setOnItemLongClickListener(this);
        setOnScrollListener(this);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mobileCell != null) {
            mobileCell.draw(canvas);
        }
    }

    private BitmapDrawable getBitmapDrawable(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas (bitmap);
        view.draw(canvas);

        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        drawable.setBounds(new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));
        return drawable;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (pointerId == INVALID_POINTER_ID) {
                    pointerId = ev.getPointerId(0);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointerId == INVALID_POINTER_ID) {
                    break;
                }
                int pointerIndex = ev.findPointerIndex(pointerId);

                if (mobileCell != null) {
                    int width = mobileCell.getBounds().width();
                    int height = mobileCell.getBounds().height();
                    int left = mobileCell.getBounds().left;
                    int top = (int) (ev.getY(pointerIndex) - (height/2));

                    mobileCell.setBounds(left, top, left+width, top+height);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (pointerId != INVALID_POINTER_ID) {
                    endChanges();
                    pointerId = INVALID_POINTER_ID;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (pointerId != INVALID_POINTER_ID) {
                    cancelChanges();
                    pointerId = INVALID_POINTER_ID;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                        MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                if (pointerId == ev.getPointerId(pointerIndex)) {
                    endChanges();
                    pointerId = INVALID_POINTER_ID;
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private View getViewForID (int itemID) {
        int firstVisiblePosition = getFirstVisiblePosition();
        BaseAdapter adapter = ((BaseAdapter)getAdapter());
        for(int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            int position = firstVisiblePosition + i;
            long id = adapter.getItemId(position);
            if (id == itemID) {
                return v;
            }
        }
        return null;
    }

    private void endChanges() {
        if (mobileCell != null) {
            View view = getViewForID(mobileCellId);

            ObjectAnimator animator = ObjectAnimator.ofObject(mobileCell, "bounds",
                    sBoundEvaluator, new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    invalidate();
                }
            });

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    View view = getViewForID(mobileCellId);
                    view.setVisibility(VISIBLE);

                    mobileCell = null;
                    mobileCellId = INVALID_ITEM_ID;

                    setEnabled(true);
                    invalidate();
                }
            });
            animator.start();
        }
    }

    private void cancelChanges() {
        if (mobileCell != null) {
            mobileCell = null;
            mobileCellId = INVALID_ITEM_ID;
            invalidate();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(LOG_TAG, "onItemLongClick");

        mobileCell = getBitmapDrawable(view);
        mobileCellId = position;
        view.setVisibility(INVISIBLE);
        return true;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    private final static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction),
                    interpolate(startValue.top, endValue.top, fraction),
                    interpolate(startValue.right, endValue.right, fraction),
                    interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int)(start + fraction * (end - start));
        }
    };
}
