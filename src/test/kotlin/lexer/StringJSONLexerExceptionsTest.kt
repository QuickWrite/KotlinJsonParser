package lexer

import net.quickwrite.JSONLexerException
import net.quickwrite.lexer.StringJSONLexer
import org.junit.jupiter.api.Test

class StringJSONLexerExceptionsTest {
    @Test
    fun `number exception test`() {
        val inputs = arrayOf("1.", "001", "0e", "1e+")

        inputs.forEach {
            org.junit.jupiter.api.assertThrows<JSONLexerException> {
                StringJSONLexer(it).getNext()
            }
        }
    }

    @Test
    fun `literal exception test`() {
        val inputs = arrayOf("tru", "truew", "fal", "falsew", "nul", "nullw")

        inputs.forEach {
            org.junit.jupiter.api.assertThrows<JSONLexerException> {
                StringJSONLexer(it).getNext()
            }
        }
    }

    @Test
    fun `invalid token exception test`() {
        val inputs = arrayOf("a", "#", "~")

        inputs.forEach {
            org.junit.jupiter.api.assertThrows<JSONLexerException> {
                StringJSONLexer(it).getNext()
            }
        }
    }

    @Test
    fun `string termination exception test`() {
        val input = "\"Test String"

        org.junit.jupiter.api.assertThrows<JSONLexerException> {
            StringJSONLexer(input).getNext()
        }
    }

    @Test
    fun `string escaped termination exception test`() {
        val input = "\"Test String\\"

        org.junit.jupiter.api.assertThrows<JSONLexerException> {
            StringJSONLexer(input).getNext()
        }
    }

    @Test
    fun `string escape exception test`() {
        val inputs = arrayOf("\"\\a\"", "\"\\\n\"", "\"\\\u000C\"")

        inputs.forEach {
            org.junit.jupiter.api.assertThrows<JSONLexerException> {
                StringJSONLexer(it).getNext()
            }
        }
    }

    @Test
    fun `string control characters test`() {
        val inputs = arrayOfNulls<String>(0x001F)

        for (i in inputs.indices) {
            inputs[i] = "\"${i.toChar()}\""
        }

        inputs.forEach {
            org.junit.jupiter.api.assertThrows<JSONLexerException> {
                StringJSONLexer(it!!).getNext()
            }
        }
    }
}
