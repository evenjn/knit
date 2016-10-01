/**
 *
 * Copyright 2016 Marco Trevisan
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

import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.PastTheEndException;

class Subcursor<T> implements
		Cursor<T> {

	private Cursor<T> wrapped;

	private int start;

	private int length;

	private int i = 0;

	private Subcursor(Cursor<T> wrapped, int start, int length) {
		this.wrapped = wrapped;
		this.start = start;
		this.length = length;
	}

	static <T> Subcursor<T> headless( Cursor<T> wrapped, int start ) {
		return new Subcursor<>( wrapped, start, -1 );
	}

	static <T> Subcursor<T> head( Cursor<T> wrapped, int limit ) {
		return new Subcursor<>( wrapped, 0, limit );
	}

	static <T> Subcursor<T> sub( Cursor<T> wrapped, int start, int limit ) {
		return new Subcursor<>( wrapped, start, limit );
	}

	@Override
	public T next( )
			throws PastTheEndException {
		while ( i < start ) {
			wrapped.next( );
			i++;
		}
		if ( length >= 0 && i >= start + length ) {
			throw PastTheEndException.neo;
		}
		T next = wrapped.next( );
		i++;
		return next;
	}

}
