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

import java.util.Collection;
import java.util.Optional;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.github.evenjn.yarn.ArrayMap;
import org.github.evenjn.yarn.ArrayUnfoldFactory;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.CursableMap;
import org.github.evenjn.yarn.CursableMapH;
import org.github.evenjn.yarn.CursableUnfoldFactory;
import org.github.evenjn.yarn.CursableUnfoldHFactory;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.CursorMap;
import org.github.evenjn.yarn.CursorMapH;
import org.github.evenjn.yarn.CursorUnfoldFactory;
import org.github.evenjn.yarn.CursorUnfoldHFactory;
import org.github.evenjn.yarn.Di;
import org.github.evenjn.yarn.FunctionH;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.IterableMap;
import org.github.evenjn.yarn.IterableMapH;
import org.github.evenjn.yarn.IterableUnfoldFactory;
import org.github.evenjn.yarn.IterableUnfoldHFactory;
import org.github.evenjn.yarn.IteratorMap;
import org.github.evenjn.yarn.IteratorMapH;
import org.github.evenjn.yarn.IteratorUnfoldFactory;
import org.github.evenjn.yarn.IteratorUnfoldHFactory;
import org.github.evenjn.yarn.PastTheEndException;
import org.github.evenjn.yarn.SkipFoldFactory;
import org.github.evenjn.yarn.SkipFoldHFactory;
import org.github.evenjn.yarn.SkipMap;
import org.github.evenjn.yarn.SkipMapH;
import org.github.evenjn.yarn.StreamMapH;
import org.github.evenjn.yarn.StreamUnfoldHFactory;
import org.github.evenjn.yarn.Tuple;

public class KnittingCursable<I> implements
		Cursable<I> {

	private final Cursable<I> wrapped;

	private KnittingCursable(Cursable<I> cursable) {
		this.wrapped = cursable;
	}

	/**
	 * @return the concatenation of this cursable an the argument cursable.
	 */
	public KnittingCursable<I> chain( final Cursable<I> other ) {
		KnittingCursable<I> outer_cursable = this;
		Cursable<I> result = new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return outer_cursable.pull( hook ).chain( other.pull( hook ) );
			}
		};
		return wrap( result );
	}

	public <K extends Collection<? super I>> K collect( K collection ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			return pull( hook ).collect( collection );
		}
	}

	public void consume( ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			pull( hook ).consume( );
		}
	}

	public <K extends Consumer<? super I>> void consume(
			Function<Hook, K> hook_consumer ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			K consumer = hook_consumer.apply( hook );
			pull( hook ).tap( consumer ).consume( );
		}
	}

	/**
	 * 
	 * @return a cursable that scrolls over this and the other in parallel, each
	 *         time applying the bifunction on the result of the two elements, and
	 *         returning in output the application result.
	 */
	public <R, M> KnittingCursable<M> entwine(
			Cursable<R> other,
			BiFunction<I, R, M> bifunction ) {
		KnittingCursable<I> outer = this;
		Cursable<M> result = new Cursable<M>( ) {

			@Override
			public Cursor<M> pull( Hook hook ) {
				return outer.pull( hook ).entwine( other.pull( hook ), bifunction );
			}
		};
		return wrap( result );
	}

	/**
	 * @param stateless_predicate
	 *          A stateless system that decides to keep or to discard elements.
	 * @return A cursable to access the only the elements of this cursable that
	 *         are not discarded by the predicate.
	 */
	public KnittingCursable<I>
			filter( Predicate<? super I> stateless_predicate ) {
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.filter( stateless_predicate );
			}
		} );
	}

	public <O> KnittingCursable<O> flatmapArray(
			ArrayMap<? super I, O> stitch ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.flatmapArray( stitch );
			}
		} );
	}

	public <O> KnittingCursable<O> flatmapCursable(
			CursableMap<? super I, O> stitch ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).flatmapCursable(
						stitch );
			}
		} );
	}

	public <O> KnittingCursable<O> flatmapCursable(
			CursableMapH<? super I, O> stitch ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).flatmapCursable(
						hook,
						stitch );
			}
		} );
	}

	public <O> KnittingCursable<O> flatmapCursor(
			CursorMap<? super I, O> stitch ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).flatmapCursor(
						stitch );
			}
		} );
	}

	public <O> KnittingCursable<O> flatmapCursor(
			CursorMapH<? super I, O> stitch ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).flatmapCursor( hook,
						stitch );
			}
		} );
	}

	public <O> KnittingCursable<O> flatmapIterable(
			IterableMap<? super I, O> stitch ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).flatmapIterable(
						stitch );
			}
		} );
	}

	public <O> KnittingCursable<O> flatmapIterable(
			IterableMapH<? super I, O> stitch ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).flatmapIterable(
						hook, stitch );
			}
		} );
	}

	public <O> KnittingCursable<O> flatmapIterator(
			IteratorMap<? super I, O> stitch ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).flatmapIterator(
						stitch );
			}
		} );
	}

	public <O> KnittingCursable<O> flatmapIterator(
			IteratorMapH<? super I, O> stitch ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).flatmapIterator(
						hook, stitch );
			}
		} );
	}

	public <O> KnittingCursable<O> flatmapStream(
			StreamMapH<? super I, O> stitch ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).flatmapStream( hook,
						stitch );
			}
		} );
	}

	@Deprecated
	public KnittingCursable<I> head( int limit ) {
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return Subcursor.sub( wrapped.pull( hook ), 0, limit );
			}
		} );
	}

	public KnittingCursable<I> head( int start, int limit ) {
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return Subcursor.sub( wrapped.pull( hook ), start, limit );
			}
		} );
	}

	public KnittingCursable<I> headless( int start ) {
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return Subcursor.skip( wrapped.pull( hook ), start );
			}
		} );
	}

	public <O> KnittingCursable<O>
			map( Function<? super I, O> stateless_function ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).map(
						stateless_function );
			}
		} );
	}

	public <O> KnittingCursable<O>
			map( FunctionH<? super I, O> stateless_function ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).map( hook,
						stateless_function );
			}
		} );
	}

	public KnittingCursable<Di<Integer, I>> numbered( ) {
		KnittingCursable<I> outer_this = this;
		return wrap( new Cursable<Di<Integer, I>>( ) {

			@Override
			public Cursor<Di<Integer, I>> pull( Hook hook ) {
				return outer_this.pull( hook ).numbered( );
			}
		} );
	}

	/**
	 * throws IllegalStateException when it not the case that there is exactly one
	 * element.
	 */
	public I one( Hook hook ) {
		try {
			KnittingCursor<I> pulled = pull( hook );
			I result = pulled.next( );
			if ( pulled.hasNext( ) ) {
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
	public Optional<I> optionalOne( Hook hook ) {
		try {
			KnittingCursor<I> pulled = pull( hook );
			I result = pulled.next( );
			if ( pulled.hasNext( ) ) {
				return Optional.empty( );
			}
			return Optional.of( result );
		}
		catch ( PastTheEndException e ) {
			return Optional.empty( );
		}
	}

	@Override
	public KnittingCursor<I> pull( Hook hook ) {
		return KnittingCursor.wrap( wrapped.pull( hook ) );
	}

	public <K> K reduce( K zero, BiFunction<K, I, K> fun ) {
		K reduction = zero;
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			KnittingCursor<I> kc = pull( hook );
			try {
				for ( ;; ) {
					reduction = fun.apply( reduction, kc.next( ) );
				}
			}
			catch ( PastTheEndException e ) {
			}
			return reduction;
		}
	}

	public int size( ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			return pull( hook ).size( );
		}
	}

	public <O> KnittingCursable<O>
			skipfold( SkipFoldFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).skipfold(
						factory.create( ) );
			}
		} );
	}

	public <O> KnittingCursable<O>
			skipfold( SkipFoldHFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).skipfold( hook,
						factory.create( ) );
			}
		} );
	}

	public <O> KnittingCursable<O> skipmap(
			SkipMap<? super I, O> skipmap ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).skipmap( skipmap );
			}
		} );
	}

	public <O> KnittingCursable<O> skipmap(
			SkipMapH<? super I, O> skipmap ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).skipmap( hook, skipmap );
			}
		} );
	}

	public KnittingCursable<KnittingCursor<I>> split( Predicate<I> predicate ) {
		return KnittingCursable.wrap( h -> pull( h ).split( predicate ) );
	}

	/**
	 * Use head(start, length)
	 */
	@Deprecated
	public KnittingCursable<I> sub( int start, int length ) {
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return Subcursor.sub( wrapped.pull( hook ), start, length );
			}
		} );
	}

	public KnittingCursable<I> tail( int length ) {
		KnittingCursable<I> outer = this;
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				int size = outer.size( );
				if ( length > size ) {
					return wrapped.pull( hook );
				}
				return Subcursor.skip( wrapped.pull( hook ), size - length );
			}
		} );
	}

	public KnittingCursable<I> tail( int skip, int length ) {
		KnittingCursable<I> outer = this;
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				int size = outer.size( );
				if ( length > size ) {
					return wrapped.pull( hook );
				}
				return Subcursor.sub( wrapped.pull( hook ), size - ( length + skip ),
						length );
			}
		} );
	}

	public KnittingCursable<I> tailless( int skip ) {
		KnittingCursable<I> outer = this;
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				int len = outer.size( ) - skip;
				if ( skip < 0 ) {
					len = 0;
				}
				return Subcursor.sub( wrapped.pull( hook ), 0, len );
			}
		} );
	}

	public KnittingCursable<I> tap( Consumer<? super I> consumer ) {
		KnittingCursable<I> outer_this = this;
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return outer_this.pull( hook ).tap( consumer );
			}

		} );
	}

	public <O> KnittingCursable<O>
			unfoldArray( ArrayUnfoldFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).unfoldArray( factory.create( ) );
			}
		} );
	}

	public <O> KnittingCursable<O>
			unfoldCursable( CursableUnfoldFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).unfoldCursable( factory.create( ) );
			}
		} );
	}

	public <O> KnittingCursable<O>
			unfoldCursable( CursableUnfoldHFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).unfoldCursable( hook,
						factory.create( ) );
			}
		} );
	}

	public <O> KnittingCursable<O>
			unfoldCursor( CursorUnfoldFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).unfoldCursor( factory.create( ) );
			}
		} );
	}

	public <O> KnittingCursable<O>
			unfoldCursor( CursorUnfoldHFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull )
						.unfoldCursor( hook, factory.create( ) );
			}
		} );
	}

	public <O> KnittingCursable<O>
			unfoldIterable( IterableUnfoldFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).unfoldIterable( factory.create( ) );
			}
		} );
	}

	public <O> KnittingCursable<O>
			unfoldIterable( IterableUnfoldHFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).unfoldIterable( hook,
						factory.create( ) );
			}
		} );
	}

	public <O> KnittingCursable<O>
			unfoldIterator( IteratorUnfoldFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).unfoldIterator( factory.create( ) );
			}
		} );
	}

	public <O> KnittingCursable<O>
			unfoldIterator( IteratorUnfoldHFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).unfoldIterator( hook,
						factory.create( ) );
			}
		} );
	}

	public <O> KnittingCursable<O>
			unfoldStream( StreamUnfoldHFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).unfoldStream(
						hook, factory.create( ) );
			}
		} );
	}

	@SuppressWarnings("unchecked")
	public static <K> KnittingCursable<K> empty( ) {
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
	 * Stops as soon as one of the sources is depleted.
	 * 
	 * @return an iterator that pulls the next element from the i-th iterator,
	 *         where i is the next integer pulled by selector.
	 */
	public static <T> KnittingCursable<T> blend( Cursable<Integer> selector,
			final Tuple<Cursable<T>> sources ) {
		return wrap( new Cursable<T>( ) {

			@Override
			public Cursor<T> pull( Hook hook ) {
				Cursor<Integer> selector_pulled = selector.pull( hook );
				Vector<Cursor<T>> sources_vector = new Vector<>( );
				int size = sources.size( );
				for ( int i = 0; i < size; i++ ) {
					sources_vector.add( sources.get( i ).pull( hook ) );
				}

				Cursor<T> result = new Cursor<T>( ) {

					@Override
					public T next( )
							throws PastTheEndException {
						Integer index = selector_pulled.next( );
						Cursor<T> iterator = sources_vector.get( index );
						return iterator.next( );
					}
				};
				return result;
			}
		} );
	}

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

	public static <K> KnittingCursable<K> wrap( Cursable<K> cursable ) {
		if ( cursable instanceof KnittingCursable ) {
			return (KnittingCursable<K>) cursable;
		}
		return new KnittingCursable<K>( cursable );
	}

	public static <K> KnittingCursable<K> wrap( Iterable<K> iterable ) {
		return wrap( new IterableCursable<K>( iterable ) );
	}

	public static <K> KnittingCursable<K> wrap( K[] array ) {
		return wrap( new ArrayItterable<K>( array ) );
	}
}
