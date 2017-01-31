package APLLex;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

	private class LexTokenPair {
		private String Token;
		private String Lexem;
		private String Error;
	}

	private ArrayList<LexTokens> Tokens = new ArrayList<LexTokens>();
	private ArrayList<SymbolTable> SymTable = new ArrayList<SymbolTable>();

	Scanner sc = new Scanner(System.in);
	int i = 0;

	public Parser(ArrayList<LexTokens> Tokens) {
		this.Tokens = Tokens;
	}

	private String GetLexem(int index) {
		return Tokens.get(index).GetLexem();
	}

	private String GetToken(int index) {
		return Tokens.get(index).GetToken().toUpperCase();
	}

	private SymbolTable GetSymbol(String strName) {
		for (SymbolTable st : SymTable) {
			if (st.GetName().equals(strName)) {
				return st;
			}
		}
		return null;
	}

	public void PrintSymbolTable() {
		try {
			for (SymbolTable st : SymTable) {
				System.out.println("Name : " + st.GetName() + " Type : " + st.GetType() + " Value :" + st.GetValue());
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	public boolean GetBoolExpVal() {
		i++;
		String LHS = "";
		String RHS = "";
		String Operator = "";
		String strLineNum;
		
		String strToken = GetToken(i);
		String strLexem = GetLexem(i);
		Loop1: while (true) {
			strLineNum = Tokens.get(i).GetLineNumber();
			if(strToken == "CONSTANT" || strToken == "IDENTIFIER")
			{
				if (Operator == "")
					LHS = GetArithmeticVal();
				else
					RHS = GetArithmeticVal();
			}
			else if (strLexem.equals(">") || strLexem.equals("<") || strLexem.equals("=")) {
				if (Operator != "") {
					System.err.println("Invalid Operator in If...Then Statement");
					System.exit(0);
				}
				Operator = strLexem;
			} else if (strLexem.toUpperCase().equals("THEN")) {
				break Loop1;
			}
			i++;
			strToken = GetToken(i);
			strLexem = GetLexem(i);
			if(Tokens.get(i).GetLineNumber() != strLineNum)
			{
				break Loop1;
			}
		}
		
		if(LHS == "" || Operator == "" || RHS == "")
		{
			System.err.println("Missing Expression or Operator in IF...Then statement.");
			System.exit(0);
		}
		
		if(!strLexem.toUpperCase().equals("THEN"))
		{
			System.err.println("IF Statement should be followed by Then statement.");
			System.exit(0);
		}
		
		int iLHS = Integer.parseInt(LHS);
		int iRHS = Integer.parseInt(RHS);
		
		switch (Operator) {
		case "=":
			if (iLHS == iRHS)
				return true;
			break;
		case ">":
			if (iLHS > iRHS)
				return true;
			break;
		case "<":
			if (iLHS < iRHS)
				return true;
			break;
		}
		return false;
	}

	public String GetArithmeticVal() {
		String strExpression = "";
		boolean bOpenBrace = false;
		String strToken = GetToken(i);
		String strLexem = GetLexem(i);
		Loop1: while (true) {
			if (strToken == "CONSTANT") {
				strExpression += strLexem;
			} else if (strToken == "IDENTIFIER") {
				SymbolTable objID = GetSymbol(strLexem);
				if (objID != null) {
					strExpression += objID.GetValue();
				} else {
					System.err.println("Undefined local variable " + strLexem);
					return "";
				}
			} else if (strLexem.equals("+") || strLexem.equals("-") || strLexem.equals("*") || strLexem.equals("/")
					|| strLexem.equals("(")) {
				if (strLexem.equals("-"))
					strLexem = strLexem.replace("-", "+-");
				strExpression += strLexem;
				if (strLexem.equals("("))
					bOpenBrace = true;
			} else if (strLexem.equals(")") && bOpenBrace) {
				strExpression += strLexem;
				bOpenBrace = false;
			} else {
				i--;
				break Loop1;
			}
			i++;
			strToken = GetToken(i);
			strLexem = GetLexem(i);
		}
		if (IsSingleDigit(strExpression))
			return strExpression;
		else
			return Integer.toString(EvaluateExp(strExpression));
	}

	private boolean IsSingleDigit(String strExp) {
		Pattern pattern = Pattern.compile("-?[0-9]+");
		Matcher m = pattern.matcher(strExp);
		return m.matches();
	}

	public boolean fnEvaluateStat(LexTokenPair objLexTokenPair, boolean blnStart, String strKeyword) {
		i++;
		if (!blnStart) {
			if (i >= Tokens.size())
				return false;
			if (GetLexem(i).equals("\r\n")) {
				i--;
				return false;
			}
			if (GetLexem(i).equals(",")) {
				i++;
				CheckSyntaxValidity(i, strKeyword);
			} else {
				objLexTokenPair.Error = "Syntax error in " + strKeyword + " Statement.";
				return false;
			}
		}
		objLexTokenPair.Token = GetToken(i);
		objLexTokenPair.Lexem = GetLexem(i);
		return true;
	}

	public void fnEvaluateProg() {
		LexTokenPair objLexTokenPair = new LexTokenPair();
		ArrayList<Integer> LineHistory = new ArrayList<Integer>();
		boolean blnStartStat;
		String strPrint;

		MainLoop: for (; i < Tokens.size(); i++) {
			objLexTokenPair.Error = "";
			strPrint = "";
			blnStartStat = true;

			switch (GetToken(i)) {
			case "RESERVED":
				switch (GetLexem(i).toUpperCase()) {
				case "END":
					CheckRETStat(LineHistory.size());
					System.exit(0);
					break;
				case "PRINT":
					while (fnEvaluateStat(objLexTokenPair, blnStartStat, "PRINT")) {
						if (objLexTokenPair.Token == "LITERAL") {
							strPrint += objLexTokenPair.Lexem.substring(1, objLexTokenPair.Lexem.length() - 1);
						} else // Expression
						{
							strPrint += GetArithmeticVal();
							if (strPrint == null || strPrint == "")
								break MainLoop;
						}
						blnStartStat = false;
					}

					if (strPrint != null && strPrint != "")
						System.out.print(strPrint);
					break;
				case "PRINTLN":
					while (fnEvaluateStat(objLexTokenPair, blnStartStat, "PRINTLN")) {
						if (objLexTokenPair.Token == "LITERAL") {
							strPrint += objLexTokenPair.Lexem.substring(1, objLexTokenPair.Lexem.length() - 1);
						} else // Expression
						{
							strPrint += GetArithmeticVal();
							if (strPrint == null || strPrint == "")
								break MainLoop;
						}
						blnStartStat = false;
					}

					if (strPrint != null && strPrint != "")
						System.out.println(strPrint);
					break;
				case "INTEGER":
					while (fnEvaluateStat(objLexTokenPair, blnStartStat, "INTEGER")) {
						if (objLexTokenPair.Token == "IDENTIFIER") {
							SymbolTable objSymbol = GetSymbol(objLexTokenPair.Lexem);
							if (objSymbol == null) {
								objSymbol = new SymbolTable(objLexTokenPair.Lexem, "INTEGER", 0);
								SymTable.add(objSymbol);
							} else {
								System.err.println("Duplicate local variable " + objLexTokenPair.Lexem);
								break;
							}
						}
						blnStartStat = false;
					}
					break;
				case "INPUT":
					String strMultipleInput = "";
					while (fnEvaluateStat(objLexTokenPair, blnStartStat, "INPUT")) {
						if (objLexTokenPair.Token == "IDENTIFIER") {
							strMultipleInput += objLexTokenPair.Lexem + ",";
						} else {
							System.err
									.println("Incorrect syntax in Input statement. Input must contain only IDENTIFIER");
							break;
						}
						blnStartStat = false;
					}

					if (strMultipleInput != "") {
						strMultipleInput = strMultipleInput.substring(0, strMultipleInput.length() - 1);
						String[] strInputList = strMultipleInput.split(",");
						int[] varList = new int[strInputList.length];

						for (int k = 0; k < varList.length; k++) {
							try {
								varList[k] = sc.nextInt();
							} catch (Exception ex) {
								System.err.println("Invalid Input");
								break MainLoop;
							}
						}
						for (int k = 0; k < varList.length; k++) {
							SymbolTable objSymbol3 = GetSymbol(strInputList[k]);
							if (objSymbol3 != null) {
								objSymbol3.SetValue(varList[k]);
							} else {
								System.err.println("Undefined local variable " + strInputList[k]);
								break;
							}
						}
					}
					break;
				case "LET":
					i++;
					CheckSyntaxValidity(i, "LET");
					objLexTokenPair.Token = GetToken(i);
					if (objLexTokenPair.Token == "IDENTIFIER") {
						String ID = GetLexem(i);
						SymbolTable objSymbol2 = GetSymbol(ID);
						if (objSymbol2 != null) {
							i++;
							CheckSyntaxValidity(i, "LET");
							objLexTokenPair.Lexem = GetLexem(i);
							if (objLexTokenPair.Lexem.equals("=")) {
								i++;
								CheckSyntaxValidity(i, "LET");
								String strExpVal = GetArithmeticVal();
								if (strExpVal == null || strExpVal == "")
									break MainLoop;
								try {
									objSymbol2.SetValue(new Integer(strExpVal));
								} catch (Exception ex) {
									System.err.println("Invalid Input");
									break;
								}
							} else {
								System.err.println("Syntax error near " + ID);
								break;
							}
						} else {
							System.err.println("Undefined local variable " + ID);
							break;
						}
					}
					break;
				case "GOTO":
					i++;
					CheckSyntaxValidity(i, "GOTO");
					objLexTokenPair.Token = GetToken(i);
					if (objLexTokenPair.Token == "CONSTANT") {
						objLexTokenPair.Lexem = GetLexem(i);
						int j = 0;
						for (; j < Tokens.size(); j++) {
							if (Tokens.get(j).GetLineNumber().equals(objLexTokenPair.Lexem)) {
								i = j - 1;
								break;
							}
						}
						if (j >= Tokens.size()) {
							System.err.println("Error : Line number specified in GOTO statement does not exists.");
							break MainLoop;
						}
					} else {
						System.err.println("Line number is expected in GOTO statement.");
						break MainLoop;
					}
					break;
				case "GOSUB":
					i++;
					CheckSyntaxValidity(i, "GOSUB");
					objLexTokenPair.Token = GetToken(i);
					if (objLexTokenPair.Token == "CONSTANT") {
						objLexTokenPair.Lexem = GetLexem(i);
						int j = 0;
						for (; j < Tokens.size(); j++) {
							if (Tokens.get(j).GetLineNumber().equals(objLexTokenPair.Lexem)) {
								LineHistory.add(i);
								i = j - 1;
								break;
							}
						}
						if (j >= Tokens.size()) {
							System.err.println("Error : Line number specified in GOSUB statement does not exists.");
							break MainLoop;
						}
					} else {
						System.err.println("Line number is expected in GOSUB statement.");
						break MainLoop;
					}
					break;
				case "RET":
					if(LineHistory.size() > 0)
					{
						i = LineHistory.get(LineHistory.size() - 1);
						LineHistory.remove(LineHistory.size() - 1);
						continue;
					}
					else
					{
						System.err.println("No GOSUB statement is found before RET.");
						System.exit(0);
					}
				case "IF":
					if (GetBoolExpVal())
						continue;
					else {
						while (Tokens.get(i).GetLineNumber() == Tokens.get(++i).GetLineNumber())
							continue;
						i--;
						break;
					}
				}
				break;
			case "COMMENT":
				while (!GetLexem(++i).equals("\r\n")) {
					continue;
				}
				break;
			}
			if (!objLexTokenPair.Error.equals("")) {
				System.err.println(objLexTokenPair.Error);
				break MainLoop;
			}
		}
		CheckRETStat(LineHistory.size());
	}
	
	private void CheckRETStat(int iGoSub)
	{
		if (iGoSub > 0) {
			System.err.println("No RET statement found for GOTOSUB");
			System.exit(0);
		}
	}
	
	private void CheckSyntaxValidity(int iCurrent, String strSentence) {
		if (iCurrent >= Tokens.size()) {
			System.err.println("Incomplete " + strSentence + ".... sentence");
			System.exit(0);
		}
	}

	// If Operator2 has higher or equal precedence as Operator1...then return
	// true
	private boolean CheckPrecedence(char Operator1, char Operator2) {
		if (Operator2 == '(' || Operator2 == ')')
			return false;
		if ((Operator1 == '*' || Operator1 == '/') && (Operator2 == '+' || Operator2 == '-'))
			return false;
		else
			return true;
	}

	private int PerformOperation(char Operator, int Operand2, int Operand1) {
		switch (Operator) {
		case '+':
			return Operand1 + Operand2;
		case '-':
			return Operand1 - Operand2;
		case '*':
			return Operand1 * Operand2;
		case '/':
			if (Operand2 == 0)
				throw new UnsupportedOperationException("Cannot divide by zero");
			return Operand1 / Operand2;
		}
		return 0;
	}

	private int EvaluateExp(String strExp) {
		char chPrevChar = '\0';
		char[] chTokens = strExp.toCharArray();

		Stack<Integer> Operands = new Stack<Integer>();
		Stack<Character> Operator = new Stack<Character>();

		for (int j = 0; j < chTokens.length; j++) {
			if (j == 0 && chTokens[j] == '+')
				continue;

			if (chTokens[j] == ' ')
				continue;

			if (chTokens[j] == '-' && chTokens[j + 1] >= '0' && chTokens[j + 1] <= '9') {
				chPrevChar = '-';
				continue;
			}

			if (chTokens[j] >= '0' && chTokens[j] <= '9') {
				StringBuffer sb = new StringBuffer();

				while (j < chTokens.length && chTokens[j] >= '0' && chTokens[j] <= '9') {
					if (chPrevChar == '-') {
						sb.append('-');
						chPrevChar = '\0';
					}
					sb.append(chTokens[j++]);
				}
				Operands.push(Integer.parseInt(sb.toString()));
				j--;
			}

			// Current token is an opening brace, push it to 'Operator'
			else if (chTokens[j] == '(')
				Operator.push(chTokens[j]);

			// Closing brace encountered, solve entire brace
			else if (chTokens[j] == ')') {
				while (Operator.peek() != '(')
					Operands.push(PerformOperation(Operator.pop(), Operands.pop(), Operands.pop()));
				Operator.pop();
			}

			// Current token is an operator.
			else if (chTokens[j] == '+' || chTokens[j] == '-' || chTokens[j] == '*' || chTokens[j] == '/') {
				// While Precedence of Top Operator is higher or equal to
				// current operator
				// Apply top operator to top two operands
				while (!Operator.empty() && CheckPrecedence(chTokens[j], Operator.peek()))
					Operands.push(PerformOperation(Operator.pop(), Operands.pop(), Operands.pop()));

				// Push current token to 'Operator'.
				Operator.push(chTokens[j]);
			}
		}

		// Entire expression has been parsed at this point, apply remaining
		// Operator to remaining Operands
		while (!Operator.empty())
			Operands.push(PerformOperation(Operator.pop(), Operands.pop(), Operands.pop()));

		// Top of 'Operands' contains result, return it
		return Operands.pop();
	}
}
