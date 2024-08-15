package net.quickwrite.lexer

class StringJSONLexer(private val content: CharSequence) : JSONLexer {
    private var position = 0

    override fun getNext(): JSONLexeme {
        skipWhitespace()

        if(position >= content.length) {
            return JSONLexeme(JSONLexemeType.EOF)
        }

        return when(content.getChar(position)) {
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
            else -> TODO("Not implemented")
        }
    }

    private fun charLexeme(type: JSONLexemeType): JSONLexeme {
        position++
        return JSONLexeme(type)
    }

    private fun parseString(): JSONLexeme {
        position++

        val builder = StringBuilder()

        while (content.getChar(position) != '"' && content.getChar(position) != 0.toChar()) {
            if (content[position] == '\\') {
                position++
                builder.append(when(content.getChar(position)) {
                    '"' -> '"'
                    '\\' -> '\\'
                    '/' -> '/'
                    'b' -> '\b'
                    'f' -> '\u000C'
                    'n' -> '\n'
                    'r' -> '\r'
                    't' -> '\t'
                    'u' -> parseUDigitNumber()
                    else -> TODO()
                })
                position++
                continue
            }

            builder.append(content[position])
            position++
        }

        if (position >= content.length) {
            throw LexerException("String wasn't terminated") // TODO: Better error handling
        }

        return JSONLexeme(JSONLexemeType.STRING, builder.toString())
    }

    private fun parseUDigitNumber(): Char {
        var number = 0

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

            throw LexerException("An invalid hex digit has been provided. Expected 0-9A-Fa-f, but got '$char'") // TODO: Better error handling
        }

        return number.toChar()
    }

    private fun parseNumber(negative: Boolean): JSONLexeme {
        val start = position

        parseFirstPart()

        if (content.getChar(position) == '.') {
            position++
            val start = position
            parseNumber()
            if (start == position)
                throw LexerException("Trailing dot is not allowed") // TODO: Better error handling
        }

        if (content.getChar(position) == 'e' || content.getChar(position) == 'E') {
            position++

            if (content.getChar(position) == '-' || content.getChar(position) == '+')
                position++

            val start = position
            parseNumber()
            if (start == position)
                throw LexerException("Trailing '${content.getChar(position)}' is not allowed") // TODO: Better error handling
        }

        return JSONLexeme(JSONLexemeType.NUMBER, content.substring(if(negative) start - 1 else start, position))
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
                throw LexerException("A number cannot start with a zero") // TODO: Better error handling
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
        while (!(position >= content.length || !Character.isWhitespace(content.getChar(position)))) {
            position++
        }
    }
}

private fun CharSequence.getChar(index: Int): Char {
    if (index >= this.length) {
        return 0.toChar()
    }

    return this[index]
}