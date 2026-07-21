package cardoil.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "orange.sms")
public class OrangeSmsProperties {
    private String apiUrl;
    private String login;
    private String token;
    private String privateKey;
    private String password;
    private String signature;
    private String subject;
}