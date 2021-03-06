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

import org.github.evenjn.yarn.Tuple;

class Subtuple<K> implements
		Tuple<K> {

	private final Tuple<K> tuple;

	private final int start;

	private final int length;

	public Subtuple(Tuple<K> tuple, int start, int length) {
		if ( length < 0 || start < 0 ) {
			throw new IllegalArgumentException( );
		}
		this.tuple = tuple;
		int max = tuple.size( );
		if ( start >= max ) {
			this.start = 0;
			this.length = 0;
		} else {
			this.start = start;
			if ( max < start + length ) {
				this.length = max - start;
			} else {
				this.length = length;
			}
		}
	}

	@Override
	public K get( int t ) {
		if ( t < 0 || t >= length )
			throw new IllegalArgumentException( );
		return tuple.get( start + t );
	}

	@Override
	public int size( ) {
		return length;
	}
}
