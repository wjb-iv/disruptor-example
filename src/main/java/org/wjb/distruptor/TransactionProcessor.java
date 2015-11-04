package org.wjb.distruptor;

import org.wjb.accountstore.AccountStore;
import org.wjb.accountstore.model.Transaction;
import org.wjb.distruptor.event.TransactionEvent;
import org.wjb.distruptor.handler.GenericExceptionHandler;
import org.wjb.distruptor.handler.JournalTransactionHandler;
import org.wjb.distruptor.handler.PostTransactionHandler;
import org.wjb.distruptor.handler.ReplicateTransactionHandler;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>An example of using the LMAX disruptor framework:
 *       http://lmax-exchange.github.io/disruptor/ </p>
 * 
 * <p>Inspired by this blog post:
 *       http://blog.jteam.nl/2011/07/20/processing-1m-tps-with-axon-framework-and-the-disruptor/ </p>
 * 
 * <p>This is an extremely oversimplified version of the "diamond configuration" 
 * as described in http://mechanitis.blogspot.com/2011/07/dissecting-disruptor-wiring-up.html </p>
 * 
 * <p>In this implementation, a journal and replication step happen concurrently, 
 * and both must succeed for the post step to occur, as shown below: </p>
 * 
 * <pre>
 *          replicate
 *            /   \
 *           /     \
 *  event ->       post
 *           \     /
 *            \   /
 *           journal
 * </pre>
 * 
 * <p>This is also an example of in-memory storage based on "event sourcing" -
 * see Martin Fowler: http://martinfowler.com/eaaDev/EventSourcing.html</p>
 * 
 */
public class TransactionProcessor {
    private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
    private Disruptor disruptor;
    private RingBuffer ringBuffer;
    private final AccountStore accountStore;
    private final TransactionEventPublisher publisher = new TransactionEventPublisher();
    
    private JournalTransactionHandler journal;
    private ReplicateTransactionHandler replicate;
    private PostTransactionHandler post;

    public TransactionProcessor(AccountStore accountStore) {
        this.accountStore = accountStore;
    }
    
    public void postTransaction(Transaction transaction) {
        disruptor.publishEvent(publisher, transaction);
    }
    
    public void init() {
        disruptor = new Disruptor<>(
                TransactionEvent.EVENT_FACTORY, 
                1024, 
                EXECUTOR,
                ProducerType.SINGLE,
                new YieldingWaitStrategy());
        
        // Pretend that we have real journaling, just to demo it...
        File journalDir = new File("target/test");
        journalDir.mkdirs();
        File journalFile = new File(journalDir, "test-journal.txt");
        
        // In this example start fresh each time - though a real implementation
        // might roll over the journal or the like.
        if (journalFile.exists()) {
            journalFile.delete(); 
        }

        journal = new JournalTransactionHandler(journalFile);
        
        replicate = new ReplicateTransactionHandler();
        
        post = new PostTransactionHandler(accountStore);

        // This is where the magic happens 
        // (see "diamond configuration" in javadoc above)
        disruptor.handleEventsWith(journal, replicate).then(post);
        
        // We don't do any fancy exception handling in this demo, but if we
        // did, one way to set it up for each handler is like this:
        ExceptionHandler exh = new GenericExceptionHandler();
        disruptor.handleExceptionsFor(journal).with(exh);
        disruptor.handleExceptionsFor(replicate).with(exh);
        disruptor.handleExceptionsFor(post).with(exh);

        ringBuffer = disruptor.start();
        
    }
    
    public void destroy() {
        try {
            journal.closeJournal();
        } catch (Exception ignored) {}
        
        try {
            disruptor.shutdown();
        } catch (Exception ignored) {}
        
        EXECUTOR.shutdownNow();
    }
    
    /**
     * This is the way events get into the system - the ring buffer is full of pre-allocated events
     * and this is how a specific event's state is input to the buffer. Pre-allocation of the buffer
     * is a key component of the Disruptor pattern.
     */
    class TransactionEventPublisher implements EventTranslatorOneArg<TransactionEvent, Transaction> {
        @Override
        public void translateTo(TransactionEvent event, long sequence, Transaction tx) {
            event.setTransaction(tx);
            event.setBufferSeq(sequence); // We don't really use this, just demonstrating its availability
        }
    }
}
