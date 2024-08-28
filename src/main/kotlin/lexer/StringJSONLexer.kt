package net.quickwrite.lexer

import net.quickwrite.JSONLexerException
import kotlin.jvm.Throws

class StringJSONLexer(private val content: CharSequence) : JSONLexer {
    private var position = 0

    @Throws(JSONLexerException::class)
    override fun getNext(): JSONLexeme {
        skipWhitespace()

        if(position >= content.length) {
            return JSONLexeme(JSONLexemeType.EOF, position)
        }

        return when(val char = content.getChar(position)) {
            '{' -> charLexeme(JSONLexemeType.CURLY_OPEN)
            '}' -> charLexeme(JSONLexemeType.CURLY_CLOSE)
            '[' -> charLexeme(JSONLexemeType.SQUARE_OPEN)
            ']' -> charLexeme(JSONLexemeType.SQUARE_CLOSE)
            ':' -> charLexeme(JSONLexemeType.COLON)
            ',' -> charLexeme(JSONLexemeType.COMMA)
            in '0' .. '9' -> parseNumber(false)
            '-' -> {
                position++
                parseNumber(true)
            }
            '"' -> parseString()
            't' -> parseLiteral("true", JSONLexemeType.TRUE)
            'f' -> parseLiteral("false", JSONLexemeType.FALSE)
            'n' -> parseLiteral("null", JSONLexemeType.NULL)
            else -> throw JSONLexerException("Did not recognize '$char' as a valid token character", getPosition(position))
        }
    }

    private fun parseLiteral(input: String, type: JSONLexemeType): JSONLexeme {
        fun captureRestToken() {
            // Try to capture the rest of the token
            while(!content.getChar(position).isWhitespace() && content.getChar(position) != 0.toChar()) {
                position++
            }
        }

        for (i in input.indices) {
            if(content.getChar(position) != input[i]) {
                val start = position - i
                captureRestToken()

                throw JSONLexerException("Expected '$input', but got malformed input", getPosition(start, position - start))
            }
            position++
        }

        if (content.getChar(position).isLetter()) {
            val start = position - input.length
            captureRestToken()

            throw JSONLexerException("Expected '$input', but got malformed input", getPosition(start, position - start))
        }

        return JSONLexeme(type, position - input.length, input.length)
    }

    private fun charLexeme(type: JSONLexemeType): JSONLexeme {
        position++
        return JSONLexeme(type, position - 1)
    }

    private fun parseString(): JSONLexeme {
        val start = position
        position++

        val builder = StringBuilder()

        while (content.getChar(position) != '"' && content.getChar(position) != 0.toChar()) {
            if (content[position] == '\\') {
                position++
                builder.append(when(val char = content.getChar(position)) {
                    '"' -> '"'
                    '\\' -> '\\'
                    '/' -> '/'
                    'b' -> '\b'
                    'f' -> '\u000C'
                    'n' -> '\n'
                    'r' -> '\r'
                    't' -> '\t'
                    'u' -> parseUDigitNumber()
                    0.toChar() -> throw JSONLexerException("String wasn't correctly terminated", getPosition(content.length - 1))
                    else -> throw JSONLexerException("The character $char cannot be escaped", getPosition(position - 1, 2))
                })
                position++
                continue
            }

            builder.append(content[position])
            position++
        }

        if (position >= content.length) {
            throw JSONLexerException("String wasn't correctly terminated", getPosition(content.length - 1))
        }

        position++

        return JSONLexeme(JSONLexemeType.STRING, start, position - start, builder.toString())
    }

    private fun parseUDigitNumber(): Char {
        var number = 0
        val start = position

        for (i in 3 downTo 0) {
            position++
            val char = content.getChar(position)

            if (char in '0' .. '9') {
                number += (char - 48).code shl (i * 4)

                continue
            }

            if (char in 'A' .. 'F') {
                number += (char - 65 + 10).code shl (i * 4)

                continue
            }

            if (char in 'a' .. 'f') {
                number += (char - 97 + 10).code shl (i * 4)

                continue
            }

            throw JSONLexerException(
                "An invalid hex digit has been provided. Expected 0-9A-Fa-f, but got '$char'",
                getPosition(start - 1, position - start + 1)
            )
        }

        return number.toChar()
    }

    private fun parseNumber(negative: Boolean): JSONLexeme {
        val start = position

        parseFirstPart()

        if (content.getChar(position) == '.') {
            position++
            val pStart = position
            parseNumber()
            if (pStart == position)
                throw JSONLexerException(
                    "Trailing dot is not allowed",
                    getPosition(start,  position - start)
                )
        }

        if (content.getChar(position) == 'e' || content.getChar(position) == 'E') {
            position++

            if (content.getChar(position) == '-' || content.getChar(position) == '+')
                position++

            val pStart = position
            parseNumber()
            if (pStart == position)
                throw JSONLexerException(
                    "Trailing '${content.getChar(position - 1)}' is not allowed",
                    getPosition(start,  position - start)
                )
        }

        return JSONLexeme(
            JSONLexemeType.NUMBER,
            start,
            position - start,
            content.substring(if(negative) start - 1 else start, position)
        )
    }

    private fun parseFirstPart() {
        /**
         * If there is a leading zero, this must be
         * a fraction like 0.5 else it will be an
         * error.
         */
        if (content.getChar(position) == '0') {
            position++

            if (content.getChar(position) in '0' .. '9') {
                throw JSONLexerException("A number cannot start with a zero", getPosition(position - 1, 2))
            }
            return
        }

        parseNumber()
    }

    private fun parseNumber() {
        while (content.getChar(position) in '0' .. '9')
            position++
    }

    private fun skipWhitespace() {
        while (!(position >= content.length || !content.getChar(position).isWhitespace())) {
            position++
        }
    }

    override fun getPosition(position: Int, length: Int): JSONPositionData {
        var pos = position

        while (pos - 1 >= 0 && content[pos - 1] != '\n' && content[pos - 1] != '\r') {
            pos--
        }

        val startLine = pos

        pos = position
        while (pos < content.length && content[pos] != '\n' && content[pos] != '\r') {
            pos++
        }

        class Position : JSONPositionData {
            override fun lineNumber(): Int {
                return content.slice(0..position).lines().size
            }

            override fun linePosition(): Int {
                return position - startLine + 1
            }

            override fun getLength(): Int {
                return length
            }

            override fun getLine(): String {
                return content.substring(startLine, pos)
            }

        }

        return Position()
    }

    private fun CharSequence.getChar(index: Int): Char {
        if (index >= this.length) {
            return 0.toChar()
        }

        return this[index]
    }

    private fun Char.isWhitespace(): Boolean {
        return this == ' ' || this == '\r' || this == '\n' || this == '\t'
    }
}
