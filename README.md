# search-engine

This project includes 2 subprojects

#To build search 
cd search
gradlew clean build

#To Run crawler
java -jar build/libs/search-engine-1.0.0.jar -d "# depth" -u "web site name"

#To build data dump jar
cd data 
gradlew clean build

#To run data dump
java -jar build/libs/data-1.0.0.jar 
