# Lab 2 Web Server -- Project Report

## What I specified

Por suerte, al leer el guión de la práctica me di cuenta de que está muy guiada y se entendía todo a la perfección; por eso, lo que pensé en hacer era lo indicado: las 3 tareas acompañadas del step further indicado al final de cada sección:

1. **Página de error personalizada:** cuando se acceda a una ruta que no existe, la aplicación debe devolver el código de error `404`, además de una página personalizada mostrando el estado y la ruta.
*Comprobación:* un test que pida una ruta inexistente y verifique que la respuesta es `404` y que el HTML contiene el código y la ruta.

2. **Endpoint `/time`:** una petición `GET /time` debe devolver la hora actual del servidor en formato JSON. Además, se podrá poner una zona horaria con `?zone=`; si la zona no existe, devolveré el error `400` (no se puede procesar la petición).
*Comprobación:* tests que verifiquen que la respuesta es `200` con el campo `time`; como extra, se comprueba con una zona nueva para ver si se devuelve correctamente y también con una zona falsa.

3. **HTTP/2 y TLS:** configurar HTTPS en el puerto 8443 con un certificado autofirmado y usar HTTP/2. Para el step further, el certificado deberá ser válido tanto para `localhost` como para `127.0.0.1`.
*Comprobación:* con `curl -v --http2 -k` debe aparecer `ALPN: server accepted h2` y `HTTP/2 200`.


## What I changed

En esta práctica únicamente se modificó `docs/.gitignore` para añadir la carpeta `/.vscode/`, y **build.gradle.kts**, que fue reformateado al ejecutar `./gradlew ktlintFormat` ya que al principio fallaba por el formato (ktlint). El resto de archivos añadidos han sido:

**Añadidos**

**Para la página de error:**
- `src/main/resources/templates/error.html`: página de error personalizada, uso Thymeleaf para mostrar el código de error y la ruta.
- `src/test/kotlin/es/unizar/webeng/lab2/ErrorPageTest.kt`: contiene el test de la página de error comprobando que una ruta que no existe devuelve `404` y muestra la página con el código y la ruta.
- `src/test/resources/application.yml`: desactiva SSL en los tests para usar HTTP normal.

**Para el endpoint `/time`:**
- `src/main/kotlin/es/unizar/webeng/lab2/TimeComponent.kt`: `TimeDTO`, `TimeProvider`, `TimeService` y `TimeController`. `GET /time` devuelve la hora actual en JSON; el parámetro opcional `?zone=` elige la zona horaria y una zona desconocida devuelve `400`.
- `src/test/kotlin/es/unizar/webeng/lab2/TimeControllerTest.kt`: comprueba la respuesta JSON, el parámetro `zone` y el `400` con una zona que no existe.

**Para HTTP/2 + TLS:**
- `src/main/resources/application.yml`: HTTPS en el puerto 8443 con el keystore PKCS12, el alias de la clave y HTTP/2 activado.
- `src/main/resources/localhost.p12`: keystore con el certificado.
- `openssl-localhost.cnf` (en `.gitignore`): configuración de OpenSSL.


## Technical decisions

Al ser una práctica muy guiada y no haber hecho el bonus, las decisiones principales están en los *step further* y en cómo probar cada parte:

- **Página de error:** muestra el código y la ruta (el *step further* propuesto).
- **`/time`:** elegí el parámetro `?zone=` en vez del test del timestamp exacto, porque me pareció más interesante. Una zona inválida devuelve el error `400` al ser error de cliente y no de servidor, y la respuesta incluye la zona para que el cliente sepa a qué hora corresponde.
- **TLS:** seguí el *step further* propuesto: añadí `IP:127.0.0.1` al certificado para que también sea válido al acceder por IP, y configuré `key-alias` para indicar a Tomcat qué entrada del keystore usar.


## How I verified

Para verificarlo usé los tests creados y el que ya existía:
`./gradlew check` para ejecutar todos los tests, pasan sin fallos. Como mencioné anteriormente, al iniciar el proyecto tuve un fallo que solucioné con `./gradlew ktlintFormat`.
- `ApplicationTests`: 1 test, el que ya había para ver si la aplicación arranca.
- `ErrorPageTest`: 1 test (`/missing` devuelve `404` con la página personalizada, el código y la ruta).
- `TimeControllerTest`: 3 tests (`/time` devuelve JSON con `time`, `?zone=Europe/London` devuelve esa zona, una zona inválida devuelve `400`).

Comprobaciones con curl indicadas en el guión:

Para la página de error con http2:
- `curl -v --http2 -k -H "Accept: text/html" -i https://127.0.0.1:8443/`
- Comprueba que una ruta sin página devuelve `HTTP/2 404` con mi `error.html`, mostrando el código y la ruta, y que la conexión usa HTTP/2 (`ALPN: server accepted h2`).

Para el apartado time con http2:
- `curl -v --http2 -k -i https://127.0.0.1:8443/time`
- Comprueba que `/time` devuelve `HTTP/2 200` con un JSON que contiene la hora y la zona.

Después del step further del apartado 3, tras añadir la IP `127.0.0.1`:
- `openssl x509 -in localhost.crt -noout -ext subjectAltName`
- Salida: `DNS:localhost, IP Address:127.0.0.1`.

## AI disclosure


- **Tools / skills:** Para esta práctica utilicé Claude Code.
- **Purpose:** Principalmente su uso fue para explicar cómo estaba construida la aplicación y entender el código proporcionado en el guión y alguna de las tareas extras proporcionadas, también para redactar bien el `REPORT.md` o que me diera buenas salidas en markdown.
- **Representative prompts:** 
  - "Explícame la estructura del proyecto y sus clases principales."
  - "¿Por qué me salta este fallo?" (con la salida de `check` por el `application.yml` de los test)
  - "Explícame los step further solicitados y que función tienen."
  - "¿Cómo sería una estructura correcta en `error.html`?"
- **Affected files/sections:**
  - `REPORT.md`
  - `error.html`
  - `TimeControllerTest.kt` para el test opcional
  - `application.yml`
  - `TimeComponent.kt`
- **Validation steps:** Las comprobaciones descritas en el apartado anterior.
- **Citations:** Código y comandos proporcionados en la guía.
- **Human-reviewed:** Revisé los cambios que me proponía para mejorar el `REPORT.md` o los cambios que me propuso en los step further  
para entenderlos y saber si eran correctos y por qué hacían falta.