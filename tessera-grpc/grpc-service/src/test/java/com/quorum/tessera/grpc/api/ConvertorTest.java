package com.quorum.tessera.grpc.api;

import com.google.protobuf.ByteString;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class ConvertorTest {

    @Test
    public void constructUnSupported() throws Exception {
        Constructor c = Convertor.class.getDeclaredConstructor();
        c.setAccessible(true);
        try {
            c.newInstance();
            failBecauseExceptionWasNotThrown(InvocationTargetException.class);
        } catch (InvocationTargetException invocationTargetException) {
            assertThat(invocationTargetException).hasCauseExactlyInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Test
    public void toGrpcReceiveRequest() {

        String key = "Some Key";
        String to = "Mr Benn";

        com.quorum.tessera.api.model.ReceiveRequest receiveRequest = new com.quorum.tessera.api.model.ReceiveRequest();

        receiveRequest.setKey(key);
        receiveRequest.setTo(to);

        ReceiveRequest result = Convertor.toGrpc(receiveRequest);

        assertThat(result).isNotNull();

        assertThat(result.getKey()).isEqualTo(key);
        assertThat(result.getTo()).isEqualTo(to);
    }

    @Test
    public void toModelReceiveRequest() {

        String key = "Some Key";
        String to = "Mr Benn";

        ReceiveRequest receiveRequest = ReceiveRequest.newBuilder().setKey(key).setTo(to).build();

        com.quorum.tessera.api.model.ReceiveRequest result = Convertor.toModel(receiveRequest);

        assertThat(result).isNotNull();

        assertThat(result.getKey()).isEqualTo(key);
        assertThat(result.getTo()).isEqualTo(to);
    }

    @Test
    public void toGrpcSendRequest() {

        com.quorum.tessera.api.model.SendRequest sendRequest = new com.quorum.tessera.api.model.SendRequest();
        sendRequest.setFrom("FROM");
        sendRequest.setPayload("PAYLOAD".getBytes());
        sendRequest.setTo(new String[] {"TO1"});
        sendRequest.setExecHash("execHash");
        sendRequest.setAffectedContractTransactions(new String[] {"acoth1"});

        SendRequest result = Convertor.toGrpc(sendRequest);
        assertThat(result.getFrom()).isEqualTo("FROM");
        assertThat(result.getPayload().toStringUtf8()).isEqualTo("PAYLOAD");
        assertThat(result.getExecHash()).isEqualTo("execHash");
        assertThat(result.getToList()).containsExactly("TO1");
        assertThat(result.getAffectedContractTransactionsList()).containsExactly("acoth1");
    }

    @Test
    public void toGrpcSendSignedRequest() {

        com.quorum.tessera.api.model.SendSignedRequest sendSignedRequest =
                new com.quorum.tessera.api.model.SendSignedRequest();
        sendSignedRequest.setHash("HASH".getBytes());
        sendSignedRequest.setTo(new String[] {"TO1"});
        sendSignedRequest.setExecHash("execHash");
        sendSignedRequest.setAffectedContractTransactions(new String[] {"acoth1"});

        SendSignedRequest result = Convertor.toGrpc(sendSignedRequest);
        assertThat(result.getHash().toStringUtf8()).isEqualTo("HASH");
        assertThat(result.getExecHash()).isEqualTo("execHash");
        assertThat(result.getToList()).containsExactly("TO1");
        assertThat(result.getAffectedContractTransactionsList()).containsExactly("acoth1");
    }

    @Test
    public void toModelSendRequest() {
        SendRequest grpcSendRequest =
                SendRequest.newBuilder()
                        .setFrom("FROM")
                        .setPayload(ByteString.copyFromUtf8("PAYLOAD"))
                        .addTo("TO1")
                        .addTo("TO2")
                        .setExecHash("execHash")
                        .addAffectedContractTransactions("acoth1")
                        .build();

        com.quorum.tessera.api.model.SendRequest result = Convertor.toModel(grpcSendRequest);
        assertThat(result).isNotNull();
        assertThat(result.getTo()).containsExactly("TO1", "TO2");
        assertThat(result.getAffectedContractTransactions()).containsExactly("acoth1");
        assertThat(result.getExecHash()).isEqualTo("execHash");
        assertThat(result.getFrom()).isEqualTo("FROM");
        assertThat(result.getPayload()).isEqualTo("PAYLOAD".getBytes());
    }

    @Test
    public void toModelSendSignedRequest() {
        SendSignedRequest grpcSendSignedRequest =
                SendSignedRequest.newBuilder()
                        .setHash(ByteString.copyFromUtf8("HASH"))
                        .addTo("TO1")
                        .addTo("TO2")
                        .setExecHash("execHash")
                        .addAffectedContractTransactions("acoth1")
                        .build();

        com.quorum.tessera.api.model.SendSignedRequest result = Convertor.toModel(grpcSendSignedRequest);
        assertThat(result).isNotNull();
        assertThat(result.getTo()).containsExactly("TO1", "TO2");
        assertThat(result.getAffectedContractTransactions()).containsExactly("acoth1");
        assertThat(result.getExecHash()).isEqualTo("execHash");
        assertThat(result.getHash()).isEqualTo("HASH".getBytes());
    }

    @Test
    public void toModelSendRequestEmptyFromField() {
        SendRequest grpcSendRequest =
                SendRequest.newBuilder().setPayload(ByteString.copyFromUtf8("PAYLOAD")).addTo("TO1").build();

        com.quorum.tessera.api.model.SendRequest result = Convertor.toModel(grpcSendRequest);
        assertThat(result).isNotNull();
        assertThat(result.getTo()).containsExactly("TO1");
        assertThat(result.getFrom()).isNull();
        assertThat(result.getPayload()).isEqualTo("PAYLOAD".getBytes());
    }
}
