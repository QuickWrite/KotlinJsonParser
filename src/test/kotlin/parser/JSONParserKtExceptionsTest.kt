package parser

import net.quickwrite.JSONParserException
import net.quickwrite.jsonParse
import org.junit.jupiter.api.Test

class JSONParserKtExceptionsTest {
    @Test
    fun `invalid token at start test`() {
        val inputs = arrayOf("]", "}", ":", ",")

        inputs.forEach {
            org.junit.jupiter.api.assertThrows<JSONParserException> {
                jsonParse(it)
            }
        }
    }

    @Test
    fun `invalid in object test`() {
        val inputs = arrayOf("{,}", "{:}", "{]}")

        inputs.forEach {
            org.junit.jupiter.api.assertThrows<JSONParserException> {
                jsonParse(it)
            }
        }
    }

    @Test
    fun `invalid in array test`() {
        val inputs = arrayOf("[,]", "[:]", "[}]")

        inputs.forEach {
            org.junit.jupiter.api.assertThrows<JSONParserException> {
                jsonParse(it)
            }
        }
    }

    @Test
    fun `missing comma test`() {
        val inputs = arrayOf("{\"name\":42 \"name2\":43}", "[42 43]")

        inputs.forEach {
            org.junit.jupiter.api.assertThrows<JSONParserException> {
                jsonParse(it)
            }
        }
    }

    @Test
    fun `invalid entry test`() {
        val inputs = arrayOf("{42:43}", "{\"test\" \"this\"}", "{\"test\":}")

        inputs.forEach {
            org.junit.jupiter.api.assertThrows<JSONParserException> {
                jsonParse(it)
            }
        }
    }

    @Test
    fun `empty document test`() {
        val input = ""

        org.junit.jupiter.api.assertThrows<JSONParserException> {
            jsonParse(input)
        }
    }
}
