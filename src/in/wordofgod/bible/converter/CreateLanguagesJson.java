package in.wordofgod.bible.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CreateLanguagesJson {

	public static void process() {
		System.out.println("Languages JSON Creation Started...");
		
		try {
			// Find all *-information.ini files in the directory
			List<BibleInformation> bibleInfoList = readAllBibleInformationFiles();
			
			if (!bibleInfoList.isEmpty()) {
				// Create languages JSON structure
				Map<String, Object> languagesJson = createLanguagesJsonStructure(bibleInfoList);
				
				// Write to languages.json file
				writeLanguagesJsonFile(languagesJson);
				
				System.out.println("Languages JSON Creation Completed...");
				System.out.println("Total Bibles processed: " + bibleInfoList.size());
				System.out.println("Languages JSON saved to: " + BibleConverter.outputPath + "/languages.json");
			} else {
				System.err.println("No *-information.ini files found in directory: " + new File(BibleConverter.bibleInformationPath).getParent());
			}
			
		} catch (IOException e) {
			System.err.println("Error during languages JSON creation: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static List<BibleInformation> readAllBibleInformationFiles() throws IOException {
		File bibleInfoFile = new File(BibleConverter.bibleInformationPath);
		File directory = bibleInfoFile.getParentFile();
		
		if (directory == null) {
			directory = new File(".");
		}
		
		if (!directory.exists() || !directory.isDirectory()) {
			System.err.println("Directory not found: " + directory.getAbsolutePath());
			return new ArrayList<>();
		}
		
		List<BibleInformation> bibleInfoList = new ArrayList<>();
		
		// Find all *-information.ini files
		File[] files = directory.listFiles((dir, name) -> 
			name.toLowerCase().endsWith("-information.ini"));
		
		if (files == null || files.length == 0) {
			System.err.println("No *-information.ini files found in directory: " + directory.getAbsolutePath());
			return bibleInfoList;
		}
		
		System.out.println("Found " + files.length + " information files:");
		for (File file : files) {
			System.out.println("  - " + file.getName());
			
			BibleInformation bibleInfo = readBibleInformation(file);
			if (bibleInfo != null) {
				bibleInfoList.add(bibleInfo);
			}
		}
		
		// Sort by languageCode first, then by abbr
		Collections.sort(bibleInfoList, new Comparator<BibleInformation>() {
			@Override
			public int compare(BibleInformation b1, BibleInformation b2) {
				int langCodeCompare = b1.languageCode.compareToIgnoreCase(b2.languageCode);
				if (langCodeCompare != 0) {
					return langCodeCompare;
				}
				return b1.abbr.compareToIgnoreCase(b2.abbr);
			}
		});
		
		return bibleInfoList;
	}
	
	private static BibleInformation readBibleInformation(File infoFile) throws IOException {
		BibleInformation bibleInfo = new BibleInformation();
		bibleInfo.fileName = infoFile.getName();
		
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(infoFile), StandardCharsets.UTF_8))) {
			
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				
				// Skip empty lines and comments
				if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) {
					continue;
				}
				
				// Parse key=value pairs
				if (line.contains("=")) {
					String[] parts = line.split("=", 2);
					if (parts.length == 2) {
						String key = parts[0].trim();
						String value = parts[1].trim();
						
						parseKeyValue(bibleInfo, key, value);
					}
				}
			}
		}
		
		// Set defaults if not found
		setDefaults(bibleInfo);
		
		return bibleInfo;
	}
	
	private static void parseKeyValue(BibleInformation bibleInfo, String key, String value) {
		// Handle bible.info. prefixed keys from sample-information.ini
		if (key.startsWith("bible.info.")) {
			key = key.substring("bible.info.".length());
		}
		
		switch (key.toLowerCase()) {
			case "abbr":
				bibleInfo.abbr = value;
				break;
			case "commonname":
				bibleInfo.commonName = value;
				break;
			case "shortname":
				bibleInfo.shortName = value;
				break;
			case "longname":
				bibleInfo.longName = value;
				break;
			case "longenglishname":
				bibleInfo.longEnglishName = value;
				break;
			case "languagecode":
				bibleInfo.languageCode = value;
				break;
			case "publishedyear":
				bibleInfo.publishedYear = value;
				break;
			case "publishedby":
				bibleInfo.publishedBy = value;
				break;
			case "translatedby":
				bibleInfo.translatedBy = value;
				break;
			case "copyright":
				bibleInfo.copyRight = value;
				break;
		}
	}
	
	private static void setDefaults(BibleInformation bibleInfo) {
		if (bibleInfo.abbr == null || bibleInfo.abbr.isEmpty()) {
			// Try to extract abbr from filename
			String fileName = bibleInfo.fileName;
			if (fileName != null && fileName.endsWith("-information.ini")) {
				bibleInfo.abbr = fileName.substring(0, fileName.length() - "-information.ini".length()).toUpperCase();
			} else {
				bibleInfo.abbr = "UNKNOWN";
			}
		}
		if (bibleInfo.commonName == null || bibleInfo.commonName.isEmpty()) {
			bibleInfo.commonName = bibleInfo.longName != null ? bibleInfo.longName : "Unknown Bible";
		}
		if (bibleInfo.languageCode == null || bibleInfo.languageCode.isEmpty()) {
			bibleInfo.languageCode = "unknown";
		}
		
		// Determine language name based on language code
		if (bibleInfo.languageName == null || bibleInfo.languageName.isEmpty()) {
			bibleInfo.languageName = Utils.getLanguageNameFromCode(bibleInfo.languageCode);
		}
	}
	
	private static Map<String, Object> createLanguagesJsonStructure(List<BibleInformation> bibleInfoList) {
		Map<String, Object> root = new LinkedHashMap<>();
		
		// Use TreeMap to maintain sorted order by language name
		Map<String, Map<String, Object>> biblesByLanguage = new TreeMap<>();
		
		// Group bibles by language
		for (BibleInformation bibleInfo : bibleInfoList) {
			String languageName = bibleInfo.languageName;
			
			// Get or create language entry
			Map<String, Object> languageEntry = biblesByLanguage.get(languageName);
			if (languageEntry == null) {
				languageEntry = new LinkedHashMap<>();
				languageEntry.put("languageCode", bibleInfo.languageCode);
				languageEntry.put("languageName", bibleInfo.languageName);
				languageEntry.put("bibles", new ArrayList<Map<String, Object>>());
				biblesByLanguage.put(languageName, languageEntry);
			}
			
			// Create bible entry
			Map<String, Object> bibleEntry = new LinkedHashMap<>();
			bibleEntry.put("abbr", bibleInfo.abbr);
			bibleEntry.put("isDefault", false); // Default to false, user can manually set default
			bibleEntry.put("commonName", bibleInfo.commonName);
			bibleEntry.put("hide", false);
			
			// Add to bibles list
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> bibles = (List<Map<String, Object>>) languageEntry.get("bibles");
			bibles.add(bibleEntry);
		}
		
		// Sort bibles within each language by abbr
		for (Map<String, Object> languageEntry : biblesByLanguage.values()) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> bibles = (List<Map<String, Object>>) languageEntry.get("bibles");
			Collections.sort(bibles, new Comparator<Map<String, Object>>() {
				@Override
				public int compare(Map<String, Object> b1, Map<String, Object> b2) {
					String abbr1 = (String) b1.get("abbr");
					String abbr2 = (String) b2.get("abbr");
					return abbr1.compareToIgnoreCase(abbr2);
				}
			});
		}
		
		// Create metadata
		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("totalLanguages", biblesByLanguage.size());
		
		int totalBibles = 0;
		List<String> supportedLanguages = new ArrayList<>();
		for (Map<String, Object> languageEntry : biblesByLanguage.values()) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> bibles = (List<Map<String, Object>>) languageEntry.get("bibles");
			totalBibles += bibles.size();
			supportedLanguages.add((String) languageEntry.get("languageName"));
		}
		
		metadata.put("totalBibles", totalBibles);
		metadata.put("lastUpdated", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		metadata.put("supportedLanguages", supportedLanguages);
		
		// Build final structure
		root.put("biblesByLanguage", biblesByLanguage);
		root.put("metadata", metadata);
		
		return root;
	}
	
	private static void writeLanguagesJsonFile(Map<String, Object> languagesJson) throws IOException {
		// Ensure output directory exists
		File outputDir = new File(BibleConverter.outputPath);
		outputDir.mkdirs();
		
		File outputFile = new File(outputDir, "languages.json");
		
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
			
			ObjectMapper mapper = new ObjectMapper();
			// Enable pretty print for readable JSON
			mapper.writerWithDefaultPrettyPrinter().writeValue(writer, languagesJson);
		}
	}
	
	// Inner class to hold Bible information
	private static class BibleInformation {
		String fileName;
		String abbr;
		String commonName;
		String shortName;
		String longName;
		String longEnglishName;
		String languageCode;
		String languageName;
		String publishedYear;
		String publishedBy;
		String translatedBy;
		String copyRight;
	}
}
