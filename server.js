import express from 'express';
import cors from 'cors';

const app = express();
const PORT = 3001;

// Habilitar CORS para permitir peticiones desde cualquier origen (ej. frontend en localhost:3000)
app.use(cors());

// Middleware para procesar cuerpos JSON (necesario para peticiones POST)
app.use(express.json());

// Lista en memoria de tareas
let tareas = [
  { id: 1, title: "Comprender la estructura básica de HTML y CSS", completed: true },
  { id: 2, title: "Aprender a seleccionar y modificar elementos del DOM", completed: true },
  { id: 3, title: "Entender qué es una Promesa y sus tres estados en JS", completed: false },
  { id: 4, title: "Consumir una API externa usando fetch y async/await", completed: false },
  { id: 5, title: "Renderizar dinámicamente los datos en la interfaz web", completed: false }
];

// 1. GET /api/tasks — Obtener todas las tareas
app.get('/api/tasks', (req, res) => {
  res.json(tareas);
});

// 2. POST /api/tasks — Crear una nueva tarea
app.post('/api/tasks', (req, res) => {
  const { title } = req.body;
  if (!title || !title.trim()) {
    return res.status(400).json({ error: "El título de la tarea es requerido." });
  }

  const nuevaTarea = {
    id: Date.now(),
    title: title.trim(),
    completed: false
  };

  tareas.push(nuevaTarea);
  res.status(201).json(nuevaTarea);
});

// 3. DELETE /api/tasks/:id — Eliminar una tarea por su ID
app.delete('/api/tasks/:id', (req, res) => {
  const id = req.params.id;
  const existe = tareas.some(t => String(t.id) === String(id));

  if (!existe) {
    return res.status(404).json({ error: "Tarea no encontrada." });
  }

  tareas = tareas.filter(t => String(t.id) !== String(id));
  res.json({ message: "Tarea eliminada exitosamente", id });
});

// Endpoint raíz informativo
app.get('/', (req, res) => {
  res.json({
    mensaje: "API sencilla de tareas para Computación en Internet I",
    endpoints: {
      listar: "GET http://localhost:3001/api/tasks",
      crear: "POST http://localhost:3001/api/tasks (body: { \"title\": \"...\" })",
      eliminar: "DELETE http://localhost:3001/api/tasks/:id"
    }
  });
});

app.listen(PORT, () => {
  console.log(`✅ Servidor de tareas iniciado en: http://localhost:${PORT}`);
  console.log(`👉 Endpoints disponibles en: http://localhost:${PORT}/api/tasks (GET, POST, DELETE)`);
});
