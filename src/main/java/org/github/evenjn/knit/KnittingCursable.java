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
import org.github.evenjn.yarn.CursableMaph;
import org.github.evenjn.yarn.CursableUnfoldFactory;
import org.github.evenjn.yarn.CursableUnfoldHFactory;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.CursorMap;
import org.github.evenjn.yarn.CursorMaph;
import org.github.evenjn.yarn.CursorUnfoldFactory;
import org.github.evenjn.yarn.CursorUnfoldHFactory;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.IterableMap;
import org.github.evenjn.yarn.IterableMaph;
import org.github.evenjn.yarn.IterableUnfoldFactory;
import org.github.evenjn.yarn.IterableUnfoldHFactory;
import org.github.evenjn.yarn.IteratorMap;
import org.github.evenjn.yarn.IteratorMaph;
import org.github.evenjn.yarn.IteratorUnfoldFactory;
import org.github.evenjn.yarn.IteratorUnfoldHFactory;
import org.github.evenjn.yarn.PastTheEndException;
import org.github.evenjn.yarn.SkipFoldFactory;
import org.github.evenjn.yarn.SkipFoldHFactory;
import org.github.evenjn.yarn.SkipMap;
import org.github.evenjn.yarn.StreamMapH;
import org.github.evenjn.yarn.StreamUnfoldHFactory;
import org.github.evenjn.yarn.Tuple;

public class KnittingCursable<I> implements
		Cursable<I> {

	private final Cursable<I> wrapped;

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
		return wrap( new ArrayCursable<K>( array ) );
	}

	private KnittingCursable(Cursable<I> cursable) {
		this.wrapped = cursable;
	}

	/**
	 * Stops as soon as one of the sources is depleted.
	 * 
	 * @return an iterator that pulls the next element from the i-th iterator,
	 *         where i is the next integer pulled by selector.
	 */
	public static <T> Cursable<T> blend( Cursor<Integer> selector,
			final Tuple<Cursable<T>> sources ) {
		return new Cursable<T>( ) {

			@Override
			public Cursor<T> pull( Hook hook ) {
				Cursor<Integer> selector_pulled = selector;
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
		};
	}

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

	public <R, M> KnittingCursable<M> entwine(
			Cursable<R> right,
			BiFunction<I, R, M> bifunction ) {
		Cursable<I> outer_cursable = this;
		Cursable<M> result = new Cursable<M>( ) {

			@Override
			public Cursor<M> pull( Hook hook ) {
				Cursor<I> left_cursor = outer_cursable.pull( hook );
				Cursor<R> right_cursor = right.pull( hook );
				Cursor<M> comapd = new Cursor<M>( ) {

					@Override
					public M next( )
							throws PastTheEndException {
						return bifunction.apply( left_cursor.next( ), right_cursor.next( ) );
					}
				};
				return comapd;
			}
		};
		return wrap( result );
	}

	public <K extends Consumer<I>> K consume( K consumer ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			return pull( hook ).consume( consumer );
		}
	}


  public void consume( ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			pull( hook ).consume( );
		}
	}
  
	/**
	 * @param stateless_predicate
	 *          A stateless system that decides to keep or to discard elements.
	 * @return A cursable to access the only the elements of this cursable that
	 *         are not discarded by the predicate.
	 */
	public KnittingCursable<I> filter( Predicate<? super I> stateless_predicate ) {
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
			CursableMaph<? super I, O> stitch ) {
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
			CursorMaph<? super I, O> stitch ) {
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
			IterableMaph<? super I, O> stitch ) {
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
			IteratorMaph<? super I, O> stitch ) {
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

	public KnittingCursable<I> sub( int start, int length ) {
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return Subcursor.sub( wrapped.pull( hook ), start, length );
			}
		} );
	}

	public KnittingCursable<I> headless( int start ) {
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return Subcursor.headless( wrapped.pull( hook ), start );
			}
		} );
	}

	public KnittingCursable<I> head( int limit ) {
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return Subcursor.head( wrapped.pull( hook ), limit );
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
			unfoldStream( StreamUnfoldHFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).unfoldStream(
						hook, factory.create( ) );
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

	public <K extends Collection<I>> K collect( K collection ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			KnittingCursor<I> kc = pull( hook );
			try {
				for ( ;; ) {
					collection.add( kc.next( ) );
				}
			}
			catch ( PastTheEndException e ) {
			}
			return collection;
		}
	}


	/**
	 * throws IllegalStateException when it it not the case that there is exactly one element.
	 */
	public I one( ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
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

	public int size( ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			return pull( hook ).size( );
		}
	}

	public Iterable<I> once( Hook hook ) {
		return pull( hook ).once( );
	}

	@SuppressWarnings("unchecked")
	public static <K> KnittingCursable<K> empty( ) {
		return (KnittingCursable<K>) neo;
	}

	private static final KnittingCursable<Void> neo = wrap(new Cursable<Void>( ) {

		@Override
		public Cursor<Void> pull( Hook hook ) {
			return KnittingCursor.empty( );
		}
	});
}
