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
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
import org.github.evenjn.yarn.EndOfCursorException;
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
import org.github.evenjn.yarn.OptionFold;
import org.github.evenjn.yarn.OptionFoldFactory;
import org.github.evenjn.yarn.OptionFoldH;
import org.github.evenjn.yarn.OptionFoldHFactory;
import org.github.evenjn.yarn.OptionMap;
import org.github.evenjn.yarn.OptionMapH;
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
 * 
 * <p>
 * A {@code KnittingCursor} wraps a cursor and provides utility methods to
 * access its contents.
 * </p>
 * 
 * <p>
 * Briefly, a {@code KnittingCursor} may be used in one of three ways:
 * </p>
 * 
 * <ul>
 * <li>As a simple Cursor, invoking the
 * {@link org.github.evenjn.yarn.Cursor#next() next()} method;</li>
 * <li>As a resource to be harvested, invoking a terminal method such as
 * {@link #collect(Collection)};</li>
 * <li>As the input to a function, invoking a transformation method such as
 * {@link #map(Function)};</li>
 * </ul>
 * 
 * <p>
 * These three modes of operation are exclusive: a {@code KnittingCursor} may be
 * used in one and only one of those three ways. This protocol is documented in
 * detail below.
 * </p>
 * 
 * <h2>Methods of a KnittingCursor</h2>
 * 
 * <p>
 * Non-static public methods of {@code KnittingCursor} fall into one of the
 * following four categories:
 * </p>
 * 
 * <ul>
 * <li>Object methods (inherited from {@link java.lang.Object Object})</li>
 * <li>Cursor methods ({@link #hasNext()} and {@link #next()})</li>
 * <li>Terminal methods (listed below)</li>
 * <li>Transformation methods (listed below)</li>
 * </ul>
 * 
 * <p>
 * Terminal methods repeatedly invoke the wrapped cursor's
 * {@link org.github.evenjn.yarn.Cursor#next() next()} until the end is reached.
 * The following methods are terminal:
 * </p>
 * 
 * <ul>
 * <li>{@link #collect(Collection)}</li>
 * <li>{@link #consume(Function)}</li>
 * <li>{@link #one()}</li>
 * <li>{@link #optionalOne()}</li>
 * <li>{@link #reduce(Object, BiFunction)}</li>
 * <li>{@link #roll()}</li>
 * </ul>
 * 
 * <p>
 * Transformations are a group of methods that return a new
 * {@code KnittingCursor} object, which provides a new view of the contents of
 * the wrapped cursor. Transformation methods do not invoke the wrapped cursor's
 * {@link org.github.evenjn.yarn.Cursor#next() next()}. The following methods
 * are terminal:
 * </p>
 * 
 * <ul>
 * <li>{@link #asIterator()}</li>
 * <li>{@link #asStream()}</li>
 * <li>{@link #chain(Cursor)}</li>
 * <li>{@link #entwine(Cursor, BiFunction)}</li>
 * <li>{@link #filter(Predicate)}</li>
 * <li>{@link #flatmap(CursorMap)}</li>
 * <li>{@link #flatmap(Hook, CursorMapH)}</li>
 * <li>{@link #flatmapArray(ArrayMap)}</li>
 * <li>{@link #flatmapCursable(CursableMap)}</li>
 * <li>{@link #flatmapCursable(Hook, CursableMapH)}</li>
 * <li>{@link #flatmapCursor(CursorMap)}</li>
 * <li>{@link #flatmapCursor(Hook, CursorMapH)}</li>
 * <li>{@link #flatmapIterable(IterableMap)}</li>
 * <li>{@link #flatmapIterable(Hook, IterableMapH)}</li>
 * <li>{@link #flatmapIterator(IteratorMap)}</li>
 * <li>{@link #flatmapIterator(Hook, IteratorMapH)}</li>
 * <li>{@link #flatmapOptional(OptionMap)}</li>
 * <li>{@link #flatmapOptional(Hook, OptionMapH)}</li>
 * <li>{@link #head(int, int)}</li>
 * <li>{@link #headless(int)}</li>
 * <li>{@link #map(Function)}</li>
 * <li>{@link #map(Hook, FunctionH)}</li>
 * <li>{@link #numbered()}</li>
 * <li>{@link #once()}</li>
 * <li>{@link #optionfold(OptionFold)}</li>
 * <li>{@link #optionfold(OptionFoldFactory)}</li>
 * <li>{@link #optionfold(Hook, OptionFoldH)}</li>
 * <li>{@link #optionfold(Hook, OptionFoldHFactory)}</li>
 * <li>{@link #skipfold(SkipFold)}</li>
 * <li>{@link #skipfold(SkipFoldFactory)}</li>
 * <li>{@link #skipfold(Hook, SkipFoldH)}</li>
 * <li>{@link #skipfold(Hook, SkipFoldHFactory)}</li>
 * <li>{@link #skipmap(SkipMap)}</li>
 * <li>{@link #skipmap(Hook, SkipMapH)}</li>
 * <li>{@link #split(Predicate)}</li>
 * <li>{@link #tap(Consumer)}</li>
 * <li>{@link #unfoldArray(ArrayUnfold)}</li>
 * <li>{@link #unfoldCursable(CursableUnfold)}</li>
 * <li>{@link #unfoldCursable(Hook, CursableUnfoldH)}</li>
 * <li>{@link #unfoldCursor(CursorUnfold)}</li>
 * <li>{@link #unfoldCursor(Hook, CursorUnfoldH)}</li>
 * <li>{@link #unfoldIterable(IterableUnfold)}</li>
 * <li>{@link #unfoldIterable(Hook, IterableUnfoldH)}</li>
 * <li>{@link #unfoldIterator(IteratorUnfold)}</li>
 * <li>{@link #unfoldIterator(Hook, IteratorUnfoldH)}</li>
 * </ul>
 * 
 * <h2>States of a KnittingCursor</h2>
 * 
 * <p>
 * At any point in time, a {@code KnittingCursor} object is in one of three
 * states: pristine, used, or locked.
 * </p>
 * 
 * <h2>Pristine state</h2>
 * 
 * <p>
 * Every {@code KnittingCursor} object is in pristine state when created, and
 * remains in pristine state until the first invocation of any method other than
 * those inherited from {@link java.lang.Object}. After {@code KnittingCursor}
 * object leaves the pristine state, it never returns to the pristine state. Any
 * method may be invoked on a {@code KnittingCursor} object in pristine state.
 * </p>
 * 
 * <h2>Used state</h2>
 * 
 * <p>
 * When {@link #hasNext()} or {@link #next()} is invoked on a
 * {@code KnittingCursor} object in pristine state, the object enters the used
 * state. Transformation methods and terminal methods may not be invoked on an
 * object in used state. Invocation of a transformation or terminal method on a
 * cursor that is in used state will throw an {@code IllegalStateException}.
 * </p>
 * 
 * <h2>Locked state</h2>
 * 
 * <p>
 * When the first transformation or terminal method of a {@code KnittingCursor}
 * object is invoked, the object enters the locked state. Invocation of any
 * method other than those inherited from {@link java.lang.Object} will throw an
 * {@code IllegalStateException}.
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

	private I cached = null;

	private boolean is_cached = false;

	private boolean used = false;

	private boolean locked = false;

	private KnittingCursor(Cursor<I> to_wrap) {
		this.wrapped = to_wrap;
	}

	private void lock( ) {
		if ( used || locked ) {
			throw new IllegalStateException(
					"This cursor is not in pristine state." );
		}
		locked = true;
	}

	public Iterable<I> once( )
			throws IllegalStateException {
		lock( );
		return private_once( );
	}

	public Iterator<I> asIterator( )
			throws IllegalStateException {
		lock( );
		return private_once( ).iterator( );
	}

	public Stream<I> asStream( ) {
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize( new Iterator<I>( ) {

					KnittingCursor<I> kc = KnittingCursor.wrap( wrapped );

					@Override
					public boolean hasNext( ) {
						return kc.hasNext( );
					}

					@Override
					public I next( ) {
						try {
							return kc.next( );
						}
						catch ( EndOfCursorException e ) {
							throw new NoSuchElementException( );
						}
					}
				}, Spliterator.ORDERED ),
				false );
	}

	private Iterable<I> private_once( )
			throws IllegalStateException {
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

					KnittingCursor<I> kc = KnittingCursor.wrap( wrapped );

					@Override
					public boolean hasNext( ) {
						return kc.hasNext( );
					}

					@Override
					public I next( ) {
						try {
							return kc.next( );
						}
						catch ( EndOfCursorException e ) {
							throw new NoSuchElementException( );
						}
					}
				};
			}
		};
	}

	/**
	 * <p>
	 * Returns a view of the concatenation of the argument cursor after this
	 * cursor.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param tail
	 *          A cursor to concatenate after this.
	 * @return A view of the concatenation of the argument cursor after this
	 *         cursor.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<I> chain( Cursor<? extends I> tail )
			throws IllegalStateException {
		lock( );
		return wrap( new ChainCursor<>( wrapped, tail ) );
	}

	/**
	 * <p>
	 * Adds all elements of this cursor to the argument collection, then returns
	 * it.
	 * </p>
	 * 
	 * <p>
	 * This is a terminal method.
	 * </p>
	 * 
	 * @param <K>
	 *          The type the argument collection.
	 * @param collection
	 *          The collection to add elements to.
	 * @return The argument collection.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <K extends Collection<? super I>> K collect( K collection )
			throws IllegalStateException {
		lock( );
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			try {
				for ( ;; ) {
					collection.add( wrapped.next( ) );
				}
			}
			catch ( EndOfCursorException e ) {
			}
			return collection;
		}
	}

	/**
	 * <p>
	 * Feeds a consumer with the elements of this cursor.
	 * </p>
	 * 
	 * <p>
	 * Obtains a consumer from the argument {@code consumer_provider}, hooking it
	 * to a local, temporary hook. Then, this method invokesthe wrapped cursor's
	 * {@link org.github.evenjn.yarn.Cursor#next() next()} method, passing each
	 * resulting object to the consumer, until a
	 * {@link org.github.evenjn.yarn.EndOfCursorException EndOfCursorException} is
	 * thrown.
	 * </p>
	 * 
	 * <p>
	 * Returns the number of invocations of {@code next()} that did not throw
	 * {@code EndOfCursorException}.
	 * </p>
	 * 
	 * <p>
	 * This is a terminal method.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of {@code Consumer} returned by the argument.
	 * @param consumer_provider
	 *          A system that provides hooked consumers.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <K extends Consumer<? super I>> void consume(
			Function<Hook, K> consumer_provider )
			throws IllegalStateException {
		lock( );
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			K consumer = consumer_provider.apply( hook );
			for ( ;; ) {
				consumer.accept( wrapped.next( ) );
			}
		}
		catch ( EndOfCursorException e ) {
		}
	}

	/**
	 * <p>
	 * Returns a cursor that rolls the wrapped cursor and the argument cursor in
	 * parallel, applying the argument bifunction to each pair of elements, and
	 * providing a view of the result of each application.
	 * </p>
	 * 
	 * <p>
	 * The retuned cursor provides as many elements as the cursor with the least
	 * elements.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <R>
	 *          The type of elements accessible via the argument cursor.
	 * @param <M>
	 *          The type of elements returned by the bifunction.
	 * @param other
	 *          The cursor to roll in parallel to the wrapped cursor.
	 * @param bifunction
	 *          The bifunction to apply to each pair of element.
	 * @return a cursor that rolls the wrapped cursor and the argument cursor in
	 *         parallel, applying the argument bifunction to each pair of
	 *         elements, and providing a view of the result of each application.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <R, M> KnittingCursor<M> entwine(
			Cursor<R> other,
			BiFunction<? super I, ? super R, M> bifunction )
			throws IllegalStateException {
		lock( );
		return wrap( new EntwineCursor<>( wrapped, other, bifunction ) );
	}

	/**
	 * <p>
	 * Returns a view showing only the elements which satisfy the argument
	 * {@code predicate} in the wrapped cursor.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param predicate
	 *          A stateless system that selects elements to show.
	 * @return A view showing only the elements which satisfy the argument
	 *         {@code stateless_predicate} in the wrapped cursor.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<I> filter( Predicate<? super I> predicate )
			throws IllegalStateException {
		lock( );
		return wrap( new FilterCursor<>( wrapped, predicate ) );
	}

	/**
	 * <p>
	 * Returns a view. For each element {@code E} of the wrapped cursor, the view
	 * shows elements in the cursor {@code C} returned by the argument
	 * {@code cursor_map} when invoked with argument {@code E}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument {@code cursor_map}.
	 * @param cursor_map
	 *          A stateless function that returns cursors.
	 * @return A view.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmap(
			CursorMap<? super I, O> cursor_map )
			throws IllegalStateException {
		lock( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return cursor_map.get( input );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view. For each element {@code E} of the wrapped cursor, the view
	 * shows elements in the cursor {@code C} returned by the argument
	 * {@code cursor_map_h} when invoked with argument {@code E} and a temporary
	 * {@link org.github.evenjn.yarn.Hook hook}. The temporary hook is hooked to
	 * the argument {@code hook}. The returned view closes the temporary hook at
	 * the end of cursor {@code C}. The argument {@code hook} is in charge of
	 * closing resources in the event that the returned view is discarded before
	 * reaching the end of cursor {@code C} or any other.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument
	 *          {@code cursor_map_h}.
	 * @param hook
	 *          A hook.
	 * @param cursor_map_h
	 *          A stateless function that returns cursors.
	 * @return A view.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmap(
			Hook hook,
			CursorMapH<? super I, O> cursor_map_h )
			throws IllegalStateException {
		lock( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return cursor_map_h.get( hook, input );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmap(CursorMap)} except that the view shows elements in the
	 * array returned by the argument {@code array_map}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument {@code array_map}.
	 * @param array_map
	 *          A stateless function that returns arrays.
	 * @return A view.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapArray(
			ArrayMap<? super I, O> array_map )
			throws IllegalStateException {
		lock( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				O[] nextArray = array_map.get( input );
				return nextArray == null ? null : new ArrayCursor<>( nextArray );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmap(Hook, CursorMapH)} except that the view shows elements in
	 * the cursable returned by the argument {@code cursable_map}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument
	 *          {@code cursable_map}.
	 * @param hook
	 *          A hook.
	 * @param cursable_map
	 *          A stateless function that returns cursables.
	 * @return A view.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapCursable(
			Hook hook,
			CursableMap<? super I, O> cursable_map )
			throws IllegalStateException {
		lock( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Cursable<O> cursable = cursable_map.get( input );
				return cursable == null ? null : cursable.pull( hook );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmap(Hook, CursorMapH)} except that the view shows elements in
	 * the cursable returned by the argument {@code cursable_map_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument
	 *          {@code cursable_map_h}.
	 * @param hook
	 *          A hook.
	 * @param cursable_map_h
	 *          A stateless function that returns cursables.
	 * @return A view.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapCursable(
			Hook hook,
			CursableMapH<? super I, O> cursable_map_h )
			throws IllegalStateException {
		lock( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Cursable<O> cursable = cursable_map_h.get( hook, input );
				return cursable == null ? null : cursable.pull( hook );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	/**
	 * <p>
	 * An alias of {@link #flatmap(CursorMap)}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument {@code cursor_map}.
	 * @param hook
	 *          A hook.
	 * @param cursor_map
	 *          A stateless function that returns cursors.
	 * @return A view.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapCursor(
			CursorMap<? super I, O> cursor_map )
			throws IllegalStateException {
		lock( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return cursor_map.get( input );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	/**
	 * <p>
	 * An alias of {@link #flatmap(Hook, CursorMapH)}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument
	 *          {@code cursor_map_h}.
	 * @param hook
	 *          A hook.
	 * @param cursor_map_h
	 *          A stateless function that returns cursors.
	 * @return A view.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapCursor(
			Hook hook,
			CursorMapH<? super I, O> cursor_map_h )
			throws IllegalStateException {
		lock( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return cursor_map_h.get( hook, input );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmap(Hook, CursorMapH)} except that the view shows elements in
	 * the iterable returned by the argument {@code iterable_map_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument
	 *          {@code iterable_map_h}.
	 * @param hook
	 *          A hook.
	 * @param iterable_map_h
	 *          A stateless function that returns iterables.
	 * @return A view.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapIterable(
			Hook hook,
			IterableMapH<? super I, O> iterable_map_h )
			throws IllegalStateException {
		lock( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Iterable<O> nextIterable = iterable_map_h.get( hook, input );
				return nextIterable == null ? null : new IteratorCursor<>(
						nextIterable.iterator( ) );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmap(CursorMap)} except that the view shows elements in the
	 * iterable returned by the argument {@code iterable_map}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument
	 *          {@code iterable_map}.
	 * @param iterable_map
	 *          A stateless function that returns iterables.
	 * @return A view.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapIterable(
			IterableMap<? super I, O> iterable_map )
			throws IllegalStateException {
		lock( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Iterable<O> nextIterable = iterable_map.get( input );
				return nextIterable == null ? null : new IteratorCursor<>(
						nextIterable.iterator( ) );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmap(Hook, CursorMapH)} except that the view shows elements in
	 * the iterator returned by the argument {@code iterator_map_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument
	 *          {@code iterator_map_h}.
	 * @param hook
	 *          A hook.
	 * @param iterator_map_h
	 *          A stateless function that returns iterators.
	 * @return A view.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapIterator(
			Hook hook,
			IteratorMapH<? super I, O> iterator_map_h )
			throws IllegalStateException {
		lock( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Iterator<O> nextIterator = iterator_map_h.get( hook, input );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmap(CursorMap)} except that the view shows elements in the
	 * iterator returned by the argument {@code iterator_map}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument
	 *          {@code iterator_map}.
	 * @param iterator_map
	 *          A stateless function that returns iterators.
	 * @return A view.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapIterator(
			IteratorMap<? super I, O> iterator_map )
			throws IllegalStateException {
		lock( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Iterator<O> nextIterator = iterator_map.get( input );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmap(CursorMap)} except that the view shows elements in the
	 * optional returned by the argument {@code optional_map}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument
	 *          {@code optional_map}.
	 * @param iterator_map
	 *          A stateless function that returns optionals.
	 * @return A view.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapOptional(
			OptionMap<? super I, O> optional_map )
			throws IllegalStateException {
		lock( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Optional<O> optional = optional_map.get( input );
				if ( optional.isPresent( ) ) {
					return new SingletonCursor<O>( optional.get( ) );
				}
				return null;
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmap(Hook, CursorMapH)} except that the view shows elements in
	 * the optional returned by the argument {@code optional_map_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument
	 *          {@code optional_map_h}.
	 * @param hook
	 *          A hook.
	 * @param optional_map_h
	 *          A stateless function that returns optionals.
	 * @return A view.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapOptional( Hook hook,
			OptionMapH<? super I, O> optional_map_h )
			throws IllegalStateException {
		lock( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Optional<O> optional = optional_map_h.get( hook, input );
				if ( optional.isPresent( ) ) {
					return new SingletonCursor<O>( optional.get( ) );
				}
				return null;
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmap(Hook, CursorMapH)} except that the view shows elements in
	 * the stream returned by the argument {@code stream_map_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument
	 *          {@code stream_map_h}.
	 * @param hook
	 *          A hook.
	 * @param stream_map_h
	 *          A stateless function that returns streams.
	 * @return A view.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapStream(
			Hook hook,
			StreamMapH<? super I, O> stream_map_h )
			throws IllegalStateException {
		lock( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Stream<O> nextStream = stream_map_h.get( hook, input );
				return nextStream == null ? null : new IteratorCursor<>(
						nextStream.iterator( ) );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns whether the next invocation of {@link KnittingCursor#next() next()}
	 * will throw a {@link EndOfCursorException}.
	 * </p>
	 * 
	 * <p>
	 * If the next element has not been cached, invoking this method will update
	 * the cache with the element obtained invoking
	 * {@link org.github.evenjn.yarn.Cursor#next() next()} on the wrapped cursor.
	 * </p>
	 * 
	 * <p>
	 * Invoking this method causes the object to enter the used state. Invoking
	 * this method when the cursor is in locked state will throw an
	 * {@code IllegalStateException}.
	 * </p>
	 * 
	 * @return whether the next invocation of {@link KnittingCursor#next() next()}
	 *         will throw a {@link EndOfCursorException}.
	 * @throws IllegalStateException
	 *           when the cursor is in locked state.
	 * @since 1.0
	 */
	public boolean hasNext( ) {
		if ( locked ) {
			throw new IllegalStateException( "This cursor has been locked." );
		}
		used = true;
		if ( is_cached ) {
			return true;
		}
		try {
			cached = wrapped.next( );
		}
		catch ( EndOfCursorException e ) {
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
	 * This is a transformation method.
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
		lock( );
		locked = true;
		int final_show = show < 0 ? 0 : show;
		int final_hide = hide < 0 ? 0 : hide;
		return wrap( Subcursor.sub( wrapped, final_hide, final_show ) );
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
	 * This is a transformation method.
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
		lock( );
		locked = true;
		int final_hide = hide < 0 ? 0 : hide;
		return wrap( Subcursor.skip( wrapped, final_hide ) );
	}

	/**
	 * <p>
	 * Returns a view. For each element {@code E} of the wrapped cursor, the view
	 * shows element returned by the argument {@code map} when invoked with
	 * argument {@code E}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument {@code map}.
	 * @param map
	 *          A stateless function.
	 * @return A view.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> map( Function<? super I, O> function )
			throws IllegalStateException {
		lock( );
		locked = true;
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return new SingletonCursor<O>( function.apply( input ) );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}
	
	/**
	 * <p>
	 * Returns a view. For each element {@code E} of the wrapped cursor, the view
	 * shows the returned by the argument {@code map_h} when invoked with argument
	 * {@code E} and a temporary {@link org.github.evenjn.yarn.Hook hook}. The
	 * temporary hook is hooked to the argument {@code hook}. The returned view
	 * closes the temporary hook when an attempt to retrieve the next element is
	 * made. The argument {@code hook} is in charge of closing resources in the
	 * event that the returned view is discarded before reaching the end of the
	 * wrapped cursor.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument
	 *          {@code map_h}.
	 * @param hook
	 *          A hook.
	 * @param map_h
	 *          A stateless function.
	 * @return A view.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O>
			map( Hook hook, FunctionH<? super I, O> function )
					throws IllegalStateException {
		lock( );
		locked = true;
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return new SingletonCursor<O>( function.get( hook, input ) );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns the next element provided by the wrapped cursor.
	 * </p>
	 * 
	 * <p>
	 * If the next element has been cached, invoking this method will return the
	 * cached element, then empty the cache. Otherwise, this method will invoke
	 * {@link org.github.evenjn.yarn.Cursor#next() next()} on the wrapped cursor
	 * and return the result.
	 * </p>
	 * 
	 * <p>
	 * Invoking this method causes the object to enter the used state. Invoking
	 * this method when the cursor is in locked state will throw an
	 * {@code IllegalStateException}.
	 * </p>
	 * 
	 * @return the next element provided by the wrapped cursor.
	 * @throws EndOfCursorException
	 *           when there are no more elements to retrieve from the wrapped
	 *           cursor.
	 * @throws IllegalStateException
	 *           when the cursor is in locked state.
	 * @since 1.0
	 */
	@Override
	public I next( )
			throws EndOfCursorException {
		if ( locked ) {
			throw new IllegalStateException( "This cursor has been locked." );
		}
		used = true;
		I result = null;
		if ( is_cached ) {
			is_cached = false;
			result = cached;
		}
		else {
			result = wrapped.next( );
		}
		return result;
	}

	/**
	 * <p>
	 * Returns a cursor that, for each element of the wrapped cursor, provides a
	 * {@linkplain org.github.knit.Bi pair} containing the element itself and the
	 * number of elements retrieved so far.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 *
	 * @return A view showing the first {@code show} elements visible after hiding
	 *         the first {@code hide} elements in this cursor.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<Bi<I, Integer>> numbered( )
			throws IllegalStateException {
		lock( );
		return wrap( new NumberedCursor<>( wrapped ) );
	}

	/**
	 * <p>
	 * When the cursor provides access to one element only, this method returns
	 * it.
	 * </p>
	 * 
	 * <p>
	 * This is a terminal operation.
	 * </p>
	 * 
	 * @return The only element accessible via this cursor.
	 * @throws IllegalStateException
	 *           when it not the case that there is exactly one element, when the
	 *           cursor is not in pristine state.
	 * @since 1.0
	 */
	public I one( )
			throws IllegalStateException {
		lock( );
		try {
			I result = wrapped.next( );
			try {
				wrapped.next( );
				throw new IllegalStateException(
						"This cursor provided more than one element." );
			}
			catch ( EndOfCursorException e ) {
				return result;
			}
		}
		catch ( EndOfCursorException e ) {
			throw new IllegalStateException( "The cursor provided no elements." );
		}
	}

	/**
	 * <p>
	 * When the cursor provides access to one non-null element, this method
	 * returns it wrapped in an {@code Optional}.
	 * </p>
	 * 
	 * <p>
	 * When it not the case that there is exactly one element, or when the only
	 * element accessible is {@code null}, this method returns an empty
	 * {@code Optional}.
	 * </p>
	 * 
	 * <p>
	 * This is a terminal method.
	 * </p>
	 * 
	 * @return The only element accessible via this cursor.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public Optional<I> optionalOne( )
			throws IllegalStateException {
		lock( );
		try {
			I result = wrapped.next( );
			try {
				wrapped.next( );
				return Optional.empty( );
			}
			catch ( EndOfCursorException e ) {
				return Optional.ofNullable( result );
			}
		}
		catch ( EndOfCursorException e ) {
			return Optional.empty( );
		}
	}

	<O> KnittingCursor<O> optionfold(
			Hook hook,
			OptionFoldH<? super I, O> option_fold_h )
			throws IllegalStateException {
		lock( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Optional<O> next = option_fold_h.next( hook, input );
				if ( next.isPresent( ) ) {
					return new SingletonCursor<O>( next.get( ) );
				}
				return null;
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				Optional<O> end = option_fold_h.end( hook );
				if ( end.isPresent( ) ) {
					return new SingletonCursor<O>( end.get( ) );
				}
				return null;
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	public <O> KnittingCursor<O> optionfold(
			OptionFold<? super I, O> option_fold )
			throws IllegalStateException {
		lock( );
		CursorUnfoldH<I, O> stitch = new CursorUnfoldH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Optional<O> next = option_fold.next( input );
				if ( next.isPresent( ) ) {
					return new SingletonCursor<O>( next.get( ) );
				}
				return null;
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				Optional<O> end = option_fold.end( );
				if ( end.isPresent( ) ) {
					return new SingletonCursor<O>( end.get( ) );
				}
				return null;
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns the result of a computation taking into account all the elements of
	 * the wrapped cursor.
	 * </p>
	 * 
	 * <p>
	 * This method stores into the local variable the argument {@code zero}. Then,
	 * for each element in the wrapped cursor, this method invokes the argument
	 * {@code bifunction} using the content of the local variable and the element.
	 * At the end of each such invocation, this method stores into the local
	 * variable the result of the invocation. Finally, this method returns the
	 * content of the local variable.
	 * </p>
	 * 
	 * <p>
	 * This is a terminal method.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of argument {@code zero} and of the elements returned by
	 *          the argument {@code bifunction}.
	 * @param zero
	 *          The reduction value for an empty cursor.
	 * @param bifunction
	 *          A bifunction that will be invoked once for each element of the
	 *          wrapped cursor.
	 * @return the result of a computation taking into account all the elements of
	 *         the wrapped cursor.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public <K> K reduce( K zero, BiFunction<K, I, K> bifunction )
			throws IllegalStateException {
		lock( );
		K reduction = zero;
		try {
			for ( ;; ) {
				reduction = bifunction.apply( reduction, wrapped.next( ) );
			}
		}
		catch ( EndOfCursorException e ) {
		}
		return reduction;
	}

	/**
	 * <p>
	 * Invokes the wrapped cursor's {@link org.github.evenjn.yarn.Cursor#next()
	 * next()} method until a {@link org.github.evenjn.yarn.EndOfCursorException
	 * EndOfCursorException} is thrown.
	 * </p>
	 * 
	 * <p>
	 * Returns the number of invocations of {@code next()} that did not throw
	 * {@code EndOfCursorException}.
	 * </p>
	 * 
	 * <p>
	 * This is a terminal method.
	 * </p>
	 * 
	 * @return the number of invocations of {@code next()} that did not throw
	 *         {@code EndOfCursorException}.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public int roll( )
			throws IllegalStateException {
		lock( );
		int so_far = 0;
		try {
			for ( ;; so_far++ ) {
				wrapped.next( );
			}
		}
		catch ( EndOfCursorException e ) {
			return so_far;
		}
	}

	public <O> KnittingCursor<O> skipfold( SkipFold<? super I, O> skip_fold )
			throws IllegalStateException {
		lock( );
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
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	<O> KnittingCursor<O> skipfold(
			Hook hook,
			SkipFoldH<? super I, O> skip_fold_h )
			throws IllegalStateException {
		lock( );
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
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	<O> KnittingCursor<O> skipmap( SkipMap<? super I, O> skip_map )
			throws IllegalStateException {
		lock( );
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

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	<O> KnittingCursor<O> skipmap(
			Hook hook,
			SkipMapH<? super I, O> skip_map_h )
			throws IllegalStateException {
		lock( );
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

			@Override
			public Cursor<O> end( Hook hook ) {
				return empty( );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a cursor where each element is a cursor providing access to a
	 * subsequence of contiguous elements that satisfy the argument
	 * {@code predicate} in the wrapped cursor.
	 * </p>
	 * 
	 * @param predicate
	 *          A stateless system that identifies elements that should be kept
	 *          together.
	 * @return a cursor where each element is a cursor providing access to a
	 *         subsequence of contiguous elements that satisfy the argument
	 *         {@code predicate} in the wrapped cursor.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<KnittingCursor<I>> splat( Predicate<I> predicate )
			throws IllegalStateException {
		lock( );
		return KnittingCursor
				.wrap( new SplitCursor<I>( wrapped, predicate.negate( ) ) );
	}

	/**
	 * <p>
	 * Returns a cursor providing access to the same elements provided by the
	 * wrapped cursor, which passes the elements to the argument consumer before
	 * retuning them.
	 * </p>
	 * 
	 * <p>
	 * At each invocation of {@code next()} on the returned {@code KnittingCursor}
	 * object, that object fetches the next element from the wrapped cursor, then
	 * invokes the argument {@code consumer} using the fetched element as
	 * argument, then returns the fetched element.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param consumer
	 *          A consumer that will consume each element retrieved from the
	 *          wrapped cursor.
	 * @return A cursor with same elements provided by the wrapped cursor that
	 *         passes the elements to the argument consumer before retuning them.
	 * @throws IllegalStateException
	 *           when the cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<I> tap( Consumer<? super I> consumer )
			throws IllegalStateException {
		lock( );
		return KnittingCursor.wrap( new TapCursor<I>( wrapped, consumer ) );
	}

	public <O> KnittingCursor<O> unfoldArray(
			ArrayUnfold<? super I, O> array_unfold )
			throws IllegalStateException {
		lock( );
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
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	public <O> KnittingCursor<O> unfoldCursable(
			CursableUnfold<? super I, O> cursable_unfold )
			throws IllegalStateException {
		lock( );

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
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	public <O> KnittingCursor<O> unfoldCursable(
			Hook hook,
			CursableUnfoldH<? super I, O> cursable_unfold_h )
			throws IllegalStateException {
		lock( );
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
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	public <O> KnittingCursor<O> unfoldCursor(
			CursorUnfold<? super I, O> cursor_unfold )
			throws IllegalStateException {
		lock( );
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
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	public <O> KnittingCursor<O> unfoldCursor(
			Hook hook,
			CursorUnfoldH<? super I, O> cursor_unfold_h )
			throws IllegalStateException {
		lock( );
		return wrap(
				new CursorStitchProcessor<I, O>( hook, wrapped, cursor_unfold_h ) );
	}

	public <O> KnittingCursor<O> unfoldIterable(
			IterableUnfold<? super I, O> iterable_unfold )
			throws IllegalStateException {
		lock( );
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
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	public <O> KnittingCursor<O> unfoldIterable(
			Hook hook,
			IterableUnfoldH<? super I, O> iterable_unfold_h )
			throws IllegalStateException {
		lock( );
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
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	public <O> KnittingCursor<O> unfoldIterator(
			IteratorUnfold<? super I, O> iterator_unfold )
			throws IllegalStateException {
		lock( );
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
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	public <O> KnittingCursor<O> unfoldIterator(
			Hook hook,
			IteratorUnfoldH<? super I, O> iterator_unfold_h )
			throws IllegalStateException {
		lock( );
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
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	public <O> KnittingCursor<O> unfoldStream(
			Hook hook,
			StreamUnfoldH<? super I, O> stream_unfold_h )
			throws IllegalStateException {
		lock( );
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
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns an empty cursor.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of elements in the empty cursor.
	 * 
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
				throws EndOfCursorException {
			throw EndOfCursorException.neo( );
		}
	} );

	/*
	 * Stops as soon as one of the sources is depleted.
	 * 
	 * @return an iterator that pulls the next element from the i-th iterator,
	 * where i is the next integer pulled by selector.
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
					throws EndOfCursorException {
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
					throws EndOfCursorException {
				if ( i >= tuple.size( ) ) {
					throw EndOfCursorException.neo( );
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
					throws EndOfCursorException {
				if ( iterator.hasNext( ) )
					return iterator.next( );
				throw EndOfCursorException.neo( );
			}
		} );
	}

	public static <K> KnittingCursor<K> wrap( Stream<K> stream ) {
		return wrap( stream.iterator( ) );
	}
}
