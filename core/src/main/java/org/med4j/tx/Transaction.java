package org.med4j.tx;

import com.google.protobuf.ByteString;
import org.med4j.account.Account;
import org.med4j.core.protobuf.BlockChain;
import org.med4j.core.protobuf.BlockChain.TransactionHashTarget;
import org.med4j.core.protobuf.Rpc.SendTransactionRequest;
import org.med4j.crypto.Hash;
import org.med4j.healthdata.HealthData;
import org.med4j.utils.Numeric;

import java.util.Calendar;

public class Transaction {
    static final int VALUE_SIZE = 16;

    private class Type {
        static final String ADD_RECORD = "add_record";
    }

    private class Algorithm {
        static final int SECP256K1 = 1;
        static final int SHA3256 = 2;
    }

    public static String hashTx(TransactionHashTarget hashTarget) {
        byte[] bytes = hashTarget.toByteArray();
        return Numeric.byteArrayToHex(Hash.sha3256(bytes));
    }

    public static SendTransactionRequest getSendTransactionRequest(byte[] data, Account account, String password, long timeStamp, long nonce, int chainId) {
        timeStamp = timeStamp != 0 ? timeStamp : getCurrentTimeInSecond();
        byte[] dataHash = HealthData.hashData(data);

        BlockChain.AddRecordPayload payload = BlockChain.AddRecordPayload.newBuilder()
                .setHash(ByteString.copyFrom(dataHash))
                .build();

        BlockChain.TransactionHashTarget txHashTarget = BlockChain.TransactionHashTarget.newBuilder()
                .setTxType(Type.ADD_RECORD)
                .setFrom(ByteString.copyFrom(Numeric.hexStringToByteArray(account.getAddress())))
                .setTo(ByteString.copyFrom(new byte[33])) // default value
                .setValue(ByteString.copyFrom(new byte[16])) // default value
                .setTimestamp(timeStamp)
                .setNonce(nonce) // TODO : thread safe
                .setChainId(chainId) // TODO : read from config
                .setPayload(ByteString.copyFrom(payload.toByteArray()))
                .build();

        String sign = ""; // TODO

        return getTxRequestBuilderWithValuesOf(txHashTarget)
                .setHash(hashTx(txHashTarget))
                .setHashAlg(Algorithm.SHA3256)
                .setCryptoAlg(Algorithm.SECP256K1)
                //.setPayerSign(null)
                .setSign(sign)
                .build();

        // TODO : implement send()
    }

    private static long getCurrentTimeInSecond() {
        return Calendar.getInstance().getTimeInMillis() / 1000;
    }

    private static SendTransactionRequest.Builder getTxRequestBuilderWithValuesOf(BlockChain.TransactionHashTarget txHashTarget) {
        return SendTransactionRequest.newBuilder()
                .setChainId(txHashTarget.getChainId())
                .setNonce(txHashTarget.getNonce())
                .setTimestamp(txHashTarget.getTimestamp())
                .setPayload(Numeric.toHexStringNoPrefix(txHashTarget.getPayload().toByteArray()))
                .setTo(Numeric.toHexStringNoPrefix(txHashTarget.getTo().toByteArray()))
                .setTxType(txHashTarget.getTxType())
                .setValue(Numeric.toHexStringNoPrefix(txHashTarget.getValue().toByteArray()));
    }
}
