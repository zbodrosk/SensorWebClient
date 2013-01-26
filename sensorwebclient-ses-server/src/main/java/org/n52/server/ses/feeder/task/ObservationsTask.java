
package org.n52.server.ses.feeder.task;

import java.util.List;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.n52.server.ses.feeder.util.DatabaseAccess;
import org.n52.shared.serializable.pojos.FeedingMetadata;
import org.n52.shared.serializable.pojos.TimeseriesFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles the collection of all necessary observations for every
 * registered sensor. It starts a thread for every sensor in the database.
 * 
 * @author Jan Schulte
 * 
 */
public class ObservationsTask extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationsTask.class);

    private ExecutorService executor = Executors.newFixedThreadPool(1);
    
    private boolean active;

    private Vector<String> currentlyFeededTimeseries;

    public ObservationsTask(Vector<String> currentlyFeededTimeseries) {
        this.currentlyFeededTimeseries = currentlyFeededTimeseries;
    }

    @Override
    public void run() {
    	/////// TODO remove this tests
//    	SosSesFeeder feeder = SosSesFeeder.getInst();
//    	FeedingMetadata feedingMetadata = new FeedingMetadata();
//    	feedingMetadata.setFeatureOfInterest("Konstanz_0906");
//    	feedingMetadata.setOffering("WASSERSTAND_ROHDATEN");
//    	feedingMetadata.setPhenomenon("Wasserstand");
//    	feedingMetadata.setProcedure("Wasserstand-Konstanz_0906");
//    	feedingMetadata.setServiceUrl("http://pegelonline.wsv.de/webservices/gis/gdi-sos");
//    	
//    	feeder.enableSensorForFeeding(feedingMetadata);
    	/////
    	
        LOGGER.info("Currenty feeded sensors: " + currentlyFeededTimeseries.size());
        active = true;
        try {
            LOGGER.debug("############## Prepare Observations task ################");
            List<TimeseriesFeed> timeseriesFeeds = DatabaseAccess.getUsedTimeseriesFeeds();
            LOGGER.debug("Number of Feeds: " + timeseriesFeeds.size());
            for (TimeseriesFeed timeseriesFeed : timeseriesFeeds) {
                if (shallFeed(timeseriesFeed)) {
                    FeedingMetadata metadata = timeseriesFeed.getFeedingMetadata();
                    if (!this.currentlyFeededTimeseries.contains(metadata)) {
                        FeedObservationThread obsThread = new FeedObservationThread(timeseriesFeed, currentlyFeededTimeseries);
                        if (!executor.isShutdown()) {
                            executor.execute(obsThread);
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Could not parse 'KEY_OBSERVATIONS_TASK_PERIOD'.", e);
        } catch (IllegalStateException e) {
            LOGGER.debug("Configuration is not available (anymore).", e);
        }
        finally {
        	active = false;
        }
    }

    boolean shallFeed(TimeseriesFeed timeseriesFeed) {
        boolean noUpdateYet = timeseriesFeed.getLastUpdate() == null;
        return noUpdateYet || isThresholdExceeded(timeseriesFeed);
    }

    boolean isThresholdExceeded(TimeseriesFeed timeseriesFeed) {
        long now = System.currentTimeMillis();
        long lastUpdate = timeseriesFeed.getLastUpdate().getTimeInMillis();
        return now - timeseriesFeed.getUpdateInterval() > lastUpdate;
    }
    
    public void stopObservationFeeds() {
        LOGGER.info("############## Stop Observations task ################");
        List<Runnable> threads = executor.shutdownNow();
        LOGGER.debug("Threads aktiv: " + threads.size());
        for (Runnable runnable : threads) {
            FeedObservationThread thread = (FeedObservationThread) runnable;
            if (thread != null) {
                thread.setRunning(false);
            }
        }
        while (!executor.isTerminated()) {
            LOGGER.debug("Wait while ObservationThreads are finished");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.error("Error during stop of observations threads.", e);
            }
        }
        LOGGER.info("############## Observation task stopped ##############.");
    }

    public boolean isActive() {
        return active;
    }

}