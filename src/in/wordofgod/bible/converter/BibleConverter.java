/**
 * 
 */
package in.wordofgod.bible.converter;

import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * 
 */
public class BibleConverter {

	public static String bibleSourcePath;
	public static String[] bibleSourcePaths;
	public static String bibleInformationPath;
	public static String outputFormat;
	public static String outputPath;

	/**
	 * @param args
	 * @throws URISyntaxException
	 */
	public static void main(String[] args)
			throws ParserConfigurationException, TransformerException, URISyntaxException {

		if (!validateInput(args)) {
			return;
		}

		if (bibleSourcePath != null) {
			process();
		} else {
			if (bibleSourcePaths != null) {
				for (String bible : bibleSourcePaths) {
					System.out.println("Processing the bible: " + bible);
					bibleSourcePath = bible;
					bibleInformationPath = null;
					process();
				}
			}
		}
	}

	private static void process() throws URISyntaxException {
		// If bible information path is not given, then consider the default path
		if (bibleInformationPath == null) {
			if (bibleSourcePath.endsWith(".ont")) {
				bibleInformationPath = bibleSourcePath.replace(".ont", "-information.ini");
			} else if (bibleSourcePath.endsWith(".ot")) {
				bibleInformationPath = bibleSourcePath.replace(".ot", "-information.ini");
			} else {
				bibleInformationPath = bibleSourcePath.replace(".nt", "-information.ini");
			}
		}

		switch (outputFormat.toUpperCase()) {
		case Constants.FORMAT_TEXTFILES:
			TextFiles.createTextFilesByDirectory();
			break;

		case Constants.FORMAT_SINGLE_TEXTFILE:
			TextFiles.createSingleTextFile();
			break;

		case Constants.FORMAT_TEXTFILES_BY_DIRECTORY:
			TextFiles.createTextFilesByDirectory();
			break;

		case Constants.FORMAT_THEWORDWITHOUTHTMLTAGS:
			TextFiles.createTheWordModuleWithoutHtmlTags();
			break;

		case Constants.FORMAT_JSON:
			Json.createJson();
			break;

		case Constants.FORMAT_NORMALIZETEXT:
			NormalizeText.process();
			break;

		case Constants.FORMAT_CREATELANGUAGESJSON:
			CreateLanguagesJson.process();
			break;

		default:
			System.out.println("Given format is not supported, pls check the supported format below.");
			printHelpMessage();
			break;
		}
	}

	private static boolean validateInput(String[] args) throws URISyntaxException {
		if (args.length < 1) {
			System.out.println("Please give additional details in the expected format..");
			printHelpMessage();
			return false;
		} else {
			outputFormat = args[0];

			if (args[1] != null) {
				if (args[1].contains(",")) {
					bibleSourcePaths = args[1].split(",");
				} else {
					bibleSourcePath = args[1];
				}
			}

			if (args.length > 2 && args[2] != null) {
				bibleInformationPath = args[2];
			}
		}
		return true;
	}

	public static void printHelpMessage() {
		System.out.println("\nHelp on Usage of this program:");
		System.out.println(
				"\nSupported formats:\n\t1. TextFiles\n\t2. TextFilesByDirectory\n\t3. JSON\n\t4. SingleTextFile\n\t5. TheWordWithoutHtmlTags\n\t6. NormalizeText\n\t7. CreateLanguagesJson");
		System.out.println(
				"\nSyntax to run this program:\njava -jar bible-coverter.jar [OUTPUT-FORMAT] [SOURCE-BIBLE-TEXT-FILE-PATH] [BIBLE-INFORMATION-FILE-PATH]");
		System.out.println(
				"\n[BIBLE-INFORMATION-FILE-PATH] is optional, if not given program will consider english book names");
		System.out.println("\nExample 1: java -jar bible-coverter.jar TextFiles C:/taOV.ont C:/taOV-information.ini");
		System.out.println(
				"\nExample 2: java -jar bible-coverter.jar TextFilesByDirectory C:/taOV.ont C:/taOV-information.ini");
		System.out.println(
				"\nExample 3: java -jar bible-coverter.jar CreateLanguagesJson C:/taOV.ont C:/taOV-information.ini");
	}
}