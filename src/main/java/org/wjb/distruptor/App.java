package org.wjb.distruptor;

import org.wjb.accountstore.AccountStore;
import org.wjb.accountstore.model.Transaction;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class App {

    public static void main(String[] args) throws Exception {
        
        App app = new App();
        app.multiThreadUpdatesTest();
        app.restoreFromJournalTest();
        
    }
    
    public void multiThreadUpdatesTest() throws InterruptedException {
        AccountStore accounts = new AccountStore();
        TransactionProcessor processor = new TransactionProcessor(accounts);
        processor.init();
        
        Thread t1 = new Thread(new DemoWorker(TEST_SET_A, processor));
        Thread t2 = new Thread(new DemoWorker(TEST_SET_B, processor));
        t1.start();
        t2.start();
        
        // Wait for the transactions to filter through, of course you would
        // usually have the transaction processor lifecycle managed by a 
        // container or in some other more sophisticated way...
        try {
            Thread.sleep(3000);
        } catch (Exception ignored){}
        
        processor.destroy();
        
        accounts.dumpAccounts();
    }
    
    public void restoreFromJournalTest() throws IOException {
        AccountStore accounts = new AccountStore();
        accounts.restoreFromJournal(new File("target/test/test-journal.txt"));
        accounts.dumpAccounts();
    }
    
    class DemoWorker implements Runnable {
        private TransactionProcessor tp;
        private List<Transaction> testSet;
        
        public DemoWorker(List<Transaction> testSet, TransactionProcessor tp) {
            this.tp = tp;
            this.testSet = testSet;
        }
        
        public void run() {
            for (Transaction tx: testSet) {
                tp.postTransaction(tx);
            }
        }
        
    }
    
    private static final List<Transaction> TEST_SET_A = new ArrayList<Transaction>();
    static {
        TEST_SET_A.add(new Transaction(new Date(), new BigDecimal("1200.00"), "54321", "New Account Opened"));
        TEST_SET_A.add(new Transaction(new Date(), new BigDecimal("-134.56"), "54321", "Check Cleared"));
        TEST_SET_A.add(new Transaction(new Date(), new BigDecimal("129.78"), "12345", "Direct Deposit"));
        TEST_SET_A.add(new Transaction(new Date(), new BigDecimal("100.00"), "54321", "Deposit"));
        TEST_SET_A.add(new Transaction(new Date(), new BigDecimal("-60"), "12345", "ATM Withdrawal"));
        TEST_SET_A.add(new Transaction(new Date(), new BigDecimal("125.56"), "12345", "Direct Deposit"));
        TEST_SET_A.add(new Transaction(new Date(), new BigDecimal("-568.90"), "54321", "Loan Payment"));
    }
    
    private static final List<Transaction> TEST_SET_B = new ArrayList<Transaction>();
    static {
        TEST_SET_B.add(new Transaction(new Date(), new BigDecimal("2478.45"), "66611", "Direct Deposit"));
        TEST_SET_B.add(new Transaction(new Date(), new BigDecimal("-234.60"), "66611", "Check Cleared"));
        TEST_SET_B.add(new Transaction(new Date(), new BigDecimal("400.34"), "12345", "Deposit"));
        TEST_SET_B.add(new Transaction(new Date(), new BigDecimal("95.50"), "54321", "Deposit"));
        TEST_SET_B.add(new Transaction(new Date(), new BigDecimal("-50"), "66611", "ATM Withdrawal"));
        TEST_SET_B.add(new Transaction(new Date(), new BigDecimal("-10.00"), "12345", "Service Fee"));
        TEST_SET_B.add(new Transaction(new Date(), new BigDecimal("-10.00"), "54321", "Service Fee"));
    }
}
