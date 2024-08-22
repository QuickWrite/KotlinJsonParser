package net.quickwrite

import net.quickwrite.lexer.JSONLexeme
import net.quickwrite.lexer.JSONLexemeType.*
import net.quickwrite.lexer.StringJSONLexer
import java.math.BigDecimal
import java.util.Stack
import kotlin.jvm.Throws

private enum class State {
    START,
    OBJECT,
    ARRAY,
}

@Throws(JSONParseException::class)
fun jsonParse(input: String): Any? {
    val lexer = StringJSONLexer(input)

    var state = State.START

    var token: JSONLexeme

    val stack = Stack<Any?>()

    while (true) {
        token = lexer.getNext()

        if (token.type == EOF) {
            break
        }

        when(state) {
            State.START -> {
                when(token.type) {
                    NULL -> stack.push(null)
                    TRUE -> stack.push(true)
                    FALSE -> stack.push(false)
                    STRING -> stack.push(token.content)
                    NUMBER -> stack.push(BigDecimal(token.content!!))
                    CURLY_OPEN -> {
                        state = State.OBJECT
                        stack.push(HashMap<String, Any?>())
                        continue
                    }
                    SQUARE_OPEN -> {
                        state = State.ARRAY
                        stack.push(ArrayList<Any?>())
                        continue
                    }
                    EOF -> error("Unreachable")
                    else -> throw JSONParserException("Invalid Token", lexer.getPosition(token.position, token.length)) // TODO: Better error reporting
                }
                break
            }
            State.OBJECT -> {
                if(token.type == CURLY_CLOSE) {
                    val jsonObject = stack.pop()

                    if(stack.empty()){
                        stack.push(jsonObject)
                        break
                    }

                    // If this is an object
                    if (stack.peek() is String) {
                        val name = stack.pop() as String

                        @Suppress("UNCHECKED_CAST")
                        (stack.peek() as HashMap<String, Any?>)[name] = jsonObject
                        continue
                    }

                    // Should now be an array
                    @Suppress("UNCHECKED_CAST")
                    (stack.peek() as ArrayList<Any?>).add(jsonObject)

                    state = State.ARRAY
                    continue
                }

                @Suppress("UNCHECKED_CAST")
                val map = stack.peek() as HashMap<String, Any?>

                if(map.isNotEmpty()) {
                    if (token.type != COMMA) {
                        throw JSONParserException("Expected a ',' or a '}', but got ${token.type}", lexer.getPosition(token.position, token.length)) // TODO: Better error reporting
                    }

                    token = lexer.getNext()
                }

                if (token.type != STRING) {
                    throw JSONParserException("Expected an entry or a '}', but got ${token.type}", lexer.getPosition(token.position, token.length)) // TODO: Better error reporting
                }

                val identifier = token.content!!

                token = lexer.getNext()

                if (token.type != COLON) {
                    throw JSONParserException("Expected ':' (COLON), but got ${token.type}", lexer.getPosition(token.position, token.length)) // TODO: Better error reporting
                }

                token = lexer.getNext()

                when(token.type) {
                    NULL -> map[identifier] = null
                    TRUE -> map[identifier] = true
                    FALSE -> map[identifier] = false
                    STRING -> map[identifier] = token.content
                    NUMBER -> map[identifier] = BigDecimal(token.content!!)
                    CURLY_OPEN -> {
                        stack.push(identifier)
                        state = State.OBJECT
                        stack.push(HashMap<String, Any?>())
                        continue
                    }
                    SQUARE_OPEN -> {
                        stack.push(identifier)
                        state = State.ARRAY
                        stack.push(ArrayList<Any?>())
                        continue
                    }
                    EOF -> break
                    COMMA -> throw JSONParserException("Expected a value, but got a ','", lexer.getPosition(token.position))
                    COLON -> throw JSONParserException("Expected a value, but got a ','", lexer.getPosition(token.position))
                    SQUARE_CLOSE -> throw JSONParserException("Object got closed too early. Expected a value, but got '}'", lexer.getPosition(token.position))
                    else -> throw JSONParserException("Invalid Token", lexer.getPosition(token.position, token.length)) // TODO: Better error reporting
                }
            }
            State.ARRAY -> {
                if (token.type == SQUARE_CLOSE) {
                    val array = stack.pop()

                    if(stack.empty()){
                        stack.push(array)
                        break
                    }

                    // If this is an object
                    if (stack.peek() is String) {
                        val name = stack.pop() as String

                        @Suppress("UNCHECKED_CAST")
                        (stack.peek() as HashMap<String, Any?>)[name] = array

                        state = State.OBJECT
                        continue
                    }

                    // Should now be an array
                    @Suppress("UNCHECKED_CAST")
                    (stack.peek() as ArrayList<Any?>).add(array)
                    continue
                }

                @Suppress("UNCHECKED_CAST")
                val array = stack.peek() as ArrayList<Any?>

                if (array.isNotEmpty()) {
                    if (token.type != COMMA) {
                        throw JSONParserException("Expected a ',' or a ']', but got ${token.type}", lexer.getPosition(token.position, token.length)) // TODO: Better error reporting
                    }

                    token = lexer.getNext()
                }

                when(token.type) {
                    NULL -> array.add(null)
                    TRUE -> array.add(true)
                    FALSE -> array.add(false)
                    STRING -> array.add(token.content)
                    NUMBER -> array.add(BigDecimal(token.content!!))
                    CURLY_OPEN -> {
                        state = State.OBJECT
                        stack.push(HashMap<String, Any?>())
                        continue
                    }
                    SQUARE_OPEN -> {
                        state = State.ARRAY
                        stack.push(ArrayList<Any?>())
                        continue
                    }
                    EOF -> break
                    COMMA -> throw JSONParserException("Expected a value, but got a ','", lexer.getPosition(token.position))
                    COLON -> throw JSONParserException("Expected a value, but got a ','", lexer.getPosition(token.position))
                    SQUARE_CLOSE -> throw JSONParserException("List got closed too early. Expected a value, but got ']'", lexer.getPosition(token.position))
                    else -> throw JSONParserException("Invalid Token", lexer.getPosition(token.position, token.length)) // TODO: Better error reporting
                }
            }
        }
    }

    if (stack.empty()) {
        throw JSONParserException("JSON cannot be empty", null)
    }

    token = lexer.getNext()
    // Too much JSON
    if (token.type != EOF) {
        throw JSONParserException("Invalid JSON", lexer.getPosition(token.position, token.length)) // TODO: Better error handling
    }

    val result = stack.pop()

    // Too little JSON
    if (!stack.empty()) {
        throw JSONParserException("JSON ended before it could be completely parsed", null) // TODO: Better error handling
    }

    return result
}
