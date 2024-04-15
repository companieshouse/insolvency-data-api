#!/bin/bash
#
# Start script for insolvency-data-api

PORT=8080

exec java -jar -Dserver.port="${PORT}" "insolvency-data-api.jar"
