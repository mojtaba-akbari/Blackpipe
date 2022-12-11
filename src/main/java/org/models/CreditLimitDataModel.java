package org.models;

import org.baseModels.BaseDataModel;
import org.baseModels.BaseDataModelAnnotation;

public class CreditLimitDataModel implements BaseDataModel {
    @BaseDataModelAnnotation(ColumnName = "Name")
    private String name;
    @BaseDataModelAnnotation(ColumnName = "Address")
    private String address;
    @BaseDataModelAnnotation(ColumnName = "Postcode")
    private String postCode;
    @BaseDataModelAnnotation(ColumnName = "Phone")
    private String phone;
    @BaseDataModelAnnotation(ColumnName = "Credit Limit")
    private Double creditLimit;
    @BaseDataModelAnnotation(ColumnName = "Birthday")
    private String birthDay;

    public CreditLimitDataModel() {

    }

    public CreditLimitDataModel(String name, String address, String postCode, String phone, Double creditLimit, String birthDay) {
        this.name = name;
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

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

}
