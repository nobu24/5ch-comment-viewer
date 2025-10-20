package com.nobu.a5ch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val titleView = TextView(this).apply {
            text = "5ch実況ビューア"
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 32
            }
        }
        mainLayout.addView(titleView)

        val subTitleView = TextView(this).apply {
            text = "コメントソースを選択"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        }
        mainLayout.addView(subTitleView)

        // 5ch ボタン
        val a5chButton = Button(this).apply {
            text = "5ch スレッド"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
            setOnClickListener {
                startActivity(Intent(this@MainActivity, ThreadListActivity::class.java))
            }
        }
        mainLayout.addView(a5chButton)

        // Twitter ボタン
        val twitterButton = Button(this).apply {
            text = "Twitter / X"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
            setOnClickListener {
                startActivity(Intent(this@MainActivity, TwitterSearchActivity::class.java))
            }
        }
        mainLayout.addView(twitterButton)

        // YouTube ボタン（グレーアウト）
        val youtubeButton = Button(this).apply {
            text = "YouTube (準備中)"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
            isEnabled = false
        }
        mainLayout.addView(youtubeButton)

        setContentView(mainLayout)
    }
}

