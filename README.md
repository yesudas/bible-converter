# bible-converter
Converts given bible text in TheWord format to other formats

# How to use?
1. Download & Install JRE or Java from https://www.oracle.com/in/java/technologies/downloads/
2. Syntax to run this program:

~~~java -jar bible-coverter.jar [OUTPUT-FORMAT] [SOURCE-BIBLE-TEXT-FILE-PATH] [BIBLE-INFORMATION-FILE-PATH]
~~~
[BIBLE-INFORMATION-FILE-PATH] is optional, if not given program will consider english book names

Example 1: java -jar bible-coverter.jar TextFiles C:/taOV.ont C:/taOV-information.ini
Example 2: java -jar bible-coverter.jar TextFilesByDirectory C:/taOV.ont C:/taOV-information.ini

# Supported formats:
1. TextFiles
2. TextFilesByDirectory
3. JSON
