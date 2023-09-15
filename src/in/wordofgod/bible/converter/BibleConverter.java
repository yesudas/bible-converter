/**
 * 
 */
package in.wordofgod.bible.converter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * 
 */
public class BibleConverter {

	public static String bibleSourcePath;
	public static String bibleInformationPath;
	public static String outputFormat;
	public static String outputPath;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws ParserConfigurationException, TransformerException {

		if (!validateInput(args)) {
			return;
		}

		switch (outputFormat.toUpperCase()) {
		case Constants.FORMAT_TEXTFILES:
			TextFiles.createTextFiles(false);
			break;

		case Constants.FORMAT_TEXTFILES_BY_DIRECTORY:
			TextFiles.createTextFiles(true);
			break;

		case Constants.FORMAT_JSON:

			break;

		default:
			System.out.println("Given format is not supported, pls check the supported format below.");
			printHelpMessage();
			break;
		}
	}

	private static boolean validateInput(String[] args) {
		if (args.length < 2) {
			System.out.println("Please give additional details in the expected format..");
			printHelpMessage();
			return false;
		} else {
			outputFormat = args[0];

			if (args[1] != null) {
				bibleSourcePath = args[1];
			}

			if (args.length > 2 && args[2] != null) {
				bibleInformationPath = args[2];
			}

			outputPath = bibleSourcePath != null && bibleSourcePath.lastIndexOf(".") > 0
					? bibleSourcePath.substring(0, bibleSourcePath.lastIndexOf("."))
					: bibleSourcePath;
		}
		return true;
	}

	public static void printHelpMessage() {
		System.out.println("\nHelp on Usage of this program:");
		System.out.println("\nSupported formats:\n\t1. TextFiles\n\t2.TextFilesByDirectory\n\t3.JSON");
		System.out.println(
				"\nSyntax to run this program:\njava -jar bible-coverter.jar [OUTPUT-FORMAT] [SOURCE-BIBLE-TEXT-FILE-PATH] [BIBLE-INFORMATION-FILE-PATH]");
		System.out.println(
				"\n[BIBLE-INFORMATION-FILE-PATH] is optional, if not given program will consider english book names");
		System.out.println(
				"\nExample 1: java -jar bible-coverter.jar TextFiles C:/taOV.ont C:/taOV-information.ini");
		System.out.println(
				"\nExample 2: java -jar bible-coverter.jar TextFilesByDirectory C:/taOV.ont C:/taOV-information.ini");
	}

}
