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
        summaryDto.setTotalRevenue(transactionRepository.getTotalRevenue() != null ? transactionRepository.getTotalRevenue() : 0.0);

        summaryDto.setMonthlyOrders(orderRepository.countMonthlyOrders());
        summaryDto.setYearlyOrders(orderRepository.countYearlyOrders());
        summaryDto.setMonthlyRevenue(transactionRepository.getMonthlyRevenue());
        summaryDto.setYearlyRevenue(transactionRepository.getYearlyRevenue());

        return summaryDto;
    }
}

