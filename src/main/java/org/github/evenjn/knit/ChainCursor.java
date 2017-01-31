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
import org.github.evenjn.yarn.PastTheEndException;

class ChainCursor<I> implements
		Cursor<I> {

	private final Cursor<I> head;
	private final Cursor<? extends I> tail;
	
	private boolean in_head = true;

	public ChainCursor(Cursor<I> head, Cursor<? extends I> tail) {
		this.head = head;
		this.tail = tail;
	}

	@Override
	public I next( )
			throws PastTheEndException {
		if ( in_head ) {
			try {
				return head.next( );
			}
			catch ( PastTheEndException e ) {
				in_head = false;
			}
		}
		return tail.next( );
	}

}
