package dev.crepe.domain.channel.market.like.service.impl;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.channel.market.like.model.entity.Like;
import dev.crepe.domain.channel.market.like.repository.LikeRepository;
import dev.crepe.domain.channel.market.like.service.LikeService;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeServiceImpl implements LikeService {

    private final ActorRepository actorRepository;
    private final LikeRepository likeRepository;
    private final ExceptionDbService exceptionDbService;

    @Transactional
    public void addLike(String userEmail, Long storeId) {
        Actor user= actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("ACTOR_002"));

        Actor store = actorRepository.findById(storeId)
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));

        Optional<Like> existingLike = likeRepository.findByUserAndStore(user, store);

        if (existingLike.isPresent()) {
            Like storeLike = existingLike.get();

            if (storeLike.isActive()) {
                return;
            }
            storeLike.activate();
        } else {
            Like storeLike = Like.builder()
                    .user(user)
                    .store(store)
                    .active(true)
                    .build();

            likeRepository.save(storeLike);
        }
    }


    @Transactional
    public void removeLike(String userEmail, Long storeId) {
        Actor user= actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("ACTOR_002"));

        Actor store = actorRepository.findById(storeId)
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));


        Optional<Like> existingLike = likeRepository.findByUserAndStore(user, store);

        if (existingLike.isPresent()) {
            Like storeLike = existingLike.get();

            if (!storeLike.isActive()) {
                return;
            }
            storeLike.deactivate();
        }
    }

    @Override
    public boolean isLikedByUser(String userEmail, Long storeId) {
        Actor user= actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("ACTOR_002"));

        Actor store = actorRepository.findById(storeId)
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));

        return likeRepository.findByUserAndStore(user, store)
                .map(Like::isActive)
                .orElse(false);
    }


}
