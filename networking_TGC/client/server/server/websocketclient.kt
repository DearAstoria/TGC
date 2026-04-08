package net
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.Json
import okhttp3.*
import okio.ByteString
import protocol.ClientMessage
import protocol.ServerMessage
import java.nio.ByteBuffer
class WebSocketClient(
   private val url: String
) : WebSocketListener() {
   private var socket: WebSocket? = null
   private val client = OkHttpClient()
   private val _events = MutableSharedFlow<ServerMessage>()
   val events: SharedFlow<ServerMessage> = _events
   private val _binaryMoves = MutableSharedFlow<ServerMessage.CardMoved>()
   val binaryMoves: SharedFlow<ServerMessage.CardMoved> = _binaryMoves
   private val json = Json { ignoreUnknownKeys = true }
   fun connect() {
       val request = Request.Builder().url(url).build()
       socket = client.newWebSocket(request, this)
   }
   fun disconnect() {
       socket?.close(1000, "Client closed")
   }
   fun sendJson(msg: ClientMessage) {
       val text = json.encodeToString(ClientMessage.serializer(), msg)
       socket?.send(text)
   }
   fun sendBinaryMove(cardId: Int, x: Float, y: Float, z: Int) {
       val bytes = encodeBinaryMove(cardId, x, y, z)
       socket?.send(ByteString.of(*bytes))
   }
   override fun onMessage(webSocket: WebSocket, text: String) {
       val msg = json.decodeFromString(ServerMessage.serializer(), text)
       CoroutineScope(Dispatchers.IO).launch {
           _events.emit(msg)
       }
   }
   override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
       val arr = bytes.toByteArray()
       if (arr.isNotEmpty() && arr[0] == 0x01.toByte()) {
           val move = decodeBinaryMove(arr)
           CoroutineScope(Dispatchers.IO).launch {
               _binaryMoves.emit(move)
           }
       }
   }
   private fun encodeBinaryMove(cardId: Int, x: Float, y: Float, z: Int): ByteArray {
       val buffer = ByteArray(14)
       buffer[0] = 0x01
       ByteBuffer.wrap(buffer).apply {
           position(1)
           putInt(cardId)
           putFloat(x)
           putFloat(y)
           put(z.toByte())
       }
       return buffer
   }
   private fun decodeBinaryMove(bytes: ByteArray): ServerMessage.CardMoved {
       val buffer = ByteBuffer.wrap(bytes)
       buffer.position(1)
       val cardId = buffer.int
       val x = buffer.float
       val y = buffer.float
       val z = buffer.get().toInt()
       return ServerMessage.CardMoved(cardId, x, y, z, playerId = -1)
   }
}