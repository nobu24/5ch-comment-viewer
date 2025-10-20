package com.nobu.a5ch

import android.content.Context
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.TextView
import android.util.AttributeSet
import android.graphics.Color

class CommentStreamView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val commentViews = mutableListOf<TextView>()
    private var settings = AppSettings()
    private var nextY = 80f

    init {
        setBackgroundColor(Color.BLACK)
    }

    fun addComment(comment: Comment) {
        val textView = TextView(context).apply {
            text = "${comment.name}: ${comment.body}"
            textSize = settings.fontSize
            setTextColor(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = nextY.toInt()
            }
        }

        addView(textView)
        commentViews.add(textView)

        // コメント用のアニメーション
        val animation = TranslateAnimation(
            width.toFloat(),      // fromXDelta
            -500f,                // toXDelta
            0f,                   // fromYDelta
            0f                    // toYDelta
        ).apply {
            duration = 6000       // 6秒かけてスクロール
            fillAfter = false
            setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    removeView(textView)
                    commentViews.remove(textView)

                    // Y座標をリセット
                    if (commentViews.isEmpty()) {
                        nextY = 80f
                    }
                }
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })
        }

        textView.startAnimation(animation)

        // 次のコメント用にY座標を調整
        nextY += 60f
        if (nextY > height - 100) {
            nextY = 80f
        }

        // 最大表示数を制限
        if (commentViews.size > settings.maxDisplayComments) {
            val oldView = commentViews.removeAt(0)
            removeView(oldView)
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        settings = newSettings
    }

    fun clearComments() {
        removeAllViews()
        commentViews.clear()
        nextY = 80f
    }
}