package com.bankhub.transaction.application.port.in;

public interface CompletePixUseCase {
    void execute(String transactionId, String finalStatus, String failureReason);
}
