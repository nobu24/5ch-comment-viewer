package com.nobu.a5ch

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TwitterClient(
    private val bearerToken: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun searchTweets(query: String, maxResults: Int = 50): List<Comment> {
        return try {
            val url = "https://api.twitter.com/2/tweets/search/recent?query=${query}&max_results=${maxResults}&tweet.fields=created_at,author_id&expansions=author_id&user.fields=username"

            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $bearerToken")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val responseBody = response.body?.string() ?: return emptyList()
            val json = JSONObject(responseBody)

            val comments = mutableListOf<Comment>()
            var number = 1

            // ツイートデータを取得
            if (json.has("data")) {
                val tweetsArray = json.getJSONArray("data")

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
                    val username = usersMap[authorId] ?: "ユーザー"

                    comments.add(
                        Comment(
                            number = number++,
                            name = username,
                            body = tweetText,
                            date = createdAt.takeLast(5)
                        )
                    )
                }
            }

            comments
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}