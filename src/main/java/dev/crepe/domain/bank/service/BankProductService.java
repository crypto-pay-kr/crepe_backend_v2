package dev.crepe.domain.bank.service;

import dev.crepe.domain.core.product.model.dto.request.RegisterProductRequest;
import dev.crepe.domain.core.product.model.dto.response.RegisterProductResponse;
import org.springframework.web.multipart.MultipartFile;

public interface BankProductService {
    RegisterProductResponse registerProduct(String email, MultipartFile productImg, RegisterProductRequest request);

}
