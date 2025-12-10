package base.api.service.impl;

import base.api.dto.response.SummaryDto;
import base.api.repository.IOrderRepository;
import base.api.repository.IProductRepository;
import base.api.repository.ITransactionRepository;
import base.api.repository.IUserRepository;
import base.api.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {

    private final IUserRepository userRepository;
    private final IProductRepository productRepository;
    private final IOrderRepository orderRepository;
    private final ITransactionRepository transactionRepository;

    @Override
    public SummaryDto getSummary() {
        SummaryDto summaryDto = new SummaryDto();

        summaryDto.setTotalUsers(userRepository.count());
        summaryDto.setTotalProducts(productRepository.count());
        summaryDto.setTotalOrders(orderRepository.count());
        
        Double totalRevenue = transactionRepository.getTotalRevenue();
        Double totalRefunded = transactionRepository.getTotalRefunded();
        summaryDto.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0);
        summaryDto.setTotalRefunded(totalRefunded != null ? totalRefunded : 0.0);
        summaryDto.setNetRevenue((totalRevenue != null ? totalRevenue : 0.0) - (totalRefunded != null ? totalRefunded : 0.0));

        summaryDto.setSuccessfulOrders(orderRepository.countSuccessfulOrders() != null ? orderRepository.countSuccessfulOrders() : 0L);
        summaryDto.setDeliveringOrders(orderRepository.countDeliveringOrders() != null ? orderRepository.countDeliveringOrders() : 0L);
        summaryDto.setPendingOrders(orderRepository.countPendingOrders() != null ? orderRepository.countPendingOrders() : 0L);
        summaryDto.setFailedOrders(orderRepository.countFailedOrders() != null ? orderRepository.countFailedOrders() : 0L);
        summaryDto.setRefundedOrders(orderRepository.countRefundedOrders() != null ? orderRepository.countRefundedOrders() : 0L);

        summaryDto.setOrderAmountsByStatus(orderRepository.getOrderAmountsByStatus());
        summaryDto.setMonthlyOrders(orderRepository.countMonthlyOrders());
        summaryDto.setMonthlyOrdersByStatus(orderRepository.getMonthlyOrdersByStatus());
        summaryDto.setYearlyOrders(orderRepository.countYearlyOrders());
        summaryDto.setMonthlyRevenue(transactionRepository.getMonthlyRevenue());
        summaryDto.setYearlyRevenue(transactionRepository.getYearlyRevenue());
        summaryDto.setTopCustomers(orderRepository.getTopCustomers());

        return summaryDto;
    }
}

