package com.refuel.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Created by egucer on 01-Feb-19.
 */
public class RefuelInfo {

    private String fuelName;
    private Double fuelPrice;
    private Double fuelAmount;
    private LocalDate refuellingDate;

    public RefuelInfo() {
    }

    public RefuelInfo(String fuelName, Double fuelPrice, Double fuelAmount, LocalDate refuellingDate) {
        this.fuelName = fuelName;
        this.fuelPrice = fuelPrice;
        this.fuelAmount = fuelAmount;
        this.refuellingDate = refuellingDate;
    }

    public String getFuelName() {
        return fuelName;
    }

    public void setFuelName(String fuelName) {
        this.fuelName = fuelName;
    }

    public Double getFuelPrice() {
        return fuelPrice;
    }

    public void setFuelPrice(Double fuelPrice) {
        this.fuelPrice = fuelPrice;
    }

    public Double getFuelAmount() {
        return fuelAmount;
    }

    public void setFuelAmount(Double fuelAmount) {
        this.fuelAmount = fuelAmount;
    }

    public LocalDate getRefuellingDate() {
        return refuellingDate;
    }

    public void setRefuellingDate(LocalDate refuellingDate) {
        this.refuellingDate = refuellingDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefuelInfo refuelInfo = (RefuelInfo) o;
        return Objects.equals(fuelName, refuelInfo.fuelName)
                && Objects.equals(fuelPrice, refuelInfo.fuelPrice)
                && Objects.equals(fuelAmount, refuelInfo.fuelAmount)
                && Objects.equals(refuellingDate, refuelInfo.refuellingDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fuelName, fuelPrice, fuelAmount, refuellingDate);
    }
}
