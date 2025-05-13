package dev.crepe.domain.admin.service;

public interface AdminRefundService {

    void approveRefund(Long payId, String storeEmail);

}
