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

import org.github.evenjn.lang.BiOptional;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;

class DiffIterator<F, B> implements
		Cursor<BiOptional<F, B>> {

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

	private BiOptionalTray<F, B> tray;

	private int original_start;

	private int original_length;

	private int revised_start;

	private int revised_length;

	@Override
	public BiOptional<F, B> next( )
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
					tray = BiOptionalTray.nu( null, kc_back.next( ), false, true );
					revised_length--;
					if ( revised_length == 0 ) {
						current = null;
					}
					revised_start++;
					break;
				case EQUAL:
					tray = BiOptionalTray.nu( kc_front.next( ), kc_back.next( ), true, true );
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
					tray = BiOptionalTray.nu( kc_front.next( ), null, true, false );
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
