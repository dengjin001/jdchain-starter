package com.jd.blockchain;

import com.jd.blockchain.crypto.KeyGenUtils;
import com.jd.blockchain.crypto.PrivKey;
import com.jd.blockchain.crypto.PubKey;
import com.jd.blockchain.ledger.BlockchainKeyGenerator;
import com.jd.blockchain.ledger.BlockchainKeypair;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jd.blockchain.contract.SDKDemo_Constant.GW_PASSWORD;

public class ResponseTest extends SDKTest {
    private String invalid_GW_PUB_KEY = "3snPdw7i7PnBmpLtTjUt7Lyuo3RpWZzxNtjDsx1rRNi9jc3892N2Uc";
    private String invalid_GW_PRIV_KEY = "177gjxbtF999qv654gxRHwqvbibbwtNb2LknLvmsRVTDKt4MJehZE3me63r9uxpANjMEHGC";
    PrivKey gwPrivkey0 = KeyGenUtils.decodePrivKey(invalid_GW_PRIV_KEY, GW_PASSWORD);
    PubKey gwPubKey0 = KeyGenUtils.decodePubKey(invalid_GW_PUB_KEY);
    BlockchainKeypair invalidAdminKey = new BlockchainKeypair(gwPubKey0, gwPrivkey0);

    @Before
    public void setup(){
    }


    //1.使用无效的endpoint用户,执行一次注册数据账户并set值的任务;
    @Test
    public void test_invalid_endPoint(){
        this.insertData(null,invalidAdminKey);
    }

    /**
     * 2.use the multiThread to insert date，构建单个区块中包含有效用户签名+无效用户签名的记录场景;
     */
    @Test
    public void test_validPlusInvalidSign_multiThread(){
        //1. 提供指定线程数量的线程池；
        ExecutorService service = Executors.newFixedThreadPool(10);
        //2. 执行指定的线程的操作，有效用户签名+无效用户签名；
        try {
            for(int i=0;i<10;i++){
                service.submit(new DataAccountThreadCall21());//适用于Callable
                service.submit(new DataAccountThreadCall31());//适用于Callable
            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //3. 关闭连接池
        service.shutdown();
    }

    //3.使用相同的数据账户地址插入数据;
    @Test
    public void test_insertData_same_dataAccount(){
        BlockchainKeypair dataAccount = BlockchainKeyGenerator.getInstance().generate();
        this.insertData(dataAccount,null);
        this.insertData(dataAccount,null);
    }

    //4.使用相同的数据账户地址插入数据;多线程执行效果；
    @Test
    public void test_insertData_same_dataAccount_multiThread(){
        //1. 提供指定线程数量的线程池；
        ExecutorService service = Executors.newFixedThreadPool(10);
        //2. 执行指定的线程的操作，需要提供实现Runnable接口或Callable接口实现类的对象
        for(int i=0;i<10;i++){
            service.submit(new DataAccountThreadCall41());//适用于Callable
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //3. 关闭连接池
        service.shutdown();
    }

    //5.使用相同的用户注册;
    @Test
    public void test_registerUser_reply(){
        BlockchainKeypair userKey = BlockchainKeyGenerator.getInstance().generate();
        this.registerUser(null,userKey);
        this.registerUser(null,userKey);
    }

    //6.使用相同的用户注册，多线程并发;
    @Test
    public void test_registerUser_reply_multiThread(){
        //1. 提供指定线程数量的线程池；
        ExecutorService service = Executors.newFixedThreadPool(10);
        //2. 执行指定的线程的操作;
        for(int i=0;i<10;i++){
            service.submit(new UserAccountThreadCall61());
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //3. 关闭连接池
        service.shutdown();
    }

    //7.使用相同的合约合约账户，单线程;
    @Test
    public void test_deployContract_reply(){
        BlockchainKeypair contractDeployKey = BlockchainKeyGenerator.getInstance().generate();
        this.contractHandle(null,null,contractDeployKey,true,false);
        this.contractHandle(null,null,contractDeployKey,true,false);
    }



    //8.数据账户未注册，直接set;

}



//===========inner Class============

// 2-1. 创建一个实现Callable的实现类
class DataAccountThreadCall21 extends SDKTest implements Callable {
    // 2. 实现call方法，将此线程需要执行的操作声明在call中
    @Override
    public Object call() {
        insertData();
        return null;
    }
}

// 3-1. 创建一个实现Callable的实现类，无效用户进行签名；
class DataAccountThreadCall31 extends ResponseTest implements Callable {
    // 2. 实现call方法，将此线程需要执行的操作声明在call中
    @Override
    public Object call() {
        insertData(null,invalidAdminKey);
        return null;
    }
}
// 4-1. 创建一个实现Callable的实现类，相同数据账户执行两次insert；
class DataAccountThreadCall41 extends ResponseTest implements Callable {
    // 2. 实现call方法；
    @Override
    public Object call() {
        BlockchainKeypair dataAccount = BlockchainKeyGenerator.getInstance().generate();
        this.insertData(dataAccount,null);
        this.insertData(dataAccount,null);
        return null;
    }
}

// 6-1. 创建一个实现Callable的实现类，相同用户注册;
class UserAccountThreadCall61 extends ResponseTest implements Callable {
    // 2. 实现call方法；
    @Override
    public Object call() {
        BlockchainKeypair userKey = BlockchainKeyGenerator.getInstance().generate();
        this.registerUser(null,userKey);
        this.registerUser(null,userKey);
        return null;
    }
}