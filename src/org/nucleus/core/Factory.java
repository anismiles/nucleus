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

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

import org.nucleus.Nucleus;

/**
 * Used for processing dependency injection annotations like {@link Inject} and
 * {@link InjectList}.
 */
public class Factory {

	/**
	 * Fills all required injections.
	 */
	public static Object inject(Object obj) {
		if (obj != null) {
			inject(obj, obj.getClass());
		}
		return obj;
	}

	private static void inject(Object obj, Class<?> clazz) {
		if (Object.class.equals(clazz)) {
			return;
		}
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			if (field.isAnnotationPresent(Inject.class)) {
				injectIntoField(field, obj);
			}
			if (field.isAnnotationPresent(InjectList.class)) {
				injectListIntoField(field, obj);
			}
		}
		inject(obj, clazz.getSuperclass());
	}

	private static void injectListIntoField(Field field, Object obj) {
		if (List.class.isAssignableFrom(field.getType())) {
			try {
				field.set(obj, Nucleus.findParts(field.getAnnotation(
						InjectList.class).value()));
			} catch (Throwable e) {
				Nucleus.LOG.log(
						Level.WARNING,
						obj.getClass() + "." + field.getName() + ": "
								+ e.getMessage(), e);
			}
		} else {
			Nucleus.LOG
					.warning(obj.getClass()
							+ "."
							+ field.getName()
							+ ": @InjectList required a java.util.List<E> as field type");
		}
	}

	private static void injectIntoField(Field field, Object obj) {
		try {
			field.set(obj, Nucleus.findPart(field.getType()));
		} catch (Throwable e) {
			Nucleus.LOG.log(
					Level.WARNING,
					obj.getClass() + "." + field.getName() + ": "
							+ e.getMessage(), e);
		}
	}

	/**
	 * Creates a new instance of the given class. This should be used for
	 * classes which are NOT registered in {@link Nucleus} but which also wear
	 * {@link Inject} annotations.
	 */
	@SuppressWarnings("unchecked")
	public <I> I create(Class<I> type) {
		try {
			return (I) inject(type.newInstance());
		} catch (Throwable e) {
			throw new IllegalArgumentException(e);
		}
	}

}
