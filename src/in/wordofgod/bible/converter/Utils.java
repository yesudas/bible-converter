package in.wordofgod.bible.converter;

import java.io.File;
import java.net.URISyntaxException;

public class Utils {

	static String getLanguageNameFromCode(String languageCode) {
		if (languageCode == null) {
			return "Unknown Language";
		}

		switch (languageCode.toLowerCase()) {
		case "ta":
			return "தமிழ்";
		case "en":
			return "English";
		case "kn":
			return "ಕನ್ನಡ";
		case "te":
			return "తెలుగు";
		case "hi":
			return "हिन्दी";
		case "ml":
			return "മലയാളം";
		case "he":
		case "iw":
			return "Hebrew";
		case "grc":
			return "Greek";
		case "el":
			return "Greek";
		case "ar":
			return "Arabic";
		case "la":
			return "Latin";
		default:
			// Log unknown language codes for user to be aware
			System.out.println("Warning: Unknown language code encountered: " + languageCode);
			return "Unknown Language";
		}
	}

	static void setOutputFolder(String languageCode) throws URISyntaxException {
		if (BibleConverter.bibleSourcePath != null) {
			// Get path of the running JAR
			File jarFile = new File(BibleConverter.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			String bibleName = (new File(BibleConverter.bibleSourcePath)).getName();
			bibleName = bibleName.replace(".ont", "").replace(".nt", "").replace(".ot", "");

			String languageName = "";
			if (languageCode == null) {// For Normalizing Text all files together do not want language folder
				languageName = "";
				bibleName = "";
			} else {
				languageName = getLanguageNameFromCode(languageCode) + "/";
			}

			String outputFolder = BibleConverter.outputFormat + "/" + languageName + bibleName;

			// Get parent directory of the JAR
			BibleConverter.outputPath = jarFile.getParentFile().getAbsolutePath() + "/Output/" + outputFolder;
		}
	}

}
