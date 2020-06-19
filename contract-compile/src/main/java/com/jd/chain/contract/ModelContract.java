package com.jd.chain.contract;


import com.jd.blockchain.contract.Contract;
import com.jd.blockchain.contract.ContractEvent;


@Contract
public interface ModelContract {
    
    @ContractEvent(name = "readModel")
    String readModel(String address, String modelHash);
    
    @ContractEvent(name = "installModel")
    String installModel(String address, String modelHash, String content);
    
    @ContractEvent(name = "updateModel")
    String updateModel(String address, String modelHash, String content);
    
}
