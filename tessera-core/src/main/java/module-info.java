module tessera.transaction {
  requires org.slf4j;
  requires tessera.config;
  requires tessera.enclave.api;
  requires tessera.encryption.api;
  requires tessera.shared;
  requires tessera.data;
  // requires tessera.partyinfo;
  requires tessera.context;

  exports com.quorum.tessera.transaction;
  exports com.quorum.tessera.transaction.exception;
  exports com.quorum.tessera.transaction.publish;
  exports com.quorum.tessera.privacygroup;
  exports com.quorum.tessera.privacygroup.exception;
  exports com.quorum.tessera.privacygroup.publish;

  uses com.quorum.tessera.transaction.publish.PayloadPublisher;
  uses com.quorum.tessera.transaction.TransactionManager;
  uses com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
  uses com.quorum.tessera.transaction.EncodedPayloadManager;
  uses com.quorum.tessera.transaction.resend.ResendManager;
  uses com.quorum.tessera.transaction.PrivacyHelper;
  uses com.quorum.tessera.privacygroup.PrivacyGroupManager;
  uses com.quorum.tessera.privacygroup.publish.BatchPrivacyGroupPublisher;
  uses com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;

  provides com.quorum.tessera.transaction.TransactionManager with
      com.quorum.tessera.transaction.internal.TransactionManagerProvider;
  provides com.quorum.tessera.transaction.EncodedPayloadManager with
      com.quorum.tessera.transaction.internal.EncodedPayloadManagerProvider;
  provides com.quorum.tessera.transaction.PrivacyHelper with
      com.quorum.tessera.transaction.internal.PrivacyHelperProvider;
  provides com.quorum.tessera.transaction.resend.ResendManager with
      com.quorum.tessera.transaction.resend.internal.ResendManagerProvider;
  provides com.quorum.tessera.privacygroup.PrivacyGroupManager with
      com.quorum.tessera.privacygroup.internal.PrivacyGroupManagerProvider;
  provides com.quorum.tessera.privacygroup.ResidentGroupHandler with
      com.quorum.tessera.privacygroup.internal.ResidentGroupHandlerProvider;
}
