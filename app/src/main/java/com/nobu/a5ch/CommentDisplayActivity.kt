package com.nobu.a5ch

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentDisplayActivity : AppCompatActivity() {

    private lateinit var commentStreamView: CommentStreamView
    private var settings = AppSettings()
    private var allComments = listOf<Comment>()
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val threadUrl = intent.getStringExtra("thread_url") ?: ""
        val threadTitle = intent.getStringExtra("thread_title") ?: "スレッド"

        val mainLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        commentStreamView = CommentStreamView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            updateSettings(settings)
        }
        mainLayout.addView(commentStreamView)

        val controlPanel = LinearLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                350
            ).apply {
                gravity = android.view.Gravity.BOTTOM
            }
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.argb(220, 30, 30, 30))
            setPadding(16, 16, 16, 16)
        }

        val titleView = TextView(this).apply {
            text = threadTitle
            textSize = 14f
            setTextColor(android.graphics.Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
        }
        controlPanel.addView(titleView)

        val loadButton = Button(this).apply {
            text = "コメント読み込み"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
            setOnClickListener {
                loadCommentsFromUrl(threadUrl)
            }
        }
        controlPanel.addView(loadButton)

        val startButton = Button(this).apply {
            text = "コメント自動流し開始"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
            setOnClickListener {
                startAutoScroll()
            }
        }
        controlPanel.addView(startButton)

        val infoText = TextView(this).apply {
            text = "まず「読み込み」を押してからスタートをタップしてください"
            textSize = 12f
            setTextColor(android.graphics.Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        controlPanel.addView(infoText)

        mainLayout.addView(controlPanel)
        setContentView(mainLayout)
    }

    private fun loadCommentsFromUrl(threadUrl: String) {
        isLoading = true

        lifecycleScope.launch(Dispatchers.Main) {
            // ダミーコメントを使用
            allComments = DummyData.getComments("1001")
            android.util.Log.d("CommentDisplay", "${allComments.size}件のコメントを取得しました")
            isLoading = false
        }
    }

    private fun startAutoScroll() {
        if (allComments.isEmpty()) {
            return
        }

        commentStreamView.clearComments()

        Thread {
            try {
                for (comment in allComments) {
                    runOnUiThread {
                        commentStreamView.addComment(comment)
                    }
                    java.lang.Thread.sleep(500L)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}