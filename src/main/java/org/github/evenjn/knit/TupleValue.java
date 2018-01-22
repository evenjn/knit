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

import org.github.evenjn.lang.Equivalencer;
import org.github.evenjn.yarn.Tuple;

public final class TupleValue<I> implements
		Tuple<I> {

	private Tuple<I> wrapped;

	private int size;

	private int hash_code;

	private Equivalencer<I,Object> equivalencer;

	private static <T> boolean equal_null( T first, T second ) {
		if ( first == null && second == null )
			return true;
		if ( first == null || second == null )
			return false;
		return first.equals( second );
	}

	public TupleValue(Tuple<I> tuple) {
		this( tuple, TupleValue::equal_null );
	}

	public TupleValue(Tuple<I> tuple, Equivalencer<I,Object> equivalencer) {
		this.equivalencer = equivalencer;
		int hashCode = 1;
		this.size = tuple.size( );
		for ( int i = 0; i < size; i++ ) {
			I e = tuple.get( i );
			hashCode = 31 * hashCode + ( e == null ? 0 : e.hashCode( ) );
		}
		this.wrapped = tuple;
		this.hash_code = hashCode;
	}

	@Override
	public int hashCode( ) {
		return hash_code;
	}

	@Override
	public boolean equals( Object other ) {
		if ( other == this )
			return true;
		if ( !( other instanceof Tuple ) )
			return false;
		Tuple<?> o = (Tuple<?>) other;
		if ( size != o.size( ) ) {
			return false;
		}
		for ( int i = 0; i < size; i++ ) {
			I e = wrapped.get( i );
			Object oe = o.get( i );
			if ( !equivalencer.equivalent( e, oe ) ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public I get( int index ) {
		return wrapped.get( index );
	}

	@Override
	public int size( ) {
		return size;
	}
}
