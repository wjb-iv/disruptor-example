package org.wjb.distruptor.handler;

import org.wjb.distruptor.event.TransactionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In the real world, this handler would write the transaction event to a durable journal, such
 * that these events can be re-played after a system failure, to rebuild the state of the
 * in memory store. In this example handler, we just do a simplistic file write as a stand-in
 * for a more sophisticated approach.
 */
public class JournalTransactionHandler extends AbstractEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(JournalTransactionHandler.class);
    private FileWriter journal;
    
    public JournalTransactionHandler(File journalFile) {
        try {
            this.journal = new FileWriter(journalFile, true);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    public void closeJournal() throws IOException {
        if (journal!=null) {
            journal.flush();
            journal.close();
        }
    }

    @Override
    public void onEvent(TransactionEvent event, long sequence, boolean endOfBatch) throws Exception {
        journal.write(event.asJournalEntry());
        journal.flush();
        logger.debug("JOURNALED TRANSACTION -> {}", event.getTransaction().toString());
    }
}
