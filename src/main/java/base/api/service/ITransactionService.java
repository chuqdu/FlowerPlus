package base.api.service;

import base.api.dto.response.TransactionWithOrderDto;
import base.api.entity.TransactionModel;

import java.util.List;

public interface ITransactionService {
    List<TransactionModel> getListTransactions();
    List<TransactionWithOrderDto> getListTransactionsWithOrderInfo();
    TransactionModel updateTransactionStatus(Long transactionId, String status);
}
