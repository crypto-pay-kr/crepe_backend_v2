package dev.crepe.domain.channel.market.like.service;

import org.springframework.stereotype.Service;

@Service
public interface LikeService {
    void addLike(String userEmail, Long storeId);
    void removeLike(String userEmail, Long storeId);
    boolean isLikedByUser(String userEmail, Long storeId);
}
