package Tokenizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.core.util.PublicScanner;

import com.google.common.collect.Lists;

public class JavaTokenizer {
	private static final Logger LOGGER = Logger.getLogger(JavaTokenizer.class
			.getName());
	
	static final String SENTENCE_END = "<SENTENCE_END/>";
	static final String SENTENCE_START = "<SENTENCE_START>";
	
	public static final RegexFileFilter FileFilter = new RegexFileFilter(
			".*\\.java$");

	public static final String IDENTIFIER_ID = Integer
			.toString(ITerminalSymbols.TokenNameIdentifier);
	public static final String[] KEYWORD_TYPE_IDs = new String[] {
		Integer.toString(ITerminalSymbols.TokenNameboolean),
		Integer.toString(ITerminalSymbols.TokenNamebyte),
		Integer.toString(ITerminalSymbols.TokenNamechar),
		Integer.toString(ITerminalSymbols.TokenNamedouble),
		Integer.toString(ITerminalSymbols.TokenNamefloat),
		Integer.toString(ITerminalSymbols.TokenNameint),
		Integer.toString(ITerminalSymbols.TokenNamelong),
		Integer.toString(ITerminalSymbols.TokenNameshort),
		Integer.toString(ITerminalSymbols.TokenNamevoid) };
	public static final String[] STRING_LITERAL_IDs = new String[] {
		Integer.toString(ITerminalSymbols.TokenNameStringLiteral),
		Integer.toString(ITerminalSymbols.TokenNameCharacterLiteral) };
	public static final String[] NUMBER_LITERAL_IDs = new String[] {
		Integer.toString(ITerminalSymbols.TokenNameDoubleLiteral),
		Integer.toString(ITerminalSymbols.TokenNameFloatingPointLiteral),
		Integer.toString(ITerminalSymbols.TokenNameIntegerLiteral),
		Integer.toString(ITerminalSymbols.TokenNameLongLiteral) };
	public static final String[] COMMENT_IDs = new String[] {
		Integer.toString(ITerminalSymbols.TokenNameCOMMENT_BLOCK),
		Integer.toString(ITerminalSymbols.TokenNameCOMMENT_JAVADOC),
		Integer.toString(ITerminalSymbols.TokenNameCOMMENT_LINE) };
	public static final String[] OPERATOR_IDs = new String[] {
		Integer.toString(ITerminalSymbols.TokenNameAND),
		Integer.toString(ITerminalSymbols.TokenNameAND_AND),
		Integer.toString(ITerminalSymbols.TokenNameAND_EQUAL),
		Integer.toString(ITerminalSymbols.TokenNameCOLON),
		Integer.toString(ITerminalSymbols.TokenNameCOMMA),
		Integer.toString(ITerminalSymbols.TokenNameDIVIDE),
		Integer.toString(ITerminalSymbols.TokenNameDIVIDE_EQUAL),
		Integer.toString(ITerminalSymbols.TokenNameDOT),
		Integer.toString(ITerminalSymbols.TokenNameELLIPSIS),
		Integer.toString(ITerminalSymbols.TokenNameEQUAL),
		Integer.toString(ITerminalSymbols.TokenNameEQUAL_EQUAL),
		Integer.toString(ITerminalSymbols.TokenNameGREATER),
		Integer.toString(ITerminalSymbols.TokenNameGREATER_EQUAL),
		Integer.toString(ITerminalSymbols.TokenNameLBRACKET),
		Integer.toString(ITerminalSymbols.TokenNameLEFT_SHIFT),
		Integer.toString(ITerminalSymbols.TokenNameLEFT_SHIFT_EQUAL),
		Integer.toString(ITerminalSymbols.TokenNameLESS),
		Integer.toString(ITerminalSymbols.TokenNameLESS_EQUAL),
		Integer.toString(ITerminalSymbols.TokenNameLPAREN),
		Integer.toString(ITerminalSymbols.TokenNameMINUS),
		Integer.toString(ITerminalSymbols.TokenNameMINUS_EQUAL),
		Integer.toString(ITerminalSymbols.TokenNameMINUS_MINUS),
		Integer.toString(ITerminalSymbols.TokenNameMULTIPLY),
		Integer.toString(ITerminalSymbols.TokenNameMULTIPLY_EQUAL),
		Integer.toString(ITerminalSymbols.TokenNameNOT),
		Integer.toString(ITerminalSymbols.TokenNameNOT_EQUAL),
		Integer.toString(ITerminalSymbols.TokenNameOR),
		Integer.toString(ITerminalSymbols.TokenNameOR_EQUAL),
		Integer.toString(ITerminalSymbols.TokenNameOR_OR),
		Integer.toString(ITerminalSymbols.TokenNamePLUS),
		Integer.toString(ITerminalSymbols.TokenNamePLUS_EQUAL),
		Integer.toString(ITerminalSymbols.TokenNamePLUS_PLUS),
		Integer.toString(ITerminalSymbols.TokenNameQUESTION),
		Integer.toString(ITerminalSymbols.TokenNameRBRACKET),
		Integer.toString(ITerminalSymbols.TokenNameREMAINDER),
		Integer.toString(ITerminalSymbols.TokenNameREMAINDER_EQUAL),
		Integer.toString(ITerminalSymbols.TokenNameRIGHT_SHIFT),
		Integer.toString(ITerminalSymbols.TokenNameRIGHT_SHIFT_EQUAL),
		Integer.toString(ITerminalSymbols.TokenNameRPAREN),
		Integer.toString(ITerminalSymbols.TokenNameSEMICOLON),
		Integer.toString(ITerminalSymbols.TokenNameTWIDDLE),
		Integer.toString(ITerminalSymbols.TokenNameUNSIGNED_RIGHT_SHIFT),
		Integer.toString(ITerminalSymbols.TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL),
		Integer.toString(ITerminalSymbols.TokenNameXOR),
		Integer.toString(ITerminalSymbols.TokenNameXOR_EQUAL) };
	public static final String[] BRACE_IDs = new String[] {
		Integer.toString(ITerminalSymbols.TokenNameLBRACE),
		Integer.toString(ITerminalSymbols.TokenNameRBRACE), };
	public static final String[] SYNTAX_IDs = {
		Integer.toString(ITerminalSymbols.TokenNameCOMMA),
		Integer.toString(ITerminalSymbols.TokenNameDOT),
		Integer.toString(ITerminalSymbols.TokenNameELLIPSIS),
		Integer.toString(ITerminalSymbols.TokenNameSEMICOLON),
		Integer.toString(ITerminalSymbols.TokenNameLBRACE),
		Integer.toString(ITerminalSymbols.TokenNameRBRACE),
		Integer.toString(ITerminalSymbols.TokenNameLPAREN),
		Integer.toString(ITerminalSymbols.TokenNameRPAREN),
		Integer.toString(ITerminalSymbols.TokenNameLBRACKET),
		Integer.toString(ITerminalSymbols.TokenNameRBRACKET) };
	
	private final boolean comments;
	
	public JavaTokenizer(){
		comments = false;
	}
	
	public JavaTokenizer(final boolean includeComments) {
		this.comments = includeComments;
	}
	
	public RegexFileFilter getFileFilter(){
		return FileFilter;
	}
	
	public String getIdentifierType(){
		return IDENTIFIER_ID;
	}
	
	public ArrayList<String> getKeywordTypes(){
		return (ArrayList<String>) Arrays.asList(KEYWORD_TYPE_IDs);
	}
	
	public ArrayList<String> getLiteralTypes(){
		ArrayList<String> allLiterals = (ArrayList<String>) Arrays.asList(STRING_LITERAL_IDs);
		allLiterals.addAll((ArrayList<String>) Arrays.asList(NUMBER_LITERAL_IDs));
		return allLiterals;
	}
	
	String stripTokenIfNeeded(String token) {
		return token.replace('\n', ' ').replace('\t', ' ').replace('\r', ' ')
				.replace("\n", " ").replace("\t", " ").replace("\r", " ")
				.replace("\'\\\\\'", "\'|\'").replace("\\", "|");
	}

	public ArrayList<Token> getTokenList(char[] code){
		PublicScanner sc = new PublicScanner();
		sc.tokenizeComments = comments;
		ArrayList<Token> tokens = Lists.newArrayList();
		tokens.add(new Token(SENTENCE_START, SENTENCE_START));
		sc.setSource(code);
		do {
			try {
				int token = sc.getNextToken();
				if (token == ITerminalSymbols.TokenNameEOF) {
					break;
				}

				tokens.add(new Token(stripTokenIfNeeded(sc.getCurrentTokenString()), Integer.toString(token)));
			} catch (final InvalidInputException e) {
				LOGGER.warning(ExceptionUtils.getFullStackTrace(e));
			} catch (final StringIndexOutOfBoundsException e) {
				LOGGER.warning(ExceptionUtils.getFullStackTrace(e));
			}
		} while (!sc.atEnd());
		tokens.add(new Token(SENTENCE_END, SENTENCE_END));
		return tokens;
	}
	
	public ArrayList<Token> getTokenListFromFile(File codeFile)
			throws IOException {
		return getTokenList(FileUtils.readFileToString(codeFile)
				.toCharArray());
	}
	
	public ArrayList<String> tokenList(char[] code){
		PublicScanner sc = new PublicScanner();
		sc.tokenizeComments = comments;
		ArrayList<String> tokens = Lists.newArrayList();
		tokens.add(SENTENCE_START);
		sc.setSource(code);
		do {
			try {
				int token = sc.getNextToken();
				if (token == ITerminalSymbols.TokenNameEOF) {
					break;
				}

				tokens.add(stripTokenIfNeeded(sc.getCurrentTokenString()));
			} catch (final InvalidInputException e) {
				LOGGER.warning(ExceptionUtils.getFullStackTrace(e));
			} catch (final StringIndexOutOfBoundsException e) {
				LOGGER.warning(ExceptionUtils.getFullStackTrace(e));
			}
		} while (!sc.atEnd());
		tokens.add(SENTENCE_END);
		return tokens;
	}
	
	public ArrayList<String> tokenListFromFile(File codeFile)
			throws IOException {
		return tokenList(FileUtils.readFileToString(codeFile)
				.toCharArray());
	}
	
	public static void main(String[] args) throws IOException {
		JavaTokenizer jt = new JavaTokenizer();
		File toks = new File("/Users/udayantandon/Documents/workspace/AutoSuggest/src/Tokenizer/Token.java");
		System.out.println(jt.getTokenListFromFile(toks));
	}
	
}
