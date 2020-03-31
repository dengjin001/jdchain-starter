package com.jd.chain.contract;

/*广互链码 add by syf 2019.09.03*/

import com.jd.blockchain.contract.Contract;
import com.jd.blockchain.contract.ContractEvent;

@Contract
public interface Guanghu {
    @ContractEvent(name = "putval")
    String putval(String address, String account, String content);

    @ContractEvent(name = "putval1")
    String putval(String address, String account, String content, Long time);

    @ContractEvent(name = "getval")
    String getval(String address, String account);

    @ContractEvent(name = "putvalBif")
    String putvalBifurcation(String address, String account, String content, String isHalf);
}
