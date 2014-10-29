GenericsLibLocalizer
====================

Currently under heavy development and subject to large changes.

GenericsLib localization file generator. Parses jar class files for GenericsLib @Localizable annotated string fields to create a language localization key file that should be inserted into the jars resource directory. If the jar properly implements GenericsLib localization, the key file will allow changing hard coded text that is localizable.

In order to change the the localization, the key file should be copied (but not changed in any way) and the copy should be named lang.txt. The text entries in lang.txt can then be changed and inserted into the jars resource directory which will change all the localizable text to the entries in the lang.txt file.

The lang.key.txt file and the lang.txt file follow a format. Each line represents a single entry so line returns in text should be represented with \n, not an actual return.

A line that begins with "version> " (without the quotes) specifies the file version. Multiple versions can be set using comma delimiters or multiple "version> " lines can be inserted. The lang.key.txt file must contain all versions specified by the lang.txt file.

A line that begins with "#" (without the quotes) is a comment and is ignored. Empty lines are also ignored.

A line that begins with a number, a right angle and a space (i.e. "10> ") indicates a localization line. The number indicates an index which is used to match the line with localization lines in the lang.txt file. They should not be changed in the lang.key.txt file. The text after the initial "10> " is the text.

In the lang.txt file, not all localization lines are required. You may only include the lines that are actually changed, so long as the index number remains the same.

Plugins that implement GenericsLib LanguageManager can also merge external language files, though the plugin must provide a file to merge via its own implementation.

Dependencies:
ASM library - ByteCode manipulation and analysis framework
