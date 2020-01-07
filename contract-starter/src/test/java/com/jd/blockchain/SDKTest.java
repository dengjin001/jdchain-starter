package com.jd.blockchain;

import com.jd.blockchain.contract.SDK_Base_Demo;
import com.jd.blockchain.ledger.*;
import org.junit.Before;
import org.junit.Test;

/**
 * @author zhaogw
 * date 2019/8/8 10:43
 */
public class SDKTest extends SDK_Base_Demo {
    //because it need to connect the web, so make the switch;
    private boolean isTest = true;
    private String strDataAccount;
    private BlockchainKeypair existUser;

    @Before
    public void setup() {
        existUser = BlockchainKeyGenerator.getInstance().generate();
    }

    @Test
    public void checkXml_existDataAcount() {
        if (!isTest) return;
        insertData();
        TransactionTemplate txTemp = blockchainService.newTransaction(ledgerHash);

        //add some data for retrieve;
        System.out.println("current dataAccount=" + this.strDataAccount);
        txTemp.dataAccount(this.strDataAccount).setText("textKey", "{\"dest\":\"KA001\",\"id\":\"cc-fin01-01\",\"items\":\"FIN001|5000\",\"source\":\"FIN001\"}", -1);
        txTemp.dataAccount(this.strDataAccount).setXML("xmlKey", "<person>\n" +
                "    <age value=\"too young\" />\n" +
                "    <experience value=\"too simple\" />\n" +
                "    <result value=\"sometimes naive\" />\n" +
                "</person>", -1);

        // TX 准备就绪
        PreparedTransaction prepTx = txTemp.prepare();
        prepTx.sign(adminKey);

        // 提交交易；
        prepTx.commit();
    }

    /**
     * 生成一个区块链数据账户，并注册到区块链；
     */
    @Test
    public void insertData() {
        if (!isTest) return;
        // 在本地定义注册账号的 TX；
        TransactionTemplate txTemp = blockchainService.newTransaction(ledgerHash);
        //采用KeyGenerator来生成BlockchainKeypair;
        BlockchainKeypair dataAccount = BlockchainKeyGenerator.getInstance().generate();

        txTemp.dataAccounts().register(dataAccount.getIdentity());
        txTemp.dataAccount(dataAccount.getAddress()).setText("key1", "value1", -1);
        //add some data for retrieve;
        this.strDataAccount = dataAccount.getAddress().toBase58();
        System.out.println("current dataAccount=" + dataAccount.getAddress());
        txTemp.dataAccount(dataAccount.getAddress()).setText("cc-fin01-01", "{\"dest\":\"KA001\",\"id\":\"cc-fin01-01\",\"items\":\"FIN001|5000\",\"source\":\"FIN001\"}", -1);
        txTemp.dataAccount(dataAccount.getAddress()).setJSON("cc-fin02-01", "{\"dest\":\"KA001\",\"id\":\"cc-fin02-01\",\"items\":\"FIN002|2000\",\"source\":\"FIN002\"}", -1);

        // TX 准备就绪
        PreparedTransaction prepTx = txTemp.prepare();
        prepTx.sign(adminKey);

        // 提交交易；
        TransactionResponse transactionResponse = prepTx.commit();
        if (transactionResponse.isSuccess()) {
            System.out.println("success.");
        } else {
            System.out.println("exception=" + transactionResponse.getExecutionState().toString());
        }
    }

    @Test
    public void insertDataMore() throws InterruptedException {
        for (int i = 0; i < 15; i++) {
            insertData();
            Thread.sleep(1000);
        }

    }

    /**
     * 根据已有的数据账户地址，添加数据;
     */
    @Test
    public void inserDataByExisDataAccount() {
        if (!isTest) return;
        this.strDataAccount = "LdeNremWbMBmmn4hJkgYBqGqruMYE8iZqjeF5";
        TransactionTemplate txTemp = blockchainService.newTransaction(ledgerHash);

        //add some data for retrieve;
        System.out.println("current dataAccount=" + this.strDataAccount);
        txTemp.dataAccount(this.strDataAccount).setText("cc-fin01-01",
                "{\"dest\":\"KA001\",\"id\":\"cc-fin01-01\",\"items\":\"FIN001|5000\",\"source\":\"FIN001\"}", -1);
        txTemp.dataAccount(this.strDataAccount).setText("cc-fin02-01",
                "{\"dest\":\"KA001\",\"id\":\"cc-fin02-01\",\"items\":\"FIN002|2000\",\"source\":\"FIN002\"}", -1);
        txTemp.dataAccount(this.strDataAccount).setText("cc-fin03-01",
                "{\"dest\":\"KA001\",\"id\":\"cc-fin03-01\",\"items\":\"FIN001|5000\",\"source\":\"FIN003\"}", -1);
        txTemp.dataAccount(this.strDataAccount).setText("cc-fin04-01",
                "{\"dest\":\"KA002\",\"id\":\"cc-fin04-01\",\"items\":\"FIN003|3000\",\"source\":\"FIN002\"}", -1);
        txTemp.dataAccount(this.strDataAccount).setText("cc-fin05-01",
                "{\"dest\":\"KA003\",\"id\":\"cc-fin05-01\",\"items\":\"FIN001|5000\",\"source\":\"FIN001\"}", -1);
        txTemp.dataAccount(this.strDataAccount).setText("cc-fin06-01",
                "{\"dest\":\"KA004\",\"id\":\"cc-fin06-01\",\"items\":\"FIN002|2020\",\"source\":\"FIN001\"}", -1);
        txTemp.dataAccount(this.strDataAccount).setText("cc-fin07-01",
                "{\"dest\":\"KA005\",\"id\":\"cc-fin07-01\",\"items\":\"FIN001|5010\",\"source\":\"FIN001\"}", -1);
        txTemp.dataAccount(this.strDataAccount).setText("cc-fin08-01",
                "{\"dest\":\"KA006\",\"id\":\"cc-fin08-01\",\"items\":\"FIN001|3030\",\"source\":\"FIN001\"}", -1);

        // TX 准备就绪
        PreparedTransaction prepTx = txTemp.prepare();
        prepTx.sign(adminKey);

        // 提交交易；
        prepTx.commit();
    }
}
