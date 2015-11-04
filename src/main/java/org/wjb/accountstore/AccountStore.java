package org.wjb.accountstore;

import org.wjb.accountstore.model.Account;
import org.wjb.accountstore.model.Transaction;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A quick and dirty stand-in for a sophisticated in-memory data store. This
 * implementation is of dubious thread-safety, but suffices for illustrating 
 * the example.
 */
public class AccountStore {
    
    Map<String,Account> store = new ConcurrentHashMap<String,Account>();
    
    public Account getAccount(String acctnbr) {
        return store.get(acctnbr);
    }
    
    public void saveAccount(Account account) {
        store.put(account.getAccountnbr(), account);
    }
    
    /**
     * This is probably not the right place to put this method - but I wanted
     * to validate the example journal somehow, and this seemed easiest...
     * 
     * @param journalFile 
     */
    public void restoreFromJournal(File journalFile) throws FileNotFoundException, IOException {
        if (journalFile.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(journalFile));
                String line;
                while ( (line=reader.readLine())!=null ) {
                    if ( !"".equals(line) ) {
                        String[] attrs = line.split("\\|");
                        // 0 - sequence (long)
                        // 1 - date     (long)
                        // 2 - account  (String)
                        // 3 - type     (String)
                        // 4 - amount   (BigDecimal)
                        Transaction t = new Transaction(new Date(Long.parseLong(attrs[1])), new BigDecimal(attrs[4]), attrs[2], attrs[3]);
                        Account act = getAccount(t.getAccountnbr());
                        if (act==null) {
                            act = new Account(t.getAccountnbr());
                        }
                        act.post(t);
                        saveAccount(act);
                    }
                }
            } finally {
                if (reader!=null) {
                    reader.close();
                }
            }
        }
    }
    
    public void dumpAccounts() {
        for (Account account: store.values()) {
            System.out.println(account.toString()+"==============================================================");
        }
    }
}
