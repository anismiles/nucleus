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
package org.nucleus.timer;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import org.nucleus.Nucleus;
import org.nucleus.core.InjectList;
import org.nucleus.core.Register;
import org.nucleus.incidents.Incidents;

/**
 * Internal service which is responsible for executing timers.
 */
@Register(classes = { TimerInfo.class })
public class TimerService implements TimerInfo {

	@InjectList(EveryMinute.class)
	private List<EveryMinute> everyMinute;
	private long lastOneMinuteExecution = 0;

	private Timer timer;
	private ReentrantLock timerLock = new ReentrantLock();

	public TimerService() {
		start();
	}

	public void start() {
		try {
			timerLock.lock();
			try {
				if (timer == null) {
					timer = new Timer(true);
				} else {
					timer.cancel();
					timer = new Timer(true);
				}
				timer.schedule(new InnerTimerTask(), 1000 * 60, 1000 * 60);
			} finally {
				timerLock.unlock();
			}
		} catch (Throwable t) {
			Nucleus.LOG.log(Level.WARNING, t.getMessage(), t);
		}
	}

	public void stop() {
		try {
			timerLock.lock();
			try {
				if (timer != null) {
					timer.cancel();
				}
			} finally {
				timerLock.unlock();
			}
		} catch (Throwable t) {
			Nucleus.LOG.log(Level.WARNING, t.getMessage(), t);
		}
	}

	private class InnerTimerTask extends TimerTask {

		@Override
		public void run() {
			for (EveryMinute task : everyMinute) {
				try {
					task.runTimer();
				} catch (Exception e) {
					Incidents.named("EveryMinuteTaskFailed")
							.set("class", task.getClass().getName()).handle();
				}
			}
			lastOneMinuteExecution = System.currentTimeMillis();
		}

	}

	@Override
	public String getLastOneMinuteExecution() {
		if (lastOneMinuteExecution == 0) {
			return "-";
		}
		return DateFormat.getDateTimeInstance().format(
				new Date(lastOneMinuteExecution));
	}
}
