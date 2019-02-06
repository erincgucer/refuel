# Refuel

Refuel is a maven project written in Java 8. 

## Usage

When the program starts, click the "Choose Data File" button in the "Data Entry" tab. Select a .txt file containing proper refuel information. If successful, you should see the "File selected" message. Click on the "Refuel Report" tab to see the refuel report. The color coding is as follows;

Maximum value(s) - red
Minimum value(s) - green
All other values - yellow

Selecting a fuel type from the "Fuel Type" combobox will refresh the chart according to the selected fuel type.

After uploading a .txt file, the program listens to the changes in that file. If you change the contents of the file and save, the bar chart will refresh with new values accordingly.

## Data Format

The format of the data in the file that is being uploaded to the system is as follows;

fuelName|fuelPrice|fuelAmount|refuellingDate

Legend;

fuelName - a string

fuel fuelPrice - can be with . or , (1.345; 1,345)

fuel fuelAmount - can be with . or , (50.53; 50,53)

refuellingDate - format is dd.mm.yyyy

Sample Data;

98|1.319|50.56|01.01.2016

## Assumptions
It is assumed that the uploaded file (input .txt file) does not contain the byte order mark that some UTF files might have.

## Credits
Icon made by Freepik from www.flaticon.com
