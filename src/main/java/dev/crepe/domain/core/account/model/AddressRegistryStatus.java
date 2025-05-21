package dev.crepe.domain.core.account.model;

public enum AddressRegistryStatus {
    REGISTERING,        //등록 중
    ACTIVE,             //등록 완료
    NOT_REGISTERED,     //미 등록
    UNREGISTERED,       //해지 중
    UNREGISTERED_AND_REGISTERING, //해지 중 및 재 등록 중
}
