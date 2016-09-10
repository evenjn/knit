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

import java.util.Iterator;

import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.IteratorUnfoldH;
import org.github.evenjn.yarn.Itterator;
import org.github.evenjn.yarn.PastTheEndException;

class ItteratorStitchProcessor<I, O> implements
		Itterator<O> {

	private final Itterator<? extends I> itterator;

	private final IteratorUnfoldH<? super I, O> knitting;

	private BasicAutoHook internal_hook;

	private final boolean use_internal_hook;

	ItteratorStitchProcessor(
			Itterator<I> itterator,
			IteratorUnfoldH<? super I, O> knitting) {
		this.itterator = itterator;
		this.knitting = knitting;
		this.use_internal_hook = false;
	}

	ItteratorStitchProcessor(
			Hook hook,
			Itterator<I> itterator,
			IteratorUnfoldH<? super I, O> knitting) {
		this.use_internal_hook = true;
		hook.hook( new AutoCloseable( ) {

			@Override
			public void close( ) {
				if ( internal_hook != null ) {
					internal_hook.close( );
				}
			}
		} );
		this.itterator = itterator;
		this.knitting = knitting;
	}

	private Iterator<O> current = null;

	private boolean end = false;

	@Override
	public O next( )
			throws PastTheEndException {
		for ( ;; ) {
			if ( current != null ) {
				if (current.hasNext( ))
				{
					O next = current.next( );
					return next;
				}
				current = null;
			}
			if ( end ) {
				if ( use_internal_hook && internal_hook != null ) {
					internal_hook.close( );
					internal_hook = null;
				}
				throw PastTheEndException.neo;
			}

			try {
				I next = itterator.next( );
				if ( use_internal_hook ) {
					if (internal_hook != null) {
						internal_hook.close( );
						internal_hook = null;
					}
					internal_hook = new BasicAutoHook( );
				}
				current = knitting.next( internal_hook, next );
			}
			catch (PastTheEndException e) {
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
