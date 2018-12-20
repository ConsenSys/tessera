package com.quorum.tessera.node;

import com.quorum.tessera.client.P2pClient;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.ConnectException;
import java.net.URI;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class PartyInfoPollerTest {

    private static final URI OWN_URL = URI.create("http://own.com:8080");

    private static final URI TARGET_URL = URI.create("http://bogus.com:9878");

    private static final byte[] RESPONSE = "BOGUS".getBytes();

    private PartyInfoService partyInfoService;

    private PartyInfoParser partyInfoParser;

    private PartyInfoPoller partyInfoPoller;

    private P2pClient p2pClient;

    @Before
    public void setUp() {
        this.partyInfoService = mock(PartyInfoService.class);
        this.partyInfoParser = mock(PartyInfoParser.class);
        this.p2pClient = mock(P2pClient.class);

        this.partyInfoPoller = new PartyInfoPoller(partyInfoService, partyInfoParser, p2pClient);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(partyInfoService, partyInfoParser, p2pClient);
    }

    @Test
    public void run() {

        doReturn(RESPONSE).when(p2pClient).getPartyInfo(TARGET_URL, RESPONSE);

        final PartyInfo partyInfo = new PartyInfo(OWN_URL.toString(), emptySet(), singleton(new Party(TARGET_URL)));
        doReturn(partyInfo).when(partyInfoService).getPartyInfo();

        doReturn(RESPONSE).when(partyInfoParser).to(partyInfo);

        final PartyInfo updatedPartyInfo
            = new PartyInfo(OWN_URL.toString(), emptySet(), singleton(new Party(TARGET_URL)));
        doReturn(updatedPartyInfo).when(partyInfoParser).from(RESPONSE);

        partyInfoPoller.run();

        verify(partyInfoService).getPartyInfo();
        verify(partyInfoService).updatePartyInfo(updatedPartyInfo);

        verify(partyInfoParser).from(RESPONSE);
        verify(partyInfoParser).to(partyInfo);

        verify(p2pClient).getPartyInfo(TARGET_URL, RESPONSE);
    }

    @Test
    public void testWhenURLIsOwn() {

        doReturn(RESPONSE).when(p2pClient).getPartyInfo(OWN_URL, RESPONSE);

        final PartyInfo partyInfo
            = new PartyInfo(OWN_URL.toString(), emptySet(), singleton(new Party(OWN_URL)));
        doReturn(partyInfo).when(partyInfoService).getPartyInfo();
        doReturn(RESPONSE).when(partyInfoParser).to(partyInfo);

        final PartyInfo updatedPartyInfo = mock(PartyInfo.class);
        doReturn(updatedPartyInfo).when(partyInfoParser).from(RESPONSE);

        partyInfoPoller.run();

        verify(partyInfoParser).to(partyInfo);
        verify(partyInfoService).getPartyInfo();
    }

    @Test
    public void testWhenPostFails() {

        doReturn(null).when(p2pClient).getPartyInfo(TARGET_URL, RESPONSE);

        final PartyInfo partyInfo = new PartyInfo(OWN_URL.toString(), emptySet(), singleton(new Party(TARGET_URL)));

        doReturn(partyInfo).when(partyInfoService).getPartyInfo();
        doReturn(RESPONSE).when(partyInfoParser).to(partyInfo);

        final PartyInfo updatedPartyInfo = mock(PartyInfo.class);
        doReturn(updatedPartyInfo).when(partyInfoParser).from(RESPONSE);

        partyInfoPoller.run();

        verify(partyInfoParser, never()).from(RESPONSE);
        verify(partyInfoParser).to(partyInfo);
        verify(partyInfoService).getPartyInfo();
        verify(p2pClient).getPartyInfo(TARGET_URL, RESPONSE);
    }

    @Test
    public void runThrowsException() {

        final PartyInfo partyInfo = new PartyInfo(OWN_URL.toString(), emptySet(), singleton(new Party(TARGET_URL)));

        doReturn(partyInfo).when(partyInfoService).getPartyInfo();
        doReturn(RESPONSE).when(partyInfoParser).to(partyInfo);

        PartyInfo updatedPartyInfo = mock(PartyInfo.class);
        doReturn(updatedPartyInfo).when(partyInfoParser).from(RESPONSE);

        doThrow(UnsupportedOperationException.class).when(p2pClient).getPartyInfo(TARGET_URL, RESPONSE);

        final Throwable throwable = catchThrowable(partyInfoPoller::run);
        assertThat(throwable).isInstanceOf(UnsupportedOperationException.class);

        verify(p2pClient).getPartyInfo(TARGET_URL, RESPONSE);

        verify(partyInfoService).getPartyInfo();
        verify(partyInfoService, never()).updatePartyInfo(updatedPartyInfo);
        verify(partyInfoParser, never()).from(RESPONSE);
        verify(partyInfoParser).to(partyInfo);
    }

    @Test
    public void runThrowsConnectionExceptionAndDoesNotThrow() {

        final PartyInfo partyInfo = new PartyInfo(OWN_URL.toString(), emptySet(), singleton(new Party(TARGET_URL)));

        doReturn(partyInfo).when(partyInfoService).getPartyInfo();
        doReturn(RESPONSE).when(partyInfoParser).to(partyInfo);

        final PartyInfo updatedPartyInfo = mock(PartyInfo.class);
        doReturn(updatedPartyInfo).when(partyInfoParser).from(RESPONSE);

        final RuntimeException connectionException = new RuntimeException(new ConnectException("OUCH"));
        doThrow(connectionException).when(p2pClient).getPartyInfo(TARGET_URL, RESPONSE);

        partyInfoPoller.run();

        verify(p2pClient).getPartyInfo(TARGET_URL, RESPONSE);

        verify(partyInfoService).getPartyInfo();
        verify(partyInfoService, never()).updatePartyInfo(updatedPartyInfo);
        verify(partyInfoParser, never()).from(RESPONSE);
        verify(partyInfoParser).to(partyInfo);
    }

}
