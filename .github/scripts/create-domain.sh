#!/bin/bash
set -euo pipefail

curl -v -X POST http://$(docker compose port gpas 8080)/create-domain?domain=MII
