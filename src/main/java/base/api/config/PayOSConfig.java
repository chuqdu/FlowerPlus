package base.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;


@Configuration
public class PayOSConfig {

    @Bean
    public PayOS payOS(PayOSProperties properties) {
        return new PayOS(
                properties.getClientId(),
                properties.getApiKey(),
                properties.getChecksumKey()
        );
    }
}