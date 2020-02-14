package com.jd.blockchain;

import com.jd.blockchain.contract.SDK_Base_Demo;
import com.jd.blockchain.crypto.HashDigest;
import com.jd.blockchain.ledger.*;
import com.jd.blockchain.sdk.converters.ClientResolveUtil;
import com.jd.blockchain.transaction.GenericValueHolder;
import com.jd.blockchain.utils.Bytes;
import com.jd.chain.contract.Guanghu;
import org.junit.Before;
import org.junit.Test;

import static com.jd.blockchain.contract.SDKDemo_Constant.readChainCodes;
import static com.jd.blockchain.transaction.ContractReturnValue.decode;

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

        getData(strDataAccount);
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
            getData(dataAccount.getAddress().toBase58());
        } else {
            System.out.println("exception=" + transactionResponse.getExecutionState().toString());
        }
    }

    /**
     * 根据已有的数据账户地址，添加数据;
     */
    @Test
    public void insertDataByExistDataAccount() {
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

        getData(strDataAccount);
    }

    public void getData(String commerceAccount) {
        // 查询区块信息；
        // 区块高度；
        long ledgerNumber = blockchainService.getLedger(ledgerHash).getLatestBlockHeight();
        // 最新区块；
        LedgerBlock latestBlock = blockchainService.getBlock(ledgerHash, ledgerNumber);
        // 区块中的交易的数量；
        long txCount = blockchainService.getTransactionCount(ledgerHash, latestBlock.getHash());
        // 获取交易列表；
        LedgerTransaction[] txList = blockchainService.getTransactions(ledgerHash, ledgerNumber, 0, 100);
        // 遍历交易列表
        for (LedgerTransaction ledgerTransaction : txList) {
            TransactionContent txContent = ledgerTransaction.getTransactionContent();
            Operation[] operations = txContent.getOperations();
            if (operations != null && operations.length > 0) {
                for (Operation operation : operations) {
                    operation = ClientResolveUtil.read(operation);
                    // 操作类型：数据账户注册操作
                    if (operation instanceof DataAccountRegisterOperation) {
                        DataAccountRegisterOperation daro = (DataAccountRegisterOperation) operation;
                        BlockchainIdentity blockchainIdentity = daro.getAccountID();
                    }
                    // 操作类型：用户注册操作
                    else if (operation instanceof UserRegisterOperation) {
                        UserRegisterOperation uro = (UserRegisterOperation) operation;
                        BlockchainIdentity blockchainIdentity = uro.getUserID();
                    }
                    // 操作类型：账本注册操作
                    else if (operation instanceof LedgerInitOperation) {

                        LedgerInitOperation ledgerInitOperation = (LedgerInitOperation) operation;
                        LedgerInitSetting ledgerInitSetting = ledgerInitOperation.getInitSetting();

                        ParticipantNode[] participantNodes = ledgerInitSetting.getConsensusParticipants();
                    }
                    // 操作类型：合约发布操作
                    else if (operation instanceof ContractCodeDeployOperation) {
                        ContractCodeDeployOperation ccdo = (ContractCodeDeployOperation) operation;
                        BlockchainIdentity blockchainIdentity = ccdo.getContractID();
                    }
                    // 操作类型：合约执行操作
                    else if (operation instanceof ContractEventSendOperation) {
                        ContractEventSendOperation ceso = (ContractEventSendOperation) operation;
                    }
                    // 操作类型：KV存储操作
                    else if (operation instanceof DataAccountKVSetOperation) {
                        DataAccountKVSetOperation.KVWriteEntry[] kvWriteEntries =
                                ((DataAccountKVSetOperation) operation).getWriteSet();
                        if (kvWriteEntries != null && kvWriteEntries.length > 0) {
                            for (DataAccountKVSetOperation.KVWriteEntry kvWriteEntry : kvWriteEntries) {
                                BytesValue bytesValue = kvWriteEntry.getValue();
                                DataType dataType = bytesValue.getType();
                                Object showVal = ClientResolveUtil.readValueByBytesValue(bytesValue);
                                System.out.println("writeSet.key=" + kvWriteEntry.getKey());
                                System.out.println("writeSet.value=" + showVal);
                                System.out.println("writeSet.type=" + dataType);
                                System.out.println("writeSet.version=" + kvWriteEntry.getExpectedVersion());
                            }
                        }
                    }
                }
            }
        }

        //根据交易的 hash 获得交易；注：客户端生成 PrepareTransaction 时得到交易hash；
        HashDigest txHash = txList[0].getTransactionContent().getHash();
//		Transaction tx = blockchainService.getTransactionByContentHash(ledgerHash, txHash);
//		String[] objKeys = new String[] { "x001", "x002" };
//		KVDataEntry[] kvData = blockchainService.getDataEntries(ledgerHash, commerceAccount, objKeys);

        // 获取数据账户下所有的KV列表
        TypedKVEntry[] kvData = blockchainService.getDataEntries(ledgerHash, commerceAccount, 0, 100);
        if (kvData != null && kvData.length > 0) {
            for (TypedKVEntry kvDatum : kvData) {
                System.out.println("kvData.key=" + kvDatum.getKey());
                System.out.println("kvData.version=" + kvDatum.getVersion());
                System.out.println("kvData.type=" + kvDatum.getType());
                System.out.println("kvData.value=" + kvDatum.getValue());
            }
        }
    }

    @Test
    public void executeContractOK() {
        // 发布jar包
        // 定义交易模板
        TransactionTemplate txTpl = blockchainService.newTransaction(ledgerHash);

        // 将jar包转换为二进制数据
        byte[] contractCode = readChainCodes("contract-JDChain-Contract.jar");

        // 生成一个合约账号
        BlockchainKeypair contractDeployKey = BlockchainKeyGenerator.getInstance().generate();
        System.out.println("contract's address=" + contractDeployKey.getAddress());

        // 生成发布合约操作
        txTpl.contracts().deploy(contractDeployKey.getIdentity(), contractCode);

        // 生成预发布交易；
        PreparedTransaction ptx = txTpl.prepare();

        // 对交易进行签名
        ptx.sign(adminKey);

        // 提交并等待共识返回；
        TransactionResponse txResp = ptx.commit();

        // 获取合约地址
        Bytes contractAddress = contractDeployKey.getAddress();

        // 打印交易返回信息
        System.out.printf("Tx[%s] -> BlockHeight = %s, BlockHash = %s, isSuccess = %s, ExecutionState = %s \r\n",
                txResp.getContentHash().toBase58(), txResp.getBlockHeight(), txResp.getBlockHash().toBase58(),
                txResp.isSuccess(), txResp.getExecutionState());

        // 打印合约地址
        System.out.printf("ContractAddress = %s \r\n", contractAddress.toBase58());

        // 注册一个数据账户
        BlockchainKeypair dataAccount = createDataAccount();
        // 获取数据账户地址
        String dataAddress = dataAccount.getAddress().toBase58();
        // 打印数据账户地址
        System.out.printf("DataAccountAddress = %s \r\n", dataAddress);

        // 创建两个账号：
        String account0 = "jd_zhangsan";
        String content = "{\"dest\":\"KA006\",\"id\":\"cc-fin08-01\",\"items\":\"FIN001|3030\",\"source\":\"FIN001\"}";
        System.out.println(create(dataAddress, account0, content, contractAddress));
    }

    //contract bifurcation
    @Test
    public void executeContractBifByHalf() {
        this.executeContractBif("half");
    }

    /**
     *  test  more bifurcation, then insertData;
     *  purpose: Is the system still robust after much rollback ?
     */

    @Test
    public void executeContractBifByHalf_more() {
        for(int i=0;i<100;i++){
            this.executeContractBif("half");
        }
        this.insertData();
    }

    /**
     * bifurcation, 3:1;
     */
    @Test
    public void executeContractBifBy31() {
        this.executeContractBif("most");
    }

    private void executeContractBif(String isHalf) {
        // 发布jar包
        // 定义交易模板
        TransactionTemplate txTpl = blockchainService.newTransaction(ledgerHash);

        // 将jar包转换为二进制数据
        byte[] contractCode = readChainCodes("contract-JDChain-Contract.jar");

        // 生成一个合约账号
        BlockchainKeypair contractDeployKey = BlockchainKeyGenerator.getInstance().generate();
        System.out.println("contract's address=" + contractDeployKey.getAddress());

        // 生成发布合约操作
        txTpl.contracts().deploy(contractDeployKey.getIdentity(), contractCode);

        // 生成预发布交易；
        PreparedTransaction ptx = txTpl.prepare();

        // 对交易进行签名
        ptx.sign(adminKey);

        // 提交并等待共识返回；
        TransactionResponse txResp = ptx.commit();

        // 获取合约地址
        Bytes contractAddress = contractDeployKey.getAddress();

        // 打印交易返回信息
        System.out.printf("Tx[%s] -> BlockHeight = %s, BlockHash = %s, isSuccess = %s, ExecutionState = %s \r\n",
                txResp.getContentHash().toBase58(), txResp.getBlockHeight(), txResp.getBlockHash().toBase58(),
                txResp.isSuccess(), txResp.getExecutionState());

        // 打印合约地址
        System.out.printf("ContractAddress = %s \r\n", contractAddress.toBase58());

        // 注册一个数据账户
        BlockchainKeypair dataAccount = createDataAccount();
        // 获取数据账户地址
        String dataAddress = dataAccount.getAddress().toBase58();
        // 打印数据账户地址
        System.out.printf("DataAccountAddress = %s \r\n", dataAddress);

        // 创建两个账号：
        String account0 = "jd_zhangsan";
        String content = "{\"dest\":\"KA006\",\"id\":\"cc-fin08-01\",\"items\":\"FIN001|3030\",\"source\":\"FIN001\"}";
        System.out.println("executeContractByHalf="+createBif(dataAddress, account0, content, contractAddress, isHalf));
    }

    private BlockchainKeypair createDataAccount() {
        // 首先注册一个数据账户
        BlockchainKeypair newDataAccount = BlockchainKeyGenerator.getInstance().generate();

        TransactionTemplate txTpl = blockchainService.newTransaction(ledgerHash);
        txTpl.dataAccounts().register(newDataAccount.getIdentity());
//        txTpl.dataAccount(newDataAccount.getIdentity().getAddress()).setInt64("key1",123,-1);
        commit(txTpl);
        return newDataAccount;
    }

    private String create(String address, String account, String content, Bytes contractAddress) {
        TransactionTemplate txTpl = blockchainService.newTransaction(ledgerHash);
        // 使用合约创建
        Guanghu guanghu = txTpl.contract(contractAddress, Guanghu.class);
        GenericValueHolder<String> result = decode(guanghu.putval(address, account, content));
        commit(txTpl);
        return result.get();
    }

    private String createBif(String address, String account, String content, Bytes contractAddress, String isHalf) {
        TransactionTemplate txTpl = blockchainService.newTransaction(ledgerHash);
        // 使用合约创建
        Guanghu guanghu = txTpl.contract(contractAddress, Guanghu.class);
        GenericValueHolder<String> result = decode(guanghu.putvalBifurcation(address, account, content, isHalf));
        commit(txTpl);
        return result.get();
    }

    /**
     * 生成一个区块链用户，并注册到区块链；
     */
    @Test
    public void registerUserTest() {
        this.registerUser();
    }

    @Test
    public void rigisterUserMore() {
        for (int i = 0; i < 15; i++) {
            this.registerUser();
        }
    }

    /**
     * use the exist user to sign;
     */
    @Test
    public void registerNewUserByExistUser() {
        //first invoke the case: registerExistUser(), to rigister user in the ledger;
        //now use the existUser to sign;
        // 在本地定义注册账号的 TX；
        TransactionTemplate txTemp = blockchainService.newTransaction(ledgerHash);

        BlockchainKeypair user = BlockchainKeyGenerator.getInstance().generate();
        System.out.println("user'id=" + user.getAddress());
        txTemp.users().register(user.getIdentity());
        // TX 准备就绪；
        PreparedTransaction prepTx = txTemp.prepare();
        prepTx.sign(existUser);

        // 提交交易；
        prepTx.commit();
    }

    /**
     * insert data by the exist user; no think the permission;
     */
    @Test
    public void insertDataByExistUser() {
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

        // TX 准备就绪
        PreparedTransaction prepTx = txTemp.prepare();
        prepTx.sign(existUser);

        // 提交交易；
        TransactionResponse transactionResponse = prepTx.commit();
        if (transactionResponse.isSuccess()) {
            System.out.println("result=" + transactionResponse.isSuccess());
        } else {
            System.out.println("exception=" + transactionResponse.getExecutionState().toString());
        }
    }

    private void registerRole(String roleName) {
        // 在本地定义注册账号的 TX；
        TransactionTemplate txTemp = blockchainService.newTransaction(ledgerHash);

        // 定义角色权限；
        txTemp.security().roles().configure(roleName).enable(LedgerPermission.APPROVE_TX)
                .enable(LedgerPermission.REGISTER_USER).disable(LedgerPermission.REGISTER_DATA_ACCOUNT)
                .enable(TransactionPermission.DIRECT_OPERATION);

        // TX 准备就绪；
        PreparedTransaction prepTx = txTemp.prepare();
        prepTx.sign(adminKey);

        // 提交交易；
        prepTx.commit();
    }

    /**
     * rigister the exist user to ledger;
     */
    private void registerExistUser(String roleName) {
        // 在本地定义注册账号的 TX；
        TransactionTemplate txTemp = blockchainService.newTransaction(ledgerHash);
        System.out.println("user'id=" + existUser.getAddress());

        txTemp.users().register(existUser.getIdentity());
//        txTemp.security().authorziations().forUser(newUser.getIdentity()).authorize("ROLE-ADD-DATA").setPolicy(RolesPolicy.INTERSECT);
        txTemp.security().authorziations().forUser(existUser.getIdentity()).unauthorize("DEFAULT").authorize(roleName);

        // TX 准备就绪；
        PreparedTransaction prepTx = txTemp.prepare();
        prepTx.sign(adminKey);

        // 提交交易；
        prepTx.commit();
        System.out.println("registerExistUser() done.");
    }

    private void checkInsertDataByExistUser() {
        System.out.println("checkInsertDataByExistUser() start...");
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

        // TX 准备就绪
        PreparedTransaction prepTx = txTemp.prepare();
        prepTx.sign(existUser);

        // 提交交易；
        TransactionResponse transactionResponse = prepTx.commit();
        if (transactionResponse.isSuccess()) {
            System.out.println("result=" + transactionResponse.isSuccess());
        } else {
            System.out.println("exception=" + transactionResponse.getExecutionState().toString());
        }
    }

    /**
     * use the same blockchainService(connected by adminKey);
     * because it's policy is at_least_one(see: DataAccountRegisterOperationHandle.java),
     * although signed by existUser, the operation of "txTemp.dataAccounts().register(...)" also can passed;
     * so you will use a new user to connect the gateway and signed by it. you can see the demo {@link SDKDemo_RegisterUser#checkPermission()}
     */
    @Test
    public void checkPermission() {
        String roleName = "ROLE-ADD-DATA";
        registerRole(roleName);
        registerExistUser(roleName);
        checkInsertDataByExistUser();
    }
}
