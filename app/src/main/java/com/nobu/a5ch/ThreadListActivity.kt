package com.nobu.a5ch

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThreadListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val threads = mutableListOf<Thread>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
        }

        recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            layoutManager = LinearLayoutManager(this@ThreadListActivity)
        }
        mainLayout.addView(recyclerView)

        setContentView(mainLayout)

        // ダミーデータを使用（実際の5chスレッド取得は後で実装）
        loadThreads()
    }

    private fun loadThreads() {
        // ダミーデータを表示
        threads.clear()
        threads.addAll(DummyData.getThreads())

        val adapter = ThreadListAdapter(threads) { thread ->
            // スレッドが選択された
            onThreadSelected(thread)
        }

        recyclerView.adapter = adapter
    }

    private fun onThreadSelected(thread: Thread) {
        // CommentDisplayActivity へ遷移してコメント取得
        val intent = Intent(this, CommentDisplayActivity::class.java).apply {
            putExtra("thread_url", thread.url)
            putExtra("thread_title", thread.title)
        }
        startActivity(intent)
    }
}