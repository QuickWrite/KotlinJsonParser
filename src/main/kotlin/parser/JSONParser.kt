package net.quickwrite.parser

import net.quickwrite.lexer.JSONLexeme
import net.quickwrite.lexer.JSONLexemeType.*
import net.quickwrite.lexer.StringJSONLexer
import java.math.BigDecimal
import java.util.Stack

private enum class State {
    START,
    OBJECT,
    ARRAY,
}

fun jsonParse(input: String): Any? {
    val lexer = StringJSONLexer(input)

    var state = State.START

    var token: JSONLexeme

    var stack = Stack<Any?>()

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
                    else -> throw ParserException("Invalid Token") // TODO: Better error reporting
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

                        (stack.peek() as HashMap<String, Any?>)[name] = jsonObject
                        continue
                    }

                    // Should now be an array
                    (stack.peek() as ArrayList<Any?>).add(jsonObject)

                    state = State.ARRAY
                    continue
                }

                val map = stack.peek() as HashMap<String, Any?>

                if(map.isNotEmpty()) {
                    if (token.type != COMMA) {
                        throw ParserException("Expected a ',' or a '}', but got ${token.type}") // TODO: Better error reporting
                    }

                    token = lexer.getNext()
                }

                if (token.type != STRING) {
                    throw ParserException("Expected an entry or a '}', but got ${token.type}") // TODO: Better error reporting
                }

                val identifier = token.content!!

                token = lexer.getNext()

                if (token.type != COLON) {
                    throw ParserException("Expeced ':' (COLON), but got ${token.type}") // TODO: Better error reporting
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
                    else -> throw ParserException("Invalid Token") // TODO: Better error reporting
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

                        (stack.peek() as HashMap<String, Any?>)[name] = array

                        state = State.OBJECT
                        continue
                    }

                    // Should now be an array
                    (stack.peek() as ArrayList<Any?>).add(array)
                    continue
                }

                val array = stack.peek() as ArrayList<Any?>

                if (array.isNotEmpty()) {
                    if (token.type != COMMA) {
                        throw ParserException("Expected a ',' or a ']', but got ${token.type}") // TODO: Better error reporting
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
                    else -> throw ParserException("Invalid Token") // TODO: Better error reporting
                }
            }
        }
    }

    if (stack.empty()) {
        throw ParserException("JSON cannot be empty")
    }

    // Too much JSON
    if (lexer.getNext().type != EOF) {
        throw ParserException("Invalid JSON") // TODO: Better error handling
    }

    val result = stack.pop()

    // Too little JSON
    if (!stack.empty()) {
        throw ParserException("Invalid JSON") // TODO: Better error handling
    }

    return result
}
