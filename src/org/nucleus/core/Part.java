/**
 * Copyright (c) 2012 scireum GmbH - Andreas Haufler - aha@scireum.de
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.nucleus.core;

import org.nucleus.Nucleus;

/**
 * Provides access to a part registered for a given type. The part is initially
 * fetched from the model and then cached locally.
 */
public class Part<P> {

	private P object;
	private Class<P> clazz;
	private boolean loaded;

	private Part(Class<P> clazz) {
		this.clazz = clazz;
	}

	/**
	 * Creates a new part which queries for the given class.
	 */
	public static <P> Part<P> of(Class<P> clazz) {
		return new Part<P>(clazz);
	}

	/**
	 * Returns the first object which was registered for the given class.
	 */
	public P get() {
		if (!loaded) {
			object = Nucleus.findPart(clazz);
			loaded = true;
		}
		return object;
	}
}
