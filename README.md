1. About

    - Application is divided into several modules (core - for authentication shared models, persistence - for backing store support, authenticator - for authentication shared logic, rest-api - exposable REST api)
    I was trying to create some reusable authentication components that I can further apply with the business logic you provided
    - There is an `application.conf` in resources where the configuration settings reside, default configuration is:
    ```
        service {
          host = "localhost"
          port = "8888"
          tokenValidity = 1 hour
          authServiceName = "L2G Authentication Service"
    
          secretKey = "Z/BNYIOru5gMTECn/Q304g=="
          mongo {
            uri = "mongodb://localhost:27017"
            dbName = "l2g"
          }
        }
    ```
    - Configuration can be updated by another `application.conf` which path can is taken from first argument of the `main` method, that will be merged with the default one.
    - Stack:
        - akka-http with Swagger
        - mongodb as backing store support (reactivemongo)
        - play-json for JSON manipulation
        - log4j for logging
    
2. Prerequisites

    - MongoDB - run `docker-compose up` in `docker` folder and you'll start a mongo db instance running on `mongodb://localhost:27017`
    - Database - in `database` folder there is a `recreate.sh` script that will add the collections and will pre-populate database with some documents:
        2 users: `superuser` and `test1`
        3 secrets: `superUserSecret1` and `superUserSecretShared` (shared with `test1`) for `superuser` user and `test1Secret` for `test1` user
    
    - run application:
        run `sbt l2g-rest-api/assembly` to create a fat jar that will be located in `/repository` folder
        run ./run.sh in `repository` folder and go to `http://localhost:8888/swagger` where all the routes are exposed

3. Application

    AUTHENTICATION
     - user can sign up using `auth/signUp` POST route
        if there is a valid application code, then the user is registered directly (without needing to await for mate permission) and the output of this wil be a token that will expire in an hour - token is the AES encryption of the json representation of the authenticator
            (this is how I get the first Permission to log the first User in - this solution works fine, because can be seen as an application extension and can be used for other special cases)
        if not, then the user is added in the DB but will be active only after the mate approves it
     - user can login using `auth/signIn` POST route
        if the user signed up with activation code, it will get a token that can be further used token that will expire in an hour
        if not, then we check if the mate is logged in and return either a token, if so or a relevant message otherwise
    
    PERMISSIONS (requires user to be authenticated - a valid token needs to be specified)
     - user can see the users he has to give permission to authenticate using `user/permissions` GET route
     - user can behave as a mate and allow other user to authenticate if needed using `user/permission/add` POST route with:
        ```
        {
          "forUserName": "john.doe"
        }
        ```
        
    SECRETS (requires user to be authenticated - a valid token needs to be specified)
     - if authenticated, user can create see his using `secrets` GET
     - if authenticated, user can create see the secrets from other users he is allowed to see `secrets/allowed` GET 
     - if authenticated, user can create a secret using `secrets/add` POST route with:
     ```
        {
          "value": "mySecret"
        }
     ```
     - if authenticated, user can share a secret with another user
     ```
        {
          "secretText": "mySecret",
          "userName": "jane.doe"
        }
     ```


4. TO DO:
    - add possibility for token to reside in one of the headers (right now it's on query string - for faster testing)
    - improve API - some response code can be refined, also the structure of some JSONs in body
    - add tests - did not add any tests so far, my 8 hours ended :) - testing it properly requires some time (have to create in memory mocks for everything), so I was testing entirely using Swagger API
            all common scenarios worked fine
    - improve docker compose file to include a container for the application, depending on mongodb container for a standalone deploy strategy
    - fixed potential bugs that will appear after tests
