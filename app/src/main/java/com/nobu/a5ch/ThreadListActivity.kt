package com.nobu.a5ch

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ThreadListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
        }

        val titleView = TextView(this).apply {
            text = "5ch実況スレッド一覧"
            textSize = 20f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 16)
            }
        }
        mainLayout.addView(titleView)

        val recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
            ).apply {
                weight = 1f
            }
            layoutManager = LinearLayoutManager(this@ThreadListActivity)
        }
        mainLayout.addView(recyclerView)

        val threads = DummyData.getThreads()
        val adapter = ThreadListAdapter(threads) { thread ->
            val intent = Intent(this, CommentDisplayActivity::class.java).apply {
                putExtra("thread_url", thread.url)
                putExtra("thread_title", thread.title)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        setContentView(mainLayout)
    }
}