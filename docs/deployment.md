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

The API runs as a non-root user, so this file must be readable by it:

```bash
chmod 644 /home/deployer/projects/portfolio/config/application-prod.yaml
```

### 3. Upload the two credential files

Each file holds the value and nothing else — **no trailing newline**, which is why
`printf` is used instead of `echo`:

```bash
printf 'portfolio' > /home/deployer/projects/portfolio/config/mongo_root_username
openssl rand -base64 32 | tr -d '\n' > /home/deployer/projects/portfolio/config/mongo_root_password
chmod 644 /home/deployer/projects/portfolio/config/mongo_root_*
chmod 700 /home/deployer/projects/portfolio/config
```

### Why the files are 644 and the directory is 700

Neither container runs as root. MongoDB's entrypoint drops to the `mongodb` user
before it reads the `_FILE` paths, and the API runs as `spring` (uid 1001). A file
mode of `600` owned by root is unreadable to both, and the failure is not subtle:

```
docker-entrypoint.sh: line 83: /run/secrets/mongo_root_username: Permission denied
```

The protection comes from the directory instead. `700` on
`/home/deployer/projects/portfolio/config` means no other user on the host can even
traverse into it, while `644` on the files themselves keeps them readable inside the
containers — Docker resolves the bind mount as root and hands the file to the
container directly, so the directory mode never enters that path.

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

## How Traefik reaches the API

Dokploy writes one file per application into `/etc/dokploy/traefik/dynamic/`, and
Traefik picks it up through its file provider. Nothing is expressed as a container
label. Creating the application in the Dokploy UI is what generates the file, so
the shape below is not something to write by hand — it is what the result should
look like, copied from the Alertax backend which already works on this host:

```yaml
http:
  routers:
    <app>-router-N:
      rule: Host(`eportfolioapi.rbsuport.com`)
      service: <app>-service-N
      middlewares:
        - redirect-to-https
      entryPoints:
        - web
    <app>-router-websecure-N:
      rule: Host(`eportfolioapi.rbsuport.com`)
      service: <app>-service-N
      middlewares: []
      entryPoints:
        - websecure
      tls:
        certResolver: letsencrypt
  services:
    <app>-service-N:
      loadBalancer:
        servers:
          - url: http://<container>:8086
        passHostHeader: true
```

Two entry points, `web` and `websecure`; the plain one redirects to HTTPS through
the shared `redirect-to-https` middleware; the secure one asks the `letsencrypt`
resolver for its certificate. In the UI this means one thing only: attach
`eportfolioapi.rbsuport.com` to service `portfolio-api` on port `8086` with HTTPS
enabled. After deploying, confirm the generated file matches:

```bash
ls /etc/dokploy/traefik/dynamic/
cat /etc/dokploy/traefik/dynamic/portfolio-*.yml
```

## Cloudflare

The DNS is already in place. `eportfolioapi.rbsuport.com` resolves to the same two
Cloudflare addresses as `alertax-api.rbsuport.com`, which serves traffic today, so
the record needs no change.

The certificate a browser sees on that host is issued by Google Trust Services for
`CN=rbsuport.com` — Cloudflare's own, not the Let's Encrypt one Traefik holds.
Cloudflare terminates TLS for visitors and talks to the VPS behind it. Since an
application on this exact path already works with `certResolver: letsencrypt`,
copying that configuration is the safe move: the combination is proven here, and
this is not the place to invent a different one.

## Protecting the admin surface

There is no authentication in the application, and the Traefik on this host has
exactly one middleware defined — `redirect-to-https`. No `basicAuth` exists yet.

Traefik can add one. Define the middleware in its own file so that redeploying the
application, which regenerates the file Dokploy owns, cannot erase it:

```yaml
# /etc/dokploy/traefik/dynamic/portfolio-auth.yml
http:
  middlewares:
    portfolio-admin-auth:
      basicAuth:
        users:
          - "esteban:$2y$05$..."
```

Generate the entry with bcrypt:

```bash
docker run --rm httpd:alpine htpasswd -nbB esteban 'the-password'
```

Then reference `portfolio-admin-auth` from the `websecure` router, which is the
part Dokploy regenerates — check after every redeploy that it survived, or set it
through Dokploy's own Traefik configuration editor so it is stored with the app.

This still lives in the proxy rather than in the code. It is the difference between
an open write surface and a closed one, but it is not an identity: the application
still cannot tell who is calling, and nothing is audited.

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

## What the application still does not do

`/api/admin/*` has no notion of who is calling. Update and delete are not exposed
at all — they were removed for exactly this reason — but creating a project is open
to anyone who reaches the endpoint, and nothing is audited.

The middleware above closes the door. Spring Security, or an API key filter, would
put the lock in the application where it belongs, and would let update and delete
come back.
