package dev.crepe.domain.channel.actor.service;

import dev.crepe.domain.core.subscribe.model.dto.request.SubscribeProductRequest;
import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeProductResponse;
import org.springframework.stereotype.Service;

@Service
public interface ActorSubscribeService {
    SubscribeProductResponse subscribeProduct(String userEmail, SubscribeProductRequest request);
}
