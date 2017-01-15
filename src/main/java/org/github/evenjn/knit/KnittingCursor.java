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
import java.util.Optional;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.github.evenjn.yarn.ArrayMap;
import org.github.evenjn.yarn.ArrayUnfold;
import org.github.evenjn.yarn.AutoHook;
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
import org.github.evenjn.yarn.OptionalMap;
import org.github.evenjn.yarn.OptionalMapH;
import org.github.evenjn.yarn.PastTheEndException;
import org.github.evenjn.yarn.SkipException;
import org.github.evenjn.yarn.SkipFold;
import org.github.evenjn.yarn.SkipFoldFactory;
import org.github.evenjn.yarn.SkipFoldH;
import org.github.evenjn.yarn.SkipFoldHFactory;
import org.github.evenjn.yarn.SkipMap;
import org.github.evenjn.yarn.SkipMapH;
import org.github.evenjn.yarn.StreamMapH;
import org.github.evenjn.yarn.StreamUnfoldH;
import org.github.evenjn.yarn.Tuple;

/**
 * <h1>KnittingCursor</h1>
 * <p>
 * A KnittingCursor wraps a cursor and provides utility methods to access its
 * contents.
 * </p>
 * 
 * 
 * 
 * <h2>Pristine state</h2>
 * 
 * <p>
 * Every cursor is in pristine state when created, and remains in pristine state
 * until the first invocation of {@code next()}, {@code hasNext()} or any
 * terminal operations carried out on it or on its views. Moreover, an
 * invocation of {@code iterator()} on the object returned by {@code once()}
 * also causes the cursor to leave the pristine state.
 * </p>
 * 
 * </p>
 * Invocation of certain methods on a cursor that is not in pristine state will
 * throw an {@code IllegalStateException}.
 * </p>
 * 
 * <h2>Terminal operations</h2>
 * 
 * <p>
 * Terminal operations are a group of methods that may be invoked only if the
 * cursor is in pristine state. Terminal operations are marked as such in the
 * documentation.
 * </p>
 *
 * @param <I>
 *          The type of elements accessible via this cursor.
 * @see org.github.evenjn.knit
 * @since 1.0
 */
public class KnittingCursor<I> implements
		Cursor<I> {

	private final Cursor<I> wrapped;

	private int so_far = 0;

	private I cached = null;

	private boolean is_cached = false;

	private boolean once = true;

	private KnittingCursor(Cursor<I> to_wrap) {
		this.wrapped = to_wrap;
	}

	private void failWhenDirty( ) {
		if ( !once || so_far != 0 ) {
			throw new IllegalStateException( "This cursor has already been used." );
		}
	}

	@Override
	public String toString( ) {
		return "A knitting cursor.";
	}

	/**
	 * <p>
	 * Returns a view of the concatenation of the argument cursor after this
	 * cursor.
	 * </p>
	 * 
	 * <p>
	 * This operation preserves the pristine state.
	 * </p>
	 * 
	 * @param tail
	 *          The cursor to concatenate after this.
	 * @return A view of the concatenation of the argument cursor after this
	 *         cursor.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<I> chain( Cursor<I> tail )
			throws IllegalStateException {
		failWhenDirty( );
		final KnittingCursor<I> outer_this = this;

		Cursor<I> chained = new Cursor<I>( ) {

			Cursor<I> current = outer_this;

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
					if ( current == outer_this ) {
						current = tail;
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
	 * <p>
	 * Adds all elements of this cursor in the argument collection, then returns
	 * it.
	 * </p>
	 * 
	 * <p>
	 * This is a terminal operation.
	 * </p>
	 * 
	 * @param collection
	 *          The collection to add elements to.
	 * @return The argument collection.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <K extends Collection<? super I>> K collect( K collection )
			throws IllegalStateException {
		failWhenDirty( );
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			try {
				for ( ;; ) {
					collection.add( next( ) );
				}
			}
			catch ( PastTheEndException e ) {
			}
			return collection;
		}
	}

	/**
	 * <p>
	 * Invokes {@code next()} until a {@code PastTheEndException} is thrown.
	 * </p>
	 * 
	 * <p>
	 * This is a terminal operation.
	 * </p>
	 * 
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public void consume( )
			throws IllegalStateException {
		failWhenDirty( );
		try {
			for ( ;; ) {
				next( );
			}
		}
		catch ( PastTheEndException e ) {
		}
	}

	/**
	 * <p>
	 * Feeds a consumer with the elements of this cursor.
	 * </p>
	 * <p>
	 * Obtains a consumer from the argument {@code consumer_provider}, hooking it
	 * to a local, temporary hook. Then invokes {@link #next() next()} until a
	 * {@link org.github.evenjn.yarn.PastTheEndException PastTheEndException} is
	 * thrown, passing each resulting object to the consumer.
	 * </p>
	 * 
	 * <p>
	 * This is a terminal operation.
	 * </p>
	 * 
	 * @param consumer_provider
	 *          A system that provides hooked consumers.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <K extends Consumer<? super I>> void consume(
			Function<Hook, K> consumer_provider )
			throws IllegalStateException {
		failWhenDirty( );
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			K consumer = consumer_provider.apply( hook );
			for ( ;; ) {
				consumer.accept( next( ) );
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
			BiFunction<I, R, M> bifunction )
			throws IllegalStateException {
		failWhenDirty( );
		final KnittingCursor<I> outer_this = this;
		Cursor<M> result = new Cursor<M>( ) {

			@Override
			public M next( )
					throws PastTheEndException {
				return bifunction.apply( outer_this.next( ), other.next( ) );
			}
		};
		return wrap( result );
	}

	/**
	 * <p>
	 * Returns a view hiding the elements which do not satisfy the argument
	 * {@code predicate} in this cursor.
	 * </p>
	 * 
	 * <p>
	 * This operation preserves the pristine state.
	 * </p>
	 * 
	 * @param predicate
	 *          A system that decides to show or hide elements.
	 * @return A view hiding the elements which do not satisfy the argument
	 *         {@code stateless_predicate} in this cursor.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<I> filter( Predicate<? super I> predicate )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, I> stitch = new CursorUnfoldH<I, I>( ) {

			@Override
			public Cursor<I> next( Hook hook, I input ) {
				return predicate.test( input ) ? new SingletonCursor<I>( input ) : null;
			}
		};
		return wrap( new CursorStitchProcessor<I, I>( this, stitch ) );
	}

	public <O> KnittingCursor<O>
			flatmapArray( ArrayMap<? super I, O> array_map )
					throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				O[] nextArray = array_map.get( input );
				return nextArray == null ? null : new ArrayCursor<>( nextArray );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( this, stitch ) );
	}

	public <O> KnittingCursor<O> flatmapCursable(
			CursableMap<? super I, O> cursable_map )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Cursable<O> cursable = cursable_map.get( input );
				return cursable == null ? null : cursable.pull( hook );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( this, stitch ) );
	}

	public <O> KnittingCursor<O> flatmapCursable(
			Hook hook,
			CursableMapH<? super I, O> cursable_map_h )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Cursable<O> cursable = cursable_map_h.get( hook, input );
				return cursable == null ? null : cursable.pull( hook );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, this, stitch ) );
	}

	public <O> KnittingCursor<O> flatmapCursor(
			CursorMap<? super I, O> cursor_map )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return cursor_map.get( input );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( this, stitch ) );
	}

	public <O> KnittingCursor<O> flatmapCursor(
			Hook hook,
			CursorMapH<? super I, O> cursor_map_h )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return cursor_map_h.get( hook, input );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( hook, this, stitch ) );
	}

	public <O> KnittingCursor<O> flatmapIterable(
			Hook hook,
			IterableMapH<? super I, O> iterable_map_h )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Iterable<O> nextIterable = iterable_map_h.get( hook, input );
				return nextIterable == null ? null : new IteratorCursor<>(
						nextIterable.iterator( ) );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, this, stitch ) );
	}

	public <O> KnittingCursor<O> flatmapIterable(
			IterableMap<? super I, O> iterable_map )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Iterable<O> nextIterable = iterable_map.get( input );
				return nextIterable == null ? null : new IteratorCursor<>(
						nextIterable.iterator( ) );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( this, stitch ) );
	}

	public <O> KnittingCursor<O> flatmapIterator(
			Hook hook,
			IteratorMapH<? super I, O> iterator_map_h )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Iterator<O> nextIterator = iterator_map_h.get( hook, input );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, this, stitch ) );
	}

	public <O> KnittingCursor<O>
			flatmapIterator( IteratorMap<? super I, O> iterator_map )
					throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Iterator<O> nextIterator = iterator_map.get( input );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( this, stitch ) );
	}

	public <O> KnittingCursor<O> flatmapOptional(
			OptionalMap<? super I, O> optional_map )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Optional<O> optional = optional_map.get( input );
				if ( optional.isPresent( ) ) {
					return new SingletonCursor<O>( optional.get( ) );
				}
				return null;
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( this, stitch ) );
	}

	public <O> KnittingCursor<O> flatmapOptional( Hook hook,
			OptionalMapH<? super I, O> optional_map_h )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Optional<O> optional = optional_map_h.get( hook, input );
				if ( optional.isPresent( ) ) {
					return new SingletonCursor<O>( optional.get( ) );
				}
				return null;
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, this, stitch ) );
	}

	public <O> KnittingCursor<O> flatmapStream(
			Hook hook,
			StreamMapH<? super I, O> stream_map_h )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Stream<O> nextStream = stream_map_h.get( hook, input );
				return nextStream == null ? null : new IteratorCursor<>(
						nextStream.iterator( ) );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( hook, this, stitch ) );
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
	 * <p>
	 * Returns a view showing the first {@code show} elements visible after hiding
	 * the first {@code hide} elements.
	 * </p>
	 * 
	 * <p>
	 * The returned view may be empty. This happens when this cursor's size is
	 * smaller than {@code hide}.
	 * </p>
	 * 
	 * <p>
	 * The returned view may contain less than {@code show} elements. This happens
	 * when this cursor's size is smaller than {@code hide + show}.
	 * </p>
	 * 
	 * <p>
	 * This operation preserves the pristine state.
	 * </p>
	 *
	 * @param hide
	 *          The number of elements to hide. A negative numbers counts as zero.
	 * @param show
	 *          The number of elements to show. A negative numbers counts as zero.
	 * @return A view showing the first {@code show} elements visible after hiding
	 *         the first {@code hide} elements in this cursor.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<I> head( int hide, int show )
			throws IllegalStateException {
		failWhenDirty( );
		int final_show = show < 0 ? 0 : show;
		int final_hide = hide < 0 ? 0 : hide;
		return wrap( Subcursor.sub( this, final_hide, final_show ) );
	}

	/**
	 * <p>
	 * Returns a view hiding the first {@code hide} elements in this cursor.
	 * </p>
	 * 
	 * <p>
	 * The returned view may be empty. This happens when this cursor's size is
	 * smaller than {@code hide}.
	 * </p>
	 * 
	 * <p>
	 * This operation preserves the pristine state.
	 * </p>
	 * 
	 * @param hide
	 *          The number of elements to hide. A negative numbers counts as zero.
	 * @return A view hiding the first {@code hide} elements in this cursor.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<I> headless( int hide )
			throws IllegalStateException {
		failWhenDirty( );
		int final_hide = hide < 0 ? 0 : hide;
		return wrap( Subcursor.skip( this, final_hide ) );
	}

	public <O> KnittingCursor<O> map( Function<? super I, O> function )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return new SingletonCursor<O>( function.apply( input ) );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( this, stitch ) );
	}

	public <O> KnittingCursor<O>
			map( Hook hook, FunctionH<? super I, O> function )
					throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return new SingletonCursor<O>( function.get( hook, input ) );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( hook, this, stitch ) );
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

	public KnittingCursor<Bi<Integer, I>> numbered( )
			throws IllegalStateException {
		failWhenDirty( );
		final KnittingCursor<I> outer_this = this;
		Bi<Integer, I> bi = Bi.nu( null, null );
		return wrap( new Cursor<Bi<Integer, I>>( ) {

			@Override
			public Bi<Integer, I> next( )
					throws PastTheEndException {
				return bi.set( soFar( ), outer_this.next( ) );
			}
		} );
	}

	public Iterable<I> once( )
			throws IllegalStateException {
		failWhenDirty( );
		final KnittingCursor<I> outer_this = this;
		return new Iterable<I>( ) {

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
	 * throws IllegalStateException when it not the case that there is exactly one
	 * element.
	 */
	public I one( )
			throws IllegalStateException {
		failWhenDirty( );
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

	/**
	 * This is a terminal operation.
	 * 
	 * returns an empty optionsl when it not the case that there is exactly one
	 * element.
	 */
	public Optional<I> optionalOne( )
			throws IllegalStateException {
		failWhenDirty( );
		try {
			I result = next( );
			if ( hasNext( ) ) {
				return Optional.empty( );
			}
			return Optional.of( result );
		}
		catch ( PastTheEndException e ) {
			return Optional.empty( );
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
		consume( );
		return so_far;
	}

	public <O> KnittingCursor<O> skipfold( SkipFold<? super I, O> skip_fold )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				try {
					return new SingletonCursor<O>( skip_fold.next( input ) );
				}
				catch ( SkipException e ) {
					return null;
				}
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				try {
					return new SingletonCursor<O>( skip_fold.end( ) );
				}
				catch ( SkipException e ) {
					return null;
				}
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( this, stitch ) );
	}

	public <O> KnittingCursor<O> skipfold( Hook hook,
			SkipFoldH<? super I, O> skip_fold_h )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				try {
					return new SingletonCursor<O>( skip_fold_h.next( hook, input ) );
				}
				catch ( SkipException e ) {
					return null;
				}
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				try {
					return new SingletonCursor<O>( skip_fold_h.end( hook ) );
				}
				catch ( SkipException e ) {
					return null;
				}
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, this, stitch ) );
	}

	public <O> KnittingCursor<O>
			skipfold( SkipFoldFactory<? super I, O> factory )
					throws IllegalStateException {
		failWhenDirty( );
		return skipfold( factory.create( ) );
	}

	public <O> KnittingCursor<O> skipfold( Hook hook,
			SkipFoldHFactory<? super I, O> factory )
			throws IllegalStateException {
		failWhenDirty( );
		return skipfold( hook, factory.create( ) );
	}

	public <O> KnittingCursor<O> skipmap( SkipMap<? super I, O> skip_map )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				try {
					return new SingletonCursor<O>( skip_map.get( input ) );
				}
				catch ( SkipException e ) {
					return null;
				}
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( this, stitch ) );
	}

	public <O> KnittingCursor<O> skipmap( Hook hook,
			SkipMapH<? super I, O> skip_map_h )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				try {
					return new SingletonCursor<O>( skip_map_h.get( hook, input ) );
				}
				catch ( SkipException e ) {
					return null;
				}
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( hook, this, stitch ) );
	}

	public int soFar( ) {
		return so_far;
	}

	public KnittingCursor<KnittingCursor<I>> split( Predicate<I> predicate )
			throws IllegalStateException {
		failWhenDirty( );
		return KnittingCursor.wrap( new SplitCursor<>( this, predicate ) );
	}

	public KnittingCursor<I> tap( Consumer<? super I> consumer )
			throws IllegalStateException {
		failWhenDirty( );
		KnittingCursor<I> outer_this = this;
		return wrap( new Cursor<I>( ) {

			@Override
			public I next( )
					throws PastTheEndException {
				I next = outer_this.next( );
				consumer.accept( next );
				return next;
			}
		} );
	}

	public <O> KnittingCursor<O> unfoldArray(
			ArrayUnfold<? super I, O> array_unfold )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				O[] array = array_unfold.next( input );
				return array == null ? null : new ArrayCursor<>( array );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				O[] array = array_unfold.end( );
				return array == null ? null : new ArrayCursor<>( array );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( this, stitch ) );
	}

	public <O> KnittingCursor<O> unfoldCursable(
			CursableUnfold<? super I, O> cursable_unfold )
			throws IllegalStateException {
		failWhenDirty( );

		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Cursable<O> nextCursable = cursable_unfold.next( input );
				return nextCursable == null ? null : nextCursable.pull( hook );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Cursable<O> nextCursable = cursable_unfold.end( );
				return nextCursable == null ? null : nextCursable.pull( hook );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( this, stitch ) );
	}

	public <O> KnittingCursor<O> unfoldCursable(
			Hook hook,
			CursableUnfoldH<? super I, O> cursable_unfold_h )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Cursable<O> nextCursable = cursable_unfold_h.next( hook, input );
				return nextCursable == null ? null : nextCursable.pull( hook );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Cursable<O> nextCursable = cursable_unfold_h.end( hook );
				return nextCursable == null ? null : nextCursable.pull( hook );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, this, stitch ) );
	}

	public <O> KnittingCursor<O> unfoldCursor(
			CursorUnfold<? super I, O> cursor_unfold )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return cursor_unfold.next( input );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return cursor_unfold.end( );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( this, stitch ) );
	}

	public <O> KnittingCursor<O> unfoldCursor(
			Hook hook,
			CursorUnfoldH<? super I, O> cursor_unfold_h )
			throws IllegalStateException {
		failWhenDirty( );
		return wrap(
				new CursorStitchProcessor<I, O>( hook, this, cursor_unfold_h ) );
	}

	public <O> KnittingCursor<O> unfoldIterable(
			IterableUnfold<? super I, O> iterable_unfold )
			throws IllegalStateException {
		failWhenDirty( );

		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Iterable<O> nextIterable = iterable_unfold.next( input );
				return new IteratorCursor<>( nextIterable.iterator( ) );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Iterable<O> nextIterable = iterable_unfold.end( );
				return new IteratorCursor<>( nextIterable.iterator( ) );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( this, stitch ) );
	}

	public <O> KnittingCursor<O> unfoldIterable(
			Hook hook,
			IterableUnfoldH<? super I, O> iterable_unfold_h )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Iterable<O> nextIterable = iterable_unfold_h.next( hook, input );
				return nextIterable == null ? null : new IteratorCursor<>(
						nextIterable.iterator( ) );
			}

			public Cursor<O> end( Hook hook ) {
				final Iterable<O> nextIterable = iterable_unfold_h.end( hook );
				return nextIterable == null ? null : new IteratorCursor<>(
						nextIterable.iterator( ) );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( hook, this, stitch ) );
	}

	public <O> KnittingCursor<O> unfoldIterator(
			IteratorUnfold<? super I, O> iterator_unfold )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Iterator<O> nextIterator = iterator_unfold.next( input );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Iterator<O> nextIterator = iterator_unfold.end( );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( this, stitch ) );
	}

	public <O> KnittingCursor<O> unfoldIterator(
			Hook hook,
			IteratorUnfoldH<? super I, O> iterator_unfold_h )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Iterator<O> nextIterator = iterator_unfold_h.next( hook, input );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Iterator<O> nextIterator = iterator_unfold_h.end( hook );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, this, stitch ) );
	}

	public <O> KnittingCursor<O> unfoldStream(
			Hook hook,
			StreamUnfoldH<? super I, O> stream_unfold_h )
			throws IllegalStateException {
		failWhenDirty( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Stream<O> nextStream = stream_unfold_h.next( hook, input );
				return nextStream == null ? null : new IteratorCursor<>(
						nextStream.iterator( ) );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Stream<O> nextStream = stream_unfold_h.end( hook );
				return nextStream == null ? null : new IteratorCursor<>(
						nextStream.iterator( ) );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, this, stitch ) );
	}

	/**
	 * <p>
	 * Returns an empty cursor.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of elements in the empty cursor.
	 * @return An empty cursor.
	 * 
	 * @since 1.0
	 */
	public static <K> KnittingCursor<K> empty( ) {
		return private_empty( );
	}

	@SuppressWarnings("unchecked")
	private static <K> KnittingCursor<K> private_empty( ) {
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
	@SafeVarargs
	public static <T> KnittingCursor<T> blend(
			Cursor<Integer> selector,
			Cursor<T> ... sources ) {
		Vector<Cursor<T>> defensive_sources = new Vector<>( );
		for ( int i = 0; i < sources.length; i++ ) {
			defensive_sources.add( sources[i] );
		}
		return wrap( new Cursor<T>( ) {

			@Override
			public T next( )
					throws PastTheEndException {
				Integer index = selector.next( );
				Cursor<T> iterator = defensive_sources.get( index );
				return iterator.next( );
			}
		} );
	}

	@SafeVarargs
	public static <K> KnittingCursor<K> on( K ... elements ) {
		return wrap( new ArrayCursor<K>( elements ) );
	}

	public static <K> KnittingCursor<K> wrap( Tuple<K> tuple ) {
		return new KnittingCursor<>( new Cursor<K>( ) {

			private int i = 0;

			@Override
			public K next( )
					throws PastTheEndException {
				if ( i >= tuple.size( ) ) {
					throw PastTheEndException.neo;
				}
				return tuple.get( i++ );
			}
		} );
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
