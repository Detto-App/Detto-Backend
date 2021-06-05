# Detto-Backend

## About 
Detto is a project management and evaluation tool which can be used by both lectures and students to manage all the phases of project management and also allows the lecturer to maintain fine grain control over the projects and helps them to evaluate the project better.

## Detto-FrontEnd
This github repository referes to the frontend code of this project https://github.com/Detto-App/detto

## Requirements 
- OS : Ubuntu 20.0.4
- RAM & Storage : 2GB Ram and 32 GB Storage

## Steps to Deploy Server

- Step 1 : Install Nginx Server https://ubuntu.com/tutorials/install-and-configure-nginx#2-installing-nginx
- Step 2 : Install Mongo DB https://docs.mongodb.com/manual/tutorial/install-mongodb-on-ubuntu/
- Step 3 : Git pull & gradle build inside the git folder <br> 
           ``` 
           gradle build
           ```
- Step 4 : Copy the built jar file into the main folder <br>
           ```
            cp build/libs/detto-0.0.1*all.jar detto.jar
            ```
- Step 5 : Jar file start command <br>
            ```
            nohup java -server -XX:+UnlockExperimentalVMOptions -XX:+UseContainerSupport -XX:InitialRAMFraction=2 -XX:MinRAMFraction=2 -XX:MaxRAMFraction=2 -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseStringDeduplication -jar detto.jar &^C
            ```
