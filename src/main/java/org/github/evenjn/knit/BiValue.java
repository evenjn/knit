package org.github.evenjn.knit;

import org.github.evenjn.lang.Bi;
import org.github.evenjn.lang.Equivalencer;

/**
 * An implementation of {@link org.github.evenjn.yarn.Di Di}.
 *
 * @param <A>
 *          The type of the object in the <em>front</em> slot.
 * @param <B>
 *          The type of the object in the <em>back</em> slot..
 * @since 1.0
 */
public final class BiValue<F, B> implements
		Bi<F, B> {

	private final int hash_code;

	private final F front;

	private final B back;

	private Equivalencer<F, Object> front_equivalencer;

	private Equivalencer<B, Object> back_equivalencer;

	private BiValue(
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

	public static <F, B> BiValue<F, B> nu(
			F front,
			B back,
			Equivalencer<F, Object> front_equivalencer,
			Equivalencer<B, Object> back_equivalencer ) {
		return new BiValue<F, B>( front, back, front_equivalencer,
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
		if ( !( other instanceof BiValue ) )
			return false;
		BiValue<?, ?> o = (BiValue<?, ?>) other;
		return front_equivalencer.equivalent( front, o.front )
				&& back_equivalencer.equivalent( back, o.back );
	}

}
