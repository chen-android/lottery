package com.cs.lottery

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.use
import kotlin.math.*
import kotlin.random.Random

/**
 * @Author chenshuai
 * @Time 2020/12/17 0017 17:02
 */
class LotteryView(context: Context, attributeSet: AttributeSet?) : View(context, attributeSet) {
    private val bgPaint: Paint = Paint()
    private val ballPaint: Paint = Paint()
    private val textPaint: Paint = Paint()
    private val ballList: MutableList<Ball> = mutableListOf()
    private var ballCount: Int = 8
    private var ballRadius: Float = 20f
    private var textSize: Float = 12f
    private var diameter: Int = 0
    private var bgRadius: Float = 0f

    init {
        context.obtainStyledAttributes(attributeSet, R.styleable.LotteryView).use {
            ballCount = it.getInteger(R.styleable.LotteryView_ballCount, 8)
            ballRadius = it.getDimension(R.styleable.LotteryView_ballRadius, 40f)
            textSize = it.getDimension(R.styleable.LotteryView_android_textSize, 40f)
        }

        bgPaint.color = Color.rgb(99, 99, 99)
        bgPaint.style = Paint.Style.STROKE
        bgPaint.strokeWidth = 10f

        ballPaint.style = Paint.Style.STROKE
        ballPaint.strokeWidth = 4f

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = textSize
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        ballList.clear()
        diameter = min(w, h)
        bgRadius = diameter / 2f
        for (i in 1..ballCount) {
            var x = 0f
            var y = 0f
            while (true) {
                x = Random.nextInt(diameter).toFloat()
                y = Random.nextInt(diameter).toFloat()
                if (twoPointAway(x, y, bgRadius, bgRadius) > bgRadius - ballRadius) {
                    continue
                }
                if (ballList.find { ball -> twoPointAway(x, y, ball.x, ball.y) < ballRadius * 2 } != null) {
                    continue
                }
                break
            }
            ballList.add(
                Ball(
                    i.toString(), x, y,
                    Color.rgb(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255)),
                    ballRadius, 10, Random.nextInt(360)
                )
            )
        }
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
        ballList.forEach { ball ->
            drawBall(canvas, ball)
        }
        ballList.forEach { ball ->
            var targetX = ball.x + (ball.speed * cos(Math.toRadians(ball.angle.toDouble()))).toFloat()
            var targetY = ball.x + (ball.speed * sin(Math.toRadians(ball.angle.toDouble()))).toFloat()
            val twoPointAway = twoPointAway(targetX, targetY, bgRadius, bgRadius)
            if (twoPointAway > bgRadius - ballRadius) {

            }
        }
    }

    private fun drawBall(canvas: Canvas?, ball: Ball) {
        ballPaint.color = ball.color
        canvas?.drawCircle(ball.x, ball.y, ball.radius, ballPaint)
        canvas?.drawText(ball.text, ball.x, ball.y + textSize * 2 / 5, textPaint)
    }

    private fun twoPointAway(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))
    }


    data class Ball(
        val text: String,
        var x: Float,
        var y: Float,
        val color: Int,
        val radius: Float,
        var speed: Int,
        var angle: Int
    )
}