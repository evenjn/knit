/**
 *
 * Copyright 2017 Marco Irevisan
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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.github.evenjn.yarn.ArrayMap;
import org.github.evenjn.yarn.ArrayPurl;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.CursableMap;
import org.github.evenjn.yarn.CursableMapH;
import org.github.evenjn.yarn.CursablePurl;
import org.github.evenjn.yarn.CursablePurlH;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.CursorMap;
import org.github.evenjn.yarn.CursorMapH;
import org.github.evenjn.yarn.CursorPurl;
import org.github.evenjn.yarn.CursorPurlH;
import org.github.evenjn.yarn.EndOfCursorException;
import org.github.evenjn.yarn.FunctionH;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.IterableMap;
import org.github.evenjn.yarn.IterableMapH;
import org.github.evenjn.yarn.IterablePurl;
import org.github.evenjn.yarn.IterablePurlH;
import org.github.evenjn.yarn.IteratorMap;
import org.github.evenjn.yarn.IteratorMapH;
import org.github.evenjn.yarn.IteratorPurl;
import org.github.evenjn.yarn.IteratorPurlH;
import org.github.evenjn.yarn.OptionMap;
import org.github.evenjn.yarn.OptionMapH;
import org.github.evenjn.yarn.OptionalPurl;
import org.github.evenjn.yarn.OptionalPurlH;
import org.github.evenjn.yarn.StreamMapH;
import org.github.evenjn.yarn.StreamPurlH;
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
 * <li>As a resource to be harvested, invoking a rolling method such as
 * {@link #collect(Collection)};</li>
 * <li>As a resource to transform, invoking a transformation method such as
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
 * <li>Rolling methods (listed below)</li>
 * <li>Transformation methods (listed below)</li>
 * </ul>
 * 
 * <p>
 * Rolling methods repeatedly invoke this cursor's
 * {@link org.github.evenjn.yarn.Cursor#next() next()}, typically (but not
 * necessarily) until the end is reached. The following methods are rolling:
 * </p>
 * 
 * <ul>
 * <li>{@link #collect(Collection)}</li>
 * <li>{@link #count()}</li>
 * <li>{@link #consume(Function)}</li>
 * <li>{@link #one()}</li>
 * <li>{@link #optionalOne()}</li>
 * <li>{@link #reduce(Object, BiFunction)}</li>
 * <li>{@link #roll()}</li>
 * </ul>
 * 
 * <p>
 * Transformations are a group of methods that return a new
 * {@code KnittingCursor} object (or something similar), which provides a new
 * view of the contents of the wrapped cursor. Transformation methods do not
 * invoke the wrapped cursor's {@link org.github.evenjn.yarn.Cursor#next()
 * next()}; they return lazy wrappers. The following methods are
 * transformations:
 * </p>
 * 
 * <ul>
 * <li>{@link #append(Cursor)}</li>
 * <li>{@link #asIterator()}</li>
 * <li>{@link #asStream()}</li>
 * <li>{@link #crop(Predicate)}</li>
 * <li>{@link #entwine(Cursor, BiFunction)}</li>
 * <li>{@link #filter(Predicate)}</li>
 * <li>{@link #flatmapArray(ArrayMap)}</li>
 * <li>{@link #flatmapCursable(Hook, CursableMap)}</li>
 * <li>{@link #flatmapCursable(Hook, CursableMapH)}</li>
 * <li>{@link #flatmapCursor(CursorMap)}</li>
 * <li>{@link #flatmapCursor(Hook, CursorMapH)}</li>
 * <li>{@link #flatmapIterable(IterableMap)}</li>
 * <li>{@link #flatmapIterable(Hook, IterableMapH)}</li>
 * <li>{@link #flatmapIterator(IteratorMap)}</li>
 * <li>{@link #flatmapIterator(Hook, IteratorMapH)}</li>
 * <li>{@link #flatmapOptional(OptionMap)}</li>
 * <li>{@link #flatmapOptional(Hook, OptionMapH)}</li>
 * <li>{@link #flatmapStream(Hook, StreamMapH)}</li>
 * <li>{@link #head(int, int)}</li>
 * <li>{@link #headless(int)}</li>
 * <li>{@link #map(Function)}</li>
 * <li>{@link #map(Hook, FunctionH)}</li>
 * <li>{@link #numbered()}</li>
 * <li>{@link #once()}</li>
 * <li>{@link #peek(Consumer)}</li>
 * <li>{@link #prepend(Cursor)}</li>
 * <li>{@link #purlArray(ArrayPurl)}</li>
 * <li>{@link #purlCursable(CursablePurl)}</li>
 * <li>{@link #purlCursable(Hook, CursablePurlH)}</li>
 * <li>{@link #purlCursor(CursorPurl)}</li>
 * <li>{@link #purlCursor(Hook, CursorPurlH)}</li>
 * <li>{@link #purlIterable(IterablePurl)}</li>
 * <li>{@link #purlIterable(Hook, IterablePurlH)}</li>
 * <li>{@link #purlIterator(IteratorPurl)}</li>
 * <li>{@link #purlIterator(Hook, IteratorPurlH)}</li>
 * <li>{@link #purlOptional(OptionalPurl)}</li>
 * <li>{@link #purlOptional(Hook, OptionalPurlH)}</li>
 * <li>{@link #purlStream(Hook, StreamPurlH)}</li>
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
 * remains in pristine state until the first invocation of any method not
 * inherited from {@link java.lang.Object}. After a {@code KnittingCursor}
 * object leaves the pristine state, it never returns to the pristine state. Any
 * method may be invoked on a {@code KnittingCursor} object in pristine state.
 * </p>
 * 
 * <h2>Used state</h2>
 * 
 * <p>
 * When {@link #hasNext()} or {@link #next()} is invoked on a
 * {@code KnittingCursor} object in pristine state, the object enters the used
 * state. Transformation methods and rolling methods may not be invoked on an
 * object in used state. Invocation of a transformation or rolling method on a
 * cursor that is in used state will throw an {@code IllegalStateException}.
 * </p>
 * 
 * <h2>Locked state</h2>
 * 
 * <p>
 * When the first transformation or rolling method of a {@code KnittingCursor}
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
	 *          A cursor to concatenate after this cursor.
	 * @return A view of the concatenation of the argument cursor after this
	 *         cursor.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<I> append( Cursor<? extends I> tail )
			throws IllegalStateException {
		lock( );
		return wrap( new ConcatenateCursor<I>( wrapped, tail ) );
	}

	/**
	 * <p>
	 * Returns a view of this cursor as a {@link java.util.Iterator}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @return A view of this cursor as a {@link java.util.Iterator}.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public Iterator<I> asIterator( )
			throws IllegalStateException {
		lock( );
		return private_once( ).iterator( );
	}

	/**
	 * <p>
	 * Returns a view of this cursor as a {@link java.util.stream.Stream}.
	 * </p>
	 * 
	 * <p>
	 * This method does not transfer the responsibility of closing resources to
	 * the returned stream.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @return A view of this cursor as a {@link java.util.stream.Stream}.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
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

	/**
	 * <p>
	 * Adds all elements of this cursor to the argument collection, then returns
	 * it.
	 * </p>
	 * 
	 * <p>
	 * The objects collected may be dead. In general, cursors do not guarantee
	 * that the objects they return survive subsequent invocations of
	 * {@link #next()}.
	 * </p>
	 * 
	 * <p>
	 * This is a rolling method.
	 * </p>
	 * 
	 * @param <K>
	 *          The type the argument collection.
	 * @param collection
	 *          The collection to add elements to.
	 * @return The argument collection.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
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
	 * to a local, temporary hook. Then, this method invokes this cursor's
	 * {@link org.github.evenjn.yarn.Cursor#next() next()} method, passing each
	 * object obtained this way to the consumer, until the end of this cursor.
	 * </p>
	 * 
	 * <p>
	 * This is a rolling method.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of {@code Consumer} returned by the argument.
	 * @param consumer_provider
	 *          A system that provides hooked consumers.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
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
	 * Counts the number of elements in this cursor.
	 * </p>
	 * 
	 * <p>
	 * Invokes this cursor's {@link org.github.evenjn.yarn.Cursor#next() next()}
	 * method until a {@link org.github.evenjn.yarn.EndOfCursorException
	 * EndOfCursorException} is thrown.
	 * </p>
	 * 
	 * <p>
	 * Returns the number of invocations of {@code next()} that did not throw
	 * {@code EndOfCursorException}.
	 * </p>
	 * 
	 * <p>
	 * This is a rolling method.
	 * </p>
	 * 
	 * @return The number of elements in this cursor.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public int count( ) {
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

	/**
	 * <p>
	 * Returns a cursor where each element is a cursor providing access to a
	 * subsequence of contiguous elements in this cursor that satisfy the argument
	 * {@code stateless_predicate}.
	 * </p>
	 * 
	 * @param stateless_predicate
	 *          A stateless system that identifies elements that should be kept
	 *          together.
	 * @return a cursor where each element is a cursor providing access to a
	 *         subsequence of contiguous elements in this cursor that satisfy the
	 *         argument {@code stateless_predicate}.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<KnittingCursor<I>>
			crop( Predicate<I> stateless_predicate )
					throws IllegalStateException {
		lock( );
		return KnittingCursor
				.wrap( new SplitCursor<I>( wrapped, stateless_predicate.negate( ) ) );
	}

	/**
	 * <p>
	 * Returns a cursor that traverses this cursor and the argument
	 * {@code other_cursor} in parallel, applying the argument
	 * {@code stateless_bifunction} to each pair of elements, and providing a view
	 * of the result of each application.
	 * </p>
	 * 
	 * <p>
	 * The returned cursor provides as many elements as the cursor with the least
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
	 * @param other_cursor
	 *          The cursor to roll in parallel to this cursor.
	 * @param stateless_bifunction
	 *          The stateless bifunction to apply to each pair of element.
	 * @return a cursor that traverses this cursor and the argument cursor in
	 *         parallel, applying the argument bifunction to each pair of
	 *         elements, and providing a view of the result of each application.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <R, M> KnittingCursor<M> entwine(
			Cursor<R> other_cursor,
			BiFunction<? super I, ? super R, M> stateless_bifunction )
			throws IllegalStateException {
		lock( );
		return wrap(
				new EntwineCursor<>( wrapped, other_cursor, stateless_bifunction ) );
	}

	/**
	 * <p>
	 * Returns a view showing only the elements which satisfy the argument
	 * {@code stateless_predicate} in this cursor.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param stateless_predicate
	 *          A stateless system that selects elements to show.
	 * @return A view showing only the elements which satisfy the argument
	 *         {@code stateless_predicate} in this cursor.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<I> filter( Predicate<? super I> stateless_predicate )
			throws IllegalStateException {
		lock( );
		return wrap( new FilterCursor<>( wrapped, stateless_predicate ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmapCursor(CursorMap)} except that the view shows elements in
	 * the arrays returned by the argument {@code stateless_array_map}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the arrays returned by the argument
	 *          {@code stateless_array_map}.
	 * @param stateless_array_map
	 *          A stateless function that returns arrays.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapArray(
			ArrayMap<? super I, O> stateless_array_map )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				O[] nextArray = stateless_array_map.get( input );
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
	 * {@link #flatmapCursor(Hook, CursorMapH)} except that the view shows
	 * elements in the cursables returned by the argument
	 * {@code stateless_cursable_map}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the cursables returned by the argument
	 *          {@code stateless_cursable_map}.
	 * @param hook
	 *          A hook.
	 * @param stateless_cursable_map
	 *          A stateless function that returns cursables.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapCursable(
			Hook hook,
			CursableMap<? super I, O> stateless_cursable_map )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Cursable<O> cursable = stateless_cursable_map.get( input );
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
	 * {@link #flatmapCursor(Hook, CursorMapH)} except that the view shows
	 * elements in the cursables returned by the argument
	 * {@code stateless_cursable_map_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the cursables returned by the argument
	 *          {@code stateless_cursable_map_h}.
	 * @param hook
	 *          A hook.
	 * @param stateless_cursable_map_h
	 *          A stateless function that returns cursables.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapCursable(
			Hook hook,
			CursableMapH<? super I, O> stateless_cursable_map_h )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Cursable<O> cursable = stateless_cursable_map_h.get( hook, input );
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
	 * Returns a complex view.
	 * </p>
	 * 
	 * <p>
	 * For each element {@code E} of this cursor, the view shows elements in the
	 * cursor {@code C} returned by the argument {@code stateless_cursor_map} when
	 * invoked with argument {@code E}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the cursors returned by the argument
	 *          {@code stateless_cursor_map}.
	 * @param stateless_cursor_map
	 *          A stateless function that returns cursors.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapCursor(
			CursorMap<? super I, O> stateless_cursor_map )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return stateless_cursor_map.get( input );
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
	 * Returns a complex view.
	 * </p>
	 * 
	 * <p>
	 * For each element {@code E} of this cursor, the view shows elements in the
	 * cursor {@code C} returned by the argument {@code stateless_cursor_map_h}
	 * when invoked with argument {@code E} and a temporary
	 * {@link org.github.evenjn.yarn.Hook hook}.
	 * </p>
	 * 
	 * <p>
	 * The temporary hook is hooked to the argument {@code hook}. The returned
	 * view closes each temporary hook at the end of each cursor {@code C}. The
	 * argument {@code hook} is in charge of closing resources in the event that
	 * the returned cursor is abandoned as garbage before reaching its end.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the cursors returned by the argument
	 *          {@code stateless_cursor_map_h}.
	 * @param hook
	 *          A hook.
	 * @param stateless_cursor_map_h
	 *          A stateless function that returns cursors.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapCursor(
			Hook hook,
			CursorMapH<? super I, O> stateless_cursor_map_h )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return stateless_cursor_map_h.get( hook, input );
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
	 * {@link #flatmapCursor(Hook, CursorMapH)} except that the view shows
	 * elements in the iterables returned by the argument
	 * {@code stateless_iterable_map_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the iterables returned by the argument
	 *          {@code stateless_iterable_map_h}.
	 * @param hook
	 *          A hook.
	 * @param stateless_iterable_map_h
	 *          A stateless function that returns iterables.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapIterable(
			Hook hook,
			IterableMapH<? super I, O> stateless_iterable_map_h )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Iterable<O> nextIterable = stateless_iterable_map_h.get( hook, input );
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
	 * {@link #flatmapCursor(CursorMap)} except that the view shows elements in
	 * the iterables returned by the argument {@code stateless_iterable_map}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the iterables returned by the argument
	 *          {@code stateless_iterable_map}.
	 * @param stateless_iterable_map
	 *          A stateless function that returns iterables.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapIterable(
			IterableMap<? super I, O> stateless_iterable_map )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Iterable<O> nextIterable = stateless_iterable_map.get( input );
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
	 * {@link #flatmapCursor(Hook, CursorMapH)} except that the view shows
	 * elements in the iterators returned by the argument
	 * {@code stateless_iterator_map_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the iterators returned by the argument
	 *          {@code stateless_iterator_map_h}.
	 * @param hook
	 *          A hook.
	 * @param stateless_iterator_map_h
	 *          A stateless function that returns iterators.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapIterator(
			Hook hook,
			IteratorMapH<? super I, O> stateless_iterator_map_h )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Iterator<O> nextIterator = stateless_iterator_map_h.get( hook, input );
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
	 * {@link #flatmapCursor(CursorMap)} except that the view shows elements in
	 * the iterators returned by the argument {@code stateless_iterator_map}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the iterators returned by the argument
	 *          {@code stateless_iterator_map}.
	 * @param stateless_iterator_map
	 *          A stateless function that returns iterators.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapIterator(
			IteratorMap<? super I, O> stateless_iterator_map )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Iterator<O> nextIterator = stateless_iterator_map.get( input );
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
	 * {@link #flatmapCursor(Hook, CursorMapH)} except that the view shows
	 * elements in the optionals returned by the argument
	 * {@code stateless_optional_map_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the optionals returned by the argument
	 *          {@code stateless_optional_map_h}.
	 * @param hook
	 *          A hook.
	 * @param stateless_optional_map_h
	 *          A stateless function that returns optionals.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapOptional( Hook hook,
			OptionMapH<? super I, O> stateless_optional_map_h )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Optional<O> optional = stateless_optional_map_h.get( hook, input );
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
	 * {@link #flatmapCursor(CursorMap)} except that the view shows elements in
	 * the optionals returned by the argument {@code stateless_optional_map}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the optionals returned by the argument
	 *          {@code stateless_optional_map}.
	 * @param stateless_optional_map
	 *          A stateless function that returns optionals.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapOptional(
			OptionMap<? super I, O> stateless_optional_map )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Optional<O> optional = stateless_optional_map.get( input );
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
	 * {@link #flatmapCursor(Hook, CursorMapH)} except that the view shows
	 * elements in the streams returned by the argument
	 * {@code stateless_stream_map_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the streams returned by the argument
	 *          {@code stateless_stream_map_h}.
	 * @param hook
	 *          A hook.
	 * @param stateless_stream_map_h
	 *          A stateless function that returns streams.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> flatmapStream(
			Hook hook,
			StreamMapH<? super I, O> stateless_stream_map_h )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Stream<O> nextStream = stateless_stream_map_h.get( hook, input );
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
	 * Invoking this method may kill the object returned at the previous
	 * invocation of {@link #next()}, if any. This happens when the wrapped cursor
	 * does not guarantee that the objects it returns survive subsequent
	 * invocations of {@link #next()}.
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
	 * Returns a view showing the first {@code show} elements of this cursor
	 * visible after hiding the first {@code hide} elements.
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
	 * @return A view showing the first {@code show} elements of this cursor
	 *         visible after hiding the first {@code hide} elements.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<I> head( int hide, int show )
			throws IllegalStateException {
		lock( );
		int final_show = show < 0 ? 0 : show;
		int final_hide = hide < 0 ? 0 : hide;
		return wrap( Subcursor.sub( wrapped, final_hide, final_show ) );
	}

	/**
	 * <p>
	 * Returns a view hiding the first {@code hide} elements of this cursor.
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
	 * @return A view hiding the first {@code hide} elements of this cursor.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<I> headless( int hide )
			throws IllegalStateException {
		lock( );
		int final_hide = hide < 0 ? 0 : hide;
		return wrap( Subcursor.skip( wrapped, final_hide ) );
	}

	/**
	 * <p>
	 * Returns a view. For each element {@code E} of this cursor, the view shows
	 * the element returned by the argument {@code stateless_function} when
	 * invoked with argument {@code E}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument
	 *          {@code stateless_function}.
	 * @param stateless_function
	 *          A stateless function.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> map( Function<? super I, O> stateless_function )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return new SingletonCursor<O>( stateless_function.apply( input ) );
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
	 * Returns a view. For each element {@code E} of this cursor, the view shows
	 * the returned by the argument {@code stateless_function_h} when invoked with
	 * argument {@code E} and a temporary {@link org.github.evenjn.yarn.Hook
	 * hook}.
	 * </p>
	 * 
	 * <p>
	 * The temporary hook is hooked to the argument {@code hook}. The returned
	 * view closes the temporary hook at the subsequent invocation of
	 * {@code next()}. The argument {@code hook} is in charge of closing resources
	 * in the event that the returned cursor is abandoned as garbage before
	 * reaching its end.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument
	 *          {@code stateless_function_h}.
	 * @param hook
	 *          A hook.
	 * @param stateless_function_h
	 *          A stateless function.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O>
			map( Hook hook, FunctionH<? super I, O> stateless_function_h )
					throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return new SingletonCursor<O>(
						stateless_function_h.get( hook, input ) );
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
	 * Invoking this method causes this object to enter the used state. Invoking
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
	 * Returns a cursor that, for each element of this cursor, provides a
	 * {@linkplain org.github.evenjn.yarn.Bi pair} containing the element itself
	 * and the number of elements retrieved so far.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 *
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<Bi<I, Integer>> numbered( )
			throws IllegalStateException {
		lock( );
		return wrap( new NumberedCursor<>( wrapped ) );
	}

	/**
	 * <p>
	 * Returns a view of this cursor as a {@link java.lang.Iterable}.
	 * </p>
	 * 
	 * <p>
	 * The method {@link java.lang.Iterable#iterator() iterator()} on the returned
	 * iterable may be invoked only once. Subsequent invocations will throw an
	 * {@code IllegalStateException}. This method is provided only to iterate on
	 * this cursor with a for-each loop. We discourage other uses.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @return A view of this cursor as a {@link java.lang.Iterable}.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public Iterable<I> once( )
			throws IllegalStateException {
		lock( );
		return private_once( );
	}

	/**
	 * <p>
	 * When this cursor provides access to one element only, this method returns
	 * it. Otherwise, it throws an {@code IllegalStateException}.
	 * </p>
	 * 
	 * <p>
	 * The object returned may be dead. In general, cursors do not guarantee that
	 * the objects they return survive subsequent invocations of {@link #next()}.
	 * </p>
	 * 
	 * <p>
	 * This is a rolling method.
	 * </p>
	 * 
	 * @return The only element accessible via this cursor.
	 * @throws IllegalStateException
	 *           when it is not the case that there is exactly one element, when
	 *           the cursor is not in pristine state.
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
	 * When this cursor provides access to one non-null element only, this method
	 * returns it wrapped in an optional.
	 * </p>
	 * 
	 * <p>
	 * In general, cursors do not guarantee that the objects they return survive
	 * subsequent invocations of {@link #next()}.
	 * </p>
	 * 
	 * <p>
	 * When it not the case that there is exactly one element, or when the only
	 * element accessible is {@code null}, this method returns an empty optional.
	 * </p>
	 * 
	 * <p>
	 * This is a rolling method.
	 * </p>
	 * 
	 * @return The only element accessible via this cursor, if any, or an empty
	 *         optional.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
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

	/**
	 * <p>
	 * Returns a view providing access to the elements of this cursor. The view
	 * passes the elements to the argument consumer before retuning them.
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
	 *          A consumer that will consume each element retrieved from this
	 *          cursor.
	 * @return A view providing access to the elements of this cursor. The view
	 *         passes the elements to the argument consumer before retuning them.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<I> peek( Consumer<? super I> consumer )
			throws IllegalStateException {
		lock( );
		return KnittingCursor.wrap( new TapCursor<I>( wrapped, consumer ) );
	}

	/**
	 * <p>
	 * Returns a view of the concatenation of the argument cursor before this
	 * cursor.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param head
	 *          A cursor to concatenate before this cursor.
	 * @return A view of the concatenation of the argument cursor before this
	 *         cursor.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public KnittingCursor<I> prepend( Cursor<? extends I> head )
			throws IllegalStateException {
		lock( );
		return wrap( new ConcatenateCursor<I>( head, wrapped ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurl)} except that the view shows elements in the
	 * arrays returned by the argument {@code array_purl}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the arrays returned by the argument
	 *          {@code array_purl}.
	 * @param array_purl
	 *          A (typically stateful) system that returns arrays.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> purlArray(
			ArrayPurl<? super I, O> array_purl )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				O[] array = array_purl.next( input );
				return array == null ? null : new ArrayCursor<>( array );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				O[] array = array_purl.end( );
				return array == null ? null : new ArrayCursor<>( array );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurl)} except that the view shows elements in the
	 * cursables returned by the argument {@code cursable_purl}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the cursables returned by the argument
	 *          {@code cursable_purl}.
	 * @param cursable_purl
	 *          A (typically stateful) system that returns cursables.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> purlCursable(
			CursablePurl<? super I, O> cursable_purl )
			throws IllegalStateException {
		lock( );

		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Cursable<O> nextCursable = cursable_purl.next( input );
				return nextCursable == null ? null : nextCursable.pull( hook );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Cursable<O> nextCursable = cursable_purl.end( );
				return nextCursable == null ? null : nextCursable.pull( hook );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(Hook, CursorPurlH)} except that the view shows elements in the
	 * cursables returned by the argument {@code cursable_purl_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the cursables returned by the argument
	 *          {@code cursable_purl_h}.
	 * @param hook
	 *          A hook.
	 * @param cursable_purl_h
	 *          A (typically stateful) system that returns cursables.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> purlCursable(
			Hook hook,
			CursablePurlH<? super I, O> cursable_purl_h )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Cursable<O> nextCursable = cursable_purl_h.next( hook, input );
				return nextCursable == null ? null : nextCursable.pull( hook );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Cursable<O> nextCursable = cursable_purl_h.end( hook );
				return nextCursable == null ? null : nextCursable.pull( hook );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a complex view. For an introduction on purling see
	 * {@link org.github.evenjn.yarn.CursorPurl CursorPurl}.
	 * </p>
	 * 
	 * <p>
	 * For each element {@code E} of this cursor, the view shows elements in the
	 * cursor {@code C} returned when invoking
	 * {@link org.github.evenjn.yarn.CursorPurl#next(Object) next(Object)} on the
	 * argument {@code cursor_purl} with argument {@code E}. Moreover, the view
	 * also includes all the elements in the cursor {@code C} returned when
	 * invoking {@link org.github.evenjn.yarn.CursorPurl#end() end()} on the
	 * argument {@code cursor_purl} at the end of this cursor.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the cursors returned by the argument
	 *          {@code cursor_purl}.
	 * @param cursor_purl
	 *          A (typically stateful) system that returns cursors.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> purlCursor(
			CursorPurl<? super I, O> cursor_purl )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				return cursor_purl.next( input );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				return cursor_purl.end( );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a complex view. For an introduction on purling see
	 * {@link org.github.evenjn.yarn.CursorPurl CursorPurl}.
	 * </p>
	 * 
	 * <p>
	 * For each element {@code E} of this cursor, the view shows elements in the
	 * cursor {@code C} returned when invoking {@code cursor_purl_h}
	 * {@link org.github.evenjn.yarn.CursorPurlH#next(Hook, Object) next(Hook,
	 * Object)} with argument {@code E} and a temporary
	 * {@link org.github.evenjn.yarn.Hook hook}. Moreover, the view also includes
	 * all the elements in the cursor {@code C} returned when invoking
	 * {@code cursor_purl_h} {@link org.github.evenjn.yarn.CursorPurlH#end(Hook)
	 * end(Hook)} with a temporary {@link org.github.evenjn.yarn.Hook hook} at the
	 * end of this cursor.
	 * </p>
	 * 
	 * <p>
	 * The temporary hook is hooked to the argument {@code hook}. The returned
	 * view closes each temporary hook at the end of each cursor {@code C}. The
	 * argument {@code hook} is in charge of closing resources in the event that
	 * the returned cursor is abandoned as garbage before reaching its end.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * 
	 * @param <O>
	 *          The type of elements in the cursors returned by the argument
	 *          {@code cursor_purl_h}.
	 * @param hook
	 *          A hook.
	 * @param cursor_purl_h
	 *          A (typically stateful) system that returns cursors.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> purlCursor(
			Hook hook,
			CursorPurlH<? super I, O> cursor_purl_h )
			throws IllegalStateException {
		lock( );
		return wrap(
				new CursorStitchProcessor<I, O>( hook, wrapped, cursor_purl_h ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurl)} except that the view shows elements in the
	 * iterables returned by the argument {@code iterable_purl}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the iterables returned by the argument
	 *          {@code iterable_purl}.
	 * @param iterable_purl
	 *          A (typically stateful) system that returns iterables.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> purlIterable(
			IterablePurl<? super I, O> iterable_purl )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Iterable<O> nextIterable = iterable_purl.next( input );
				return new IteratorCursor<>( nextIterable.iterator( ) );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Iterable<O> nextIterable = iterable_purl.end( );
				return new IteratorCursor<>( nextIterable.iterator( ) );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(Hook, CursorPurlH)} except that the view shows elements in the
	 * iterables returned by the argument {@code iterable_purl_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the iterables returned by the argument
	 *          {@code iterable_purl_h}.
	 * @param hook
	 *          A hook.
	 * @param iterable_purl_h
	 *          A (typically stateful) system that returns iterables.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> purlIterable(
			Hook hook,
			IterablePurlH<? super I, O> iterable_purl_h )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Iterable<O> nextIterable = iterable_purl_h.next( hook, input );
				return nextIterable == null ? null : new IteratorCursor<>(
						nextIterable.iterator( ) );
			}

			public Cursor<O> end( Hook hook ) {
				final Iterable<O> nextIterable = iterable_purl_h.end( hook );
				return nextIterable == null ? null : new IteratorCursor<>(
						nextIterable.iterator( ) );
			}

		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurl)} except that the view shows elements in the
	 * iterators returned by the argument {@code iterator_purl}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the iterators returned by the argument
	 *          {@code iterator_purl}.
	 * @param iterator_purl
	 *          A (typically stateful) system that returns iterators.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> purlIterator(
			IteratorPurl<? super I, O> iterator_purl )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Iterator<O> nextIterator = iterator_purl.next( input );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Iterator<O> nextIterator = iterator_purl.end( );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(Hook, CursorPurlH)} except that the view shows elements in the
	 * iterators returned by the argument {@code iterator_purl_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the iterators returned by the argument
	 *          {@code iterator_purl_h}.
	 * @param hook
	 *          A hook.
	 * @param iterator_purl_h
	 *          A (typically stateful) system that returns iterators.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> purlIterator(
			Hook hook,
			IteratorPurlH<? super I, O> iterator_purl_h )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Iterator<O> nextIterator = iterator_purl_h.next( hook, input );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Iterator<O> nextIterator = iterator_purl_h.end( hook );
				return nextIterator == null ? null
						: new IteratorCursor<>( nextIterator );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(Hook, CursorPurlH)} except that the view shows elements in the
	 * optionals returned by the argument {@code optional_purl_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the optionals returned by the argument
	 *          {@code optional_purl_h}.
	 * @param hook
	 *          A hook.
	 * @param optional_purl_h
	 *          A (typically stateful) system that returns optionals.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> purlOptional(
			Hook hook,
			OptionalPurlH<? super I, O> optional_purl_h )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Optional<O> next = optional_purl_h.next( hook, input );
				if ( next.isPresent( ) ) {
					return new SingletonCursor<O>( next.get( ) );
				}
				return null;
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				Optional<O> end = optional_purl_h.end( hook );
				if ( end.isPresent( ) ) {
					return new SingletonCursor<O>( end.get( ) );
				}
				return null;
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurl)} except that the view shows elements in the
	 * optionals returned by the argument {@code optional_purl}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the optionals returned by the argument
	 *          {@code optional_purl}.
	 * @param optional_purl
	 *          A (typically stateful) system that returns optionals.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> purlOptional(
			OptionalPurl<? super I, O> optional_purl )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				Optional<O> next = optional_purl.next( input );
				if ( next.isPresent( ) ) {
					return new SingletonCursor<O>( next.get( ) );
				}
				return null;
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				Optional<O> end = optional_purl.end( );
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
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(Hook, CursorPurlH)} except that the view shows elements in the
	 * streams returned by the argument {@code stream_purl_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the streams returned by the argument
	 *          {@code stream_purl_h}.
	 * @param hook
	 *          A hook.
	 * @param stream_purl_h
	 *          A (typically stateful) system that returns streams.
	 * @return A complex view.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public <O> KnittingCursor<O> purlStream(
			Hook hook,
			StreamPurlH<? super I, O> stream_purl_h )
			throws IllegalStateException {
		lock( );
		CursorPurlH<I, O> stitch = new CursorPurlH<I, O>( ) {

			@Override
			public Cursor<O> next( Hook hook, I input ) {
				final Stream<O> nextStream = stream_purl_h.next( hook, input );
				return nextStream == null ? null : new IteratorCursor<>(
						nextStream.iterator( ) );
			}

			@Override
			public Cursor<O> end( Hook hook ) {
				final Stream<O> nextStream = stream_purl_h.end( hook );
				return nextStream == null ? null : new IteratorCursor<>(
						nextStream.iterator( ) );
			}
		};
		return wrap( new CursorStitchProcessor<I, O>( hook, wrapped, stitch ) );
	}

	/**
	 * <p>
	 * Returns the result of a computation taking into account all the elements of
	 * this cursor.
	 * </p>
	 * 
	 * <p>
	 * This method stores into a local variable the argument {@code zero}. Then,
	 * for each element {@code E} in this cursor, this method invokes the argument
	 * {@code bifunction} using the content of the local variable and {@code E}.
	 * At the end of each such invocation, this method stores into the local
	 * variable the result of the invocation. Finally, this method returns the
	 * content of the local variable.
	 * </p>
	 * 
	 * <p>
	 * This is a rolling method.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of argument {@code zero} and of the elements returned by
	 *          the argument {@code bifunction}.
	 * @param zero
	 *          The initial value for the reduction.
	 * @param bifunction
	 *          A bifunction that will be invoked once for each element of this
	 *          cursor.
	 * @return the result of a computation taking into account all the elements of
	 *         this cursor.
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
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
	 * Invokes this cursor's {@link org.github.evenjn.yarn.Cursor#next() next()}
	 * method until a {@link org.github.evenjn.yarn.EndOfCursorException
	 * EndOfCursorException} is thrown.
	 * </p>
	 * 
	 * <p>
	 * This is a rolling method.
	 * </p>
	 * 
	 * @throws IllegalStateException
	 *           when this cursor is not in pristine state.
	 * @since 1.0
	 */
	public void roll( )
			throws IllegalStateException {
		lock( );
		try {
			for ( ;; ) {
				wrapped.next( );
			}
		}
		catch ( EndOfCursorException e ) {
		}
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

	/**
	 * <p>
	 * Returns new {@code KnittingCursor} providing access to the argument
	 * elements.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of the argument elements.
	 * @param elements
	 *          Elements to be wrapped in a new {@code KnittingCursor}.
	 * @return A new {@code KnittingCursor} providing access to the argument
	 *         elements.
	 * @since 1.0
	 */
	@SafeVarargs
	public static <K> KnittingCursor<K> on( K ... elements ) {
		return wrap( new ArrayCursor<K>( elements ) );
	}

	/**
	 * <p>
	 * Returns a view of the elements in the argument tuple.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of elements in the argument tuple.
	 * @param tuple
	 *          A tuple of elements.
	 * @return A view of the elements in the argument tuple.
	 * @since 1.0
	 */
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

	/**
	 * <p>
	 * Returns a view of the elements in the argument cursor.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of elements in the argument cursor.
	 * @param cursor
	 *          A cursor of elements.
	 * @return A view of the elements in the argument cursor.
	 * @since 1.0
	 */
	public static <K> KnittingCursor<K> wrap( Cursor<K> cursor ) {
		return new KnittingCursor<>( cursor );
	}

	/**
	 * <p>
	 * Returns a view of the elements in the argument iterable.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of elements in the argument iterable.
	 * @param iterable
	 *          An iterable of elements.
	 * @return A view of the elements in the argument iterable.
	 * @since 1.0
	 */
	public static <K> KnittingCursor<K> wrap( Iterable<K> iterable ) {
		return wrap( iterable.iterator( ) );
	}

	/**
	 * <p>
	 * Returns a view of the elements in the argument iterator.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of elements in the argument iterator.
	 * @param iterator
	 *          An iterator of elements.
	 * @return A view of the elements in the argument iterator.
	 * @since 1.0
	 */
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

	/**
	 * <p>
	 * Returns a view of the elements in the argument array.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of elements in the argument array.
	 * @param array
	 *          An array of elements.
	 * @return A view of the elements in the argument array.
	 * @since 1.0
	 */
	public static <K> KnittingCursor<K> wrap( K[] array ) {
		return wrap( new ArrayCursor<K>( array ) );
	}

	/**
	 * <p>
	 * Returns a view of the elements in the argument stream.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of elements in the argument stream.
	 * @param stream
	 *          A stream of elements.
	 * @return A view of the elements in the argument stream.
	 * @since 1.0
	 */
	public static <K> KnittingCursor<K> wrap( Stream<K> stream ) {
		return wrap( stream.iterator( ) );
	}
}
