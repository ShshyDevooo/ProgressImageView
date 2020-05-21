package com.shshy.progressimageview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @author  ShiShY
 * @Description:
 * @data  2020/5/19 11:34
 */
class ProgressImageView : AppCompatImageView {
    private var showProgress: Boolean = false
    private var currentProgress: Int = 0
    private var coverColor: Int = Color.argb(127, 0, 0, 0)
    private lateinit var textPaint: Paint
    private var textSize = 14f
    private var textColor = Color.WHITE
    private var textCirclePadding = 10f
    private lateinit var progressPaint: Paint
    private var progressStrokeWidth = 10f
    private var isCircle = false
    private var rectRadius = 0f
    private var circleRadius = 0f
    private var showProgressStroke = true
    private var progressStrokeColor = Color.WHITE
    private val clipPath = Path()
    private val viewRect: RectF = RectF()
    private val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressXfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val radiusXfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    private lateinit var coverPaint: Paint
    private var shadowAni: ValueAnimator? = null
    private lateinit var shadowPaint: Paint
    private val viewPath: Path = Path()

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressImageView)
        textSize = typeArray.getDimension(R.styleable.ProgressImageView_piv_textSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, context.resources.displayMetrics))
        showProgress = typeArray.getBoolean(R.styleable.ProgressImageView_piv_showProgress, true)
        currentProgress = typeArray.getInteger(R.styleable.ProgressImageView_piv_progress, 0)
        coverColor = typeArray.getColor(R.styleable.ProgressImageView_piv_coverColor, coverColor)
        textColor = typeArray.getColor(R.styleable.ProgressImageView_piv_textColor, textColor)
        textCirclePadding = typeArray.getDimension(R.styleable.ProgressImageView_piv_circlePadding, textCirclePadding)
        progressStrokeWidth = typeArray.getDimension(R.styleable.ProgressImageView_piv_progressStrokeWidth, progressStrokeWidth)
        isCircle = typeArray.getBoolean(R.styleable.ProgressImageView_piv_isCircle, isCircle)
        rectRadius = typeArray.getDimension(R.styleable.ProgressImageView_piv_radius, rectRadius)
        showProgressStroke = typeArray.getBoolean(R.styleable.ProgressImageView_piv_showProgressStroke, showProgressStroke)
        progressStrokeColor = typeArray.getColor(R.styleable.ProgressImageView_piv_progressStrokeColor, progressStrokeColor)
        typeArray.recycle()

        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = textSize
        textPaint.color = textColor
        textPaint.style = Paint.Style.FILL
        textPaint.textAlign = Paint.Align.CENTER

        coverPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        coverPaint.color = coverColor

        progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        progressPaint.strokeWidth = progressStrokeWidth
        progressPaint.style = Paint.Style.FILL
        progressPaint.color = coverColor

        shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        shadowPaint.style = Paint.Style.FILL
        shadowPaint.xfermode = progressXfermode
        shadowPaint.color = coverColor
    }

    override fun onDraw(canvas: Canvas?) {
        if (showProgress) {
            val radiusSaveCount = saveRadiusOrCircleLayer(canvas)
            super.onDraw(canvas)
            //画蒙板
            val layoutCount = canvas?.saveLayer(viewRect, null, Canvas.ALL_SAVE_FLAG)
            canvas?.drawRect(viewRect, coverPaint)
            //进度扇形
            val text = if (currentProgress < 10) "0$currentProgress%" else "$currentProgress%"
            progressPaint.xfermode = progressXfermode
            val progress = (currentProgress / 100f) * 360
            progressPaint.style = Paint.Style.FILL
            progressPaint.color = coverColor
            val arcRect = getArcRect()
            canvas?.drawArc(arcRect, 270f, progress, true, progressPaint)
            progressPaint.xfermode = null
            canvas?.restoreToCount(layoutCount ?: 0)
            //进度描边
            if (showProgressStroke) {
                progressPaint.style = Paint.Style.STROKE
                progressPaint.color = progressStrokeColor
                progressPaint.strokeWidth = progressStrokeWidth
                canvas?.drawArc(arcRect, (270f + progress) % 360, 360f - progress, false, progressPaint)
            }
            //进度文字
            canvas?.drawText(text, width.toFloat() / 2, getCenterBaseLine(), textPaint)
            //如果有圆角或者是圆形图片
            if (radiusSaveCount != null && radiusSaveCount != -1) {
                clipImageRadiusOrCircle(radiusSaveCount, canvas)
                canvas?.restoreToCount(radiusSaveCount)
            }
        } else {
            val radiusSaveCount = saveRadiusOrCircleLayer(canvas)
            super.onDraw(canvas)
            if (radiusSaveCount != null && radiusSaveCount != -1) {
                clipImageRadiusOrCircle(radiusSaveCount, canvas, true)
                canvas?.restoreToCount(radiusSaveCount)
            } else {
                if (shadowAni?.isRunning == true) {
                    drawShadowAnimate(viewPath, canvas)
                }
            }
        }
    }

    private fun saveRadiusOrCircleLayer(canvas: Canvas?): Int? {
        return if (rectRadius != 0f || isCircle)
            canvas?.saveLayer(viewRect, null, Canvas.ALL_SAVE_FLAG)
        else
            -1
    }

    //将此Image切成圆角或圆形,shouldAnimate是否需要显示蒙层消失动画
    private fun clipImageRadiusOrCircle(saveCount: Int?, canvas: Canvas?, shouldAnimate: Boolean = false) {
        if (saveCount != null && saveCount != -1) {
            clipPath.reset()
            clipPaint.reset()
            if (isCircle) {
                clipPath.addCircle(width.toFloat() / 2, height.toFloat() / 2, circleRadius, Path.Direction.CCW)
            } else {
                clipPath.addRoundRect(viewRect, rectRadius, rectRadius, Path.Direction.CCW)
            }
            clipPaint.xfermode = radiusXfermode
            canvas?.drawPath(clipPath, clipPaint)
            clipPaint.xfermode = null
            if (shouldAnimate && shadowAni?.isRunning == true) {
                drawShadowAnimate(clipPath, canvas)
            }
        }
    }

    //根据蒙层消失动画值画蒙层
    private fun drawShadowAnimate(path: Path, canvas: Canvas?) {
        val shadowCount = canvas?.saveLayer(viewRect, null, Canvas.ALL_SAVE_FLAG)
        canvas?.drawPath(path, coverPaint)
        canvas?.drawCircle(width.toFloat() / 2, height.toFloat() / 2, shadowAni?.animatedValue as Float, shadowPaint)
        canvas?.restoreToCount(shadowCount ?: 0)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (isCircle) {
            circleRadius = Math.min(width, height) / 2f
            viewRect.set(width / 2f - circleRadius, height / 2f - circleRadius, width / 2f + circleRadius, height / 2f + circleRadius)
        } else
            viewRect.set(0f, 0f, width.toFloat(), height.toFloat())
        viewPath.reset()
        viewPath.addRect(viewRect, Path.Direction.CCW)
        super.onSizeChanged(w, h, oldw, oldh)
    }

    //进度扇形矩形
    private fun getArcRect(): RectF {
        val arcRadius = getArcRadius()
        return RectF(width / 2 - arcRadius, height / 2 - arcRadius, width / 2 + arcRadius, height / 2 + arcRadius)
    }

    //进度扇形半径
    private fun getArcRadius(): Float {
        val textWidth = textPaint.measureText("100%")
        return textWidth / 2 + textCirclePadding
    }

    private fun getCenterBaseLine(): Float {
        val fontMetrics = textPaint.fontMetrics
        return height.toFloat() / 2 + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
    }

    fun setProgress(progress: Int) {
        currentProgress = progress
        if (currentProgress >= 100) {
            showProgress = false
            dismissShadowAnimation()
        }
        invalidate()
    }

    fun showProgress(show: Boolean) {
        this.showProgress = show
        invalidate()
    }

    fun getProgress(): Int {
        return currentProgress
    }

    //蒙层消失动画
    private fun dismissShadowAnimation() {
        Handler(Looper.getMainLooper()).post {
            val maxRadius =
                    sqrt((width.toDouble() / 2).pow(2.toDouble()) + (height.toDouble() / 2).pow(2.toDouble()))
            shadowAni = ValueAnimator.ofFloat(getArcRadius(), maxRadius.toFloat())
            shadowAni?.addUpdateListener { invalidate() }
            shadowAni?.duration = 500
            shadowAni?.start()
        }
    }
}