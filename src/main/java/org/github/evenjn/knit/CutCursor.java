package org.github.evenjn.knit;

import java.util.function.Predicate;

import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;

class CutCursor<T> implements
		Cursor<KnittingCursor<T>> {

	private final Cursor<T> wrapped;

	private final Predicate<T> predicate;

	public CutCursor(Cursor<T> wrapped, Predicate<T> predicate) {
		this.wrapped = wrapped;
		this.predicate = predicate;
	}

	private boolean end = false;

	private T cached = null;

	private boolean is_cached = false;

	@Override
	public KnittingCursor<T> next( )
			throws EndOfCursorException {
		if ( end ) {
			throw EndOfCursorException.neo( );
		}

		return KnittingCursor.wrap( new Cursor<T>( ) {

			@Override
			public T next( )
					throws EndOfCursorException {
				if ( !is_cached ) {
					try {
						cached = wrapped.next( );
					}
					catch ( EndOfCursorException e ) {
						end = true;
						throw e;
					}
					if ( predicate.test( cached ) ) {
						is_cached = true;
						throw EndOfCursorException.neo( );
					}
				}
				is_cached = false;
				return cached;
			}
		} );
	}
}
