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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Function;

import org.github.evenjn.knit.DiffPatch.Diff;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;
import org.github.evenjn.yarn.Tuple;

/**
 * 
 * <h1>KnittingTuple</h1>
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

	public static <K> KnittingTuple<K> wrap( Vector<K> vector ) {
		return wrap( new VectorTuple<K>( vector ) );
	}

	public static <K> KnittingTuple<K> wrap( K[] array ) {
		return wrap( new ArrayTuple<K>( array ) );
	}

	private KnittingTuple(Tuple<I> tuple) {
		this.wrapped = tuple;
	}

	public Iterable<Bi<I, I>> diff( Tuple<I> other ) {
		return ( ) -> KnittingCursor.wrap(
				new DiffIterator<I>( this, other, DiffPatch::equal_or_both_null ) )
				.asIterator( );
	}

	public Iterable<Bi<I, I>> diff( Tuple<I> other,
			Equivalencer<I> equivalencer ) {
		return ( ) -> KnittingCursor
				.wrap( new DiffIterator<I>( this, other, equivalencer ) )
				.asIterator( );
	}

	public int distance( Tuple<I> other ) {
		return distance( other, DiffPatch::equal_or_both_null );
	}

	/*
	 * Computes Damerau-Levenshtein distance.
	 */
	public int distance( Tuple<I> other,
			Equivalencer<I> equivalencer ) {
		DiffPatch<I> dmp = new DiffPatch<I>( );
		LinkedList<Diff<I>> diffs =
				dmp.diff_main( this, KnittingTuple.wrap( other ), equivalencer );
		return dmp.diff_levenshtein( diffs );
	}

	/*
	 * returns elements which survive the diff with the mask.
	 */
	public Vector<I> intersecting(
			Tuple<I> mask,
			Equivalencer<I> equivalencer ) {
		return intersectingAny( KnittingCursor.on( mask ), equivalencer );
	}

	/*
	 * returns only elements which survive the diff in one or more masks
	 */
	public Vector<I> intersectingAny(
			Cursor<? extends Tuple<I>> masks,
			Equivalencer<I> equivalencer ) {

		Vector<Boolean> keeps = new Vector<>( );
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

		Vector<I> result = new Vector<>( );
		int j = 0;
		for ( Boolean keep : keeps ) {
			if ( keep ) {
				result.add( get( j ) );
			}
			j++;
		}
		return result;
	}

	/*
	 * returns only elements which survive the diff with each mask
	 */
	public Vector<I> intersectingAll(
			Cursor<? extends Tuple<I>> masks,
			Equivalencer<I> equivalencer ) {

		Vector<Boolean> keeps = new Vector<>( );
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

		Vector<I> result = new Vector<>( );
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

	public KnittingCursable<I> asCursable( ) {
		return KnittingCursable.wrap( h -> pull( ) );
	}

	public KnittingCursor<I> asCursor( ) {
		return KnittingCursor.wrap( pull( ) );
	}

	public Iterable<I> asIterable( ) {
		return ( ) -> asIterator( );
	}

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
	 * Returns a view hiding the first {@code hide} elements.
	 * </p>
	 * 
	 * <p>
	 * The returned view may be empty. This happens when this tuple's size is
	 * smaller than {@code hide}.
	 * </p>
	 * 
	 * @param hide
	 *          The number of elements to hide. A negative numbers counts as zero.
	 * @return A view hiding the first {@code hide} elements.
	 * @since 1.0
	 */
	public KnittingTuple<I> headless( int hide ) {
		int final_hide = hide < 0 ? 0 : hide;
		return wrap( new Subtuple<>( wrapped, final_hide, wrapped.size( ) ) );
	}

	/**
	 * <p>
	 * Returns a view of the first {@code show} elements visible after hiding the
	 * first {@code hide} elements in this tuple.
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
	 *
	 * @param hide
	 *          The number of elements to hide. A negative numbers counts as zero.
	 * @param show
	 *          The number of elements to show. A negative numbers counts as zero.
	 * @return A view of the first {@code show} elements visible after hiding the
	 *         first {@code hide} elements in this tuple.
	 * @since 1.0
	 */
	public KnittingTuple<I> head( int hide, int show ) {
		int final_show = show < 0 ? 0 : show;
		int final_hide = hide < 0 ? 0 : hide;
		return wrap( new Subtuple<>( wrapped, final_hide, final_show ) );
	}

	@Override
	public boolean equals( Object other ) {
		if ( other == this )
			return true;
		if ( !( other instanceof Tuple ) )
			return false;
		final int size = wrapped.size( );
		Tuple<?> o = (Tuple<?>) other;
		if ( size != o.size( ) ) {
			return false;
		}
		for ( int i = 0; i < size; i++ ) {
			I e = wrapped.get( i );
			Object oe = o.get( i );
			if ( !( e == null ? oe == null : e.equals( oe ) ) ) {
				return false;
			}
		}

		return true;
	}

	public boolean equivalentTo( Tuple<?> o, Equivalencer<I> equivalencer ) {
		if ( o == this )
			return true;
		final int size = wrapped.size( );
		if ( size != o.size( ) ) {
			return false;
		}
		for ( int i = 0; i < size; i++ ) {
			I e = wrapped.get( i );
			Object oe = o.get( i );
			if ( !( e == null ? oe == null : equivalencer.equivalent( e, oe ) ) ) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode( ) {
		int hashCode = 1;
		final int size = wrapped.size( );
		for ( int i = 0; i < size; i++ ) {
			I e = wrapped.get( i );
			hashCode = 31 * hashCode + ( e == null ? 0 : e.hashCode( ) );
		}
		return hashCode;
	}

	@Override
	public String toString( ) {
		StringBuilder sb = new StringBuilder( );
		sb.append( "[ " );
		final int size = wrapped.size( );
		for ( int i = 0; i < size; i++ ) {
			I e = wrapped.get( i );
			sb.append( e.toString( ) );
			sb.append( " " );
		}
		sb.append( "]" );
		return sb.toString( );
	}

	public <O> KnittingTuple<O> map( Function<? super I, O> function ) {
		return wrap( new Tuple<O>( ) {

			@Override
			public O get( int index ) {
				return function.apply( wrapped.get( index ) );
			}

			@Override
			public int size( ) {
				return wrapped.size( );
			}
		} );
	}

	private static <T> boolean equal_null( T first, T second ) {
		if ( first == null && second == null )
			return true;
		if ( first == null || second == null )
			return false;
		return first.equals( second );
	}

	public boolean startsWith( KnittingTuple<I> target ) {
		if ( size( ) < target.size( ) )
			return false;
		return head( 0, target.size( ) ).equals( target );
	}

	public boolean endsWith( KnittingTuple<I> target ) {
		if ( size( ) < target.size( ) )
			return false;
		return headless( size( ) - target.size( ) ).equals( target );
	}

	public KnittingTuple<I> chain( KnittingTuple<I> other ) {
		KnittingTuple<I> outer_this = this;
		Tuple<I> result = new Tuple<I>( ) {

			@Override
			public I get( int index ) {
				return index < outer_this.size( ) ? outer_this.get( index ) : other
						.get( index - outer_this.size( ) );
			}

			@Override
			public int size( ) {
				return outer_this.size( ) + other.size( );
			}
		};
		return wrap( result );
	}

	/*
	 * Returns the index within this tuple of the first occurrence of the
	 * specified tuple, starting at the specified index.
	 */
	public Optional<Integer> findSubtuple( KnittingTuple<I> target, int skip ) {
		int size = size( );
		if ( skip > size || skip < 0 ) {
			throw new IllegalArgumentException( );
		}
		int target_size = target.size( );
		if ( target_size == 0 ) {
			return Optional.of( skip );
		}
		if ( skip == size ) {
			return Optional.empty( );
		}
		int limit = size - target_size;
		I first = target.get( 0 );
		for ( int i = skip; i <= limit; i++ ) {
			while ( i <= limit && !equal_null( get( i ), first ) ) {
				i++;
			}
			if ( i <= limit ) {
				int j = i + 1;
				int end = i + target_size;
				int k = 1;
				while ( j < end && equal_null( get( j ), target.get( k ) ) ) {
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
	 * Returns a view of the last {@code show} elements visible after hiding the
	 * last {@code hide} elements in this tuple.
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
	 * @return A view of the last {@code show} elements visible after hiding the
	 *         last {@code hide} elements in this tuple.
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
	 * Returns a view hiding the last {@code hide} elements in this tuple.
	 * </p>
	 * 
	 * <p>
	 * The returned view may be empty. This happens when this tuple's size is
	 * smaller than {@code hide}.
	 * </p>
	 *
	 * @param hide
	 *          The number of elements to hide. A negative numbers counts as zero.
	 * @return A view hiding the last {@code hide} elements in this tuple.
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
}
