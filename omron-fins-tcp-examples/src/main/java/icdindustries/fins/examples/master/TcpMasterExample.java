package icdindustries.fins.examples.master;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mookins.omron.fins.FinsIoAddress;
import io.github.mookins.omron.fins.FinsMaster;
import io.github.mookins.omron.fins.FinsMasterException;
import io.github.mookins.omron.fins.FinsNodeAddress;
import io.github.mookins.omron.fins.master.FinsNettyTcpMaster;

public class TcpMasterExample {

	final static Logger logger = LoggerFactory.getLogger(TcpMasterExample.class);

	public static void main(String... args) throws InterruptedException {
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);
		FinsMaster client = new FinsNettyTcpMaster("localhost", 9600, new FinsNodeAddress(0, 0, 0));

		try {
			client.connect();
			logger.info("Connected");

			FinsNodeAddress destination = new FinsNodeAddress(0, 0, 0);
			FinsIoAddress address = FinsIoAddress.parseFrom(0x82271000);

			executor.scheduleAtFixedRate(() -> {
				try {logger.info("Going to start writing");
				Short item = (short) System.currentTimeMillis();
				client.writeWord(destination, address, item);
				logger.info(String.format("Thread 1 writing data: (0x%04x)", item));
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			} , 1, 1, TimeUnit.SECONDS);

			executor.scheduleAtFixedRate(() -> {
				Short item;
				try {
					item = client.readWord(destination, address);
					logger.info(String.format("Thread 2 reading data: (0x%04x)", item));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} , 100, 500, TimeUnit.MILLISECONDS);

			TimeUnit.SECONDS.sleep(60);

		} catch (FinsMasterException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.MINUTES);
			//client.disconnect();
		}
	}
}
