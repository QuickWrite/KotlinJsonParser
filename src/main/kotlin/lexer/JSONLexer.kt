package net.quickwrite.lexer

import net.quickwrite.JSONLexerException
import kotlin.jvm.Throws

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

/**
 * The lexer interface for a JSON parser.
 *
 * It contains two different methods:
 * - [getNext] - Returns the next token
 * - [getPosition] - Returns the data for a specific position
 *   It should only be used for error handling as it can be
 *   quite expensive to calculate this.
 */
interface JSONLexer {
    /**
     * Returns the next [JSONLexeme]. This lexeme is the next
     * token that can be used by the `jsonParse` function.
     *
     * ## Functionality
     *
     * This function always returns the next token and won't return the same token
     * twice (except if the last token is an `EOF` token). The tokenizer should
     * disregard whitespace.
     *
     * If the token stream has ended an `EOF` (**E**nd **O**f **F**ile) token
     * should be emitted.
     *
     * So if the source would be:
     * ```json
     * {
     *     "test": 42
     * }
     * ```
     * it should be called `6` times and return the tokens:
     * `CURLY_OPEN`, `STRING`, `COLON`, `NUMBER`, `CURLY_CLOSE`, `EOF`.
     *
     * ## Error handling
     * If an error occurs whilst parsing the next token
     * the `getNext` function will throw a [JSONLexerException].
     *
     * If the source code would be:
     * ```json
     * truw
     * ```
     * it should throw a [JSONLexerException] with the correct position
     * data.
     */
    @Throws(JSONLexerException::class)
    fun getNext(): JSONLexeme

    /**
     * Returns the position data for the given integer position.
     *
     * This should be used if there is an error as calculating the
     * lines is a very expensive operation and does not need to be done
     * in a different context.
     *
     * @param position The starting position where the error occurred
     * @param length The length of the erroneous part
     */
    fun getPosition(position: Int, length: Int = 1): JSONPositionData
}

interface JSONPositionData {
    fun lineNumber(): Int
    fun linePosition(): Int
    fun getLength(): Int
    fun getLine(): String
}
