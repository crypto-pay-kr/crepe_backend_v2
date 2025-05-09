package dev.crepe.domain.channel.market.like.service.impl;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.channel.actor.store.exception.StoreNotFoundException;
import dev.crepe.domain.channel.actor.user.exception.UserNotFoundException;
import dev.crepe.domain.channel.market.like.model.entity.Like;
import dev.crepe.domain.channel.market.like.repository.LikeRepository;
import dev.crepe.domain.channel.market.like.service.LikeService;
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

    @Transactional
    public void addLike(String userEmail, Long storeId) {
        Actor user= actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        Actor store = actorRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));

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
        Actor user = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        Actor store = actorRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));


        Optional<Like> existingLike = likeRepository.findByUserAndStore(user, store);

        if (existingLike.isPresent()) {
            Like storeLike = existingLike.get();

            if (!storeLike.isActive()) {
                return;
            }
            storeLike.deactivate();
        }
    }

}
