package com.nobu.a5ch

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout

class CommentOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: CommentStreamView? = null
    private var params: WindowManager.LayoutParams? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // CommentStreamView を作成
        overlayView = CommentStreamView(this).apply {
            updateSettings(AppSettings())
        }

        // ウィンドウレイアウトパラメータを設定
        params = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        // ウィンドウに View を追加
        windowManager.addView(overlayView, params)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null) {
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // コメントを追加するメソッド（外部から呼び出し）
    fun addComment(comment: Comment) {
        overlayView?.addComment(comment)
    }

    // コメントをクリアするメソッド
    fun clearComments() {
        overlayView?.clearComments()
    }

    companion object {
        private var instance: CommentOverlayService? = null

        fun getInstance(): CommentOverlayService? = instance

        fun setInstance(service: CommentOverlayService?) {
            instance = service
        }
    }
}