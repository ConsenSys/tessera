package com.quorum.tessera.enclave.rest;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EnclaveFindInvalidSecurityHashesResponsePayload implements Serializable {

  @XmlMimeType("base64Binary")
  private List<byte[]> invalidSecurityHashes;

  public List<byte[]> getInvalidSecurityHashes() {
    return invalidSecurityHashes;
  }

  public void setInvalidSecurityHashes(List<byte[]> invalidSecurityHashes) {
    this.invalidSecurityHashes = invalidSecurityHashes;
  }
}
