package org.rexcellentgames.byejava.scanner;

import java.util.ArrayList;

public class Scanner {
	private String source;
	private int position;
	public int line;
	private int start;
	private boolean ended;
	public boolean hadError;

	public Scanner(String source) {
		setSource(source);
	}

	public void setSource(String source) {
		this.source = source;
		this.position = 0;
		this.start = 0;
		this.ended = false;
		this.line = 1;
		this.hadError = false;
	}

	protected Token makeToken(TokenType type) {
		return new Token(type, this.start, this.position - this.start, this.line);
	}

	public class Error extends RuntimeException {
		public String message;

		public Error(String message) {
			this.message = message;
		}

		@Override
		public String getMessage() {
			return this.message;
		}
	}

	protected void error(String error) {
		throw new Error(String.format("[line %d] %s", this.line, error));
	}

	protected char advance() {
		if (!ended) {
			this.position ++;
		} else {
			return '\0';
		}

		if (this.position >= this.source.length()) {
			this.ended = true;
		}

		return this.source.charAt(this.position - 1);
	}

	protected char peek() {
		return this.ended ? '\0' : this.source.charAt(this.position);
	}

	protected char peekNext() {
		return this.position > this.source.length() ? '\0' : this.source.charAt(this.position + 1);
	}

	protected boolean match(char c) {
		if (this.peek() == c) {
			this.advance();
			return true;
		}

		return false;
	}

	protected void skipWhitespace() {
		while (true) {
			char c = this.peek();

			switch (c) {
				case ' ':
				case '\r':
				case '\t':
					this.advance();
					continue;

				case '\n': {
					this.advance();
					this.line++;
					continue;
				}

				case '/': {
					if (this.peekNext() == '/') {
						while (this.peek() != '\n' && this.peek() != '\0') {
							this.advance();
						}

						continue;
					} else if (this.peekNext() == '*') {
						this.advance();
						this.advance();

						while (!(this.peek() == '*' && this.peekNext() == '/') && this.peek() != '\0') {
							if (this.advance() == '\n') {
								this.line++;
							}
						}

						this.advance();
						this.advance();

						continue;
					}
				}

				default: {
					this.start = this.position;
					return;
				}
			}
		}
	}

	private static boolean isAlpha(char c) {
		return c == '_' || c == '@' || Character.isAlphabetic(c);
	}

	private TokenType getIdentifierType() {
		return Keywords.types.getOrDefault(this.source.substring(this.start, this.position), TokenType.IDENTIFIER);
	}

	private Token decideToken(char c, TokenType a, TokenType b) {
		if (this.match(c)) {
			return this.makeToken(a);
		}
		
		return this.makeToken(b);
	}

	private Token decideToken(char ch, TokenType a, char e, TokenType b, TokenType c) {
		if (this.match(ch)) {
			return this.makeToken(a);
		}

		if (this.match(e)) {
			return this.makeToken(b);
		}

		return this.makeToken(c);
	}

	public Token scanToken() {
		this.skipWhitespace();
		char c = this.advance();

		if (c == '\0') {
			return makeToken(TokenType.EOF);
		}

		if (Character.isDigit(c)) {
			boolean hex = this.match('x');

			while (true) {
				char ch = this.peek();

				if (hex) {
					ch = Character.toLowerCase(ch);
				}

				if (!(Character.isDigit(ch) || (hex && (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'e' || ch == 'd' || ch == 'f')))) {
					break;
				}

				this.advance();
			}

			if (this.peek() == '.' && Character.isDigit(this.peekNext())) {
				this.advance();

				while (Character.isDigit(this.peek())) {
					this.advance();
				}
			}

			this.match('f');
			return makeToken(TokenType.NUMBER);
		}

		if (isAlpha(c)) {
			boolean at = c == '@';
			boolean ok = true;

			if (at && this.peek() != 'O') {
				ok = false;
			}

			while (true) {
				c = this.peek();

				if (isAlpha(c) || Character.isDigit(c)) {
					this.advance();
					continue;
				}

				break;
			}

			if (ok) {
				return this.makeToken(this.getIdentifierType());
			}

			return this.scanToken();
		}

		switch (c) {
			case '(': return this.makeToken(TokenType.LEFT_PAREN);
			case ')': return this.makeToken(TokenType.RIGHT_PAREN);
			case '{': return this.makeToken(TokenType.LEFT_BRACE);
			case '}': return this.makeToken(TokenType.RIGHT_BRACE);
			case '[': return this.makeToken(TokenType.LEFT_BRACKET);
			case ']': return this.makeToken(TokenType.RIGHT_BRACKET);
			case ';': return this.makeToken(TokenType.SEMICOLON);
			case ':': return this.makeToken(TokenType.COLON);
			case '?': return this.makeToken(TokenType.QUESTION);
			case ',': return this.makeToken(TokenType.COMMA);
			case '~': return this.makeToken(TokenType.TILD);
			case '=': return this.decideToken('=', TokenType.EQUAL_EQUAL, TokenType.EQUAL);
			case '-': return this.decideToken('=', TokenType.MINUS_EQUAL, '-', TokenType.MINUS_MINUS, TokenType.MINUS);
			case '+': return this.decideToken('=', TokenType.PLUS_EQUAL, '+', TokenType.PLUS_PLUS, TokenType.PLUS);
			case '/': return this.decideToken('=', TokenType.SLASH_EQUAL, TokenType.SLASH);
			case '%': return this.decideToken('=', TokenType.PERCENT_EQUAL, TokenType.PERCENT);
			case '*': return this.decideToken('=', TokenType.STAR_EQUAL, TokenType.STAR);
			case '>': return this.decideToken('=', TokenType.GREATER_EQUAL, TokenType.GREATER);
			case '<': return this.decideToken('=', TokenType.LESS_EQUAL, '<', TokenType.LESS_LESS, TokenType.LESS);
			case '!': return this.decideToken('=', TokenType.BANG_EQUAL, TokenType.BANG);
			case '&': return this.decideToken('=', TokenType.AMPERSAND_EQUAL, '&', TokenType.AND, TokenType.AMPERSAND);
			case '|': return this.decideToken('=', TokenType.BAR_EQUAL, '|', TokenType.OR, TokenType.BAR);

			case '.': {
				if (this.match('.')) {
					if (this.match('.')) {
						return this.makeToken(TokenType.DOT_DOT_DOT);
					} else {
						this.error("'.' expected");
					}
				}

				return this.makeToken(TokenType.DOT);
			}

			case '\"': {
				boolean lastSlash = false;
				char prev = '\0';

				while (true) {
					c = this.advance();

					if (c == '\0') {
						this.error("Unterminated string");
					}

					if (c == '\"' && !lastSlash) {
						break;
					}

					if (c == '\\' && prev != '\\'){
						lastSlash = true;
					} else {
						lastSlash = false;
					}

					prev = c;

					if (c == '\n') {
						this.line++;
					}
				}

				return makeToken(TokenType.STRING);
			}

			case '\'': {
				if (this.advance() == '\\') {
					this.advance();
				}

				c = this.advance();

				if (c != '\'') {
					this.error("' expected");
				}

				return this.makeToken(TokenType.CHAR);
			}

			default: this.error("Unexpected char ''");
		}

		return null;
	}

	public ArrayList<Token> scan() {
		ArrayList<Token> tokens = new ArrayList<>();

		if (this.source.length() == 0 || ended) {
			return tokens;
		}

		while (!this.ended) {
			try {
				tokens.add(this.scanToken());
			} catch (Error e) {
				this.hadError = true;
				e.printStackTrace();
				break;
			}
		}

		tokens.add(this.makeToken(TokenType.EOF));

		return tokens;
	}
}
