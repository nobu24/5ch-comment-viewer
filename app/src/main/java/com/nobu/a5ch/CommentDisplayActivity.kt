package com.nobu.a5ch

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CommentDisplayActivity : AppCompatActivity() {

    private lateinit var commentStreamView: CommentStreamView
    private var allComments = listOf<Comment>()
    private var settings = AppSettings()
    private var threadUrl = ""
    private var threadTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // インテントから情報を取得
        threadUrl = intent.getStringExtra("thread_url") ?: ""
        threadTitle = intent.getStringExtra("thread_title") ?: "スレッド"

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
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.BOTTOM
            }
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.argb(220, 30, 30, 30))
            setPadding(16, 16, 16, 16)
        }

        val titleLabel = TextView(this).apply {
            text = threadTitle
            setTextColor(android.graphics.Color.WHITE)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
        }
        controlPanel.addView(titleLabel)

        val loadButton = Button(this).apply {
            text = "コメント取得"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
            setOnClickListener {
                loadComments()
            }
        }
        controlPanel.addView(loadButton)

        val startButton = Button(this).apply {
            text = "コメント流し開始"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                startCommentFlowWithOverlay()
            }
        }
        controlPanel.addView(startButton)

        mainLayout.addView(controlPanel)
        setContentView(mainLayout)
    }

    private fun loadComments() {
        if (threadUrl.isEmpty()) {
            android.util.Log.e("CommentDisplay", "スレッド URL が設定されていません")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val client = A5chClient()
                val comments = client.getComments(threadUrl)

                withContext(Dispatchers.Main) {
                    allComments = comments
                    android.util.Log.d("CommentDisplay", "${comments.size}件のコメントを取得しました")

                    // 確認用トースト表示（オプション）
                    if (comments.isEmpty()) {
                        android.widget.Toast.makeText(
                            this@CommentDisplayActivity,
                            "コメントが取得できませんでした",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    android.util.Log.e("CommentDisplay", "エラー: ${e.message}")
                }
            }
        }
    }

    private fun startCommentFlowWithOverlay() {
        if (allComments.isEmpty()) {
            android.util.Log.w("CommentDisplay", "コメントがありません")
            return
        }

        // オーバーレイサービスを起動
        startOverlayService()

        // 短い遅延後、コメントを送信開始
        Thread {
            try {
                java.lang.Thread.sleep(500) // Service の起動を待つ
                for (comment in allComments) {
                    val service = CommentOverlayService.getInstance()
                    service?.addComment(comment)
                    java.lang.Thread.sleep(500L)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

        // アクティビティをバックグラウンドに
        moveTaskToBack(true)
    }

    private fun startOverlayService() {
        val intent = Intent(this, CommentOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            @Suppress("DEPRECATION")
            startService(intent)
        }
    }
}