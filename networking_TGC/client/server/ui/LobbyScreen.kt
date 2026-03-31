package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.RoomClient
import protocol.ServerMessage

@Composable
fun LobbyScreen(
    serverUrl: String, // "ws://100.101.102.103:8080/game" (Tailscale IP)
    openTailscale: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val client = remember { RoomClient(scope, serverUrl) }

    var playerName by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Disconnected") }

    LaunchedEffect(Unit) {
        client.connect()
        status = "Connecting..."

        scope.launch {
            client.serverMessages.collect { msg ->
                when (msg) {
                    is ServerMessage.RoomCreated ->
                        status = "Room created: ${msg.roomCode}"

                    is ServerMessage.RoomJoined ->
                        status = "Joined room ${msg.roomCode} with players: ${msg.players.joinToString()}"

                    is ServerMessage.RoomError ->
                        status = "Room error: ${msg.message}"

                    is ServerMessage.Error ->
                        status = "Error: ${msg.message}"

                    else -> Unit
                }
            }
        }
    }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("Player name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = roomCode,
            onValueChange = { roomCode = it },
            label = { Text("Room code") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Row {
            Button(onClick = {
                scope.launch { client.hostRoom(playerName) }
            }) {
                Text("Host room")
            }

            Spacer(Modifier.width(12.dp))

            Button(onClick = {
                scope.launch { client.joinRoom(roomCode, playerName) }
            }) {
                Text("Join room")
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = openTailscale) {
            Text("Open Tailscale")
        }

        Spacer(Modifier.height(16.dp))
        Text("Status: $status")
    }
}
