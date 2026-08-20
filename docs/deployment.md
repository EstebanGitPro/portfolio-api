# Deployment — VPS with Dokploy

The host already runs Alertax, FlightHours and MotoGo. Dokploy administers it and
Traefik holds ports 80 and 443, so this stack neither publishes ports nor brings a
reverse proxy of its own: Traefik reaches the API across the `dokploy-network`
overlay and terminates TLS the same way it already does for the other apps.

```
internet ─► cloudflare ─► traefik (dokploy) ─► portfolio-api ─► mongodb
                                 :80 :443       no ports         no ports
                                              dokploy-network    backend only
```

`backend` is declared `internal`, so MongoDB has no route to the internet and none
from it. The API keeps its own egress through `dokploy-network`, which the geo
lookup in `IpGuideGeoResolver` needs.

## Configuration lives in mounted files, not in the environment

Secrets are uploaded over SFTP to `/home/deployer/projects/portfolio/config` on the host and mounted read
only. Nothing sensitive appears in this repository, in the compose file, or in
`docker inspect`.

| File on the host | Mounted at | Carries |
| --- | --- | --- |
| `.../config/application-prod.yaml` | `/app/config/application-prod.yaml` | MongoDB credentials, CORS origins |
| `.../config/mongo_root_username` | `/run/secrets/mongo_root_username` | The database user |
| `.../config/mongo_root_password` | `/run/secrets/mongo_root_password` | Its password |

Two mechanisms make this work, both verified against the real images:

- The `mongo` image reads `MONGO_INITDB_ROOT_USERNAME_FILE` and
  `MONGO_INITDB_ROOT_PASSWORD_FILE` and takes the credentials from those paths.
- Spring Boot reads `./config/` relative to the working directory — `/app` in this
  image — with higher precedence than anything packaged inside the jar. A mounted
  `application-prod.yaml` overrides the packaged one property by property.

A side benefit: a bcrypt hash or a password containing `$` survives intact. Passed
through compose as an environment variable it would be silently truncated at the
first `$`.

### 1. Create the directory

```bash
mkdir -p /home/deployer/projects/portfolio/config
chmod 700 /home/deployer/projects/portfolio/config
```

### 2. Upload `application-prod.yaml`

```yaml
spring:
  data:
    mongodb:
      host: mongodb
      port: 27017
      database: portfolio-db
      username: portfolio
      password: <the same password as the file below>
      authentication-database: admin

app:
  cors:
    # The front end origins, comma separated. No wildcard: the API sends
    # credentials, and "*" would let any site on the internet call it.
    allowed-origins: https://<front-domain>
```

### 3. Upload the two credential files

Each file holds the value and nothing else — **no trailing newline**, which is why
`printf` is used instead of `echo`:

```bash
printf 'portfolio' > /home/deployer/projects/portfolio/config/mongo_root_username
openssl rand -base64 32 | tr -d '\n' > /home/deployer/projects/portfolio/config/mongo_root_password
chmod 600 /home/deployer/projects/portfolio/config/mongo_root_*
```

The password in `application-prod.yaml` must match `mongo_root_password` exactly.
They are two views of the same credential: one for the database that creates the
user, one for the client that logs in.

## Deploying through Dokploy

1. **Create a Compose application** pointing at
   `github.com/EstebanGitPro/portfolio-api`, branch `feature/hu-001` (or `main`
   once merged), with compose path `docker-compose.prod.yaml`.
2. **Attach the domain** `eportfolioapi.rbsuport.com` to service `portfolio-api`
   on port `8086`, and let Dokploy handle the certificate.
3. **Deploy.** The first build compiles with Maven inside the image, so it takes a
   few minutes.

There are no Traefik labels in the compose file, and that is deliberate. The
applications already running on this host carry `"Labels": {}` — Dokploy does not
route through swarm labels, it writes its own dynamic configuration for Traefik.
Hand-written labels would be inert. The domain is attached in the Dokploy UI and
Dokploy takes care of the rest.

The bind mounts follow the convention this host already uses: the Alertax backend
mounts `/home/deployer/projects/alertax-backend/config/prod-config.json` into
`/app/config/`, and this stack does the same under its own project directory.

## Cloudflare

The domain resolves to Cloudflare, not straight to the VPS. Two consequences:

- The certificate has to be negotiated through the proxy. If issuance stalls, set
  the record to DNS-only (grey cloud) until the certificate exists, then turn the
  proxy back on — or issue a Cloudflare Origin Certificate and let Traefik serve
  that instead of asking Let's Encrypt.
- SSL mode must be **Full (strict)**. *Flexible* would leave the leg between
  Cloudflare and the VPS unencrypted while the browser shows a padlock.

## Verify

```bash
curl -fsS https://eportfolioapi.rbsuport.com/api/projects        # 200, public
curl -o /dev/null -w '%{http_code}\n' \
     https://eportfolioapi.rbsuport.com/actuator/health          # 404, not public
```

From the host, where the actuator is still reachable:

```bash
docker exec $(docker ps -qf name=portfolio-api) \
  curl -fsS http://localhost:8086/actuator/health
```

## Memory

The host runs **without swap**. When memory runs out the kernel picks a victim,
and it does not have to be this container — it can be a MySQL belonging to another
application. Both services therefore declare limits: 768m for the API, 1g for
MongoDB. The image sizes its heap with `-XX:MaxRAMPercentage=75.0`, which only
means something because that limit exists.

## Backups

The data lives in the `mongo_data` volume; nothing else in the stack holds state.

```bash
MONGO_CONTAINER=$(docker ps -qf name=portfolio-mongodb)
docker exec $MONGO_CONTAINER mongodump \
  --username "$(cat /home/deployer/projects/portfolio/config/mongo_root_username)" \
  --password "$(cat /home/deployer/projects/portfolio/config/mongo_root_password)" \
  --authenticationDatabase admin --archive=/tmp/dump.gz --gzip
docker cp $MONGO_CONTAINER:/tmp/dump.gz ./portfolio-$(date +%F).gz
```

Worth a cron job next to the Alertax backups that already run on this host.

## The open question: authentication

The application has **no authentication of its own**. `/api/admin/*` creates, edits
and deletes projects, and nothing in the code checks who is calling.

With Caddy that gap was covered by HTTP Basic at the edge. Under Dokploy it is not
covered at all unless the equivalent is configured in Traefik. On a host shared
with three other production systems, this is the first thing to close: Spring
Security with a single admin user, or an API key filter, moves the boundary into
the application where it belongs.
