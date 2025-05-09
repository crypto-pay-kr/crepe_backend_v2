package dev.crepe.domain.channel.actor.store.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class MenuNotFoundException extends LocalizedMessageException {
    public MenuNotFoundException(Long menuId) {
        super(HttpStatus.NOT_FOUND, "menu.not.found",menuId);
    }
}
