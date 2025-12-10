package com.example.myhomepage.network

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WebSocketManager private constructor() {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _receivedMessages = MutableStateFlow<String?>(null)
    val receivedMessages: StateFlow<String?> = _receivedMessages.asStateFlow()

    enum class ConnectionState {
        Disconnected,
        Connecting,
        Connected,
        Error
    }

    fun connect(userId: String) {
        if (webSocket != null && _connectionState.value == ConnectionState.Connected) {
            Log.d(TAG, "WebSocket already connected")
            return
        }

        _connectionState.value = ConnectionState.Connecting

        // WebSocket服务器地址配置说明：
        // - Android模拟器：使用 10.0.2.2 访问PC的localhost（这是Android模拟器的特殊映射地址）
        // - 真机：需要替换为PC的实际IP地址（如：ws://192.168.x.x:8081/ws/$userId）
        //   注意：真机需要确保PC和手机在同一局域网，并且PC防火墙允许8081端口
        val serverUrl = "ws://10.0.2.2:8081/ws/$userId"
        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected for user: $userId")
                _connectionState.value = ConnectionState.Connected
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received message: $text")
                _receivedMessages.value = text
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code $reason")
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code $reason")
                _connectionState.value = ConnectionState.Disconnected
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure", t)
                _connectionState.value = ConnectionState.Error
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Manual disconnect")
        webSocket = null
        _connectionState.value = ConnectionState.Disconnected
    }

    fun sendMessage(message: String): Boolean {
        val ws = webSocket
        return if (ws != null && _connectionState.value == ConnectionState.Connected) {
            ws.send(message)
            true
        } else {
            Log.w(TAG, "Cannot send message: WebSocket not connected")
            false
        }
    }

    companion object {
        private const val TAG = "WebSocketManager"
        @Volatile
        private var INSTANCE: WebSocketManager? = null

        fun getInstance(): WebSocketManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WebSocketManager().also { INSTANCE = it }
            }
        }
    }
}

