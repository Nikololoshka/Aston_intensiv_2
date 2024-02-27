package dev.aston.intensiv.nikolay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf

class ColorfulTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    @ColorInt
    private var color: Int = Color.BLACK

    private var text: String = ""

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 32f
    }
    private var textLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, 0)
        .build()

    fun setColor(color: DrumColors) {
        setColor(Color.parseColor(color.hex))
    }

    fun setColor(@ColorInt color: Int) {
        this.color = color
        textPaint.color = color
        invalidate()
    }

    fun setText(text: String) {
        this.text = text

        val textWidth = textPaint.measureText(text).toInt()
        textLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, textWidth)
            .build()

        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = paddingLeft + paddingRight + suggestedMinimumWidth + textLayout.width
        val w = resolveSize(minWidth, widthMeasureSpec)

        val minHeight = paddingBottom + paddingTop + suggestedMinimumHeight + textLayout.height
        val h = resolveSize(minHeight, heightMeasureSpec)

        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        textLayout.draw(canvas)
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = super.onSaveInstanceState()
        return bundleOf(
            TEXT_VALUE to text,
            COLOR_VALUE to color,
            BASE_STATE to state
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            setText(state.getString(TEXT_VALUE, ""))
            setColor(state.getInt(COLOR_VALUE, Color.BLACK))

            val baseState = BundleCompat.getParcelable(state, BASE_STATE, Parcelable::class.java)
            super.onRestoreInstanceState(baseState)

        } else {
            super.onRestoreInstanceState(state)
        }
    }

    companion object {
        private const val TEXT_VALUE = "text_value"
        private const val COLOR_VALUE = "color_value"
        private const val BASE_STATE = "base_state"
    }
}