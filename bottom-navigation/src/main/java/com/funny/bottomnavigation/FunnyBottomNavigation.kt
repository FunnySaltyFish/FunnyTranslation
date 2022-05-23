package com.funny.bottomnavigation

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.funny.bottomnavigation.bean.IconButton
import java.util.*

class FunnyBottomNavigation @JvmOverloads constructor(
    private var mContext: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(
    mContext, attrs, defStyleAttr
) {
    private var mViewWidth = 0
    private var mViewHeight = 0
    var iconOffsetY = 0
    var imageWidth = 0
    var imageHeight = 0
    var highlightColor: Int
    var normalColor: Int
    var navigationBgColor: Int
    var animationDuration: Int = 500
        set(value){
            field = value
            mValueAnimator?.duration = value.toLong()
        }
    var startPage: Int
    private var iconButtonList: ArrayList<IconButton>? = null
    var onItemClickListener: OnItemClickListener? = null
    var onAnimationUpdateListener: OnAnimationUpdateListener? = null
    var clickMargin : Int = 8

    //双缓冲画布
    private lateinit var mCacheCanvas: Canvas
    private lateinit var mCacheBitmap: Bitmap
    private lateinit var mPaint: Paint
    private var mLastPage: Int
    private var mLastClickedIconButton: IconButton? = null
    private var mNeedToClickIconButton: IconButton? = null

    //以下和转移的动画效果相关
    private var mValueAnimator: ValueAnimator? = null
    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f //转移动画的开始和结束位置
    private var transformPaths: ArrayList<Path>? = null
    private var transformPathMeasure: PathMeasure? = null

    companion object {
        private const val TAG = "FunnyBottomNavigation"
        val random = Random()
        var DEBUG = false

        fun log(msg: String) {
            if (DEBUG) Log.d(TAG, msg)
        }
    }


    /**
     * 初始化底部按钮
     * @param iconIds 图片id的集合（List形式)
     */
    fun initIconButtons(iconIds: List<Int>) {
        initIconButtons(iconIds.toIntArray())
    }

    /**
     * 初始化底部按钮
     * @param iconIds 图片id的集合（数组形式)
     */
    fun initIconButtons(iconIds: IntArray) {
        if (mViewWidth == 0 || mViewHeight == 0) {
            postDelayed({ initIconButtons(iconIds) }, 100)
            return
        }
        val itemWidth = mViewWidth / iconIds.size
        iconButtonList = ArrayList()
        for (i in iconIds.indices) {
            val currentX = i * itemWidth
            val iconButton = IconButton(
                mContext,
                iconIds[i],
                currentX.toFloat(),
                iconOffsetY.toFloat(),
                itemWidth,
                mViewHeight - iconOffsetY,
                imageWidth,
                imageHeight,
                highlightColor,
                normalColor,
                clickMargin
            )
            iconButton.id = i

            iconButtonList?.add(iconButton)
        }
        mLastClickedIconButton = iconButtonList?.get(mLastPage)?.also { it.clickProgress = (100) }
        invalidate()
    }

    /**
     * 判断是否完成了初始化（即宽高测量完毕、且设置了 iconButtons )
     * @return Boolean
     */
    fun hasInitialized() = !iconButtonList.isNullOrEmpty() && mViewWidth > 0 && mViewHeight > 0

    /**
     * 初始化绘图相关
     */
    private fun initGraphics() {
        if (mViewWidth == 0 || mViewHeight == 0) {
            postDelayed({ initGraphics() }, 100)
            return
        }
        mCacheCanvas = Canvas()
        mCacheBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.ARGB_8888)
        mCacheCanvas.setBitmap(mCacheBitmap)
        mPaint = Paint()
        transformPaths = ArrayList()
        transformPathMeasure = PathMeasure()
    }

    private fun initAnimators() {
        mValueAnimator = ValueAnimator.ofInt(0, 100).also {
            it.duration = animationDuration.toLong()
            it.addUpdateListener { animation: ValueAnimator ->
                val progress = animation.animatedValue as Int
                mNeedToClickIconButton?.clickProgress = (progress)
                mLastClickedIconButton?.transformProgress = (progress)
                //Log.d(TAG, "initAnimators: clickProgress:${progress}")
                onAnimationUpdateListener?.onUpdate(progress)
                invalidate()
            }
            it.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    resetProgress()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
    }

    /**
     * 清空已经进行的动画参数
     */
    private fun resetProgress() {
        mNeedToClickIconButton?.let {
            it.clickProgress = 100
            it.transformProgress = 0
        }
        mLastClickedIconButton?.let {
            it.clickProgress = 0
            it.transformProgress = 0
        }
        invalidate()
    }

    private fun getRandomInt(min: Int, max: Int): Int {
        return min + random.nextInt(max - min)
    }

    private fun getRandomFloat(min: Float, max: Float): Float {
        return min + random.nextFloat() * (max - min)
    }

    private fun startClickAnimation() {
        if (!mValueAnimator!!.isRunning) {
            mValueAnimator!!.start()
        }
    }

    /**
     * 绘制转移时的动画
     * @param canvas 绘制的画布（此处为缓冲画布）
     */
    private fun drawTransformAnimation(canvas: Canvas?) {
        if (mLastClickedIconButton == null || mNeedToClickIconButton == null) return
        if (mNeedToClickIconButton === mLastClickedIconButton) return
        val progress = mValueAnimator!!.animatedValue as Int
        if (progress == 0) {
            val direction =
                if (mNeedToClickIconButton!!.imageX < mLastClickedIconButton!!.imageX) Direction.RIGHT_TO_LEFT else Direction.LEFT_TO_RIGHT
            if (direction == Direction.LEFT_TO_RIGHT) {
                val rightCenter = mLastClickedIconButton!!.imageRightCenter
                startX = rightCenter[0]
                startY = rightCenter[1]
                val leftCenter = mNeedToClickIconButton!!.imageLeftCenter
                endX = leftCenter[0]
                endY = leftCenter[1]
            } else {
                val rightCenter = mNeedToClickIconButton!!.imageRightCenter
                endX = rightCenter[0]
                endY = rightCenter[1]
                val leftCenter = mLastClickedIconButton!!.imageLeftCenter
                startX = leftCenter[0]
                startY = leftCenter[1]
            }
            val num = getRandomInt(4, 8)
            transformPaths!!.clear()
            for (i in 0 until num) {
                val path = Path()
                path.moveTo(startX, startY)
                path.quadTo(
                    getRandomFloat(startX, endX),
                    getRandomFloat(startY, endY) + getRandomInt(-imageHeight * 2, imageHeight * 2),
                    endX,
                    endY
                )
                transformPaths!!.add(path)
            }
        } else if (progress >= 1) {
            val position = FloatArray(2)
            val tan = FloatArray(2)
            var radius: Float
            for (path in transformPaths!!) {
                transformPathMeasure!!.setPath(path, false)
                transformPathMeasure!!.getPosTan(
                    progress / 100f * transformPathMeasure!!.length,
                    position,
                    tan
                )
                radius = if (progress <= 50) {
                    progress / 100f * imageHeight / 2
                } else {
                    (1 - progress / 100f) * imageHeight / 2
                }
                mPaint.color = highlightColor
                canvas!!.drawCircle(position[0], position[1], radius, mPaint)
            }
        }
    }

    //测量View
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        mViewWidth = MeasureSpec.getSize(widthMeasureSpec)
        mViewHeight =
            if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
                2 * imageHeight
            } else MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(mViewWidth, mViewHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (iconButtonList == null || iconButtonList!!.size == 0) return
        mCacheCanvas.drawColor(navigationBgColor)
        for (iconButton in iconButtonList!!) {
            iconButton.drawSelf(mCacheCanvas)
        }
        drawTransformAnimation(mCacheCanvas)
        canvas.drawBitmap(mCacheBitmap, 0f, 0f, mPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                var i = 0
                while (i < iconButtonList!!.size) {
                    val iconButton = iconButtonList!![i]
                    if (iconButton.isClicked(event.x, event.y)) {
                        moveTo(i, true)
                        return true
                    }
                    i++
                }
                return performClick()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    /**
     * 跳转到对应页面
     * @param page 需要跳转的页面，取值 [0,页面总数-1]
     * @param hasAnimation 是否有动画效果
     * @param performClick 是否同时执行点击事件【请确保点击事件不会造成方法死循环】
     */
    @JvmOverloads
    fun moveTo(page: Int, hasAnimation: Boolean = true, performClick: Boolean = true) {
        if (iconButtonList == null) throw RuntimeException("Button list has not been initialized! Please make sure you have called initIconButtons(...) before or wait a moment and try again.")
        require(!(page < 0 || page >= iconButtonList!!.size)) { "Illegal page index! Please make sure that page is from 0 to (the number of buttons - 1)." }
        if (mValueAnimator!!.isRunning) return
        if (mLastPage != page) {
            mNeedToClickIconButton?.clickProgress = 0 //在开始动画之前先把之前完成的点击进度清零
            mLastClickedIconButton?.clickProgress = 0

            mLastClickedIconButton = iconButtonList!![mLastPage]
            mNeedToClickIconButton = iconButtonList!![page]
            val direction =
                if (mNeedToClickIconButton!!.imageX < mLastClickedIconButton!!.imageX) Direction.RIGHT_TO_LEFT else Direction.LEFT_TO_RIGHT
            mLastClickedIconButton!!.direction = (direction)

            mNeedToClickIconButton!!.direction = (direction)
            if (hasAnimation) startClickAnimation() else resetProgress()
            mLastPage = page
        }
        if (performClick) {
            onItemClickListener?.onClick(page)
        }
    }

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = object : OnItemClickListener {
            override fun onClick(position: Int) {
                listener(position)
            }
        }
    }

    override fun getPaddingTop(): Int {
        return iconOffsetY
    }

    /*
     当点击到底部按钮时会回调此接口
     参数 position 为当前点击的按钮位置，取值为[0,总数-1]
     注意，当动画仍在进行时点击无效，此时不会触发此回调
    */
    interface OnItemClickListener {
        fun onClick(position: Int)
    }

    /**
     * 当动画进行时会回调此接口
     * 参数 progress 值为 [[0-100]] 整数，代表当前动画进行的百分比
     */
    interface OnAnimationUpdateListener {
        fun onUpdate(progress: Int)
    }

    enum class Direction {
        LEFT_TO_RIGHT, RIGHT_TO_LEFT
    }

    init {
        val typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.FunnyBottomNavigation)
        with(typedArray) {
            iconOffsetY =
                this.getDimensionPixelOffset(R.styleable.FunnyBottomNavigation_paddingTop, 0)
            imageWidth =
                this.getDimensionPixelOffset(R.styleable.FunnyBottomNavigation_imageWidth, 80)
            imageHeight =
                this.getDimensionPixelOffset(R.styleable.FunnyBottomNavigation_imageHeight, 80)
            mLastPage = this.getInteger(R.styleable.FunnyBottomNavigation_startPage, 0)
            startPage = mLastPage
            navigationBgColor =
                this.getColor(R.styleable.FunnyBottomNavigation_backgroundColor, Color.WHITE)
            animationDuration =
                this.getInt(R.styleable.FunnyBottomNavigation_animationDuration, 500)
            normalColor = this.getColor(
                R.styleable.FunnyBottomNavigation_normalColor,
                Color.parseColor("#6e6c6f")
            )
            highlightColor = this.getColor(
                R.styleable.FunnyBottomNavigation_highlightColor,
                Color.parseColor("#069270")
            )
            this.recycle()
        }
        initGraphics()
        initAnimators()
//        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }
}