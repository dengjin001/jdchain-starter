package com.jd.chain.contract;

import com.jd.blockchain.contract.Contract;
import com.jd.blockchain.contract.ContractEvent;

@Contract
public interface MyTransferContract {
    
    @ContractEvent(name = "create")
    String create(String address, String account, String money);
    
    /**
     * 确保转出方转出money
     *
     * @date 10:39 2019/11/4
     */
    @ContractEvent(name = "ensureFromTransfer")
    String ensureFromTransfer(String address, String from, String to, String money, String free);
    
    /**
     * 确保收款方收到money
     *
     * @date 10:39 2019/11/4
     */
    @ContractEvent(name = "ensureToTransfer")
    String ensureToTransfer(String address, String from, String to, String money, String free);
    
    @ContractEvent(name = "read")
    long read(String address, String account);
    
    @ContractEvent(name = "readAll")
    String readAll(String address, String account);
}
