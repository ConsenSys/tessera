
package com.github.nexus.keyenc;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.nexus.keyenc.KeyGeneratorException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class KeyGeneratorExceptionTest {
    
    public KeyGeneratorExceptionTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    
    @Test
    public void constructWithCause() {
        
        Exception cause = new Exception("Some punk's busted up my ride!!");
        
        KeyGeneratorException keyGeneratorException = new KeyGeneratorException(cause);
        
        assertThat(keyGeneratorException).hasCause(cause);
        
        
    }
    
}
