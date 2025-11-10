package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.dto.request.ProductDto;
import base.api.dto.response.TFUResponse;
import base.api.dto.response.TransactionDto;
import base.api.entity.ProductModel;
import base.api.entity.TransactionModel;
import base.api.service.ITransactionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController extends BaseAPIController {

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private ModelMapper mapper;

    @GetMapping("get-list-transactions")
    public ResponseEntity<TFUResponse<List<TransactionDto>>> createProduct() {
        List<TransactionModel> transactions = transactionService.getListTransactions();
        List<TransactionDto> transactionDtos = transactions.stream()
                .map(transaction -> mapper.map(transaction, TransactionDto.class))
                .toList();
        return success(transactionDtos);
    }
}
