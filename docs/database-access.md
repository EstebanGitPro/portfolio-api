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

## Dos credenciales distintas

Es el punto que más confusión genera. Conectarse desde Compass atraviesa **dos
puertas**, y cada una tiene su propia llave:

| Pestaña de Compass | Qué credencial pide | Cuál es |
| --- | --- | --- |
| **Proxy/SSH** | la del **servidor Linux** | usuario `root` y la contraseña del VPS |
| **Authentication** | la de **MongoDB** | usuario `portfolio` y la contraseña generada |

Primero SSH permite entrar a la máquina. Ya dentro, MongoDB pide las suyas. Poner la
credencial del servidor en la pestaña de MongoDB produce
`All configured authentication methods failed`, que suena a problema de red y no lo
es.

El usuario de MongoDB es `portfolio`, no `root`. Conviene tenerlo presente porque en
el `docker-compose.yaml` de desarrollo el usuario **sí** es `root`: en el VPS se
eligió otro nombre para que la credencial de la base no se pareciera a la del
servidor.

Ambos valores salen de los archivos montados:

```bash
echo
echo "usuario: $(cat /home/deployer/projects/portfolio/config/mongo_root_username)"
echo "clave:   $(cat /home/deployer/projects/portfolio/config/mongo_root_password)"
echo
```

Los `echo` de los extremos no son adorno. Los archivos se escribieron **sin salto de
línea final** —a propósito, para que MongoDB no interprete el `\n` como parte de la
contraseña— y sin ellos el prompt de la terminal queda pegado al valor y se copia
junto con él.

## Opción B — Compass, a través de un túnel SSH

Compass abre el túnel por su cuenta; no hace falta mantener un `ssh -L` en otra
terminal.

**Cadena de conexión, sin credenciales:**

```
mongodb://127.0.0.1:27017/portfolio-db
```

**Advanced Connection Options → Authentication:**

| Campo | Valor |
| --- | --- |
| Authentication Method | Username/Password |
| Username | `portfolio` |
| Password | la contraseña generada |
| Authentication Database | `admin` |
| Authentication Mechanism | Default |

`admin` es correcto porque los usuarios creados por `MONGO_INITDB_ROOT_USERNAME`
nacen siempre en esa base, no en `portfolio-db`.

**Advanced Connection Options → Proxy/SSH → SSH with Password:**

| Campo | Valor |
| --- | --- |
| SSH Hostname | la dirección del VPS |
| SSH Port | `22` |
| SSH Username | `root` |
| SSH Password | la contraseña del servidor |

Si en lugar de contraseña se usa una llave, la opción es *SSH with Identity File* y
hay que apuntar al archivo de la llave privada.

Compass abre el túnel y resuelve el host de MongoDB **desde el punto de vista del
servidor**. Por eso `127.0.0.1` es correcto aquí: se refiere al loopback del VPS, no
al de la máquina local.

### Por qué las credenciales no van en la URI

Una contraseña de `openssl rand -base64` contiene `/`, `+` y `=`. En una URI la
barra corta la sección de credenciales, y el resultado es
`Invalid scheme, expected connection string to start with "mongodb://"` — un error
que señala al esquema cuando el problema está en la contraseña.

Puestas en la pestaña Authentication, Compass las codifica sola y el problema
desaparece.

No se expone nada nuevo. El tráfico viaja dentro de SSH y el túnel se cierra junto
con Compass.

### Si el binding a loopback todavía no está desplegado

Sin un puerto publicado, el contenedor sigue siendo alcanzable desde el host por su
propia dirección en la red de Docker:

```bash
docker inspect $(docker ps -qf name=portfolio.*mongodb) \
  --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'
```

Esa dirección funciona con la misma configuración de Compass —el túnel la resuelve
desde el servidor— pero cambia cada vez que el contenedor se recrea, es decir, en
cada despliegue. El binding a loopback existe para que la dirección sea estable.

El puerto es `27017` en ambos casos. En desarrollo se usa `27019` porque el
`docker-compose.yaml` local mapea `"27019:27017"`, y ese número solo existe cuando
hay un mapeo de puertos de por medio. Hablándole a la dirección del contenedor se
entra por dentro, donde MongoDB siempre escucha en `27017`.

### Cómo leer los errores de conexión

| Mensaje de Compass | Qué significa |
| --- | --- |
| `socket closed` | el túnel llegó al servidor y no había nadie escuchando en ese host y puerto |
| `All configured authentication methods failed` | el túnel funciona y MongoDB respondió: las credenciales son las equivocadas |
| `Invalid scheme...` | la URI está mal formada, casi siempre por caracteres especiales en la contraseña |

La distinción importa: el primero es un problema de red, el segundo ya no. Cambiar
de uno a otro es una señal de progreso, aunque los dos se vean como un fallo.

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
