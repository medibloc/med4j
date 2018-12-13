package org.med4j.core;

import com.google.protobuf.ByteString;
import org.junit.Test;
import org.med4j.account.Account;
import org.med4j.account.AccountUtils;
import org.med4j.core.protobuf.BlockChain;
import org.med4j.core.protobuf.Rpc;
import org.med4j.tx.Transaction;
import org.med4j.utils.Numeric;
import org.med4j.utils.Strings;

import java.io.File;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class TransactionTest {

    private ByteString hexToByteString(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return ByteString.copyFrom(data);
    }

    private ByteString getAddress(String address) {
        int targetLength = 33 * 2;
        if (address.length() < targetLength) {
            int zeros = targetLength - address.length();
            address = String.format("%0" + zeros + "d", 0) + address;
        }
        return hexToByteString(address);
    }

    private ByteString getValue(String value) {
        // TODO VALUE_SIZE
        return hexToByteString(Numeric.toHexStringZeroPadded(new BigInteger(value), 16 * 2));
    }

    @Test
    public void testHashTx() throws Exception {
        BlockChain.TransactionHashTarget.Builder builder = BlockChain.TransactionHashTarget.newBuilder();
        builder.setChainId(180830);
        builder.setNonce(1);

        String to = "02bd4879f148079dee2bd096248ef3c4432ec1899681af4bdae2aa6a7451c72c7b";
        builder.setTo(getAddress(to));
        String from = "03349913aad7662ff63e3d200680a1773085184ccf34eca9022e76eabb53d55c98";
        builder.setFrom(getAddress(from));
        assertEquals(Numeric.byteArrayToHex(builder.getTo().toByteArray()), to);
        assertEquals(Numeric.byteArrayToHex(builder.getFrom().toByteArray()), from);

        builder.setTimestamp(1542702990085L);
        builder.setValue(getValue("1"));
        builder.setPayload(ByteString.EMPTY);
        builder.setTxType("transfer");
        String hash = Transaction.hashTx(builder.build());
        System.out.println(hash);
    }

    @Test
    public void testGetSendTransactionRequest() throws Exception {
        Account account = AccountUtils.loadAccount(new File("sampleAccount.json"));

        Rpc.SendTransactionRequest expected
                = Rpc.SendTransactionRequest.newBuilder()
                .setHashAlg(2)
                .setHash("14700682751b209f89551b809a09161a9d5d11a0b9f2e1c9c82a2fb0b13b4a03")
                .setChainId(181112)
                .setCryptoAlg(1)
                .setNonce(1)
                .setTimestamp(1540000000)
                //.setPayerSign(null)
                .setPayload("0a206f6f129471590d2c91804c812b5750cd44cbdfb7238541c451e1ea2bc0193177")
                .setSign("b41027c77b0b41c617a15101ffa23924f29cb9519627162ee4da95db04b975ad412fce876aed3a6c183d678971e6c4dcf3aaafc512311c578bb323d9b5db17fc01")
                .setTo(Strings.zeros(33*2)) // default value
                .setValue(Strings.zeros(16*2)) // default value
                .setTxType("add_record")
                .build();

        Rpc.SendTransactionRequest actual = Transaction.getSendTransactionRequest("abcd".getBytes(), account, "sample", 1540000000, 1, 181112);

        assertEquals(expected, actual);
    }
}
