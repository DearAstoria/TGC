package vm
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.WebSocketClient
import protocol.ClientMessage
import protocol.ServerMessage
class GameViewModel : ViewModel() {
   private val ws = WebSocketClient("ws://YOUR_SERVER_IP:8080/game")
   private val _players = MutableStateFlow<List<String>>(emptyList())
   val players = _players.asStateFlow()
   private val _roomCode = MutableStateFlow<String?>(null)
   val roomCode = _roomCode.asStateFlow()
   private val _cardMoves = MutableSharedFlow<ServerMessage.CardMoved>()
   val cardMoves = _cardMoves.asSharedFlow()
   private var myPlayerId = -1
   init {
       ws.connect()
       viewModelScope.launch {
           ws.events.collect { msg ->
               when (msg) {
                   is ServerMessage.RoomCreated -> {
                       _roomCode.value = msg.roomCode
                   }
                   is ServerMessage.RoomJoined -> {
                       _roomCode.value = msg.roomCode
                       _players.value = msg.players
                       myPlayerId = msg.yourPlayerId
                   }
                   is ServerMessage.PlayerJoined -> {
                       _players.value = _players.value + msg.playerName
                   }
                   is ServerMessage.CardMoved -> {
                       _cardMoves.emit(msg)
                   }
                   is ServerMessage.ChatBroadcast -> {
                       // Hook into UI chat system
                   }
                   ServerMessage.Pong -> {}
               }
           }
       }
       viewModelScope.launch {
           ws.binaryMoves.collect { move ->
               _cardMoves.emit(move)
           }
       }
   }
   fun hostRoom(name: String) {
       ws.sendJson(ClientMessage.HostRoom(name))
   }
   fun joinRoom(code: String, name: String) {
       ws.sendJson(ClientMessage.JoinRoom(code, name))
   }
   fun moveCard(cardId: Int, x: Float, y: Float, z: Int) {
       ws.sendBinaryMove(cardId, x, y, z)
   }
   fun sendChat(name: String, message: String) {
       ws.sendJson(ClientMessage.Chat(name, message))
   }
}