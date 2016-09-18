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

import org.github.evenjn.yarn.Itterator;
import org.github.evenjn.yarn.PastTheEndException;

class ArrayCursor<T> implements
		Itterator<T> {

	private T[] array;

	private int i = 0;

	public ArrayCursor(T[] array) {
		this.array = array;
	}

	@Override
	public T next( )
			throws PastTheEndException {
		if ( i < array.length ) {
			return array[i++];
		}
		throw PastTheEndException.neo;
	}

}