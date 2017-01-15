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

import java.util.Collections;
import java.util.LinkedList;

import org.github.evenjn.yarn.AutoHook;

/**
 * An implementation of AutoHook that returns to the initial state after
 * invoking {@code close()}. In other words, a closed ReusableAutoHook is as
 * good as a brand new one, and may be immediately re-used.
 *
 * @since 1.0
 */
public final class ReusableAutoHook implements
		AutoHook {

	private LinkedList<AutoCloseable> objects_to_close;

	@Override
	public void close( ) {
		if ( objects_to_close != null ) {
			Collections.reverse( objects_to_close );
			for ( AutoCloseable ac : objects_to_close ) {
				try {
					ac.close( );
				}
				catch ( Exception e ) {
					throw new IllegalStateException( e );
				}
			}
			objects_to_close = null;
		}
	}

	@Override
	public <T extends AutoCloseable> T hook( T auto_closeable ) {
		if ( objects_to_close == null ) {
			objects_to_close = new LinkedList<AutoCloseable>( );
		}
		objects_to_close.add( auto_closeable );
		return auto_closeable;
	}
}
