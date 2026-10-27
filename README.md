# API Sencilla de Tareas (Express) — Computación en Internet I

Servidor backend básico diseñado como soporte para el **Taller Formativo de Promesas y Consumo de APIs en el Frontend**.

Su único propósito es suministrar una lista predefinida de tareas en formato JSON para que los estudiantes aprendan a consumir APIs desde el navegador utilizando la función nativa `fetch()` y el concepto de **Promesas** (`Promise`, `.then()`, `.catch()`, `async / await`).

---

## 📄 Guía del Taller Formativo

La guía práctica oficial del taller formativo para la construcción del frontend paso a paso se encuentra en este repositorio:

* 📘 **Guía Oficial en PDF:** [`taller/Taller_Formativo_Frontend_Consumo_API_Express_Promesas.pdf`](./taller/Taller_Formativo_Frontend_Consumo_API_Express_Promesas.pdf)
* 📝 **Fuente LaTeX:** [`taller/Taller_Formativo_Frontend_Consumo_API_Express_Promesas.tex`](./taller/Taller_Formativo_Frontend_Consumo_API_Express_Promesas.tex)

### 🎯 Objetivos de la Práctica
1. Construir una interfaz completa en **JavaScript Puro (Vanilla JS)** manipulando el DOM a partir de un HTML minimalista (`<div id="root"></div>`).
2. Implementar una **arquitectura modular de componentes funcionales** (`Header`, `TaskForm`, `TaskItem`, `TaskList`, `LoadingSpinner`, `ErrorAlert`, `App`).
3. Dominar el flujo unidireccional de datos mediante **props** descendentes y **callbacks** de eventos ascendentes.
4. Consumir las operaciones `GET`, `POST` y `DELETE` de esta API usando **Promesas** con `fetch` y `async / await` en una capa de servicio independiente (`taskService.js`).

---

## 🚀 Puesta en Marcha en 2 Pasos

```bash
# 1. Instalar dependencias
npm install

# 2. Iniciar el servidor
npm start
```

El servidor quedará escuchando en: **`http://localhost:3001`**.

---

## 📡 Endpoints Provistos

### 1. Obtener todas las tareas (`GET /api/tasks`)
* **URL:** `http://localhost:3001/api/tasks`
* **Método:** `GET`
* **Respuesta de Ejemplo:**
  ```json
  [
    { "id": 1, "title": "Comprender la estructura básica de HTML y CSS", "completed": true },
    { "id": 2, "title": "Aprender a seleccionar y modificar elementos del DOM", "completed": true },
    { "id": 3, "title": "Entender qué es una Promesa y sus tres estados en JS", completed: false }
  ]
  ```

### 2. Crear una nueva tarea (`POST /api/tasks`)
* **URL:** `http://localhost:3001/api/tasks`
* **Método:** `POST`
* **Headers:** `Content-Type: application/json`
* **Cuerpo (Body):**
  ```json
  { "title": "Estudiar asincronía en JavaScript" }
  ```
* **Respuesta:** Objeto creado con su nuevo `id` (`201 Created`).

### 3. Eliminar una tarea (`DELETE /api/tasks/:id`)
* **URL:** `http://localhost:3001/api/tasks/1`
* **Método:** `DELETE`
* **Respuesta:** Mensaje de confirmación (`200 OK`).

---

## 🏛️ Créditos
Universidad Icesi — Departamento de Computación y Sistemas Inteligentes (DCSI).
