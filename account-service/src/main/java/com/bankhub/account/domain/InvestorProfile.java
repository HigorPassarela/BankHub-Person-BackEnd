package com.bankhub.account.domain;

public enum InvestorProfile {
    PENDING,        // Cliente ainda não conversou com a IA
    CONSERVATIVE,   // Foco em CDBs e Tesouro Direto
    MODERATE,       // Mescla Renda Fixa com Fundos Imobiliários (FIIs)
    AGGRESSIVE      // Foco agressivo em Ações (STOCK) e alto risco
}
