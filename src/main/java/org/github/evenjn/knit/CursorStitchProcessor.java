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

import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.CursorUnfoldH;
import org.github.evenjn.yarn.EndOfCursorException;
import org.github.evenjn.yarn.Hook;

class CursorStitchProcessor<I, O> implements
		Cursor<O> {

	private final Cursor<? extends I> cursor;

	private final CursorUnfoldH<? super I, O> knitting;

	private BasicAutoHook internal_hook;

	private final boolean use_internal_hook;

	CursorStitchProcessor(
			Cursor<I> cursor,
			CursorUnfoldH<? super I, O> knitting) {
		this.cursor = cursor;
		this.knitting = knitting;
		this.use_internal_hook = false;
	}

	CursorStitchProcessor(
			Hook hook,
			Cursor<I> cursor,
			CursorUnfoldH<? super I, O> knitting) {
		this.use_internal_hook = true;
		hook.hook( new AutoCloseable( ) {

			@Override
			public void close( ) {
				if ( internal_hook != null ) {
					internal_hook.close( );
				}
			}
		} );
		this.cursor = cursor;
		this.knitting = knitting;
	}

	private Cursor<O> current = null;

	private boolean end = false;

	@Override
	public O next( )
			throws EndOfCursorException {
		for ( ;; ) {
			if ( current != null ) {
				try {
					O next = current.next( );
					return next;
				}
				catch ( EndOfCursorException t ) {
					current = null;
				}
			}
			if ( end ) {
				if ( use_internal_hook && internal_hook != null ) {
					internal_hook.close( );
					internal_hook = null;
				}
				throw EndOfCursorException.neo();
			}

			try {
				I next = cursor.next( );
				if ( use_internal_hook ) {
					if (internal_hook != null) {
						internal_hook.close( );
						internal_hook = null;
					}
					internal_hook = new BasicAutoHook( );
				}
				current = knitting.next( internal_hook, next );
			}
			catch ( EndOfCursorException t ) {
				end = true;
				if ( use_internal_hook ) {
					if (internal_hook != null) {
						internal_hook.close( );
						internal_hook = null;
					}
					internal_hook = new BasicAutoHook( );
				}
				current = knitting.end( internal_hook );
			}
		}
	}

}
