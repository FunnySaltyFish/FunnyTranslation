package com.funny.translation.translate.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;

import androidx.appcompat.widget.AppCompatEditText;

import com.funny.translation.translate.R;
/*来源https://github.com/opprime/EditTextField
	修改byFunnySaltyFish 2020.2.8
	*/

/**
 * Created by opprime on 16-7-21.
 */
public class EditTextField extends AppCompatEditText {
    private Context mContext;
    private Bitmap mClearButton;
    private Paint mPaint;

    private boolean mClearStatus;

    //按钮显示方式
    private ClearButtonMode mClearButtonMode;
    //初始化输入框右内边距
    private int mInitPaddingRight;
    //按钮的左右内边距，默认为3dp
    private int mButtonPadding = dp2px(3);
    //按钮的Rect
    private Rect mButtonRect;
    private int mClearButtonGravity;
    private int viewWidth = 0;
    private int viewHeight = 0;
    private int clearButtonTint;

    /**
     * 按钮显示方式
     * NEVER   不显示清空按钮
     * ALWAYS  始终显示清空按钮
     * WHILEEDITING   输入框内容不为空且有获得焦点
     * UNLESSEDITING  输入框内容不为空且没有获得焦点
     */
    public enum ClearButtonMode {
        NEVER, ALWAYS, WHILEEDITING, UNLESSEDITING
    }

    public static int CLEAR_BUTTON_GRAVITY_BOTTOM = 0;

    public EditTextField(Context context) {
        super(context);
        init(context, null);
    }

    public EditTextField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EditTextField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 初始化
     */
    private void init(Context context, AttributeSet attributeSet) {
        this.mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.EditTextField);

        switch (typedArray.getInteger(R.styleable.EditTextField_clearButtonMode, 0)) {
            case 1:
                mClearButtonMode = ClearButtonMode.ALWAYS;
                break;
            case 2:
                mClearButtonMode = ClearButtonMode.WHILEEDITING;
                break;
            case 3:
                mClearButtonMode = ClearButtonMode.UNLESSEDITING;
                break;
            default:
                mClearButtonMode = ClearButtonMode.NEVER;
                break;
        }
        mClearButtonGravity = typedArray.getInt(R.styleable.EditTextField_clearButtonGravity, 0);
        clearButtonTint = typedArray.getColor(R.styleable.EditTextField_clearButtonTint, Color.WHITE);
        int clearButton = typedArray.getResourceId(R.styleable.EditTextField_clearButtonDrawable, android.R.drawable.ic_delete);
        typedArray.recycle();

        //按钮的图片
        int targetWidth, targetHeight;
        targetWidth = targetHeight = getLineHeight();//ApplicationUtil.sp2px(mContext,(int)getPaint().getTextSize());
        mClearButton = BitmapUtil.getBitmapFromResources(getResources(), clearButton, targetWidth, targetHeight);//(getDrawableCompat(clearButton)).getBitmap();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mInitPaddingRight = getPaddingRight();
    }


    /**
     * 按钮状态管理
     *
     * @param canvas onDraw的Canvas
     */
    private void buttonManager(Canvas canvas) {
        switch (mClearButtonMode) {
            case ALWAYS:
                mButtonRect = getRect(true);
                drawBitmap(canvas, mButtonRect);
                break;
            case WHILEEDITING:
                mButtonRect = getRect(hasFocus() && getText().length() > 0);
                drawBitmap(canvas, mButtonRect);
                break;
            case UNLESSEDITING:
                break;
            default:
                mButtonRect = getRect(false);
                drawBitmap(canvas, mButtonRect);
                break;
        }
    }


    /**
     * 设置输入框的内边距
     *
     * @param isShow 是否显示按钮
     */
    public void setPadding(boolean isShow) {
        int paddingRight = mInitPaddingRight + (isShow ? mClearButton.getWidth() + mButtonPadding : 0);
        setPadding(getPaddingLeft(), getPaddingTop(), paddingRight, getPaddingBottom());
        //System.out.println("setPadding");
    }

    public boolean getIsShow() {
        boolean isShow = false;
        switch (mClearButtonMode) {
            case ALWAYS:
                isShow = true;
                break;
            case WHILEEDITING:
                isShow = (hasFocus() && getText().length() > 0);
                break;
            case UNLESSEDITING:
                break;
            default:
                break;
        }
        return isShow;
    }

    /**
     * 取得显示按钮与不显示按钮时的Rect
     *
     * @param isShow 是否显示按钮
     */
    private Rect getRect(boolean isShow) {
        int left, top, right, bottom;
        if (!isShow) {//不展示
            right = top = left = bottom = 0;
        } else {
            right = viewWidth + getScrollX() - mButtonPadding;
            left = right - mClearButton.getWidth();
            int line = getLineCount();
            int textHeight = (int) (line * getLineHeight() * getLineSpacingMultiplier());
            //System.out.printf("line:%d height:%d\n",line,textHeight);
            if (line == 1) {
                top = (getMeasuredHeight() - mClearButton.getHeight()) / 2;
            } else {
                top = (textHeight - mClearButton.getHeight());
            }
            bottom = top + mClearButton.getHeight();
        }
        //更新输入框内边距
        //setPadding(isShow);
        return new Rect(left, top, right, bottom);
    }

    /**
     * 绘制按钮图片
     *
     * @param canvas onDraw的Canvas
     * @param rect   图片位置
     */
    private void drawBitmap(Canvas canvas, Rect rect) {
        if (rect != null) {
            //创建一个新的bitmap在上面绘制出指定的颜色的配色，mode使用默认值
            ColorFilter filter = new PorterDuffColorFilter(clearButtonTint, PorterDuff.Mode.SRC_IN);
            //创建画笔及设置过滤器
            Paint paint = new Paint();
            paint.setColorFilter(filter);
            canvas.drawBitmap(mClearButton, null, rect, paint);
        }
    }

    private boolean isClickBitmap(int x, int y) {
        int lineCount = getLineCount();
        int maxShowCount = getMaxLines();
        if (lineCount <= maxShowCount) {
            return mButtonRect.contains(x, y);
        } else {
            return mButtonRect.contains(x, y + getScrollY());
            //System.out.printf("x:%d,y:%d",x,y);
            //int bottom=(int)(lineCount*getLineHeight()*getLineSpacingMultiplier());
            //System.out.printf("left:%d ,right:%d ,bottom:%d ,top:%d",mButtonRect.left,mButtonRect.right,bottom,bottom-mClearButton.getHeight());
            //return (x>=mButtonRect.left&&x<=mButtonRect.right&&y<=bottom&&y>=bottom-mClearButton.getHeight());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        buttonManager(canvas);
        canvas.restore();
        //System.out.println("onDraw()");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO: Implement this method
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        setPadding(getIsShow());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                //判断是否点击到按钮所在的区域
                if (isClickBitmap((int) event.getX(), (int) event.getY())) {
                    setError(null);
                    this.setText("");
                }
                break;
        }

        return super.onTouchEvent(event);
    }


    /**
     * 获取Drawable
     *
     * @param resourseId 资源ID
     */
    private BitmapDrawable getDrawableCompat(int resourseId) {
        //Bitmap b=BitmapUtil.getBigBitmapFromResources(getResources(),resourseId,targetWidth,targetHeight);
        //return new BitmapDrawable(b);
        return null;
    }

    /**
     * 设置按钮左右内边距
     *
     * @param buttonPadding 单位为dp
     */
    public void setButtonPadding(int buttonPadding) {
        this.mButtonPadding = dp2px(buttonPadding);
    }

    /**
     * 设置按钮显示方式
     *
     * @param clearButtonMode 显示方式
     */
    public void setClearButtonMode(ClearButtonMode clearButtonMode) {
        this.mClearButtonMode = clearButtonMode;
    }

    public boolean isShowing() {
        return mClearStatus;
    }

    public int dp2px(float dipValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
