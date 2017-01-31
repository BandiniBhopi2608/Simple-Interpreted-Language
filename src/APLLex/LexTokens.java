package APLLex;

public class LexTokens {

	public static enum Token {
		NEWLINE("\r\n"), 
		CONSTANT("[0-9]+"), 
		COMMENT("//"), 
		SPECIAL("[=|>|<|(|)|*|/|+|\\-|%|,|!||&|[|]|{|}|#]"),
		LITERAL("\"[^\"\\\\]*(\\\\.[^\"\\\\]*)*\""), 
		RESERVED("PRINTLN|PRINT|INTEGER|INPUT|LET|END|GOTO|GOSUB|RET|IF|THEN"), 
		IDENTIFIER("[a-z|A-Z][a-z|A-Z|0-9|'_']*"),
		WHITESPACE("[ \t\f\r\n]+");

		public String strRegExp;

		private Token(String strRegExp) {
			this.strRegExp = strRegExp;
		}
	}

	// ****** Private Variables********//
	private Token Token;
	private String strLexem;
	private String strLineNumber;
	// ************* END **************//

	// **************** CONSTRUCTOR ***********************/
	public LexTokens(Token Token, String strLexem, String strLineNumber) {
		this.Token = Token;
		this.strLexem = strLexem;
		this.strLineNumber = strLineNumber;
	}
	// ********************* End **************************/

	// ************** Public Method To Access private variables **************//
	public String GetLexem() {
		return this.strLexem;
	}

	public String GetToken() {
		return this.Token.name().toString();
	}

	public String GetLineNumber() {
		return this.strLineNumber.toString();
	}
	// ********************************* END *******************************//
}
