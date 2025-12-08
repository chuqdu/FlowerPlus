package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.dto.request.UpdateTransactionStatusDto;
import base.api.dto.response.TFUResponse;
import base.api.dto.response.TransactionDto;
import base.api.dto.response.TransactionWithOrderDto;
import base.api.entity.TransactionModel;
import base.api.service.ITransactionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class  TransactionController extends BaseAPIController {

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private ModelMapper mapper;

    @GetMapping("get-list-transactions")
    public ResponseEntity<TFUResponse<List<TransactionDto>>> getListTransactions() {
        List<TransactionModel> transactions = transactionService.getListTransactions();
        List<TransactionDto> transactionDtos = transactions.stream()
                .map(transaction -> mapper.map(transaction, TransactionDto.class))
                .toList();
        return success(transactionDtos);
    }

    @GetMapping("get-list-transactions-with-order")
    public ResponseEntity<TFUResponse<List<TransactionWithOrderDto>>> getListTransactionsWithOrder() {
        List<TransactionWithOrderDto> transactions = transactionService.getListTransactionsWithOrderInfo();
        return success(transactions);
    }

    @PutMapping("{transactionId}/update-status")
    public ResponseEntity<TFUResponse<TransactionDto>> updateTransactionStatus(
            @PathVariable Long transactionId,
            @RequestBody UpdateTransactionStatusDto dto
    ) {
        TransactionModel transaction = transactionService.updateTransactionStatus(transactionId, dto.getStatus());
        TransactionDto transactionDto = mapper.map(transaction, TransactionDto.class);
        return success(transactionDto);
    }
}
