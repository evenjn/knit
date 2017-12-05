package org.github.evenjn.knit;

import java.util.HashSet;

import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;

class DistinctNonEqualCursor<I> implements
		Cursor<I> {

	private final Cursor<I> wrapped;

	private final HashSet<I> set = new HashSet<>( );

	DistinctNonEqualCursor(Cursor<I> cursor) {
		this.wrapped = cursor;
	}

	@Override
	public I next( )
			throws EndOfCursorException {
		for ( ;; ) {
			I next = wrapped.next( );
			if ( !set.contains( next ) ) {
				set.add( next );
				return next;
			}
		}
	}

}
