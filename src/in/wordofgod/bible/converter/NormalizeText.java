package in.wordofgod.bible.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;

public class NormalizeText {

	public static void process() throws URISyntaxException {
		System.out.println("Text Normalization Started...");
		
		Utils.setOutputFolder(null);
		
		try {
			File sourceFile = new File(BibleConverter.bibleSourcePath);
			String fileName = sourceFile.getName();
			File outputFile = new File(BibleConverter.outputPath, fileName);
			
			// Ensure output directory exists
			outputFile.getParentFile().mkdirs();
			
			// Read source file in UTF-8 and write normalized text to output file
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(sourceFile), StandardCharsets.UTF_8));
				 BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
				
				String line;
				while ((line = reader.readLine()) != null) {
					String normalizedLine = normalizeText(line);
					writer.write(normalizedLine);
					writer.newLine();
				}
			}
			
			System.out.println("Text Normalization Completed...");
			System.out.println("Normalized file saved to: " + outputFile.getAbsolutePath());
			
		} catch (IOException e) {
			System.err.println("Error during text normalization: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static String normalizeText(String text) {
		if (text == null || text.trim().isEmpty()) {
			return text;
		}
		
		// Apply all rules in sequence
		text = removeNumberCommas(text);
		text = removeUnreadableCharacters(text);
		text = replaceFractions(text);
		text = addSpaceAfterPunctuation(text);
		text = replaceInterlude(text);
		text = removeSpecificTags(text);
		text = replaceNoteTags(text);
		text = replaceReferenceTags(text);
		text = stripTagsAndDecodeEntities(text);
		text = normalizeSpaces(text);
		
		return text;
	}
	
	private static String removeNumberCommas(String text) {
		// Remove all commas from numbers like 6,000 -> 6000 and 1,44,000 -> 144000
		// Simple approach: remove all commas that are between digits
		return text.replaceAll("(\\d),(\\d)", "$1$2")
				   .replaceAll("(\\d),(\\d)", "$1$2")
				   .replaceAll("(\\d),(\\d)", "$1$2")
				   .replaceAll("(\\d),(\\d)", "$1$2")
				   .replaceAll("(\\d),(\\d)", "$1$2");
	}
	
	private static String removeUnreadableCharacters(String text) {
		// Remove control characters and other unreadable characters
		// Keep only printable characters, spaces, tabs, and newlines
		text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
		
		// Remove non-breaking spaces and other problematic Unicode whitespace characters
		text = text.replaceAll("\\u00A0", " "); // Non-breaking space -> regular space
		text = text.replaceAll("\\u2007", " "); // Figure space -> regular space
		text = text.replaceAll("\\u202F", " "); // Narrow no-break space -> regular space
		text = text.replaceAll("\\u2060", "");  // Word joiner -> remove
		text = text.replaceAll("\\uFEFF", "");  // Zero width no-break space (BOM) -> remove
		text = text.replace(" ", ""); // Em space -> remove
		
		return text;
	}
	
	private static String replaceFractions(String text) {
		// Replace "([0-9]+) 1/2" with "\1.5"
		text = text.replaceAll("(\\d+)\\s+1/2", "$1.5");
		
		// Replace "([0-9]+) 3/4" with "\1.75"
		text = text.replaceAll("(\\d+)\\s+3/4", "$1.75");
		
		// Replace " 1/2 " with " 0.5 "
		text = text.replaceAll("\\s+1/2\\s+", " 0.5 ");
		
		return text;
	}
	
	private static String stripTagsAndDecodeEntities(String text) {
		// First, temporarily replace WH and WG tags with placeholders to preserve them
		// These are standalone tags, not HTML-style with closing tags
		String tempText = text.replaceAll("<(WH\\d+)>", "PLACEHOLDER_WH_$1_PLACEHOLDER");
		tempText = tempText.replaceAll("<(WG\\d+)>", "PLACEHOLDER_WG_$1_PLACEHOLDER");
		
		// Use Jsoup to strip HTML tags and decode entities
		tempText = Jsoup.parse(tempText).text();
		
		// Restore the WH and WG tags
		tempText = tempText.replaceAll("PLACEHOLDER_WH_(WH\\d+)_PLACEHOLDER", "<$1>");
		tempText = tempText.replaceAll("PLACEHOLDER_WG_(WG\\d+)_PLACEHOLDER", "<$1>");
		
		return tempText;
	}
	
	private static String addSpaceAfterPunctuation(String text) {
		// Add space after full stop when followed by a word
		text = text.replaceAll("\\.(\\p{L})", ". $1");
		
		// Add space after comma when followed by a word
		text = text.replaceAll(",(\\p{L})", ", $1");
		
		text = text.replaceAll("<W", " <W");
		
		return text;
	}
	
	private static String replaceInterlude(String text) {
		// Replace various forms of "<CL>Interlude<CL>" with "(Interlude)"
		text = text.replaceAll("<CL>\\s*Interlude\\s*<CL>", "(Interlude)");
		return text;
	}
	
	private static String removeSpecificTags(String text) {
		// Remove all occurrences of specific tags
		text = text.replaceAll("<Ts>", ") ");
		text = text.replaceAll("<TS1>", "(");
		text = text.replaceAll("<FR>", " ");
		text = text.replaceAll("<Fr>", " ");
		text = text.replaceAll("<CL>", " ");
		text = text.replaceAll("<CM>", " ");
		text = text.replaceAll("<pb/>", " ");
		text = text.replaceAll("<J>", " ");
		text = text.replaceAll("</J>", " ");
		
		// Remove "<f>XXX</f>" where XXX can be any character or word
		text = text.replaceAll("<f>.*?</f>", "");
		
		text = text.replaceAll("\\(<b>[0-9]+ [0-9]+:.*\\)", "");
		
		return text;
	}
	
	private static String replaceNoteTags(String text) {
		// Replace <n> with "(" and </n> with ")"
		text = text.replaceAll("<n>", "(");
		text = text.replaceAll("</n>", ")");
		
		return text;
	}
	
	private static String replaceReferenceTags(String text) {
		// Case sensitive - replace <RF> with "(" and <Rf> with ")"
		text = text.replaceAll("<RF>", " (");
		text = text.replaceAll("<Rf>", ") ");
		
		return text;
	}
	
	private static String normalizeSpaces(String text) {
		// Remove empty parentheses
		text = text.replaceAll("\\(\\)", "");
		// Replace multiple spaces with single space
		return text.replaceAll("\\s+", " ").trim();
	}
}
