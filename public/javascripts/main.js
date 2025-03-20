document.addEventListener("DOMContentLoaded", function () {
    cargarNoticias();
    cargarArticulos();

    document.getElementById("form-articulo").addEventListener("submit", function (event) {
        event.preventDefault();
        agregarArticulo();
    });
});

function cargarNoticias() {
    fetch("/noticias")
        .then(response => response.json())
        .then(data => {
            // Noticias Principales
            const noticiaPrincipal = document.getElementById("noticia-principal");
            const noticiaSecundaria = document.getElementById("noticia-secundaria");

            if (data.length > 0) {
                const principal = data[0];
                noticiaPrincipal.innerHTML = `
                    <h1>${principal.titulo}</h1>
                    <p>${principal.descripcion}</p>
                    <a href="${principal.url}" target="_blank">Leer más</a>
                `;
            }

            if (data.length > 1) {
                const secundaria = data[1];
                noticiaSecundaria.innerHTML = `
                    <img src="${secundaria.imagen}" alt="${secundaria.titulo}">
                    <h2>${secundaria.titulo}</h2>
                    <p>${secundaria.descripcion}</p>
                    <a href="${secundaria.url}" target="_blank">Leer más</a>
                `;
            }

            // Noticias Adicionales (Limitar a 6 elementos para 2 filas)
            const noticiasLista = document.getElementById("noticias-lista");
            const maxNoticias = 8; // Ajusta este valor según el número de filas deseadas
            const noticiasAdicionales = data.slice(2, 2 + maxNoticias); // Noticias desde la tercera

            noticiasAdicionales.forEach(noticia => {
                const item = document.createElement("div");
                item.className = "news-card";
                item.innerHTML = `
                    <img src="${noticia.imagen}" alt="${noticia.titulo}">
                    <h3>${noticia.titulo}</h3>
                    <p>${noticia.descripcion}</p>
                    <a href="${noticia.url}" target="_blank">Leer más</a>
                `;
                noticiasLista.appendChild(item);
            });
        })
        .catch(error => console.error("Error al cargar noticias:", error));
}


