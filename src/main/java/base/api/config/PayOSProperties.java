package base.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payos")
@Getter
@Setter
public class PayOSProperties {
    private String clientId;
    private String apiKey;
    private String checksumKey;
}
