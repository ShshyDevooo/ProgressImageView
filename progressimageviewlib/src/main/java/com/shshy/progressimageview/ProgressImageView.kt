package com.shshy.progressimageview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView

/**
 * @author  ShiShY
 * @Description:
 * @data  2020/5/19 11:34
 */
class ProgressImageView : ImageView {
    private var showProgress: Boolean = false
    private var currentProgress: Int = 0
    private var coverColor: Int = Color.argb(127, 0, 0, 0)
    private lateinit var textPaint: Paint
    private var textSize = 14f
    private var textColor = Color.WHITE
    private var textCirclePadding = 10f
    private lateinit var circlePaint: Paint
    private var circleStrokeWidth = 10f
    private val progressXfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private var isCircle = false
    private var radius = 0f
    private val clipPath = Path()
    private val radiusXfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    private val viewRect: RectF = RectF()
    private val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG)

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
        circleStrokeWidth = typeArray.getDimension(R.styleable.ProgressImageView_piv_circleStrokeWidth, circleStrokeWidth)
        isCircle = typeArray.getBoolean(R.styleable.ProgressImageView_piv_isCircle, isCircle)
        radius = typeArray.getDimension(R.styleable.ProgressImageView_piv_radius, radius)
        typeArray.recycle()

        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = textSize
        textPaint.color = textColor
        textPaint.style = Paint.Style.FILL
        textPaint.textAlign = Paint.Align.CENTER

        circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint.strokeWidth = circleStrokeWidth
        circlePaint.style = Paint.Style.FILL_AND_STROKE
        circlePaint.color = coverColor

        clipPaint.color = Color.WHITE
        circlePaint.style = Paint.Style.FILL
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas?) {
        if (showProgress) {
            val radiusSaveCount = if (radius != 0f || isCircle)
                canvas?.saveLayer(viewRect, clipPaint, Canvas.ALL_SAVE_FLAG)
            else
                -1
            super.onDraw(canvas)
            //画蒙板
            val layoutCount = canvas?.saveLayer(viewRect, circlePaint, Canvas.ALL_SAVE_FLAG)
            canvas?.drawRect(viewRect, circlePaint)
            val text = if (currentProgress < 10) "0$currentProgress%" else "$currentProgress%"
            circlePaint.xfermode = progressXfermode
            val progress = (currentProgress / 100f) * 360
            canvas?.drawArc(getArcRect(), 270f, progress, true, circlePaint)
            circlePaint.xfermode = null
            canvas?.restoreToCount(layoutCount ?: 0)
            canvas?.drawText(text, width.toFloat() / 2, getCenterBaseLine(), textPaint)
            if (radiusSaveCount != null && radiusSaveCount != -1) {
                clipPath.reset()
                if (isCircle) {
                    val minDimension = width.coerceAtMost(height)
                    clipPath.addCircle(width.toFloat() / 2, height.toFloat() / 2, minDimension.toFloat(), Path.Direction.CCW)
                } else {
                    clipPath.addRoundRect(viewRect, radius, radius, Path.Direction.CCW)
                }
                clipPaint.xfermode = radiusXfermode
                canvas?.drawPath(clipPath, clipPaint)
                clipPaint.xfermode = null
                canvas?.restoreToCount(radiusSaveCount)
            }
        } else {
            super.onDraw(canvas)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
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