package lexer

import net.quickwrite.JSONLexerException
import net.quickwrite.lexer.JSONLexeme
import net.quickwrite.lexer.JSONLexemeType
import net.quickwrite.lexer.StringJSONLexer
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class StringJSONLexerTest {
    @Test
    fun `empty String test`() {
        val emptyStrings = mutableListOf("", " ", "\t", "\n", "\r\n")

        emptyStrings.forEach {
            assertEquals(StringJSONLexer(it).getNext().type, JSONLexemeType.EOF)
        }
    }

    @Test
    fun `arbitrary tokens test`() {
        val input = "[]{}:,"
        val expected = mutableListOf(
            JSONLexemeType.SQUARE_OPEN,
            JSONLexemeType.SQUARE_CLOSE,
            JSONLexemeType.CURLY_OPEN,
            JSONLexemeType.CURLY_CLOSE,
            JSONLexemeType.COLON,
            JSONLexemeType.COMMA,
            JSONLexemeType.EOF
        )
        val lexer = StringJSONLexer(input)
        val output: MutableList<JSONLexemeType> = mutableListOf()

        var iterator = 0
        while (true) {
            val element = lexer.getNext()
            output.add(element.type)
            if (element.type == JSONLexemeType.EOF) {
                break
            }
            iterator++

            if (iterator >= 100) break
        }

        assertEquals(expected, output)
    }

    @Test
    fun `numbers test`() {
        val inputs = arrayOf("42", "1.5", "0.512", "1e5", "0e5", "1E5", "123456789")

        inputs.forEach {
            assertEquals(JSONLexeme(JSONLexemeType.NUMBER, 0, it.length, it), StringJSONLexer(it).getNext())

            // Add padding
            assertEquals(JSONLexeme(JSONLexemeType.NUMBER, 2, it.length, it), StringJSONLexer(" \t$it").getNext())
            assertEquals(JSONLexeme(JSONLexemeType.NUMBER, 2, it.length, it), StringJSONLexer(" \t$it\t ").getNext())
        }
    }

    @Test
    fun `simple String test`() {
        val inputs = arrayOf("\"\"", "\"Hello World\"")

        inputs.forEach {
            val lexer = StringJSONLexer(it)
            assertEquals(JSONLexeme(JSONLexemeType.STRING, 0, it.length, it.substring(1, it.length - 1)), lexer.getNext())
            assertEquals(JSONLexemeType.EOF, lexer.getNext().type)

            // Add padding
            assertEquals(JSONLexeme(JSONLexemeType.STRING, 2, it.length, it.substring(1, it.length - 1)), StringJSONLexer(" \t$it").getNext())
            assertEquals(JSONLexeme(JSONLexemeType.STRING, 2, it.length, it.substring(1, it.length - 1)), StringJSONLexer(" \t$it\t ").getNext())
        }
    }

    @Test
    fun `complex String test`() {
        val input = "\"\\\" \\\\ \\/ \\b \\f \\n \\r \\t \\u000A\""

        assertEquals(
            JSONLexeme(JSONLexemeType.STRING, 0, input.length, "\" \\ / \b \u000C \n \r \t \u000A"),
            StringJSONLexer(input).getNext()
        )
    }

    @Test
    fun `true literal test`() {
        val input = "true"

        assertEquals(
            JSONLexemeType.TRUE,
            StringJSONLexer(input).getNext().type
        )
    }

    @Test
    fun `false literal test`() {
        val input = "false"

        assertEquals(
            JSONLexemeType.FALSE,
            StringJSONLexer(input).getNext().type
        )
    }

    @Test
    fun `null literal test`() {
        val input = "null"

        assertEquals(
            JSONLexemeType.NULL,
            StringJSONLexer(input).getNext().type
        )
    }
}
