package com.nobu.a5ch

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
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

    // Twitter Bearer Token（あなたのトークンに置き換える）
    private val BEARER_TOKEN = "AAAAAAAAAAAAAAAAAAAAAIyu4wEAAAAAWcipDBMtgy0tsuvVf7mRZWObakg%3D6g9ODKJYGm9Lt344LauudiHxTxK7OiWddSPvIkMbBURSSp8uEM"

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
                startCommentFlow()
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

    private fun startCommentFlow() {
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