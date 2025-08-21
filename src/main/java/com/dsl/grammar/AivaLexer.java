package com.dsl.grammar;// Generated from Aiva.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class AivaLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, IDENTIFIER=9, 
		WS=10;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "IDENTIFIER", 
			"WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'START_TRACK'", "'END_TRACK'", "'.'", "'CALL_FUNCTION_SWITCH'", 
			"'->'", "'SEND_TEMPLATE'", "'GOTO'", "':'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, "IDENTIFIER", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public AivaLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Aiva.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\nh\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001"+
		"\u0007\u0001\b\u0001\b\u0005\b]\b\b\n\b\f\b`\t\b\u0001\t\u0004\tc\b\t"+
		"\u000b\t\f\td\u0001\t\u0001\t\u0000\u0000\n\u0001\u0001\u0003\u0002\u0005"+
		"\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n"+
		"\u0001\u0000\u0003\u0003\u0000@Z__az\u0004\u000009AZ__az\u0003\u0000\t"+
		"\n\r\r  i\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000"+
		"\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000"+
		"\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000"+
		"\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000"+
		"\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000"+
		"\u0001\u0015\u0001\u0000\u0000\u0000\u0003!\u0001\u0000\u0000\u0000\u0005"+
		"+\u0001\u0000\u0000\u0000\u0007-\u0001\u0000\u0000\u0000\tB\u0001\u0000"+
		"\u0000\u0000\u000bE\u0001\u0000\u0000\u0000\rS\u0001\u0000\u0000\u0000"+
		"\u000fX\u0001\u0000\u0000\u0000\u0011Z\u0001\u0000\u0000\u0000\u0013b"+
		"\u0001\u0000\u0000\u0000\u0015\u0016\u0005S\u0000\u0000\u0016\u0017\u0005"+
		"T\u0000\u0000\u0017\u0018\u0005A\u0000\u0000\u0018\u0019\u0005R\u0000"+
		"\u0000\u0019\u001a\u0005T\u0000\u0000\u001a\u001b\u0005_\u0000\u0000\u001b"+
		"\u001c\u0005T\u0000\u0000\u001c\u001d\u0005R\u0000\u0000\u001d\u001e\u0005"+
		"A\u0000\u0000\u001e\u001f\u0005C\u0000\u0000\u001f \u0005K\u0000\u0000"+
		" \u0002\u0001\u0000\u0000\u0000!\"\u0005E\u0000\u0000\"#\u0005N\u0000"+
		"\u0000#$\u0005D\u0000\u0000$%\u0005_\u0000\u0000%&\u0005T\u0000\u0000"+
		"&\'\u0005R\u0000\u0000\'(\u0005A\u0000\u0000()\u0005C\u0000\u0000)*\u0005"+
		"K\u0000\u0000*\u0004\u0001\u0000\u0000\u0000+,\u0005.\u0000\u0000,\u0006"+
		"\u0001\u0000\u0000\u0000-.\u0005C\u0000\u0000./\u0005A\u0000\u0000/0\u0005"+
		"L\u0000\u000001\u0005L\u0000\u000012\u0005_\u0000\u000023\u0005F\u0000"+
		"\u000034\u0005U\u0000\u000045\u0005N\u0000\u000056\u0005C\u0000\u0000"+
		"67\u0005T\u0000\u000078\u0005I\u0000\u000089\u0005O\u0000\u00009:\u0005"+
		"N\u0000\u0000:;\u0005_\u0000\u0000;<\u0005S\u0000\u0000<=\u0005W\u0000"+
		"\u0000=>\u0005I\u0000\u0000>?\u0005T\u0000\u0000?@\u0005C\u0000\u0000"+
		"@A\u0005H\u0000\u0000A\b\u0001\u0000\u0000\u0000BC\u0005-\u0000\u0000"+
		"CD\u0005>\u0000\u0000D\n\u0001\u0000\u0000\u0000EF\u0005S\u0000\u0000"+
		"FG\u0005E\u0000\u0000GH\u0005N\u0000\u0000HI\u0005D\u0000\u0000IJ\u0005"+
		"_\u0000\u0000JK\u0005T\u0000\u0000KL\u0005E\u0000\u0000LM\u0005M\u0000"+
		"\u0000MN\u0005P\u0000\u0000NO\u0005L\u0000\u0000OP\u0005A\u0000\u0000"+
		"PQ\u0005T\u0000\u0000QR\u0005E\u0000\u0000R\f\u0001\u0000\u0000\u0000"+
		"ST\u0005G\u0000\u0000TU\u0005O\u0000\u0000UV\u0005T\u0000\u0000VW\u0005"+
		"O\u0000\u0000W\u000e\u0001\u0000\u0000\u0000XY\u0005:\u0000\u0000Y\u0010"+
		"\u0001\u0000\u0000\u0000Z^\u0007\u0000\u0000\u0000[]\u0007\u0001\u0000"+
		"\u0000\\[\u0001\u0000\u0000\u0000]`\u0001\u0000\u0000\u0000^\\\u0001\u0000"+
		"\u0000\u0000^_\u0001\u0000\u0000\u0000_\u0012\u0001\u0000\u0000\u0000"+
		"`^\u0001\u0000\u0000\u0000ac\u0007\u0002\u0000\u0000ba\u0001\u0000\u0000"+
		"\u0000cd\u0001\u0000\u0000\u0000db\u0001\u0000\u0000\u0000de\u0001\u0000"+
		"\u0000\u0000ef\u0001\u0000\u0000\u0000fg\u0006\t\u0000\u0000g\u0014\u0001"+
		"\u0000\u0000\u0000\u0003\u0000^d\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}