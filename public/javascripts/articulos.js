// Función para cargar artículos desde el servidor
function cargarArticulos() {
    fetch("/articulos")
        .then(response => response.json())
        .then(data => {
            const lista = document.getElementById("articulos-lista");
            lista.innerHTML = "";
            data.forEach(articulo => {
                const item = document.createElement("li");
                item.innerHTML = `<strong>${articulo.titulo}</strong>: ${articulo.contenido} <em>(${new Date(articulo.fechaPublicacion).toLocaleDateString()})</em>`;
                lista.appendChild(item);
            });
        })
        .catch(error => console.error("Error al cargar artículos:", error));
}

// Función para agregar un nuevo artículo al servidor
function agregarArticulo() {
    const titulo = document.getElementById("titulo").value;
    const contenido = document.getElementById("contenido").value;

    fetch("/articulos", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ titulo, contenido, fechaPublicacion: new Date().toISOString() })
    })
        .then(response => {
            if (response.ok) {
                cargarArticulos(); // Recargar lista de artículos
                document.getElementById("form-articulo").reset();
            } else {
                console.error("Error al agregar artículo");
            }
        })
        .catch(error => console.error("Error en la solicitud:", error));
}

export { cargarArticulos, agregarArticulo }; // Exportar funciones para usarlas en main.js
