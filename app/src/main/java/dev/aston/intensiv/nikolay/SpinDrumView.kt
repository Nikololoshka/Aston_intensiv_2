package dev.aston.intensiv.nikolay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.animation.addListener
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.math.min
import kotlin.random.Random

class SpinDrumView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val sectorColors: List<Int> = DrumColors.entries.map { color ->
        Color.parseColor(color.hex)
    }

    private val sectorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sectorAngle = 360f / sectorColors.size
    private var sectorsBoundRect: RectF = RectF()

    private val spinArrowFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        style = Paint.Style.FILL
    }
    private val spinArrowBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }
    private val arrowPath = Path()

    private var animator: ValueAnimator = ValueAnimator().apply {
        duration = DEFAULT_SPIN_DURATION
        interpolator = FastOutSlowInInterpolator()
        addUpdateListener {
            currentSpinAngle = it.animatedValue as Float % 360f
            invalidate()
        }
        addListener(
            onEnd = {
                spinFinished()
            }
        )
    }
    private var currentSpinAngle = -90f
    private var targetSpinAngle = 0f

    private var onSpinResultListener: ((color: DrumColors) -> Unit)? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = paddingLeft + paddingRight + suggestedMinimumWidth
        val w = resolveSize(minWidth, widthMeasureSpec)
        val minHeight = paddingBottom + paddingTop + suggestedMinimumHeight
        val h = resolveSize(minHeight, heightMeasureSpec)

        val centerX = w / 2f
        val centerY = h / 2f
        val radius = min(w, h) * 0.4f
        sectorsBoundRect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        val arrowOffset = radius * 0.1f
        arrowPath.reset()
        arrowPath.moveTo(centerX, centerY - radius + arrowOffset)
        arrowPath.lineTo(centerX - arrowOffset, centerY - radius - arrowOffset / 2)
        arrowPath.lineTo(centerX + arrowOffset, centerY - radius - arrowOffset / 2)
        arrowPath.lineTo(centerX, centerY - radius + arrowOffset)
        arrowPath.close()

        setMeasuredDimension(w, h)
    }

    override fun performClick(): Boolean {
        super.performClick()

        startSpin()

        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                true
            }

            MotionEvent.ACTION_UP -> {
                performClick()
                true
            }

            else -> {
                false
            }
        }
    }

    fun setOnSpinResultListener(listener: (color: DrumColors) -> Unit) {
        onSpinResultListener = listener
    }

    private fun startSpin(
        startPlaytime: Long = 0L,
        targetAngle: Float = 360f + Random.nextInt(360 * 10)
    ) {
        if (!animator.isRunning) {
            targetSpinAngle = targetAngle

            animator.setFloatValues(currentSpinAngle, targetSpinAngle)
            animator.start()
            animator.currentPlayTime = startPlaytime
        }
    }

    private fun currentSpinIndex(): Int {
        return (((360 + 270 - currentSpinAngle) % 360) / sectorAngle).toInt()
    }

    private fun spinFinished() {
        val color = DrumColors.entries[currentSpinIndex()]
        onSpinResultListener?.invoke(color)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawSectors()
        canvas.drawSpinArrow()
    }

    private fun Canvas.drawSectors() {
        for ((index, color) in sectorColors.withIndex()) {
            sectorPaint.color = color
            drawArc(
                sectorsBoundRect,
                currentSpinAngle + sectorAngle * index,
                sectorAngle,
                true,
                sectorPaint
            )
        }
    }

    private fun Canvas.drawSpinArrow() {
        drawPath(arrowPath, spinArrowFillPaint)
        drawPath(arrowPath, spinArrowBorderPaint)
    }

    override fun onSaveInstanceState(): Parcelable {
        animator.pause()

        val state = super.onSaveInstanceState()
        return bundleOf(
            CURRENT_SPIN_ANGLE to currentSpinAngle,
            TARGET_SPIN_ANGLE to targetSpinAngle,
            REMAINING_SPIN_DURATION to animator.currentPlayTime,
            BASE_STATE to state
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val spinAngle = state.getFloat(CURRENT_SPIN_ANGLE)
            val targetAngle = state.getFloat(TARGET_SPIN_ANGLE)
            val remainingSpin = state.getLong(REMAINING_SPIN_DURATION)

            currentSpinAngle = spinAngle
            if (remainingSpin != 0L) {
                startSpin(remainingSpin, targetAngle)
            }

            val baseState = BundleCompat.getParcelable(state, BASE_STATE, Parcelable::class.java)
            super.onRestoreInstanceState(baseState)

        } else {
            super.onRestoreInstanceState(state)
        }
    }

    companion object {
        private const val CURRENT_SPIN_ANGLE = "current_spin_angle"
        private const val TARGET_SPIN_ANGLE = "target_spin_angle"
        private const val REMAINING_SPIN_DURATION = "remaining_spin_duration"
        private const val BASE_STATE = "base_state"
        private const val DEFAULT_SPIN_DURATION = 3000L
    }
}