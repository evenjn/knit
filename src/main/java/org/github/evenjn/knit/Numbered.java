package org.github.evenjn.knit;

public class Numbered<K> {

	private final K value;

	private final int number;

	public Numbered(K value, int number) {
		this.value = value;
		this.number = number;
	}

	public K get( ) {
		return value;
	}

	public int getNumber( ) {
		return number;
	}
}
