package cardoil.backend.exception;

import cardoil.backend.controller.LiaisonCompagnieController;
import cardoil.backend.controller.RechargeExterneApiController;
import cardoil.backend.controller.RechargeSupervisionController;
import cardoil.backend.controller.SuperAdminEtablissementFinancierController;
import cardoil.backend.dto.response.ErreurRechargeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(assignableTypes = {
        RechargeExterneApiController.class,
        SuperAdminEtablissementFinancierController.class,
        LiaisonCompagnieController.class,
        RechargeSupervisionController.class
})
public class RechargeExterneExceptionHandler {

    @ExceptionHandler(RechargeException.class)
    public ResponseEntity<ErreurRechargeDTO> gererRechargeException(RechargeException ex) {
        ErreurRechargeDTO corps = ErreurRechargeDTO.builder()
                .errorCode(ex.getCodeErreur().name())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(ex.getStatusHttp()).body(corps);
    }

    @ExceptionHandler(CardoilException.class)
    public ResponseEntity<ErreurRechargeDTO> gererCardoilException(CardoilException ex) {
        ErreurRechargeDTO corps = ErreurRechargeDTO.builder()
                .errorCode("ERR_BUSINESS_RULE")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corps);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurRechargeDTO> gererValidation(MethodArgumentNotValidException ex) {
        FieldError premiereErreur = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);

        String champ = premiereErreur != null ? premiereErreur.getField() : null;
        String message = premiereErreur != null ? premiereErreur.getDefaultMessage() : "Requête invalide";

        String errorCode = switch (champ != null ? champ : "") {
            case "phoneNumber" -> "ERR_INVALID_PHONE";
            case "amount" -> "ERR_INVALID_AMOUNT";
            default -> "ERR_VALIDATION";
        };

        ErreurRechargeDTO corps = ErreurRechargeDTO.builder()
                .errorCode(errorCode)
                .message(message)
                .champ(champ)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corps);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurRechargeDTO> gererErreurInattendue(Exception ex) {
        log.error("Erreur inattendue sur un endpoint Recharge Externe / Établissement Financier", ex);
        ErreurRechargeDTO corps = ErreurRechargeDTO.builder()
                .errorCode("ERR_INTERNAL")
                .message("Une erreur interne est survenue.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(corps);
    }
}