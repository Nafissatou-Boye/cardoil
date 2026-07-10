package cardoil.backend.service;

import cardoil.backend.entity.Promotion;
import cardoil.backend.entity.Transaction;

public interface FideliteService {
    void attribuerPoints(Transaction transaction, Promotion promotion);
}