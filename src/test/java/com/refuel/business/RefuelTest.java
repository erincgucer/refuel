package com.refuel.business;

import com.refuel.business.exception.RefuelDataException;
import com.refuel.model.RefuelInfo;
import com.refuel.view.RefuelController;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by egucer on 01-Feb-19.
 */
public class RefuelTest {

    private static File exampleFile;

    @BeforeClass
    public static void readFileBeforeClass() throws Exception {
        exampleFile = new File("src/test/resources/data/refuel_test_input.txt");
    }

    @Test(expected = RefuelDataException.class)
    public void negativeFuelPriceShouldThrowException() {
        RefuelController refuelController = new RefuelController();
        refuelController.generateRefuelnfo("98|-1.319|50.56|01.01.2016");
    }

    @Test(expected = RefuelDataException.class)
    public void negativeFuelAmountShouldThrowException() {
        RefuelController refuelController = new RefuelController();
        refuelController.generateRefuelnfo("98|1.319|-50.56|01.01.2016");
    }

    @Test(expected = RefuelDataException.class)
    public void invalidFuelPriceShouldThrowException() {
        RefuelController refuelController = new RefuelController();
        refuelController.generateRefuelnfo("98|asd|50.56|01.01.2016");
    }

    @Test(expected = RefuelDataException.class)
    public void invalidFuelAmountShouldThrowException() {
        RefuelController refuelController = new RefuelController();
        refuelController.generateRefuelnfo("98|1.319|asd|01.01.2016");
    }

    @Test
    public void refuelInfoShouldBeSetCorrectlyWithDots() {
        RefuelController refuelController = new RefuelController();
        RefuelInfo refuelInfo = refuelController.generateRefuelnfo("98|1.319|50.56|01.01.2016");
        assertThat(refuelInfo.getFuelName(), is(equalTo("98")));
        assertThat(refuelInfo.getFuelPrice(), is(equalTo(1.319)));
        assertThat(refuelInfo.getFuelAmount(), is(equalTo(50.56)));
        assertThat(refuelInfo.getRefuellingDate(), is(equalTo(LocalDate.of(2016, 1, 1))));
    }

    @Test
    public void refuelInfoShouldBeSetCorrectlyWithCommas() {
        RefuelController refuelController = new RefuelController();
        RefuelInfo refuelInfo = refuelController.generateRefuelnfo("98|1,319|50,56|01.01.2016");
        assertThat(refuelInfo.getFuelName(), is(equalTo("98")));
        assertThat(refuelInfo.getFuelPrice(), is(equalTo(1.319)));
        assertThat(refuelInfo.getFuelAmount(), is(equalTo(50.56)));
        assertThat(refuelInfo.getRefuellingDate(), is(equalTo(LocalDate.of(2016, 1, 1))));
    }

    @Test
    public void refuelInfoListShouldBeFilledCorrectly() throws Exception {
        RefuelController refuelController = new RefuelController();
        refuelController.fillRefuelInfoList(exampleFile);
        List<RefuelInfo> refuelInfoList = refuelController.getRefuelInfoList();
        assertThat(refuelInfoList, hasSize(5));
        assertThat(refuelInfoList, hasItem(new RefuelInfo("98", 1.319, 50.56, LocalDate.of(2016, 1, 1))));
        assertThat(refuelInfoList, hasItem(new RefuelInfo("95", 1.319, 45.32, LocalDate.of(2016, 1, 15))));
        assertThat(refuelInfoList, hasItem(new RefuelInfo("95", 1.319, 5.00, LocalDate.of(2016, 4, 1))));
        assertThat(refuelInfoList, hasItem(new RefuelInfo("D", 1.219, 5.00, LocalDate.of(2016, 2, 1))));
        assertThat(refuelInfoList, hasItem(new RefuelInfo("E85", 0.95, 15.12, LocalDate.of(2016, 11, 12))));
    }

    @Test
    public void minMaxValuesOfRefuelGroupShouldBeCorrect() throws Exception {
        RefuelController refuelController = new RefuelController();
        List<RefuelInfo> refuelInfoList = new ArrayList<RefuelInfo>();
        refuelInfoList.add(new RefuelInfo("98", 3.0, 50.00, LocalDate.of(2016, 1, 1)));
        refuelInfoList.add(new RefuelInfo("95", 2.0, 40.00, LocalDate.of(2016, 1, 1)));
        refuelInfoList.add(new RefuelInfo("95", 2.0, 20.00, LocalDate.of(2016, 4, 1)));
        refuelInfoList.add(new RefuelInfo("D", 1.5, 10.00, LocalDate.of(2016, 2, 1)));
        refuelInfoList.add(new RefuelInfo("E85", 2.0, 30.00, LocalDate.of(2016, 11, 1)));

        refuelController.setRefuelInfoList(refuelInfoList);
        Map<Integer, Double> refuelingByMonth = refuelController.createRefuelingByMonth(RefuelConstants.FUEL_TYPE_ALL);
        assertThat(refuelController.getMaxOfRefuelingByMonth(refuelingByMonth), is(equalTo(230.0)));
        Map<Integer, Double> refuelingByMonth2 = refuelController.createRefuelingByMonth("95");
        assertThat(refuelController.getMaxOfRefuelingByMonth(refuelingByMonth2), is(equalTo(80.0)));

        Map<Integer, Double> refuelingByMonth3 = refuelController.createRefuelingByMonth(RefuelConstants.FUEL_TYPE_ALL);
        assertThat(refuelController.getMinOfRefuelingByMonth(refuelingByMonth3), is(equalTo(15.0)));
        Map<Integer, Double> refuelingByMonth4 = refuelController.createRefuelingByMonth("95");
        assertThat(refuelController.getMinOfRefuelingByMonth(refuelingByMonth4), is(equalTo(40.0)));
    }

}
