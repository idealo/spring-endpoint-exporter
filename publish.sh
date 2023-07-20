#!/bin/bash
mvn -B -ntp -Ppublish-ghcr -DskipTests install spring-boot:build-image
mvn -B -ntp -Pnative -Ppublish-ghcr -DskipTests install spring-boot:build-image
