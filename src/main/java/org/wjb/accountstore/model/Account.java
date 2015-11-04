package org.wjb.accountstore.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Example account, for demo purposes.
 */
public class Account {
    private String accountnbr;
    private List<Transaction> history = new ArrayList<Transaction>();
    private BigDecimal balance = new BigDecimal(0);

    public Account(String accountnbr) {
        this.accountnbr = accountnbr;
    }

    public String getAccountnbr() {
        return accountnbr;
    }
    
    public void post(Transaction transaction) {
        balance = balance.add(transaction.getAmount());
        history.add(transaction);
    }

    public List<Transaction> getHistory() {
        return history;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Account: %s -- Balance: %s \n", accountnbr, balance));
        builder.append("Transactions: \n");
        for (Transaction t: history) {
            builder.append(t.toString());
            builder.append("\n");
        }
        return builder.toString();
    }
}
