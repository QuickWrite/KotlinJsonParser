package net.quickwrite

abstract class JSONParseException(content: String) : Exception(content)

class JSONLexerException(content: String) : JSONParseException(content)

class JSONParserException(content: String) : JSONParseException(content)
