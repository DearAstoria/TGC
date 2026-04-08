package protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Represents a binary MoveCard message:
 *
 *  Byte Layout:
 *  ---------------------------------------------------------
 *  | 1 byte | 1 byte       | N bytes     | 4 | 4 | 4 bytes |
 *  ---------------------------------------------------------
 *  | Type   | cardIdLength | cardId UTF8 |  x | y | z floats|
 *  ---------------------------------------------------------
 *
 *  Total = 1 + 1 + cardIdLength + 12 bytes
 */
data class MoveCardBinary(
    val cardId: String,
    val x: Float,
    val y: Float,
    val z: Float
)

object BinaryProtocol {

    private const val TYPE_MOVE_CARD: Byte = 0x01

    /**
     * Encodes a MoveCardBinary message into a compact binary packet.
     */
    fun encodeMoveCard(msg: MoveCardBinary): ByteArray {
        val cardBytes = msg.cardId.toByteArray(Charsets.UTF_8)
        require(cardBytes.size <= 255) { "cardId too long for binary protocol" }

        val totalSize = 1 + 1 + cardBytes.size + 12
        val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.BIG_ENDIAN)

        buffer.put(TYPE_MOVE_CARD)                 // 1 byte
        buffer.put(cardBytes.size.toByte())        // 1 byte
        buffer.put(cardBytes)                      // N bytes
        buffer.putFloat(msg.x)                     // 4 bytes
        buffer.putFloat(msg.y)                     // 4 bytes
        buffer.putFloat(msg.z)                     // 4 bytes

        return buffer.array()
    }

    /**
     * Decodes a MoveCardBinary packet from raw bytes.
     */
    fun decodeMoveCard(bytes: ByteArray): MoveCardBinary {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)

        val type = buffer.get()
        require(type == TYPE_MOVE_CARD) {
            "Invalid message type: expected TYPE_MOVE_CARD"
        }

        val idLen = buffer.get().toInt() and 0xFF
        require(bytes.size >= 1 + 1 + idLen + 12) {
            "Invalid MoveCard packet length"
        }

        val idBytes = ByteArray(idLen)
        buffer.get(idBytes)

        val x = buffer.getFloat()
        val y = buffer.getFloat()
        val z = buffer.getFloat()

        val cardId = idBytes.toString(Charsets.UTF_8)

        return MoveCardBinary(cardId, x, y, z)
    }
}
