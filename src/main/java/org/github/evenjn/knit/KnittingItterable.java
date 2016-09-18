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

import java.util.Collection;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.Itterable;
import org.github.evenjn.yarn.Itterator;
import org.github.evenjn.yarn.PastTheEndException;
import org.github.evenjn.yarn.Tuple;

/**
 * It's fishy.
 */
public class KnittingItterable<I> implements
		Itterable<I> {

	private Itterable<I> wrapped;

	private KnittingItterable(Itterable<I> to_wrap) {
		this.wrapped = to_wrap;
	}
	@SafeVarargs
	public static <K> KnittingItterable<K> on( K ... elements ) {
		Itterable<K> cursable = new Itterable<K>( ) {

			@Override
			public Itterator<K> pull( ) {
				return new ArrayCursor<>(elements);
			}
		};
		return wrap( cursable );
	}

	public static <K> KnittingItterable<K> wrap( Itterable<K> to_wrap ) {
		return new KnittingItterable<K>( to_wrap );
	}

	public static <K> KnittingItterable<K> wrap( Iterable<K> to_wrap ) {
		return new KnittingItterable<K>( new Itterable<K>( ) {

			@Override
			public KnittingItterator<K> pull( ) {
				return KnittingItterator.wrap( to_wrap.iterator( ) );
			}
		} );
	}
	
	/**
	 * @return the concatenation of this itterable an the argument itterable.
	 */
	public KnittingItterable<I> chain( final Itterable<I> other ) {
		KnittingItterable<I> outer = this;
		Itterable<I> result = new Itterable<I>( ) {

			@Override
			public Itterator<I> pull( ) {
				return outer.pull( ).chain( other.pull( ) );
			}
		};
		return wrap( result );
	}

	public void consume( ) {
		pull( ).consume( );
	}

	public <K extends Consumer<I>> K consume( K consumer ) {
		return pull( ).consume( consumer );
	}

	public <K extends Collection<? super I>> K collect( K collection ) {
		return pull( ).collect( collection );
	}

	/**
	 * 
	 * @return an itterable that scrolls over this and the other in parallel, each
	 *         time applying the bifunction on the result of the two elements, and
	 *         returning in output the application result.
	 */
	public <R, M> KnittingItterable<M> entwine(
			Itterable<R> other,
			BiFunction<I, R, M> bifunction ) {
		KnittingItterable<I> outer = this;
		Itterable<M> result = new Itterable<M>( ) {

			@Override
			public Itterator<M> pull( ) {
				return outer.pull( ).entwine( other.pull( ), bifunction );
			}
		};
		return wrap( result );
	}
	
	/**
	 * @param stateless_predicate
	 *          A stateless system that decides to keep or to discard elements.
	 * @return A cursable to access the only the elements of this cursable that
	 *         are not discarded by the predicate.
	 */
	public KnittingItterable<I> filter( Predicate<? super I> stateless_predicate ) {
		return wrap( new Itterable<I>( ) {

			@Override
			public Itterator<I> pull( ) {
				return KnittingItterator.wrap( wrapped.pull( ) )
						.filter( stateless_predicate );
			}
		} );
	}

	@Override
	public KnittingItterator<I> pull( ) {
		return KnittingItterator.wrap( wrapped.pull( ) );
	}

	public Iterable<I> once( ) {
		return pull( ).once( );
	}

	public int size( ) {
			return pull( ).size( );
	}

	/**
	 * Stops as soon as one of the sources is depleted.
	 * 
	 * @return an iterator that pulls the next element from the i-th iterator,
	 *         where i is the next integer pulled by selector.
	 */
	public static <T> Itterable<T> blend( Cursor<Integer> selector,
			final Tuple<Itterable<T>> sources ) {
		return new Itterable<T>( ) {

			@Override
			public Itterator<T> pull( ) {
				Cursor<Integer> selector_pulled = selector;
				Vector<Itterator<T>> sources_vector = new Vector<>( );
				int size = sources.size( );
				for ( int i = 0; i < size; i++ ) {
					sources_vector.add( sources.get( i ).pull( ) );
				}

				Itterator<T> result = new Itterator<T>( ) {

					@Override
					public T next( )
							throws PastTheEndException {
						Integer index = selector_pulled.next( );
						Itterator<T> iterator = sources_vector.get( index );
						return iterator.next( );
					}
				};
				return result;
			}
		};
	}
}
