package dev.crepe.domain.bank.service;

import dev.crepe.domain.bank.model.dto.response.GetAllProductResponse;
import dev.crepe.domain.core.product.model.dto.request.RegisterProductRequest;
import dev.crepe.domain.core.product.model.dto.response.RegisterProductResponse;
import dev.crepe.domain.core.product.model.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BankProductService {
    RegisterProductResponse registerProduct(String email, MultipartFile productImg, MultipartFile guideFile, RegisterProductRequest request);

    List<GetAllProductResponse> findAllProductsByBankEmail(String email);

    GetAllProductResponse findProductByIdAndBankEmail(String email, Long id);

    List<GetAllProductResponse> findSuspendedProductsByBankEmail(String email);

}
