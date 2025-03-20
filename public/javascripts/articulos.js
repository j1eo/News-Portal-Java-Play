// Función para cargar artículos desde el servidor
function cargarArticulos() {
    fetch("/articulos")
        .then(response => {
            if (!response.ok) {
                throw new Error("Error al obtener artículos del servidor");
            }
            return response.json();
        })
        .then(data => {
            const lista = document.getElementById("articulos-lista");
            lista.innerHTML = ""; // Limpia la lista antes de llenarla
            data.forEach(articulo => {
                const item = document.createElement("li");
                item.innerHTML = `
                    <strong>${articulo.titulo}</strong>: ${articulo.contenido} 
                    <em>(${articulo.fechaPublicacion})</em> <!-- Mostrar tal cual -->
                `;
                lista.appendChild(item);
            });
        })
        .catch(error => console.error("Error al cargar artículos:", error));
}


// Función para agregar un nuevo artículo al servidor
function agregarArticulo(event) {
    event.preventDefault(); // Evita que el formulario recargue la página

    const titulo = document.getElementById("titulo").value;
    const contenido = document.getElementById("contenido").value;

    fetch("/articulos", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ titulo, contenido, fechaPublicacion: new Date().toISOString() })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Error al agregar artículo");
            }
            // Recargar la lista de artículos tras agregar uno nuevo
            cargarArticulos();
            document.getElementById("form-articulo").reset();
        })
        .catch(error => console.error("Error en la solicitud:", error));
}

// Asocia eventos al DOM
document.addEventListener("DOMContentLoaded", () => {
    // Cargar los artículos al iniciar la página
    cargarArticulos();

    // Escuchar el envío del formulario para agregar un artículo
    const formArticulo = document.getElementById("form-articulo");
    formArticulo.addEventListener("submit", agregarArticulo);
});
