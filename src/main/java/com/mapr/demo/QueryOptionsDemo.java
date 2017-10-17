package com.mapr.demo;

import com.mapr.ojai.store.impl.OjaiOptions;
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
 * Created by aravi on 10/15/17.
 */
public class QueryOptionsDemo {
    public static final Logger logger = Logger.getLogger(GeneralUtils.getInvokingClassName());

    public static void main( String[] args ) {
        try {
            /**
             * Driver can be considered as an entry point to Ojai API.
             * Driver can be used to build QueryConditions / Queries / Mutations etc.
             */
            Driver driver = DriverManager.getDriver(Constants.CONNECTION_URL);

            //We first build a query condition which forms the "where" clause for a Query
            QueryCondition condition = driver.newCondition()
                    .and() //condition support prefix notation, start with join op
                    .is("stars", QueryCondition.Op.GREATER, 3) //condition 1
                    .is("state", QueryCondition.Op.EQUAL, "NV") //condition 2
                    .close() //close the predicate
                    .build(); //build immutable condition object

            logger.info("Query Condition: " + condition.toString());

            //We now build a query object by specifying the fields to be projected
            Query query = driver.newQuery()
                    .select("name", "address", "review_count") //if no select or select("*"), all fields are returned
                    .where(condition)
                    .orderBy("review_count", SortOrder.DESC)
                    .orderBy("address", SortOrder.ASC)
                    .setOption(OjaiOptions.OPTION_FORCE_SORT, true) //Force non-covering sort
                    .setTimeout(Constants.TIMEOUT_IN_MILLIS) //Set query timeout in ms
                    .build();

            AtomicInteger counter = new AtomicInteger(0);
            StopWatch stopWatch = new StopWatch();

            DocumentStream stream = null;
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

            } finally {
                stopWatch.stop();
                if(stream != null)
                    stream.close();
            }
            logger.info("Query returned " + counter.intValue() + " number of documents in "
                    + stopWatch.getTime() + " ms.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}