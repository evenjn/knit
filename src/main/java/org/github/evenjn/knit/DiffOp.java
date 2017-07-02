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

import org.github.evenjn.knit.DiffPatch.Operation;

/**
 * Class representing one diff operation.
 */
public class DiffOp<T,Y> {
	
  /**
   * One of: INSERT, DELETE or EQUAL.
   */
  public Operation operation;
  /**
   * The text associated with this diff operation.
   */

  protected KnittingTuple<T> text_front;
  protected KnittingTuple<Y> text_back;
  
  public void turnDeleteToInsert(KnittingTuple<Y> text) {
  	if (operation == Operation.DELETE) {
  		this.text_front = null;
  		this.text_back = text;
  		this.operation = Operation.INSERT;
  		return;
  	}
		throw new IllegalStateException( );
  }
  
  public void turnInsertToDelete(KnittingTuple<T> text) {
  	if (operation == Operation.INSERT) {
  		this.text_front = text;
  		this.text_back = null;
  		this.operation = Operation.DELETE;
  		return;
  	}
		throw new IllegalStateException( );
  }
  

  public void setEqualText(KnittingTuple<T> text_front, KnittingTuple<Y> text_back) {
  	if (operation != Operation.EQUAL) {
  		throw new IllegalStateException( );
  	}
		this.text_front = text_front;
		this.text_back = text_back;
  }
  
  public void setDeletedText(KnittingTuple<T> text) {
  	if (operation == Operation.DELETE) {
  		this.text_front = text;
  		return;
  	}
		throw new IllegalStateException( );
  }
  
  public void setInsertedText(KnittingTuple<Y> text) {
  	if (operation == Operation.INSERT) {
    	this.text_back = text;
  		return;
  	}
		throw new IllegalStateException( );
  }
  
  public KnittingTuple<T> getDeletedText() {
  	if (operation == Operation.DELETE) {
  		if (text_front == null) {
    		throw new IllegalStateException( );
    	}
  		return text_front;
  	}
		throw new IllegalStateException( );
  }

  
  public KnittingTuple<Y> getInsertedText() {
  	if (operation == Operation.INSERT) {
  		if (text_back == null) {
    		throw new IllegalStateException( );
    	}
  		return text_back;
  	}
		throw new IllegalStateException( );
  }
  
  public KnittingTuple<Y> getTextBack() {
  	if (operation == Operation.DELETE) {
    		throw new IllegalStateException( );
  	}
  	return text_back;
  }
  

  public KnittingTuple<T> getTextFront() {
  	if (operation == Operation.INSERT) {
    		throw new IllegalStateException( );
  	}
  	return text_front;
  }
  
  public int getEqualSize() {
  	if (operation != Operation.EQUAL) {
  		throw new IllegalStateException( );
  	}
  	return text_front.size( );
  }
	
	static <T,Y> DiffOp<T,Y> insert(KnittingTuple<Y> text) {
		return new DiffOp<T,Y>(Operation.INSERT, null, text );
	}
	static <T,Y> DiffOp<T,Y> delete(KnittingTuple<T> text) {
		return new DiffOp<T,Y>(Operation.DELETE, text, null );
	}
	static <T,Y> DiffOp<T,Y> equal(KnittingTuple<T> text_front, KnittingTuple<Y> text_back) {
		return new DiffOp<T,Y>(Operation.EQUAL, text_front, text_back );
	}

  /**
   * Constructor.  Initializes the diff with the provided values.
   * @param operation One of INSERT, DELETE or EQUAL.
   * @param text The text being applied.
   */
  protected DiffOp(Operation operation, KnittingTuple<T> text_front, KnittingTuple<Y> text_back ) {
    // Construct a diff with the specified operation and text.
    this.operation = operation;
    this.text_front = text_front;
    this.text_back = text_back;
    if (operation == Operation.EQUAL) {
    	if (! text_front.equivalentTo( text_back ) ) {
    		throw new IllegalStateException( );
    	}
    }
  }

}