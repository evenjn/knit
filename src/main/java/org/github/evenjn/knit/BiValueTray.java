package org.github.evenjn.knit;

import org.github.evenjn.lang.Bi;
import org.github.evenjn.lang.Equivalencer;

/**
 * 
 * <h2>Design Notes</h2>
 * 
 * <p>
 * Type {@code BiValueTray} is opaque, generic and non-descriptive. While it is
 * convenient to use {@code BiValueTray} to develop code quickly, we recommend
 * that APIs never expose {@code BiValueTray} or types that extends or implement
 * {@code BiValue}. Instead, we recommend that APIs define their own types that
 * provide context and explanation for the elements of the pair.
 * </p>
 * 
 * <p>
 * In particular, for return types, we recommend that APIs expose a custom
 * interface (that does not extend {@code BiValueTray}) and implementation uses
 * a private class extending {@code BiValueTray} and implementing the custom
 * interface.
 * </p>
 * 
 * @param <F>
 *          The type of the object in the <em>front</em> slot.
 * @param <B>
 *          The type of the object in the <em>back</em> slot..
 * @since 1.0
 */
public class BiValueTray<F, B> implements
		Bi<F, B> {

	private final int hash_code;

	private final F front;

	private final B back;

	private Equivalencer<F, Object> front_equivalencer;

	private Equivalencer<B, Object> back_equivalencer;

	protected BiValueTray(
			F front,
			B back,
			Equivalencer<F, Object> front_equivalencer,
			Equivalencer<B, Object> back_equivalencer) {
		this.front_equivalencer = front_equivalencer;
		this.back_equivalencer = back_equivalencer;
		int hashCode = 1;
		hashCode = 31 * hashCode + ( front == null ? 0 : front.hashCode( ) );
		hashCode = 31 * hashCode + ( back == null ? 0 : back.hashCode( ) );
		this.hash_code = hashCode;
		this.front = front;
		this.back = back;
	}

	public static <F, B> BiValueTray<F, B> nu(
			F front,
			B back,
			Equivalencer<F, Object> front_equivalencer,
			Equivalencer<B, Object> back_equivalencer ) {
		return new BiValueTray<F, B>( front, back, front_equivalencer,
				back_equivalencer );
	}

	@Override
	public F front( ) {
		return front;
	}

	@Override
	public B back( ) {
		return back;
	}

	@Override
	public int hashCode( ) {
		return hash_code;
	}

	@Override
	public boolean equals( Object other ) {
		if ( other == this )
			return true;
		if ( !( other instanceof BiValueTray ) )
			return false;
		BiValueTray<?, ?> o = (BiValueTray<?, ?>) other;
		return front_equivalencer.equivalent( front, o.front )
				&& back_equivalencer.equivalent( back, o.back );
	}

}
