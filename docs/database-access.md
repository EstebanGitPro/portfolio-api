# Acceso a la base de datos, y cómo mantenerla inalcanzable para el resto

MongoDB guarda todos los proyectos del portfolio y es lo único con estado en el
stack. Este documento explica cómo llegar a ella y por qué llegar cuesta un poco de
trabajo a propósito.

## La regla

**La base de datos nunca se publica a internet.** Se publica únicamente en la
interfaz de loopback del host:

```yaml
ports:
  - "127.0.0.1:27017:27017"
```

Ese prefijo es toda la frontera de seguridad de este archivo. Escrito como
`"27017:27017"` el puerto se asocia a `0.0.0.0` y la base queda alcanzable desde
cualquier parte.

Un firewall cerrado no protege aquí, y esta es la parte que sorprende: **Docker
escribe sus propias reglas de iptables y se evalúan antes que las de ufw**. Un
puerto publicado es alcanzable aunque `ufw status` afirme que está denegado. Lo que
decide es la dirección de binding en el compose, no el firewall.

El motivo para cuidarlo no es teórico. Las instancias de MongoDB abiertas son
rastreadas de forma continua por bots que se conectan, borran las colecciones y
dejan una nota de rescate. La ventana entre publicar un puerto y ser encontrado se
mide en horas.

## Opción A — una terminal en el servidor

Para corregir datos es suficiente y no requiere configurar nada:

```bash
docker exec -it $(docker ps -qf name=portfolio.*mongodb) mongosh \
  -u portfolio \
  -p "$(cat /home/deployer/projects/portfolio/config/mongo_root_password)" \
  --authenticationDatabase admin portfolio-db
```

Comandos útiles una vez dentro:

```javascript
db.projects.find({}, {slug: 1, title: 1, published: 1, order: 1})
db.projects.updateOne({slug: "motogo"}, {$set: {order: 2}})
db.projects.deleteOne({slug: "typo-en-el-titulo"})
```

Esto importa más de lo que parece: la API no expone `PUT` ni `DELETE`, así que un
proyecto creado con un error de tipeo en el título — y por lo tanto en su slug —
solo se puede corregir desde aquí.

## Opción B — Compass, a través de un túnel SSH

Compass abre el túnel por su cuenta; no hace falta mantener un `ssh -L` en otra
terminal.

**Cadena de conexión:**

```
mongodb://portfolio:<contraseña>@127.0.0.1:27017/portfolio-db?authSource=admin
```

**Advanced Connection Options → Proxy/SSH → SSH with Identity File:**

| Campo | Valor |
| --- | --- |
| SSH Hostname | la dirección del VPS |
| SSH Port | `22` |
| SSH Username | `root` |
| Identity File | la misma llave que se usa para la terminal |

Compass abre el túnel y resuelve el host de MongoDB **desde el punto de vista del
servidor**. Por eso `127.0.0.1` es correcto aquí: se refiere al loopback del VPS, no
al de la máquina local.

La contraseña se obtiene con:

```bash
cat /home/deployer/projects/portfolio/config/mongo_root_password
```

No se expone nada nuevo. El tráfico viaja dentro de SSH y el túnel se cierra junto
con Compass.

### Antes de que existiera el binding a loopback

Sin un puerto publicado, el contenedor sigue siendo alcanzable desde el host por su
propia dirección en la red de Docker:

```bash
docker inspect $(docker ps -qf name=portfolio.*mongodb) \
  --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'
```

Esa dirección funciona con la misma configuración de Compass, pero cambia cada vez
que el contenedor se recrea, es decir, en cada despliegue. El binding a loopback
existe para que la dirección sea estable.

## Auditar qué está expuesto

Conviene ejecutar esto en el host cada vez que cambia un stack. Responde una sola
pregunta: qué puede alcanzar internet.

```bash
echo "=== contenedores publicando en 0.0.0.0 (alcanzables desde internet) ==="
docker ps --format '{{.Names}}\t{{.Ports}}' | grep '0.0.0.0' || echo "ninguno"

echo "=== todo lo que está escuchando, con su proceso ==="
ss -tlnp

echo "=== lo que cree ufw (no gobierna a Docker) ==="
ufw status verbose
```

Todo lo que aparezca en el primer bloque está abierto al mundo. La forma de leer esa
lista es preguntarse: *¿estaría cómodo si esto estuviera en una página web pública?*
Porque en la práctica lo está.

Para confirmarlo desde afuera en lugar de confiar en la vista local, desde cualquier
otra máquina:

```bash
for p in 27017 3306 8080 9443; do
  timeout 5 bash -c "</dev/tcp/<ip-del-vps>/$p" 2>/dev/null \
    && echo "$p ABIERTO" || echo "$p cerrado"
done
```

La vista local tranquiliza y puede estar equivocada. Esta comprobación no.

## Cerrar un puerto que no debería estar abierto

Se cambia el binding, no se recurre al firewall:

```yaml
ports:
  - "127.0.0.1:3306:3306"   # en lugar de "3306:3306"
```

Después se recrea el contenedor. El servicio sigue siendo alcanzable por túnel SSH
para quien lo administre, y deja de serlo para todos los demás.

## Rotar la contraseña

`MONGO_INITDB_ROOT_PASSWORD_FILE` se lee **únicamente mientras el volumen de datos
está vacío**. Editar el archivo después no cambia nada: el usuario ya existe. La
rotación ocurre primero dentro de la base y el archivo se actualiza a continuación.

```javascript
db.getSiblingDB("admin").changeUserPassword("portfolio", "<contraseña nueva>")
```

```bash
printf '<contraseña nueva>' > /home/deployer/projects/portfolio/config/mongo_root_password
# y el mismo valor en application-prod.yaml; luego reiniciar la API
```

El archivo y `application-prod.yaml` son dos vistas de una misma credencial. Se
desincronizan en silencio, y el síntoma es un error de autenticación sin causa
aparente.
