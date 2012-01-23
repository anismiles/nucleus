package examples;

import java.text.DateFormat;
import java.util.Date;

import org.nucleus.Nucleus;
import org.nucleus.core.Register;
import org.nucleus.timer.EveryMinute;


@Register(classes = EveryMinute.class)
public class ExampleNucleus implements EveryMinute {

	public static void main(String[] args) throws Exception {
		Nucleus.init();
		while (true) {
			Thread.sleep(1000);
		}
	}

	@Override
	public void runTimer() throws Exception {
		System.out.println(DateFormat.getTimeInstance().format(new Date()));
	}

}
