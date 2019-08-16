package com.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.PartyInfoValidatorCallback;
import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.partyinfo.ResendRequestType;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.transaction.TransactionManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;

import org.junit.Test;
import static org.mockito.Mockito.*;

public class PartyInfoEndpointTest {

    private PartyInfoEndpoint partyInfoEndpoint;

    private Session session;

    private PartyInfoService partyInfoService;

    private TransactionManager transactionManager;

    @Before
    public void onSetUp() {

        partyInfoService = mock(PartyInfoService.class);

        transactionManager = mock(TransactionManager.class);

        partyInfoEndpoint = new PartyInfoEndpoint(partyInfoService, transactionManager);
        session = mock(Session.class);
        when(session.getId()).thenReturn(UUID.randomUUID().toString());
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService, transactionManager);
    }

    @Test
    public void onSyncPartyInfo() throws Exception {

        PartyInfo partyInfo = Fixtures.samplePartyInfo();

        when(partyInfoService.validateAndExtractValidRecipients(
                        any(PartyInfo.class), any(PartyInfoValidatorCallback.class)))
                .thenReturn(partyInfo.getRecipients());
        when(partyInfoService.updatePartyInfo(any(PartyInfo.class))).thenReturn(partyInfo);

        SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.PARTY_INFO).withPartyInfo(partyInfo).build();

        Basic basic = mock(Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);

        partyInfoEndpoint.onOpen(session);
        partyInfoEndpoint.onSync(session, syncRequestMessage);
        partyInfoEndpoint.onClose(session);

        verify(basic).sendObject(any(SyncResponseMessage.class));
        verify(partyInfoService).updatePartyInfo(any(PartyInfo.class));
        verify(partyInfoService).getPartyInfo();
        verify(partyInfoService)
                .validateAndExtractValidRecipients(any(PartyInfo.class), any(PartyInfoValidatorCallback.class));
    }

    @Test
    public void onSyncTransactions() throws Exception {

        PublicKey recipientKey = Fixtures.sampleKey();

        SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.TRANSACTION_SYNC)
                        .withRecipientKey(recipientKey)
                        .build();

        List<ResendRequest> requests = new ArrayList<>();
        doAnswer(
                        (iom) -> {
                            requests.add(iom.getArgument(0));
                            return null;
                        })
                .when(transactionManager)
                .resend(any(ResendRequest.class));

        partyInfoEndpoint.onSync(session, syncRequestMessage);

        assertThat(requests).hasSize(1);

        ResendRequest result = requests.get(0);
        assertThat(result.getType()).isEqualTo(ResendRequestType.ALL);
        assertThat(result.getPublicKey()).isEqualTo(recipientKey.encodeToBase64());

        verify(transactionManager).resend(any(ResendRequest.class));
    }

    @Test
    public void onSyncTransactionPush() throws Exception {

        EncodedPayload transactions = Fixtures.samplePayload();

        SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.TRANSACTION_PUSH)
                        .withTransactions(transactions)
                        .build();

        partyInfoEndpoint.onSync(session, syncRequestMessage);

        byte[] expectedData = PayloadEncoder.create().encode(transactions);
        verify(transactionManager).storePayload(expectedData);
    }

    @Test
    public void onError() {
        partyInfoEndpoint.onError(new Exception("Ouch"));
    }

    @Test
    public void onSyncPartyInfoNoInfoProvided() throws Exception {

        PartyInfo partyInfo = Fixtures.samplePartyInfo();

        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);
        when(partyInfoService.updatePartyInfo(any(PartyInfo.class))).thenReturn(partyInfo);
        when(partyInfoService.validateAndExtractValidRecipients(
                        any(PartyInfo.class), any(PartyInfoValidatorCallback.class)))
                .thenReturn(partyInfo.getRecipients());

        SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.PARTY_INFO).build();

        Basic basic = mock(Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);

        partyInfoEndpoint.onOpen(session);
        partyInfoEndpoint.onSync(session, syncRequestMessage);
        partyInfoEndpoint.onClose(session);

        verify(basic).sendObject(any(SyncResponseMessage.class));
        verify(partyInfoService).getPartyInfo();
        verify(partyInfoService).updatePartyInfo(any(PartyInfo.class));
        verify(partyInfoService)
                .validateAndExtractValidRecipients(any(PartyInfo.class), any(PartyInfoValidatorCallback.class));
    }

    @Test
    public void onSyncNoValidRecipient() throws Exception {

        PartyInfo partyInfo = Fixtures.samplePartyInfo();

        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        when(partyInfoService.validateAndExtractValidRecipients(
                        any(PartyInfo.class), any(PartyInfoValidatorCallback.class)))
                .thenReturn(Collections.EMPTY_SET);

        SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.PARTY_INFO).build();

        try {
            partyInfoEndpoint.onOpen(session);
            partyInfoEndpoint.onSync(session, syncRequestMessage);
            partyInfoEndpoint.onClose(session);
            failBecauseExceptionWasNotThrown(SecurityException.class);
        } catch (SecurityException ex) {

            verify(partyInfoService).getPartyInfo();

            verify(partyInfoService)
                    .validateAndExtractValidRecipients(any(PartyInfo.class), any(PartyInfoValidatorCallback.class));
        }
    }
}
