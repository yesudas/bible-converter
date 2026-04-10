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
			TextFiles.createTextFilesPerBook();
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

		case Constants.FORMAT_MSWORD_BY_BOOKS:
			MSWord.createMSWordByBooks();
			break;

		case Constants.FORMAT_MYBIBLEZONE:
			MyBibleZone.createMyBibleZone();
			break;

		case Constants.FORMAT_HTML:
			HTML.createHTML();
			break;

		case Constants.FORMAT_JSONBIBLE:
			JsonBible.createJsonBible();
			break;

		case Constants.FORMAT_THML:
			ThML.createThML();
			break;

		case Constants.FORMAT_THML_SINGLE:
			ThML.createThMLSingleFile();
			break;

		case Constants.FORMAT_OSIS:
			OSIS.createOSIS();
			break;

		case Constants.FORMAT_ZEFANIAXML:
			ZefaniaXML.createZefaniaXML();
			break;

		case Constants.FORMAT_MYSWORD:
			MySword.createMySword();
			break;

		case Constants.FORMAT_MSEXCEL:
			MSExcel.createMSExcel();
			break;

		case Constants.FORMAT_CSV:
			CSV.createCSV();
			break;

		case Constants.FORMAT_ALL:
			processAll();
			break;

		default:
			System.out.println("Given format is not supported, pls check the supported format below.");
			printHelpMessage();
			break;
		}
	}

	private static void processAll() throws URISyntaxException {
		System.out.println("Exporting bible to ALL formats...");
		TextFiles.createTextFilesPerBook();
		TextFiles.createSingleTextFile();
		TextFiles.createTextFilesByDirectory();
		TextFiles.createTheWordModuleWithoutHtmlTags();
		Json.createJson();
		MSWord.createMSWordByBooks();
		MSExcel.createMSExcel();
		MyBibleZone.createMyBibleZone();
		HTML.createHTML();
		JsonBible.createJsonBible();
		ThML.createThML();
		ThML.createThMLSingleFile();
		OSIS.createOSIS();
		ZefaniaXML.createZefaniaXML();
		MySword.createMySword();
		CSV.createCSV();
		System.out.println("Export to ALL formats completed.");
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
		System.out.println("\nSupported formats:");
		System.out.println("\t 1. TextFiles");
		System.out.println("\t 2. TextFilesByDirectory");
		System.out.println("\t 3. JSON");
		System.out.println("\t 4. SingleTextFile");
		System.out.println("\t 5. TheWordWithoutHtmlTags");
		System.out.println("\t 6. NormalizeText");
		System.out.println("\t 7. CreateLanguagesJson");
		System.out.println("\t 8. MSWordByBooks");
		System.out.println("\t 9. MyBibleZone");
		System.out.println("\t10. HTML");
		System.out.println("\t11. JsonBible");
		System.out.println("\t12. ThML");
		System.out.println("\t13. ThMLSingle");
		System.out.println("\t14. OSIS");
		System.out.println("\t15. ZefaniaXML");
		System.out.println("\t16. MySword");
		System.out.println("\t17. MSExcel");
		System.out.println("\t18. CSV");
		System.out.println("\t19. All");
		System.out.println(
				"\nSyntax to run this program:\njava -jar bible-coverter.jar [OUTPUT-FORMAT] [SOURCE-BIBLE-TEXT-FILE-PATH] [BIBLE-INFORMATION-FILE-PATH]");
		System.out.println(
				"\n[BIBLE-INFORMATION-FILE-PATH] is optional, if not given program will consider english book names");
		System.out.println("\nExample 1: java -jar bible-coverter.jar TextFiles C:/taOV.ont C:/taOV-information.ini");
		System.out.println(
				"\nExample 2: java -jar bible-coverter.jar TextFilesByDirectory C:/taOV.ont C:/taOV-information.ini");
	}
}