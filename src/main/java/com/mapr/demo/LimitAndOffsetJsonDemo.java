package com.mapr.demo;

import com.mapr.utils.Constants;
import com.mapr.utils.GeneralUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.Connection;
import org.ojai.store.DocumentStore;
import org.ojai.store.Driver;
import org.ojai.store.DriverManager;
import org.ojai.store.Query;
import org.ojai.store.QueryCondition;
import org.ojai.store.SortOrder;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by aravi on 10/16/17.
 */
public class LimitAndOffsetJsonDemo {
    public static final Logger logger = Logger.getLogger(GeneralUtils.getInvokingClassName());

    public static void main( String[] args ) {
        AtomicInteger counter = new AtomicInteger(0);
        StopWatch stopWatch = new StopWatch();
        DocumentStream stream = null;
        try {
            /**
             * Driver can be considered as an entry point to Ojai API.
             * Driver can be used to build QueryConditions / Queries / Mutations etc.
             */
            Driver driver = DriverManager.getDriver(Constants.CONNECTION_URL);

            //We now build a query object by specifying the fields to be projected
            String queryJSON = "{\"$select\":[\"name\",\"address\",\"review_count\"]," +
                    "\"$where\":{\"$and\":[{\"$gt\":{\"stars\":3.0}},{\"$eq\":{\"state\":\"NV\"}}]}," +
                    "\"$orderby\":[{\"review_count\":\"desc\"},{\"name\":\"asc\"}]," +
                    "\"$offset\":10," +
                    "\"$limit\":15}";
            logger.info("QueryJSON: " + queryJSON);
            Query query = driver.newQuery(queryJSON);
            try (final Connection connection = DriverManager.getConnection(Constants.CONNECTION_URL);
                 final DocumentStore store = connection.getStore(Constants.TABLE_NAME)) {

                stopWatch.start(); //Start stopWatch
                stream = store.findQuery(query);

                logger.info("Query Plan: " + stream.getQueryPlan().asJsonString()); //Log Query Plan for debugging
                logger.info("Query Result:");
                for(Document document : stream) {
                    logger.info(document.asJsonString());
                    counter.incrementAndGet();

                }

            }
        } catch (Exception e) {
            logger.error("Application failed with " + e.getMessage());
            e.printStackTrace();
        } finally {
            if(stopWatch.getTime() > 0) {
                logger.info("Query returned " + counter.intValue() + " number of documents in "
                        + stopWatch.getTime() + " ms.");
                stopWatch.stop();
            }
            if(stream != null)
                stream.close();
        }
    }
}
