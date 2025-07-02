#!/bin/bash
#
# Start script for insolvency-data-api

PORT=8080

exec java -jar -Dserver.port="${PORT}" -XX:MaxRAMPercentage=80 "insolvency-data-api.jar"
