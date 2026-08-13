#!/bin/sh

# The canonical Maven Wrapper script is maintained by transaction-service.
# This root entry point lets IDEs and the multi-module reactor use it.
exec "$(dirname "$0")/services/transaction-service/mvnw" "$@"
