package net.quickwrite.lexer

enum class JSONLexemeType
{
    NULL,
    TRUE,
    FALSE,
    STRING,
    NUMBER,
    CURLY_OPEN,
    CURLY_CLOSE,
    SQUARE_OPEN,
    SQUARE_CLOSE,
    COMMA,
    COLON,
    EOF
}

data class JSONLexeme(val type: JSONLexemeType, val content: String? = null)

interface JSONLexer {
    fun getNext(): JSONLexeme
}
