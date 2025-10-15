package in.wordofgod.bible.converter;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Test class for NormalizeText functionality
 */
public class NormalizeTextTest {

	public static void main(String[] args) throws URISyntaxException {
		System.out.println("Starting NormalizeText Test...");
		
		// Create test input file
		createTestInputFile();
		
		// Set up BibleConverter paths for testing
		BibleConverter.bibleSourcePath = "/Users/yesudas/WOG/github-projects/bible-converter/test-input.txt";
		BibleConverter.outputPath = "/Users/yesudas/WOG/github-projects/bible-converter/test-output";
		
		// Run the normalization process
		NormalizeText.process();
		
		System.out.println("NormalizeText Test Completed!");
		System.out.println("Check the output file at: " + BibleConverter.outputPath + "/test-input.txt");
	}
	
	private static void createTestInputFile() {
		String testContent = createTestContent();
		
		try (FileWriter writer = new FileWriter("/Users/yesudas/WOG/github-projects/bible-converter/test-input.txt")) {
			writer.write(testContent);
			System.out.println("Test input file created successfully.");
		} catch (IOException e) {
			System.err.println("Error creating test input file: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static String createTestContent() {
		StringBuilder content = new StringBuilder();
		
		// Test case 1: Number commas
		content.append("Test 1 - Number commas:\n");
		content.append("Population was 6,000 people in the city.\n");
		content.append("The total amount was 2,35,000 rupees.\n");
		content.append("Small number like 1,5 should also work.\n\n");
		
		// Test case 2: Fractions
		content.append("Test 2 - Fractions:\n");
		content.append("He walked 5 1/2 miles to reach the destination.\n");
		content.append("The measurement was 3 3/4 inches long.\n");
		content.append("Only 1/2 of the work was completed.\n");
		content.append("About 7 1/2 hours and 2 3/4 days were needed.\n\n");
		
		// Test case 3: HTML entities and tags
		content.append("Test 3 - HTML entities and tags:\n");
		content.append("This is &lt;b&gt;bold&lt;/b&gt; text with &amp; symbol.\n");
		content.append("<p>Paragraph with <em>emphasis</em> and <strong>strong</strong> text.</p>\n");
		content.append("Quote: &quot;Hello World&quot; said the &apos;developer&apos;.\n\n");
		
		// Test case 4: Punctuation spacing
		content.append("Test 4 - Punctuation spacing:\n");
		content.append("அகன்றது.எனவே தமிழ் மொழியில் உள்ளது.\n");
		content.append("God created.And he saw that it was good.\n");
		content.append("First item,second item,third item in the list.\n");
		content.append("அகன்றது,எனவே இது சரியாக இருக்கும்.\n\n");
		
		// Test case 5: Interlude variations
		content.append("Test 5 - Interlude variations:\n");
		content.append("Before <CL>Interlude<CL> after text.\n");
		content.append("Before <CL>Interlude <CL> after text.\n");
		content.append("Before <CL> Interlude <CL> after text.\n");
		content.append("Before <CL> Interlude<CL> after text.\n\n");
		
		// Test case 6: Specific tags removal
		content.append("Test 6 - Specific tags removal:\n");
		content.append("Text with <FR>foreign reference</FR> should be cleaned.\n");
		content.append("Text with <Fr>another foreign reference</Fr> should be cleaned.\n");
		content.append("Some <CL>centered line</CL> in the middle.\n");
		content.append("A <CM>comment</CM> tag here.\n");
		content.append("Page break <pb/> in between text.\n");
		content.append("Jesus said <J>I am the way</J> to his disciples.\n");
		content.append("Footnote <f>This is a footnote with details</f> should be removed.\n");
		content.append("Another <f>short note</f> here.\n\n");
		
		// Test case 7: Note tags
		content.append("Test 7 - Note tags:\n");
		content.append("This is a verse <n>with a note explanation</n> in between.\n");
		content.append("Multiple <n>first note</n> and <n>second note</n> in same line.\n\n");
		
		// Test case 8: Reference tags (case sensitive)
		content.append("Test 8 - Reference tags:\n");
		content.append("Reference <RF>Matthew 5:16</RF> should become parentheses.\n");
		content.append("Another reference <RF>John 3:16<Rf> should work correctly.\n");
		content.append("Mixed case <rf>should not change</rf> but <RF>this should<Rf>.\n\n");
		
		// Test case 9: Multiple spaces
		content.append("Test 9 - Multiple spaces:\n");
		content.append("Text    with     multiple        spaces   should    be   normalized.\n");
		content.append("தமிழ்     மொழியில்      பல       இடைவெளிகள்     உள்ளன.\n\n");
		
		// Test case 10: Control characters (invisible)
		content.append("Test 10 - Control characters:\n");
		content.append("Text with\u0001control\u0002characters\u0003should\u0004be\u0005cleaned.\n");
		content.append("Normal\ttab\rand\ncarriage\nreturn\tshould\tremain.\n\n");
		
		// Test case 11: Non-breaking spaces and other Unicode whitespace
		content.append("Test 11 - Non-breaking spaces:\n");
		content.append("Text with\u00A0non-breaking\u00A0spaces should be normalized.\n");
		content.append("Figure\u2007space and narrow\u202Fno-break space test.\n");
		content.append("Word\u2060joiner and\uFEFFzero-width no-break space test.\n\n");
		
		// Test case 12: WH and WG tags preservation
		content.append("Test 12 - WH and WG tags preservation:\n");
		content.append("Hebrew word <WH123> should be preserved in text.\n");
		content.append("Greek word <WG456> should also be preserved.\n");
		content.append("Multiple tags: <WH1> beginning, <WG999> middle, <WH12345> end.\n");
		content.append("Mixed with HTML: <WH789> word with <b>bold</b> text and <WG101112> another word.\n");
		content.append("Combined test: <p>Paragraph with <WH2468> Hebrew and <WG1357> Greek words</p>.\n\n");
		
		// Test case 13: Complex multilingual content
		content.append("Test 13 - Complex multilingual content:\n");
		content.append("English: God created.And he saw 6,000 people.\n");
		content.append("தமிழ்: கடவுள் உருவாக்கினார்.அவர் 2,35,000 மக்களைப் பார்த்தார்.\n");
		content.append("हिंदी: भगवान ने बनाया.और उन्होंने 1,50,000 लोगों को देखा।\n");
		content.append("ಕನ್ನಡ: ದೇವರು ಸೃಷ್ಟಿಸಿದನು.ಮತ್ತು ಅವನು 3,25,000 ಜನರನ್ನು ನೋಡಿದನು.\n");
		content.append("తెలుగు: దేవుడు సృష్టించాడు.మరియు అతను 4,75,000 మందిని చూశాడు.\n\n");
		
		// Test case 14: Combined complex case with WH/WG tags
		content.append("Test 14 - Combined complex case with WH/WG tags:\n");
		content.append("In the beginning <n>approximately 4,000 years ago</n> God <WH430> created <f>see Genesis 1:1</f> the heavens.And he made 1,44,000 stars <RF>Revelation 7:4<Rf> to shine <CL>Interlude <CL> brightly <J>with great power</J>.The measurement was 5 1/2 cubits    and    the    time    was    3 3/4 hours with Greek word <WG2316> for God.\n\n");
		
		// Test case 15: Edge cases for WH/WG tags
		content.append("Test 15 - Edge cases for WH/WG tags:\n");
		content.append("Single digit: <WH1> and <WG2> should work.\n");
		content.append("Large numbers: <WH99999> and <WG123456789> should be preserved.\n");
		content.append("Adjacent tags: <WH123><WG456> without spaces.\n");
		content.append("With punctuation: <WH789>, <WG101>. <WH202>! <WG303>?\n");
		content.append("Case sensitivity: <wh123> and <wg456> should NOT be preserved (lowercase).\n");
		content.append("Partial matches: <WHabc> and <WG> should NOT be preserved.\n\n");
		
		return content.toString();
	}
}
