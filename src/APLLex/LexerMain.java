package APLLex;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import APLLex.LexTokens.Token;

public class LexerMain {

	private static ArrayList<LexTokens> objLexTokens = new ArrayList<LexTokens>();
	
	/*
	 * Method Name : GetToken - Separate the input stream into tokens Parameter
	 * : strInput - Input Stream
	 */
	private static void GetToken(String strInput) {
		String strLexRegExp = "";
		String strLineNumber = strInput.substring(0, strInput.indexOf(' '));
		if (!IsNumber(strLineNumber)) {
			System.err.println("Statement should start with appropriate Line number");
			return;
		}
		strInput = strInput.substring(strInput.indexOf(' ') + 1);

		// Constructing Regular expression
		for (Token Token : Token.values()) {
			strLexRegExp += "|(?<" + Token.name() + ">" + Token.strRegExp + ")";
		}
		strLexRegExp = strLexRegExp.substring(1);

		Pattern lexPattern = Pattern.compile(strLexRegExp, Pattern.CASE_INSENSITIVE);
		// ==== This will match the tokens and add in ArrayList of type LexTokens=====//
		Matcher lexMathcher = lexPattern.matcher(strInput);
		while (lexMathcher.find()) {
			for (Token TokenVar : Token.values()) {
				if (lexMathcher.group(Token.NEWLINE.name()) != null)
				{
					objLexTokens.add(new LexTokens(TokenVar, lexMathcher.group(Token.NEWLINE.name()), strLineNumber));
					break;
				}
				if (lexMathcher.group(Token.WHITESPACE.name()) != null) // IF whitespace found then it will ignore
					continue;
				else if (lexMathcher.group(TokenVar.name()) != null) {
					// If Token is IDENTIFIER and length of lexeme is greater
					// than 32 then it will not consider as lexeme
					if (TokenVar.name() == Token.IDENTIFIER.name() & lexMathcher.group(TokenVar.name()).length() > 32)
						continue;
					objLexTokens.add(new LexTokens(TokenVar, lexMathcher.group(TokenVar.name()), strLineNumber));
					break;
				}
			}
		}
		// =================================== END  ==================================//
	}

	private static boolean IsNumber(String strExp) {
		Pattern pattern = Pattern.compile("[0-9]+");
		Matcher m = pattern.matcher(strExp);
		return m.matches();
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		// ========== Take Input stream from text file =============//
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("InputFile.txt"));
			String line = br.readLine();

			while (line != null) {
				StringBuilder sb = new StringBuilder();
				sb.append(line);
				sb.append(System.lineSeparator());
				GetToken(sb.toString());
				line = br.readLine();
			}

		} finally {
			br.close();
		}
		
		Parser objParser = new Parser(objLexTokens);
		objParser.fnEvaluateProg();
	}
}