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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.github.evenjn.yarn.ArrayMap;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.CursableMap;
import org.github.evenjn.yarn.CursableMapH;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.CursorMap;
import org.github.evenjn.yarn.CursorMapH;
import org.github.evenjn.yarn.CursorUnfoldH;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.IteratorUnfoldH;
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

	public static <K> KnittingItterator<K> wrap( Stream<K> to_wrap ) {
		return wrap( to_wrap.iterator( ) );
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
	 * @return the concatenation of this itterator an the argument itterator.
	 */
	public KnittingItterator<I> chain( Itterator<I> other ) {

		Itterator<I> chained = new Itterator<I>( ) {

			Itterator<I> current = wrapped;

			@Override
			public I next( )
					throws PastTheEndException {
				if ( current == null ) {
					throw PastTheEndException.neo;
				}
				try {
					return current.next( );
				}
				catch ( PastTheEndException t ) {
					if ( current == wrapped ) {
						current = other;
						return current.next( );
					}
					else {
						current = null;
						throw PastTheEndException.neo;
					}
				}
			}
		};
		return wrap( chained );
	}

	public <K extends Collection<? super I>> K collect( K collection ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			try {
				for ( ;; ) {
					collection.add( wrapped.next( ) );
				}
			}
			catch ( PastTheEndException e ) {
			}
			return collection;
		}
	}

	public void consume( ) {
		try {
			for ( ;; ) {
				wrapped.next( );
			}
		}
		catch ( PastTheEndException e ) {
		}
	}

	public <K extends Consumer<I>> K consume( K consumer ) {
		try {
			for ( I next = wrapped.next( );; next = wrapped.next( ) ) {
				consumer.accept( next );
			}
		}
		catch ( PastTheEndException e ) {
		}
		return consumer;
	}

	/**
	 * 
	 * @return an itterator that scrolls over this and the other in parallel, each
	 *         time applying the bifunction on the result of the two elements, and
	 *         returning in output the application result.
	 */
	public <R, M> KnittingItterator<M> entwine(
			Itterator<R> other,
			BiFunction<I, R, M> bifunction ) {
		Itterator<I> outer = this;
		Itterator<M> result = new Itterator<M>( ) {

			@Override
			public M next( )
					throws PastTheEndException {
				return bifunction.apply( outer.next( ), other.next( ) );
			}
		};
		return wrap( result );
	}

	/**
	 * @param predicate
	 *          A system that decides to keep or to discard elements.
	 * @return An itterator to access the only the elements of this itterator that
	 *         are not discarded by the predicate.
	 */
	public KnittingItterator<I> filter( Predicate<? super I> predicate ) {
		IteratorUnfoldH<I, I> internal_stitch = new IteratorUnfoldH<I, I>( ) {

			@Override
			public Iterator<I> next( Hook hook, I input ) {
				if ( predicate.test( input ) ) {
					return new SingletonIterator<I>( input );
				}
				return null;
			}
		};
		return wrap( new ItteratorStitchProcessor<I, I>(
				wrapped,
				internal_stitch ) );
	}

	public <O> KnittingItterator<O> flatmapArray( ArrayMap<? super I, O> stitch ) {
		IteratorUnfoldH<I, O> internal_stitch = new IteratorUnfoldH<I, O>( ) {

			@Override
			public Iterator<O> next( Hook hook, I input ) {
				O[] nextArray = stitch.get( input );
				return nextArray == null ? null : new ArrayIterator<>( nextArray );
			}
		};
		return wrap( new ItteratorStitchProcessor<I, O>( wrapped, internal_stitch ) );
	}

	public <O> KnittingCursor<O> flatmapCursable( CursableMap<? super I, O> stitch ) {
		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Cursable<O> cursable = stitch.get( input );
				return cursable == null ? null : cursable.pull( hook );
			}

		};
		return KnittingCursor.wrap( new CursorStitchProcessor<I, O>( wrapped, internal_stitch ) );
	}

	public <O> KnittingCursor<O> flatmapCursable(
			Hook hook,
			CursableMapH<? super I, O> stitch ) {
		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Cursable<O> cursable = stitch.get( hook, input );
				return cursable == null ? null : cursable.pull( hook );
			}
		};
		return KnittingCursor.wrap( new CursorStitchProcessor<I, O>( hook, wrapped,
				internal_stitch ) );
	}
	
	public <O> KnittingCursor<O> flatmapCursor( CursorMap<? super I, O> stitch ) {
		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return stitch.get( input );
			}

		};
		return KnittingCursor.wrap( new CursorStitchProcessor<I, O>( wrapped, internal_stitch ) );
	}

	public <O> KnittingCursor<O> flatmapCursor(
			Hook hook,
			CursorMapH<? super I, O> stitch ) {
		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return stitch.get( hook, input );
			}

		};
		return KnittingCursor.wrap( new CursorStitchProcessor<I, O>( hook, wrapped,
				internal_stitch ) );
	}
	
	/**
	 * Stops as soon as one of the sources is depleted.
	 * 
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

	public int size( ) {
		int size = 0;
		try {
			for ( ;; ) {
				wrapped.next( );
				size++;
			}
		}
		catch ( PastTheEndException e ) {
		}
		return size;
	}
}
