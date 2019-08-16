
package com.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.PartyInfoService;
import java.io.IOException;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PartyInfoValidationEndpointTest {
    private PartyInfoValidationEndpoint partyInfoValidationEndpoint;
    
    private PartyInfoService partyInfoService;
    
    @Before
    public void onSetUp() {
        partyInfoService = mock(PartyInfoService.class);
        partyInfoValidationEndpoint = new PartyInfoValidationEndpoint(partyInfoService);
    }
    
    @Test
    public void unencryptSampleData() throws IOException {
        Session session = mock(Session.class);
        Basic basic = mock(Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);
        
        byte[] data = "HEllow".getBytes();
        
        byte[] unencrypted = "Goodbye".getBytes();
        
        when(partyInfoService.unencryptSampleData(data)).thenReturn(unencrypted);
        
        partyInfoValidationEndpoint.unencryptSampleData(session, data);
        
        verify(session).getBasicRemote();
        verify(basic).sendText("Goodbye");
        
    }
}
