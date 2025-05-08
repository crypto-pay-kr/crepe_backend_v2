package dev.crepe.infra.naver.id_ocr.entity;

import lombok.Getter;

@Getter
public enum IdCardType {

    ID_CARD("ic"),
    DRIVERS_LISCENSE("dl");

    private final String parentField;

    IdCardType(String parentField) {
        this.parentField = parentField;
    }

}
