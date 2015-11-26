package ru.ifmo.pp;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bank implementation.
 * <p>
 *
 * @author Philippov Dmitry, M3339
 */
public class BankImpl implements Bank {
    /**
     * An array of accounts by index.
     */
    private final Account[] accounts;

    /**
     * Creates new bank instance.
     *
     * @param n the number of accounts (numbered from 0 to n-1).
     */
    public BankImpl(int n) {
        accounts = new Account[n];
        for (int i = 0; i < n; i++) {
            accounts[i] = new Account();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfAccounts() {
        return accounts.length;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public long getAmount(int index) {
        accounts[index].lock();
        long ans = 0;
        try {
            ans = accounts[index].amount;
        } finally {
            accounts[index].unlock();
        }
        return ans;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public long getTotalAmount() {
        long sum = 0;
        for (Account account : accounts) {
            account.lock();
        }
        for (Account account : accounts) {
            sum += account.amount;
        }
        for (Account account : accounts) {
            account.unlock();
        }
        return sum;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public long deposit(int index, long amount) {
        long ans = 0;
        Account account = accounts[index];
        account.lock();
        try {
            if (amount <= 0) {
                throw new IllegalArgumentException("Invalid amount: " + amount);
            }
            if (amount > MAX_AMOUNT || account.amount + amount > MAX_AMOUNT) {
                throw new IllegalStateException("Overflow");
            }
            account.amount += amount;
            ans = account.amount;
        } finally {
            account.unlock();
        }
        return ans;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public long withdraw(int index, long amount) {
        long ans = 0;
        Account account = accounts[index];
        account.lock();
        try {
            if (amount <= 0) {
                throw new IllegalArgumentException("Invalid amount: " + amount);
            }
            if (account.amount - amount < 0) {
                throw new IllegalStateException("Underflow");
            }
            account.amount -= amount;
            ans = account.amount;
        } finally {
            account.unlock();
        }
        return ans;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public void transfer(int fromIndex, int toIndex, long amount) {
        Account from = accounts[fromIndex];
        Account to = accounts[toIndex];
        if (fromIndex < toIndex) {
            from.lock();
            to.lock();
        } else {
            to.lock();
            from.lock();
        }
        try {
            if (amount <= 0) {
                throw new IllegalArgumentException("Invalid amount: " + amount);
            }
            if (fromIndex == toIndex) {
                throw new IllegalArgumentException("fromIndex == toIndex");
            }
            if (amount > from.amount) {
                throw new IllegalStateException("Underflow");
            } else if (amount > MAX_AMOUNT || to.amount + amount > MAX_AMOUNT) {
                throw new IllegalStateException("Overflow");
            }
            from.amount -= amount;
            to.amount += amount;
        } finally {
            if (fromIndex < toIndex) {
                to.unlock();
                from.unlock();
            } else {
                from.unlock();
                to.unlock();
            }
        }
    }

    /**
     * Private account data structure.
     */
    private static class Account {
        /**
         * Constructor for Account class - initializes lock with ReentrantLock
         *
         * @see java.util.concurrent.locks.ReentrantLock
         */
        public Account() {
            lock = new ReentrantLock();
        }

        /**
         * Amount of funds in this account.
         */
        long amount;

        /**
         * Thin lock
         */
        private final Lock lock;

        /**
         * Locks lock
         */
        void lock() {
            lock.lock();
        }

        /**
         * Unlocks lock
         */
        void unlock() {
            lock.unlock();
        }
    }
}
