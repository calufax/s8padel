package com.calufax.s8padel.data

import com.calufax.s8padel.model.FixtureMode
import com.calufax.s8padel.model.MatchResult
import com.calufax.s8padel.model.Player
import com.calufax.s8padel.model.TournamentState

/**
 * Serializador JSON puro en Kotlin (sin dependencias externas ni stubs de Android SDK),
 * permitiendo persistencia atómica local y tests unitarios completos en JVM.
 */
object TournamentJsonSerializer {

    fun encode(state: TournamentState): String = buildString {
        append("{\n")
        append("  \"version\": 1,\n")
        append("  \"tournamentName\": ").append(quoteJson(state.tournamentName)).append(",\n")
        append("  \"fixtureMode\": ").append(quoteJson(state.fixtureMode.name)).append(",\n")
        append("  \"updatedAtMillis\": ").append(state.updatedAtMillis).append(",\n")

        append("  \"swappedRounds\": [")
        append(state.swappedRounds.sorted().joinToString(", "))
        append("],\n")

        append("  \"players\": [\n")
        val normalizedPlayers = (1..8).map { slot ->
            state.players.firstOrNull { it.slot == slot } ?: Player(slot, "Jugador $slot")
        }
        normalizedPlayers.forEachIndexed { index, player ->
            append("    {\"slot\": ").append(player.slot)
                .append(", \"name\": ").append(quoteJson(player.name)).append("}")
            if (index < normalizedPlayers.lastIndex) append(",")
            append("\n")
        }
        append("  ],\n")

        append("  \"results\": [\n")
        val sortedResults = state.results.entries.sortedBy { it.key }
        sortedResults.forEachIndexed { index, (matchId, res) ->
            append("    {\"matchId\": ").append(matchId)
                .append(", \"teamAGames\": ").append(res.teamAGames)
                .append(", \"teamBGames\": ").append(res.teamBGames)
                .append("}")
            if (index < sortedResults.lastIndex) append(",")
            append("\n")
        }
        append("  ]\n")
        append("}")
    }

    fun decode(json: String): TournamentState {
        val root = MiniJsonParser(json).parseValue() as? Map<*, *>
            ?: return TournamentState()

        val tournamentName = (root["tournamentName"] as? String)
            ?: TournamentState.DEFAULT_TOURNAMENT_NAME

        val fixtureModeRaw = root["fixtureMode"] as? String
        val fixtureMode = FixtureMode.entries.firstOrNull { it.name == fixtureModeRaw }
            ?: FixtureMode.BALANCED_WHIST

        val updatedAtMillis = (root["updatedAtMillis"] as? Number)?.toLong() ?: 0L

        val swappedRounds = if (root.containsKey("swappedRounds")) {
            val rawList = root["swappedRounds"] as? List<*> ?: emptyList<Any?>()
            rawList.mapNotNull { (it as? Number)?.toInt() }
                .filter { it in 1..7 }
                .toSet()
        } else {
            TournamentState.DEFAULT_SWAPPED_ROUNDS
        }

        val parsedPlayersBySlot = mutableMapOf<Int, Player>()
        val rawPlayers = root["players"] as? List<*> ?: emptyList<Any?>()
        for (entry in rawPlayers) {
            val map = entry as? Map<*, *> ?: continue
            val slot = (map["slot"] as? Number)?.toInt() ?: continue
            if (slot !in 1..8) continue
            val name = (map["name"] as? String) ?: "Jugador $slot"
            parsedPlayersBySlot[slot] = Player(slot = slot, name = name)
        }
        val players = (1..8).map { slot ->
            parsedPlayersBySlot[slot] ?: Player(slot = slot, name = "Jugador $slot")
        }

        val results = mutableMapOf<Int, MatchResult>()
        val rawResults = root["results"] as? List<*> ?: emptyList<Any?>()
        for (entry in rawResults) {
            val map = entry as? Map<*, *> ?: continue
            val matchId = (map["matchId"] as? Number)?.toInt() ?: continue
            if (matchId !in 1..14) continue
            val a = (map["teamAGames"] as? Number)?.toInt() ?: continue
            val b = (map["teamBGames"] as? Number)?.toInt() ?: continue
            if (MatchResult.isValid(a, b)) {
                results[matchId] = MatchResult(teamAGames = a, teamBGames = b)
            }
        }

        return TournamentState(
            tournamentName = tournamentName,
            fixtureMode = fixtureMode,
            players = players,
            results = results,
            swappedRounds = swappedRounds,
            updatedAtMillis = updatedAtMillis
        )
    }

    private fun quoteJson(value: String): String = buildString {
        append('"')
        for (ch in value) {
            when (ch) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> {
                    if (ch.code < 0x20) {
                        append("\\u")
                        append(ch.code.toString(16).padStart(4, '0'))
                    } else {
                        append(ch)
                    }
                }
            }
        }
        append('"')
    }

    private class MiniJsonParser(private val src: String) {
        private var pos = 0

        fun parseValue(): Any? {
            skipWhitespace()
            if (pos >= src.length) return null
            return when (val ch = src[pos]) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"' -> parseString()
                't', 'f' -> parseBoolean()
                'n' -> parseNull()
                '-', in '0'..'9' -> parseNumber()
                else -> throw IllegalArgumentException("Carácter JSON inesperado '$ch' en posición $pos")
            }
        }

        private fun parseObject(): Map<String, Any?> {
            expect('{')
            skipWhitespace()
            val map = LinkedHashMap<String, Any?>()
            if (peek() == '}') {
                pos++
                return map
            }
            while (pos < src.length) {
                skipWhitespace()
                val key = parseString()
                skipWhitespace()
                expect(':')
                val value = parseValue()
                map[key] = value
                skipWhitespace()
                when (peek()) {
                    ',' -> pos++
                    '}' -> {
                        pos++
                        return map
                    }
                    else -> throw IllegalArgumentException("Se esperaba ',' o '}' en posición $pos")
                }
            }
            throw IllegalArgumentException("Objeto JSON sin cerrar")
        }

        private fun parseArray(): List<Any?> {
            expect('[')
            skipWhitespace()
            val list = mutableListOf<Any?>()
            if (peek() == ']') {
                pos++
                return list
            }
            while (pos < src.length) {
                list.add(parseValue())
                skipWhitespace()
                when (peek()) {
                    ',' -> pos++
                    ']' -> {
                        pos++
                        return list
                    }
                    else -> throw IllegalArgumentException("Se esperaba ',' o ']' en posición $pos")
                }
            }
            throw IllegalArgumentException("Array JSON sin cerrar")
        }

        private fun parseString(): String {
            expect('"')
            val sb = StringBuilder()
            while (pos < src.length) {
                val ch = src[pos++]
                when (ch) {
                    '"' -> return sb.toString()
                    '\\' -> {
                        if (pos >= src.length) throw IllegalArgumentException("Escape incompleto")
                        when (val esc = src[pos++]) {
                            '"' -> sb.append('"')
                            '\\' -> sb.append('\\')
                            '/' -> sb.append('/')
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\u000C')
                            'n' -> sb.append('\n')
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'u' -> {
                                if (pos + 4 > src.length) {
                                    throw IllegalArgumentException("Unicode escape incompleto")
                                }
                                val hex = src.substring(pos, pos + 4)
                                pos += 4
                                sb.append(hex.toInt(16).toChar())
                            }
                            else -> sb.append(esc)
                        }
                    }
                    else -> sb.append(ch)
                }
            }
            throw IllegalArgumentException("String JSON sin cerrar")
        }

        private fun parseNumber(): Number {
            val start = pos
            if (peek() == '-') pos++
            while (pos < src.length && src[pos] in '0'..'9') pos++
            var isDouble = false
            if (pos < src.length && src[pos] == '.') {
                isDouble = true
                pos++
                while (pos < src.length && src[pos] in '0'..'9') pos++
            }
            if (pos < src.length && (src[pos] == 'e' || src[pos] == 'E')) {
                isDouble = true
                pos++
                if (pos < src.length && (src[pos] == '+' || src[pos] == '-')) pos++
                while (pos < src.length && src[pos] in '0'..'9') pos++
            }
            val token = src.substring(start, pos)
            return if (isDouble) token.toDouble() else token.toLong()
        }

        private fun parseBoolean(): Boolean {
            return if (src.startsWith("true", pos)) {
                pos += 4
                true
            } else if (src.startsWith("false", pos)) {
                pos += 5
                false
            } else {
                throw IllegalArgumentException("Booleano inválido en posición $pos")
            }
        }

        private fun parseNull(): Any? {
            if (src.startsWith("null", pos)) {
                pos += 4
                return null
            }
            throw IllegalArgumentException("Null inválido en posición $pos")
        }

        private fun skipWhitespace() {
            while (pos < src.length && src[pos].isWhitespace()) {
                pos++
            }
        }

        private fun peek(): Char? = if (pos < src.length) src[pos] else null

        private fun expect(expected: Char) {
            if (pos >= src.length || src[pos] != expected) {
                throw IllegalArgumentException("Se esperaba '$expected' en posición $pos")
            }
            pos++
        }
    }
}
