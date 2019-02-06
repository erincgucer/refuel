package com.refuel.view;

import com.refuel.MainApp;
import com.refuel.business.RefuelConstants;
import com.refuel.business.exception.RefuelDataException;
import com.refuel.business.watcher.FileWatcher;
import com.refuel.model.RefuelInfo;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by egucer on 01-Feb-19.
 */
public class RefuelController {

    // Reference to the main application.
    private MainApp mainApp;

    // Reference to the uploaded data file.
    private File dataFile;

    // Reference to the file watcher.
    private FileWatcher dataFileWatcher;

    // Reference to the constructed refuel info objects.
    private List<RefuelInfo> refuelInfoList = new ArrayList<>();

    // Reference to the group that contains texts for each bar.
    private Group groupOfPanesAndLabels;

    @FXML
    private Label dataFileLabel;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private BarChart<CategoryAxis, NumberAxis> refuelBarChart;

    @FXML
    private ComboBox<String> fuelTypeComboBox;

    /**
     * Called when the user clicks choose data file.
     */
    @FXML
    private void uploadDataFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Txt Files", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            dataFileLabel.setText("File selected: " + selectedFile.getName());
            dataFile = selectedFile;

            applyFileToGraph(selectedFile);

            stopDataFileWatcher();
            startNewDataFileWatcher();
        } else {
            dataFileLabel.setText("File selection cancelled.");
        }
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        initializeFuelTypeComboBox();
        setDefaultAxisProperties();
        setDefaultRefuelBarChartProperties();
        loadEmptyRefuelBarChart();
    }

    private void initializeFuelTypeComboBox() {
        fuelTypeComboBox.setItems(FXCollections.observableArrayList(RefuelConstants.FUEL_TYPE_ALL));
        fuelTypeComboBox.setValue(RefuelConstants.FUEL_TYPE_ALL);
        fuelTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                generateBarChart(newVal);
            }
        });
    }

    private void setDefaultAxisProperties() {
        xAxis.setLabel("Months");
        yAxis.setLabel("Euro");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
    }

    private void setDefaultRefuelBarChartProperties() {
        refuelBarChart.setLegendVisible(false);
    }

    private void loadEmptyRefuelBarChart() {
        XYChart.Series dataSeries = new XYChart.Series();
        for (Month monthEnum : Month.values()) {
            dataSeries.getData().add(new XYChart.Data((Month.of(monthEnum.getValue())).toString(), 0));
        }
        refuelBarChart.getData().add(dataSeries);
    }

    public void stopDataFileWatcher() {
        if (dataFileWatcher != null) {
            dataFileWatcher.stopThread();
        }
    }

    public void startNewDataFileWatcher() {
        dataFileWatcher = new FileWatcher(dataFile, this);
        dataFileWatcher.start();
    }

    public void applyFileToGraph(File file) {
        try {
            fillRefuelInfoList(file);
        } catch (RefuelDataException dataEx) {
            dataEx.printStackTrace();
            showErrorDialog(dataEx.getMessage());
            return;
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Invalid Data");
            return;
        }

        try {
            fillFuelTypeComboBox();
            generateBarChart(RefuelConstants.FUEL_TYPE_ALL);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("A general exception occurred. Detail: " + e.getMessage());
        }
    }

    public void fillRefuelInfoList(File file) throws Exception {
        refuelInfoList = new ArrayList<>();
        try {
            Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            stream.forEach(s -> refuelInfoList.add(generateRefuelnfo(s)));
        } catch (Exception e) {
            throw e;
        }
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void fillFuelTypeComboBox() {
        Set<String> distinctFuelTypes = refuelInfoList.stream().map(elem -> elem.getFuelName()).collect(Collectors.toSet());
        distinctFuelTypes.add(RefuelConstants.FUEL_TYPE_ALL);

        if (fuelTypeComboBox.getItems() != null) {
            fuelTypeComboBox.getItems().clear();
        }
        fuelTypeComboBox.setItems(FXCollections.observableArrayList(distinctFuelTypes));
        fuelTypeComboBox.setValue(RefuelConstants.FUEL_TYPE_ALL);
    }

    private void generateBarChart(String fuelType) {

        removeOldBarChartData();
        removeOldBarChartDataLabels();

        Map<Integer, Double> refuelingByMonth = createRefuelingByMonth(fuelType);

        Double maxValue = getMaxOfRefuelingByMonth(refuelingByMonth);
        Double minValue = getMinOfRefuelingByMonth(refuelingByMonth);

        setUpperBoundAccordingToMaxValue(maxValue);
        generateDataSeriesWithRefuelingByMonth(refuelingByMonth, maxValue, minValue);
    }

    /**
     * remove old data
     */
    private void removeOldBarChartData() {
        if (refuelBarChart.getData() != null && refuelBarChart.getData().size() > 0) {
            refuelBarChart.getData().remove(0);
        }
    }

    /**
     * remove old data labels
     */
    private void removeOldBarChartDataLabels() {
        if (groupOfPanesAndLabels != null && groupOfPanesAndLabels.getChildren() != null) {
            groupOfPanesAndLabels.getChildren().clear();
        }
    }

    /**
     * put the upperbound a little higher than the maximum so that the value of it can still be seen
     */
    private void setUpperBoundAccordingToMaxValue(Double maxValue) {
        yAxis.setUpperBound(Math.round(maxValue + (maxValue / 10)));
    }

    /**
     * returns money spent on refuelings grouped by month
     */
    public Map<Integer, Double> createRefuelingByMonth(String fuelType) {
        Stream<RefuelInfo> refuelInfoStream = refuelInfoList.stream();
        if (!RefuelConstants.FUEL_TYPE_ALL.equals(fuelType)) {
            refuelInfoStream = refuelInfoStream.filter(r -> r.getFuelName().equals(fuelType));
        }

        return refuelInfoStream.collect(Collectors.groupingBy(refuelInfo -> refuelInfo.getRefuellingDate().getMonthValue(),
                Collectors.summingDouble(refuelInfo -> refuelInfo.getFuelPrice() * refuelInfo.getFuelAmount())));
    }

    /**
     * returns maximum spent money
     */
    public Double getMaxOfRefuelingByMonth(Map<Integer, Double> refuelingByMonth) {
        return refuelingByMonth.entrySet().stream().max(Map.Entry.comparingByValue()).get().getValue();
    }

    /**
     * returns minimum spent money
     */
    public Double getMinOfRefuelingByMonth(Map<Integer, Double> refuelingByMonth) {
        return refuelingByMonth.entrySet().stream().min(Map.Entry.comparingByValue()).get().getValue();
    }

    /**
     * generate and add the data to the refuel bar chart
     */
    private void generateDataSeriesWithRefuelingByMonth(Map<Integer, Double> refuelingByMonth, Double maxValue, Double minValue) {
        XYChart.Series dataSeries = new XYChart.Series();
        for (Month monthEnum : Month.values()) {
            Double yValue;
            if (!refuelingByMonth.keySet().contains(monthEnum.getValue())) {
                yValue = new Double(0);
            } else {
                yValue = refuelingByMonth.get(monthEnum.getValue());
            }

            XYChart.Data<String, Number> data = new XYChart.Data((Month.of(monthEnum.getValue())).toString(), yValue);
            data.nodeProperty().addListener(new ChangeListener<Node>() {
                @Override
                public void changed(ObservableValue<? extends Node> ov, Node oldNode, final Node node) {
                    if (node != null) {
                        setNodeStyle(data, maxValue, minValue);
                        displayLabelForData(data);
                    }
                }
            });
            dataSeries.getData().add(data);
        }

        refuelBarChart.getData().add(dataSeries);
    }

    /**
     * coloring for different data values
     */
    private void setNodeStyle(XYChart.Data<String, Number> data, Double maxValue, Double minValue) {
        Node node = data.getNode();
        if (data.getYValue().doubleValue() == maxValue.doubleValue()) {
            node.setStyle("-fx-bar-fill: -fx-max-value;");
        } else if (data.getYValue().doubleValue() == minValue.doubleValue()) {
            node.setStyle("-fx-bar-fill: -fx-min-value;");
        } else {
            node.setStyle("-fx-bar-fill: -fx-middle-value;");
        }
    }

    /**
     * spent money will be displayed on top of the bars of the chart
     */
    private void displayLabelForData(XYChart.Data<String, Number> data) {

        DecimalFormat df = new DecimalFormat("0.000");
        df.setRoundingMode(RoundingMode.CEILING);

        final Node node = data.getNode();
        final Text dataText = new Text(df.format(data.getYValue()) + "");
        node.parentProperty().addListener(new ChangeListener<Parent>() {
            @Override
            public void changed(ObservableValue<? extends Parent> ov, Parent oldParent, Parent parent) {
                Group parentGroup = (Group) parent;
                if (parentGroup != null && parentGroup.getChildren() != null) {
                    parentGroup.getChildren().add(dataText);
                    groupOfPanesAndLabels = parentGroup;
                }
            }
        });

        node.boundsInParentProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> ov, Bounds oldBounds, Bounds bounds) {
                dataText.setLayoutX(
                        Math.round(
                                bounds.getMinX() + bounds.getWidth() / 2 - dataText.prefWidth(-1) / 2
                        )
                );
                dataText.setLayoutY(
                        Math.round(
                                bounds.getMinY() - dataText.prefHeight(-1) * 0.5
                        )
                );
            }
        });
    }

    /**
     * creates Refuelnfo object from the line of string
     */
    public RefuelInfo generateRefuelnfo(String line) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        List valueList = Stream.of(line.split("\\|")).map(elem -> new String(elem)).collect(Collectors.toList());

        RefuelInfo refuelInfo = new RefuelInfo();
        refuelInfo.setFuelName((String) valueList.get(0));

        NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
        try {
            Number fuelPrice = format.parse(((String) valueList.get(1)).replace('.', ','));
            refuelInfo.setFuelPrice(fuelPrice.doubleValue());

            Number fuelAmount = format.parse(((String) valueList.get(2)).replace('.', ','));
            refuelInfo.setFuelAmount(fuelAmount.doubleValue());

        } catch (ParseException e) {
            throw new RefuelDataException("There is an error in Fuel Price/Amount Values");
        }

        refuelInfo.setRefuellingDate(LocalDate.parse((String) valueList.get(3), formatter));

        if (refuelInfo.getFuelPrice().doubleValue() < 0 || refuelInfo.getFuelAmount().doubleValue() < 0) {
            throw new RefuelDataException("Values Cannot Be Negative");
        }

        return refuelInfo;
    }

    public MainApp getMainApp() {
        return mainApp;
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public ComboBox<String> getFuelTypeComboBox() {
        return fuelTypeComboBox;
    }

    public List<RefuelInfo> getRefuelInfoList() {
        return refuelInfoList;
    }

    public void setRefuelInfoList(List<RefuelInfo> refuelInfoList) {
        this.refuelInfoList = refuelInfoList;
    }
}
