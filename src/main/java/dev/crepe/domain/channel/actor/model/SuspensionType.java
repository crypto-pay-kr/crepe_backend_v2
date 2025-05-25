package dev.crepe.domain.channel.actor.model;

import lombok.Getter;

@Getter
public enum SuspensionType {
    TEMPORARY("임시정지"),
    PERMANENT("영구정지");

    private final String description;

    SuspensionType(String description) {
        this.description = description;
    }

}