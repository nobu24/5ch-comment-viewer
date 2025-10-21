package com.nobu.a5ch

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TwitterSearchActivity : AppCompatActivity() {

    private lateinit var commentStreamView: CommentStreamView
    private var allComments = listOf<Comment>()
    private var settings = AppSettings()

    // Twitter Bearer Token
    private val BEARER_TOKEN = "AAAAAAAAAAAAAAAAAAAAAIyu4wEAAAAAKINmHRi6dd%2FrAyIRlJw6462%2F%2BFI%3DOxL2QYYXymtlrvp9cI6WLsKMMvkWizGjgcK39iEJEkF0sByUzu"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val searchLabel = TextView(this).apply {
            text = "ハッシュタグで検索"
            setTextColor(android.graphics.Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
        }
        controlPanel.addView(searchLabel)

        val searchInput = EditText(this).apply {
            hint = "#テレビ番組名 など"
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
        }
        controlPanel.addView(searchInput)

        val searchButton = Button(this).apply {
            text = "ツイート検索"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
            setOnClickListener {
                val query = searchInput.text.toString()
                if (query.isNotEmpty()) {
                    searchTweets(query)
                }
            }
        }
        controlPanel.addView(searchButton)

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

    private fun searchTweets(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val client = TwitterClient(BEARER_TOKEN)
                val comments = client.searchTweets(query, 30)

                withContext(Dispatchers.Main) {
                    allComments = comments
                    android.util.Log.d("TwitterSearch", "${comments.size}件のツイートを取得しました")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    android.util.Log.e("TwitterSearch", "エラー: ${e.message}")
                }
            }
        }
    }

    private fun startCommentFlowWithOverlay() {
        if (allComments.isEmpty()) {
            android.util.Log.w("TwitterSearch", "コメントがありません")
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

        // オプション：アクティビティをバックグラウンドに
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

    override fun onDestroy() {
        super.onDestroy()
        // アクティビティが閉じられてもオーバーレイは続く
    }
}