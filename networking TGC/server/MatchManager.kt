package server

import io.ktor.server.websocket.*
import java.util.concurrent.ConcurrentHashMap

object MatchManager {

    private val matches =
        ConcurrentHashMap<String, Match>()

    private val playerMatch =
        ConcurrentHashMap<DefaultWebSocketServerSession, String>()

    fun assignPlayer(session: DefaultWebSocketServerSession): Match {

        val match =
            matches.values.find { it.hasSpace() }
                ?: createMatch()

        match.addPlayer(session)

        playerMatch[session] = match.id

        return match
    }

    fun removePlayer(session: DefaultWebSocketServerSession) {

        val matchId =
            playerMatch.remove(session)
                ?: return

        matches[matchId]?.removePlayer(session)
    }

    fun getMatch(session: DefaultWebSocketServerSession): Match? {

        val matchId =
            playerMatch[session]
                ?: return null

        return matches[matchId]
    }

    private fun createMatch(): Match {

        val match =
            Match()

        matches[match.id] = match

        println("Created match ${match.id}")

        return match
    }
}