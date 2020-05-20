package com.shshy.progressimageview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView

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
    }

    override fun onDraw(canvas: Canvas?) {
        if (showProgress) {
            val radiusSaveCount = if (rectRadius != 0f || isCircle)
                canvas?.saveLayer(viewRect, null, Canvas.ALL_SAVE_FLAG)
            else
                -1
            super.onDraw(canvas)
            //画蒙板
            val layoutCount = canvas?.saveLayer(viewRect, null, Canvas.ALL_SAVE_FLAG)
            canvas?.drawRect(viewRect, coverPaint)
            val text = if (currentProgress < 10) "0$currentProgress%" else "$currentProgress%"
            progressPaint.xfermode = progressXfermode
            val progress = (currentProgress / 100f) * 360
            progressPaint.style = Paint.Style.FILL_AND_STROKE
            progressPaint.color = coverColor
            canvas?.drawArc(getArcRect(), 270f, progress, true, progressPaint)
            progressPaint.xfermode = null
            canvas?.restoreToCount(layoutCount ?: 0)
            if (showProgressStroke) {
                progressPaint.style = Paint.Style.STROKE
                progressPaint.color = progressStrokeColor
                progressPaint.strokeWidth = progressStrokeWidth
                canvas?.drawArc(getArcRect(), (270f + progress) % 360, 360f - progress, false, progressPaint)
            }
            canvas?.drawText(text, width.toFloat() / 2, getCenterBaseLine(), textPaint)
            //如果有圆角或者是圆形图片
            if (radiusSaveCount != null && radiusSaveCount != -1) {
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
                canvas?.restoreToCount(radiusSaveCount)
            }
        } else {
            val radiusSaveCount = if (rectRadius != 0f || isCircle)
                canvas?.saveLayer(viewRect, null, Canvas.ALL_SAVE_FLAG)
            else
                -1
            super.onDraw(canvas)
            if (radiusSaveCount != null && radiusSaveCount != -1) {
                clipPath.reset()
                if (isCircle) {
                    clipPath.addCircle(width.toFloat() / 2, height.toFloat() / 2, circleRadius, Path.Direction.CCW)
                } else {
                    clipPath.addRoundRect(viewRect, rectRadius, rectRadius, Path.Direction.CCW)
                }
                clipPaint.xfermode = radiusXfermode
                canvas?.drawPath(clipPath, clipPaint)
                clipPaint.xfermode = null
                canvas?.restoreToCount(radiusSaveCount)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (isCircle) {
            circleRadius = Math.min(width, height) / 2f
            viewRect.set(width / 2f - circleRadius, height / 2f - circleRadius, width / 2f + circleRadius, height / 2f + circleRadius)
        } else
            viewRect.set(0f, 0f, width.toFloat(), height.toFloat())
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun getArcRect(): RectF {
        val textWidth = textPaint.measureText("100%")
        return RectF(width / 2 - (textWidth / 2 + textCirclePadding), height / 2 - (textWidth / 2 + textCirclePadding), width / 2 + (textWidth / 2 + textCirclePadding), height / 2 + (textWidth / 2 + textCirclePadding))
    }

    private fun getCenterBaseLine(): Float {
        val fontMetrics = textPaint.fontMetrics
        return height.toFloat() / 2 + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
    }

    fun setProgress(progress: Int) {
        currentProgress = progress
        invalidate()
    }

    fun showProgress(show: Boolean) {
        this.showProgress = show
        invalidate()
    }
}