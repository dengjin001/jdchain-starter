package com.jd.chain.contract;

import com.alibaba.fastjson.JSON;
import com.jd.blockchain.contract.ContractEventContext;
import com.jd.blockchain.contract.ContractException;
import com.jd.blockchain.contract.EventProcessingAware;
import com.jd.blockchain.crypto.HashDigest;
import com.jd.blockchain.ledger.KVDataVO;
import com.jd.blockchain.ledger.KVInfoVO;
import com.jd.blockchain.ledger.TypedKVEntry;
import com.jd.blockchain.utils.Bytes;
import com.jd.blockchain.utils.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class MyTransferContractImpl implements EventProcessingAware, MyTransferContract {
    
    private ContractEventContext eventContext;
    
    private HashDigest ledgerHash;
    
    @Override
    public String create(String address, String account, String money) {
        TypedKVEntry[] kvDataEntries = eventContext.getLedger().getDataEntries(ledgerHash, address, account);
        // 肯定有返回值，但若不存在则返回version=-1
        if (kvDataEntries != null && kvDataEntries.length > 0) {
            long currVersion = kvDataEntries[0].getVersion();
            if (currVersion > -1) {
                throw new IllegalStateException(String.format("%s -> %s already have created !!!", address, account));
            }
            eventContext.getLedger().dataAccount(address).setJSON(account, money, -1L);
        } else {
            throw new IllegalStateException(String.format("Ledger[%s] inner Error !!!", ledgerHash.toBase58()));
        }
        return String.format("DataAccountAddress[%s] -> Create(By Contract Operation) Account = %s and Money = %s Success!!! \r\n",
                address, account, money);
    }
    
    @Override
    public String ensureFromTransfer(String address, String from, String to, String money, String free) {
        BigDecimal transferMoney = new BigDecimal(money);
        BigDecimal transferFree = new BigDecimal(free);
        if (transferFree.doubleValue() >= 1) {
            throw new ContractException("费率有误");
        }
        if (transferMoney.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ContractException("转账金额有误");
        }
        //查询转出、转入、手续账户现在的余额和版本
        Map<String, BigDecimal> oldBalance = countBalance(address, from, to, transferMoney, transferFree, true);
        long oldFromVersion = oldBalance.get("oldFromVersion").longValue();
        long oldToVersion = oldBalance.get("oldToVersion").longValue();
        long oldFreeVersion = oldBalance.get("oldFreeVersion").longValue();
        BigDecimal newFromBalance = oldBalance.get("newFromBalance");
        BigDecimal newToBalance = oldBalance.get("newToBalance");
        BigDecimal newFreeBalance = oldBalance.get("newFreeBalance");
        //对3个账户进行数据写入
        eventContext.getLedger().dataAccount(address).setJSON(from, newFromBalance.toString(), oldFromVersion)
                .getOperation();
        eventContext.getLedger().dataAccount(address).setJSON(to, newToBalance.toString(), oldToVersion)
                .getOperation();
        if (newFreeBalance.compareTo(BigDecimal.ZERO) > 0) {
            eventContext.getLedger().dataAccount(address)
                    .setJSON(address, newFreeBalance.toString(), oldFreeVersion).getOperation();
        }
        return String.format("DataAccountAddress[%s] transfer from [%s] to [%s] and [money = %s] Success !!!", address, from, to, money);
    }
    
    @Override
    public String ensureToTransfer(String address, String from, String to, String money, String free) {
        BigDecimal transferMoney = new BigDecimal(money);
        BigDecimal transferFree = new BigDecimal(free);
        if (transferFree.doubleValue() >= 1) {
            throw new ContractException("费率有误");
        }
        if (transferMoney.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ContractException("转账金额有误");
        }
        //查询转出、转入、手续账户现在的余额和版本
        Map<String, BigDecimal> oldBalance = countBalance(address, from, to, transferMoney, transferFree, false);
        long oldFromVersion = oldBalance.get("oldFromVersion").longValue();
        long oldToVersion = oldBalance.get("oldToVersion").longValue();
        long oldFreeVersion = oldBalance.get("oldFreeVersion").longValue();
        BigDecimal newFromBalance = oldBalance.get("newFromBalance");
        BigDecimal newToBalance = oldBalance.get("newToBalance");
        BigDecimal newFreeBalance = oldBalance.get("newFreeBalance");
        //对3个账户进行数据写入
        eventContext.getLedger().dataAccount(address).setJSON(from, newFromBalance.toString(), oldFromVersion)
                .getOperation();
        eventContext.getLedger().dataAccount(address).setJSON(to, newToBalance.toString(), oldToVersion)
                .getOperation();
        if (newFreeBalance.compareTo(BigDecimal.ZERO) > 0) {
            eventContext.getLedger().dataAccount(address)
                    .setJSON(address, newFreeBalance.toString(), oldFreeVersion).getOperation();
        }
        return String.format("DataAccountAddress[%s] transfer from [%s] to [%s] and [money = %s] Success !!!", address, from, to, money);
        
    }
    
    
    @Override
    public long read(String address, String account) {
        TypedKVEntry[] kvDataEntries = eventContext.getLedger().getDataEntries(ledgerHash, address, account);
        if (kvDataEntries == null || kvDataEntries.length == 0) {
            return -1;
        }
        return (long) kvDataEntries[0].getValue();
    }
    
    @Override
    public String readAll(String address, String account) {
        TypedKVEntry[] kvDataEntries = eventContext.getLedger().getDataEntries(ledgerHash, address, account);
        // 获取最新的版本号
        if (kvDataEntries == null || kvDataEntries.length == 0) {
            return "";
        }
        long newestVersion = kvDataEntries[0].getVersion();
        if (newestVersion == -1) {
            return "";
        }
        KVDataVO[] kvDataVOS = new KVDataVO[1];
        long[] versions = new long[(int) newestVersion + 1];
        for (int i = 0; i < versions.length; i++) {
            versions[i] = i;
        }
        KVDataVO kvDataVO = new KVDataVO(account, versions);
        kvDataVOS[0] = kvDataVO;
        KVInfoVO kvInfoVO = new KVInfoVO(kvDataVOS);
        TypedKVEntry[] allEntries = eventContext.getLedger().getDataEntries(ledgerHash, address, kvInfoVO);
        return JSON.toJSONString(allEntries);
    }
    
    @Override
    public void beforeEvent(ContractEventContext eventContext) {
        this.eventContext = eventContext;
        this.ledgerHash = eventContext.getCurrentLedgerHash();
    }
    
    @Override
    public void postEvent(ContractEventContext eventContext, Exception error) {
    }
    
    /**
     * @param type : true:转账方出money,
     *             false:收款方收money
     * @return java.util.Map<java.lang.String, java.math.BigDecimal>
     */
    private Map<String, BigDecimal> countBalance(String address, String fromAddress,
                                                 String toAddress, BigDecimal amount, BigDecimal freeCharge, boolean type) {
        BigDecimal oldFromBalance;
        BigDecimal oldToBalance;
        BigDecimal oldFreeBalance;
        BigDecimal oldFromVersion;
        BigDecimal oldToVersion;
        BigDecimal oldFreeVersion;
        BigDecimal newFromBalance;
        BigDecimal newToBalance;
        BigDecimal newFreeBalance = BigDecimal.valueOf(0);
        TypedKVEntry[] dataEntries = eventContext.getLedger().getDataEntries(ledgerHash, address, fromAddress,
                toAddress, address);
        TypedKVEntry fromBalance = dataEntries[0];
        TypedKVEntry toBalance = dataEntries[1];
        TypedKVEntry freeBalance = dataEntries[2];
        oldFromBalance = StringUtils.isEmpty(fromBalance.getValue()) ? BigDecimal.valueOf(0) : new BigDecimal(fromBalance.getValue().toString());
        oldFromVersion = BigDecimal.valueOf(fromBalance.getVersion());
        oldToBalance = StringUtils.isEmpty(toBalance.getValue()) ? BigDecimal.valueOf(0) : new BigDecimal(toBalance.getValue().toString());
        oldToVersion = BigDecimal.valueOf(toBalance.getVersion());
        oldFreeBalance = StringUtils.isEmpty(freeBalance.getValue()) ? BigDecimal.valueOf(0) : new BigDecimal(freeBalance.getValue().toString());
        oldFreeVersion = StringUtils.isEmpty(freeBalance.getValue()) ? BigDecimal.valueOf(-1) : BigDecimal.valueOf(freeBalance.getVersion());
        if (freeCharge.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal free = amount.multiply(freeCharge).setScale(2, BigDecimal.ROUND_HALF_UP);
            if (toAddress.equals(address) || fromAddress.equals(address)) {
                //不计算手续费
                newFromBalance = oldFromBalance.subtract(amount);
                newToBalance = oldToBalance.add(amount);
            } else {
                //根据i来判断是保证哪方是转账金额
                if (type) {
                    //转出余额=转出旧余额-转账金额
                    newFromBalance = oldFromBalance.subtract(amount);
                    //转入余额=转入旧余额+转账金额-手续费
                    newToBalance = oldToBalance.add(amount).subtract(free);
                    //手续账户余额=旧余额+本次手续费
                    newFreeBalance = oldFreeBalance.add(free);
                } else {
                    //转出余额=转出旧余额-转账金额
                    newFromBalance = oldFromBalance.subtract(amount).subtract(free);
                    //转入余额=转入旧余额+转账金额
                    newToBalance = oldToBalance.add(amount);
                    //手续账户余额=旧余额+本次手续费
                    newFreeBalance = oldFreeBalance.add(free);
                }
            }
            
        } else {
            newFromBalance = oldFromBalance.subtract(amount);
            newToBalance = oldToBalance.add(amount);
        }
        Bytes address1 = eventContext.getLedger().getDataAccount(ledgerHash, fromAddress).getAddress();
        if (StringUtils.isEmpty(address1)) {
            throw new ContractException("cannot found fromAddress");
        }
        Bytes address2 = eventContext.getLedger().getDataAccount(ledgerHash, toAddress).getAddress();
        if (StringUtils.isEmpty(address2)) {
            throw new ContractException("cannot found toAddress");
        }
        //判断转出账户余额
        if (newFromBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ContractException("Insufficient balance!");
        }
        Map<String, BigDecimal> map = new HashMap<>(16);
        map.put("oldFromVersion", oldFromVersion);
        map.put("oldToVersion", oldToVersion);
        map.put("oldFreeVersion", oldFreeVersion);
        map.put("newFromBalance", newFromBalance);
        map.put("newToBalance", newToBalance);
        map.put("newFreeBalance", newFreeBalance);
        return map;
    }
    
    
}
