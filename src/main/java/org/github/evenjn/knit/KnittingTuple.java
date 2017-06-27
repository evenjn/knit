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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;
import org.github.evenjn.yarn.Equivalencer;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.Tuple;

/**
 * 
 * <h1>KnittingTuple</h1>
 * 
 * <p>
 * A {@code KnittingTuple} wraps a tuple and provides utility methods to access
 * its contents.
 * </p>
 * 
 * <p>
 * Briefly, a {@code KnittingTuple} may be used in three ways:
 * </p>
 * 
 * <ul>
 * <li>As a simple Tuple, invoking the
 * {@link org.github.evenjn.knit.KnittingTuple#get(int) get(int)} method;</li>
 * <li>As a resource to be harvested, invoking a rolling method such as
 * {@link #collect(Collection)};</li>
 * <li>As a value to be compared, invoking a comparison method such as
 * {@link #diff(Tuple)};</li>
 * <li>As a resource to transform, invoking a transformation method such as
 * {@link #map(Function)};</li>
 * </ul>
 * 
 * <p>
 * Unlike {@code KnittingCursor}, these three modes of operation are not
 * exclusive: a {@code KnittingTuple} may be used several times, in any mode.
 * </p>
 * 
 * <h2>Methods of a KnittingTuple</h2>
 * 
 * <p>
 * Non-static public methods of {@code KnittingTuple} fall into one of the
 * following four categories:
 * </p>
 * 
 * <ul>
 * <li>Object methods (inherited from {@link java.lang.Object Object})</li>
 * <li>Tuple methods ({@link #get(int)}, {@link #size()})</li>
 * <li>Transformation methods (listed below)</li>
 * <li>Comparison methods (listed below)</li>
 * <li>Other methods (listed below)</li>
 * </ul>
 * 
 * 
 * <p>
 * Transformations are methods that return a new {@code KnittingTuple} object
 * (or something similar), which provides a new view of the contents of the
 * wrapped tuple. Transformation methods do not access slots at the time of
 * their invocation; they return lazy wrappers. The following methods are
 * transformations:
 * </p>
 * 
 * <ul>
 * <li>{@link #append(Tuple)}</li>
 * <li>{@link #asIterable()}</li>
 * <li>{@link #asIterator()}</li>
 * <li>{@link #asKnittingCursable()}</li>
 * <li>{@link #asKnittingCursor()}</li>
 * <li>{@link #asStream()}</li>
 * <li>{@link #head(int, int)}</li>
 * <li>{@link #headless(int)}</li>
 * <li>{@link #map(Function)}</li>
 * <li>{@link #prepend(Tuple)}</li>
 * <li>{@link #reverse()}</li>
 * <li>{@link #tail(int, int)}</li>
 * <li>{@link #tailless(int)}</li>
 * </ul>
 *
 * <p>
 * Tuple comparison methods compares this tuple with one or more tuples. The
 * following methods are comparisons:
 * </p>
 * 
 * <ul>
 * <li>{@link #diff(Tuple)}</li>
 * <li>{@link #diff(Tuple, Equivalencer)}</li>
 * <li>{@link #contains(Tuple)}</li>
 * <li>{@link #contains(Tuple, Equivalencer)}</li>
 * <li>{@link #distance(Tuple)}</li>
 * <li>{@link #distance(Tuple, Equivalencer)}</li>
 * <li>{@link #endsWith(Tuple)}</li>
 * <li>{@link #endsWith(Tuple, Equivalencer)}</li>
 * <li>{@link #equivalentTo(Tuple)}</li>
 * <li>{@link #equivalentTo(Tuple, Equivalencer)}</li>
 * <li>{@link #findSubtuple(Tuple)}</li>
 * <li>{@link #findSubtuple(Tuple, Equivalencer)}</li>
 * <li>{@link #lcs(Tuple)}</li>
 * <li>{@link #lcs(Tuple, Equivalencer)}</li>
 * <li>{@link #lcsUnion(Cursor)}</li>
 * <li>{@link #lcsUnion(Cursor, Equivalencer)}</li>
 * <li>{@link #lcsIntersection(Cursor)}</li>
 * <li>{@link #lcsIntersection(Cursor, Equivalencer)}</li>
 * <li>{@link #startsWith(Tuple)}</li>
 * <li>{@link #startsWith(Tuple, Equivalencer)}</li>
 * </ul>
 *
 * <p>
 * Other methods are:
 * </p>
 * 
 * <ul>
 * <li>{@link #collect(Collection)}</li>
 * <li>{@link #consume(Function)}</li>
 * <li>{@link #one()}</li>
 * <li>{@link #optionalOne()}</li>
 * <li>{@link #reduce(Object, BiFunction)}</li>
 * </ul>
 *
 * @param <I>
 *          The type of elements accessible via this tuple.
 * @since 1.0
 */
public class KnittingTuple<I> implements
		Tuple<I> {

	private final Tuple<I> wrapped;

	@SafeVarargs
	public static <K> KnittingTuple<K> on( K ... elements ) {
		return wrap( new ArrayTuple<K>( elements ) );
	}

	public static <K> KnittingTuple<K> wrap( Tuple<K> tuple ) {
		if ( tuple instanceof KnittingTuple ) {
			return (KnittingTuple<K>) tuple;
		}
		return new KnittingTuple<K>( tuple );
	}

	public static <K> KnittingTuple<K> wrap( ArrayList<K> arraylist ) {
		return wrap( new ArrayListTuple<K>( arraylist ) );
	}

	public static <K> KnittingTuple<K> wrap( Vector<K> vector ) {
		return wrap( new VectorTuple<K>( vector ) );
	}

	public static <K> KnittingTuple<K> wrap( K[] array ) {
		return wrap( new ArrayTuple<K>( array ) );
	}

	private KnittingTuple(Tuple<I> tuple) {
		this.wrapped = tuple;
	}

	/**
	 * Returns a view of the concatenation of the argument tuple after this tuple.
	 * 
	 * @param tail
	 *          A tuple to concatenate after this tuple.
	 * @return A view of the concatenation of the argument tuple after this tuple.
	 * @since 1.0
	 */
	public KnittingTuple<I> append( Tuple<I> tail ) {
		KnittingTuple<I> outer_this = this;
		Tuple<I> result = new Tuple<I>( ) {

			@Override
			public I get( int index ) {
				return index < outer_this.size( ) ? outer_this.get( index ) : tail
						.get( index - outer_this.size( ) );
			}

			@Override
			public int size( ) {
				return outer_this.size( ) + tail.size( );
			}
		};
		return wrap( result );
	}

	/**
	 * <p>
	 * Returns a view of this tuple as an {@link java.util.Iterable Iterable}.
	 * </p>
	 * 
	 * @return A view of this tuple as an {@link java.util.Iterable Iterable}.
	 * @since 1.0
	 */
	public Iterable<I> asIterable( ) {
		return ( ) -> asIterator( );
	}

	/**
	 * <p>
	 * Returns a view of this tuple as an {@link java.util.Iterator Iterator}.
	 * </p>
	 * 
	 * @return A view of this tuple as an {@link java.util.Iterator Iterator}.
	 * @since 1.0
	 */
	public Iterator<I> asIterator( ) {
		return new Iterator<I>( ) {

			final int size = wrapped.size( );

			int i = 0;

			@Override
			public boolean hasNext( ) {
				return i < size;
			}

			@Override
			public I next( ) {
				return wrapped.get( i++ );
			}
		};
	}

	/**
	 * <p>
	 * Returns a view of this tuple as a
	 * {@link org.github.evenjn.knit.KnittingCursable KnittingCursable}.
	 * </p>
	 * 
	 * @return A view of this tuple as a
	 *         {@link org.github.evenjn.knit.KnittingCursable KnittingCursable}.
	 * @since 1.0
	 */
	public KnittingCursable<I> asKnittingCursable( ) {
		return KnittingCursable.wrap( h -> pull( ) );
	}

	/**
	 * <p>
	 * Returns a view of this tuple as a
	 * {@link org.github.evenjn.knit.KnittingCursor KnittingCursor}.
	 * </p>
	 * 
	 * @return A view of this tuple as a
	 *         {@link org.github.evenjn.knit.KnittingCursor KnittingCursor}.
	 * @since 1.0
	 */
	public KnittingCursor<I> asKnittingCursor( ) {
		return KnittingCursor.wrap( pull( ) );
	}

	/**
	 * <p>
	 * Returns a view of this tuple as a {@link java.util.stream.Stream Stream}.
	 * </p>
	 * 
	 * @return A view of this tuple as a {@link java.util.stream.Stream Stream}.
	 * @since 1.0
	 */
	public Stream<I> asStream( ) {
		return asKnittingCursor( ).asStream( );
	}

	/**
	 * <p>
	 * Adds all elements of this tuple to the argument collection, then returns
	 * it.
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
			return this.asKnittingCursor( ).collect( collection );
		}
	}

	/**
	 * <p>
	 * Feeds a consumer with the elements of this tuple.
	 * </p>
	 * 
	 * <p>
	 * Obtains a consumer from the argument {@code consumer_provider}, hooking it
	 * to a local, temporary hook. Then, this method iterates over all the slots,
	 * passing the object in each slot to the consumer, until the end of the
	 * slots.
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
			this.asKnittingCursor( ).peek( consumer ).roll( );
		}
	}

	/**
	 * <p>
	 * Returns {@code true} when the argument tuple is a subtuple of this tuple;
	 * {@code false} otherwise.
	 * </p>
	 * 
	 * <p>
	 * This method invokes {@link KnittingTuple#contains(Tuple,Equivalencer)
	 * contains(Tuple, Equivalencer)} using an equivalencer that marks two objects
	 * as equivalent if and only if they are {@linkplain java.lang.Object#equals
	 * equal} to each other or both {@code null}.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @return {@code true} when the argument tuple is a subtuple of this tuple;
	 *         {@code false} otherwise.
	 * @since 1.0
	 */
	public boolean contains( KnittingTuple<I> other ) {
		return contains( other, KnittingTuple::equal_null );
	}

	/**
	 * <p>
	 * Returns {@code true} when the argument tuple is a subtuple of this tuple;
	 * {@code false} otherwise.
	 * </p>
	 * 
	 * <p>
	 * This method uses the argument {@code equivalencer} to decide whether an
	 * element of this tuple is equal to an element of the argument tuple.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @param equivalencer
	 *          A system that can tell whether two objects are equivalent.
	 * @return {@code true} when the argument tuple is a subtuple of this tuple;
	 *         {@code false} otherwise.
	 * @since 1.0
	 */
	public boolean contains( KnittingTuple<I> other,
			Equivalencer<I, Object> equivalencer ) {
		if ( size( ) < other.size( ) )
			return false;
		return findSubtuple( other, 0, equivalencer ).isPresent( );
	}

	/**
	 * <p>
	 * Returns an alignment of this tuple with the argument tuple.
	 * </p>
	 * 
	 * <p>
	 * This method invokes {@link KnittingTuple#diff(Tuple,Equivalencer)
	 * diff(Tuple, Equivalencer)} using an equivalencer that marks two objects as
	 * equivalent if and only if they are {@linkplain java.lang.Object#equals
	 * equal} to each other or both {@code null}.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @return An alignment of this tuple with the argument tuple.
	 * @since 1.0
	 */
	public Iterable<Bi<I, I>> diff( Tuple<I> other ) {
		return ( ) -> KnittingCursor.wrap(
				new DiffIterator<I>( this, other, DiffPatch::equal_or_both_null ) )
				.asIterator( );
	}

	/**
	 * <p>
	 * Returns an alignment of this tuple with the argument tuple, represented as
	 * a list of pairs.
	 * </p>
	 * 
	 * <p>
	 * Each pair has two slots: one at the front, one at the back. At least one of
	 * the two slots is filled in with an element of the tuples.
	 * </p>
	 * 
	 * <p>
	 * The list is such that this tuple is equivalent to the tuple obtained by
	 * iterating over the list of pairs and collecting the elements in the front
	 * slot of each pair whenever that slot is filled in.
	 * </p>
	 * 
	 * <p>
	 * The list is such that the argument tuple is equivalent to the tuple
	 * obtained by iterating over the list of pairs and collecting the elements in
	 * the back slot of each pair whenever that slot is filled in.
	 * </p>
	 * 
	 * <p>
	 * The list is such that the tuple obtained by iterating over the list of
	 * pairs and collecting one of the elements of each pair whenever both slots
	 * are filled in is one of the
	 * <a href="https://en.wikipedia.org/wiki/Longest_common_subsequence_problem"
	 * >longest common subtuples</a> between this tuple and the argument tuple.
	 * </p>
	 * 
	 * <p>
	 * This method's implementation is adapted from
	 * <a href="https://neil.fraser.name/" >Neil Fraser's work</a>.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @param equivalencer
	 *          A system that can tell whether two objects are equivalent.
	 * @return An alignment of this tuple with the argument tuple.
	 * @since 1.0
	 */
	public Iterable<Bi<I, I>> diff(
			Tuple<I> other,
			Equivalencer<I, Object> equivalencer ) {
		return ( ) -> KnittingCursor
				.wrap( new DiffIterator<I>( this, other, equivalencer ) )
				.asIterator( );
	}

	/**
	 * <p>
	 * Returns the <a href=
	 * "https://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance"
	 * >Damerau–Levenshtein distance</a> between this tuple and the argument
	 * tuple.
	 * </p>
	 * 
	 * <p>
	 * This method invokes {@link KnittingTuple#distance(Tuple,Equivalencer)
	 * distance(Tuple, Equivalencer)} using an equivalencer that marks two objects
	 * as equivalent if and only if they are {@linkplain java.lang.Object#equals
	 * equal} to each other or both {@code null}.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @return The distance between this tuple and the argument tuple.
	 * @since 1.0
	 */
	public int distance( Tuple<I> other ) {
		return distance( other, DiffPatch::equal_or_both_null );
	}

	/**
	 * <p>
	 * Returns the <a href=
	 * "https://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance"
	 * >Damerau–Levenshtein distance</a> between this tuple and the argument
	 * tuple.
	 * </p>
	 * 
	 * <p>
	 * This method uses the argument {@code equivalencer} to decide whether an
	 * element of this tuple is equal to an element of the argument tuple.
	 * </p>
	 * 
	 * <p>
	 * Adapted from Apache Commons Lang StringUtils.
	 * </p>
	 * 
	 * <p>
	 * Apache Commons Lang Copyright 2001-2017 The Apache Software Foundation
	 * </p>
	 * 
	 * <p>
	 * This product includes software developed at The Apache Software Foundation
	 * (http://www.apache.org/).
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @param equivalencer
	 *          A system that can tell whether two objects are equivalent.
	 * @return The distance between this tuple and the argument tuple.
	 * @since 1.0
	 */
	public int distance( Tuple<I> other,
			Equivalencer<I, Object> equivalencer ) {
		Tuple<I> s = this;
		Tuple<I> t = other;

		int n = s.size( );
		int m = t.size( );

		if ( n == 0 ) {
			return m;
		}
		else
			if ( m == 0 ) {
				return n;
			}

		if ( n > m ) {
			// swap the input strings to consume less memory
			final Tuple<I> tmp = s;
			s = t;
			t = tmp;
			n = m;
			m = t.size( );
		}

		final int p[] = new int[n + 1];
		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t
		int upper_left;
		int upper;

		I t_j; // jth character of t
		int cost;

		for ( i = 0; i <= n; i++ ) {
			p[i] = i;
		}

		for ( j = 1; j <= m; j++ ) {
			upper_left = p[0];
			t_j = t.get( j - 1 );
			p[0] = j;

			for ( i = 1; i <= n; i++ ) {
				upper = p[i];
				cost = equivalencer.equivalent( s.get( i - 1 ), t_j ) ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left and up
				// +cost
				p[i] =
						Math.min( Math.min( p[i - 1] + 1, p[i] + 1 ), upper_left + cost );
				upper_left = upper;
			}
		}

		return p[n];

	}

	/**
	 * <p>
	 * Returns {@code true} when the argument tuple is a suffix of this tuple;
	 * {@code false} otherwise.
	 * </p>
	 * 
	 * <p>
	 * This method invokes {@link KnittingTuple#endsWith(Tuple,Equivalencer)
	 * endsWith(Tuple, Equivalencer)} using an equivalencer that marks two objects
	 * as equivalent if and only if they are {@linkplain java.lang.Object#equals
	 * equal} to each other or both {@code null}.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @return {@code true} when the argument tuple is a suffix of this tuple;
	 *         {@code false} otherwise.
	 * @since 1.0
	 */
	public boolean endsWith( KnittingTuple<I> other ) {
		return endsWith( other, KnittingTuple::equal_null );
	}

	/**
	 * <p>
	 * Returns {@code true} when the argument tuple is a suffix of this tuple;
	 * {@code false} otherwise.
	 * </p>
	 * 
	 * <p>
	 * This method uses the argument {@code equivalencer} to decide whether an
	 * element of this tuple is equal to an element of the argument tuple.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @param equivalencer
	 *          A system that can tell whether two objects are equivalent.
	 * @return {@code true} when the argument tuple is a suffix of this tuple;
	 *         {@code false} otherwise.
	 * @since 1.0
	 */
	public boolean endsWith( KnittingTuple<I> other,
			Equivalencer<I, Object> equivalencer ) {
		if ( size( ) < other.size( ) )
			return false;
		return headless( size( ) - other.size( ) ).equivalentTo( other,
				equivalencer );
	}

	/**
	 * <p>
	 * Returns {@code true} when this tuple and the argument tuple have the same
	 * number of slots, and when the content of each i-th slot of this tuple is
	 * equivalent to the content of the i-th slot of the argument tuple. Returns
	 * {@code false} otherwise.
	 * </p>
	 * 
	 * <p>
	 * This method invokes {@link KnittingTuple#equivalentTo(Tuple,Equivalencer)
	 * equivalentTo(Tuple, Equivalencer)} using an equivalencer that marks two
	 * objects as equivalent if and only if they are
	 * {@linkplain java.lang.Object#equals equal} to each other or both
	 * {@code null}.
	 * </p>
	 * 
	 * 
	 * @param other
	 *          Another tuple.
	 * @return {@code true} when this tuple and the argument tuple have the same
	 *         number of elements, and when the content of each i-th slot of this
	 *         tuple is equivalent to the content of the i-th slot of the argument
	 *         tuple; {@code false} otherwise.
	 * @since 1.0
	 */
	public boolean equivalentTo( Tuple<?> other ) {
		return equivalentTo( other, KnittingTuple::equal_null );
	}

	/**
	 * <p>
	 * Returns {@code true} when this tuple and the argument tuple have the same
	 * number of slots, and when the content of each i-th slot of this tuple is
	 * equivalent to the content of the i-th slot of the argument tuple. Returns
	 * {@code false} otherwise.
	 * </p>
	 * 
	 * <p>
	 * This method uses the argument {@code equivalencer} to decide whether an
	 * element of this tuple is equal to an element of the argument tuple.
	 * </p>
	 * 
	 * 
	 * @param other
	 *          Another tuple.
	 * @param equivalencer
	 *          A system that can tell whether two objects are equivalent.
	 * @return {@code true} when this tuple and the argument tuple have the same
	 *         number of elements, and when the content of each i-th slot of this
	 *         tuple is equivalent to the content of the i-th slot of the argument
	 *         tuple; {@code false} otherwise.
	 * @since 1.0
	 */
	public boolean equivalentTo( Tuple<?> other,
			Equivalencer<I, Object> equivalencer ) {
		if ( other == this )
			return true;
		final int size = wrapped.size( );
		if ( size != other.size( ) ) {
			return false;
		}
		for ( int i = 0; i < size; i++ ) {
			I e = wrapped.get( i );
			Object oe = other.get( i );
			if ( !( e == null ? oe == null : equivalencer.equivalent( e, oe ) ) ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>
	 * Returns the index of the first slot of a subtuple that fulfils certain
	 * requirements, if such a subtuple exists.
	 * </p>
	 * 
	 * 
	 * <p>
	 * This method invokes
	 * {@link KnittingTuple#findSubtuple(Tuple,int,Equivalencer)
	 * findSubtuple(Tuple,int,Equivalencer)} using an equivalencer that marks two
	 * objects as equivalent if and only if they are
	 * {@linkplain java.lang.Object#equals equal} to each other or both
	 * {@code null}.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @param equivalencer
	 *          A system that can tell whether two objects are equivalent.
	 * @param skip
	 *          The number of slots to skip.
	 * @return The index of the first slot of a subtuple that fulfils certain
	 *         requirements, if such a subtuple exists.
	 * @since 1.0
	 */
	public Optional<Integer> findSubtuple( Tuple<I> other, int skip ) {
		return findSubtuple( other, skip, KnittingTuple::equal_null );
	}

	/**
	 * <p>
	 * Returns the index of the first slot of a subtuple that fulfils certain
	 * requirements, if such a subtuple exists.
	 * </p>
	 * 
	 * <p>
	 * The subtuple must be equivalent to the argument tuple.
	 * </p>
	 * 
	 * <p>
	 * The index of the first slot of the subtuple must be greater than or equal
	 * to the argument number {@code skip}.
	 * </p>
	 * 
	 * <p>
	 * This method uses the argument {@code equivalencer} to decide whether an
	 * element of this tuple is equal to an element of the argument tuple.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @param equivalencer
	 *          A system that can tell whether two objects are equivalent.
	 * @param skip
	 *          The number of slots to skip.
	 * @return The index of the first slot of a subtuple that fulfils certain
	 *         requirements, if such a subtuple exists.
	 * @since 1.0
	 */
	public Optional<Integer> findSubtuple( Tuple<I> other, int skip,
			Equivalencer<I, Object> equivalencer ) {
		int size = size( );
		if ( skip > size || skip < 0 ) {
			throw new IllegalArgumentException( );
		}
		int target_size = other.size( );
		if ( target_size == 0 ) {
			return Optional.of( skip );
		}
		if ( skip == size ) {
			return Optional.empty( );
		}
		int limit = size - target_size;
		I first = other.get( 0 );
		for ( int i = skip; i <= limit; i++ ) {
			while ( i <= limit && !equivalencer.equivalent( get( i ), first ) ) {
				i++;
			}
			if ( i <= limit ) {
				int j = i + 1;
				int end = i + target_size;
				int k = 1;
				while ( j < end && equal_null( get( j ), other.get( k ) ) ) {
					j++;
					k++;
				}
				if ( j == end ) {
					return Optional.of( i );
				}
			}
		}
		return Optional.empty( );
	}

	/**
	 * <p>
	 * Returns the element mapped to {@code index} by this tuple.
	 * </p>
	 * 
	 * @param index
	 *          A natural number. A negative numbers counts as zero.
	 * @return The element mapped to {@code index} by this tuple.
	 * @throws IllegalArgumentException
	 *           when {@code index} is larger than or equal to the size of this
	 *           tuple.
	 * 
	 * @since 1.0
	 */
	@Override
	public I get( int index ) {
		int final_index = index < 0 ? 0 : index;
		if ( final_index >= size( ) ) {
			throw new IllegalArgumentException( );
		}
		return wrapped.get( final_index );
	}

	/**
	 * <p>
	 * Returns a view showing the first {@code show} elements of this tuple
	 * visible after hiding the first {@code hide} elements.
	 * </p>
	 * 
	 * <p>
	 * The returned view may be empty. This happens when this tuple's size is
	 * smaller than {@code hide}.
	 * </p>
	 * 
	 * <p>
	 * The returned view may contain less than {@code show} elements. This happens
	 * when this tuple's size is smaller than {@code hide + show}.
	 * </p>
	 * 
	 * @param hide
	 *          The number of elements to hide. A negative numbers counts as zero.
	 * @param show
	 *          The number of elements to show. A negative numbers counts as zero.
	 * @return A view showing the first {@code show} elements of this tuple
	 *         visible after hiding the first {@code hide} elements.
	 * @since 1.0
	 */
	public KnittingTuple<I> head( int hide, int show ) {
		int final_show = show < 0 ? 0 : show;
		int final_hide = hide < 0 ? 0 : hide;
		return wrap( new Subtuple<>( wrapped, final_hide, final_show ) );
	}

	/**
	 * <p>
	 * Returns a view hiding the first {@code hide} elements of this tuple.
	 * </p>
	 * 
	 * <p>
	 * The returned view may be empty. This happens when this tuple's size is
	 * smaller than {@code hide}.
	 * </p>
	 * 
	 * @param hide
	 *          The number of elements to hide. A negative numbers counts as zero.
	 * @return A view hiding the first {@code hide} elements of this tuple.
	 * @since 1.0
	 */
	public KnittingTuple<I> headless( int hide ) {
		int final_hide = hide < 0 ? 0 : hide;
		return wrap( new Subtuple<>( wrapped, final_hide, wrapped.size( ) ) );
	}

	/**
	 * <p>
	 * Returns a longest common subsequence between this tuple and the argument
	 * tuple. Elements are drawn from this tuple.
	 * </p>
	 * 
	 * <p>
	 * This method invokes {@link KnittingTuple#lcs(Tuple,Equivalencer) lcs(Tuple,
	 * Equivalencer)} using an equivalencer that marks two objects as equivalent
	 * if and only if they are {@linkplain java.lang.Object#equals equal} to each
	 * other or both {@code null}.
	 * </p>
	 * 
	 * @param masks
	 *          Other tuples.
	 * @return a longest common subsequence between this tuple and the argument
	 *         tuple.
	 * @since 1.0
	 */
	public ArrayList<I> lcs(
			Tuple<I> mask ) {
		return lcs( mask, KnittingTuple::equal_null );
	}

	/**
	 * <p>
	 * Returns a longest common subsequence between this tuple and the argument
	 * tuple. Elements are drawn from this tuple.
	 * </p>
	 * 
	 * <p>
	 * In general, there may be more than one longest common subsequences between
	 * two tuples.
	 * </p>
	 * 
	 * @param masks
	 *          Other tuples.
	 * @param equivalencer
	 *          A system that can tell whether two objects are equivalent.
	 * @return a longest common subsequence between this tuple and the argument
	 *         tuple.
	 * @since 1.0
	 */
	public ArrayList<I> lcs(
			Tuple<I> mask,
			Equivalencer<I, Object> equivalencer ) {
		ArrayList<I> result = new ArrayList<>( );
		for ( Bi<I, I> bi : diff( mask, equivalencer ) ) {
			if ( bi.front( ) != null && bi.back( ) != null ) {
				result.add( bi.front( ) );
			}
		}
		return result;
	}

	/**
	 * <p>
	 * Returns the intersection of some longest common subsequences between this
	 * tuple and each tuple in the argument cursor. Elements are drawn from this
	 * tuple.
	 * </p>
	 * 
	 * <p>
	 * This method invokes
	 * {@link KnittingTuple#lcsIntersection(Tuple,Equivalencer)
	 * lcsIntersection(Tuple, Equivalencer)} using an equivalencer that marks two
	 * objects as equivalent if and only if they are
	 * {@linkplain java.lang.Object#equals equal} to each other or both
	 * {@code null}.
	 * </p>
	 * 
	 * @param masks
	 *          A cursor of tuples.
	 * @return the intersection of some longest common subsequences between this
	 *         tuple and each tuple in the argument cursor.
	 * @since 1.0
	 */
	public ArrayList<I> lcsIntersection(
			Cursor<? extends Tuple<I>> masks ) {
		return lcsIntersection( masks, KnittingTuple::equal_null );
	}

	/**
	 * <p>
	 * Returns the intersection of some longest common subsequences between this
	 * tuple and each tuple in the argument cursor. Elements are drawn from this
	 * tuple.
	 * </p>
	 * 
	 * <p>
	 * The intersection is defined here as the list of element of this tuple that
	 * appear in every longest common subsequences computed between this tuple and
	 * the other tuples.
	 * </p>
	 * 
	 * <p>
	 * In general, there may be more than one longest common subsequences between
	 * two tuples. This implementation does not guarantee that the intersection is
	 * the longest or the shortest intersection subsequence. However, it does
	 * guarantee that, for each element in the intersection, an equivalent element
	 * can be found in each of the other tuples.
	 * </p>
	 * 
	 * <p>
	 * In particular, this method does not guarantee that it returns the longest
	 * subsequence common to all tuples.
	 * </p>
	 * 
	 * @param masks
	 *          A cursor of tuples.
	 * @param equivalencer
	 *          A system that can tell whether two objects are equivalent.
	 * @return the intersection of some longest common subsequences between this
	 *         tuple and each tuple in the argument cursor.
	 * @since 1.0
	 */
	public ArrayList<I> lcsIntersection(
			Cursor<? extends Tuple<I>> masks,
			Equivalencer<I, Object> equivalencer ) {

		ArrayList<Boolean> keeps = new ArrayList<>( );
		for ( int i = 0; i < size( ); i++ ) {
			keeps.add( true );
		}

		for ( Tuple<I> single_mask : KnittingCursor.wrap( masks ).once( ) ) {

			int j = 0;
			for ( Bi<I, I> bi : diff( single_mask, equivalencer ) ) {
				if ( bi.front( ) != null && bi.back( ) != null ) {
					keeps.set( j, false );
				}
				if ( bi.front( ) != null ) {
					j++;
				}
			}
		}

		ArrayList<I> result = new ArrayList<>( );
		int j = 0;
		for ( Boolean keep : keeps ) {
			if ( keep ) {
				result.add( get( j ) );
			}
			j++;
		}
		return result;
	}

	/**
	 * <p>
	 * Returns the union of some longest common subsequences between this tuple
	 * and each tuple in the argument cursor. Elements are drawn from this tuple.
	 * </p>
	 * 
	 * <p>
	 * This method invokes {@link KnittingTuple#lcsUnion(Tuple,Equivalencer)
	 * lcsUnion(Tuple, Equivalencer)} using an equivalencer that marks two objects
	 * as equivalent if and only if they are {@linkplain java.lang.Object#equals
	 * equal} to each other or both {@code null}.
	 * </p>
	 * 
	 * @param masks
	 *          A cursor of tuples.
	 * @param equivalencer
	 *          A system that can tell whether two objects are equivalent.
	 * @return the union of some longest common subsequences between this tuple
	 *         and each tuple in the argument cursor.
	 * @since 1.0
	 */
	public ArrayList<I> lcsUnion(
			Cursor<? extends Tuple<I>> masks ) {
		return lcsUnion( masks, KnittingTuple::equal_null );
	}

	/**
	 * <p>
	 * Returns the union of some longest common subsequences between this tuple
	 * and each tuple in the argument cursor. Elements are drawn from this tuple.
	 * </p>
	 * 
	 * <p>
	 * The union is defined here as the list of element of this tuple that appear
	 * in one or more of the longest common subsequences computed between this
	 * tuple and the other tuples.
	 * </p>
	 * 
	 * <p>
	 * In general, there may be more than one longest common subsequences between
	 * two tuples. This implementation does not guarantee that the union is the
	 * longest or the shortest union subsequence. However, it does guarantee that,
	 * for each element in the union, an equivalent element can be found in one of
	 * the other tuples.
	 * </p>
	 * 
	 * @param masks
	 *          A cursor of tuples.
	 * @param equivalencer
	 *          A system that can tell whether two objects are equivalent.
	 * @return the union of some longest common subsequences between this tuple
	 *         and each tuple in the argument cursor.
	 * @since 1.0
	 */
	public ArrayList<I> lcsUnion(
			Cursor<? extends Tuple<I>> masks,
			Equivalencer<I, Object> equivalencer ) {

		ArrayList<Boolean> keeps = new ArrayList<>( );
		for ( int i = 0; i < size( ); i++ ) {
			keeps.add( false );
		}

		for ( Tuple<I> single_mask : KnittingCursor.wrap( masks ).once( ) ) {

			int j = 0;
			for ( Bi<I, I> bi : diff( single_mask, equivalencer ) ) {
				if ( bi.front( ) != null && bi.back( ) != null ) {
					keeps.set( j, true );
				}
				if ( bi.front( ) != null ) {
					j++;
				}
			}
		}

		ArrayList<I> result = new ArrayList<>( );
		int j = 0;
		for ( Boolean keep : keeps ) {
			if ( keep ) {
				result.add( get( j ) );
			}
			j++;
		}
		return result;
	}

	/**
	 * <p>
	 * Returns a view. For each element {@code E} of this tuple, the view shows
	 * the element returned by the argument {@code stateless_function} when
	 * invoked with argument {@code E}.
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
	public <O> KnittingTuple<O> map( Function<? super I, O> stateless_function ) {
		return wrap( new Tuple<O>( ) {

			@Override
			public O get( int index ) {
				return stateless_function.apply( wrapped.get( index ) );
			}

			@Override
			public int size( ) {
				return wrapped.size( );
			}
		} );
	}

	/**
	 * <p>
	 * When this tuple has a single slot, this method returns the content of that
	 * slot. Otherwise, it throws an {@code IllegalStateException}.
	 * </p>
	 * 
	 * @return The only element accessible via this tuple.
	 * @throws IllegalStateException
	 *           when it is not the case that there is exactly one element.
	 * @since 1.0
	 */
	public I one( ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			return this.asKnittingCursor( ).one( );
		}
	}

	/**
	 * <p>
	 * When this tuple provides access to one non-null element only, this method
	 * returns it wrapped in an optional.
	 * </p>
	 * 
	 * <p>
	 * When it not the case that there is exactly one element, or when the only
	 * element accessible is {@code null}, this method returns an empty optional.
	 * </p>
	 * 
	 * @return The only element accessible via this tuple, if any, or an empty
	 *         optional.
	 * @since 1.0
	 */
	public Optional<I> optionalOne( ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			return asKnittingCursor( ).optionalOne( );
		}
	}

	/**
	 * Returns a view of the concatenation of the argument tuple before this
	 * tuple.
	 * 
	 * @param tail
	 *          A tuple to concatenate before this tuple.
	 * @return A view of the concatenation of the argument tuple before this
	 *         tuple.
	 * @since 1.0
	 */
	public KnittingTuple<I> prepend( Tuple<I> head ) {
		KnittingTuple<I> outer_this = this;
		Tuple<I> result = new Tuple<I>( ) {

			@Override
			public I get( int index ) {
				return index < head.size( ) ? head.get( index ) : outer_this
						.get( index - head.size( ) );
			}

			@Override
			public int size( ) {
				return head.size( ) + outer_this.size( );
			}
		};
		return wrap( result );
	}

	/**
	 * <p>
	 * Returns the result of a computation taking into account all the elements of
	 * this tuple.
	 * </p>
	 * 
	 * <p>
	 * This method stores into a local variable the argument {@code zero}. Then,
	 * for each element {@code E} in this tuple, this method invokes the argument
	 * {@code bifunction} using the content of the local variable and {@code E}.
	 * At the end of each such invocation, this method stores into the local
	 * variable the result of the invocation. Finally, this method returns the
	 * content of the local variable.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of argument {@code zero} and of the elements returned by
	 *          the argument {@code bifunction}.
	 * @param zero
	 *          The initial value for the reduction.
	 * @param bifunction
	 *          A bifunction that will be invoked once for each element of this
	 *          tuple.
	 * @return the result of a computation taking into account all the elements of
	 *         this tuple.
	 * @since 1.0
	 */
	public <K> K reduce( K zero, BiFunction<K, I, K> fun ) {
		K reduction = zero;
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Cursor<I> kc = pull( );
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
	 * Returns a view of this tuple where the order of the elements is reversed.
	 * </p>
	 * 
	 * @return a view of this tuple where the order of the elements is reversed.
	 * @since 1.0
	 */
	public KnittingTuple<I> reverse( ) {
		int len = size( );
		KnittingTuple<I> outer_this = this;
		return wrap( new Tuple<I>( ) {

			@Override
			public I get( int index ) {
				return outer_this.get( len - ( index + 1 ) );
			}

			@Override
			public int size( ) {
				return len;
			}
		} );
	}

	/**
	 * <p>
	 * Returns the size of this tuple.
	 * </p>
	 * 
	 * @return The size of this tuple.
	 * 
	 * @since 1.0
	 */
	@Override
	public int size( ) {
		return wrapped.size( );
	}

	private Cursor<I> pull( ) {
		return new Cursor<I>( ) {

			final int size = wrapped.size( );

			int i = 0;

			@Override
			public I next( )
					throws EndOfCursorException {
				if ( i >= size )
					throw EndOfCursorException.neo( );
				return wrapped.get( i++ );
			}
		};
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
	 * Returns {@code true} when the argument tuple is a prefix of this tuple;
	 * {@code false} otherwise.
	 * </p>
	 * 
	 * <p>
	 * This method invokes {@link KnittingTuple#startsWith(Tuple,Equivalencer)
	 * startsWith(Tuple, Equivalencer)} using an equivalencer that marks two
	 * objects as equivalent if and only if they are
	 * {@linkplain java.lang.Object#equals equal} to each other or both
	 * {@code null}.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @return {@code true} when the argument tuple is a prefix of this tuple;
	 *         {@code false} otherwise.
	 * @since 1.0
	 */
	public boolean startsWith( KnittingTuple<I> other ) {
		return startsWith( other, KnittingTuple::equal_null );
	}

	/**
	 * <p>
	 * Returns {@code true} when the argument tuple is a prefix of this tuple;
	 * {@code false} otherwise.
	 * </p>
	 * 
	 * <p>
	 * This method uses the argument {@code equivalencer} to decide whether an
	 * element of this tuple is equal to an element of the argument tuple.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @param equivalencer
	 *          A system that can tell whether two objects are equivalent.
	 * @return {@code true} when the argument tuple is a prefix of this tuple;
	 *         {@code false} otherwise.
	 * @since 1.0
	 */
	public boolean startsWith( KnittingTuple<I> other,
			Equivalencer<I, Object> equivalencer ) {
		if ( size( ) < other.size( ) )
			return false;
		return head( 0, other.size( ) ).equivalentTo( other, equivalencer );
	}

	/**
	 * <p>
	 * Returns a view of the last {@code show} elements of this tuple visible
	 * after hiding the last {@code hide} elements.
	 * </p>
	 * 
	 * <p>
	 * The returned view may be empty. This happens when this tuple's size is
	 * smaller than {@code hide}.
	 * </p>
	 * 
	 * <p>
	 * The returned view may contain less than {@code show} elements. This happens
	 * when this tuple's size is smaller than {@code hide + show}.
	 * </p>
	 *
	 * @param hide
	 *          The number of elements to hide. A negative numbers counts as zero.
	 * @param show
	 *          The number of elements to show. A negative numbers counts as zero.
	 * @return A view of the last {@code show} elements of this tuple visible
	 *         after hiding the last {@code hide} elements.
	 * @since 1.0
	 */
	public KnittingTuple<I> tail( int hide, int show ) {
		int final_show = show < 0 ? 0 : show;
		int final_hide = hide < 0 ? 0 : hide;
		int len = size( ) - final_hide;
		if ( len > final_show ) {
			len = final_show;
		}
		int skip = size( ) - ( final_hide + len );
		if ( skip < 0 ) {
			skip = 0;
		}
		int final_len = len;
		int final_skip = skip;
		return wrap( new Subtuple<>( wrapped, final_skip, final_len ) );
	}

	/**
	 * <p>
	 * Returns a view hiding the last {@code hide} elements of this tuple.
	 * </p>
	 * 
	 * <p>
	 * The returned view may be empty. This happens when this tuple's size is
	 * smaller than {@code hide}.
	 * </p>
	 *
	 * @param hide
	 *          The number of elements to hide. A negative numbers counts as zero.
	 * @return A view hiding the last {@code hide} elements of this tuple.
	 * @since 1.0
	 */
	public KnittingTuple<I> tailless( int hide ) {
		int final_hide = hide < 0 ? 0 : hide;
		int len = size( ) - final_hide;
		if ( len < 0 ) {
			len = 0;
		}
		int final_len = len;
		return wrap( new Subtuple<>( wrapped, 0, final_len ) );
	}

	/**
	 * <p>
	 * Returns an empty tuple.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of elements in the empty tuple.
	 * @return An empty tuple.
	 * 
	 * @since 1.0
	 */
	public static <K> KnittingTuple<K> empty( ) {
		return private_empty( );
	}

	@SuppressWarnings("unchecked")
	private static <K> KnittingTuple<K> private_empty( ) {
		return (KnittingTuple<K>) neo;
	}

	private static final KnittingTuple<Void> neo = wrap( new Tuple<Void>( ) {

		@Override
		public Void get( int index ) {
			throw new IllegalArgumentException( );
		}

		@Override
		public int size( ) {
			return 0;
		}

	} );

	/**
	 * Returns an equivalencer that marks two objects as equivalent if and only if
	 * they are {@linkplain java.lang.Object#equals equal} to each other or both
	 * {@code null}.
	 * 
	 * @return an equivalencer that marks two objects as equivalent if and only if
	 *         they are {@linkplain java.lang.Object#equals equal} to each other
	 *         or both {@code null}.
	 */
	public static <K> Equivalencer<K, Object> getNullEquivalencer( ) {
		return new Equivalencer<K, Object>( ) {

			@Override
			public boolean equivalent( K first, Object second ) {
				if ( first == null && second == null )
					return true;
				if ( first == null || second == null )
					return false;
				return first.equals( second );
			}
		};
	}
}
