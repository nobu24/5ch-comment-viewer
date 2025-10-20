package com.nobu.a5ch

import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

data class Thread(
    val id: String,
    val title: String,
    val url: String,
    val resCount: Int,
    val momentum: Int
)

data class Comment(
    val number: Int,
    val name: String,
    val body: String,
    val date: String
)

data class AppSettings(
    var fontSize: Float = 16f,
    var fontColor: Int = Color.WHITE,
    var commentSpeed: Float = 1.0f,
    var maxDisplayComments: Int = 10,
    var transparency: Float = 1.0f
)

// ダミースレッドデータを生成
object DummyData {
    fun getThreads(): List<Thread> {
        return listOf(
            Thread("1001", "【速報】今日のアニメ実況スレ",
                "https://sora.5ch.net/test/read.cgi/livetbs/1760909530/l50", 850, 420),
            Thread("1002", "ドラマ感想スレッド",
                "https://sora.5ch.net/test/read.cgi/livetbs/1760909530/l50", 620, 310),
            Thread("1003", "スポーツ中継実況",
                "https://sora.5ch.net/test/read.cgi/livetbs/1760909530/l50", 1200, 600),
            Thread("1004", "ゲーム配信実況",
                "https://sora.5ch.net/test/read.cgi/livetbs/1760909530/l50", 450, 225),
            Thread("1005", "映画鑑賞会スレ",
                "https://sora.5ch.net/test/read.cgi/livetbs/1760909530/l50", 320, 160),
            Thread("1006", "音楽番組実況",
                "https://sora.5ch.net/test/read.cgi/livetbs/1760909530/l50", 780, 390),
            Thread("1007", "ニュース解説スレ",
                "https://sora.5ch.net/test/read.cgi/livetbs/1760909530/l50", 540, 270),
            Thread("1008", "バラエティ実況",
                "https://sora.5ch.net/test/read.cgi/livetbs/1760909530/l50", 920, 460),
        )
    }

    fun getComments(threadId: String): List<Comment> {
        val baseComments = listOf(
            Comment(1, "ユーザー1", "今日も実況楽しみ！", "19:00"),
            Comment(2, "ユーザー2", "このシーン神演技だ", "19:01"),
            Comment(3, "ユーザー3", "wwwwww", "19:02"),
            Comment(4, "ユーザー1", "次はどうなるんだろう", "19:03"),
            Comment(5, "ユーザー4", "感動した", "19:04"),
            Comment(6, "ユーザー2", "この展開は予想できなかった", "19:05"),
            Comment(7, "ユーザー5", "素晴らしい", "19:06"),
            Comment(8, "ユーザー3", "続きが気になる", "19:07"),
            Comment(9, "ユーザー1", "ここが一番好き", "19:08"),
            Comment(10, "ユーザー4", "毎週楽しみにしてます", "19:09"),
        )
        return baseComments
    }
}

// 5ch クライアント
class A5chClient {
    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 7.0) AppleWebKit/537.36"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun getComments(threadUrl: String): List<Comment> {
        return try {
            val request = Request.Builder()
                .url(threadUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val html = response.body?.string() ?: return emptyList()
            val doc = Jsoup.parse(html)

            val comments = mutableListOf<Comment>()
            var number = 1

            // 5chの投稿を解析
            doc.select(".thread").forEach { element ->
                try {
                    val name = element.select(".name").text().takeIf { it.isNotEmpty() } ?: "名無しさん"
                    val body = element.select(".message").text().takeIf { it.isNotEmpty() } ?: ""
                    val date = element.select(".date").text().takeIf { it.isNotEmpty() } ?: ""

                    if (body.isNotEmpty()) {
                        comments.add(
                            Comment(
                                number = number++,
                                name = name,
                                body = body,
                                date = date
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            comments.take(50)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

// ThreadListAdapter
class ThreadListAdapter(
    private val threads: List<Thread>,
    private val onThreadClick: (Thread) -> Unit
) : RecyclerView.Adapter<ThreadListAdapter.ThreadViewHolder>() {

    class ThreadViewHolder(val container: LinearLayout) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreadViewHolder {
        val container = LinearLayout(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 1)
            }
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            setBackgroundColor(Color.WHITE)
            isClickable = true
        }

        val titleView = TextView(parent.context).apply {
            textSize = 16f
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(titleView)

        val infoView = TextView(parent.context).apply {
            textSize = 12f
            setTextColor(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 4
            }
        }
        container.addView(infoView)

        return ThreadViewHolder(container)
    }

    override fun onBindViewHolder(holder: ThreadViewHolder, position: Int) {
        val thread = threads[position]
        val titleView = holder.container.getChildAt(0) as TextView
        val infoView = holder.container.getChildAt(1) as TextView

        titleView.text = thread.title
        infoView.text = "レス: ${thread.resCount} | 勢い: ${thread.momentum}"

        holder.container.setOnClickListener {
            onThreadClick(thread)
        }
    }

    override fun getItemCount() = threads.size
}