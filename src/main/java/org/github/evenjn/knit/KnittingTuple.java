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

import org.github.evenjn.lang.BasicEquivalencer;
import org.github.evenjn.lang.BasicRook;
import org.github.evenjn.lang.Equivalencer;
import org.github.evenjn.lang.Ring;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;
import org.github.evenjn.yarn.Tuple;

/**
 * 
 * <h1>KnittingTuple</h1>
 * 
 * <p>
 * A {@code KnittingTuple} wraps a {@link org.github.evenjn.yarn.Tuple Tuple}
 * and provides utility methods to access its contents.
 * </p>
 * 
 * <p>
 * Briefly, a {@code KnittingTuple} may be used in three ways:
 * </p>
 * 
 * <ul>
 * <li>As a simple tuple, invoking the
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
 * Public instance methods of {@code KnittingTuple} fall into one of the
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
 * <li>{@link #asTupleValue()}</li>
 * <li>{@link #asTupleValue(Equivalencer)}</li>
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
 * <li>{@link #findSubtuple(Tuple, int)}</li>
 * <li>{@link #findSubtuple(Tuple, int, Equivalencer)}</li>
 * <li>{@link #longestCommonSubtuple(Tuple)}</li>
 * <li>{@link #longestCommonSubtuple(Tuple, Equivalencer)}</li>
 * <li>{@link #longestCommonSubtupleUnion(Cursor)}</li>
 * <li>{@link #longestCommonSubtupleUnion(Cursor, Equivalencer)}</li>
 * <li>{@link #longestCommonSubtupleIntersection(Cursor)}</li>
 * <li>{@link #longestCommonSubtupleIntersection(Cursor, Equivalencer)}</li>
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
 * <li>{@link #consume(Ring)}</li>
 * <li>{@link #one()}</li>
 * <li>{@link #optionalOne()}</li>
 * <li>{@link #reduce(Object, BiFunction)}</li>
 * </ul>
 *
 * <p>
 * This class is part of package {@link org.github.evenjn.knit Knit}.
 * </p>
 * 
 * @param <I>
 *          The type of elements accessible via this tuple.
 * @since 1.0
 */
public class KnittingTuple<I> implements
		Tuple<I> {

	private final Tuple<I> wrapped;

	/**
	 * <p>
	 * {@code append} returns a view of the concatenation of the argument tuple
	 * after this tuple.
	 * </p>
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
	 * {@code asIterable} returns a view of this tuple as an
	 * {@link java.util.Iterable Iterable}.
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
	 * {@code asIterator} returns a view of this tuple as an
	 * {@link java.util.Iterator Iterator}.
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
	 * {@code asKnittingCursable} returns a view of this tuple as a
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
	 * {@code asKnittingCursor} returns a view of this tuple as a
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
	 * {@code asStream} returns a view of this tuple as a
	 * {@link java.util.stream.Stream Stream}.
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
	 * {@code asTupleValue} returns a view of this tuple as a
	 * {@link org.github.evenjn.knit.TupleValue TupleValue}.
	 * </p>
	 * 
	 * <p>
	 * This method invokes {@link KnittingTuple#asTupleValue(Equivalencer)
	 * asTupleValue(Equivalencer)} using a
	 * {@link org.github.evenjn.lang.BasicEquivalencer BasicEquivalencer}.
	 * </p>
	 * 
	 * @return A view of this tuple as a {@link org.github.evenjn.knit.TupleValue
	 *         TupleValue}.
	 * @since 1.0
	 */
	public TupleValue<I> asTupleValue( ) {
		return new TupleValue<>( this, KnittingTuple.private_equivalencer( ) );
	}

	/**
	 * <p>
	 * {@code asTupleValue} returns a view of this tuple as a
	 * {@link org.github.evenjn.knit.TupleValue TupleValue}.
	 * </p>
	 * 
	 * <p>
	 * The resulting TupleValue implementation of
	 * {@linkplain java.lang.Object#equals(Object) equals}, when invoked passing a
	 * {@link org.github.evenjn.yarn.Tuple Tuple} argument, uses the argument
	 * {@code equivalencer} to decide whether an element of this tuple is equal to
	 * an element of the argument tuple.
	 * </p>
	 * 
	 * @param equivalencer
	 *          A system that can tell whether two objects are equivalent.
	 * @return A view of this tuple as a {@link org.github.evenjn.knit.TupleValue
	 *         TupleValue}.
	 * @since 1.0
	 */
	public TupleValue<I> asTupleValue( Equivalencer<I, Object> equivalencer ) {
		return new TupleValue<>( this, equivalencer );
	}

	/**
	 * <p>
	 * {@code collect} adds all elements of this tuple to the argument collection,
	 * then returns it.
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
			return this.asKnittingCursor( ).collect( collection );
		}
	}

	/**
	 * <p>
	 * {@code consume} feeds a consumer with the elements of this tuple.
	 * </p>
	 * 
	 * <p>
	 * Obtains a consumer from the argument {@code consumer_provider}, hooking it
	 * to a local, temporary rook. Then, this method iterates over all the slots,
	 * passing the object in each slot to the consumer, until the end of the
	 * slots.
	 * </p>
	 * 
	 * @param consumer_provider
	 *          A system that provides a consumer.
	 * @since 1.0
	 */
	public void consume( Ring<? extends Consumer<? super I>> consumer_provider ) {
		try ( BasicRook rook = new BasicRook( ) ) {
			Consumer<? super I> consumer = consumer_provider.get( rook );
			this.asKnittingCursor( ).peek( consumer ).roll( );
		}
	}

	/**
	 * <p>
	 * {@code contains} returns {@code true} when the argument tuple is a subtuple
	 * of this tuple; {@code false} otherwise.
	 * </p>
	 * 
	 * <p>
	 * This method invokes {@link KnittingTuple#contains(Tuple,Equivalencer)
	 * contains(Tuple, Equivalencer)} using a
	 * {@link org.github.evenjn.lang.BasicEquivalencer BasicEquivalencer}.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @return {@code true} when the argument tuple is a subtuple of this tuple;
	 *         {@code false} otherwise.
	 * @since 1.0
	 */
	public <Y> boolean contains( Tuple<Y> other ) {
		return contains( other, private_equivalencer( ) );
	}

	/**
	 * <p>
	 * {@code contains} returns {@code true} when the argument tuple is a subtuple
	 * of this tuple; {@code false} otherwise.
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
	public <Y> boolean contains( Tuple<Y> other,
			Equivalencer<I, Y> equivalencer ) {
		if ( size( ) < other.size( ) )
			return false;
		return findSubtuple( other, 0, equivalencer ).isPresent( );
	}

	/**
	 * <p>
	 * {@code diff} returns an alignment of this tuple with the argument tuple.
	 * </p>
	 * 
	 * <p>
	 * This method invokes {@link KnittingTuple#diff(Tuple,Equivalencer)
	 * diff(Tuple, Equivalencer)} using a
	 * {@link org.github.evenjn.lang.BasicEquivalencer BasicEquivalencer}.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @return An alignment of this tuple with the argument tuple.
	 * @since 1.0
	 */
	public <Y> Iterable<DiffPair<I, Y>> diff( Tuple<Y> other ) {
		return diff( other, private_equivalencer( ) );
	}

	/**
	 * <p>
	 * {@code diff} returns an alignment of this tuple with the argument tuple,
	 * represented as a list of {@link org.github.evenjn.knit.DiffPair pairs}.
	 * </p>
	 * 
	 * <p>
	 * Each pair has two slots: one at the front, one at the back. At least one of
	 * the two slots is filled in with an element of the tuples.
	 * </p>
	 * 
	 * <p>
	 * There is no special provision for {@code null}. When a tuple contains
	 * {@code null}, a slot will be filled with {@code null}.
	 * </p>
	 * 
	 * <p>
	 * Whenever the front slot of a pair
	 * {@linkplain org.github.evenjn.knit.DiffPair#hasFront( ) is filled in}, that
	 * slot contains an element of this tuple. That element may be {@code null}.
	 * </p>
	 * 
	 * <p>
	 * Whenever the back slot of a pair
	 * {@linkplain org.github.evenjn.knit.DiffPair#hasBack( ) is filled in}, that
	 * slot contains an element of the argument tuple. That element may be
	 * {@code null}.
	 * </p>
	 * 
	 * <p>
	 * Whenever both the front slot and the back slot
	 * {@linkplain org.github.evenjn.knit.DiffPair#hasBoth( ) are filled in}, the
	 * content of the front slot is equivalent (as specified by the argument
	 * {@code equivalencer}) to the content of the second slot. They may be both
	 * {@code null}.
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
	public <Y> Iterable<DiffPair<I, Y>> diff(
			Tuple<Y> other,
			Equivalencer<I, Y> equivalencer ) {
		return new DiffIterable<I, Y>( this, other, equivalencer );
	}

	/**
	 * <p>
	 * {@code distance} Returns the
	 * <a href= "https://en.wikipedia.org/wiki/Levenshtein_distance" >Levenshtein
	 * distance</a> between this tuple and the argument tuple.
	 * </p>
	 * 
	 * <p>
	 * This method invokes {@link KnittingTuple#distance(Tuple,Equivalencer)
	 * distance(Tuple, Equivalencer)} using a
	 * {@link org.github.evenjn.lang.BasicEquivalencer BasicEquivalencer}.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @return The distance between this tuple and the argument tuple.
	 * @since 1.0
	 */
	public <Y> int distance( Tuple<Y> other ) {
		return distance( other, private_equivalencer( ) );
	}

	/**
	 * <p>
	 * {@code distance} returns the
	 * <a href= "https://en.wikipedia.org/wiki/Levenshtein_distance" >Levenshtein
	 * distance</a> between this tuple and the argument tuple.
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
	public <Y> int distance( Tuple<Y> other,
			Equivalencer<I, Y> equivalencer ) {
		Tuple<I> s = this;
		Tuple<Y> t = other;

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
			return KnittingTuple.wrap( other ).distance( this,
					equivalencer.swap( ) );
		}

		final int p[] = new int[n + 1];
		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t
		int upper_left;
		int upper;

		Y t_j; // jth character of t
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
	 * {@code endsWith} returns {@code true} when the argument tuple is a suffix
	 * of this tuple; {@code false} otherwise.
	 * </p>
	 * 
	 * <p>
	 * This method invokes {@link KnittingTuple#endsWith(Tuple,Equivalencer)
	 * endsWith(Tuple, Equivalencer)} using a
	 * {@link org.github.evenjn.lang.BasicEquivalencer BasicEquivalencer}.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @return {@code true} when the argument tuple is a suffix of this tuple;
	 *         {@code false} otherwise.
	 * @since 1.0
	 */
	public boolean endsWith( Tuple<I> other ) {
		return endsWith( other, private_equivalencer( ) );
	}

	/**
	 * <p>
	 * {@code endsWith} returns {@code true} when the argument tuple is a suffix
	 * of this tuple; {@code false} otherwise.
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
	public <Y> boolean endsWith( Tuple<Y> other,
			Equivalencer<I, Y> equivalencer ) {
		if ( size( ) < other.size( ) )
			return false;
		return headless( size( ) - other.size( ) ).equivalentTo( other,
				equivalencer );
	}

	/**
	 * <p>
	 * {@code equivalentTo} returns {@code true} when this tuple and the argument
	 * tuple have the same number of slots, and when the content of each i-th slot
	 * of this tuple is equivalent to the content of the i-th slot of the argument
	 * tuple. Returns {@code false} otherwise.
	 * </p>
	 * 
	 * <p>
	 * This method invokes {@link KnittingTuple#equivalentTo(Tuple,Equivalencer)
	 * equivalentTo(Tuple, Equivalencer)} using a
	 * {@link org.github.evenjn.lang.BasicEquivalencer BasicEquivalencer}.
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
	public <Y> boolean equivalentTo( Tuple<Y> other ) {
		return equivalentTo( other, private_equivalencer( ) );
	}

	/**
	 * <p>
	 * {@code equivalentTo} returns {@code true} when this tuple and the argument
	 * tuple have the same number of slots, and when the content of each i-th slot
	 * of this tuple is equivalent to the content of the i-th slot of the argument
	 * tuple. Returns {@code false} otherwise.
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
	public <Y> boolean equivalentTo( Tuple<Y> other,
			Equivalencer<I, Y> equivalencer ) {
		if ( other == this )
			return true;
		final int size = wrapped.size( );
		if ( size != other.size( ) ) {
			return false;
		}
		for ( int i = 0; i < size; i++ ) {
			I e = wrapped.get( i );
			Y oe = other.get( i );
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
	 * findSubtuple(Tuple,int,Equivalencer)} using a
	 * {@link org.github.evenjn.lang.BasicEquivalencer BasicEquivalencer}.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @param skip
	 *          The number of slots to skip.
	 * @return The index of the first slot of a subtuple that fulfils certain
	 *         requirements, if such a subtuple exists.
	 * @throws IllegalArgumentException
	 *           when {@code skip} is negative, or when it is larger than or equal
	 *           to the size of this tuple.
	 * @since 1.0
	 */
	public <Y> Optional<Integer> findSubtuple( Tuple<Y> other, int skip ) {
		return findSubtuple( other, skip, private_equivalencer( ) );
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
	 * @throws IllegalArgumentException
	 *           when {@code skip} is negative, or when it is larger than or equal
	 *           to the size of this tuple.
	 * @since 1.0
	 */
	public <Y> Optional<Integer> findSubtuple( Tuple<Y> other, int skip,
			Equivalencer<I, Y> equivalencer ) {
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
		Y first = other.get( 0 );
		for ( int i = skip; i <= limit; i++ ) {
			while ( i <= limit && !equivalencer.equivalent( get( i ), first ) ) {
				i++;
			}
			if ( i <= limit ) {
				int j = i + 1;
				int end = i + target_size;
				int k = 1;
				while ( j < end
						&& equivalencer.equivalent( get( j ), other.get( k ) ) ) {
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
	 *          A natural number. It must be non-negative.
	 * @return The element mapped to {@code index} by this tuple.
	 * @throws IllegalArgumentException
	 *           when {@code index} is negative, or when it is larger than or
	 *           equal to the size of this tuple.
	 * @since 1.0
	 */
	@Override
	public I get( int index ) {
		if ( index < 0 || index >= size( ) ) {
			throw new IllegalArgumentException( );
		}
		return wrapped.get( index );
	}

	/**
	 * <p>
	 * Returns a view showing the first {@code show} elements of this tuple
	 * visible after hiding the first {@code hide} elements.
	 * </p>
	 * 
	 * <p>
	 * Unlike {@link #subTuple(int, int)} this mehtod does not throw any exception
	 * when the arguments are out of the range of this tuple. This behaviour is
	 * consistent with {@link org.github.evenjn.knit.KnittingCursor#head(int, int)
	 * KnittingCursor's head}.
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
	 * Unlike {@link #subTuple(int, int)} this mehtod does not throw any exception
	 * when the arguments are out of the range of this tuple. This behaviour is
	 * consistent with {@link org.github.evenjn.knit.KnittingCursor#head(int, int)
	 * KnittingCursor's head}.
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
	 * This method invokes
	 * {@link KnittingTuple#longestCommonSubtuple(Tuple,Equivalencer)
	 * longestCommonSubtuple(Tuple, Equivalencer)} using a
	 * {@link org.github.evenjn.lang.BasicEquivalencer BasicEquivalencer}.
	 * </p>
	 * 
	 * @param tuple
	 *          Another tuple.
	 * @return a longest common subsequence between this tuple and the argument
	 *         tuple.
	 * @since 1.0
	 */
	public <Y> KnittingTuple<I> longestCommonSubtuple(
			Tuple<Y> tuple ) {
		return longestCommonSubtuple( tuple, private_equivalencer( ) );
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
	 * @param tuple
	 *          Another tuple.
	 * @param equivalencer
	 *          A system that can tell whether two objects are equivalent.
	 * @return a longest common subsequence between this tuple and the argument
	 *         tuple.
	 * @since 1.0
	 */
	public <Y> KnittingTuple<I> longestCommonSubtuple(
			Tuple<Y> tuple,
			Equivalencer<I, Y> equivalencer ) {
		ArrayList<I> result = new ArrayList<>( );
		for ( DiffPair<I, Y> bi : diff( tuple, equivalencer ) ) {
			if ( bi.hasBoth( ) ) {
				result.add( bi.front( ) );
			}
		}
		return wrap( result );
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
	 * {@link KnittingTuple#longestCommonSubtupleIntersection(Cursor,Equivalencer)
	 * longestCommonSubtupleIntersection(Cursor, Equivalencer)} using a
	 * {@link org.github.evenjn.lang.BasicEquivalencer BasicEquivalencer}.
	 * </p>
	 * 
	 * @param masks
	 *          A cursor of tuples.
	 * @return the intersection of some longest common subsequences between this
	 *         tuple and each tuple in the argument cursor.
	 * @since 1.0
	 */
	public <Y> KnittingTuple<I> longestCommonSubtupleIntersection(
			Cursor<? extends Tuple<Y>> masks ) {
		return longestCommonSubtupleIntersection( masks,
				private_equivalencer( ) );
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
	public <Y> KnittingTuple<I> longestCommonSubtupleIntersection(
			Cursor<? extends Tuple<Y>> masks,
			Equivalencer<I, Y> equivalencer ) {

		ArrayList<Boolean> keeps = new ArrayList<>( );
		for ( int i = 0; i < size( ); i++ ) {
			keeps.add( true );
		}

		for ( Tuple<Y> single_mask : KnittingCursor.wrap( masks ).once( ) ) {

			int j = 0;
			for ( DiffPair<I, Y> bi : diff( single_mask, equivalencer ) ) {
				if ( bi.hasFront( ) ) {
					if ( !bi.hasBoth( ) ) {
						keeps.set( j, false );
					}
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
		return wrap( result );
	}

	/**
	 * <p>
	 * Returns the union of some longest common subsequences between this tuple
	 * and each tuple in the argument cursor. Elements are drawn from this tuple.
	 * </p>
	 * 
	 * <p>
	 * This method invokes
	 * {@link KnittingTuple#longestCommonSubtupleUnion(Cursor,Equivalencer)
	 * longestCommonSubtupleUnion(Cursor, Equivalencer)} using a
	 * {@link org.github.evenjn.lang.BasicEquivalencer BasicEquivalencer}.
	 * </p>
	 * 
	 * @param masks
	 *          A cursor of tuples.
	 * @return the union of some longest common subsequences between this tuple
	 *         and each tuple in the argument cursor.
	 * @since 1.0
	 */
	public <Y> KnittingTuple<I> longestCommonSubtupleUnion(
			Cursor<? extends Tuple<Y>> masks ) {
		return longestCommonSubtupleUnion( masks, private_equivalencer( ) );
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
	public <Y> KnittingTuple<I> longestCommonSubtupleUnion(
			Cursor<? extends Tuple<Y>> masks,
			Equivalencer<I, Y> equivalencer ) {

		ArrayList<Boolean> keeps = new ArrayList<>( );
		for ( int i = 0; i < size( ); i++ ) {
			keeps.add( false );
		}

		for ( Tuple<Y> single_mask : KnittingCursor.wrap( masks ).once( ) ) {

			int j = 0;
			for ( DiffPair<I, Y> bi : diff( single_mask, equivalencer ) ) {
				if ( bi.hasFront( ) ) {
					if ( !bi.hasBoth( ) ) {
						keeps.set( j, false );
					}
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
		return wrap( result );
	}

	/**
	 * Returns the longest common prefix of this tuple and the argument tuple.
	 * 
	 * @param other
	 *          Another tuple.
	 * @param equivalencer
	 *          A system that can tell whether two objects are equivalent.
	 * @return The common prefix of this tuple and the argument tuple.
	 * @since 1.0
	 */
	public <Y> int longestCommonPrefix( Tuple<Y> other,
			Equivalencer<I, Y> equivalencer ) {
		int n = Math.min( size( ), other.size( ) );
		for ( int i = 0; i < n; i++ ) {
			if ( !equivalencer.equivalent( get( i ), other.get( i ) ) ) {
				return i;
			}
		}
		return n;
	}

	/**
	 * Returns the longest common suffix of this tuple and the argument tuple.
	 * 
	 * @param other
	 *          Another tuple.
	 * @param equivalencer
	 *          A system that can tell whether two objects are equivalent.
	 * @return The common suffix of this tuple and the argument tuple.
	 * @since 1.0
	 */
	public <Y> int longestCommonSuffix( Tuple<Y> other,
			Equivalencer<I, Y> equivalencer ) {
		int text1_length = size( );
		int text2_length = other.size( );
		int n = Math.min( text1_length, text2_length );
		for ( int i = 1; i <= n; i++ ) {
			if ( !equivalencer.equivalent( get( text1_length - i ),
					other.get( text2_length - i ) ) ) {
				return i - 1;
			}
		}
		return n;
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
		return wrap( new MapTuple<>( wrapped, stateless_function ) );
	}

	/**
	 * <p>
	 * Returns a view. For each element {@code E} of this tuple, the view shows
	 * the element wrapped in a {@link org.github.evenjn.knit.Numbered Numbered}
	 * object, containing the element itself and the number of elements preceding
	 * it in this tuple..
	 * </p>
	 * 
	 * @return A complex view.
	 * @since 1.0
	 */
	public KnittingTuple<Numbered<I>> numbered( ) {
		return wrap( new NumberedTuple<>( wrapped ) );
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
		try ( BasicRook rook = new BasicRook( ) ) {
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
		try ( BasicRook rook = new BasicRook( ) ) {
			return asKnittingCursor( ).optionalOne( );
		}
	}

	/**
	 * Returns a view of the concatenation of the argument tuple before this
	 * tuple.
	 * 
	 * @param head
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
	public <K> K reduce( K zero, BiFunction<K, I, K> bifunction ) {
		K reduction = zero;
		try ( BasicRook rook = new BasicRook( ) ) {
			Cursor<I> kc = pull( );
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
	 * {@code size} returns the size of this tuple.
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

	/**
	 * <p>
	 * {@code subTuple} returns a view showing elements of this tuple in slots
	 * between {@code n} and {@code m}, including {@code n} and excluding
	 * {@code m}.
	 * </p>
	 * 
	 * @param n
	 *          The index, in this tuple, of the element that will appear first in
	 *          the resulting view.
	 * @param m
	 *          One plus the index, in this tuple, of the element that will appear
	 *          last in the resulting view.
	 * @return A view showing elements of this tuple in slots between {@code n}
	 *         and {@code m}, including {@code n} and excluding {@code m}.
	 * @throws IllegalArgumentException
	 *           when {@code n} is negative, when {@code m} is negative, when
	 *           {@code n} is larger than {@code m}, when {@code m} is larger than
	 *           the size of this tuple, when {@code n} is larger than the size of
	 *           this tuple.
	 * @since 1.0
	 */
	public KnittingTuple<I> subTuple( int n, int m ) {
		if ( n < 0 ) {
			throw new IllegalArgumentException( );
		}
		if ( m < 0 ) {
			throw new IllegalArgumentException( );
		}
		if ( n > size( ) ) {
			throw new IllegalArgumentException( );
		}
		if ( m > size( ) ) {
			throw new IllegalArgumentException( );
		}
		if ( n > m ) {
			throw new IllegalArgumentException( );
		}
		return wrap( new Subtuple<>( wrapped, n, m - n ) );
	}

	/**
	 * <p>
	 * Returns {@code true} when the argument tuple is a prefix of this tuple;
	 * {@code false} otherwise.
	 * </p>
	 * 
	 * <p>
	 * This method invokes {@link KnittingTuple#startsWith(Tuple,Equivalencer)
	 * startsWith(Tuple, Equivalencer)} using a
	 * {@link org.github.evenjn.lang.BasicEquivalencer BasicEquivalencer}.
	 * </p>
	 * 
	 * @param other
	 *          Another tuple.
	 * @return {@code true} when the argument tuple is a prefix of this tuple;
	 *         {@code false} otherwise.
	 * @since 1.0
	 */
	public <Y> boolean startsWith( Tuple<Y> other ) {
		return startsWith( other, private_equivalencer( ) );
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
	public <Y> boolean startsWith( Tuple<Y> other,
			Equivalencer<I, Y> equivalencer ) {
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
	 * Unlike {@link #subTuple(int, int)} this mehtod does not throw any exception
	 * when the arguments are out of the range of this tuple. This behaviour is
	 * consistent with {@link org.github.evenjn.knit.KnittingCursor#head(int, int)
	 * KnittingCursor's head}.
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
	 * Unlike {@link #subTuple(int, int)} this mehtod does not throw any exception
	 * when the arguments are out of the range of this tuple. This behaviour is
	 * consistent with {@link org.github.evenjn.knit.KnittingCursor#head(int, int)
	 * KnittingCursor's head}.
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

	private KnittingTuple(Tuple<I> tuple) {
		this.wrapped = tuple;
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
		return (KnittingTuple<K>) empty;
	}

	private static final KnittingTuple<Void> empty = wrap( new Tuple<Void>( ) {

		@Override
		public Void get( int index ) {
			throw new IllegalArgumentException( );
		}

		@Override
		public int size( ) {
			return 0;
		}

	} );

	@SuppressWarnings("unchecked")
	private static <K, Y> Equivalencer<K, Y> private_equivalencer( ) {
		return (Equivalencer<K, Y>) basic_equivalencer;
	}

	private final static BasicEquivalencer<Object, Object> basic_equivalencer =
			new BasicEquivalencer<Object, Object>( );

	/**
	 * <p>
	 * {@code on} returns a new {@code KnittingTuple} providing access to the
	 * argument elements.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of the argument elements.
	 * @param elements
	 *          Elements to be wrapped in a new {@code KnittingTuple}.
	 * @return A new {@code KnittingTuple} providing access to the argument
	 *         elements.
	 * @since 1.0
	 */
	@SafeVarargs
	public static <K> KnittingTuple<K> on( K ... elements ) {
		return wrap( new ArrayTuple<K>( elements ) );
	}

	/**
	 * <p>
	 * {@code wrap} returns a view of the elements in the argument
	 * {@link java.util.ArrayList ArrayList}.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of elements in the argument {@link java.util.ArrayList
	 *          ArrayList}.
	 * @param arraylist
	 *          An {@link java.util.ArrayList ArrayList} of elements.
	 * @return A view of the elements in the argument {@link java.util.ArrayList
	 *         ArrayList}.
	 * @since 1.0
	 */
	public static <K> KnittingTuple<K> wrap( ArrayList<K> arraylist ) {
		return wrap( new ArrayListTuple<K>( arraylist ) );
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
	public static <K> KnittingTuple<K> wrap( K[] array ) {
		return wrap( new ArrayTuple<K>( array ) );
	}

	/**
	 * <p>
	 * {@code wrap} returns a view of the elements in the argument
	 * {@link org.github.evenjn.yarn.Tuple Tuple}.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of elements in the argument
	 *          {@link org.github.evenjn.yarn.Tuple Tuple}.
	 * @param tuple
	 *          A {@link org.github.evenjn.yarn.Tuple Tuple} of elements.
	 * @return A view of the elements in the argument
	 *         {@link org.github.evenjn.yarn.Tuple Tuple}.
	 * @since 1.0
	 */
	public static <K> KnittingTuple<K> wrap( Tuple<K> tuple ) {
		if ( tuple instanceof KnittingTuple ) {
			return (KnittingTuple<K>) tuple;
		}
		return new KnittingTuple<K>( tuple );
	}

	/**
	 * <p>
	 * {@code wrap} returns a view of the elements in the argument
	 * {@link java.util.Vector Vector}.
	 * </p>
	 * 
	 * @param <K>
	 *          The type of elements in the argument {@link java.util.Vector
	 *          Vector}.
	 * @param vector
	 *          A {@link java.util.Vector Vector} of elements.
	 * @return A view of the elements in the argument {@link java.util.Vector
	 *         Vector}.
	 * @since 1.0
	 */
	public static <K> KnittingTuple<K> wrap( Vector<K> vector ) {
		return wrap( new VectorTuple<K>( vector ) );
	}
}
