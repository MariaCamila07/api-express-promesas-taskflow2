// app.js: orquestador. Flujo: Evento -> API (Promesa) -> Estado -> Render.

import {
  loadTasksFromStorage,
  createTaskInStorage,
  setTaskCompleted,
  deleteTaskFromStorage
} from './storage.js';
import {
  renderTaskList,
  renderCounter,
  renderActiveFilter,
  showLoading,
  showError,
  clearStatus,
  setFormBusy
} from './dom.js';

// Estado: unica fuente de verdad
let tasks = [];
let currentFilter = 'all';

const form = document.querySelector('#task-form');
const input = document.querySelector('#task-input');
const list = document.querySelector('#task-list');
const filters = document.querySelector('#filters');
const status = document.querySelector('#status');

function render() {
  renderTaskList(tasks, currentFilter);
  renderCounter(tasks);
  renderActiveFilter(currentFilter);
}

/** Envuelve cada accion asincrona: si la Promesa se rechaza, mostramos el error. */
async function run(action) {
  try {
    await action();
    clearStatus();
    return true;
  } catch (error) {
    showError(error.message);
    return false;
  }
}

// 0. Carga inicial
async function init() {
  showLoading();
  try {
    tasks = await loadTasksFromStorage();
    clearStatus();
  } catch (error) {
    showError(error.message);
  }
  render();
}

// 1. Submit del formulario
form.addEventListener('submit', async (e) => {
  e.preventDefault();
  const title = input.value.trim();
  if (!title) return;

  setFormBusy(true);
  await run(async () => {
    const created = await createTaskInStorage(title);   // el servidor asigna el id
    tasks.push(created);
    input.value = '';
    render();
  });
  setFormBusy(false);
  input.focus();
});

// 2. Delegacion en la lista: UN listener por tipo de evento, no uno por tarea
list.addEventListener('click', (e) => {
  if (!e.target.matches('.btn-delete')) return;
  const id = Number(e.target.closest('.task-item').dataset.id);

  run(async () => {
    await deleteTaskFromStorage(id);
    tasks = tasks.filter(t => t.id !== id);
    render();
  });
});

list.addEventListener('change', async (e) => {
  if (!e.target.matches('.task-check')) return;
  const id = Number(e.target.closest('.task-item').dataset.id);
  const completed = e.target.checked;

  const ok = await run(async () => {
    const updated = await setTaskCompleted(id, completed);
    tasks = tasks.map(t => (t.id === id ? updated : t));
    render();
  });
  if (!ok) render();   // si falla, re-render revierte el checkbox al estado real
});

// 3. Filtros (delegacion tambien)
filters.addEventListener('click', (e) => {
  const btn = e.target.closest('.filter-btn');
  if (!btn) return;
  currentFilter = btn.dataset.filter;
  render();
});

// 4. Cerrar el mensaje de error
status.addEventListener('click', (e) => {
  if (e.target.matches('[data-action="dismiss"]')) clearStatus();
});

init();
