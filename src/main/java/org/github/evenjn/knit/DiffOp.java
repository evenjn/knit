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
 * 
 * Adaptation of work by Neil Fraser at Google Inc.
 * 
 * Copyright 2006 Google Inc.
 *  
 * http://code.google.com/p/google-diff-match-patch/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.github.evenjn.knit;

/**
 * Class representing one diff operation.
 */
class DiffOp<F, B> {

	public enum Operation {
		DELETE,
		INSERT,
		EQUAL
	}

	private Operation operation;

	protected KnittingTuple<F> front;

	protected KnittingTuple<B> back;

	public Operation getOperation( ) {
		return operation;
	}

	public void turnDeleteToInsert( KnittingTuple<B> back ) {
		if ( operation == Operation.DELETE ) {
			this.front = null;
			this.back = back;
			this.operation = Operation.INSERT;
			return;
		}
		throw new IllegalStateException( );
	}

	public void turnInsertToDelete( KnittingTuple<F> front ) {
		if ( operation == Operation.INSERT ) {
			this.front = front;
			this.back = null;
			this.operation = Operation.DELETE;
			return;
		}
		throw new IllegalStateException( );
	}

	public void setEqualText( KnittingTuple<F> front,
			KnittingTuple<B> back ) {
		if ( operation != Operation.EQUAL ) {
			throw new IllegalStateException( );
		}
		this.front = front;
		this.back = back;
	}

	public void setDeletedText( KnittingTuple<F> text ) {
		if ( operation == Operation.DELETE ) {
			this.front = text;
			return;
		}
		throw new IllegalStateException( );
	}

	public void setInsertedText( KnittingTuple<B> text ) {
		if ( operation == Operation.INSERT ) {
			this.back = text;
			return;
		}
		throw new IllegalStateException( );
	}

	public KnittingTuple<F> getDeletedText( ) {
		if ( operation == Operation.DELETE ) {
			if ( front == null ) {
				throw new IllegalStateException( );
			}
			return front;
		}
		throw new IllegalStateException( );
	}

	public KnittingTuple<B> getInsertedText( ) {
		if ( operation == Operation.INSERT ) {
			if ( back == null ) {
				throw new IllegalStateException( );
			}
			return back;
		}
		throw new IllegalStateException( );
	}

	public KnittingTuple<B> getTextBack( ) {
		if ( operation == Operation.DELETE ) {
			throw new IllegalStateException( );
		}
		return back;
	}

	public KnittingTuple<F> getTextFront( ) {
		if ( operation == Operation.INSERT ) {
			throw new IllegalStateException( );
		}
		return front;
	}

	public int getEqualSize( ) {
		if ( operation != Operation.EQUAL ) {
			throw new IllegalStateException( );
		}
		return front.size( );
	}

	public static <T, Y> DiffOp<T, Y> insert( KnittingTuple<Y> text ) {
		return new DiffOp<T, Y>( Operation.INSERT, null, text );
	}

	public static <T, Y> DiffOp<T, Y> delete( KnittingTuple<T> text ) {
		return new DiffOp<T, Y>( Operation.DELETE, text, null );
	}

	public static <T, Y> DiffOp<T, Y> equal( KnittingTuple<T> text_front,
			KnittingTuple<Y> text_back ) {
		return new DiffOp<T, Y>( Operation.EQUAL, text_front, text_back );
	}

	protected DiffOp(Operation operation, KnittingTuple<F> text_front,
			KnittingTuple<B> text_back) {
		this.operation = operation;
		this.front = text_front;
		this.back = text_back;
	}
}
