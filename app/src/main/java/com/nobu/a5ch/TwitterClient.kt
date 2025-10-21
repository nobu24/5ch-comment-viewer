package com.nobu.a5ch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class TwitterClient(
    private val bearerToken: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun searchTweets(query: String, maxResults: Int = 50): List<Comment> {
        return withContext(Dispatchers.IO) {
            try {
                // URL エンコーディング
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                val url = "https://api.twitter.com/2/tweets/search/recent?" +
                        "query=$encodedQuery&" +
                        "max_results=$maxResults&" +
                        "tweet.fields=created_at,author_id&" +
                        "expansions=author_id&" +
                        "user.fields=username"

                android.util.Log.d("TwitterClient", "リクエスト URL: $url")
                android.util.Log.d("TwitterClient", "Bearer Token 長: ${bearerToken.length}")

                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $bearerToken")
                    .build()

                val response = client.newCall(request).execute()

                android.util.Log.d("TwitterClient", "ステータスコード: ${response.code}")

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "No error body"
                    android.util.Log.e("TwitterClient", "API Error: ${response.code} - ${response.message}")
                    android.util.Log.e("TwitterClient", "Error Body: $errorBody")
                    return@withContext emptyList()
                }

                val responseBody = response.body?.string() ?: return@withContext emptyList()
                android.util.Log.d("TwitterClient", "レスポンス: $responseBody")

                val json = JSONObject(responseBody)

                val comments = mutableListOf<Comment>()
                var number = 1

                // ツイートデータを取得
                if (json.has("data")) {
                    val tweetsArray = json.getJSONArray("data")
                    android.util.Log.d("TwitterClient", "ツイート数: ${tweetsArray.length()}")

                    // ユーザー情報を取得
                    val usersMap = mutableMapOf<String, String>()
                    if (json.has("includes")) {
                        val includes = json.getJSONObject("includes")
                        if (includes.has("users")) {
                            val usersArray = includes.getJSONArray("users")
                            for (i in 0 until usersArray.length()) {
                                val user = usersArray.getJSONObject(i)
                                usersMap[user.getString("id")] = user.getString("username")
                            }
                        }
                    }

                    // ツイートを処理
                    for (i in 0 until tweetsArray.length()) {
                        val tweet = tweetsArray.getJSONObject(i)
                        val tweetText = tweet.getString("text")
                        val authorId = tweet.getString("author_id")
                        val createdAt = tweet.getString("created_at")
                        val username = usersMap[authorId] ?: "Unknown"

                        comments.add(
                            Comment(
                                number = number++,
                                name = username,
                                body = tweetText,
                                date = createdAt.takeLast(5)
                            )
                        )
                    }

                    android.util.Log.d("TwitterClient", "取得コメント数: ${comments.size}")
                } else {
                    android.util.Log.w("TwitterClient", "レスポンスに data フィールドがありません")
                    if (json.has("errors")) {
                        val errors = json.getJSONArray("errors")
                        android.util.Log.e("TwitterClient", "API エラー: $errors")
                    }
                }

                comments
            } catch (e: Exception) {
                android.util.Log.e("TwitterClient", "Exception: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }
}