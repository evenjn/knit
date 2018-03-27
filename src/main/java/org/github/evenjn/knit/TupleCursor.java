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

import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;
import org.github.evenjn.yarn.Tuple;

class TupleCursor<I> implements
		Cursor<I> {

	private final Tuple<I> tuple;

	private int i = 0;

	TupleCursor(Tuple<I> tuple) {
		this.tuple = tuple;
	}

	@Override
	public I next( )
			throws EndOfCursorException {
		if ( i >= tuple.size( ) ) {
			throw EndOfCursorException.neo( );
		}
		I result = tuple.get( i );
		i++;
		return result;
	}

}
