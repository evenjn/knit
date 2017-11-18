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

import java.util.Vector;

import org.github.evenjn.lang.Tuple;

class VectorTuple<T> implements
		Tuple<T> {

	private Vector<T> vector;

	private int size = 0;

	public VectorTuple(Vector<T> vector) {
		this.vector = vector;
		size = vector.size( );
	}

	@Override
	public T get( int index ) {
		if ( index >= size ) {
			throw new IllegalArgumentException( );
		}
		return vector.get( index );
	}

	@Override
	public int size( ) {
		return size;
	}

}
