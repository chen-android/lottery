package com.cs.lottery

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
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
    private val tag = "lotteryView"
    private val bgPaint: Paint = Paint()
    private val ballPaint: Paint = Paint()
    private val textPaint: Paint = Paint()
    private val linePaint: Paint = Paint()
    private val ballList: MutableList<Ball> = mutableListOf()
    val nameList: MutableList<String> = mutableListOf()
    private var ballCount: Int = 8
    private var ballRadius: Float = 20f
    private var defaultTextSize: Float = 12f
    private val maxTextSize = 50f
    private var diameter: Int = 0
    private var bgRadius: Float = 0f
    var currentStatus = Status.INIT
    private var findBall: Ball? = null
    private var lineLength = 1f
    var findGoatListener: (ball: Ball) -> Unit = {}

    init {
        context.obtainStyledAttributes(attributeSet, R.styleable.LotteryView).use {
            ballCount = it.getInteger(R.styleable.LotteryView_ballCount, 8)
            ballRadius = it.getDimension(R.styleable.LotteryView_ballRadius, 40f)
            defaultTextSize = it.getDimension(R.styleable.LotteryView_android_textSize, 40f)
        }

        bgPaint.color = Color.rgb(99, 99, 99)
        bgPaint.style = Paint.Style.STROKE
        bgPaint.strokeWidth = 10f
        bgPaint.isAntiAlias = true

        ballPaint.style = Paint.Style.STROKE
        ballPaint.strokeWidth = 4f
        ballPaint.isAntiAlias = true

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = defaultTextSize

        linePaint.color = Color.RED
        linePaint.strokeWidth = 4f
        linePaint.isAntiAlias = true
    }

    fun setNames(names: List<String>) {
        this.nameList.clear()
        this.nameList.addAll(names)
        this.ballCount = names.size
        resetBalls()
        currentStatus = Status.INIT
        invalidate()
    }

    fun startRoll() {
        findBall = null
        lineLength = 1f
        currentStatus = Status.START_ROLL
        invalidate()
    }

    fun stopRoll() {
        currentStatus = Status.STOP_ROLL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        diameter = min(w, h)
        resetBalls()
    }

    private fun resetBalls() {
        if (diameter == 0) {
            return
        }
        ballList.clear()
        bgRadius = diameter / 2f
        for (i in 0 until ballCount) {
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
                    if (i < nameList.size) nameList[i] else i.toString(), x, y,
                    Color.rgb(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255)),
                    ballRadius, 10, Random.nextInt(360), textSize = defaultTextSize
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

        when (currentStatus) {
            Status.INIT -> {
                ballList.forEach { ball ->
                    drawBall(canvas, ball)
                }
            }
            Status.START_ROLL -> {
                ballList.forEach { ball ->
                    drawBall(canvas, ball)
                }
                ballList.forEach { ball ->
                    moveToNewPoint(ball)
                }
                this.postDelayed({ invalidate() }, 10L)
            }
            Status.STOP_ROLL -> {
                ballList.forEach { ball ->
                    drawBall(canvas, ball)
                }
                ballList.forEach { ball ->
                    moveToNewPoint(ball)
                }
                this.postDelayed({ invalidate() }, 10L)
            }
            Status.FIND_ONE -> {
                ballList.forEach { ball ->
                    drawBall(canvas, ball)
                }
                var delay = 0L
                if (findBall == null) {
                    findBall = ballList.minByOrNull { ball ->
                        val dis = twoPointAway(ball.x, ball.y, bgRadius, bgRadius)
                        ball.distance = dis
                        ball.hRatio = (ball.y - bgRadius) / dis
                        ball.wRatio = (ball.x - bgRadius) / dis
                        dis
                    }
                    Log.d(tag, "findBall:  $findBall")
                    delay = 1000L
                    lineLength = 1f
                } else {
                    delay = 10L
                }
                if (lineLength < findBall!!.distance!!) {
                    val lineEndX = bgRadius + lineLength * findBall!!.wRatio!!
                    val lineEndY = bgRadius + lineLength * findBall!!.hRatio!!
                    drawLine(canvas, lineEndX, lineEndY)
                    lineLength += 6
                } else {
                    lineLength = findBall!!.distance!!
                    currentStatus = Status.SHOW_GOAT
                }
                this.postDelayed({ invalidate() }, delay)
            }
            Status.SHOW_GOAT -> {
                lineLength -= 10
                if (lineLength > 0) {
                    ballList.forEach { ball ->
                        if (ball.text == findBall!!.text) {
                            ball.x = bgRadius + lineLength * findBall!!.wRatio!!
                            ball.y = bgRadius + lineLength * findBall!!.hRatio!!
                        } else {
                            ball.alpha = lineLength / ball.distance!!
                        }
                        drawBall(canvas, ball)
                    }
                } else {
                    findBall!!.radius += 10f
                    if (findBall!!.textSize <= maxTextSize) {
                        findBall!!.textSize += 3f
                    }
                    drawBall(canvas, findBall!!)
                    if (findBall!!.radius >= bgRadius * 0.5) {
                        findGoatListener(findBall!!)
                        return
                    }
                }
                this.postDelayed({ invalidate() }, 10L)
            }

        }
    }

    private fun drawBall(canvas: Canvas?, ball: Ball) {
        ballPaint.color = ball.color
        ballPaint.alpha = (ball.alpha * 255).toInt()
        textPaint.alpha = (ball.alpha * 255).toInt()
        textPaint.textSize = ball.textSize
        canvas?.drawCircle(ball.x, ball.y, ball.radius, ballPaint)
        canvas?.drawText(ball.text, ball.x, ball.y + defaultTextSize * 2 / 5, textPaint)
    }

    private fun twoPointAway(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))
    }

    private fun moveToNewPoint(ball: Ball) {
        var targetX = ball.x + (ball.speed * cos(Math.toRadians(ball.angle.toDouble()))).toFloat()
        var targetY = ball.y + (ball.speed * sin(Math.toRadians(ball.angle.toDouble()))).toFloat()
        val twoPointAway = twoPointAway(targetX, targetY, bgRadius, bgRadius)
        if (twoPointAway > bgRadius - ballRadius) {
            ball.angle += 60 + Random.nextInt(180)
            moveToNewPoint(ball)
        } else {
            ball.x = targetX
            ball.y = targetY

            if (currentStatus == Status.START_ROLL && ball.speed < 160) ball.speed += 1
            if (currentStatus == Status.STOP_ROLL && ball.speed > 0) {
                ball.speed -= 1
                if (ball.speed == 0) {
                    currentStatus = Status.FIND_ONE
                }
            }
        }
    }

    private fun drawLine(canvas: Canvas?, x: Float, y: Float) {
        canvas?.drawLine(bgRadius, bgRadius, x, y, linePaint)
    }


    data class Ball(
        val text: String,
        var x: Float,
        var y: Float,
        val color: Int,
        var radius: Float,
        var speed: Int,
        var angle: Int,
        var alpha: Float = 1f,
        var textSize: Float = 12f,
        var distance: Float? = null,
        var hRatio: Float? = null,
        var wRatio: Float? = null
    )

    enum class Status {
        INIT, START_ROLL, STOP_ROLL, FIND_ONE, SHOW_GOAT
    }
}