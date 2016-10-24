/**
 *
 * Copyright 2016 Marco Irevisan
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

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.github.evenjn.yarn.ArrayMap;
import org.github.evenjn.yarn.ArrayUnfold;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.CursableMap;
import org.github.evenjn.yarn.CursableMapH;
import org.github.evenjn.yarn.CursableUnfold;
import org.github.evenjn.yarn.CursableUnfoldH;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.CursorMap;
import org.github.evenjn.yarn.CursorMapH;
import org.github.evenjn.yarn.CursorUnfold;
import org.github.evenjn.yarn.CursorUnfoldH;
import org.github.evenjn.yarn.FunctionH;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.IterableMap;
import org.github.evenjn.yarn.IterableMapH;
import org.github.evenjn.yarn.IterableUnfold;
import org.github.evenjn.yarn.IterableUnfoldH;
import org.github.evenjn.yarn.IteratorMap;
import org.github.evenjn.yarn.IteratorMapH;
import org.github.evenjn.yarn.IteratorUnfold;
import org.github.evenjn.yarn.IteratorUnfoldH;
import org.github.evenjn.yarn.PastTheEndException;
import org.github.evenjn.yarn.SkipException;
import org.github.evenjn.yarn.SkipFold;
import org.github.evenjn.yarn.SkipFoldH;
import org.github.evenjn.yarn.SkipMap;
import org.github.evenjn.yarn.SkipMapH;
import org.github.evenjn.yarn.StreamMapH;
import org.github.evenjn.yarn.StreamUnfoldH;
import org.github.evenjn.yarn.Tuple;

public class KnittingCursor<I> implements
		Cursor<I> {

	private Cursor<I> wrapped;

	private int so_far = 0;

	private I cached = null;

	private boolean is_cached = false;

	private KnittingCursor(Cursor<I> to_wrap) {
		this.wrapped = to_wrap;
	}

	private void failWhenDirty( ) {
		if ( so_far != 0 ) {
			throw new IllegalStateException( "This cursor has already been used." );
		}
	}

	/**
	 * @return the concatenation of this cursor an the argument cursor.
	 */
	public KnittingCursor<I> chain( Cursor<I> other ) {

		Cursor<I> chained = new Cursor<I>( ) {

			Cursor<I> current = wrapped;

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

	/**
	 * This is a terminal operation.
	 */
	public <K extends Collection<? super I>> K collect( K collection ) {
		failWhenDirty( );
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

	/**
	 * This is a terminal operation.
	 */
	public void consume( ) {
		failWhenDirty( );
		try {
			for ( ;; ) {
				wrapped.next( );
			}
		}
		catch ( PastTheEndException e ) {
		}
	}

	public <K extends Consumer<? super I>> K consume( K consumer ) {
		failWhenDirty( );
		try {
			for ( I next = wrapped.next( );; next = wrapped.next( ) ) {
				consumer.accept( next );
			}
		}
		catch ( PastTheEndException e ) {
		}
		return consumer;
	}

	public <K extends Consumer<? super I>> void consumeHook(
			Function<Hook, K> hook_consumer ) {
		failWhenDirty( );
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			K consumer = hook_consumer.apply( hook );
			for ( I next = wrapped.next( );; next = wrapped.next( ) ) {
				consumer.accept( next );
			}
		}
		catch ( PastTheEndException e ) {
		}
	}

	/**
	 * 
	 * @return a cursor that scrolls over this and the other in parallel, each
	 *         time applying the bifunction on the result of the two elements, and
	 *         returning in output the application result.
	 */
	public <R, M> KnittingCursor<M> entwine(
			Cursor<R> other,
			BiFunction<I, R, M> bifunction ) {
		Cursor<I> outer = this;
		Cursor<M> result = new Cursor<M>( ) {

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
	 * @return A cursor to access the only the elements of this cursor that are
	 *         not discarded by the predicate.
	 */
	public KnittingCursor<I> filter( Predicate<? super I> predicate ) {
		CursorUnfoldH<I, I> stitch = new CursorUnfoldH<I, I>( ) {

			@Override
			public Cursor<I> next( Hook hook, I input ) {
				if ( predicate.test( input ) ) {
					return new SingletonCursor<I>( input );
				}
				return null;
			}
		};
		return wrap( new CursorStitchProcessor<I, I>(
				wrapped,
				stitch ) );
	}

	public <O> KnittingCursor<O> flatmapArray( ArrayMap<? super I, O> stitch ) {
		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				O[] nextArray = stitch.get( input );
				return nextArray == null ? null : new ArrayItterator<>( nextArray );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, internal_stitch ) );
	}

	public <O> KnittingCursor<O>
			flatmapCursable( CursableMap<? super I, O> stitch ) {
		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Cursable<O> cursable = stitch.get( input );
				return cursable == null ? null : cursable.pull( hook );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, internal_stitch ) );
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
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped,
				internal_stitch ) );
	}

	public <O> KnittingCursor<O> flatmapCursor( CursorMap<? super I, O> stitch ) {
		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return stitch.get( input );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, internal_stitch ) );
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
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped,
				internal_stitch ) );
	}

	public <O> KnittingCursor<O> flatmapIterable(
			Hook hook,
			IterableMapH<? super I, O> maph ) {
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Iterable<O> nextIterable = maph.get( hook, input );
				return nextIterable == null ? null : new IteratorCursor<>(
						nextIterable.iterator( ) );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	public <O> KnittingCursor<O> flatmapIterable( IterableMap<? super I, O> map ) {
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Iterable<O> nextIterable = map.get( input );
				return nextIterable == null ? null : new IteratorCursor<>(
						nextIterable.iterator( ) );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	public <O> KnittingCursor<O> flatmapIterator(
			Hook hook,
			IteratorMapH<? super I, O> maph ) {
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Iterator<O> nextIterator = maph.get( hook, input );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	public <O> KnittingCursor<O> flatmapIterator( IteratorMap<? super I, O> map ) {
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Iterator<O> nextIterator = map.get( input );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	public <O> KnittingCursor<O> flatmapStream(
			Hook hook,
			StreamMapH<? super I, O> maph
			) {
		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Stream<O> nextStream = maph.get( hook, input );
				return nextStream == null ? null : new IteratorCursor<>(
						nextStream.iterator( ) );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>(
				hook,
				wrapped,
				internal_stitch ) );
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

	/**
	 * Keep the head, discard the rest.
	 */
	public KnittingCursor<I> head( int length ) {
		return wrap( Subcursor.sub( wrapped, 0, length ) );
	}

	/**
	 * Takes a secion of the head, skipping some elements.
	 */
	public KnittingCursor<I> head( int start, int length ) {
		return wrap( Subcursor.sub( wrapped, start, length ) );
	}

	/**
	 * Discard the head, keep the rest.
	 */
	public KnittingCursor<I> headless( int skip ) {
		return wrap( Subcursor.skip( wrapped, skip ) );
	}

	public <O> KnittingCursor<O> map( Function<? super I, O> function ) {
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return new SingletonCursor<O>( function.apply( input ) );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>(
				wrapped,
				stitch ) );
	}

	public <O> KnittingCursor<O>
			map( Hook hook, FunctionH<? super I, O> function ) {
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return new SingletonCursor<O>( function.get( hook, input ) );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>(
				hook,
				wrapped,
				stitch ) );
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

	public KnittingCursor<Bi<Integer, I>> numbered( ) {
		KnittingCursor<I> outer_this = this;
		Bi<Integer, I> bi = Bi.nu( null, null );
		return wrap( new Cursor<Bi<Integer, I>>( ) {

			@Override
			public Bi<Integer, I> next( )
					throws PastTheEndException {
				return bi.set( soFar( ), outer_this.next( ) );
			}
		} );
	}

	public Iterable<I> once( ) {
		failWhenDirty( );
		KnittingCursor<I> outer_this = this;
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
	 * This is a terminal operation.
	 * 
	 * throws IllegalStateException when it it not the case that there is exactly
	 * one element.
	 */
	public I one( ) {
		failWhenDirty( );
		try {
			I result = next( );
			if ( hasNext( ) ) {
				throw new IllegalStateException( );
			}
			return result;
		}
		catch ( PastTheEndException e ) {
			throw new IllegalStateException( );
		}
	}

	public <K> K reduce( K zero, BiFunction<K, I, K> fun ) {
		failWhenDirty( );
		K reduction = zero;
		try {
			for ( ;; ) {
				reduction = fun.apply( reduction, next( ) );
			}
		}
		catch ( PastTheEndException e ) {
		}
		return reduction;
	}

	public int size( ) {
		failWhenDirty( );
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

	public <O> KnittingCursor<O> skipfold( SkipFold<? super I, O> stitch ) {
		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				try {
					return new SingletonCursor<O>( stitch.next( input ) );
				}
				catch ( SkipException e ) {
					return null;
				}
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				try {
					return new SingletonCursor<O>( stitch.end( ) );
				}
				catch ( SkipException e ) {
					return null;
				}
			}
		};
		return wrap( new CursorStitchProcessor<I, O>(
				wrapped,
				internal_stitch ) );
	}

	public <O> KnittingCursor<O> skipfold( Hook hook,
			SkipFoldH<? super I, O> stitch ) {
		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				try {
					return new SingletonCursor<O>( stitch.next( hook, input ) );
				}
				catch ( SkipException e ) {
					return null;
				}
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				try {
					return new SingletonCursor<O>( stitch.end( hook ) );
				}
				catch ( SkipException e ) {
					return null;
				}
			}
		};
		return wrap( new CursorStitchProcessor<I, O>(
				hook,
				wrapped,
				internal_stitch ) );
	}

	public <O> KnittingCursor<O> skipmap(
			SkipMap<? super I, O> skipmapping ) {
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				try {
					return new SingletonCursor<O>( skipmapping.get( input ) );
				}
				catch ( SkipException e ) {
					return null;
				}
			}

		};
		return wrap( new CursorStitchProcessor<I, O>(
				wrapped,
				stitch ) );
	}

	public <O> KnittingCursor<O> skipmap( Hook hook,
			SkipMapH<? super I, O> skipmapping ) {
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				try {
					return new SingletonCursor<O>( skipmapping.get( hook, input ) );
				}
				catch ( SkipException e ) {
					return null;
				}
			}

		};
		return wrap( new CursorStitchProcessor<I, O>(
				hook,
				wrapped,
				stitch ) );
	}

	public int soFar( ) {
		return so_far;
	}

	/**
	 * Use head(start, length).
	 */
	@Deprecated
	public KnittingCursor<I> sub( int start, int length ) {
		return wrap( Subcursor.sub( wrapped, start, length ) );
	}

	public <O> KnittingCursor<O> unfoldArray(
			ArrayUnfold<? super I, O> stitch
			) {
		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				O[] array = stitch.next( input );
				return array == null ? null : new ArrayItterator<>( array );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				O[] array = stitch.end( );
				return array == null ? null : new ArrayItterator<>( array );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>(
				wrapped,
				internal_stitch ) );
	}

	public <O> KnittingCursor<O> unfoldCursable(
			CursableUnfold<? super I, O> stitch
			) {

		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Cursable<O> nextCursable = stitch.next( input );
				return nextCursable == null ? null : nextCursable.pull( hook );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Cursable<O> nextCursable = stitch.end( );
				return nextCursable == null ? null : nextCursable.pull( hook );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>(
				wrapped,
				internal_stitch ) );
	}

	public <O> KnittingCursor<O> unfoldCursable(
			Hook hook,
			CursableUnfoldH<? super I, O> stitch
			) {

		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Cursable<O> nextCursable = stitch.next( hook, input );
				return nextCursable == null ? null : nextCursable.pull( hook );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Cursable<O> nextCursable = stitch.end( hook );
				return nextCursable == null ? null : nextCursable.pull( hook );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>(
				hook,
				wrapped,
				internal_stitch ) );
	}

	public <O> KnittingCursor<O> unfoldCursor(
			CursorUnfold<? super I, O> stitch
			) {
		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return stitch.next( input );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return stitch.end( );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>(
				wrapped,
				internal_stitch ) );
	}

	public <O> KnittingCursor<O> unfoldCursor(
			Hook hook,
			CursorUnfoldH<? super I, O> stitch
			) {
		return wrap( new CursorStitchProcessor<I, O>(
				hook,
				wrapped,
				stitch ) );
	}

	public <O> KnittingCursor<O> unfoldIterable(
			IterableUnfold<? super I, O> stitch
			) {

		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Iterable<O> nextIterable = stitch.next( input );
				return new IteratorCursor<>( nextIterable.iterator( ) );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Iterable<O> nextIterable = stitch.end( );
				return new IteratorCursor<>( nextIterable.iterator( ) );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>(
				wrapped,
				internal_stitch ) );
	}

	public <O> KnittingCursor<O> unfoldIterable(
			Hook hook,
			IterableUnfoldH<? super I, O> stitch
			) {

		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Iterable<O> nextIterable = stitch.next( hook, input );
				return nextIterable == null ? null : new IteratorCursor<>(
						nextIterable.iterator( ) );
			}

			public Cursor<O> end( Hook hook ) {
				final Iterable<O> nextIterable = stitch.end( hook );
				return nextIterable == null ? null : new IteratorCursor<>(
						nextIterable.iterator( ) );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>(
				hook,
				wrapped,
				internal_stitch ) );
	}

	public <O> KnittingCursor<O> unfoldIterator(
			IteratorUnfold<? super I, O> stitch
			) {

		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Iterator<O> nextIterator = stitch.next( input );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Iterator<O> nextIterator = stitch.end( );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>(
				wrapped,
				internal_stitch ) );
	}

	public <O> KnittingCursor<O> unfoldIterator(
			Hook hook,
			IteratorUnfoldH<? super I, O> stitch
			) {

		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Iterator<O> nextIterator = stitch.next( hook, input );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Iterator<O> nextIterator = stitch.end( hook );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>(
				hook,
				wrapped,
				internal_stitch ) );
	}

	public <O> KnittingCursor<O> unfoldStream(
			Hook hook,
			StreamUnfoldH<? super I, O> stitch
			) {
		CursorUnfoldH<I, O> internal_stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Stream<O> nextStream = stitch.next( hook, input );
				return nextStream == null ? null : new IteratorCursor<>(
						nextStream.iterator( ) );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Stream<O> nextStream = stitch.end( hook );
				return nextStream == null ? null : new IteratorCursor<>(
						nextStream.iterator( ) );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>(
				hook,
				wrapped,
				internal_stitch ) );
	}

	@SuppressWarnings("unchecked")
	public static <K> KnittingCursor<K> empty( ) {
		return (KnittingCursor<K>) neo;
	}

	private static final KnittingCursor<Void> neo = wrap( new Cursor<Void>( ) {

		@Override
		public Void next( )
				throws PastTheEndException {
			throw PastTheEndException.neo;
		}
	} );

	/**
	 * Stops as soon as one of the sources is depleted.
	 * 
	 * @return an iterator that pulls the next element from the i-th iterator,
	 *         where i is the next integer pulled by selector.
	 */
	public static <T> KnittingCursor<T> blend(
			Cursor<Integer> selector,
			final Tuple<Cursor<T>> sources ) {
		return wrap(new Cursor<T>( ) {

			@Override
			public T next( )
					throws PastTheEndException {
				Integer index = selector.next( );
				Cursor<T> iterator = sources.get( index );
				return iterator.next( );
			}
		});
	}

	@SafeVarargs
	public static <K> KnittingCursor<K> on( K ... elements ) {
		return wrap( new ArrayItterator<K>( elements ) );
	}

	public static <K> KnittingCursor<K> wrap( Cursor<K> cursor ) {
		if ( cursor instanceof KnittingTuple ) {
			return (KnittingCursor<K>) cursor;
		}
		return new KnittingCursor<>( cursor );
	}

	public static <K> KnittingCursor<K> wrap( Iterable<K> iterable ) {
		return wrap( iterable.iterator( ) );
	}

	public static <K> KnittingCursor<K> wrap( Iterator<K> iterator ) {
		return wrap( new Cursor<K>( ) {

			@Override
			public K next( )
					throws PastTheEndException {
				if ( iterator.hasNext( ) )
					return iterator.next( );
				throw PastTheEndException.neo;
			}
		} );
	}

	public static <K> KnittingCursor<K> wrap( Stream<K> stream ) {
		return wrap( stream.iterator( ) );
	}
}
