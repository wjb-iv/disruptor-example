package org.wjb.distruptor.handler;

import org.wjb.distruptor.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In the real world, this handler would replicate the transaction event to in-memory 
 * date stores running on one or more other nodes as part of a redundancy strategy.
 */
public class ReplicateTransactionHandler extends AbstractEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(ReplicateTransactionHandler.class);

    @Override
    public void onEvent(TransactionEvent event, long sequence, boolean endOfBatch) throws Exception {
        logger.warn("TODO: REPLICATE -> {}", event.getTransaction().toString());
    }
}
