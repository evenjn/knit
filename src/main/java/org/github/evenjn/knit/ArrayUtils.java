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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.github.evenjn.lang.Rook;
import org.github.evenjn.yarn.Tuple;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;

class ArrayCursor<T> implements
		Cursor<T> {

	private final T[] array;

	private int i = 0;

	public ArrayCursor(T[] array) {
		this.array = array;
	}

	@Override
	public T next( )
			throws EndOfCursorException {
		if ( i < array.length ) {
			return array[i++];
		}
		throw EndOfCursorException.neo();
	}
}

class ArrayCursable<T> implements
		Cursable<T> {

	private final T[] array;

	public ArrayCursable(T[] array) {
		this.array = array;
	}

	@Override
	public Cursor<T> pull( Rook rook ) {
		return new ArrayCursor<>( array );
	}
}

class ArrayIterator<T> implements
		Iterator<T> {

	private final T[] array;

	private int i = 0;

	public ArrayIterator(T[] array) {
		this.array = array;
	}

	@Override
	public T next( ) {
		if ( i < array.length ) {
			return array[i++];
		}
		throw new NoSuchElementException( );
	}

	@Override
	public boolean hasNext( ) {
		return i < array.length;
	}
}

class ArrayIterable<T> implements
		Iterable<T> {

	private final T[] array;

	public ArrayIterable(T[] array) {
		this.array = array;
	}

	@Override
	public Iterator<T> iterator( ) {
		return new ArrayIterator<>( array );
	}
}

class ArrayTuple<T> implements
		Tuple<T> {

	private final T[] array;

	public ArrayTuple(T[] array) {
		this.array = array;
	}

	@Override
	public T get( int index ) {
		return array[index];
	}

	@Override
	public int size( ) {
		return array.length;
	}

}
