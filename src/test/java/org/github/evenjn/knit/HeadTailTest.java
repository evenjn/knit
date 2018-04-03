package org.github.evenjn.knit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HeadTailTest {

	@Test
	public void test( ) {
		KnittingCursable<Integer> t1 = KnittingCursable.on( 1, 2, 3, 4 );
		assertEquals( "head", true, KnittingCursable.on( 1, 2 )
				.equivalentTo( KnittingCursable.on( 1, 2 ), Integer::equals ) );
		assertEquals( "head", true,
				t1.headless( 0 ).head( 2 ).equivalentTo( KnittingCursable.on( 1, 2 ),
						Integer::equals ) );
		assertEquals( "head", true,
				t1.headless( 1 ).head( 3 ).equivalentTo( KnittingCursable.on( 2, 3, 4 ),
						Integer::equals ) );
		// assertEquals( "tail", true,
		// t1.tail( 0, 2 ).equivalentTo( KnittingCursable.on( 3, 4 ),
		// Integer::equals ) );
		// assertEquals( "tail", true,
		// t1.tail( 1, 3 ).equivalentTo( KnittingCursable.on( 1, 2, 3 ),
		// Integer::equals ) );
		assertEquals( "headless", true,
				t1.headless( 2 ).equivalentTo( KnittingCursable.on( 3, 4 ),
						Integer::equals ) );
		// assertEquals( "tailless", true,
		// t1.tailless( 2 ).equivalentTo( KnittingCursable.on( 1, 2 ),
		// Integer::equals ) );
	}
}
