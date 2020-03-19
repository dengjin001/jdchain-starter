package contract.service;

import com.jd.blockchain.contract.Contract;
import com.jd.blockchain.contract.ContractEvent;

/**
 * Author cbc8df10-ab0b-4604-8d5b-0b7f57c5de0e
 * Date  2020-03-19 14:43:03
 */
@Contract
public interface QANVUVVEIBR2Contract {

    @ContractEvent(name = "writeQANVUVVEIBR2")
    String writeQANVUVVEIBR2(String address, String requestJson);

    @ContractEvent(name = "readQANVUVVEIBR2")
    String readQANVUVVEIBR2(String address, int fromIndex, int count);

}
