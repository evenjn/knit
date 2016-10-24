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

import java.util.LinkedList;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.knit.DiffPatch.Diff;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.PastTheEndException;
import org.github.evenjn.yarn.Tuple;

public class KnittingTuple<I> implements
		Tuple<I>,
		Cursable<I> {

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

	public KnittingItterable<Bi<I, I>> diff( Tuple<I> other ) {
		return KnittingItterable.wrap( ( ) -> new DiffIterator<I>( this, other ) );
	}

	/**
	 * Computes Damerau-Levenshtein distance.
	 */
	public int distance( Tuple<?> other ) {
		DiffPatch dmp = new DiffPatch( );
		LinkedList<Diff> diffs =
				dmp.diff_main( map( x -> x ), KnittingTuple.wrap( other ).map( x -> x ) );
		return dmp.diff_levenshtein( diffs );
	}

	public <K extends Consumer<I>> K consume( K consumer ) {
		return pull( ).consume( consumer );
	}

	@Override
	public I get( int index ) {
		return wrapped.get( index );
	}

	@Override
	public int size( ) {
		return wrapped.size( );
	}

	public KnittingCursor<I> pull( ) {
		return KnittingCursor.wrap( new Cursor<I>( ) {

			final int size = wrapped.size( );

			int i = 0;

			@Override
			public I next( )
					throws PastTheEndException {
				if ( i >= size )
					throw PastTheEndException.neo;
				return wrapped.get( i++ );
			}
		} );
	}

	@Override
	public KnittingCursor<I> pull( Hook hook ) {
		return pull( );
	}

	public Optional<Integer> findElement( Object object ) {
		return findElement( object, 0 );
	}

	public Optional<Integer> findElement( Object object, int skip ) {
		final int size = size( );
		for ( int i = skip; i < size; i++ ) {
			I e = wrapped.get( i );
			if ( e == null ? object == null : object.equals( e ) ) {
				return Optional.of( i );
			}
		}
		return Optional.empty( );
	}

	/*
	 * Formally computes whether the object is in the image. Bear in mind
	 * that there may be mulitiple elements in the domain of this function that
	 * are mapped to the argument object.
	 */
	public boolean contains( Object object ) {
		final int size = size( );
		for ( int i = 0; i < size; i++ ) {
			I e = wrapped.get( i );
			if ( e == null ? object == null : object.equals( e ) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Use head(start, length). 
	 */
	@Deprecated
	public KnittingTuple<I> sub( final int start, final int length ) {
		return wrap( new Subtuple<>( wrapped, start, length ) );
	}

	public KnittingTuple<I> headless( int start ) {
		return wrap( new Subtuple<>( wrapped, start, wrapped.size( ) ) );
	}

	public KnittingTuple<I> head( int limit ) {
		return wrap( new Subtuple<>( wrapped, 0, limit ) );
	}

	public KnittingTuple<I> head( int skip, int limit ) {
		return wrap( new Subtuple<>( wrapped, skip, limit ) );
	}

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

	public int hashCode( ) {
		int hashCode = 1;
		final int size = wrapped.size( );
		for ( int i = 0; i < size; i++ ) {
			I e = wrapped.get( i );
			hashCode = 31 * hashCode + ( e == null ? 0 : e.hashCode( ) );
		}
		return hashCode;
	}

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

	KnittingTuple<? extends I> blurType( ) {
		return this;
	}

	public Iterable<I> once( ) {
		return pull( ).once( );
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

	/**
	 * Returns the index within this tuple of the first occurrence of the
	 * specified tuple.
	 */
	public Optional<Integer> findSubtuple( KnittingTuple<I> target ) {
		return findSubtuple( target, 0 );
	}

	public boolean startsWith( KnittingTuple<I> target ) {
		if ( size( ) < target.size( ) )
			return false;
		return head( target.size( ) ).equals( target );
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

	/**
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
			return Optional.of(skip);
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
					return Optional.of(i);
				}
			}
		}
		return Optional.empty( );
	}

	@SuppressWarnings("unchecked")
	public static <K> KnittingTuple<K> empty( ) {
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
