package org.models;

import org.baseModels.baseModel;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class CreditLimitModel implements baseModel {
    private String name;
    private String lastName;
    private String address;
    private String postCode;
    private String phone;
    private Double creditLimit;
    private LocalDateTime birthDay;

    public CreditLimitModel(){

    }

    public CreditLimitModel(String name, String lastName, String address, String postCode, String phone, Double creditLimit, LocalDateTime birthDay) {
        this.name = name;
        this.lastName = lastName;
        this.address = address;
        this.postCode = postCode;
        this.phone = phone;
        this.creditLimit = creditLimit;
        this.birthDay = birthDay;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        address = address;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(Double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public LocalDateTime getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(LocalDateTime birthDay) {
        this.birthDay = birthDay;
    }

    @Override
    public  String[] captionElements() {
        return new String[]{"Name","Lastname","Address","PostCode","Phone","Credit Limit","Birthday"};
    }

}
