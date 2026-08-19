# Kaitai Struct Support

English | [Simplified Chinese](README.zh-CN.md)

Kaitai Struct Support is an IntelliJ Platform language plugin for Kaitai Struct YAML (`.ksy`) files.

## Features

### `.ksy` editor support

- Recognizes `.ksy` files with a dedicated file type and icon.
- Highlights KSY/YAML keys, built-in types, booleans, numbers, strings, comments, anchors, aliases, and punctuation.
- Completes keys according to the current KSY section, including root declarations, `meta`, sequence attributes, instances, parameters, validation maps, and cross-references.
- Completes primitive types, locally declared types and enums, endianness, repetition modes, encodings, process names, booleans, expression symbols, and field identifiers.
- Supports line comments, quote handling, and matching flow-style brackets and braces.
- Exposes syntax categories under **Settings | Editor | Color Scheme | Kaitai Struct YAML**.
- Provides English and Simplified Chinese UI text.

The editor uses a flat, error-free PSI tree. It does not currently provide schema validation, symbol navigation, formatting, or code generation.

## Kaitai Struct reference

- [Kaitai Struct user guide](https://doc.kaitai.io/user_guide.html)
- [KSY style guide](https://doc.kaitai.io/ksy_style_guide.html)
- [Official format gallery](https://formats.kaitai.io/)

Kaitai Struct is a project of the Kaitai Project. This project is an independent IntelliJ Platform plugin and is not affiliated with the Kaitai Project.

## License

This plugin is licensed under **GNU GPL v3 or later** (`GPL-3.0-or-later`) because it embeds and distributes the official Kaitai Struct Compiler. The compiler is copyright 2015-2025 Kaitai Project and is distributed under GPL v3 or later. Generated Java code remains governed by the license of the user's KSY input, as documented by the compiler project.

The Kaitai Struct Java Runtime is distributed under the MIT License. Full third-party license texts and notices are included in the source tree and plugin archive.

## Build

The project requires JDK 21 and Gradle 9 or later:

```shell
gradle test buildPlugin
```

To build against an installed IDE, pass its installation directory:

```shell
gradle test buildPlugin -PlocalIdePath=/path/to/IntelliJ-IDEA
```

The resulting ZIP is written to `build/distributions/`.

## Compatibility

- IntelliJ IDEA 2025.1 or later (build 251+)
- Hex Support 3.0.0 or later for Binary Structure integration
- JDK 21 for building from source
- Gradle 9 or later
