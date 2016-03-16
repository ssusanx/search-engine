# search-engine

This project includes 3 subprojects: search, rank, query(web application)

#To build search project 
cd search
gradlew clean build

#To Run crawler
java -jar build/libs/search-engine-1.0.0.jar -d "# depth" -u "web site name"

#To build rank jar
cd rank 
gradlew clean build

#To run ranking project (tf idf and link analysis)
java -jar build/libs/rank-1.0.0.jar -r 

#The query project is a Spring boot web app 
deployed to port 9090, could be overriden in application.properties

#to build 
cd query 
gradlew clean build

#To deploy
java -jar build/libs/search-query-1.0.0.jar 

