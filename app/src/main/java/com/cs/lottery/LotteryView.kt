package com.cs.lottery

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.math.min

/**
 * @Author chenshuai
 * @Time 2020/12/17 0017 17:02
 */
class LotteryView(context: Context, attributeSet: AttributeSet?) : View(context, attributeSet) {
    private val bgPaint: Paint = Paint()

    init {
        bgPaint.color = Color.rgb(99, 99, 99)
        bgPaint.style = Paint.Style.STROKE
        bgPaint.strokeWidth = 10f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        var wSize = MeasureSpec.getSize(widthMeasureSpec)
        var hSize = MeasureSpec.getSize(heightMeasureSpec)
        if (wMode == MeasureSpec.AT_MOST) {
            wSize =
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    200f,
                    context.resources.displayMetrics
                ).toInt()

        }
        if (hMode == MeasureSpec.AT_MOST) {
            hSize =
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    200f,
                    context.resources.displayMetrics
                ).toInt()

        }
        setMeasuredDimension(wSize, hSize)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val radius = min(measuredWidth, measuredHeight) / 2f
        canvas?.drawCircle(radius, radius, radius, bgPaint)
    }
}