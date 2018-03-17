#!/bin/bash
function main {
local databaseName="l2g"
local hostAddress="localhost"
local portNumber="27017"
local scriptsFolder="./scripts"

mongo "mongodb://${hostAddress}:${portNumber}/${databaseName}" --eval "db.dropDatabase()"
mongo "mongodb://${hostAddress}:${portNumber}/${databaseName}" ${scriptsFolder}/createSchema.js
mongoimport --db ${databaseName} -h ${hostAddress}:${portNumber} --collection user --file ${scriptsFolder}/user.json --jsonArray
mongoimport --db ${databaseName} -h ${hostAddress}:${portNumber} --collection auth --file ${scriptsFolder}/auth.json --jsonArray
mongoimport --db ${databaseName} -h ${hostAddress}:${portNumber} --collection secret --file ${scriptsFolder}/secret.json --jsonArray
}
main
