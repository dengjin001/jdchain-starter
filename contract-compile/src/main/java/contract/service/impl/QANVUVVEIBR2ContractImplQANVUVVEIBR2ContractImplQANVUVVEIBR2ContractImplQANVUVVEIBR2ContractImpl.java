package contract.service.impl;

import com.alibaba.fastjson.JSON;
import com.jd.blockchain.contract.ContractEventContext;
import com.jd.blockchain.contract.EventProcessingAware;
import com.jd.blockchain.crypto.HashDigest;
import com.jd.blockchain.ledger.TypedKVEntry;
import com.jd.blockchain.utils.StringUtils;
import contract.entity.QANVUVVEIBR2;
import contract.service.QANVUVVEIBR2Contract;

;

/**
 * Author cbc8df10-ab0b-4604-8d5b-0b7f57c5de0e
 * Date  2020-03-19 14:43:03
 */
public class QANVUVVEIBR2ContractImplQANVUVVEIBR2ContractImplQANVUVVEIBR2ContractImplQANVUVVEIBR2ContractImpl implements EventProcessingAware, QANVUVVEIBR2Contract {

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
    public String writeQANVUVVEIBR2(String address, String requestJson){
        try {
            StringBuilder errorMsg = new StringBuilder();
            QANVUVVEIBR2 obj = validQANVUVVEIBR2(requestJson, errorMsg);
            if(!StringUtils.isEmpty(errorMsg.toString())){
                return errorMsg.toString();
            }
            TypedKVEntry[] kvDataEntries = eventContext.getLedger().getDataEntries(ledgerHash, address, JSON.toJSONString(obj.getUserName()));
            // 肯定有返回值，但若不存在则返回version=-1
            if (kvDataEntries != null && kvDataEntries.length > 0) {
                 long currVersion = kvDataEntries[0].getVersion();
                 if (currVersion > -1) {
                      throw new IllegalStateException(String.format("%s -> %s already have created !!!", address, obj.getUserName()));
                 }
//                System.out.println("in contract, dataAccount="+address+", json="+JSON.toJSONString(obj.getUserName())+",requestJson="+requestJson);
                eventContext.getLedger().dataAccount(address).setText("key1","value1",-1);
                eventContext.getLedger().dataAccount(address).setText("requestJson",requestJson,-1);
                eventContext.getLedger().dataAccount(address).setText("getUserName",JSON.toJSONString(obj.getUserName()),-1);
                 eventContext.getLedger().dataAccount(address).setText(JSON.toJSONString(obj.getUserName()), requestJson, -1L);
            } else {
                 throw new IllegalStateException(String.format("Ledger[%s] inner Error !!!", ledgerHash.toBase58()));
            }
            return String.format("DataAccountAddress[%s] -> Create(By Contract Operation) bizId = %s and requestJson = %s Success!!! \r\n",address, obj.getUserName(), requestJson);
        } catch (Exception e) {
            return String.format("Fail, DataAccountAddress[%s] -> Create( e = %s !!! \r\n",address,  e);
        }
    }

    @Override
    public String readQANVUVVEIBR2(String address, int fromIndex, int count){
        TypedKVEntry[] kvDataEntries = eventContext.getLedger().getDataEntries(ledgerHash, address, fromIndex, count);
        if (kvDataEntries == null || kvDataEntries.length == 0) {
            return null;
        }
        return (kvDataEntries[0].getValue() instanceof String) ? (String) kvDataEntries[0].getValue() : null;
    }

    /**
     * 校验并转换请求参数
     *
     * @param reqStr
     * @return
     */
    private QANVUVVEIBR2 validQANVUVVEIBR2(String reqStr, StringBuilder errorMsg) {
        //转换请求参数
        QANVUVVEIBR2 obj = null;
        try {
            obj = JSON.parseObject(reqStr, QANVUVVEIBR2.class);
        } catch (Exception e) {
            errorMsg.append("QANVUVVEIBR2不能为null");
            return null;
        }

        //校验请求参数
        if (obj == null){
            errorMsg.append("QANVUVVEIBR2不能为null");
        }
        if(StringUtils.isEmpty(obj.getUserName())){errorMsg.append("userName不能为null");}
        if(obj.getAge()==null){errorMsg.append("age不能为null");}
        

        return obj;
    }

}
