package net.quickwrite

import net.quickwrite.lexer.JSONPositionData

abstract class JSONParseException(message: String, private val position: JSONPositionData?) : Exception(message) {
    override fun getLocalizedMessage(): String {
        if (position == null) {
            return "error: " + message!!
        }

        val line = position.getLine()
        val lineNumber = position.lineNumber().toString()
        val linePosition = position.linePosition()

        val length = position.getLength()

        return """
            error: $lineNumber:$linePosition $message
            $lineNumber | $line
            ${" ".repeat(lineNumber.length)} | ${" ".repeat(linePosition - 1)}${"^".repeat(length)}
        """.trimIndent()
    }
}

class JSONLexerException(message: String, position: JSONPositionData)
    : JSONParseException(message, position)

class JSONParserException(message: String, position: JSONPositionData?)
    : JSONParseException(message, position)
