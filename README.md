# KotlinJSONParser

KotlinJSONParser is a simple and lightweight JSON parser written in Kotlin. It allows you to easily parse JSON strings into Kotlin objects.
This is mostly a toy project and with that less useful for normal usage. If you want to use a JSON parser, you should consider JSON parsers like [Jackson](https://github.com/FasterXML/jackson) or [Gson](https://github.com/google/gson).

## Features

- Lightweight and easy to use
- Built with Kotlin
- Simple API

## Getting Started

### Prerequisites

- Kotlin
- Gradle

### Usage

Here's a simple example of how to use KotlinJSONParser:

```kotlin
val jsonString = """{"name": "John", "age": 30}"""
val json = jsonParse(jsonString)

println(json["name"]) // Output: John
println(json["age"])  // Output: 30
```

## License
This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details.
