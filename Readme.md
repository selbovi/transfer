# RESTful service for money transfers between accounts.


## drawbacks found:
* Lombok;
* Balances stored in double;
* Commit in final block, no rollbacks;
* Poor REST: plain text responses, query params, GET for transfers;
* Double.compare instead of a direct comparison;
* No params validation;
* Interfaces/*Impl classes without a visible reason;
* Unit tests are functional ones in fact.
* And the critical issue - there is no interface for creating accounts and no pre-created ones. So, basically, the main functionality is unreachable.

## About testing:

##### test proves that parallel transfer operations, between same accounts, do not lead for data integrity loss   

Here we transfer 1 unit of money from first account to the second each time. Transfer operations go in parallel. 
At the end we assert that all money from first account were transferred to the second account with no loss or exception. 

> mvn -Dtest=TransferTest#concurrentWithdrawalTest test

##### low balance, incorrect or same accounts, invalid transfer amount exceptions are handled (4 tests each per exception)

Extraordinary situations are properly handled. 
 
> mvn -Dtest=TransferTest#throw* test

##### tests that ensure successful end2end interaction via rest api

Few tests to show interaction through rest api, including successful and failing transfer attempts.
Response body always contains information about operation. And the status code is always 200, irrespectively if transfer was 
successful or not in case service could correctly process the request.

> mvn -Dtest=ControllerTest test

##### for fun and demonstration purposes

Game which is also demonstrates services fault tolerance. In this demonstration, random amount of money
transferred between randomly choosen accounts. Transfer operations between random accounts pass in parallell.
At the end of the game, we assert that total amount of money on the accounts before and after the game are same. 

> mvn -Dtest=ControllerTest#gameOfDrunkTransferAttempts test

##### run all tests

> mvn clean test  
