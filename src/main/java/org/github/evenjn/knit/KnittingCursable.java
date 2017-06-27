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

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.github.evenjn.yarn.ArrayMap;
import org.github.evenjn.yarn.ArrayPurl;
import org.github.evenjn.yarn.ArrayPurlFactory;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.CursableMap;
import org.github.evenjn.yarn.CursableMapH;
import org.github.evenjn.yarn.CursablePurl;
import org.github.evenjn.yarn.CursablePurlFactory;
import org.github.evenjn.yarn.CursablePurlH;
import org.github.evenjn.yarn.CursablePurlHFactory;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.CursorMap;
import org.github.evenjn.yarn.CursorMapH;
import org.github.evenjn.yarn.CursorPurl;
import org.github.evenjn.yarn.CursorPurlFactory;
import org.github.evenjn.yarn.CursorPurlH;
import org.github.evenjn.yarn.CursorPurlHFactory;
import org.github.evenjn.yarn.EndOfCursorException;
import org.github.evenjn.yarn.Equivalencer;
import org.github.evenjn.yarn.FunctionH;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.IterableMap;
import org.github.evenjn.yarn.IterableMapH;
import org.github.evenjn.yarn.IterablePurl;
import org.github.evenjn.yarn.IterablePurlFactory;
import org.github.evenjn.yarn.IterablePurlH;
import org.github.evenjn.yarn.IterablePurlHFactory;
import org.github.evenjn.yarn.IteratorMap;
import org.github.evenjn.yarn.IteratorMapH;
import org.github.evenjn.yarn.IteratorPurl;
import org.github.evenjn.yarn.IteratorPurlFactory;
import org.github.evenjn.yarn.IteratorPurlH;
import org.github.evenjn.yarn.IteratorPurlHFactory;
import org.github.evenjn.yarn.OptionMap;
import org.github.evenjn.yarn.OptionMapH;
import org.github.evenjn.yarn.OptionalPurl;
import org.github.evenjn.yarn.OptionalPurlFactory;
import org.github.evenjn.yarn.OptionalPurlH;
import org.github.evenjn.yarn.OptionalPurlHFactory;
import org.github.evenjn.yarn.StreamMapH;
import org.github.evenjn.yarn.StreamPurlH;
import org.github.evenjn.yarn.StreamPurlHFactory;

/**
 * 
 * <h1>KnittingCursable</h1>
 * 
 * <p>
 * A {@code KnittingCursable} wraps a cursable and provides utility methods to
 * access its contents.
 * </p>
 * 
 * <p>
 * Briefly, a {@code KnittingCursable} may be used in three ways:
 * </p>
 * 
 * <ul>
 * <li>As a simple Cursable, invoking the
 * {@link org.github.evenjn.knit.KnittingCursable#pull(Hook) pull} method;</li>
 * <li>As a resource to be harvested, invoking a rolling method such as
 * {@link #collect(Collection)};</li>
 * <li>As a resource to transform, invoking a transformation method such as
 * {@link #map(Function)};</li>
 * </ul>
 * 
 * <p>
 * Unlike {@code KnittingCursor}, these three modes of operation are not
 * exclusive: a {@code KnittingCursable} may be used several times, in any mode.
 * </p>
 * 
 * <h2>Methods of a KnittingCursable</h2>
 * 
 * <p>
 * Non-static public methods of {@code KnittingCursable} fall into one of the
 * following four categories:
 * </p>
 * 
 * <ul>
 * <li>Object methods (inherited from {@link java.lang.Object Object})</li>
 * <li>Cursable methods ({@link #pull(Hook)})</li>
 * <li>Rolling methods (listed below)</li>
 * <li>Transformation methods (listed below)</li>
 * </ul>
 *
 * <p>
 * Rolling methods instantiate a {@link KnittingCursor} by invoking
 * {@link #pull(Hook)} and repeatedly invoke the method
 * {@link KnittingCursor#map(Hook, FunctionH)} typically (but not necessarily)
 * until the end is reached. The following methods are rolling:
 * </p>
 * 
 * <ul>
 * <li>{@link #collect(Collection)}</li>
 * <li>{@link #consume(Function)}</li>
 * <li>{@link #count()}</li>
 * <li>{@link #equivalentTo(Cursable)}</li>
 * <li>{@link #equivalentTo(Cursable, Equivalencer)}</li>
 * <li>{@link #one()}</li>
 * <li>{@link #optionalOne()}</li>
 * <li>{@link #reduce(Object, BiFunction)}</li>
 * <li>{@link #roll()}</li>
 * </ul>
 * 
 * 
 * <p>
 * Transformations are a group of methods that return a new
 * {@code KnittingCursable} object (or something similar), which provides a new
 * view of the contents of the wrapped cursable. Transformation methods do not
 * instantiate cursors at the time of their invocation; they return lazy
 * wrappers. The following methods are transformations:
 * </p>
 * 
 * <ul>
 * <li>{@link #append(Cursable)}</li>
 * <li>{@link #asIterator(Hook)}</li>
 * <li>{@link #asStream(Hook)}</li>
 * <li>{@link #crop(Predicate)}</li>
 * <li>{@link #entwine(Cursable, BiFunction)}</li>
 * <li>{@link #filter(Predicate)}</li>
 * <li>{@link #flatmapArray(ArrayMap)}</li>
 * <li>{@link #flatmapCursable(CursableMap)}</li>
 * <li>{@link #flatmapCursable(CursableMapH)}</li>
 * <li>{@link #flatmapCursor(CursorMap)}</li>
 * <li>{@link #flatmapCursor(CursorMapH)}</li>
 * <li>{@link #flatmapIterable(IterableMap)}</li>
 * <li>{@link #flatmapIterable(IterableMapH)}</li>
 * <li>{@link #flatmapIterator(IteratorMap)}</li>
 * <li>{@link #flatmapIterator(IteratorMapH)}</li>
 * <li>{@link #flatmapOptional(OptionMap)}</li>
 * <li>{@link #flatmapOptional(OptionMapH)}</li>
 * <li>{@link #flatmapStream(StreamMapH)}</li>
 * <li>{@link #head(int, int)}</li>
 * <li>{@link #headless(int)}</li>
 * <li>{@link #map(Function)}</li>
 * <li>{@link #map(FunctionH)}</li>
 * <li>{@link #numbered()}</li>
 * <li>{@link #peek(Consumer)}</li>
 * <li>{@link #prepend(Cursable)}</li>
 * <li>{@link #purlArray(ArrayPurlFactory)}</li>
 * <li>{@link #purlCursable(CursablePurlFactory)}</li>
 * <li>{@link #purlCursable(CursablePurlHFactory)}</li>
 * <li>{@link #purlCursor(CursorPurlFactory)}</li>
 * <li>{@link #purlCursor(CursorPurlHFactory)}</li>
 * <li>{@link #purlIterable(IterablePurlFactory)}</li>
 * <li>{@link #purlIterable(IterablePurlHFactory)}</li>
 * <li>{@link #purlIterator(IteratorPurlFactory)}</li>
 * <li>{@link #purlIterator(IteratorPurlHFactory)}</li>
 * <li>{@link #purlOptional(OptionalPurlFactory)}</li>
 * <li>{@link #purlOptional(OptionalPurlHFactory)}</li>
 * <li>{@link #purlStream(StreamPurlHFactory)}</li>
 * </ul>
 * 
 * @param <I>
 *          The type of elements accessible via this cursable.
 * @see org.github.evenjn.knit
 * @since 1.0
 */
public class KnittingCursable<I> implements
		Cursable<I> {

	private final Cursable<I> wrapped;

	private KnittingCursable(Cursable<I> cursable) {
		this.wrapped = cursable;
	}

	/**
	 * Returns a view of the concatenation of the argument cursable after this
	 * cursable.
	 * 
	 * @param tail
	 *          A cursable to concatenate after this cursable.
	 * @return A view of the concatenation of the argument cursable after this
	 *         cursable.
	 * @since 1.0
	 */
	public KnittingCursable<I> append( final Cursable<? extends I> tail ) {
		return wrap( new ConcatenateCursable<I>( wrapped, tail ) );
	}

	/**
	 * <p>
	 * Returns a view of this cursable as a {@link java.util.Iterator}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param hook
	 *          A hook.
	 * @return A view of this cursable as a {@link java.util.Iterator}.
	 * @since 1.0
	 */
	public Iterator<I> asIterator( Hook hook ) {
		return pull( hook ).asIterator( );
	}

	/**
	 * <p>
	 * Returns a view of this cursable as a {@link java.util.stream.Stream}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param hook
	 *          A hook.
	 * @return A view of this cursable as a {@link java.util.stream.Stream}.
	 * @since 1.0
	 */
	public Stream<I> asStream( Hook hook ) {
		return pull( hook ).asStream( );
	}

	/**
	 * <p>
	 * Adds all elements of this cursable to the argument collection, then returns
	 * it.
	 * </p>
	 * 
	 * <p>
	 * The objects collected may be dead. This is due to the fact that cursors do
	 * not guarantee that the objects they return survive subsequent invocations
	 * of {@link org.github.evenjn.yarn.Cursor#next() next()}, or closing the
	 * hook.
	 * </p>
	 * 
	 * @param <K>
	 *          The type the argument collection.
	 * @param collection
	 *          The collection to add elements to.
	 * @return The argument collection.
	 * @since 1.0
	 */
	public <K extends Collection<? super I>> K collect( K collection ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			return pull( hook ).collect( collection );
		}
	}

	/**
	 * <p>
	 * Feeds a consumer with the elements of this cursable.
	 * </p>
	 * 
	 * <p>
	 * Obtains a consumer from the argument {@code consumer_provider}, hooking it
	 * to a local, temporary hook. Then, this method obtains a cursor using this
	 * cursable's {@link #pull(Hook)}, hooking it to the same local hook.
	 * </p>
	 * 
	 * <p>
	 * Finally, this method invokes that cursor's
	 * {@link org.github.evenjn.yarn.Cursor#next() next()} method, passing each
	 * object obtained this way to the consumer, until the end of the cursor.
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
	 * @since 1.0
	 */
	public <K extends Consumer<? super I>> void consume(
			Function<Hook, K> consumer_provider ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			K consumer = consumer_provider.apply( hook );
			pull( hook ).peek( consumer ).roll( );
		}
	}

	/**
	 * <p>
	 * Returns a cursable where each element is a cursor providing access to a
	 * subsequence of contiguous elements in this cursable that satisfy the
	 * argument {@code stateless_predicate}.
	 * </p>
	 * 
	 * @param stateless_predicate
	 *          A stateless system that identifies elements that should be kept
	 *          together.
	 * @return a cursable where each element is a cursor providing access to a
	 *         subsequence of contiguous elements in this cursable that satisfy
	 *         the argument {@code stateless_predicate}.
	 * @since 1.0
	 */
	public KnittingCursable<KnittingCursor<I>>
			crop( Predicate<I> stateless_predicate ) {
		return KnittingCursable.wrap( h -> pull( h ).crop( stateless_predicate ) );
	}

	/**
	 * <p>
	 * Returns a cursable that traverses this cursable and the argument
	 * {@code other_cursor} in parallel, applying the argument
	 * {@code stateless_bifunction} to each pair of elements, and providing a view
	 * of the result of each application.
	 * </p>
	 * 
	 * <p>
	 * The returned cursable provides as many elements as the cursor with the
	 * least elements.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <R>
	 *          The type of elements accessible via the argument cursable.
	 * @param <M>
	 *          The type of elements returned by the bifunction.
	 * @param other_cursable
	 *          The cursable to roll in parallel to this cursable.
	 * @param stateless_bifunction
	 *          The stateless bifunction to apply to each pair of element.
	 * @return a cursable that traverses this cursable and the argument cursable
	 *         in parallel, applying the argument bifunction to each pair of
	 *         elements, and providing a view of the result of each application.
	 * @since 1.0
	 */
	public <R, M> KnittingCursable<M> entwine(
			Cursable<R> other_cursable,
			BiFunction<I, R, M> stateless_bifunction ) {
		KnittingCursable<I> outer = this;
		Cursable<M> result = new Cursable<M>( ) {

			@Override
			public Cursor<M> pull( Hook hook ) {
				return outer.pull( hook ).entwine( other_cursable.pull( hook ),
						stateless_bifunction );
			}
		};
		return wrap( result );
	}

	private static <T> boolean equal_null( T first, T second ) {
		if ( first == null && second == null )
			return true;
		if ( first == null || second == null )
			return false;
		return first.equals( second );
	}

	/**
	 * <p>
	 * Returns true when this cursable and the {@code other} cursable have the
	 * same number of elements, and when each i-th element of this cursable is
	 * equivalent to the i-th element of the {@code other} cursable. Returns false
	 * otherwise.
	 * </p>
	 * 
	 * <p>
	 * For the purposes of this method, two {@code null} elements are equivalent.
	 * </p>
	 * 
	 * <p>
	 * This is a rolling method.
	 * </p>
	 * 
	 * @param other
	 *          Another cursable.
	 * @return true when this cursable and the {@code other} cursable have the
	 *         same number of elements, and when each i-th element of this
	 *         cursable is equivalent to the i-th element of the {@code other}
	 *         cursable. False otherwise.
	 * @since 1.0
	 */
	public boolean equivalentTo( Cursable<?> other ) {
		if ( other == this )
			return true;
		return equivalentTo( other, KnittingCursable::equal_null );
	}

	/**
	 * <p>
	 * Returns true when this cursable and the {@code other} cursable have the
	 * same number of elements, and when each i-th element of this cursable is
	 * equivalent to the i-th element of the {@code other} cursable. Returns false
	 * otherwise.
	 * </p>
	 * 
	 * <p>
	 * This is a rolling method.
	 * </p>
	 * 
	 * @param other
	 *          Another cursable.
	 * @param equivalencer
	 *          A function that tells whether two objects are equivalent.
	 * @return true when this cursable and the {@code other} cursable have the
	 *         same number of elements, and when each i-th element of this
	 *         cursable is equivalent to the i-th element of the {@code other}
	 *         cursable. False otherwise.
	 * @since 1.0
	 */
	public boolean equivalentTo( Cursable<?> other,
			Equivalencer<I, Object> equivalencer ) {
		if ( other == this )
			return true;
		Cursable<?> o = (Cursable<?>) other;

		try ( AutoHook hook = new BasicAutoHook( ) ) {
			KnittingCursor<I> pull1 = this.pull( hook );
			KnittingCursor<?> pull2 = KnittingCursor.wrap( o.pull( hook ) );

			for ( ;; ) {
				boolean hasNext1 = pull1.hasNext( );
				boolean hasNext2 = pull2.hasNext( );
				if ( hasNext1 != hasNext2 ) {
					return false;
				}
				if ( !hasNext1 ) {
					return true;
				}
				I next1 = pull1.next( );
				Object next2 = pull2.next( );
				if ( !equivalencer.equivalent( next1, next2 ) ) {
					return false;
				}
			}
		}
		catch ( EndOfCursorException e ) {
			throw new IllegalStateException( e );
		}
	}

	/**
	 * <p>
	 * Returns a view hiding the elements which do not satisfy the argument
	 * {@code stateless_predicate} in this cursable.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param stateless_predicate
	 *          A stateless system that decides to show or hide elements.
	 * @return A view hiding the elements which do not satisfy the argument
	 *         {@code stateless_predicate} in this cursable.
	 * @since 1.0
	 */
	public KnittingCursable<I>
			filter( Predicate<? super I> stateless_predicate ) {
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return new FilterCursor<>( wrapped.pull( hook ), stateless_predicate );
			}
		} );
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
	 *          The type of elements in the cursors returned by the argument
	 *          {@code stateless_array_map}.
	 * @param stateless_array_map
	 *          A stateless function that returns arrays.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O> flatmapArray(
			ArrayMap<? super I, O> stateless_array_map ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.flatmapArray( stateless_array_map );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmapCursor(CursorMap)} except that the view shows elements in
	 * the cursables returned by the argument {@code stateless_cursable_map}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the cursables returned by the argument
	 *          {@code stateless_cursable_map}.
	 * @param stateless_cursable_map
	 *          A stateless function that returns cursables.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O> flatmapCursable(
			CursableMap<? super I, O> stateless_cursable_map ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.flatmapCursable( hook, stateless_cursable_map );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmapCursor(CursorMapH)} except that the view shows elements in
	 * the cursables returned by the argument {@code stateless_cursable_map_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the cursables returned by the argument
	 *          {@code stateless_cursable_map_h}.
	 * @param stateless_cursable_map_h
	 *          A stateless function that returns cursables.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O> flatmapCursable(
			CursableMapH<? super I, O> stateless_cursable_map_h ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.flatmapCursable( hook, stateless_cursable_map_h );
			}
		} );
	}

	/**
	 * <p>
	 * Each invocation of {@link #pull(Hook)} on the returned
	 * {@code KnittingCursable} pulls a new {@code KnittingCursor} from this
	 * cursable, transforms it using
	 * {@link KnittingCursor#flatmapCursor(CursorMap)} with the argument
	 * {@code stateless_cursor_map}, then returns the resulting cursor.
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
	 * @since 1.0
	 */
	public <O> KnittingCursable<O> flatmapCursor(
			CursorMap<? super I, O> stateless_cursor_map ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.flatmapCursor( stateless_cursor_map );
			}
		} );
	}

	/**
	 * <p>
	 * Each invocation of {@link #pull(Hook)} on the returned
	 * {@code KnittingCursable} pulls a new {@code KnittingCursor} from this
	 * cursable, transforms it using
	 * {@link KnittingCursor#flatmapCursor(Hook, CursorMapH)} with the hook
	 * already available and the argument {@code stateless_cursor_map_h}, then
	 * returns the resulting cursor.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the cursors returned by the argument
	 *          {@code stateless_cursor_map_h}.
	 * @param stateless_cursor_map_h
	 *          A stateless function that returns cursors.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O> flatmapCursor(
			CursorMapH<? super I, O> stateless_cursor_map_h ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.flatmapCursor( hook, stateless_cursor_map_h );
			}
		} );
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
	 * @since 1.0
	 */
	public <O> KnittingCursable<O> flatmapIterable(
			IterableMap<? super I, O> stateless_iterable_map ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.flatmapIterable( stateless_iterable_map );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmapCursor(CursorMapH)} except that the view shows elements in
	 * the iterables returned by the argument {@code stateless_iterable_map_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the iterables returned by the argument
	 *          {@code stateless_iterable_map_h}.
	 * @param stateless_iterable_map_h
	 *          A stateless function that returns iterators.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O> flatmapIterable(
			IterableMapH<? super I, O> stateless_iterable_map_h ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.flatmapIterable( hook, stateless_iterable_map_h );
			}
		} );
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
	 * @since 1.0
	 */
	public <O> KnittingCursable<O> flatmapIterator(
			IteratorMap<? super I, O> stateless_iterator_map ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.flatmapIterator( stateless_iterator_map );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmapCursor(CursorMapH)} except that the view shows elements in
	 * the iterators returned by the argument {@code stateless_iterator_map_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the iterators returned by the argument
	 *          {@code stateless_iterator_map_h}.
	 * @param stateless_iterator_map_h
	 *          A stateless function that returns iterators.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O> flatmapIterator(
			IteratorMapH<? super I, O> stateless_iterator_map_h ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.flatmapIterator( hook, stateless_iterator_map_h );
			}
		} );
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
	 * @since 1.0
	 */
	public <O> KnittingCursable<O> flatmapOptional(
			OptionMap<? super I, O> stateless_optional_map ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.flatmapOptional( stateless_optional_map );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmapCursor(CursorMapH)} except that the view shows elements in
	 * the optionals returned by the argument {@code stateless_optional_map_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the optionals returned by the argument
	 *          {@code stateless_optional_map_h}.
	 * @param stateless_optional_map_h
	 *          A stateless function that returns optionals.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O> flatmapOptional(
			OptionMapH<? super I, O> stateless_optional_map_h )
			throws IllegalStateException {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.flatmapOptional( hook, stateless_optional_map_h );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #flatmapCursor(CursorMapH)} except that the view shows elements in
	 * the streams returned by the argument {@code stateless_stream_map_h}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in the streams returned by the argument
	 *          {@code stateless_stream_map_h}.
	 * @param stateless_stream_map_h
	 *          A stateless function that returns streams.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O> flatmapStream(
			StreamMapH<? super I, O> stateless_stream_map_h ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.flatmapStream( hook, stateless_stream_map_h );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view showing the first {@code show} elements of this cursable
	 * visible after hiding the first {@code hide} elements.
	 * </p>
	 * 
	 * <p>
	 * The returned view may be empty. This happens when this cursable's size is
	 * smaller than {@code hide}.
	 * </p>
	 * 
	 * <p>
	 * The returned view may contain less than {@code show} elements. This happens
	 * when this cursable's size is smaller than {@code hide + show}.
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
	 * @return A view showing the first {@code show} elements of this cursable
	 *         visible after hiding the first {@code hide} elements.
	 * @since 1.0
	 */
	public KnittingCursable<I> head( int hide, int show ) {
		int final_show = show < 0 ? 0 : show;
		int final_hide = hide < 0 ? 0 : hide;
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return Subcursor.sub( wrapped.pull( hook ), final_hide, final_show );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view hiding the first {@code hide} elements of this cursable.
	 * </p>
	 * 
	 * <p>
	 * The returned view may be empty. This happens when this cursable's size is
	 * smaller than {@code hide}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param hide
	 *          The number of elements to hide. A negative numbers counts as zero.
	 * @return A view hiding the first {@code hide} elements of this cursable.
	 * @since 1.0
	 */
	public KnittingCursable<I> headless( int hide ) {
		int final_hide = hide < 0 ? 0 : hide;
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return Subcursor.skip( wrapped.pull( hook ), final_hide );
			}
		} );
	}

	/**
	 * <p>
	 * Each invocation of {@link #pull(Hook)} on the returned
	 * {@code KnittingCursable} pulls a new {@code KnittingCursor} from this
	 * cursable, transforms it using {@link KnittingCursor#map(Function)} with the
	 * argument {@code stateless_function}, then returns the resulting cursor.
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
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			map( Function<? super I, O> stateless_function ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.map( stateless_function );
			}
		} );
	}

	/**
	 * <p>
	 * Each invocation of {@link #pull(Hook)} on the returned
	 * {@code KnittingCursable} pulls a new {@code KnittingCursor} from this
	 * cursable, transforms it using {@link KnittingCursor#map(Hook, FunctionH)}
	 * with the hook already available and the argument
	 * {@code stateless_function_h}, then returns the resulting cursor.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements returned by the argument
	 *          {@code stateless_function_h}.
	 * @param stateless_function_h
	 *          A stateless function.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			map( FunctionH<? super I, O> stateless_function_h ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.map( hook, stateless_function_h );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view that, for each element of this cursable, provides a
	 * {@linkplain org.github.evenjn.yarn.Bi pair} containing the element itself
	 * and the number of elements preceding it.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @return A view that, for each element of this cursable, provides a
	 *         {@linkplain org.github.evenjn.yarn.Bi pair} containing the element
	 *         itself and the number of elements preceding it.
	 * @since 1.0
	 */
	public KnittingCursable<Bi<I, Integer>> numbered( ) {
		return wrap( new Cursable<Bi<I, Integer>>( ) {

			@Override
			public Cursor<Bi<I, Integer>> pull( Hook hook ) {
				return new NumberedCursor<>( wrapped.pull( hook ) );
			}
		} );
	}

	/**
	 * <p>
	 * When this cursable provides access to one element only, this method returns
	 * it. Otherwise, it throws an {@code IllegalStateException}.
	 * </p>
	 * 
	 * <p>
	 * The object returned may be dead. This is due to the fact that cursables do
	 * not guarantee that the objects they return survive subsequent invocations
	 * of {@link org.github.evenjn.yarn.Cursor#next() next()} on the cursors they
	 * provide, or closing the hook.
	 * </p>
	 * 
	 * <p>
	 * This is a rolling method.
	 * </p>
	 * 
	 * @return The only element accessible via this cursable.
	 * @throws IllegalStateException
	 *           when it is not the case that there is exactly one element.
	 * @since 1.0
	 */
	public I one( ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			return pull( hook ).one( );
		}
	}

	/**
	 * <p>
	 * When this cursable provides access to one non-null element only, this
	 * method returns it wrapped in an optional.
	 * </p>
	 * 
	 * <p>
	 * The object returned may be dead. This is due to the fact that cursables do
	 * not guarantee that the objects they return survive subsequent invocations
	 * of {@link org.github.evenjn.yarn.Cursor#next() next()} on the cursors they
	 * provide, or closing the hook.
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
	 * @return The only element accessible via this cursable, if any, or an empty
	 *         optional.
	 * @since 1.0
	 */
	public Optional<I> optionalOne( ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			return pull( hook ).optionalOne( );
		}
	}

	/**
	 * <p>
	 * Returns a view providing access to the elements of this cursable. The view
	 * passes the elements to the argument consumer.
	 * </p>
	 * 
	 * <p>
	 * For each {@code KnittingCursor} obtained invoking {@link #pull(Hook)} on
	 * the returned {@code KnittingCursable}, at each invocation of
	 * {@link org.github.evenjn.yarn.Cursor#next() next()}, that
	 * {@code KnittingCursor} fetches the next element from the cursor it wraps,
	 * then invokes the consumer using the fetched element as argument, then
	 * returns the fetched element.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param consumer
	 *          A consumer that will consume each element retrieved from this
	 *          cursor.
	 * @return A view providing access to the elements of this cursable. Before
	 *         retuning them, the view passes the elements to the argument
	 *         consumer.
	 * @since 1.0
	 */
	public KnittingCursable<I>
			peek( Consumer<? super I> consumer ) {
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return new TapCursor<I>( wrapped.pull( hook ), consumer );
			}

		} );
	}

	/**
	 * <p>
	 * Returns a view of the concatenation of the argument cursable before this
	 * cursable.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param head
	 *          A cursable to concatenate before this cursable.
	 * @return A view of the concatenation of the argument cursable before this
	 *         cursable.
	 * @since 1.0
	 */
	public KnittingCursable<I> prepend( final Cursable<? extends I> head ) {
		KnittingCursable<I> outer_cursable = this;
		Cursable<I> result = new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return outer_cursable.pull( hook ).prepend( head.pull( hook ) );
			}
		};
		return wrap( result );
	}

	/**
	 * <p>
	 * Returns a {@code KnittingCursor} wrapping a cursor obtained from the
	 * wrapped cursable.
	 * </p>
	 * 
	 * @param hook
	 *          A hook.
	 * @return A {@code KnittingCursor} wrapping a cursor obtained from the
	 *         wrapped cursable.
	 * @since 1.0
	 */
	@Override
	public KnittingCursor<I> pull( Hook hook ) {
		return KnittingCursor.wrap( wrapped.pull( hook ) );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurlFactory)} except that the view shows elements
	 * in arrays returned by {@link ArrayPurl} objects supplied by the argument
	 * {@code factory}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in arrays returned by {@link ArrayPurl}
	 *          objects supplied by the argument {@code factory}.
	 * @param factory
	 *          A supplier of {@link ArrayPurl} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			purlArray( ArrayPurlFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).purlArray( factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurlFactory)} except that the view shows elements
	 * in cursables returned by {@link CursablePurl} objects supplied by the
	 * argument {@code factory}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in cursables returned by {@link CursablePurl}
	 *          objects supplied by the argument {@code factory}.
	 * @param factory
	 *          A supplier of {@link CursablePurl} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			purlCursable( CursablePurlFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).purlCursable( factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurlHFactory)} except that the view shows elements
	 * in arrays returned by {@link CursablePurlH} objects supplied by the
	 * argument {@code factory}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in cursables returned by
	 *          {@link CursablePurlH} objects supplied by the argument
	 *          {@code factory}.
	 * @param factory
	 *          A supplier of {@link CursablePurlH} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			purlCursable( CursablePurlHFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).purlCursable( hook, factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a complex view.
	 * </p>
	 * 
	 * <p>
	 * Pulling a cursor from the returned cursable returns a purl of a cursor
	 * pulled from this cursable. For an introduction on purling see
	 * {@link org.github.evenjn.yarn.CursorPurl CursorPurl}.
	 * </p>
	 * 
	 * <p>
	 * In detail, when the client invokes {@link Cursable#pull(Hook) pull} on the
	 * returned cursable, that cursable pulls a new {@link KnittingCursor} from
	 * this cursable, then it obtains a new {@link CursorPurl} from the argument
	 * factory, and finally returns the cursor obtained by invoking
	 * {@link KnittingCursor#purlCursor(CursorPurl) purlCursor} using the argument
	 * purl.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * 
	 * @param <O>
	 *          The type of elements in cursors produced by {@link CursorPurl}
	 *          objects supplied by the argument {@code factory}.
	 * 
	 * @param factory
	 *          A supplier of {@link CursorPurl} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			purlCursor( CursorPurlFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull )
						.purlCursor( factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a complex view.
	 * </p>
	 * 
	 * <p>
	 * Pulling a cursor from the returned cursable returns a purl of a cursor
	 * pulled from this cursable. For an introduction on purling see
	 * {@link org.github.evenjn.yarn.CursorPurl CursorPurl}.
	 * </p>
	 * 
	 * <p>
	 * In detail, when the client invokes {@link Cursable#pull(Hook) pull} on the
	 * returned cursable, that cursable pulls a new {@link KnittingCursor} from
	 * this cursable, then it obtains a new {@link CursorPurlH} from the argument
	 * {@code factory}, and finally returns the cursor obtained by invoking
	 * {@link KnittingCursor#purlCursor(Hook, CursorPurlH) purlCursable} using the
	 * argument purl.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * 
	 * @param <O>
	 *          The type of elements in cursors produced by {@link CursorPurlH}
	 *          objects supplied by the argument {@code factory}.
	 * 
	 * @param factory
	 *          A supplier of {@link CursorPurlH} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			purlCursor( CursorPurlHFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull )
						.purlCursor( hook, factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurlFactory)} except that the view shows elements
	 * in iterables returned by {@link IterablePurl} objects supplied by the
	 * argument {@code factory}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in iterables returned by {@link IterablePurl}
	 *          objects supplied by the argument {@code factory}.
	 * @param factory
	 *          A supplier of {@link IterablePurl} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			purlIterable( IterablePurlFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).purlIterable( factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurlHFactory)} except that the view shows elements
	 * in iterables returned by {@link IterablePurlH} objects supplied by the
	 * argument {@code factory}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in iterables returned by
	 *          {@link IterablePurlH} objects supplied by the argument
	 *          {@code factory}.
	 * @param factory
	 *          A supplier of {@link IterablePurlH} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			purlIterable( IterablePurlHFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).purlIterable( hook, factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurlFactory)} except that the view shows elements
	 * in iterators returned by {@link IteratorPurl} objects supplied by the
	 * argument {@code factory}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in iterators returned by {@link IteratorPurl}
	 *          objects supplied by the argument {@code factory}.
	 * @param factory
	 *          A supplier of {@link IteratorPurl} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			purlIterator( IteratorPurlFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).purlIterator( factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurlHFactory)} except that the view shows elements
	 * in iterators returned by {@link IteratorPurlH} objects supplied by the
	 * argument {@code factory}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in iterators returned by
	 *          {@link IteratorPurlH} objects supplied by the argument
	 *          {@code factory}.
	 * @param factory
	 *          A supplier of {@link IteratorPurlH} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			purlIterator( IteratorPurlHFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).purlIterator( hook, factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurlFactory)} except that the view shows elements
	 * in optionals returned by {@link OptionalPurl} objects supplied by the
	 * argument {@code factory}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in optionals returned by {@link OptionalPurl}
	 *          objects supplied by the argument {@code factory}.
	 * @param factory
	 *          A supplier of {@link OptionalPurl} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O> purlOptional(
			OptionalPurlFactory<? super I, O> factory )
			throws IllegalStateException {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).purlOptional(
						factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurlHFactory)} except that the view shows elements
	 * in optionals returned by {@link OptionalPurlH} objects supplied by the
	 * argument {@code factory}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in optionals returned by
	 *          {@link OptionalPurlH} objects supplied by the argument
	 *          {@code factory}.
	 * @param factory
	 *          A supplier of {@link OptionalPurlH} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O> purlOptional(
			OptionalPurlHFactory<? super I, O> factory )
			throws IllegalStateException {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).purlOptional( hook,
						factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurlHFactory)} except that the view shows elements
	 * in streams returned by {@link StreamPurlH} objects supplied by the argument
	 * {@code factory}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in streams returned by {@link StreamPurlH}
	 *          objects supplied by the argument {@code factory}.
	 * @param factory
	 *          A supplier of {@link StreamPurlH} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			purlStream( StreamPurlHFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).purlStream(
						hook, factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * Returns the result of a computation taking into account all the elements of
	 * this cursable.
	 * </p>
	 * 
	 * <p>
	 * This method stores into a local variable the argument {@code zero}. Then,
	 * for each element {@code E} in this cursable, this method invokes the
	 * argument {@code bifunction} using the content of the local variable and
	 * {@code E}. At the end of each such invocation, this method stores into the
	 * local variable the result of the invocation. Finally, this method returns
	 * the content of the local variable.
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
	 *          cursable.
	 * @return the result of a computation taking into account all the elements of
	 *         this cursable.
	 * @since 1.0
	 */
	public <K> K reduce( K zero, BiFunction<K, I, K> fun ) {
		K reduction = zero;
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Cursor<I> kc = wrapped.pull( hook );
			try {
				for ( ;; ) {
					reduction = fun.apply( reduction, kc.next( ) );
				}
			}
			catch ( EndOfCursorException e ) {
			}
			return reduction;
		}
	}

	/**
	 * <p>
	 * Pulls a {@code KnittingCursor} from this cursable and invokes
	 * {@link KnittingCursor#roll() roll()} on it.
	 * </p>
	 * 
	 * <p>
	 * This is a rolling method.
	 * </p>
	 * 
	 * @since 1.0
	 */
	public void roll( ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			pull( hook ).roll( );
		}
	}

	/**
	 * <p>
	 * Counts the number of elements in this cursable.
	 * </p>
	 * 
	 * <p>
	 * Pulls a {@code KnittingCursor} from this cursable, invokes
	 * {@link KnittingCursor#count()}, and returns the result of that invocation.
	 * </p>
	 * 
	 * <p>
	 * This is a rolling method.
	 * </p>
	 * 
	 * @return The number of elements in this cursable.
	 * @since 1.0
	 */
	public int count( ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			return pull( hook ).count( );
		}
	}

	/**
	 * <p>
	 * Returns a view of the last {@code show} elements visible after hiding the
	 * last {@code hide} elements in this cursable.
	 * </p>
	 * 
	 * <p>
	 * The returned view may be empty. This happens when this cursable's size is
	 * smaller than {@code hide}.
	 * </p>
	 * 
	 * <p>
	 * The returned view may contain less than {@code show} elements. This happens
	 * when this cursable's size is smaller than {@code hide + show}.
	 * </p>
	 * 
	 * <p>
	 * This operation relies on {@code size()}. Therefore, it has a one-time
	 * computational time cost linear to the size of this cursable.
	 * </p>
	 *
	 * @param hide
	 *          The number of elements to hide. A negative numbers counts as zero.
	 * @param show
	 *          The number of elements to show. A negative numbers counts as zero.
	 * @return A view of the last {@code show} elements visible after hiding the
	 *         last {@code hide} elements in this cursable.
	 */
	@SuppressWarnings("unused")
	private KnittingCursable<I> tail( int hide, int show ) {
		int final_show = show < 0 ? 0 : show;
		int final_hide = hide < 0 ? 0 : hide;
		int len = count( ) - final_hide;
		if ( len > final_show ) {
			len = final_show;
		}
		int skip = count( ) - ( final_hide + len );
		if ( skip < 0 ) {
			skip = 0;
		}
		int final_len = len;
		int final_skip = skip;
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return Subcursor.sub( wrapped.pull( hook ), final_skip, final_len );
			}
		} );
	}

	/**
	 * <p>
	 * Returns a view hiding the last {@code hide} elements in this cursable.
	 * </p>
	 * 
	 * <p>
	 * The returned view may be empty. This happens when this cursable's size is
	 * smaller than {@code hide}.
	 * </p>
	 * 
	 * <p>
	 * This operation relies on {@code size()}. Therefore, it has a one-time
	 * computational time cost linear to the size of this cursable.
	 * </p>
	 *
	 * @param hide
	 *          The number of elements to hide. A negative numbers counts as zero.
	 * @return A view hiding the last {@code hide} elements in this cursable.
	 */
	@SuppressWarnings("unused")
	private KnittingCursable<I> tailless( int hide ) {
		int final_hide = hide < 0 ? 0 : hide;
		int len = count( ) - final_hide;
		if ( len < 0 ) {
			len = 0;
		}
		int final_len = len;
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return Subcursor.sub( wrapped.pull( hook ), 0, final_len );
			}
		} );
	}

	/**
	 * <p>
	 * Returns an empty cursable.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of elements in the empty cursable.
	 * @return An empty cursable.
	 * 
	 * @since 1.0
	 */
	public static <K> KnittingCursable<K> empty( ) {
		return private_empty( );
	}

	@SuppressWarnings("unchecked")
	private static <K> KnittingCursable<K> private_empty( ) {
		return (KnittingCursable<K>) neo;
	}

	private static final KnittingCursable<Void> neo =
			wrap( new Cursable<Void>( ) {

				@Override
				public Cursor<Void> pull( Hook hook ) {
					return KnittingCursor.empty( );
				}
			} );

	/**
	 * <p>
	 * Returns new {@code KnittingCursable} providing access to the argument
	 * elements.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of the argument elements.
	 * @param elements
	 *          Elements to be wrapped in a new {@code KnittingCursable}.
	 * @return A new {@code KnittingCursable} providing access to the argument
	 *         elements.
	 * @since 1.0
	 */
	@SafeVarargs
	public static <K> KnittingCursable<K> on( K ... elements ) {
		Cursable<K> cursable = new Cursable<K>( ) {

			@Override
			public Cursor<K> pull( Hook hook ) {
				return new ArrayCursor<K>( elements );
			}
		};
		return wrap( cursable );
	}

	/**
	 * <p>
	 * Returns a view of the elements in the argument cursable.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of elements in the argument cursable.
	 * @param cursable
	 *          A cursable of elements.
	 * @return A view of the elements in the argument cursable.
	 * @since 1.0
	 */
	public static <K> KnittingCursable<K> wrap( Cursable<K> cursable ) {
		if ( cursable instanceof KnittingCursable ) {
			return (KnittingCursable<K>) cursable;
		}
		return new KnittingCursable<K>( cursable );
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
	public static <K> KnittingCursable<K> wrap( Iterable<K> iterable ) {
		return wrap( new IterableCursable<K>( iterable ) );
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
	public static <K> KnittingCursable<K> wrap( K[] array ) {
		return wrap( new ArrayCursable<K>( array ) );
	}
}
