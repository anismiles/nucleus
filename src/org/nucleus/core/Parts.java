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

import java.util.List;

import org.nucleus.Nucleus;

/**
 * Returns all object which were registered for a given class.
 */
public class Parts<P> {

	private List<P> objects;
	private Class<P> clazz;

	private Parts(Class<P> clazz) {
		this.clazz = clazz;
	}

	/**
	 * Creates a new instance for the given class.
	 */
	public static <P> Parts<P> of(Class<P> clazz) {
		return new Parts<P>(clazz);
	}

	/**
	 * Returns a objects which were registered for the given class. This list is
	 * once fetched and kept in a local cache.
	 */
	public List<P> get() {
		if (objects == null) {
			objects = getUncached();
		}
		return objects;
	}

	/**
	 * Returns all objects without relying on the internal cache.
	 */
	public List<P> getUncached() {
		return Nucleus.findParts(clazz);
	}
}
