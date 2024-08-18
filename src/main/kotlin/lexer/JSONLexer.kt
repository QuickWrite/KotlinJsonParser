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

data class JSONLexeme(
    val type: JSONLexemeType,
    val position: Int,
    val length: Int = 1,
    val content: String? = null
)

interface JSONLexer {
    fun getNext(): JSONLexeme

    fun getPosition(position: Int, length: Int = 1): JSONPositionData
}

interface JSONPositionData {
    fun lineNumber(): Int
    fun linePosition(): Int
    fun getLength(): Int
    fun getLine(): String
}
