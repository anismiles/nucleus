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
package org.nucleus.incidents;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nucleus.core.InjectList;

/**
 * Central entity to handle all exceptions, errors and so on.
 */
public class Incidents {

	public static final Logger LOG = Logger
			.getLogger(Incidents.class.getName());

	public static class IncidentBuilder {

		private final String incidentName;
		private Map<String, String> fields = new LinkedHashMap<String, String>();
		private Throwable error;

		public IncidentBuilder(String incidentName) {
			this.incidentName = incidentName;
		}

		/**
		 * Adds the exception to the generated incident.
		 */
		public IncidentBuilder withException(Throwable t) {
			error = t;
			return this;
		}

		/**
		 * Add a parameter to the resulting message.
		 */
		public IncidentBuilder set(String param, Object value) {
			if (value != null) {
				fields.put(param, value.toString());
			}
			return this;
		}

		/**
		 * Broadcasts the incident and returns a localized error message.
		 */
		public BusinessException handle() {
			return internalHandle(createIncident());
		}

		protected Incident createIncident() {
			StringBuilder sb = new StringBuilder(incidentName);
			for (Map.Entry<String, String> field : fields.entrySet()) {
				sb.append(", " + field.getKey() + " = " + field.getValue());
			}
			if (error != null) {
				sb.append(", Exception: ");
				sb.append(error.getClass());
				sb.append(": ");
				sb.append(error.getMessage());
			}
			String origin = findOrigin(error);
			if (error == null) {
				error = new Exception();
			} else {
				LOG.severe(error.getMessage());
			}
			set("origin", origin);
			Incident i = new Incident(incidentName, sb.toString(), error,
					origin);
			return i;
		}

		protected static String findOrigin(Throwable t) {
			String origin = "-";
			if (t != null) {
				StackTraceElement[] stack = t.getStackTrace();
				if (stack.length > 0) {
					origin = stack[0].getFileName() + ":"
							+ stack[0].getLineNumber() + "["
							+ stack[0].getClassName() + "."
							+ stack[0].getMethodName() + "]";
				}
			} else {
				StackTraceElement[] stack = Thread.currentThread()
						.getStackTrace();
				if (stack.length > 5) {
					origin = stack[5].getFileName() + ":"
							+ stack[5].getLineNumber() + "["
							+ stack[5].getClassName() + "."
							+ stack[5].getMethodName() + "]";
				}
			}
			return origin;
		}

	}

	/**
	 * Creates an {@link IncidentBuilder} which is used to describe what
	 * happened and to build an appropriate error message. Calling
	 * {@link IncidentBuilder#handle()} will broadcast the generated incident
	 * and return a localized message.
	 */
	public static IncidentBuilder named(String incidentName) {
		return new IncidentBuilder(incidentName);
	}

	@InjectList(IncidentProcessor.class)
	private static List<IncidentProcessor> processors;

	/**
	 * This is used by handle for incidents as well as handle for exceptions,
	 * therefore the stack has always the same depth and the origin lookup is
	 * always the same.
	 */
	private static BusinessException internalHandle(Incident incident) {
		// Don't re-report incidents!
		if (incident.getException() != null
				&& (incident.getException() instanceof BusinessException)) {
			return (BusinessException) incident.getException();
		}
		LOG.severe(incident.toString());
		for (IncidentProcessor p : processors) {
			try {
				p.process(incident);
			} catch (Throwable e) {
				LOG.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		return new BusinessException(incident.getMessage(),
				incident.getException());
	}

	/**
	 * Handles the given exception.
	 */
	public static BusinessException handle(Throwable exception) {
		if (exception instanceof BusinessException) {
			return (BusinessException) exception;
		}

		if (exception instanceof SQLException) {
			return named("scireum.core.SQLException").withException(exception)
					.handle();
		} else {
			return named("scireum.core.UnexpectedException").withException(
					exception).handle();
		}
	}

}
