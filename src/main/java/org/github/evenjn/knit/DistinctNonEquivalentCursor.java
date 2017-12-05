package org.github.evenjn.knit;

import java.util.HashSet;
import java.util.function.Function;

import org.github.evenjn.lang.Equivalencer;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;

class DistinctNonEquivalentCursor<I> implements
		Cursor<I> {

	private final Cursor<I> wrapped;

	private final HashSet<EQ<I>> set = new HashSet<>( );

	private final Function<I, Integer> hasher;

	private final Equivalencer<I, Object> equivalencer;

	DistinctNonEquivalentCursor(Cursor<I> cursor,
			Function<I, Integer> hasher,
			Equivalencer<I, Object> equivalencer) {
		this.wrapped = cursor;
		this.hasher = hasher;
		this.equivalencer = equivalencer;
	}

	@Override
	public I next( )
			throws EndOfCursorException {
		for ( ;; ) {
			I next = wrapped.next( );
			EQ<I> eq = new EQ<I>( next, hasher, equivalencer );
			if ( !set.contains( eq ) ) {
				set.add( eq );
				return next;
			}
		}
	}

	private static class EQ<K> {

		private K value;

		private final int hc;

		private Equivalencer<K, Object> equivalencer;

		EQ(K value,
				Function<K, Integer> hasher,
				Equivalencer<K, Object> equivalencer) {
			this.value = value;
			this.equivalencer = equivalencer;
			this.hc = hasher.apply( value );
		}

		public int hashCode( ) {
			return hc;
		}

		public boolean equals( Object other ) {
			if ( other == null || !( other instanceof EQ ) ) {
				return false;
			}
			EQ<?> o = (EQ<?>) other;
			if ( equivalencer.equivalent( value, o.value ) ) {
				return true;
			}
			return false;
		}
	}

}
