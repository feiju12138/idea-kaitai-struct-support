# Kaitai Struct Support

[English](README.md) | 简体中文

Kaitai Struct Support 是一款面向 Kaitai Struct YAML（`.ksy`）文件的 IntelliJ Platform 语言插件。

## 功能

### `.ksy` 编辑支持

- 识别 `.ksy` 文件，并提供专属文件类型和图标。
- 高亮 KSY/YAML 键、内置类型、布尔值、数字、字符串、注释、锚点、别名和标点。
- 根据当前 KSY 结构补全根声明、`meta`、序列字段属性、实例、参数、校验映射和交叉引用等键。
- 补全基本类型、本文件声明的类型与枚举、端序、重复模式、编码、处理器名称、布尔值、表达式符号和字段标识符。
- 支持行注释、引号处理，以及流式 YAML 方括号和花括号匹配。
- 可在 **设置 | 编辑器 | 配色方案 | Kaitai Struct YAML** 中配置语法颜色。
- 提供英文和简体中文界面文本。

编辑器使用无错误提示的扁平 PSI 树。目前不提供完整模式校验、符号跳转、格式化或代码生成。

## Kaitai Struct 参考资料

- [Kaitai Struct 用户指南](https://doc.kaitai.io/user_guide.html)
- [KSY 风格指南](https://doc.kaitai.io/ksy_style_guide.html)
- [官方格式库](https://formats.kaitai.io/)

Kaitai Struct 是 Kaitai Project 的项目。本项目是独立的 IntelliJ Platform 插件，与 Kaitai Project 无关联。

## 许可证

由于内嵌并分发官方 Kaitai Struct Compiler，本插件整体采用 **GNU GPL v3 或更高版本**（`GPL-3.0-or-later`）。编译器版权归 2015-2025 Kaitai Project 所有，同样采用 GPL v3 或更高版本。根据编译器项目的说明，生成的 Java 代码继续遵循用户 KSY 输入本身的许可证。

Kaitai Struct Java Runtime 采用 MIT License。源码树和插件 ZIP 会包含完整的第三方许可证文本与声明。

## 构建

项目要求使用 JDK 21 和 Gradle 9 或更高版本：

```shell
gradle test buildPlugin
```

若要使用已安装的 IDE 构建，可传入其安装目录：

```shell
gradle test buildPlugin -PlocalIdePath=/path/to/IntelliJ-IDEA
```

生成的 ZIP 位于 `build/distributions/`。

## 兼容性

- IntelliJ IDEA 2025.1 或更高版本（build 251+）
- Binary Structure 集成需要 Hex Support 3.0.0 或更高版本
- 从源码构建需要 JDK 21
- Gradle 9 或更高版本
