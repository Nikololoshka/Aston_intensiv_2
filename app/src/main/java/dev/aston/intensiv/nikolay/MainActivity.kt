package dev.aston.intensiv.nikolay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import coil.ImageLoader
import coil.load
import coil.request.CachePolicy
import coil.util.DebugLogger

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textView: ColorfulTextView
    private lateinit var spinDrum: SpinDrumView

    private var lastColorResult: DrumColors? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.image_view)
        textView = findViewById(R.id.text_view)

        val resetResult: Button = findViewById(R.id.reset_result)
        resetResult.setOnClickListener { resetResults() }

        spinDrum = findViewById(R.id.spin_drum)
        spinDrum.setOnSpinResultListener(this::onSpinResult)

        val scrollView: ScrollView = findViewById(R.id.scroll_view)
        val verticalSeekView: VerticalSeekView = findViewById(R.id.seek_view)
        verticalSeekView.addOnChangeListener { _, value, _ ->
            spinDrum.drumFactor = value / 100f
        }
        verticalSeekView.setOnTouchListener { v, event ->
            if(event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                scrollView.requestDisallowInterceptTouchEvent(true)
            }
            if (event.action == MotionEvent.ACTION_UP) {
                v.performClick()
                scrollView.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(LAST_COLOR_RESULT, lastColorResult?.name)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val lastColorResult = savedInstanceState.getString(LAST_COLOR_RESULT)
        if (lastColorResult != null) {
            onSpinResult(
                color = DrumColors.valueOf(lastColorResult),
                isRestoreState = true
            )
        }
    }

    private fun onSpinResult(
        color: DrumColors,
        isRestoreState: Boolean = false
    ) {
        lastColorResult = color

        when (color) {
            DrumColors.Red, DrumColors.Yellow, DrumColors.LightBlue, DrumColors.Purple -> {
                showColorfulText(color)
            }
            else -> {
                showRandomImage(useCache = isRestoreState)
            }
        }
    }

    private fun showColorfulText(color: DrumColors) {
        textView.setColor(color)
        textView.setText(color.hex)

        imageView.hide()
        textView.show()
    }

    private fun showRandomImage(useCache: Boolean = false) {
        imageView.load(
            data = "https://loremflickr.com/640/360",
            imageLoader = ImageLoader.Builder(this)
                .logger(DebugLogger())
                .build()
        ) {
            crossfade(true)
            placeholder(R.drawable.placeholder_loading)
            error(R.drawable.placeholder_error)

            diskCachePolicy(if (useCache) { CachePolicy.READ_ONLY } else { CachePolicy.WRITE_ONLY })
        }
        textView.hide()
        imageView.show()
    }

    private fun resetResults() {
        lastColorResult = null
        textView.hide()
        imageView.hide()
    }

    companion object {
        private const val LAST_COLOR_RESULT = "last_color_result"
    }
}