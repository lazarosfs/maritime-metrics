# Maritime Metrics

## Table of Contents
- [Project Overview](#project-overview)
- [Setup Instructions](#setup-instructions)
- [Usage](#usage)
- [API EndPoints](#api-endpoints)
- [Posting Data](#posting-data)
- [Example GET Requests](#example-get-requests)
- [Error Handling](#error-handling)
- [Logging](#logging)
- [Dependencies](#dependencies)
- [Documentation Links](#documentation-links)
- [Assumptions](#assumptions)
- [Contact](#contact)

## Project Overview

Maritime Metrics is a tool designed to process and analyze maritime data metrics. It provides functionalities for importing CSV data, analyzing metrics, and generating coverage reports. This application utilizes Spring Boot for the backend and follows best practices for clean code and design patterns. It was created as an assignment for DeepSea Technologies Interview Process.

## Setup Instructions

To set up the project locally, follow these steps:

1. **Clone the Repository**
   ```bash
   git clone https://github.com/lazarosfs/maritime-metrics.git
   cd maritime-metrics
   ```
   
2. **Install Dependencies** 

Ensure you have Gradle installed on your machine. Then run:
    ```bash
    ./gradlew build
    ```
3. **Run the Application**

You can run the application using the following command:

   ```bash
   ./gradlew bootRun
   ```
Or you can use the built artifact instead:

```bash
java -jar build/libs/maritime-metrics-0.0.1-SNAPSHOT.jar
```
4.**Test the Application**

To run the tests, you can use the following command:
```bash
./gradlew test
```

5. **Access the Application**

Open your web browser and navigate to http://localhost:8080

## Usage

Once the application is running, you can access various API endpoints listed below

## API Endpoints

### Maritime Metric Controller

- **Get Speed Differences by Vessel Code**:  
  `GET /api/metrics/{vesselCode}/speed-difference`

- **Get Problem Frequencies Sorted**:  
  `GET /api/metrics/{vesselCode}/problems/frequencies`

- **Compare Vessel Compliance**:  
  `GET /api/metrics/more-compliant?vesselCode1={vesselCode1}&vesselCode2={vesselCode2}`

- **Get Summary Metrics for Vessel in Period**:  
  `GET /api/metrics/{vesselCode}/summary-metrics?startDate={startDate}&endDate={endDate}`

- **Get Consecutive Problematic Groups of waypoints for Vessel**:

  `GET /api/metrics/{vesselCode}/problems/groups`

- **Get All Data** `GET /api/metrics/all`

- **Get All Vessel Codes**:
  `GET /api/metrics/vessel-codes`

- **Get All Valid Data by Vessel Code**:  
  `GET /api/metrics/{vesselCode}/valid`

- **Get All Invalid Data by Vessel Code**:  
  `GET /api/metrics/{vesselCode}/invalid`

- **Get All Missing Data by Vessel Code**:  
  `GET /api/metrics/{vesselCode}/missing`

- **Get All Below Zero Data by Vessel Code**:  
  `GET /api/metrics/{vesselCode}/below-zero`

- **Get All Outlier Data by Vessel Code**:  
  `GET /api/metrics/{vesselCode}/outlier`

- **Get Speed Outliers**:  
  `GET /api/metrics/outliers/speed?vesselCode={vesselCode}&speedThreshold={speedThreshold}`

- **Get Fuel Outliers**:
  `GET /api/metrics/outliers/fuel?vesselCode={vesselCode}&fuelThreshold={fuelThreshold}`

### Maritime Metric CSV Controller
- **Import CSV Data**:  
  `POST /api/csv/import`

## Posting Data

To post the vessel_data.csv file (or another CSV file) to the API, you can use the following curl command:
```bash
curl -X POST -F "file=@src/main/resources/data/vessel_data.csv" http://localhost:8080/api/csv/import
```

## Example GET Requests

### 1. Get Speed Difference for Vessel 3001
To retrieve the speed differences for vessel 3001, use the following curl command:
```bash
curl -X GET "http://localhost:8080/api/metrics/3001/speed-difference"
```
### 2. Get Problem Frequencies for Vessel 19310
To get problem frequencies for vessel 19310, use:
```bash
curl -X GET "http://localhost:8080/api/metrics/19310/problems/frequencies"
```
### 3. Get Compliance Calculation Comparison Between Vessels 3001 and 19310
To compare the compliance between vessels 3001 and 19310, run:
```bash
curl -X GET "http://localhost:8080/api/metrics/more-compliant?vesselCode1=3001&vesselCode2=19310"
```
### 4. Get Summary Metrics for Vessel 3001 for Specific Start Date and End Date
To get summary metrics for vessel 3001 for a specified date range, use:
```bash
curl -X GET "http://localhost:8080/api/metrics/3001/summary-metrics?startDate=2023-06-01T00:01:00&endDate=2023-06-01T00:03:00"
```
Replace startDate and endDate with the desired date range in ISO 8601 format.
### 5. Get Consecutive Problematic Groups for Vessel 19310 of Consecutive Waypoints
To retrieve groups of consecutive problematic waypoints for vessel 19310 for a specific problem type (e.g., missing, belowzero, or outlier), use:
```bash
curl -X GET "http://localhost:8080/api/metrics/19310/problems/groups?problemType=missing"
```
Replace problemType with the desired type (e.g., missing, belowzero, or outlier) as needed.

## Error Handling
The API uses standard HTTP response codes to indicate the success or failure of requests:

- **200 OK**: The request was successful.
- **400 Bad Request**: The request was invalid or cannot be served.
- **404 Not Found**: The requested resource could not be found.
- **500 Internal Server Error**: An error occurred in the server.

## Logging

The Maritime Metrics API implements logging using Java's built-in logging framework. Logs are generated for various events in the application, such as:

- API request handling
- Data import events
- Validation errors
- Internal processing messages

### Console Logging

Logs are displayed in the console during application runtime, which can help monitor application behavior and troubleshoot issues. The logging level can be adjusted as needed to capture more or less detail.

### Example Logs

Here’s an example of what the logs may look like in the console:
```plaintext
INFO: CSV import request received
WARNING: Power value is missing: null
SEVERE: Unexpected error during CSV import: Invalid data format
```

## Dependencies
The application depends on the following libraries:
- Spring Boot: For building the web application.
- Spring Data JPA: For database interaction.
- Lombok: For reducing boilerplate code.
- H2 Database: In-memory database for development and testing.

## Documentation Links
- [View Coverage Report](htmlReport/index.html)
- [View Javadocs](build/docs/javadoc/index.html)

## Assumptions
- The application is assumed to be run in a Java environment with the specified version or higher.
- The CSV files are expected to be formatted as per the vessel_data.csv sample which was provided.
- The invalid data filtering classifies the problems in 3 types: missing, belowzero, outlier. 
- A row is considered invalid if one or more types of problems are detected.
- Outliers for Speed and Fuel are based in predefined thresholds (SPEED_PERCENTAGE_THRESHOLD, FUEL_PERCENTAGE_THRESHOLD currently = 50%)
- Compliance Comparison is based in Median Speed Difference Calculation for the two Vessels

## Contact

For any queries or issues, please contact lousou76@gmail.com
