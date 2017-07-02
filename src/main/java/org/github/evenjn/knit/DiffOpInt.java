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

public class DiffOpInt extends DiffOp<Integer,Integer>{

  	public static DiffOpInt insertFromText( String text) {
  		return new DiffOpInt(Operation.INSERT, null, DiffPatch.tt(text) );
  	}
  	public static DiffOp<Integer,Integer> deleteFromText( String text) {
  		return new DiffOpInt(Operation.DELETE, DiffPatch.tt(text), null );
  	}
  	public static DiffOp<Integer,Integer> equalFromText( String text) {
  		KnittingTuple<Integer> tt = DiffPatch.tt(text);
  		return new DiffOpInt(Operation.EQUAL, tt, tt );
  	}

		private KnittingTuple<Integer> the_text;
		
    private DiffOpInt(
    		Operation operation,
    		KnittingTuple<Integer> text_front,
    		KnittingTuple<Integer> text_back) {
    	super( operation, text_front, text_back );
      this.the_text = text_front != null ? text_front : ( KnittingTuple<Integer> ) text_back;
    }
    
    /**
     * Display a human-readable version of this Diff.
     * @return text version.
     */
    public String toString() {
      String prettyText = this.the_text.toString( );
      return "Diff(" + this.operation + ",\"" + prettyText + "\")";
    }

    /**
     * Create a numeric hash value for a Diff.
     * This function is not used by DMP.
     * @return Hash value.
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = (operation == null) ? 0 : operation.hashCode();
      result += prime * ((the_text == null) ? 0 : the_text.hashCode());
      return result;
    }

    /**
     * Is this Diff equivalent to another Diff?
     * @param obj Another Diff to compare against.
     * @return true or false.
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      DiffOp<?,?> other = (DiffOp<?,?>) obj;
      if (operation != other.operation) {
        return false;
      }

			if ( the_text == null && ( other.text_front != null || other.text_back != null )  ) {
				return false;
			}
			if ( the_text != null && ( other.text_front == null && other.text_back == null ) ) {
				return false;
			}

      int this_size = the_text.size( );
      KnittingTuple<?> other_tuple = null;
      
      if (other.operation == Operation.DELETE) {
      	other_tuple = other.getDeletedText( );
      }
      if (other.operation == Operation.INSERT) {
      	other_tuple = other.getInsertedText( );
      }
      if (other.operation == Operation.EQUAL) {
      	other_tuple = other.getTextFront( );
      }
      if (other_tuple == null) {
      	throw new IllegalStateException( );
      }
      
      int other_size = other_tuple.size( );
      if (this_size != other_size) {
      	return false;
      }
      
      for (int i = 0; i < this_size; i++) {
      	Integer t = this.the_text.get( i );
      	Object o = other_tuple.get( i );
      	if (t == null && o != null) {
      		return false;
      	}
      	if (t != null && o == null) {
      		return false;
      	}
      	if (t != null && o != null && !t.equals( o )) {
      		return false;
      	}
      }
      return true;
    }
  }