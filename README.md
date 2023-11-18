# bible-converter
Converts given bible text in TheWord format to other formats

# Usage
If you clone from Git or download a source zip, you will need a Java JDK 8 or above (tested up to 11) to build. You can use Eclipse or IntelliJ or Visual Code IDEs to build the binary and you will find a suitable distribution .jar file in the project folder.

If you download a parallel-bible-creator.jar file, you will need a Java Runtime Environment 8 or above, available from [https://www.oracle.com/in/java/technologies/downloads/](https://www.oracle.com/in/java/technologies/downloads/).

Just run

~~~
java -jar bible-coverter.jar
~~~

on the command line for usage information.

# Steps to use parallel-bible-creator.jar
1. Download & Install JRE or Java from https://www.oracle.com/in/java/technologies/downloads/
2. Open your command prompt or Terminal and use the below syntax to run the program.
3. Syntax to run this program:

~~~
java -jar bible-coverter.jar [OUTPUT-FORMAT] [SOURCE-BIBLE-TEXT-FILE-PATH] [BIBLE-INFORMATION-FILE-PATH]
~~~
[BIBLE-INFORMATION-FILE-PATH] is optional, if not given program will consider english book names

- Example 1: java -jar bible-coverter.jar TextFiles C:/taOV.ont C:/taOV-information.ini
- Example 2: java -jar bible-coverter.jar TextFilesByDirectory C:/taOV.ont C:/taOV-information.ini

# Supported formats:
1. TextFiles
2. TextFilesByDirectory
3. JSON
4. SingleTextFile
5. TheWordWithoutHtmlTags

# Where to get Bible Databases?
1. Download the Bible Databases along with *-information.ini files from [https://github.com/yesudas/all-bible-databases/tree/main/Bibles/TheWord-Bible-Databases/Tamil](https://github.com/yesudas/all-bible-databases/tree/main/Bibles/TheWord-Bible-Databases/Tamil)
2. Use these bible databases for [SOURCE-BIBLE-TEXT-FILE-PATH]
