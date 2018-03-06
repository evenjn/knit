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

import org.github.evenjn.lang.BasicRook;
import org.github.evenjn.lang.Equivalencer;
import org.github.evenjn.lang.Ring;
import org.github.evenjn.lang.Rook;
import org.github.evenjn.yarn.ArrayMap;
import org.github.evenjn.yarn.ArrayPurl;
import org.github.evenjn.yarn.ArrayPurler;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.CursableMap;
import org.github.evenjn.yarn.CursablePurl;
import org.github.evenjn.yarn.CursablePurler;
import org.github.evenjn.yarn.CursableRingMap;
import org.github.evenjn.yarn.CursableRingPurl;
import org.github.evenjn.yarn.CursableRingPurler;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.CursorMap;
import org.github.evenjn.yarn.CursorPurl;
import org.github.evenjn.yarn.CursorPurler;
import org.github.evenjn.yarn.CursorRingMap;
import org.github.evenjn.yarn.CursorRingPurl;
import org.github.evenjn.yarn.CursorRingPurler;
import org.github.evenjn.yarn.EndOfCursorException;
import org.github.evenjn.yarn.IterableMap;
import org.github.evenjn.yarn.IterablePurl;
import org.github.evenjn.yarn.IterablePurler;
import org.github.evenjn.yarn.IterableRingMap;
import org.github.evenjn.yarn.IterableRingPurl;
import org.github.evenjn.yarn.IterableRingPurler;
import org.github.evenjn.yarn.IteratorMap;
import org.github.evenjn.yarn.IteratorPurl;
import org.github.evenjn.yarn.IteratorPurler;
import org.github.evenjn.yarn.IteratorRingMap;
import org.github.evenjn.yarn.IteratorRingPurl;
import org.github.evenjn.yarn.IteratorRingPurler;
import org.github.evenjn.yarn.OptionalMap;
import org.github.evenjn.yarn.OptionalPurl;
import org.github.evenjn.yarn.OptionalPurler;
import org.github.evenjn.yarn.OptionalRingMap;
import org.github.evenjn.yarn.OptionalRingPurl;
import org.github.evenjn.yarn.OptionalRingPurler;
import org.github.evenjn.yarn.RingFunction;
import org.github.evenjn.yarn.StreamRingMap;
import org.github.evenjn.yarn.StreamRingPurl;
import org.github.evenjn.yarn.StreamRingPurler;

/**
 * 
 * <h1>KnittingCursable</h1>
 * 
 * <p>
 * A {@code KnittingCursable} wraps a {@link org.github.evenjn.yarn.Cursable
 * Cursable} and provides utility methods to access its contents.
 * </p>
 * 
 * <p>
 * Briefly, a {@code KnittingCursable} may be used in three ways:
 * </p>
 * 
 * <ul>
 * <li>As a simple cursable, invoking the
 * {@link org.github.evenjn.knit.KnittingCursable#pull(Rook) pull} method;</li>
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
 * Public instance methods of {@code KnittingCursable} fall into one of the
 * following four categories:
 * </p>
 * 
 * <ul>
 * <li>Object methods (inherited from {@link java.lang.Object Object})</li>
 * <li>Cursable methods ({@link #pull(Rook)})</li>
 * <li>Rolling methods (listed below)</li>
 * <li>Transformation methods (listed below)</li>
 * </ul>
 *
 * <p>
 * Rolling methods instantiate a {@link KnittingCursor} by invoking
 * {@link #pull(Rook)} and repeatedly invoke the method
 * {@link KnittingCursor#next()} typically (but not necessarily) until the end
 * is reached. The following methods are rolling:
 * </p>
 * 
 * <ul>
 * <li>{@link #collect(Collection)}</li>
 * <li>{@link #consume(Ring)}</li>
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
 * <li>{@link #asIterator(Rook)}</li>
 * <li>{@link #asStream(Rook)}</li>
 * <li>{@link #crop(Predicate)}</li>
 * <li>{@link #cut(Predicate)}</li>
 * <li>{@link #entwine(Cursable, BiFunction)}</li>
 * <li>{@link #filter(Predicate)}</li>
 * <li>{@link #flatmapArray(ArrayMap)}</li>
 * <li>{@link #flatmapCursable(CursableMap)}</li>
 * <li>{@link #flatmapCursable(CursableRingMap)}</li>
 * <li>{@link #flatmapCursor(CursorMap)}</li>
 * <li>{@link #flatmapCursor(CursorRingMap)}</li>
 * <li>{@link #flatmapIterable(IterableMap)}</li>
 * <li>{@link #flatmapIterable(IterableRingMap)}</li>
 * <li>{@link #flatmapIterator(IteratorMap)}</li>
 * <li>{@link #flatmapIterator(IteratorRingMap)}</li>
 * <li>{@link #flatmapOptional(OptionalMap)}</li>
 * <li>{@link #flatmapOptional(OptionalRingMap)}</li>
 * <li>{@link #flatmapStream(StreamRingMap)}</li>
 * <li>{@link #head(int)}</li>
 * <li>{@link #head(int, int)}</li>
 * <li>{@link #headless(int)}</li>
 * <li>{@link #map(Function)}</li>
 * <li>{@link #map(RingFunction)}</li>
 * <li>{@link #numbered()}</li>
 * <li>{@link #peek(Consumer)}</li>
 * <li>{@link #prepend(Cursable)}</li>
 * <li>{@link #purlArray(ArrayPurler)}</li>
 * <li>{@link #purlCursable(CursablePurler)}</li>
 * <li>{@link #purlCursable(CursableRingPurler)}</li>
 * <li>{@link #purlCursor(CursorPurler)}</li>
 * <li>{@link #purlCursor(CursorRingPurler)}</li>
 * <li>{@link #purlIterable(IterablePurler)}</li>
 * <li>{@link #purlIterable(IterableRingPurler)}</li>
 * <li>{@link #purlIterator(IteratorPurler)}</li>
 * <li>{@link #purlIterator(IteratorRingPurler)}</li>
 * <li>{@link #purlOptional(OptionalPurler)}</li>
 * <li>{@link #purlOptional(OptionalRingPurler)}</li>
 * <li>{@link #purlStream(StreamRingPurler)}</li>
 * </ul>
 * 
 * <h2>Static Methods of KnittingCursable</h2>
 * 
 * <p>
 * Static public methods of {@code KnittingCursable} return
 * {@code KnittingCursable} objects providing access to an argument sequence of
 * objects.
 * </p>
 * 
 * <ul>
 * <li>{@link #empty()}</li>
 * <li>{@link #on(Object...)}</li>
 * <li>{@link #wrap(Iterable)}</li>
 * <li>{@link #wrap(Object[])}</li>
 * </ul>
 *
 * <p>
 * This class is part of package {@link org.github.evenjn.knit Knit}.
 * </p>
 * 
 * @param <I>
 *          The type of elements accessible via this cursable.
 * @since 1.0
 */
public class KnittingCursable<I> implements
		Cursable<I> {

	private final Cursable<I> wrapped;

	private KnittingCursable(Cursable<I> cursable) {
		this.wrapped = cursable;
	}

	/**
	 * <p>
	 * {@code append} returns a view of the concatenation of the argument cursable
	 * after this cursable.
	 * </p>
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
	 * {@code asIterator} returns a view of this cursable as a
	 * {@link java.util.Iterator}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param rook
	 *          A rook.
	 * @return A view of this cursable as a {@link java.util.Iterator}.
	 * @since 1.0
	 */
	public Iterator<I> asIterator( Rook rook ) {
		return pull( rook ).asIterator( );
	}

	/**
	 * <p>
	 * {@code asStream} returns a view of this cursable as a
	 * {@link java.util.stream.Stream}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param rook
	 *          A rook.
	 * @return A view of this cursable as a {@link java.util.stream.Stream}.
	 * @since 1.0
	 */
	public Stream<I> asStream( Rook rook ) {
		return pull( rook ).asStream( );
	}

	/**
	 * <p>
	 * {@code collect} adds all elements of this cursable to the argument
	 * collection, then returns it.
	 * </p>
	 * 
	 * <p>
	 * The objects collected may be dead. This is due to the fact that cursors do
	 * not guarantee that the objects they return survive subsequent invocations
	 * of {@link org.github.evenjn.yarn.Cursor#next() next()}, or closing the
	 * rook.
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
		try ( BasicRook rook = new BasicRook( ) ) {
			return pull( rook ).collect( collection );
		}
	}

	/**
	 * <p>
	 * {@code consume} feeds a consumer with the elements of this cursable.
	 * </p>
	 * 
	 * <p>
	 * Obtains a consumer from the argument {@code consumer_provider}, hooking it
	 * to a local, temporary rook. Then, this method obtains a cursor using this
	 * cursable's {@link #pull(Rook)}, hooking it to the same local rook.
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
	 * @param consumer_provider
	 *          A system that provides a consumer.
	 * @since 1.0
	 */
	public void consume( Ring<? extends Consumer<? super I>> consumer_provider ) {
		try ( BasicRook rook = new BasicRook( ) ) {
			Consumer<? super I> consumer = consumer_provider.get( rook );
			pull( rook ).peek( consumer ).roll( );
		}
	}

	/**
	 * <p>
	 * {@code crop} returns a cursable where each element is a cursor providing
	 * access to a subsequence of contiguous elements in this cursable that
	 * satisfy the argument {@code stateless_predicate}.
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
	@Deprecated
	public KnittingCursable<KnittingCursor<I>>
			crop( Predicate<I> stateless_predicate ) {
		return KnittingCursable.wrap( h -> pull( h ).crop( stateless_predicate ) );
	}

	/**
	 * <p>
	 * {@code cut} returns a cursable where each element is a cursor providing
	 * access to a subsequence of contiguous elements that would recontsruct the
	 * original cursable if concatenated.
	 * </p>
	 * 
	 * @param stateful_predicate
	 *          A stateful system that identifies elements that mark beginning of
	 *          a new element of the partition.
	 * @return a cursable where each element is a cursor providing access to a
	 *         partition of the elements in this cursable.
	 * @since 1.0
	 */
	@Deprecated
	public KnittingCursable<KnittingCursor<I>>
			cut( Predicate<I> stateful_predicate ) {
		return KnittingCursable.wrap( h -> pull( h ).cut( stateful_predicate ) );
	}

	/**
	 * <p>
	 * {@code entwine} returns a cursable that traverses this cursable and the
	 * argument {@code other_cursor} in parallel, applying the argument
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
			public Cursor<M> pull( Rook rook ) {
				return outer.pull( rook ).entwine( other_cursable.pull( rook ),
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
	 * {@code equivalentTo} returns true when this cursable and the {@code other}
	 * cursable have the same number of elements, and when each i-th element of
	 * this cursable is equivalent to the i-th element of the {@code other}
	 * cursable. Returns false otherwise.
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
	 * {@code equivalentTo} returns true when this cursable and the {@code other}
	 * cursable have the same number of elements, and when each i-th element of
	 * this cursable is equivalent to the i-th element of the {@code other}
	 * cursable. Returns false otherwise.
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
	public <Y> boolean equivalentTo( Cursable<Y> other,
			Equivalencer<I, Y> equivalencer ) {
		if ( other == this )
			return true;

		try ( BasicRook rook = new BasicRook( ) ) {
			KnittingCursor<I> pull1 = this.pull( rook );
			KnittingCursor<Y> pull2 = KnittingCursor.wrap( other.pull( rook ) );

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
				Y next2 = pull2.next( );
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
	 * {@code filter} returns a view hiding the elements which do not satisfy the
	 * argument {@code stateless_predicate} in this cursable.
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
			public Cursor<I> pull( Rook rook ) {
				return new FilterCursor<>( wrapped.pull( rook ), stateless_predicate );
			}
		} );
	}

	/**
	 * <p>
	 * {@code flatmapArray} returns a view realizing the same transformation as
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
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) )
						.flatmapArray( stateless_array_map );
			}
		} );
	}

	/**
	 * <p>
	 * {@code flatmapCursable} returns a view realizing the same transformation as
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
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) )
						.flatmapCursable( rook, stateless_cursable_map );
			}
		} );
	}

	/**
	 * <p>
	 * {@code flatmapCursable} returns a view realizing the same transformation as
	 * {@link #flatmapCursor(CursorRingMap)} except that the view shows elements
	 * in the cursables returned by the argument {@code stateless_cursable_map_h}.
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
			CursableRingMap<? super I, O> stateless_cursable_map_h ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) )
						.flatmapCursable( rook, stateless_cursable_map_h );
			}
		} );
	}

	/**
	 * <p>
	 * {@code flatmapCursor} returns a {@code KnittingCursable} such that each
	 * invocation of {@link #pull(Rook)} on it pulls a new {@code KnittingCursor}
	 * from this cursable, transforms it using
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
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) )
						.flatmapCursor( stateless_cursor_map );
			}
		} );
	}

	/**
	 * <p>
	 * {@code flatmapCursor} returns a {@code KnittingCursable} such that each
	 * invocation of {@link #pull(Rook)} on it pulls a new {@code KnittingCursor}
	 * from this cursable, transforms it using
	 * {@link KnittingCursor#flatmapCursor(Rook, CursorRingMap)} with the rook
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
			CursorRingMap<? super I, O> stateless_cursor_map_h ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) )
						.flatmapCursor( rook, stateless_cursor_map_h );
			}
		} );
	}

	/**
	 * <p>
	 * {@code flatmapIterable} returns a view realizing the same transformation as
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
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) )
						.flatmapIterable( stateless_iterable_map );
			}
		} );
	}

	/**
	 * <p>
	 * {@code flatmapIterable} returns a view realizing the same transformation as
	 * {@link #flatmapCursor(CursorRingMap)} except that the view shows elements
	 * in the iterables returned by the argument {@code stateless_iterable_map_h}.
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
			IterableRingMap<? super I, O> stateless_iterable_map_h ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) )
						.flatmapIterable( rook, stateless_iterable_map_h );
			}
		} );
	}

	/**
	 * <p>
	 * {@code flatmapIterator} returns a view realizing the same transformation as
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
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) )
						.flatmapIterator( stateless_iterator_map );
			}
		} );
	}

	/**
	 * <p>
	 * {@code flatmapIterator} returns a view realizing the same transformation as
	 * {@link #flatmapCursor(CursorRingMap)} except that the view shows elements
	 * in the iterators returned by the argument {@code stateless_iterator_map_h}.
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
			IteratorRingMap<? super I, O> stateless_iterator_map_h ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) )
						.flatmapIterator( rook, stateless_iterator_map_h );
			}
		} );
	}

	/**
	 * <p>
	 * {@code flatmapOptional} returns a view realizing the same transformation as
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
			OptionalMap<? super I, O> stateless_optional_map ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) )
						.flatmapOptional( stateless_optional_map );
			}
		} );
	}

	/**
	 * <p>
	 * {@code flatmapOptional} returns a view realizing the same transformation as
	 * {@link #flatmapCursor(CursorRingMap)} except that the view shows elements
	 * in the optionals returned by the argument {@code stateless_optional_map_h}.
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
			OptionalRingMap<? super I, O> stateless_optional_map_h )
			throws IllegalStateException {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) )
						.flatmapOptional( rook, stateless_optional_map_h );
			}
		} );
	}

	/**
	 * <p>
	 * {@code flatmapStream} returns a view realizing the same transformation as
	 * {@link #flatmapCursor(CursorRingMap)} except that the view shows elements
	 * in the streams returned by the argument {@code stateless_stream_map_h}.
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
			StreamRingMap<? super I, O> stateless_stream_map_h ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) )
						.flatmapStream( rook, stateless_stream_map_h );
			}
		} );
	}

	/**
	 * <p>
	 * {@code head} returns a view showing the first {@code show} elements in this
	 * cursable.
	 * </p>
	 * 
	 * <p>
	 * This is a convenient shorthand to invoke {@link #head(int, int)} passing
	 * zero as the first argument.
	 * </p>
	 * 
	 * @param show
	 *          The number of elements to show. A negative numbers counts as zero.
	 * @return A view showing the first {@code show} elements in this cursable.
	 * @since 1.0
	 */
	public KnittingCursable<I> head( int show ) {
		return head( 0, show );
	}

	/**
	 * <p>
	 * {@code head} returns a view showing the first {@code show} elements of this
	 * cursable visible after hiding the first {@code hide} elements.
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
			public Cursor<I> pull( Rook rook ) {
				return Subcursor.sub( wrapped.pull( rook ), final_hide, final_show );
			}
		} );
	}

	/**
	 * <p>
	 * {@code headless} returns a view hiding the first {@code hide} elements of
	 * this cursable.
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
			public Cursor<I> pull( Rook rook ) {
				return Subcursor.skip( wrapped.pull( rook ), final_hide );
			}
		} );
	}

	/**
	 * <p>
	 * {@code map} returns a {@code KnittingCursable} such that each invocation of
	 * {@link #pull(Rook)} on it pulls a new {@code KnittingCursor} from this
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
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) )
						.map( stateless_function );
			}
		} );
	}

	/**
	 * <p>
	 * {@code map} returns a {@code KnittingCursable} such that each invocation of
	 * {@link #pull(Rook)} on it pulls a new {@code KnittingCursor} from this
	 * cursable, transforms it using
	 * {@link KnittingCursor#map(Rook, RingFunction)} with the rook already
	 * available and the argument {@code stateless_function_h}, then returns the
	 * resulting cursor.
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
			map( RingFunction<? super I, O> stateless_function_h ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) )
						.map( rook, stateless_function_h );
			}
		} );
	}

	/**
	 * <p>
	 * {@code numbered} returns a view that, for each element of this cursable,
	 * provides a {@link org.github.evenjn.knit.Numbered Numbered} wrapper,
	 * containing the element itself and the number of elements preceding it.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @return A view that, for each element of this cursable, provides a
	 *         {@link org.github.evenjn.knit.Numbered Numbered} wrapper,
	 *         containing the element itself and the number of elements preceding
	 *         it.
	 * @since 1.0
	 */
	public KnittingCursable<Numbered<I>> numbered( ) {
		return wrap( new Cursable<Numbered<I>>( ) {

			@Override
			public Cursor<Numbered<I>> pull( Rook rook ) {
				return new NumberedCursor<>( wrapped.pull( rook ) );
			}
		} );
	}

	/**
	 * <p>
	 * When this cursable provides access to one element only, {@code one} returns
	 * it. Otherwise, it throws an {@code IllegalStateException}.
	 * </p>
	 * 
	 * <p>
	 * The object returned may be dead. This is due to the fact that cursables do
	 * not guarantee that the objects they return survive subsequent invocations
	 * of {@link org.github.evenjn.yarn.Cursor#next() next()} on the cursors they
	 * provide, or closing the rook.
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
		try ( BasicRook rook = new BasicRook( ) ) {
			return pull( rook ).one( );
		}
	}

	/**
	 * <p>
	 * When this cursable provides access to one non-null element only,
	 * {@code optionalOne} returns it wrapped in an optional.
	 * </p>
	 * 
	 * <p>
	 * The object returned may be dead. This is due to the fact that cursables do
	 * not guarantee that the objects they return survive subsequent invocations
	 * of {@link org.github.evenjn.yarn.Cursor#next() next()} on the cursors they
	 * provide, or closing the rook.
	 * </p>
	 * 
	 * <p>
	 * When it not the case that there is exactly one element, or when the only
	 * element accessible is {@code null}, {@code optionalOne} returns an empty
	 * optional.
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
		try ( BasicRook rook = new BasicRook( ) ) {
			return pull( rook ).optionalOne( );
		}
	}

	/**
	 * <p>
	 * {@code peek} returns a view providing access to the elements of this
	 * cursable. The view passes the elements to the argument consumer.
	 * </p>
	 * 
	 * <p>
	 * For each {@code KnittingCursor} obtained invoking {@link #pull(Rook)} on
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
			public Cursor<I> pull( Rook rook ) {
				return new PeekCursor<I>( wrapped.pull( rook ), consumer );
			}

		} );
	}

	/**
	 * <p>
	 * {@code prepend} returns a view of the concatenation of the argument
	 * cursable before this cursable.
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
			public Cursor<I> pull( Rook rook ) {
				return outer_cursable.pull( rook ).prepend( head.pull( rook ) );
			}
		};
		return wrap( result );
	}

	/**
	 * <p>
	 * {@code pull} returns a {@code KnittingCursor} wrapping a cursor obtained
	 * from the wrapped cursable.
	 * </p>
	 * 
	 * @param rook
	 *          A rook.
	 * @return A {@code KnittingCursor} wrapping a cursor obtained from the
	 *         wrapped cursable.
	 * @since 1.0
	 */
	@Override
	public KnittingCursor<I> pull( Rook rook ) {
		return KnittingCursor.wrap( wrapped.pull( rook ) );
	}

	/**
	 * <p>
	 * {@code purlArray} returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurler)} except that the view shows elements in
	 * arrays returned by {@link ArrayPurl} objects supplied by the argument
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
			purlArray( ArrayPurler<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				Cursor<I> pull = wrapped.pull( rook );
				return KnittingCursor.wrap( pull ).purlArray( factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * {@code purlCursable} returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurler)} except that the view shows elements in
	 * cursables returned by {@link CursablePurl} objects supplied by the argument
	 * {@code factory}.
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
			purlCursable( CursablePurler<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				Cursor<I> pull = wrapped.pull( rook );
				return KnittingCursor.wrap( pull ).purlCursable( factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * {@code purlCursable} returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorRingPurler)} except that the view shows elements
	 * in arrays returned by {@link CursableRingPurl} objects supplied by the
	 * argument {@code factory}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in cursables returned by
	 *          {@link CursableRingPurl} objects supplied by the argument
	 *          {@code factory}.
	 * @param factory
	 *          A supplier of {@link CursableRingPurl} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			purlCursable( CursableRingPurler<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				Cursor<I> pull = wrapped.pull( rook );
				return KnittingCursor.wrap( pull ).purlCursable( rook, factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * {@code purlCursor} returns a complex view.
	 * </p>
	 * 
	 * <p>
	 * Pulling a cursor from the returned cursable returns a purl of a cursor
	 * pulled from this cursable. For an introduction on purling see
	 * {@link org.github.evenjn.yarn.CursorPurl CursorPurl}.
	 * </p>
	 * 
	 * <p>
	 * In detail, when the client invokes {@link Cursable#pull(Rook) pull} on the
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
			purlCursor( CursorPurler<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				Cursor<I> pull = wrapped.pull( rook );
				return KnittingCursor.wrap( pull )
						.purlCursor( factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * {@code purlCursor} returns a complex view.
	 * </p>
	 * 
	 * <p>
	 * Pulling a cursor from the returned cursable returns a purl of a cursor
	 * pulled from this cursable. For an introduction on purling see
	 * {@link org.github.evenjn.yarn.CursorPurl CursorPurl}.
	 * </p>
	 * 
	 * <p>
	 * In detail, when the client invokes {@link Cursable#pull(Rook) pull} on the
	 * returned cursable, that cursable pulls a new {@link KnittingCursor} from
	 * this cursable, then it obtains a new {@link CursorRingPurl} from the
	 * argument {@code factory}, and finally returns the cursor obtained by
	 * invoking {@link KnittingCursor#purlCursor(Rook, CursorRingPurl)
	 * purlCursable} using the argument purl.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * 
	 * @param <O>
	 *          The type of elements in cursors produced by {@link CursorRingPurl}
	 *          objects supplied by the argument {@code factory}.
	 * 
	 * @param factory
	 *          A supplier of {@link CursorRingPurl} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			purlCursor( CursorRingPurler<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				Cursor<I> pull = wrapped.pull( rook );
				return KnittingCursor.wrap( pull )
						.purlCursor( rook, factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * {@code purlIterable} returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurler)} except that the view shows elements in
	 * iterables returned by {@link IterablePurl} objects supplied by the argument
	 * {@code factory}.
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
			purlIterable( IterablePurler<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				Cursor<I> pull = wrapped.pull( rook );
				return KnittingCursor.wrap( pull ).purlIterable( factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * {@code purlIterable} returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorRingPurler)} except that the view shows elements
	 * in iterables returned by {@link IterableRingPurl} objects supplied by the
	 * argument {@code factory}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in iterables returned by
	 *          {@link IterableRingPurl} objects supplied by the argument
	 *          {@code factory}.
	 * @param factory
	 *          A supplier of {@link IterableRingPurl} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			purlIterable( IterableRingPurler<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				Cursor<I> pull = wrapped.pull( rook );
				return KnittingCursor.wrap( pull ).purlIterable( rook, factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * {@code purlIterator} returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurler)} except that the view shows elements in
	 * iterators returned by {@link IteratorPurl} objects supplied by the argument
	 * {@code factory}.
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
			purlIterator( IteratorPurler<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				Cursor<I> pull = wrapped.pull( rook );
				return KnittingCursor.wrap( pull ).purlIterator( factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * {@code purlIterator} returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorRingPurler)} except that the view shows elements
	 * in iterators returned by {@link IteratorRingPurl} objects supplied by the
	 * argument {@code factory}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in iterators returned by
	 *          {@link IteratorRingPurl} objects supplied by the argument
	 *          {@code factory}.
	 * @param factory
	 *          A supplier of {@link IteratorRingPurl} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			purlIterator( IteratorRingPurler<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				Cursor<I> pull = wrapped.pull( rook );
				return KnittingCursor.wrap( pull ).purlIterator( rook, factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * {@code purlOptional} returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorPurler)} except that the view shows elements in
	 * optionals returned by {@link OptionalPurl} objects supplied by the argument
	 * {@code factory}.
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
			OptionalPurler<? super I, O> factory )
			throws IllegalStateException {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) ).purlOptional(
						factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * {@code purlOptional} returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorRingPurler)} except that the view shows elements
	 * in optionals returned by {@link OptionalRingPurl} objects supplied by the
	 * argument {@code factory}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in optionals returned by
	 *          {@link OptionalRingPurl} objects supplied by the argument
	 *          {@code factory}.
	 * @param factory
	 *          A supplier of {@link OptionalRingPurl} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O> purlOptional(
			OptionalRingPurler<? super I, O> factory )
			throws IllegalStateException {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) ).purlOptional( rook,
						factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * {@code purlStream} returns a view realizing the same transformation as
	 * {@link #purlCursor(CursorRingPurler)} except that the view shows elements
	 * in streams returned by {@link StreamRingPurl} objects supplied by the
	 * argument {@code factory}.
	 * </p>
	 * 
	 * <p>
	 * This is a transformation method.
	 * </p>
	 * 
	 * @param <O>
	 *          The type of elements in streams returned by {@link StreamRingPurl}
	 *          objects supplied by the argument {@code factory}.
	 * @param factory
	 *          A supplier of {@link StreamRingPurl} objects.
	 * @return A complex view.
	 * @since 1.0
	 */
	public <O> KnittingCursable<O>
			purlStream( StreamRingPurler<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Rook rook ) {
				return KnittingCursor.wrap( wrapped.pull( rook ) ).purlStream(
						rook, factory.get( ) );
			}
		} );
	}

	/**
	 * <p>
	 * {@code reduce} returns the result of a computation taking into account all
	 * the elements of this cursable.
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
	public <K> K reduce( K zero, BiFunction<K, I, K> bifunction ) {
		K reduction = zero;
		try ( BasicRook rook = new BasicRook( ) ) {
			Cursor<I> kc = wrapped.pull( rook );
			try {
				for ( ;; ) {
					reduction = bifunction.apply( reduction, kc.next( ) );
				}
			}
			catch ( EndOfCursorException e ) {
			}
			return reduction;
		}
	}

	/**
	 * <p>
	 * {@code roll} pulls a {@code KnittingCursor} from this cursable and invokes
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
		try ( BasicRook rook = new BasicRook( ) ) {
			pull( rook ).roll( );
		}
	}

	/**
	 * <p>
	 * {@code count} returns the number of elements in this cursable.
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
		try ( BasicRook rook = new BasicRook( ) ) {
			return pull( rook ).count( );
		}
	}

	/**
	 * <p>
	 * {@code tail} returns a view of the last {@code show} elements visible after
	 * hiding the last {@code hide} elements in this cursable.
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
			public Cursor<I> pull( Rook rook ) {
				return Subcursor.sub( wrapped.pull( rook ), final_skip, final_len );
			}
		} );
	}

	/**
	 * <p>
	 * {@code tailless} returns a view hiding the last {@code hide} elements in
	 * this cursable.
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
			public Cursor<I> pull( Rook rook ) {
				return Subcursor.sub( wrapped.pull( rook ), 0, final_len );
			}
		} );
	}

	/**
	 * <p>
	 * {@code empty} returns an empty cursable.
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
		return (KnittingCursable<K>) empty;
	}

	private static final KnittingCursable<Void> empty =
			wrap( new Cursable<Void>( ) {

				@Override
				public Cursor<Void> pull( Rook rook ) {
					return KnittingCursor.empty( );
				}
			} );

	/**
	 * <p>
	 * {@code on} returns a new {@code KnittingCursable} providing access to the
	 * argument elements.
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
			public Cursor<K> pull( Rook rook ) {
				return new ArrayCursor<K>( elements );
			}
		};
		return wrap( cursable );
	}

	/**
	 * <p>
	 * {@code wrap} returns a view of the elements in the argument
	 * {@link org.github.evenjn.yarn.Cursable Cursable}.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of elements in the argument
	 *          {@link org.github.evenjn.yarn.Cursable Cursable}.
	 * @param cursable
	 *          A {@link org.github.evenjn.yarn.Cursable Cursable} of elements.
	 * @return A view of the elements in the argument
	 *         {@link org.github.evenjn.yarn.Cursable Cursable}.
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
	 * {@code wrap} returns a view of the elements in the argument
	 * {@link java.lang.Iterable Iterable}.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of elements in the argument {@link java.lang.Iterable
	 *          Iterable}.
	 * @param iterable
	 *          An {@link java.lang.Iterable Iterable} of elements.
	 * @return A view of the elements in the argument {@link java.lang.Iterable
	 *         Iterable}.
	 * @since 1.0
	 */
	public static <K> KnittingCursable<K> wrap( Iterable<K> iterable ) {
		return wrap( new IterableCursable<K>( iterable ) );
	}

	/**
	 * <p>
	 * {@code wrap} returns a view of the elements in the argument array.
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
