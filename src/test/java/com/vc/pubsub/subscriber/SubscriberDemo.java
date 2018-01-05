package com.vc.pubsub.subscriber;

import com.google.api.gax.core.GoogleCredentialsProvider;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.SubscriptionName;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by Vincent on 2018/1/5.
 */
public class SubscriberDemo {

    private Logger logger = LoggerFactory.getLogger(SubscriberDemo.class);
    private Subscriber subscriber = null;
    private List<String> scopeList = new ArrayList<>();

    class ThreadPerTaskExecutor implements Executor {
        public void execute(Runnable r) {
            logger.info("Thread starts");
            new Thread(r).start();
        }
    }

    @Test
    public void testSubscribe() {

        // From - mvn -Dtest=PublisherDemo#testArgLine -DargLine="-Dproject=AAA -Dtopic=BBB" test
        String subscriberId = System.getProperty("subscriberId");
        String project = System.getProperty("project");


        scopeList.add("https://www.googleapis.com/auth/pubsub");

        logger.info("***** TestSubscribe Starts  ***** ");

        logger.info("Project is : " + project);
        logger.info("SubscriberId is : " + subscriberId);

        SubscriptionName subscriptionName = SubscriptionName.of(project, subscriberId);

        // Async message receiver
        MessageReceiver messageReceiver = new MessageReceiver() {
            public void receiveMessage(PubsubMessage pubsubMessage, AckReplyConsumer ackReplyConsumer) {

                if (pubsubMessage.getData().toStringUtf8().equals("STOP")) {
                    stopSubscriber();
                } else {
                    logger.info("Received message with id :" + pubsubMessage.getMessageId());
                    logger.info("Received message with content :" + pubsubMessage.getData().toStringUtf8());
                }
                ackReplyConsumer.ack();
            }
        };

        try {

            subscriber = Subscriber.newBuilder(subscriptionName, messageReceiver)
                    .setCredentialsProvider(
                            GoogleCredentialsProvider.newBuilder().setScopesToApply(scopeList)
                                    .build())
                    .build();

            subscriber.addListener(new Subscriber.Listener() {
                public void failed(Subscriber.State from, Throwable failure) {
                    // Handle error.
                    logger.error("Error: " + from.name() + " - " + from.toString());
                }
            }, new ThreadPerTaskExecutor());


            subscriber.startAsync().awaitTerminated();


        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            if (subscriber != null) {
                subscriber.stopAsync();
            }
        }
    }

    /**
     * Stop Subscriber
     */
    private void stopSubscriber() {
        logger.info("Mannerly stopping subscriber...");
        try {
            subscriber.stopAsync();
            logger.info("Stopped.");
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }
    }

}
