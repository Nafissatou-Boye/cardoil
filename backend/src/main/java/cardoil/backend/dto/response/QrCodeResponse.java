package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QrCodeResponse {
    private String code;
    private LocalDateTime expiration;
}