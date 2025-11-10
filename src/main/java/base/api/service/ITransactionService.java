package base.api.service;

import base.api.entity.TransactionModel;

import java.util.List;

public interface ITransactionService {
    List<TransactionModel> getListTransactions();
}
