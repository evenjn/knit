/**
 *
 * Copyright 2017 Marco Trevisan
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

class NumberedCursor<I> implements
		Cursor<Numbered<I>> {

	private final Cursor<I> wrapped;

	NumberedCursor(Cursor<I> cursor) {
		wrapped = cursor;
	}

	private int i;

	@Override
	public Numbered<I> next( )
			throws EndOfCursorException {
		I next = wrapped.next( );
		return new Numbered<I>( next, i++ );
	}

}
