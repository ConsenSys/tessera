module tessera.partyinfo.jaxrs {
  requires java.json;
  requires java.annotation;
  requires java.validation;
  requires java.ws.rs;
  requires org.slf4j;
  requires tessera.config;
  requires tessera.enclave.api;
  requires tessera.encryption.api;
  requires tessera.security;
  requires tessera.shared;
  requires tessera.context;
  requires tessera.transaction;
  requires tessera.data;
  requires tessera.common.jaxrs;
  requires tessera.jaxrs.client;
  requires tessera.partyinfo;
  requires org.apache.commons.lang3;
  requires tessera.partyinfo.model;
  requires tessera.recovery;
  requires io.swagger.v3.oas.annotations;
  requires jakarta.inject;
  requires java.servlet;

  exports com.quorum.tessera.p2p;
  exports com.quorum.tessera.p2p.resend;
  exports com.quorum.tessera.p2p.partyinfo;
  exports com.quorum.tessera.p2p.recovery;

  opens com.quorum.tessera.p2p.recovery to
      org.eclipse.persistence.moxy,
      org.eclipse.persistence.core;
  opens com.quorum.tessera.p2p.resend to
      org.eclipse.persistence.moxy,
      org.eclipse.persistence.core,
      org.hibernate.validator;

  uses com.quorum.tessera.p2p.recovery.RecoveryClient;
  uses com.quorum.tessera.p2p.resend.ResendClient;
  uses com.quorum.tessera.p2p.resend.TransactionRequester;
  uses com.quorum.tessera.p2p.resend.ResendPartyStore;

  provides com.quorum.tessera.config.apps.TesseraApp with
      com.quorum.tessera.p2p.P2PRestApp;
  provides com.quorum.tessera.recovery.resend.BatchTransactionRequester with
      com.quorum.tessera.p2p.recovery.BatchTransactionRequesterProvider;
  provides com.quorum.tessera.recovery.resend.ResendBatchPublisher with
      com.quorum.tessera.p2p.recovery.ResendBatchPublisherProvider;
  provides com.quorum.tessera.p2p.resend.ResendClient with
      com.quorum.tessera.p2p.resend.ResendClientProvider;
  provides com.quorum.tessera.p2p.recovery.RecoveryClient with
      com.quorum.tessera.p2p.recovery.RecoveryClientProvider;
  provides com.quorum.tessera.p2p.resend.TransactionRequester with
      com.quorum.tessera.p2p.resend.TransactionRequesterProvider;
  provides com.quorum.tessera.partyinfo.P2pClient with
      com.quorum.tessera.p2p.partyinfo.P2pClientProvider;
  provides com.quorum.tessera.p2p.resend.ResendPartyStore with
      com.quorum.tessera.p2p.resend.ResendPartyStoreImpl;
}
