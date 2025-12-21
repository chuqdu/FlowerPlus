package base.api.service.impl;

import base.api.dto.response.TransactionWithOrderDto;
import base.api.entity.OrderModel;
import base.api.entity.TransactionModel;
import base.api.entity.UserModel;
import base.api.repository.ITransactionRepository;
import base.api.service.ITransactionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService implements ITransactionService {

    @Autowired
    private ITransactionRepository transactionRepository;

    @Autowired
    private base.api.service.IDeliveryStatusService deliveryStatusService;

    @Override
    public List<TransactionModel> getListTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<TransactionWithOrderDto> getListTransactionsWithOrderInfo() {
        List<TransactionModel> transactions = transactionRepository.findAllByOrderByCreatedAtDesc();
        return transactions.stream().map(tx -> {
            TransactionWithOrderDto dto = new TransactionWithOrderDto();
            dto.setId(tx.getId());
            dto.setOrderCode(tx.getOrderCode());
            dto.setAmount(tx.getAmount());
            dto.setStatus(tx.getStatus());
            dto.setCheckoutUrl(tx.getCheckoutUrl());
            dto.setPaymentLinkId(tx.getPaymentLinkId());
            dto.setCreatedAt(tx.getCreatedAt());
            
            OrderModel order = tx.getOrder();
            if (order != null) {
                dto.setOrderId(order.getId());
                dto.setShippingAddress(order.getShippingAddress());
                dto.setPhoneNumber(order.getPhoneNumber());
                dto.setRecipientName(order.getRecipientName());
                dto.setNote(order.getNote());
                dto.setRequestDeliveryTime(order.getRequestDeliveryTime());
                
                UserModel user = order.getUser();
                if (user != null) {
                    dto.setUserId(user.getId());
                    dto.setUserName(user.getUserName());
                    dto.setUserEmail(user.getEmail());
                    dto.setUserPhone(user.getPhone());
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public TransactionModel updateTransactionStatus(Long transactionId, String status) {
        TransactionModel transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
        
        String oldStatus = transaction.getStatus();
        transaction.setStatus(status);
        TransactionModel savedTransaction = transactionRepository.save(transaction);
        
        if ("SUCCESS".equals(status) && !"SUCCESS".equals(oldStatus)) {
            OrderModel order = transaction.getOrder();
            if (order != null) {
                // Cập nhật delivery step sang PREPARING
                deliveryStatusService.setCurrentStepCascading(
                        order.getId(),
                        base.api.enums.DeliveryStep.PREPARING,
                        "Thanh toán thành công, hệ thống đang chuẩn bị đơn hàng",
                        null,
                        null,
                        order.getUser().getId()
                );
            }
        }
        
        return savedTransaction;
    }
}
