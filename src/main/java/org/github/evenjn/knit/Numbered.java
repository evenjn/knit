package org.github.evenjn.knit;

/**
 * A {@code Numbered} object wraps an object together with an integer label
 * associated to it.
 *
 * @param <K>
 *          The type of wrapped element.
 * @since 1.0
 */
public class Numbered<K> {

	private final K wrapped;

	private final int number;

	/**
	 * <p>
	 * Constructor.
	 * </p>
	 * 
	 * @param object
	 *          The object to wrap.
	 * @param number
	 *          The label to associate with the object to wrap.
	 * @since 1.0
	 */
	public Numbered(K object, int number) {
		this.wrapped = object;
		this.number = number;
	}

	/**
	 * <p>
	 * Returns the wrapped object.
	 * </p>
	 * 
	 * @return The wrapped object.
	 * @since 1.0
	 */
	public K get( ) {
		return wrapped;
	}

	/**
	 * <p>
	 * Returns the integer label associated with the wrapped object.
	 * </p>
	 * 
	 * @return The integer label associated with the wrapped object.
	 * @since 1.0
	 */
	public int getNumber( ) {
		return number;
	}
}
