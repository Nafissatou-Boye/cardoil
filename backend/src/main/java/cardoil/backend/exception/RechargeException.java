package cardoil.backend.exception;

import cardoil.backend.enums.CodeErreurRecharge;
import lombok.Getter;

@Getter
public class RechargeException extends RuntimeException {
    private final CodeErreurRecharge codeErreur;
    private final int statusHttp;

    public RechargeException(CodeErreurRecharge codeErreur, String message, int statusHttp) {
        super(message);
        this.codeErreur = codeErreur;
        this.statusHttp = statusHttp;
    }
}