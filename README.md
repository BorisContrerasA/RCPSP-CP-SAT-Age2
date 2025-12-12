# Ejecutar el proyecto usando Docker (Java 17)

A continuación se muestra la forma **más simple y directa** de ejecutar este proyecto en un entorno controlado usando Docker.

---

## 1. Clonar el repositorio

```
git clone https://github.com/BorisContrerasA/RCPSP-CP-SAT-Age2.git
```

## 2. Navegar al directorio

```
cd RCPSP-CP-SAT-Age2
```

## 3. Construir la imagen Docker

> La imagen incluye Java 17 y compila automáticamente el proyecto.

```
docker build -t rcpsp-cp-sat .
```

## 4. Ejecutar el contenedor

```
docker run rcpsp-cp-sat
```

La aplicación se ejecutará dentro de un contenedor con **Java 17 y Maven**, sin necesidad de tenerlos instalados localmente.

---
