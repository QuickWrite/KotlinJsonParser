package parser

import net.quickwrite.jsonParse
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal

class JSONParserKtTest {

    @Test
    fun `null literal test`() {
        val input = "null"

        assertEquals(null, jsonParse(input))
    }

    @Test
    fun `true literal test`() {
        val input = "true"

        assertEquals(true, jsonParse(input))
    }

    @Test
    fun `false literal test`() {
        val input = "false"

        assertEquals(false, jsonParse(input))
    }

    @Test
    fun `number test`() {
        val inputs = arrayOf("1", "42", "42e2", "0.5", "1.42e2")

        inputs.forEach {
            assertEquals(BigDecimal(it), jsonParse(it))
        }
    }

    @Test
    fun `object test`() {
        val input = """
            {
                "hello": "World",
                "foo": "bar",
                "number": 42
            }
        """.trimIndent()

        val result = HashMap<String, Any?>()
        result["hello"] = "World"
        result["foo"] = "bar"
        result["number"] = 42

        assertEquals(result.toString(), jsonParse(input).toString()) // toString as the equals method is not completely implemented
    }

    @Test
    fun `array test`() {
        val input = """
            [1, 2, 3, "foo", "bar", null]
        """.trimIndent()

        val result = ArrayList<Any?>()
        result.add(1)
        result.add(2)
        result.add(3)
        result.add("foo")
        result.add("bar")
        result.add(null)

        assertEquals(result.toString(), jsonParse(input).toString())
    }

    @Test
    fun `nested test`() {
        val input = """
            {
                "test": [1, {
                        "yay": []
                    }
                ]
            }
        """.trimIndent()

        val result = HashMap<String, Any?>();

        val array = ArrayList<Any?>()
        array.add(1)

        val map = HashMap<String, Any?>()
        map["yay"] = ArrayList<Any?>()
        array.add(map)

        result["test"] = array

        assertEquals(result.toString(), jsonParse(input).toString())
    }
}
