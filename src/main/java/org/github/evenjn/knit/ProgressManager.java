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

import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;

public class ProgressManager {

	public static Progress safeSpawn( Hook hook, ProgressSpawner master,
			String name ) {
		if ( master == null ) {
			return DummyProgress.singleton;
		}
		Progress spawn = master.spawn( hook, name );
		return spawn;
	}

}

class DummyProgress implements
		Progress {

	final static DummyProgress singleton = new DummyProgress( );

	private DummyProgress() {
	}

	@Override
	public void step( int distance ) {
	}

	@Override
	public Progress target( int target ) {
		return this;
	}

	@Override
	public Progress info( String info ) {
		return this;
	}

	@Override
	public Progress spawn( Hook hook, String name ) {
		return this;
	}

}
