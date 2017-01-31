package org.github.evenjn.knit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HeadTailTest {

	@Test
	public void test( ) {
		KnittingCursable<Integer> t1 = KnittingCursable.on( 1, 2, 3, 4 );
		assertEquals( "head", true, KnittingCursable.on( 1, 2 )
				.contentEquals( KnittingCursable.on( 1, 2 ) ) );
		assertEquals( "head", true,
				t1.head( 0, 2 ).contentEquals( KnittingCursable.on( 1, 2 ) ) );
		assertEquals( "head", true,
				t1.head( 1, 3 ).contentEquals( KnittingCursable.on( 2, 3, 4 ) ) );
		assertEquals( "tail", true,
				t1.tail( 0, 2 ).contentEquals( KnittingCursable.on( 3, 4 ) ) );
		assertEquals( "tail", true,
				t1.tail( 1, 3 ).contentEquals( KnittingCursable.on( 1, 2, 3 ) ) );
		assertEquals( "headless", true,
				t1.headless( 2 ).contentEquals( KnittingCursable.on( 3, 4 ) ) );
		assertEquals( "tailless", true,
				t1.tailless( 2 ).contentEquals( KnittingCursable.on( 1, 2 ) ) );
	}
}
