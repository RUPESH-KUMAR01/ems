#!/bin/bash

set -e

echo "Formatting..."
mvn spotless:apply

echo "Compiling..."
mvn clean compile

echo "Running tests..."
mvn test

echo "Running Checkstyle..."
mvn checkstyle:check

echo "Running PMD..."
mvn pmd:check

echo "Running SpotBugs..."
mvn spotbugs:check

echo ""
echo "✅ All checks passed"