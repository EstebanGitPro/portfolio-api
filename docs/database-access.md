# Reaching the database, and keeping it unreachable for everyone else

MongoDB holds every project in the portfolio and it is the only stateful thing in
the stack. This is how to get to it, and why getting to it takes a little effort on
purpose.

## The rule

**The database is never published to the internet.** It is published on the host's
loopback interface only:

```yaml
ports:
  - "127.0.0.1:27017:27017"
```

That prefix is the entire security boundary of this file. Written as `"27017:27017"`
the port binds `0.0.0.0` and the database becomes reachable from anywhere.

A closed firewall is not a defence here, and this is the part that catches people:
**Docker writes its own iptables rules and they are evaluated before ufw's.** A
published port is reachable even when `ufw status` swears the port is denied. The
binding address in the compose file is what decides, not the firewall.

The reason to care is not theoretical. Open MongoDB instances are swept
continuously by bots that connect, drop the collections and leave a ransom note.
The window between publishing a port and being found is measured in hours.

## Option A — a shell on the server

For fixing data, this is enough and needs nothing set up:

```bash
docker exec -it $(docker ps -qf name=portfolio.*mongodb) mongosh \
  -u portfolio \
  -p "$(cat /home/deployer/projects/portfolio/config/mongo_root_password)" \
  --authenticationDatabase admin portfolio-db
```

Useful once inside:

```javascript
db.projects.find({}, {slug: 1, title: 1, published: 1, order: 1})
db.projects.updateOne({slug: "motogo"}, {$set: {order: 2}})
db.projects.deleteOne({slug: "typo-en-el-titulo"})
```

This matters more than it looks: the API exposes no `PUT` and no `DELETE`, so a
project created with a typo in its title — and therefore in its slug — can only be
corrected here.

## Option B — Compass, through an SSH tunnel

Compass tunnels on its own; no `ssh -L` in a separate terminal is needed.

**Connection string:**

```
mongodb://portfolio:<password>@127.0.0.1:27017/portfolio-db?authSource=admin
```

**Advanced Connection Options → Proxy/SSH → SSH with Identity File:**

| Field | Value |
| --- | --- |
| SSH Hostname | the VPS address |
| SSH Port | `22` |
| SSH Username | `root` |
| Identity File | the key already used for the shell |

Compass opens the tunnel and resolves the MongoDB host **from the server's point of
view**, which is why `127.0.0.1` is correct here: it means the VPS's loopback, not
the laptop's.

Get the password with:

```bash
cat /home/deployer/projects/portfolio/config/mongo_root_password
```

Nothing new is exposed. The traffic travels inside SSH, and the tunnel closes with
Compass.

### Before the loopback binding existed

Without a published port the container is still reachable from the host by its own
address on the Docker network:

```bash
docker inspect $(docker ps -qf name=portfolio.*mongodb) \
  --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'
```

That address works in the same Compass setup, but it changes every time the
container is recreated — which is every deploy. The loopback binding exists to make
the address stable.

## Auditing what is exposed

Run this on the host whenever a stack changes. It answers one question: what can the
internet reach?

```bash
echo "=== containers publishing on 0.0.0.0 (reachable from the internet) ==="
docker ps --format '{{.Names}}\t{{.Ports}}' | grep '0.0.0.0' || echo "none"

echo "=== everything listening, with the owning process ==="
ss -tlnp

echo "=== what ufw believes (it does not govern Docker) ==="
ufw status verbose
```

Anything listed in the first block is open to the world. Read that list as the
question "would I be comfortable if this were on a public web page?", because in
effect it is.

To confirm from the outside rather than trusting the local view, from any other
machine:

```bash
for p in 27017 3306 8080 9443; do
  timeout 5 bash -c "</dev/tcp/<vps-ip>/$p" 2>/dev/null \
    && echo "$p OPEN" || echo "$p closed"
done
```

The local view can be reassuring and wrong. This one cannot.

## Closing a port that should not be open

Change the binding, do not reach for the firewall:

```yaml
ports:
  - "127.0.0.1:3306:3306"   # instead of "3306:3306"
```

Then recreate the container. The service stays reachable through an SSH tunnel for
whoever administers it, and stops being reachable for everyone else.

## Rotating the password

`MONGO_INITDB_ROOT_PASSWORD_FILE` is read **only while the data volume is empty**.
Editing the file later changes nothing: the user already exists. Rotation happens
inside the database first, and the file is updated to match afterwards.

```javascript
db.getSiblingDB("admin").changeUserPassword("portfolio", "<new password>")
```

```bash
printf '<new password>' > /home/deployer/projects/portfolio/config/mongo_root_password
# and the same value in application-prod.yaml, then restart the API
```

The file and `application-prod.yaml` are two views of one credential. They drift
apart silently, and the symptom is an authentication error with no obvious cause.
