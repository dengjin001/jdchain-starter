package com.jd.blockchain;

import com.jd.blockchain.contract.SDK_Base_Demo;
import com.jd.blockchain.crypto.KeyGenUtils;
import com.jd.blockchain.crypto.PrivKey;
import com.jd.blockchain.crypto.PubKey;
import com.jd.blockchain.ledger.*;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author zhaogw
 * date 2019/9/18 16:23
 */
public class PermissionTest extends SDK_Base_Demo {
    /**
     * 新增加一个角色，不能操作合约；
     */
    @Test
    public void executeRoleConfig_sign() {
        // 定义交易模板
        TransactionTemplate txTpl = blockchainService.newTransaction(ledgerHash);
        txTpl.security().roles().configure("SIGN1")
                .enable(LedgerPermission.APPROVE_TX, LedgerPermission.CONSENSUS_TX)
                .disable(TransactionPermission.CONTRACT_OPERATION);
        TransactionResponse txResp = commit(txTpl);
        System.out.println(txResp.isSuccess());
    }

    /**
     * admin赋权给newUser,can register user;
     */
    @Test
    public void registerUser_cannotRegisterUser() {
        //add the role:MANAGERX
        executeRoleConfig_cannot_registerUser();

        // 在本地定义注册账号的 TX；
        TransactionTemplate txTemp = blockchainService.newTransaction(ledgerHash);
        BlockchainKeypair newUser = registerUser();

//        txTemp.security().authorziations().forUser(newUser.getAddress()).authorize("MANAGER2").setPolicy(RolesPolicy.UNION).authorize("MANAGER2");
        txTemp.security().authorziations().forUser(newUser.getAddress()).unauthorize("DEFAULT").authorize("NORMAL");

        // TX 准备就绪；
        PreparedTransaction prepTx = txTemp.prepare();
        prepTx.sign(adminKey);

        // 提交交易；
        TransactionResponse response = prepTx.commit();
        Assert.assertTrue(response.isSuccess());

        //then sign by newUser;
        // 在本地定义注册账号的 TX；
        registerUserByNewSigner(newUser);
    }

    /**
     * 新增加一个角色
     */
    @Test
    public void executeRoleConfig_manager2() {
        // 定义交易模板
        TransactionTemplate txTpl = blockchainService.newTransaction(ledgerHash);
        // 定义角色权限；
        txTpl.security().roles().configure("MANAGER2")
                .enable(LedgerPermission.REGISTER_USER)
                .enable(LedgerPermission.CONFIGURE_ROLES)
                .enable(LedgerPermission.REGISTER_DATA_ACCOUNT)
                .enable(LedgerPermission.WRITE_DATA_ACCOUNT)
//                .disable(LedgerPermission.REGISTER_USER)
                .enable(TransactionPermission.DIRECT_OPERATION);

        TransactionResponse txResp = commit(txTpl);
        System.out.println(txResp.isSuccess());
    }

    /**
     * admin赋权给newUser,can register user;
     */
    @Test
    public void registerUser_canRegisterUser() {
        // 在本地定义注册账号的 TX；
        TransactionTemplate txTemp = blockchainService.newTransaction(ledgerHash);
        BlockchainKeypair newUser = registerUser();

        // 定义角色权限；
        txTemp.security().roles().configure("MANAGER1")
                .enable(LedgerPermission.REGISTER_USER, LedgerPermission.REGISTER_DATA_ACCOUNT)
                .enable(TransactionPermission.CONTRACT_OPERATION);
        txTemp.security().authorziations().forUser(newUser.getIdentity()).authorize("MANAGER1");

        // TX 准备就绪；
        PreparedTransaction prepTx = txTemp.prepare();
        prepTx.sign(adminKey);

        // 提交交易；
        prepTx.commit();

        //then sign by newUser;
        // 在本地定义注册账号的 TX；
        registerUserByNewSigner(newUser);
    }


    /**
     * 新增加一个角色，不能注册用户；
     */
    @Test
    public void executeRoleConfig_cannot_registerUser() {
        // 定义交易模板
        TransactionTemplate txTpl = blockchainService.newTransaction(ledgerHash);
        // 定义角色权限；
        txTpl.security().roles().configure("NORMAL")
                .enable(LedgerPermission.REGISTER_DATA_ACCOUNT)
                .disable(LedgerPermission.REGISTER_USER)
                .enable(TransactionPermission.DIRECT_OPERATION)
                .enable(TransactionPermission.CONTRACT_OPERATION);

        TransactionResponse txResp = commit(txTpl);
        System.out.println(txResp.isSuccess());
    }

    /**
     * 针对系统中已经注册的用户进行赋权;
     */
    @Test
    public void addPermission4ExistUser() {
        // 在本地定义注册账号的 TX；
        TransactionTemplate txTemp = blockchainService.newTransaction(ledgerHash);
//        BlockchainKeypair user = BlockchainKeyGenerator.getInstance().generate();
        /**
         * 使用已知的用户构建一个keypair;
         pubKey=3snPdw7i7PdktosF4iYd7dSeumq6hpLnwtrRCLQWkpdpntMxABvJdZ
         privKey=177gjzfkJHt8BMxDM44Zik1KKr9bpxQXyBdy7cS4wJqFGyrnyPhqApaKpYqcka3NMJJ1M5V
         base58PwdKey=8EjkXVSTxMFjCvNNsTo8RBMDEVQmk7gYkW4SCDuvdsBG
         */

        PrivKey privKey = KeyGenUtils.decodePrivKey("177gjzfkJHt8BMxDM44Zik1KKr9bpxQXyBdy7cS4wJqFGyrnyPhqApaKpYqcka3NMJJ1M5V",
                "8EjkXVSTxMFjCvNNsTo8RBMDEVQmk7gYkW4SCDuvdsBG");
        PubKey pubKey = KeyGenUtils.decodePubKey("3snPdw7i7PdktosF4iYd7dSeumq6hpLnwtrRCLQWkpdpntMxABvJdZ");
        BlockchainKeypair newUser = new BlockchainKeypair(pubKey, privKey);
        System.out.println("user'id="+newUser.getAddress());
        System.out.println("pubKey="+newUser.getPubKey().toBase58());
        System.out.println("privKey="+newUser.getPrivKey().toBase58());

        txTemp.users().register(newUser.getIdentity());

        txTemp.security().authorziations().forUser(newUser.getIdentity()).authorize("NORMAL8");

        // TX 准备就绪；
        PreparedTransaction prepTx = txTemp.prepare();
        prepTx.sign(adminKey);

        // 提交交易；
        prepTx.commit();
    }

    @Test
    public void validPermissionByExistUser(){
        // 在本地定义注册账号的 TX；
        TransactionTemplate txTemp = blockchainService.newTransaction(ledgerHash);
        BlockchainKeypair user = BlockchainKeyGenerator.getInstance().generate();
        System.out.println("user's address="+user.getAddress());
        txTemp.users().register(user.getIdentity());

        // TX 准备就绪；
        commitA(txTemp,adminKey);
    }
}
