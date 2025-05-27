package dev.crepe.domain.core.product.service;

import dev.crepe.domain.admin.dto.request.ChangeProductSaleRequest;
import dev.crepe.domain.admin.dto.response.GetAllProductResponse;
import dev.crepe.domain.admin.dto.response.GetProductDetailResponse;
import dev.crepe.domain.channel.actor.model.dto.request.ActorEligibilityRequest;
import dev.crepe.domain.core.product.model.dto.request.ReviewProductSubmissionRequest;
import dev.crepe.domain.core.product.model.dto.response.GetOnsaleProductListReponse;
import dev.crepe.domain.core.product.model.dto.response.ReviewProductSubmissionResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProductService {
    ReviewProductSubmissionResponse productInspect(ReviewProductSubmissionRequest request);
    ReviewProductSubmissionResponse changeProductSalesStatus(ChangeProductSaleRequest request);
    List<GetAllProductResponse> getAllBankProducts(Long bankId);
    List<GetAllProductResponse> getSuspendedBankProducts(Long bankId);

    GetProductDetailResponse getProductDetail(Long bankId, Long productId);

    List<GetOnsaleProductListReponse> getOnSaleProducts();

    boolean checkEligibility(Long productId, ActorEligibilityRequest request);

}
