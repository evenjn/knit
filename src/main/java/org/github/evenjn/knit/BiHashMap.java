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

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class BiHashMap<K1, K2, V> implements
		BiFunction<K1, K2, V> {

	protected Map<K1, Map<K2, V>> core = new HashMap<K1, Map<K2, V>>( );

	protected final V ifAbsent;

	public BiHashMap(V ifAbsent) {
		this.ifAbsent = ifAbsent;
	}

	public void map( K1 row, K2 col, V val ) {
		Map<K2, V> map = core.get( row );
		if ( map == null ) {
			map = new HashMap<K2, V>( );
			core.put( row, map );
		}
		map.put( col, val );
	}

	public V apply( K1 row, K2 col ) {
		Map<K2, V> map = core.get( row );
		if ( map == null ) {
			return ifAbsent;
		}
		V double1 = map.get( col );
		if ( double1 == null )
			return ifAbsent;
		return double1;
	}

}
