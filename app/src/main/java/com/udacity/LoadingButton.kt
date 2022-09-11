package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthSize = 0
    private var heightSize = 0
    private var buttonLabel = ""
    private var valueAnimator = ValueAnimator()
    private var animationProgress = 0F
    private var progressCircleRect = RectF()
    private var leftSpace = 0F
    private var topSpace = 0F
    private var buttonTextBounds = Rect()
    private var defaultText = ""
    private var loadingText = ""
    private var bgColor = 0
    private var animColor = 0


    var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { p, old, new ->

        when (new) {
            ButtonState.Clicked -> {
//                progressBarAnimation()
            }
            ButtonState.Loading -> {
                buttonLabel = loadingText
                progressBarAnimation()
                arcAnimation()
            }

            ButtonState.Completed -> {
                buttonLabel = defaultText
            }
        }
    }

    private fun progressBarAnimation() {
        valueAnimator = ValueAnimator.ofFloat(0F, 1F)
        valueAnimator.duration = 3000
        valueAnimator.addUpdateListener { animation ->
            animationProgress = animation.animatedValue as Float
            animation.let {
                if (it.animatedValue == 1F) {
                    buttonState = ButtonState.Completed
                    animationProgress = 0F
                }
            }
            invalidate()
        }
        valueAnimator.start()
    }

    private fun arcAnimation() {
        leftSpace = width / 2 + paint.measureText(buttonLabel) / 2
        paint.getTextBounds(
            buttonLabel,
            0,
            buttonLabel.length,
            buttonTextBounds
        )

        topSpace = buttonTextBounds.height().toFloat()
        progressCircleRect = RectF(
            leftSpace,
            heightSize / 2 - topSpace / 2,
            leftSpace + paint.textSize,
            (heightSize / 2 - topSpace / 2) + paint.textSize
        )
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 50.0f
        color = Color.WHITE
    }

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            bgColor = getColor(R.styleable.LoadingButton_bgColor, 0)
            animColor = getColor(R.styleable.LoadingButton_animColor, 0)
            defaultText = getString(R.styleable.LoadingButton_defaultText)!!
            loadingText = getString(R.styleable.LoadingButton_loadingText)!!
        }
        buttonLabel = defaultText
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //Draw the button background
        paint.color = bgColor
        canvas?.drawRect(0F, 0F, widthSize.toFloat(), heightSize.toFloat(), paint)

        //draw progress bar
        paint.color = animColor
        canvas?.drawRect(0F, 0F, width * animationProgress, heightSize.toFloat(), paint)

        //draw the button label
        paint.color = Color.WHITE
        canvas?.drawText(buttonLabel, widthSize / 2f, heightSize / 2 * 1.2f, paint)

        //draw the Loading Arc
        paint.color = context.getColor(R.color.colorAccent)
        canvas?.drawArc(
            progressCircleRect, 0F, (animationProgress * 360),
            true,
            paint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )

        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    fun setLoadingBTNState(currentState: ButtonState) {
        buttonState = currentState
    }
}