package com.allan.service;

import java.util.List;

import com.allan.model.Order;
import com.allan.model.Seller;
import com.allan.model.Transaction;

public interface TransactionService {

    Transaction createTransaction(Order order);

    List<Transaction> getTransactionsBySellerId(Seller seller);

    List<Transaction> getAllTransactions();

}
