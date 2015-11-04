package org.wjb.distruptor.handler;

import org.wjb.accountstore.AccountStore;
import org.wjb.accountstore.model.Account;
import org.wjb.distruptor.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler uses the event to update the in-memory data store. Operations on 
 * this store always happen in a single thread, so concurrency issues are non-
 * existent for these updates. 
 */
public class PostTransactionHandler extends AbstractEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(PostTransactionHandler.class);
    private AccountStore accountStore;

    public PostTransactionHandler(AccountStore accountStore) {
        this.accountStore = accountStore;
    }
    
    @Override
    public void onEvent(TransactionEvent event, long sequence, boolean endOfBatch) throws Exception {
        Account act = accountStore.getAccount(event.getTransaction().getAccountnbr());
        if (act==null) {
            act = new Account(event.getTransaction().getAccountnbr());
        }
        act.post(event.getTransaction());
        accountStore.saveAccount(act); 
        logger.debug("POSTED TRANSACTION -> {}", event.getTransaction().toString());
    }
}