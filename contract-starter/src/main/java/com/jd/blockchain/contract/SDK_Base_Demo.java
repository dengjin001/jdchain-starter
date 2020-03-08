package com.jd.blockchain.contract;

import com.jd.blockchain.crypto.HashDigest;
import com.jd.blockchain.ledger.*;
import com.jd.blockchain.sdk.BlockchainService;
import com.jd.blockchain.sdk.client.GatewayServiceFactory;

public abstract class SDK_Base_Demo {

    protected BlockchainKeypair adminKey;

    protected HashDigest ledgerHash;

    protected BlockchainService blockchainService;

    public SDK_Base_Demo() {
        init();
    }

    public void init() {
        // 生成连接网关的账号
        adminKey = SDKDemo_Constant.adminKey;

        // 连接网关
        GatewayServiceFactory serviceFactory = GatewayServiceFactory.connect(SDKDemo_Constant.GW_IPADDR,
                SDKDemo_Constant.GW_PORT, false, adminKey);

        // 获取网关对应的Service处理类
        blockchainService = serviceFactory.getBlockchainService();

        HashDigest[] ledgerHashs = blockchainService.getLedgerHashs();
        // 获取当前账本Hash
        ledgerHash = ledgerHashs[0];
    }

    public TransactionResponse commit(TransactionTemplate txTpl) {
        return this.commit(txTpl,null);
    }

    public TransactionResponse commit(TransactionTemplate txTpl, BlockchainKeypair signAdminKey) {
        PreparedTransaction ptx = txTpl.prepare();
        if(signAdminKey != null){
            System.out.println("signAdminKey's pubKey = "+signAdminKey.getIdentity().getPubKey());
            ptx.sign(signAdminKey);
        }else {
            System.out.println("adminKey's pubKey = "+adminKey.getIdentity().getPubKey());
            ptx.sign(adminKey);
        }
        TransactionResponse transactionResponse = ptx.commit();
        if (transactionResponse.isSuccess()) {
            System.out.println(String.format("height=%d, ###OK#, contentHash=%s, executionState=%s",
                    transactionResponse.getBlockHeight(),
                    transactionResponse.getContentHash(), transactionResponse.getExecutionState().toString()));
        } else {
            System.out.println(String.format("height=%d, ###exception#, contentHash=%s, executionState=%s",
                    transactionResponse.getBlockHeight(),
                    transactionResponse.getContentHash(), transactionResponse.getExecutionState().toString()));
        }
        return transactionResponse;
    }

    /**
     * 生成一个区块链用户，并注册到区块链；
     */
    public BlockchainKeypair registerUser() {
        return this.registerUser(null,null);
    }

    public BlockchainKeypair registerUser(BlockchainKeypair signAdminKey, BlockchainKeypair userKeypair) {
        // 在本地定义注册账号的 TX；
        TransactionTemplate txTemp = blockchainService.newTransaction(ledgerHash);
        if(userKeypair == null){
            userKeypair = BlockchainKeyGenerator.getInstance().generate();
        }
        System.out.println("user'address="+userKeypair.getAddress());
        txTemp.users().register(userKeypair.getIdentity());
        // TX 准备就绪；
        commit(txTemp,signAdminKey);
        return userKeypair;
    }

    /**
     * 生成一个区块链用户，并注册到区块链；
     */
    public BlockchainKeypair registerUserByNewSigner(BlockchainKeypair signer) {
        return this.registerUser(signer,null);
    }
}
