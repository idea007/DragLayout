package com.example.idea.draglayout.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.idea.draglayout.R;
import com.example.idea.draglayout.utils.DpUtils;
import com.example.idea.draglayout.utils.LogUtils;


/**
 * Created by Deemo on 15/10/16.
 */
public class DragLayout extends RelativeLayout {

    //默认高度
    private final int DEFAULT_HEIGHT_DP = 240;

    private RelativeLayout mRlContainerTop;
    private RelativeLayout mRlContainerBottom;
    private ImageView mIvThumb;
    private int resTop, resBottom;
    private int type;
    private Context mContext;

    //触发移动事件的最短距离，如果小于这个距离就不触发移动控件，如viewpager就是用这个距离来判断用户是否翻页
    private int mTouchSlop;
    private float mDownX, mDownY, mMoveY;

    private boolean isTouchThumb;
    private boolean isTendToMove;

    private float mThumbMinY;

    private int mBottomContentViewHeight;
    private int mLastBottomTop;


    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //自定义控件的属性
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DragLayout, defStyleAttr, 0);
        resTop = array.getResourceId(R.styleable.DragLayout_layout_top, 0);
        resBottom = array.getResourceId(R.styleable.DragLayout_layout_bottom, 0);
        setType(array.getInteger(R.styleable.DragLayout_questionType, 0));
        array.recycle();

        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.view_draglayout, this, true);
        mRlContainerTop = (RelativeLayout) findViewById(R.id.rl_container_top);
        mRlContainerBottom = (RelativeLayout) findViewById(R.id.rl_container_bottom);
        mIvThumb = (ImageView) findViewById(R.id.iv_thumb);
        setTopAndBottomLayout(resTop, resBottom);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        initHeight();
    }

    private void initHeight() {
        float defaultHeight = DpUtils.dp2px(mContext.getResources(), DEFAULT_HEIGHT_DP);
        //通过LayoutParams来设置上面部分的高度
        ViewGroup.LayoutParams layoutParams = mRlContainerTop.getLayoutParams();
        layoutParams.height = (int) defaultHeight;
        mRlContainerTop.setLayoutParams(layoutParams);
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setTopLayout(View view) {
        if (view != null) {
            mRlContainerTop.addView(view);
        }
    }


    public void setBottomLayout(final View view) {
        if (view != null) {
            mRlContainerBottom.addView(view);
            mIvThumb.setVisibility(View.VISIBLE);

            if (view.getMeasuredHeight() == 0) {
                view.measure(0, 0);
            }
            mBottomContentViewHeight = view.getMeasuredHeight();

        } else {
            mRlContainerTop.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mRlContainerBottom.setVisibility(View.GONE);
            mIvThumb.setVisibility(View.GONE);
        }
    }

    public void setTopAndBottomLayout(int top, int bottom) {
        if (top != 0) {
            View topView = LayoutInflater.from(getContext()).inflate(top, null);
            setTopLayout(topView);
        }

        if (bottom != 0) {
            View bottomView = LayoutInflater.from(getContext()).inflate(bottom, null);
            setTopLayout(bottomView);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogUtils.d(this, String.format("DragLayout w=%d, h=%d, ow=%d, oh=%d", w, h, oldw, oldh));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                mMoveY = mDownY;
                isTouchThumb = isTouchThumb();
                if (isTouchThumb())
                    return true;
                return super.dispatchTouchEvent(ev);
            case MotionEvent.ACTION_MOVE:
                float moveY = ev.getY();
                if (isTouchThumb) {
                    float movedY = moveY - mMoveY;
                    if (!isTendToMove) {
                        isTendToMove = isTendToMove(movedY);
                    }
                    if (isTendToMove) {
                        tryToMoveThumb(movedY);
                        mMoveY = moveY;
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                reset();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void reset() {
        isTendToMove = false;
    }

    /**
     * 拖动图片的有效按压部分
     */
    private boolean isTouchThumb() {
        RectF rect = new RectF(mIvThumb.getLeft(), mIvThumb.getTop(), mIvThumb.getRight(), mIvThumb.getBottom());
        return rect.contains(mDownX, mDownY);
    }

    private boolean isTendToMove(float y) {
        return Math.abs(mDownY - y) >= mTouchSlop;
    }

    /**
     * 拖动范围限制,拖动的图片不能划出界面
     *
     * @param movedY
     */
    private void tryToMoveThumb(float movedY) {
        boolean canMove = (mIvThumb.getBottom() + movedY) < getHeight() && (mIvThumb.getTop() + movedY) > mThumbMinY;
        if (canMove) {
            mIvThumb.offsetTopAndBottom((int) movedY);
            adjustTopAndBottom((int) movedY);
            mLastBottomTop = mRlContainerBottom.getTop();
        }
    }

    /**
     * 改变上面部分高度
     *
     * @param movedY
     */
    public void adjustTopAndBottom(int movedY) {
        ViewGroup.LayoutParams layoutParams = mRlContainerTop.getLayoutParams();
        layoutParams.height += movedY;
        if (layoutParams.height < 0) {
            layoutParams.height = 0;
        }
        mRlContainerTop.setLayoutParams(layoutParams);

        LogUtils.d(this, "adjustTopAndBottom" + movedY + ", mRlytContainerTop=" + mRlContainerTop);
    }


    protected final int getBottomContentViewHeight() {
        return mBottomContentViewHeight;
    }

    protected int getBottomTop() {
        return mLastBottomTop;
    }

}
