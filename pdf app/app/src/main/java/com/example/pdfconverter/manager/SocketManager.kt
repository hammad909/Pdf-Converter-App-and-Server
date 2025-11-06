package com.example.pdfconverter.manager

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket

object SocketManager {
    private var socket: Socket? = null

    fun init(serverUrl: String) {
        if (socket != null) {
            socket?.disconnect()
            socket = null
        }

        val opts = IO.Options().apply {
            transports = arrayOf("polling")
            reconnection = true
        }

        socket = IO.socket(serverUrl, opts)

        // Logging events for debugging
        socket?.on("connect_error") { args ->
            Log.e("SocketIO", "Connection error: ${args.joinToString()}")
        }

        socket?.on("connect_timeout") {
            Log.e("SocketIO", "Connection timed out")
        }

        socket?.on("error") { args ->
            Log.e("SocketIO", "General socket error: ${args.joinToString()}")
        }

        socket?.on("disconnect") { args ->
            Log.e("SocketIO", "Disconnected: ${args.joinToString()}")
        }
    }


    fun connect() {
        socket?.connect()
    }

    fun disconnect() {
        socket?.disconnect()
    }


    fun onConnected(callback: () -> Unit) {
        socket?.on(Socket.EVENT_CONNECT) { callback() }
    }


    fun onDisconnected(callback: () -> Unit) {
        socket?.on(Socket.EVENT_DISCONNECT) {
            callback()
        }
    }

    fun onEvent(event: String, callback: (Array<Any>) -> Unit) {
        socket?.on(event) { args ->
            callback(args)
        }
    }

    fun send(event: String, data: Any) {
        socket?.emit(event, data)
    }


}
