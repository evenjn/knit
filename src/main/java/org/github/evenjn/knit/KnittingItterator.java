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

import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.Itterator;
import org.github.evenjn.yarn.PastTheEndException;
import org.github.evenjn.yarn.Tuple;

/**
 * It's fishy.
 */
public class KnittingItterator<I> implements
		Itterator<I> {

	private Itterator<I> wrapped;

	private int so_far = 0;

	private I cached = null;

	private boolean is_cached = false;

	private KnittingItterator(Itterator<I> to_wrap) {
		this.wrapped = to_wrap;
	}

	public static <K> KnittingItterator<K> wrap( Itterator<K> to_wrap ) {
		return new KnittingItterator<>( to_wrap );
	}

	public static <K> KnittingItterator<K> wrap( Iterator<K> to_wrap ) {
		return new KnittingItterator<K>( new Itterator<K>( ) {

			@Override
			public K next( )
					throws PastTheEndException {
				if ( to_wrap.hasNext( ) )
					return to_wrap.next( );
				throw PastTheEndException.neo;
			}
		} );
	}

	@Override
	public I next( )
			throws PastTheEndException {
		I result = null;
		if ( is_cached ) {
			is_cached = false;
			result = cached;
		}
		else {
			result = wrapped.next( );
		}
		so_far++;
		return result;
	}

	public boolean hasNext( ) {
		if ( is_cached ) {
			return true;
		}
		try {
			cached = next( );
		}
		catch ( PastTheEndException e ) {
			return false;
		}
		is_cached = true;
		return true;
	}

	public Iterable<I> once( ) {
		KnittingItterator<I> outer_this = this;
		return new Iterable<I>( ) {

			boolean once = true;

			@Override
			public Iterator<I> iterator( ) {
				if ( once ) {
					once = false;
				}
				else {
					throw new IllegalStateException(
							"This method cannot be invoked more than once." );
				}
				return new Iterator<I>( ) {

					@Override
					public boolean hasNext( ) {
						return outer_this.hasNext( );
					}

					@Override
					public I next( ) {
						try {
							return outer_this.next( );
						}
						catch ( PastTheEndException e ) {
							throw new NoSuchElementException( );
						}
					}
				};
			}
		};
	}

	/**
	 * Stops as soon as one of the sources is depleted.
	 * 
	 * @param selector
	 * @param iterators
	 * @return an iterator that pulls the next element from the i-th iterator,
	 *         where i is the next integer pulled by selector.
	 */
	public static <T> Itterator<T> blend( Cursor<Integer> selector,
			final Tuple<Itterator<T>> sources ) {
		return new Itterator<T>( ) {

			@Override
			public T next( )
					throws PastTheEndException {
				Integer index = selector.next( );
				Itterator<T> iterator = sources.get( index );
				return iterator.next( );
			}
		};
	}
}
