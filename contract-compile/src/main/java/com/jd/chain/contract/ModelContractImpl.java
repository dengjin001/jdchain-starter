package com.jd.chain.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jd.blockchain.contract.ContractEventContext;
import com.jd.blockchain.contract.ContractException;
import com.jd.blockchain.contract.EventProcessingAware;
import com.jd.blockchain.crypto.HashDigest;
import com.jd.blockchain.ledger.TypedKVEntry;

import java.util.List;
import java.util.Map;

public class ModelContractImpl implements EventProcessingAware, ModelContract {
    
    private ContractEventContext eventContext;
    
    private HashDigest ledgerHash;
    
    @Override
    public void beforeEvent(ContractEventContext eventContext) {
        this.eventContext = eventContext;
        this.ledgerHash = eventContext.getCurrentLedgerHash();
    }
    
    @Override
    public void postEvent(ContractEventContext eventContext, Exception error) {
    }
    
    
    @Override
    public String readModel(String address, String modelHash) {
        TypedKVEntry[] kvDataEntries = eventContext.getLedger().getDataEntries(ledgerHash, address, modelHash);
        if (kvDataEntries == null || kvDataEntries.length == 0) {
            return null;
        }
        Object value = kvDataEntries[0].getValue();
        return JSON.toJSONString(value);
    }
    
    @Override
    public String installModel(String address, String modelHash, String content) {
        TypedKVEntry[] kvDataEntries = eventContext.getLedger().getDataEntries(ledgerHash, address, modelHash);
        if (kvDataEntries != null && kvDataEntries.length > 0) {
            long currVersion = kvDataEntries[0].getVersion();
            if (currVersion > -1L) {
                throw new ContractException("该模型hash已存在");
            } else {
                Map map = JSON.parseObject(content, Map.class);
                map.put("version", 0);
                eventContext.getLedger().dataAccount(address).setJSON(modelHash, JSON.toJSONString(map, SerializerFeature.WriteMapNullValue), -1L);
                return modelHash;
            }
        } else {
            throw new IllegalStateException(String.format("Ledger[%s] inner Error !!!", this.ledgerHash.toBase58()));
            
        }
    }
    
    @Override
    public String updateModel(String address, String modelHash, String content) {
        TypedKVEntry[] kvDataEntries = eventContext.getLedger().getDataEntries(ledgerHash, address, modelHash);
        if (kvDataEntries != null && kvDataEntries.length > 0) {
            long currVersion = kvDataEntries[0].getVersion();
            if (currVersion > -1L) {
                //以前的拥有者记录
                Object value = kvDataEntries[0].getValue();
                Map map0 = JSON.parseObject(value.toString(), Map.class);
                Object ownerList = map0.get("ownerList");
                //当前购买者信息
                Map map = JSON.parseObject(content, Map.class);
                List<Map> list = JSON.parseArray(ownerList.toString(), Map.class);
                list.add(map);
                map0.put("ownerList", list);
                map0.put("version", currVersion);
                eventContext.getLedger().dataAccount(address).setJSON(modelHash, JSON.toJSONString(map0, SerializerFeature.WriteMapNullValue), kvDataEntries[0].getVersion());
                return modelHash;
            } else {
                throw new ContractException("该模型hash不存在");
            }
        } else {
            throw new IllegalStateException(String.format("Ledger[%s] inner Error !!!", this.ledgerHash.toBase58()));
        }
    }
}
