package com.example.receiptshare

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class SocketManager(private val serverIp: String, private val serverPort: Int) {
    private var clientSocket: Socket? = null
    private var out: PrintWriter? = null
    private var inBuffer: BufferedReader? = null

    fun connect() {
        try {
            clientSocket = Socket(serverIp, serverPort)
            out = PrintWriter(clientSocket!!.getOutputStream(), true)
            inBuffer = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendMessage(message: String) {
        // Send the length of the message first
        out?.println(message.length)
        Log.d("msglenght", "${message.length}")
        // Then send the message
        out?.println(message)
        Log.d("MessageEncode", message)
        out?.flush()
    }
    fun receiveMessage(): String {
        val response = StringBuilder()
        try {
            var message: String?
            // Read lines from the buffer until null (stream closed)
            while (inBuffer?.readLine().also { message = it } != null) {
                response.append(message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return response.toString()
    }


    fun close() {
        clientSocket?.close()
        out?.close()
        inBuffer?.close()
    }
}
