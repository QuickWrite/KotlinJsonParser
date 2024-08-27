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

/**
 * The interface for specific position data for error handling.
 * It allows the exceptions to have more information on where
 * the error was and what happened.
 *
 * This exists so that the tokens themselves do not have to
 * store this information, as it is very expensive to calculate,
 * and if everything is fine, it does not have to be used,
 * and with that is completely useless.
 */
interface JSONPositionData {
    /**
     * Returns the exact line number where the error happened.
     * It **must** be `1`-Based as this is a number that is
     * for the user and not the computer.
     *
     * So for example if the error happened in this document:
     * ```json
     * {
     *     "test": 42,
     *     "me": error
     * }
     * ```
     * then the function should return the value `3` as the error is
     * in the third line of the document.
     */
    fun lineNumber(): Int

    /**
     * Returns the starting position of the error in the line.
     * It **must** be `1`-based as this is a number that
     * for the user and not the computer.
     *
     * So for example if the error happened in this document:
     * ```json
     * {
     *     "test": 42,
     *     "me": error
     * }
     * ```
     * then the function should return the value `11` as the error
     * is starting at the eleventh character on that line
     * (whitespace is being included).
     */
    fun linePosition(): Int

    /**
     * Returns the length of the error that happened.
     *
     * If it is just one character that was the erroneous character
     * then the value would be `1`.
     * So if the document would be `{:}` then the `:` would throw
     * an error that is one character long.
     *
     * And if the erroneous token is longer then the length will
     * be according to the length of the token:
     *
     * Example document:
     * ```json
     * {
     *     "test": 42,
     *     "me": truw
     * }
     * ```
     * In this case the returned value would be `4` as the
     * literal `truw` is four characters long.
     */
    fun getLength(): Int

    /**
     * Returns the entire line in string form.
     *
     * So for example if the error happened in this document:
     * ```json
     * {
     *     "test": 42,
     *     "me": error
     * }
     * ```
     * then the return value would be `    "me": error`
     * (with the whitespace at the beginning).
     */
    fun getLine(): String
}
