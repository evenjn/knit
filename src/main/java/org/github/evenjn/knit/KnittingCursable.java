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
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.github.evenjn.yarn.ArrayMap;
import org.github.evenjn.yarn.ArrayPurlFactory;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.CursableMap;
import org.github.evenjn.yarn.CursableMapH;
import org.github.evenjn.yarn.CursablePurlFactory;
import org.github.evenjn.yarn.CursablePurlHFactory;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.CursorMap;
import org.github.evenjn.yarn.CursorMapH;
import org.github.evenjn.yarn.CursorPurlFactory;
import org.github.evenjn.yarn.CursorPurlHFactory;
import org.github.evenjn.yarn.EndOfCursorException;
import org.github.evenjn.yarn.FunctionH;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.IterableMap;
import org.github.evenjn.yarn.IterableMapH;
import org.github.evenjn.yarn.IterablePurlFactory;
import org.github.evenjn.yarn.IterablePurlHFactory;
import org.github.evenjn.yarn.IteratorMap;
import org.github.evenjn.yarn.IteratorMapH;
import org.github.evenjn.yarn.IteratorPurlFactory;
import org.github.evenjn.yarn.IteratorPurlHFactory;
import org.github.evenjn.yarn.OptionMap;
import org.github.evenjn.yarn.OptionMapH;
import org.github.evenjn.yarn.OptionalPurlFactory;
import org.github.evenjn.yarn.OptionalPurlHFactory;
import org.github.evenjn.yarn.StreamMapH;
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
 * <h2>Methods of a KnittingCursor</h2>
 * 
 * <p>
 * Non-static public methods of {@code KnittingCursor} fall into one of the
 * following three categories:
 * </p>
 * 
 * <ul>
 * <li>Object methods (inherited from {@link java.lang.Object Object})</li>
 * <li>Terminal methods (listed below)</li>
 * <li>Transformation methods (listed below)</li>
 * </ul>
 *
 * <p>
 * Terminal methods repeatedly invoke the wrapped cursor's
 * {@link org.github.evenjn.yarn.Cursor#next() next()} until the end is reached.
 * The following methods are terminal:
 * </p>
 * <ul>
 * <li>{@link #collect(Collection)}</li>
 * <li>{@link #consume(Function)}</li>
 * <li>{@link #one()}</li>
 * <li>{@link #optionalOne()}</li>
 * <li>{@link #reduce(Object, BiFunction)}</li>
 * <li>{@link #roll()}</li>
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

	@Override
	public String toString() {
		return "A knitting cursable.";
	}

	/**
	 * Returns a view of the concatenation of the argument cursable after this cursable.
	 * 
	 * @param tail
	 *          The cursor to concatenate after this.
	 * @return A view of the concatenation of the argument cursable after this cursable.
	 * @since 1.0
	 */
	public KnittingCursable<I> append( final Cursable<? extends I> tail ) {
		KnittingCursable<I> outer_cursable = this;
		Cursable<I> result = new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return outer_cursable.pull( hook ).append( tail.pull( hook ) );
			}
		};
		return wrap( result );
	}

	public <K extends Collection<? super I>> K collect( K collection ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			return pull( hook ).collect( collection );
		}
	}

	public int roll( ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			return pull( hook ).roll( );
		}
	}

	public <K extends Consumer<? super I>> void consume(
			Function<Hook, K> hook_consumer ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			K consumer = hook_consumer.apply( hook );
			pull( hook ).peek( consumer ).roll( );
		}
	}

	public Iterator<I> asIterator( Hook hook ) {
		return pull( hook ).asIterator( );
	}

	public Stream<I> asStream( Hook hook ) {
		return pull( hook ).asStream( );
	}

	/*
	 * 
	 * @return a cursable that rolls over this and the other in parallel, each
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
	 * <p>
	 * Returns a view hiding the elements which do not satisfy the argument
	 * {@code stateless_predicate} in this cursable.
	 * </p>
	 * 
	 * @param stateless_predicate
	 *          A stateless system that decides to show or hide elements.
	 * @return A view hiding the elements which do not satisfy the argument
	 *         {@code stateless_predicate} in this cursable.
	 */
	public KnittingCursable<I>
			filter( Predicate<? super I> stateless_predicate ) {
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return new FilterCursor<>(wrapped.pull( hook ), stateless_predicate );
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
						hook, stitch );
			}
		} );
	}

	@Deprecated
	public <O> KnittingCursable<O> flatmapCursable(
			CursableMapH<? super I, O> stitch ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) ).flatmapCursable(
						hook, stitch );
			}
		} );
	}

	@Deprecated
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

	@Deprecated
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

	@Deprecated
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

	public <O> KnittingCursable<O> flatmapOptional(
			OptionMap<? super I, O> optional_map ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.flatmapOptional( optional_map );
			}
		} );
	}

	@Deprecated
	public <O> KnittingCursable<O> flatmapOptional(
			OptionMapH<? super I, O> optional_map_h )
			throws IllegalStateException {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				return KnittingCursor.wrap( wrapped.pull( hook ) )
						.flatmapOptional( hook, optional_map_h );
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

	/**
	 * <p>
	 * Returns a view showing the first {@code show} elements visible after hiding
	 * the first {@code hide} elements in this cursable.
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
	 * @param hide
	 *          The number of elements to hide. A negative numbers counts as zero.
	 * @param show
	 *          The number of elements to show. A negative numbers counts as zero.
	 * @return A view showing the first {@code show} elements visible after hiding
	 *         the first {@code hide} elements in this cursable.
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
	 * Returns a view of the elements visible after hiding the first {@code hide}
	 * elements in this cursable.
	 * </p>
	 * 
	 * <p>
	 * The returned view may be empty. This happens when this cursable's size is
	 * smaller than {@code hide}.
	 * </p>
	 * 
	 * @param hide
	 *          The number of elements to hide. A negative numbers counts as zero.
	 * @return A view hiding the first {@code hide} elements in this cursable.
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

	public KnittingCursable<Bi<I, Integer>> numbered( ) {
		return wrap( new Cursable<Bi<I, Integer>>( ) {

			@Override
			public Cursor<Bi<I, Integer>> pull( Hook hook ) {
				return new NumberedCursor<>( wrapped.pull( hook ) );
			}
		} );
	}

	/*
	 * returned object may be dead when cursor does not guarantee objects
	 * survive next() and/or the closing of hook. 
	 * throws IllegalStateException when it not the case that there is exactly one
	 * element.
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
		catch ( EndOfCursorException e ) {
			throw new IllegalStateException( );
		}
	}

	/*
	 * This is a terminal operation.
	 * 
	 * returns an empty optionsl when it not the case that there is exactly one
	 * element.
	 * returned object may be dead when cursor does not guarantee objects
	 * survive next() and/or the closing of hook.
	 */
	public Optional<I> optionalOne( ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			KnittingCursor<I> pulled = pull( hook );
			I result = pulled.next( );
			if ( pulled.hasNext( ) ) {
				return Optional.empty( );
			}
			return Optional.of( result );
		}
		catch ( EndOfCursorException e ) {
			return Optional.empty( );
		}
	}

	public KnittingCursable<I> peek( Consumer<? super I> consumer ) {
		return wrap( new Cursable<I>( ) {

			@Override
			public Cursor<I> pull( Hook hook ) {
				return new TapCursor<I>( wrapped.pull( hook ), consumer );
			}

		} );
	}

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
			catch ( EndOfCursorException e ) {
			}
			return reduction;
		}
	}

	public int size( ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			return pull( hook ).roll( );
		}
	}

	public KnittingCursable<KnittingCursor<I>> crop( Predicate<I> predicate ) {
		return KnittingCursable.wrap( h -> pull( h ).crop( predicate ) );
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
	 * @since 1.0
	 */
	public KnittingCursable<I> tail( int hide, int show) {
		int final_show = show < 0 ? 0 : show;
		int final_hide = hide < 0 ? 0 : hide;
		int len = size( ) - final_hide;
		if ( len > final_show ) {
			len = final_show;
		}
		int skip = size( ) - ( final_hide + len );
		if (skip < 0) {
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
	 * @since 1.0
	 */
	public KnittingCursable<I> tailless( int hide ) {
		int final_hide = hide < 0 ? 0 : hide;
		int len = size( ) - final_hide;
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

	public <O> KnittingCursable<O>
			purlCursor( CursorPurlFactory<? super I, O> factory ) {
		return wrap( new Cursable<O>( ) {

			@Override
			public Cursor<O> pull( Hook hook ) {
				Cursor<I> pull = wrapped.pull( hook );
				return KnittingCursor.wrap( pull ).purlCursor( factory.get( ) );
			}
		} );
	}

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
	
	public boolean contentEquals( Cursable<?> other ) {
		if ( other == this )
			return true;
		Cursable<?> o = (Cursable<?>) other;
		
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			KnittingCursor<I> pull1 = this.pull( hook );
			KnittingCursor<?> pull2 = KnittingCursor.wrap(o.pull( hook ));
			
			for (;;) {
				boolean hasNext1 = pull1.hasNext( );
				boolean hasNext2 = pull2.hasNext( );
				if (hasNext1 != hasNext2) {
					return false;
				}
				if (!hasNext1) {
					return true;
				}
				I next1 = pull1.next( );
				Object next2 = pull2.next( );
				if ( !( next1 == null ? next2 == null : next1.equals( next2 ) ) ) {
					return false;
				}
			}
		}
		catch ( EndOfCursorException e ) {
			throw new IllegalStateException( e );
		}
	}
}
