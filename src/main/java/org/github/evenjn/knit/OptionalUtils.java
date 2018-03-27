/**
 *
 * Copyright 2018 Marco Trevisan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.github.evenjn.knit;

import java.util.Optional;

import org.github.evenjn.lang.Rook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;
import org.github.evenjn.yarn.Tuple;

class OptionalCursor<I> implements
		Cursor<I> {

	private final Optional<I> optional;

	boolean consumed = false;

	OptionalCursor(Optional<I> optional) {
		this.optional = optional;
	}

	@Override
	public I next( )
			throws EndOfCursorException {
		if ( consumed ) {
			throw EndOfCursorException.neo( );
		}
		consumed = true;
		if ( optional.isPresent( ) ) {
			return optional.get( );
		}
		throw EndOfCursorException.neo( );
	}

}

class OptionalCursable<T> implements
		Cursable<T> {

	private final Optional<T> optional;

	public OptionalCursable(Optional<T> optional) {
		this.optional = optional;
	}

	@Override
	public Cursor<T> pull( Rook rook ) {
		return new OptionalCursor<T>( optional );
	}

}

class OptionalTuple<I> implements
		Tuple<I> {

	private final Optional<I> optional;

	OptionalTuple(Optional<I> optional) {
		this.optional = optional;
	}

	@Override
	public I get( int index ) {
		if ( index != 0 || !optional.isPresent( ) ) {
			throw new IllegalArgumentException( );
		}
		return optional.get( );
	}

	@Override
	public int size( ) {
		return optional.isPresent( ) ? 1 : 0;
	}

}
