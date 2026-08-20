# Deployment — VPS with Docker Compose

The production stack is three containers on one host:

```
internet ──► caddy (80/443)  ──►  portfolio-api (8086)  ──►  mongodb (27017)
             TLS + basic auth     no published ports        no published ports
                  edge network         edge + backend            backend only
```

Only Caddy publishes ports. The API and MongoDB are reachable through the compose
networks and nowhere else, and `backend` is declared `internal`, so the database has no
route to the internet at all.

Files involved:

| File | Role |
| --- | --- |
| `docker-compose.prod.yaml` | The production stack |
| `Caddyfile` | Reverse proxy, TLS, HTTP Basic on the admin surface |
| `src/main/resources/application-prod.yaml` | The `prod` Spring profile |
| `.env.prod` | Secrets and the domain — **never committed** |
| `docker-compose.yaml` | Local development, unchanged |

## 1. DNS first

Point an A record at the VPS **before** starting anything:

```
eportfolioapi.rbsuport.com.   A   <VPS_IP>
```

Caddy asks Let's Encrypt for the certificate the first time it boots. If the name does
not resolve to this machine yet, the request fails and it backs off — verify with
`dig +short eportfolioapi.rbsuport.com` before continuing.

## 2. Firewall

```bash
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

A warning worth keeping in mind: Docker writes its own iptables rules and bypasses ufw.
A container that publishes a port is exposed even when ufw says the port is closed. That
is exactly why `mongodb` and `portfolio-api` publish nothing in this stack — closing the
port in ufw would not have been enough.

## 3. Install Docker and clone

```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER   # log out and back in

git clone https://github.com/EstebanGitPro/portfolio-api.git
cd portfolio-api
```

## 4. Write `.env.prod`

Create it on the server, next to `docker-compose.prod.yaml`. It is gitignored:

```bash
# Domain already pointing at this VPS
API_DOMAIN=eportfolioapi.rbsuport.com

# MongoDB. Generate the password, do not invent one: openssl rand -base64 32
MONGO_USERNAME=portfolio
MONGO_PASSWORD=<generated>
MONGO_DATABASE=portfolio-db

# HTTP Basic for /api/admin/* and the docs, enforced by Caddy.
# Hash it: docker run --rm caddy:2-alpine caddy hash-password --plaintext '<password>'
ADMIN_USER=esteban
ADMIN_PASSWORD_HASH=<hash, with every $ doubled — see below>

# Browser origins allowed to call the API. Comma separated, no wildcard.
APP_CORS_ALLOWED_ORIGINS=https://rbsuport.com,https://www.rbsuport.com

# Docs are off by default: they publish the shape of every endpoint.
API_DOCS_ENABLED=false
SWAGGER_UI_ENABLED=false

IMAGE_TAG=latest
```

```bash
chmod 600 .env.prod
```

### The `$` trap in the bcrypt hash

`caddy hash-password` returns something like `$2a$14$Ku3s...`. Docker Compose reads `$`
as the start of a variable, so pasted as-is the hash silently arrives at Caddy truncated
to `$2a$14` and every login fails with no useful error.

**Double every `$`** when writing it into `.env.prod`:

```
# what caddy printed:   $2a$14$Ku3sIjK9...
ADMIN_PASSWORD_HASH=$$2a$$14$$Ku3sIjK9...
```

Verify what actually reaches the container before trusting it:

```bash
docker compose -f docker-compose.prod.yaml --env-file .env.prod \
  run --rm --no-deps --entrypoint sh caddy -c 'echo $ADMIN_PASSWORD_HASH'
```

It must print the hash with single `$` and the full string intact.

Every one of these variables is mandatory except the ones with a default. The compose
file fails fast with a readable message if one is missing, instead of booting a stack
with an empty database password.

## 5. Up

```bash
docker compose -f docker-compose.prod.yaml --env-file .env.prod up -d --build
```

First boot takes a few minutes: Maven builds inside the image and Caddy negotiates the
certificate.

## 6. Verify

```bash
curl -fsS https://eportfolioapi.rbsuport.com/api/projects            # 200, public
curl -o /dev/null -w '%{http_code}\n' \
     https://eportfolioapi.rbsuport.com/api/admin/projects           # 401, protected
curl -u esteban:<password> \
     https://eportfolioapi.rbsuport.com/api/admin/projects           # 200
curl -o /dev/null -w '%{http_code}\n' \
     https://eportfolioapi.rbsuport.com/actuator/health              # 404, not public
```

Health from the host, where the actuator is still reachable:

```bash
docker compose -f docker-compose.prod.yaml ps
docker compose -f docker-compose.prod.yaml exec portfolio-api \
  curl -fsS http://localhost:8086/actuator/health
```

## 7. Deploying a new version

```bash
git pull
docker compose -f docker-compose.prod.yaml --env-file .env.prod up -d --build
```

Compose recreates only what changed. `restart: unless-stopped` brings everything back
after a reboot of the VPS.

## Backups

The data lives in the `mongo_data` volume. Nothing else in the stack holds state:

```bash
COMPOSE="docker compose -f docker-compose.prod.yaml --env-file .env.prod"

$COMPOSE exec mongodb mongodump \
  --username "$MONGO_USERNAME" --password "$MONGO_PASSWORD" \
  --authenticationDatabase admin --archive=/tmp/dump.gz --gzip
$COMPOSE cp mongodb:/tmp/dump.gz ./backup-$(date +%F).gz
```

Worth a cron job once the site carries real content.

## The open question: authentication

The application has **no authentication of its own**. `/api/admin/*` creates, edits and
deletes projects, and today the only thing guarding it is the HTTP Basic that Caddy
applies at the edge. That is real protection — the request never reaches the app without
credentials — but it lives in the proxy, not in the code:

- anything that reaches the API container directly skips it;
- there is no notion of a user, so there is nothing to audit;
- moving to another proxy or another host means re-implementing the check there.

Spring Security with a single admin user, or an API key filter, would move that boundary
into the application where it belongs. Until then, treat the Caddy credentials as the
only thing between the internet and the project catalogue.
