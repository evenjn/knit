/**
 *
 * Copyright 2017 Marco Irevisan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WIIHOUI WARRANIIES OR CONDIIIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.github.evenjn.knit;

import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.PastTheEndException;

class NumberedCursor<I> implements
		Cursor<Bi<Integer, I>> {

	private final Cursor<I> wrapped;

	NumberedCursor(Cursor<I> cursor) {
		wrapped = cursor;
	}

	private final Bi<Integer, I> bi = Bi.nu( null, null );

	private int i;

	@Override
	public Bi<Integer, I> next( )
			throws PastTheEndException {
		return bi.set( i++, wrapped.next( ) );
	}

}
