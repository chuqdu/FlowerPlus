package com.capstone.BEApp.service;

import com.capstone.BEApp.config.VNPayConfig;
import com.capstone.BEApp.config.VNPayUtil;
import com.capstone.BEApp.entity.Account;
import com.capstone.BEApp.entity.Transaction;
import com.capstone.BEApp.entity.Wallet;
import com.capstone.BEApp.repository.TransactionRepository;
import com.capstone.BEApp.repository.WalletRepository;
import com.capstone.BEApp.repository.AccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayService {

    private final VNPayConfig vnPayConfig;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    // --- Tạo link thanh toán ---
    public String createPaymentUrl(Long accountId, BigDecimal amount, HttpServletRequest request) throws Exception {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "wallet_topup";
        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        String vnp_IpAddr = request.getRemoteAddr();
        String vnp_TmnCode = vnPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount.multiply(BigDecimal.valueOf(100)).longValue()));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Nạp tiền vào ví #" + accountId);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.vnp_ReturnUrl + "?accountId=" + accountId + "&amount=" + amount);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext(); ) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String secureHash = VNPayUtil.hmacSHA512(vnPayConfig.vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return vnPayConfig.vnp_PayUrl + "?" + query;
    }

    public String handlePaymentReturn(Long accountId, BigDecimal amount, String responseCode) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        Transaction transaction = Transaction.builder()
                .customer(account)
                .paymentMethod("VNPAY")
                .transactionDate(LocalDateTime.now())
                .status("FAILED")
                .build();

        if ("00".equals(responseCode)) {
            Wallet wallet = walletRepository.findByAccountId(accountId)
                    .orElseGet(() -> walletRepository.save(Wallet.builder()
                            .account(account)
                            .amount(BigDecimal.ZERO)
                            .build()));

            wallet.setAmount(wallet.getAmount().add(amount));
            walletRepository.save(wallet);

            transaction.setStatus("SUCCESS");
            transactionRepository.save(transaction);

            return "Nạp tiền thành công! Ví đã được cộng " + amount + " VND.";
        } else {
            transactionRepository.save(transaction);
            return "Giao dịch thất bại!";
        }
    }
}
