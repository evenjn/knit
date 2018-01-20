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

import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;

class DiffPairImpl<F, B>  implements
		DiffPair<F, B> {

	private boolean has_front = false;

	private boolean has_back = false;

	/**
	 * Returns {@code true} if the both slots are filled in, {@code false}
	 * otherwise.
	 * 
	 * @return {@code true} if the both slots are filled in, {@code false}
	 *         otherwise.
	 */
	public boolean hasBoth( ) {
		return has_front && has_back;
	}

	/**
	 * Returns {@code true} if the front slot is filled in, {@code false}
	 * otherwise.
	 * 
	 * @return {@code true} if the front slot is filled in, {@code false}
	 *         otherwise.
	 */
	public boolean hasFront( ) {
		return has_front;
	}

	/**
	 * Returns {@code true} if the back slot is filled in, {@code false}
	 * otherwise.
	 * 
	 * @return {@code true} if the back slot is filled in, {@code false}
	 *         otherwise.
	 */
	public boolean hasBack( ) {
		return has_back;
	}

	/**
	 * @return The object in the <em>front</em> slot.
	 * @since 1.0
	 */
	public F front( ) {
		return front;
	}

	/**
	 * @return The object in the <em>back</em> slot.
	 * @since 1.0
	 */
	public B back( ) {
		return back;
	}

	protected DiffPairImpl(F front, B back, boolean has_front,
			boolean has_back) {
		this.front = front;
		this.back = back;
		if ( !has_back && !has_front ) {
			throw new IllegalArgumentException( );
		}
		this.has_back = has_back;
		this.has_front = has_front;
	}

	static <F, B> DiffPairImpl<F, B> nu( F front, B back,
			boolean has_front,
			boolean has_back ) {
		return new DiffPairImpl<F, B>( front, back, has_front, has_back );
	}

	private F front;

	private B back;
}

class DiffIterator<F, B> implements
		Cursor<DiffPair<F, B>> {

	private final KnittingCursor<DiffOp<F, B>> kd;

	private final KnittingCursor<F> kc_front;

	private final KnittingCursor<B> kc_back;

	DiffIterator(
			KnittingCursor<F> kc_front,
			KnittingCursor<B> kc_back,
			KnittingCursor<DiffOp<F, B>> kd) {
		this.kc_front = kc_front;
		this.kc_back = kc_back;
		this.kd = kd;
	}

	private DiffOp<F, B> current = null;

	private DiffPairImpl<F, B> tray;

	private int original_start;

	private int original_length;

	private int revised_start;

	private int revised_length;

	@Override
	public DiffPair<F, B> next( )
			throws EndOfCursorException {

		if ( current == null && kd.hasNext( ) ) {
			current = kd.next( );
			switch ( current.getOperation( ) ) {
				case INSERT:
					revised_length = current.getTextBack( ).size( );
					break;
				case EQUAL:
					original_length = current.getTextFront( ).size( );
					revised_length = current.getTextBack( ).size( );
					break;
				case DELETE:
					original_length = current.getTextFront( ).size( );
					break;
				default:
					break;
			}
		}

		if ( current != null ) {
			switch ( current.getOperation( ) ) {
				case INSERT:
					tray = DiffPairImpl.nu( null, kc_back.next( ), false, true );
					revised_length--;
					if ( revised_length == 0 ) {
						current = null;
					}
					revised_start++;
					break;
				case EQUAL:
					tray = DiffPairImpl.nu( kc_front.next( ), kc_back.next( ), true,
							true );
					original_length--;
					if ( original_length == 0 ) {
						current = null;
					}
					revised_length--;
					if ( revised_length == 0 ) {
						current = null;
					}
					original_start++;
					revised_start++;
					break;
				case DELETE:
					tray = DiffPairImpl.nu( kc_front.next( ), null, true, false );
					original_length--;
					if ( original_length == 0 ) {
						current = null;
					}
					original_start++;
					break;
				default:
					break;
			}
			return tray;

		}
		throw EndOfCursorException.neo( );
	}
}
