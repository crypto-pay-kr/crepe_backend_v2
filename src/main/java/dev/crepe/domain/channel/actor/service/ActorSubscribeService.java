package dev.crepe.domain.channel.actor.service;

import dev.crepe.domain.admin.dto.response.GetAllProductResponse;
import dev.crepe.domain.admin.dto.response.GetProductDetailResponse;
import dev.crepe.domain.core.product.model.dto.response.GetOnsaleProductListReponse;
import dev.crepe.domain.core.subscribe.model.dto.request.SubscribeProductRequest;
import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeProductResponse;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ActorSubscribeService {
    SubscribeProductResponse subscribeProduct(String userEmail, SubscribeProductRequest request);

    List<GetOnsaleProductListReponse> getAllBankProducts(String userEmail);

    GetProductDetailResponse getProductById(Long productId, String userEmail);

    boolean checkEligibility(Long productId, String actorEmail);


}
