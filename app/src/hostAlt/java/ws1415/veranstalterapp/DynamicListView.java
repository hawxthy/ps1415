package ws1415.veranstalterapp;

import android.animation.TypeEvaluator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;

/**
 * Created by Pascal Otto on 01.11.14.
 */
public class DynamicListView extends ListView implements /*AdapterView.OnItemLongClickListener,*/ AbsListView.OnScrollListener {
    private static final String LOG_TAG = DynamicListView.class.getSimpleName();
    private static final int INVALID_POINTER_ID = -1;
    private static final int INVALID_ITEM_ID = -1;

    private int minSwipeOffset;
    private int swipeMargin;

    private int mobileCellId;
    private BitmapDrawable mobileCell;

    private Point initialTouchPosition;
    private Point previousTouchPosition;
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

        initialTouchPosition = null;
        previousTouchPosition = null;
        //setOnItemLongClickListener(this);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        minSwipeOffset = (int)(0.25f*(metrics.xdpi/2.5f));
        swipeMargin = (int)(0.3f*metrics.widthPixels);

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
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (pointerId == INVALID_POINTER_ID) {
                    pointerId = ev.getPointerId(0);
                    int pointerIndex = ev.findPointerIndex(pointerId);
                    initialTouchPosition = new Point((int) ev.getX(pointerIndex), (int) ev.getY(pointerIndex));
                    previousTouchPosition = new Point((int) ev.getX(pointerIndex), (int) ev.getY(pointerIndex));
                    mobileCellId = getItemIDForPosition((int)ev.getX(pointerIndex), (int)ev.getY(pointerIndex));
                    if (mobileCellId == INVALID_ITEM_ID) {
                        pointerId = INVALID_POINTER_ID;
                        mobileCellId = INVALID_ITEM_ID;
                        requestDisallowInterceptTouchEvent(false);
                        break;
                    }
                    else {
                        requestDisallowInterceptTouchEvent(true);
                        break;
                    }
                }
            }
            case MotionEvent.ACTION_MOVE: {
                if (pointerId == INVALID_POINTER_ID) {
                    break;
                }

                int pointerIndex = ev.findPointerIndex(pointerId);
                if (mobileCell == null) {
                    if (Math.abs(ev.getX(pointerIndex)-initialTouchPosition.x) > minSwipeOffset) {
                        View v = getViewForID(mobileCellId);
                        v.setVisibility(INVISIBLE);
                        mobileCell = getBitmapDrawable(v);

                        int deltaX = (int) (previousTouchPosition.x - ev.getX(pointerIndex));

                        int width = mobileCell.getBounds().width();
                        int height = mobileCell.getBounds().height();
                        int left = mobileCell.getBounds().left-deltaX;
                        int top = mobileCell.getBounds().top;

                        mobileCell.setBounds(left, top, left+width, top+height);
                        invalidate();

                        // Sendet Cancel-Event an super damit die Auswahl verschwindet.
                        MotionEvent cancelEvent = MotionEvent.obtain(ev);
                        cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                        super.onTouchEvent(cancelEvent);

                        previousTouchPosition = new Point((int) ev.getX(pointerIndex), (int) ev.getY(pointerIndex));
                        return true;
                    }
                    else {
                        previousTouchPosition = new Point((int) ev.getX(pointerIndex), (int) ev.getY(pointerIndex));
                        break;
                    }
                }
                else {
                    int deltaX = (int) (previousTouchPosition.x - ev.getX(pointerIndex));

                    int width = mobileCell.getBounds().width();
                    int height = mobileCell.getBounds().height();
                    int left = mobileCell.getBounds().left-deltaX;
                    int top = mobileCell.getBounds().top;

                    mobileCell.setBounds(left, top, left+width, top+height);

                    int centerX = mobileCell.getBounds().centerX();
                    if (centerX < swipeMargin) {
                        int alpha = (int) (((float) centerX/(float) swipeMargin)*255.0f);
                        if (alpha < 0) alpha = 0;
                        Log.v(LOG_TAG, "" + alpha);
                        mobileCell.setAlpha(alpha);
                    }
                    else if (centerX > getWidth()-swipeMargin) {
                        int alpha = (int) (((float) (centerX-(getWidth()-swipeMargin))/(float) swipeMargin)*255.0f);
                        if (alpha < 0) alpha = 0;
                        Log.v(LOG_TAG, centerX-(getWidth()-swipeMargin) + " " + alpha);
                        mobileCell.setAlpha(alpha);
                    }

                    invalidate();
                    previousTouchPosition = new Point((int) ev.getX(pointerIndex), (int) ev.getY(pointerIndex));
                    return true;
                }
            }

                /*
                int pointerIndex = ev.findPointerIndex(pointerId);

                if (mobileCell != null) {
                    int width = mobileCell.getBounds().width();
                    int height = mobileCell.getBounds().height();
                    int left = mobileCell.getBounds().left;
                    int top = (int) (ev.getY(pointerIndex) - (height/2));

                    mobileCell.setBounds(left, top, left+width, top+height);
                    invalidate();
                }
                */
            case MotionEvent.ACTION_UP: {
                if (pointerId != INVALID_POINTER_ID) {
                    pointerId = INVALID_POINTER_ID;
                    initialTouchPosition = null;
                    previousTouchPosition = null;
                    if (mobileCell != null) {
                        endSwipe();
                        return true;
                    }
                    else {
                        mobileCellId = INVALID_ITEM_ID;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                if (pointerId != INVALID_POINTER_ID) {
                    pointerId = INVALID_POINTER_ID;
                    initialTouchPosition = null;
                    previousTouchPosition = null;
                    if (mobileCell != null) {
                        cancelSwipe();
                        return true;
                    }
                    else {
                        mobileCellId = INVALID_ITEM_ID;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                        MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                if (pointerId == ev.getPointerId(pointerIndex)) {
                    pointerId = INVALID_POINTER_ID;
                    initialTouchPosition = null;
                    previousTouchPosition = null;
                    if (mobileCell != null) {
                        endSwipe();
                        return true;
                    }
                    else {
                        mobileCellId = INVALID_ITEM_ID;
                    }
                }
                break;
            }
        }
        return super.onTouchEvent(ev);
    }



    private View getViewForPosition(int x, int y) {
        int firstVisiblePosition = getFirstVisiblePosition();
        BaseAdapter adapter = ((BaseAdapter)getAdapter());

        Rect hitRect = new Rect();
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);

            v.getHitRect(hitRect);
            if (hitRect.contains(x, y)) {
                Log.v(LOG_TAG, "" + i);
                return v;
            }
        }
        return null;
    }

    private int getItemIDForPosition(int x, int y) {
        int firstVisiblePosition = getFirstVisiblePosition();
        BaseAdapter adapter = ((BaseAdapter)getAdapter());

        Rect hitRect = new Rect();
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);

            v.getHitRect(hitRect);
            if (hitRect.contains(x, y)) {
                Log.v(LOG_TAG, "" + i);
                return firstVisiblePosition+i;
            }
        }
        return INVALID_ITEM_ID;
    }

    private View getViewForID(int itemID) {
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

    private void endSwipe() {
        requestDisallowInterceptTouchEvent(false);

        if (mobileCell != null) {
            View view = getViewForID(mobileCellId);
            view.setVisibility(VISIBLE);

            mobileCell = null;
            mobileCellId = INVALID_ITEM_ID;
            invalidate();
        }
        /*
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
        */
    }

    private void cancelSwipe() {
        requestDisallowInterceptTouchEvent(false);

        if (mobileCell != null) {
            View view = getViewForID(mobileCellId);
            view.setVisibility(VISIBLE);

            mobileCell = null;
            mobileCellId = INVALID_ITEM_ID;
            invalidate();
        }
    }

    /*
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(LOG_TAG, "onItemLongClick");

        mobileCell = getBitmapDrawable(view);
        mobileCellId = position;
        view.setVisibility(INVISIBLE);
        return true;
    }
    */

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
