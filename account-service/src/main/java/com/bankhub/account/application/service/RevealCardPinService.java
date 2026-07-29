package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.RevealCardPinUseCase;
import com.bankhub.account.application.port.in.ValidateTransactionPinUseCase;
import com.bankhub.account.application.port.out.CardPersistencePort;
import com.bankhub.account.domain.Card;
import com.bankhub.account.domain.CardType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevealCardPinService implements RevealCardPinUseCase {

    private final ValidateTransactionPinUseCase validateTransactionPinUseCase;
    private final CardPersistencePort cardPersistencePort;

    @Override
    public String execute(String accountId, String cardId, String customerId, String transactionPin) {
        log.info("Solicitação de Reveal PIN para o cartão: {}. Iniciando validação de segurança dupla.", cardId);

        validateTransactionPinUseCase.execute(accountId, customerId, transactionPin);

        Card card = cardPersistencePort.findByIdAndAccountId(cardId, accountId)
                .orElseThrow(() -> new IllegalArgumentException("Cartão não encontrado ou não pertence a esta conta."));

        if (card.isBlocked()) {
            throw new IllegalStateException("Não é possível exibir a senha de um cartão bloqueado.");
        }
        if (card.type() != CardType.PHYSICAL) {
            throw new IllegalArgumentException("Apenas cartões físicos possuem senha de maquininha.");
        }

        log.info("Validação Concluída! Revelando PIN do Cartão {} para o cliente.", cardId);

        // 4. Integração Simétrica (Em Produção, isso chamaria um KMS ou HSM para decriptar a AES-256).
        // Mock do Decrypt para o laboratório local:
        return "1234";
    }
}
