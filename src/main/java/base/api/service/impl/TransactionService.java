package base.api.service.impl;

import base.api.entity.TransactionModel;
import base.api.repository.ITransactionRepository;
import base.api.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService implements ITransactionService {

    @Autowired
    private ITransactionRepository transactionRepository;

    @Override
    public List<TransactionModel> getListTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc();
    }
}
