#!/usr/bin/env bash
# Deploy one Tertulia microservice on the EC2 host (run via SSM or SSH).
# Usage:
#   deploy-ms.sh <image> <region> <secret_arn> <wallet_bucket> <wallet_key> \
#                <container_name> <host_port> <container_port> <health_path>
# health_path examples: /categorias  /health  /auth/login
set -Eeuo pipefail

image="${1:?image is required}"
region="${2:?AWS region is required}"
secret_arn="${3:?Oracle credentials secret ARN is required}"
wallet_bucket="${4:?Oracle wallet bucket is required}"
wallet_key="${5:?Oracle wallet object key is required}"
container_name="${6:?container name is required}"
host_port="${7:?host port is required}"
container_port="${8:?container port is required}"
health_path="${9:?health path is required}"

if [[ ! "$region" =~ ^[a-z0-9-]+$ \
  || ! "$secret_arn" =~ ^arn:aws:secretsmanager:${region}:[0-9]{12}:secret:[A-Za-z0-9/_+=.@-]+$ \
  || ! "$wallet_bucket" =~ ^[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]$ \
  || ! "$wallet_key" =~ ^[A-Za-z0-9._/-]{1,1024}$ \
  || ! "$container_name" =~ ^[a-zA-Z0-9][a-zA-Z0-9_.-]*$ \
  || ! "$host_port" =~ ^[0-9]+$ \
  || ! "$container_port" =~ ^[0-9]+$ \
  || ! "$health_path" =~ ^/ ]]; then
  echo "Invalid deploy-ms arguments." >&2
  exit 2
fi

docker_config="$(mktemp -d /tmp/tertulia-ecr-docker.XXXXXX)"
env_file="$(mktemp /tmp/tertulia-ms-env.XXXXXX)"
export DOCKER_CONFIG="$docker_config"
cleanup() {
  if [[ -n "${docker_config:-}" ]]; then
    rm -rf -- "$docker_config"
    docker_config=""
  fi
  if [[ -n "${env_file:-}" ]]; then
    rm -f -- "$env_file"
    env_file=""
  fi
}
trap cleanup EXIT

if [[ "$image" == *".dkr.ecr."* ]]; then
  registry="${image%%/*}"
  aws ecr get-login-password --region "$region" \
    | docker login --username AWS --password-stdin "$registry" >/dev/null
  docker pull "$image"
  docker logout "$registry" >/dev/null 2>&1 || true
  rm -f -- "$docker_config/config.json"
elif ! docker image inspect "$image" >/dev/null 2>&1; then
  echo "Image not found locally and is not an ECR URI: $image" >&2
  exit 1
fi

aws secretsmanager get-secret-value \
  --secret-id "$secret_arn" \
  --query SecretString \
  --output text \
  --region "$region" \
  | python3 -c '
import json
import sys

secret = json.load(sys.stdin)
with open(sys.argv[1], "w", encoding="utf-8") as output:
    for key in ("DB_URL", "DB_USERNAME", "DB_PASSWORD"):
        value = secret.get(key)
        if not isinstance(value, str) or "\n" in value or "\r" in value:
            raise SystemExit(f"Secret is missing a valid {key} value.")
        output.write(f"{key}={value}\n")
    jwt = secret.get("JWT_SECRET")
    if isinstance(jwt, str) and jwt and "\n" not in jwt and "\r" not in jwt:
        output.write(f"JWT_SECRET={jwt}\n")
    else:
        output.write("JWT_SECRET=ForoJwtSecretKey2026DuocFullstackIIIExp1Semana2Segura\n")
' "$env_file"
printf 'AWS_REGION=%s\nORACLE_WALLET_S3_BUCKET=%s\nORACLE_WALLET_S3_KEY=%s\nTNS_ADMIN=/app/wallet\n' \
  "$region" "$wallet_bucket" "$wallet_key" >> "$env_file"
# Tres MS en la misma ADB: pool chico para no saturar Free Tier / ACL.
printf 'SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=3\n' >> "$env_file"
printf 'SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT=60000\n' >> "$env_file"
chmod 600 "$env_file"

backup=""
new_container_attempted=false
rollback() {
  status=$?
  trap - EXIT
  if (( status != 0 )); then
    if [[ "$new_container_attempted" == true ]] && docker inspect "$container_name" >/dev/null 2>&1; then
      docker rm -f "$container_name" >/dev/null
    fi
    if [[ -n "$backup" ]] && docker inspect "$backup" >/dev/null 2>&1; then
      docker rename "$backup" "$container_name"
      docker start "$container_name"
      echo "MS deployment failed; previous container restored ($container_name)." >&2
    fi
  fi
  cleanup
  exit "$status"
}
trap rollback EXIT

if docker inspect "$container_name" >/dev/null 2>&1; then
  backup="${container_name}-rollback-$(date +%s%N)"
  docker rename "$container_name" "$backup"
  docker stop "$backup" >/dev/null || true
fi

new_container_attempted=true
docker run -d \
  --name "$container_name" \
  --restart unless-stopped \
  --publish "127.0.0.1:${host_port}:${container_port}" \
  --env-file "$env_file" \
  "$image" >/dev/null
rm -f -- "$env_file"
env_file=""

healthy=false
for attempt in $(seq 1 60); do
  code="$(curl --silent --show-error --max-time 10 \
    -o /dev/null -w '%{http_code}' \
    "http://127.0.0.1:${host_port}${health_path}" || true)"
  if [[ "$code" =~ ^(200|401|403|405)$ ]]; then
    healthy=true
    break
  fi
  sleep 5
done
if [[ "$healthy" != true ]]; then
  echo "Health check failed for $container_name on ${health_path} (last HTTP ${code:-none})." >&2
  docker logs "$container_name" >&2 || true
  exit 1
fi

if [[ -n "$backup" ]]; then
  docker rm "$backup" >/dev/null
fi
backup=""
cleanup
trap - EXIT
echo "MS deployed successfully: $container_name <- $image"
