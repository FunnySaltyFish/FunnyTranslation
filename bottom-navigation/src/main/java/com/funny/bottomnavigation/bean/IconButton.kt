package com.funny.bottomnavigation.bean

import android.content.Context
import android.graphics.*
import com.funny.bottomnavigation.FunnyBottomNavigation
import com.funny.bottomnavigation.FunnyBottomNavigation.Companion.log
import com.funny.bottomnavigation.utils.BitmapUtils.getBitmapFromResources
import com.funny.bottomnavigation.utils.BitmapUtils.getScaledBitmap

class IconButton(
    var context: Context,
    var resId: Int,
    var x: Float, //左上角位置
    var y: Float,
    var width: Int,
    var height: Int,
    var imageWidth: Int,
    var imageHeight: Int,
    var srcColor: Int,
    var normalColor : Int,
    var clickMargin : Int = 8
) {
    var backgroundColor = Color.parseColor("#6e6c6f")
    var paddingLeft = 0
    var paddingRight = 0
    var paddingTop = 0
    var paddingBottom = 0
    var imageX = 0f
    var imageY = 0f
    lateinit var bitmap: Bitmap
    lateinit var paint: Paint
    private var xfermode: PorterDuffXfermode? = null
    var clickProgress = 0
    var transformProgress = 0
    var progressRect: RectF? = null
    private val maxScaleTimes = 1.5f
    var direction: FunnyBottomNavigation.Direction? = null

    var id = 0

    companion object {
        private const val TAG = "IconButton"
    }

    private fun initVars() {
        imageX = x + (width - imageWidth) / 2f
        imageY = y + (height - imageHeight) / 2f
    }

    private fun initGraphics() {
        bitmap = getBitmapFromResources(context.resources, resId)
        bitmap = getScaledBitmap(
            bitmap, imageWidth - paddingLeft - paddingRight, imageHeight - paddingTop - paddingBottom
        )

        paint = Paint().also {
            it.color = srcColor
            it.isAntiAlias = true
            it.alpha = 255
        }
        progressRect = RectF()
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    fun isClicked(actionX: Float, actionY: Float): Boolean {
        return actionX >= imageX - clickMargin && actionX <= imageX + imageWidth + clickMargin && actionY >= imageY - clickMargin && actionY <= imageY + imageHeight + clickMargin
    }

    /**
     * @description 绘制基本的bitmap和动画
     * @param canvas 绘制的画板
     * @return void
     */
    fun drawSelf(canvas: Canvas) {
         fun drawColorIcon(color : Int){
             val layerId = canvas.saveLayer(imageX, imageY, imageX + imageWidth, imageY + imageHeight, paint)
             paint.color = color
             paint.style = Paint.Style.FILL
             // +、- 2 是为了防止边框出现
             canvas.drawRect(imageX + 2, imageY + 2, imageX + imageWidth - 2, imageY + imageHeight - 2 ,paint)
             paint.xfermode = xfermode
             canvas.drawBitmap(bitmap, imageX, imageY, paint)
             paint.xfermode = null
             canvas.restoreToCount(layerId)
             log("$id drawColorIcon $color")
        }

        log("$id clickProgress: $clickProgress transformProgress :$transformProgress")
        if (clickProgress == 100){
            drawColorIcon(srcColor)
            return
        }
        else if(clickProgress > 0){
            canvas.save()
            if (clickProgress in 60..80) {
                val scale = 1 + (maxScaleTimes - 1f) / 20f * (clickProgress - 60)
//                log("$id drawSelf 6-8: px:${scale}")
                canvas.scale(
                    scale,
                    scale,
                    centerImageX,
                    centerImageY
                )
            } else if(clickProgress > 80) {
                val scale = maxScaleTimes - (maxScaleTimes - 1f) / 20f * (clickProgress - 80)
//                log("$id drawSelf: px 8-10:${maxScaleTimes}")
                canvas.scale(
                    scale,
                    scale,
                    centerImageX,
                    centerImageY
                )
            }

            // 单独缩放不绘制圆形不会导致边框
            val centerCircle = if(direction==FunnyBottomNavigation.Direction.LEFT_TO_RIGHT){//左向右减少
                imageLeftCenter
            }else{
                imageRightCenter
            }
            val radius = clickProgress / 100f * 2.236f * imageWidth / 2
            paint.style = Paint.Style.FILL
            drawColorIcon(normalColor)
            val layerId =
                canvas.saveLayer(imageX, imageY, imageX + imageWidth, imageY + imageHeight, paint)
            paint.color = srcColor
            // 加上clip后不会出现边框了
            canvas.clipRect(imageX + 2, imageY + 2, imageX + imageWidth - 2, imageY + imageHeight - 2)
            canvas.drawCircle(centerCircle[0],centerCircle[1],radius, paint)
            paint.xfermode = xfermode
            canvas.drawBitmap(bitmap, imageX, imageY, paint)
            paint.xfermode = null
            canvas.restoreToCount(layerId)
            //log("drawSelf: transformProgress:${transformProgress}")
            //log("drawSelf: radius:${radius}")

            //恢复到缩放前状态
            canvas.restore()
            paint.color = srcColor
            paint.style = Paint.Style.STROKE
            if (clickProgress <= 50) {
                paint.alpha = clickProgress * 255 / 100
                paint.strokeWidth = 24 * clickProgress / 100f
            } else {
                paint.alpha = 255 - clickProgress * 255 / 100
                paint.strokeWidth = 24 * (1 - clickProgress / 100f)
            }
            canvas.drawCircle(
                imageX + imageWidth / 2f,
                imageY + imageHeight / 2f,
                0.75f * imageHeight * clickProgress / 100f,
                paint
            )
        }
        else{
            drawColorIcon(normalColor)
            return
        }

        if (transformProgress > 0) {
            paint.alpha = 255
            val centerCircle = if(direction==FunnyBottomNavigation.Direction.LEFT_TO_RIGHT){//左向右减少
                imageRightCenter
            }else{
                imageLeftCenter
            }
            val radius = (100f - transformProgress) / 100f * 2.236f * imageWidth / 2
            drawColorIcon(normalColor)
            val layerId =
                canvas.saveLayer(imageX, imageY, imageX + imageWidth, imageY + imageHeight, paint)
            paint.color = srcColor
            canvas.drawCircle(centerCircle[0],centerCircle[1],radius, paint)
            paint.xfermode = xfermode
            paint.color = backgroundColor
            canvas.drawBitmap(bitmap, imageX, imageY, paint)
            paint.xfermode = null
            canvas.restoreToCount(layerId)
            //log("drawSelf: transformProgress:${transformProgress}")
            //log("drawSelf: radius:${radius}")
        }
        else drawColorIcon(normalColor)

//        log("$id drawNormal")
//        drawNormalColorIcon()
    }

    val imageLeftCenter: FloatArray
        get() = floatArrayOf(imageX, imageY + imageHeight / 2f)
    val imageRightCenter: FloatArray
        get() = floatArrayOf(imageX + imageWidth, imageY + imageHeight / 2f)

    private val centerImageX : Float
        get() = imageX + imageWidth / 2f

    private val centerImageY : Float
        get() = imageY + imageHeight / 2f

    init {
        initVars()
        initGraphics()
    }
}