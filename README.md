## Project description
The Football Events Statistics API is a service that collects and analyses the results of football matches and provides statistics on teams. The application supports different types of events (matches) and allows to obtain statistics for the indicated teams.
<br>
Location src/main/resources/files stores the input data, the input-output data and the expected results used to perform assertions on the data. The file messages.txt stores the input for the tests. The result.txt stores the data expected for testing. The file output_final.txt stores the data returned by the endpoint and written to the given file.
## Project configuration
You should install the Java JDK from the software developer's website (https://www.oracle.com/pl/java/technologies/downloads/). Then create a JAVA_HOME System Variable and assign to it the directory where you installed the Java JDK 17.0.9.<br>
### Docker configuration:<br>
```
docker run --name football -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=123 -e POSTGRES_DB=football -p 5432:5432 -d postgres
```
```
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management
```
### Example of endpoint event operation:
```
http://localhost:8080/api/v1/event
```
#### RESULT:
```
{
  "type": "RESULT",
  "result": {
    "home_team": "Bayern",
    "away_team": "Milan",
    "home_score": 3,
    "away_score": 1
  }
}
```
#### GET STATISTICS:
```
{
  "type": "GET_STATISTICS",
  "get_statistics": {
    "teams": ["Bayern", "Milan"]
  }
}
```
