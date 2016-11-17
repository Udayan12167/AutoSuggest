package Tokenizer;

import com.google.common.base.Objects;

public class Token {
	public final String token;

	public final String tokenType;

	public Token(Token other) {
		token = other.token;
		tokenType = other.tokenType;
	}

	public Token(String tokenName, String tokenType) {
		token = tokenName;
		this.tokenType = tokenType;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Token)) {
			return false;
		}
		final Token other = (Token) obj;
		return other.token.equals(token)
				&& other.tokenType.equals(tokenType);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(token, tokenType);
	}

	@Override
	public String toString() {
		return token + " (" + tokenType + ")";
	}
}
