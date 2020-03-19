package contract.entity;

import java.io.Serializable;

/**
 * Author cbc8df10-ab0b-4604-8d5b-0b7f57c5de0e
 * Date  2020-03-19 14:43:03
 */
public class QANVUVVEIBR2 implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userName;
    private Integer age;
    

    public QANVUVVEIBR2(){
    }

    public void setUserName (String userName) {this.userName = userName;}
    public String getUserName(){ return userName;}
    public void setAge (Integer age) {this.age = age;}
    public Integer getAge(){ return age;}
    
}