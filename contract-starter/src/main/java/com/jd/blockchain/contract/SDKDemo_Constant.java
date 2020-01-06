package com.jd.blockchain.contract;

import com.jd.blockchain.crypto.KeyGenUtils;
import com.jd.blockchain.crypto.PrivKey;
import com.jd.blockchain.crypto.PubKey;
import com.jd.blockchain.ledger.BlockchainKeypair;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

public class SDKDemo_Constant {

    //151.39;
//    public static final String GW_IPADDR = "localhost";
//    public static final int GW_PORT = 11000;
//    public static final String GW_PUB_KEY = "3snPdw7i7PjVKiTH2VnXZu5H8QmNaSXpnk4ei533jFpuifyjS5zzH9";
//    public static final String GW_PRIV_KEY = "177gjzHTznYdPgWqZrH43W3yp37onm74wYXT4v9FukpCHBrhRysBBZh7Pzdo5AMRyQGJD7x";
//    //the content of *.pwd file;
//    public static final String GW_PASSWORD = "DYu3G8aGTMBW1WrTw76zxQJQU4DHLw9MLyy7peG4LKkY";

    //localhost;
    public static final String GW_IPADDR = "jdchain-t.jd.com";
    public static final int GW_PORT = 8081;
    public static final String GW_PUB_KEY = "3snPdw7i7Pbdsovw3t87SMNQwcvV5F3VpvVThQiRzzctmRK122AW3k";
    public static final String GW_PRIV_KEY = "177gjudHRcYqKxAEVwj7dzQhZ8Rwev1XU63CTR71PzwfohxJ2HvKd3wNg6yuLw4Vr7bk8Uw";
    public static final String GW_PASSWORD = "DYu3G8aGTMBW1WrTw76zxQJQU4DHLw9MLyy7peG4LKkY";

    public static PrivKey gwPrivkey0 = KeyGenUtils.decodePrivKey(GW_PRIV_KEY, GW_PASSWORD);
    public static PubKey gwPubKey0 = KeyGenUtils.decodePubKey(GW_PUB_KEY);
    public static BlockchainKeypair adminKey = new BlockchainKeypair(gwPubKey0, gwPrivkey0);

    public static final byte[] readChainCodes(String contractZip) {
        // 构建合约的字节数组;
        try {
            ClassPathResource contractPath = new ClassPathResource(contractZip);
            File contractFile = new File(contractPath.getURI());
            return FileUtils.readFileToByteArray(contractFile);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
