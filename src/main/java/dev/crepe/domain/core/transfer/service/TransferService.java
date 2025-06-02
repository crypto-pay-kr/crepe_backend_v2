package dev.crepe.domain.core.transfer.service;


import dev.crepe.domain.core.transfer.model.dto.requset.GetTransferRequest;

public interface TransferService {

    void requestTransfer(GetTransferRequest request, String email);
}
