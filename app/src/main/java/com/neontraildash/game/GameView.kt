paint.setStrokeWidth(5f)
paint.setStrokeCap(Paint.Cap.ROUND)
package com.neontraildash.game

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.random.Random

class GameView(
    context: Context,
    private val onRewardRequested: () -> Unit,
    private val onRunFinished: () -> Unit
) : View(context) {
    private val prefs = context.getSharedPreferences("neon_dash", Context.MODE_PRIVATE)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stars = List(65) { Pair(Random.nextFloat(), Random.nextFloat() * .66f) }
    private var state = State.MENU
    private enum class State { MENU, RUNNING, OVER }
    private var playerY = 0f
    private var velocityY = 0f
    private var frame = 0
    private var score = 0
    private var coins = prefs.getInt("coins", 0)
    private var best = prefs.getInt("best", 0)
    private var speed = 6f
    private var obstacles = mutableListOf<Obstacle>()
    private var pickups = mutableListOf<Pickup>()
    private var lastTime = 0L
    private var bonusMessageUntil = 0L

    private data class Obstacle(var x: Float, val width: Float, val height: Float)
    private data class Pickup(var x: Float, val y: Float)

    private val cyan = Color.rgb(75, 231, 242)
    private val pink = Color.rgb(255, 80, 135)
    private val gold = Color.rgb(255, 218, 65)
    private val white = Color.WHITE

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        drawBackground(canvas, w, h)
        val ground = h * .82f
        drawTrack(canvas, w, h, ground)
        pickups.forEach { drawCoin(canvas, it.x, it.y) }
        obstacles.forEach { drawObstacle(canvas, it, ground) }
        drawRunner(canvas, w * .16f, ground - h * .105f + playerY)
        drawHud(canvas, w, h)

        when (state) {
            State.MENU -> drawOverlay(canvas, w, h, "NEON TRAIL DASH", "Tap anywhere to start")
            State.OVER -> {
                drawOverlay(canvas, w, h, "RUN OVER", "Score ${score / 5}   •   Coins $coins")
                drawButton(canvas, w * .34f, h * .69f, w * .66f, h * .81f, "WATCH AD  +50 COINS")
                drawButton(canvas, w * .69f, h * .69f, w * .91f, h * .81f, "RUN AGAIN")
            }
            State.RUNNING -> {
                if (System.currentTimeMillis() < bonusMessageUntil) {
                    centeredText(canvas, "BONUS COINS ADDED!", w / 2f, h * .24f, h * .045f, gold)
                }
            }
        }

        if (state == State.RUNNING) {
            val now = System.nanoTime()
            val dt = if (lastTime == 0L) 16f else ((now - lastTime) / 1_000_000f).coerceIn(1f, 34f)
            lastTime = now
            update(dt / 16f, w, h, ground)
            postInvalidateOnAnimation()
        } else lastTime = 0L
    }

    private fun drawBackground(c: Canvas, w: Float, h: Float) {
        c.drawColor(Color.rgb(13, 17, 49))
        paint.shader = LinearGradient(0f, 0f, 0f, h, Color.rgb(15, 26, 69), Color.rgb(84, 36, 112), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, paint)
        paint.shader = null

        stars.forEachIndexed { i, p ->
            paint.color = if (i % 4 == 0) cyan else Color.WHITE
            c.drawCircle(p.first * w, p.second * h, if (i % 5 == 0) 2.5f else 1.4f, paint)
        }

        for (i in 0..10) {
            val bw = w / 11f
            val x = i * bw
            val bh = h * (.14f + ((i * 17) % 6) * .035f)
            paint.color = Color.rgb(28, 37, 78)
            c.drawRect(x, h * .68f - bh, x + bw - 4, h * .68f, paint)
            paint.color = Color.rgb(66, 194, 222)
            for (j in 0..2) {
                c.drawRect(x + 8 + j * 13, h * .68f - bh + 12, x + 14 + j * 13, h * .68f - bh + 23, paint)
            }
        }
    }

    private fun drawTrack(c: Canvas, w: Float, h: Float, ground: Float) {
        paint.color = Color.rgb(22, 23, 48)
        c.drawRect(0f, ground, w, h, paint)

        paint.color = cyan
        paint.setStrokeWidth(4f)
        c.drawLine(0f, ground, w, ground, paint)

        paint.color = Color.rgb(110, 88, 192)
        val offset = (frame * speed * 1.8f) % 90f
        for (i in -1..(w / 90).toInt() + 1) {
            val x = i * 90f - offset
            c.drawRoundRect(x, ground + (h-ground)*.48f, x + 38f, ground + (h-ground)*.55f, 4f, 4f, paint)
        }
    }

    private fun drawRunner(c: Canvas, x: Float, y: Float) {
        paint.color = Color.argb(70, 0, 0, 0)
        c.drawOval(x - 25, height * .82f - 7, x + 25, height * .82f + 3, paint)

        paint.color = cyan
        c.drawCircle(x, y + 18, 19f, paint)

        paint.color = Color.rgb(32, 35, 73)
        c.drawRoundRect(x - 15, y + 31, x - 4, y + 53, 5f, 5f, paint)
        c.drawRoundRect(x + 5, y + 31, x + 16, y + 53, 5f, 5f, paint)

        paint.color = white
        c.drawCircle(x + 7, y + 14, 4f, paint)

        paint.color = Color.rgb(30, 35, 72)
        c.drawCircle(x + 8, y + 14, 1.7f, paint)

        paint.color = gold
        paint.setStrokeWidth(5f)
        paint.setStrokeCap(Paint.Cap.ROUND)
        c.drawLine(x - 15, y + 20, x - 29, y + 31, paint)
    }

    private fun drawObstacle(c: Canvas, o: Obstacle, ground: Float) {
        paint.color = pink
        val path = Path().apply {
            moveTo(o.x, ground)
            lineTo(o.x + o.width/2, ground-o.height)
            lineTo(o.x+o.width, ground)
            close()
        }
        c.drawPath(path, paint)

        paint.color = Color.rgb(255, 174, 192)
        c.drawRect(o.x, ground-4, o.x+o.width, ground, paint)
    }

    private fun drawCoin(c: Canvas, x: Float, y: Float) {
        paint.color = gold
        c.drawCircle(x, y, 11f, paint)

        paint.color = Color.rgb(255, 244, 173)
        paint.style = Paint.Style.STROKE
        paint.setStrokeWidth(3f)
        c.drawCircle(x, y, 7f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawHud(c: Canvas, w: Float, h: Float) {
        val size = h * .045f
        text(c, "SCORE  ${score/5}", w*.035f, h*.075f, size, white, Paint.Align.LEFT)
        text(c, "🪙  $coins", w*.5f, h*.075f, size, gold, Paint.Align.CENTER)
        text(c, "BEST  $best", w*.965f, h*.075f, size, white, Paint.Align.RIGHT)
    }

    private fun drawOverlay(c: Canvas, w: Float, h: Float, title: String, subtitle: String) {
        paint.color = 0xB900071C.toInt()
        c.drawRect(0f, 0f, w, h, paint)
        centeredText(c, title, w/2f, h*.39f, h*.105f, white)
        centeredText(c, subtitle, w/2f, h*.51f, h*.05f, cyan)
    }

    private fun drawButton(c: Canvas, l: Float, t: Float, r: Float, b: Float, label: String) {
        paint.color = if (label.startsWith("WATCH")) Color.rgb(105, 76, 207) else Color.rgb(31, 173, 190)
        c.drawRoundRect(l, t, r, b, 18f, 18f, paint)
        centeredText(c, label, (l+r)/2f, (t+b)/2f + 8f, height*.033f, white)
    }

    private fun centeredText(c: Canvas, value: String, x: Float, y: Float, size: Float, color: Int) =
        text(c, value, x, y, size, color, Paint.Align.CENTER)

    private fun text(c: Canvas, value: String, x: Float, y: Float, size: Float, color: Int, align: Paint.Align) {
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = color
        paint.typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
        paint.textSize = size
        paint.textAlign = align
        c.drawText(value, x, y, paint)
    }

    private fun update(dt: Float, w: Float, h: Float, ground: Float) {
        frame++
        score++
        speed = (6f + frame / 1500f).coerceAtMost(11f)
        velocityY += .72f * dt
        playerY += velocityY * dt

        if (playerY > 0f) {
            playerY = 0f
            velocityY = 0f
        }

        if (frame % max(48, (88 - speed * 3).toInt()) == 0) {
            obstacles.add(Obstacle(w + 30, h * .047f, h * (.09f + Random.nextFloat() * .045f)))
        }

        if (frame % 68 == 0) {
            pickups.add(Pickup(w + 25, ground - h * (.18f + Random.nextFloat()*.08f)))
        }

        obstacles.forEach { it.x -= speed * dt }
        pickups.forEach { it.x -= speed * dt }

        val px = w * .16f
        val py = ground - h * .105f + playerY
        val runnerRect = RectF(px - 15, py + 2, px + 16, py + 50)

        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val o = iterator.next()
            val obstacleRect = RectF(o.x + 5, ground-o.height+7, o.x+o.width-5, ground)
            if (RectF.intersects(runnerRect, obstacleRect)) {
                finishRun()
                return
            }
            if (o.x + o.width < -10) iterator.remove()
        }

        val coinIterator = pickups.iterator()
        while (coinIterator.hasNext()) {
            val p = coinIterator.next()
            if (kotlin.math.hypot((p.x-px).toDouble(), (p.y-(py+24)).toDouble()) < 30) {
                coins++
                prefs.edit().putInt("coins", coins).apply()
                coinIterator.remove()
            } else if (p.x < -20) coinIterator.remove()
        }
    }

    private fun finishRun() {
        state = State.OVER
        val finalScore = score / 5
        if (finalScore > best) {
            best = finalScore
            prefs.edit().putInt("best", best).apply()
        }
        onRunFinished()
        invalidate()
    }

    private fun startRun() {
        score = 0
        frame = 0
        speed = 6f
        playerY = 0f
        velocityY = 0f
        obstacles.clear()
        pickups.clear()
        state = State.RUNNING
        invalidate()
    }

    fun addBonusCoins(amount: Int) {
        coins += amount
        prefs.edit().putInt("coins", coins).apply()
        bonusMessageUntil = System.currentTimeMillis() + 2500
        invalidate()
    }

    fun pauseGame() {
        if (state == State.RUNNING) {
            state = State.MENU
            invalidate()
        }
    }

    fun resumeGame() {
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true

        val x = event.x
        val y = event.y
        val w = width.toFloat()
        val h = height.toFloat()

        when (state) {
            State.MENU -> startRun()
            State.RUNNING -> if (playerY == 0f) {
                velocityY = -14.5f
            }
            State.OVER -> {
                if (y in (h*.69f)..(h*.82f) && x in (w*.32f)..(w*.68f)) {
                    onRewardRequested()
                } else {
                    startRun()
                }
            }
        }

        invalidate()
        return true
    }
}
