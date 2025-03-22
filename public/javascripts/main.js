document.addEventListener("DOMContentLoaded", function () {
    // Cargar noticias y artículos al iniciar
    cargarNoticias();
    cargarArticulos();

    // Configurar el formulario para agregar artículos
    document.getElementById("form-articulo").addEventListener("submit", function (event) {
        event.preventDefault();
        agregarArticulo();
    });

	document.getElementById("overlay").addEventListener("click", closeNav);

	function openNav() {
	    document.getElementById("mySidenav").style.width = "450px";
	    document.getElementById("overlay").style.display = "block"; // Mostrar el overlay
	}

	function closeNav() {
	    document.getElementById("mySidenav").style.width = "0";
	    document.getElementById("overlay").style.display = "none"; // Ocultar el overlay
	}

    // Eventos para el sidenav
    document.querySelector(".closebtn").addEventListener("click", closeNav);
    document.querySelector("[onclick='openNav()']").addEventListener("click", openNav);
});

// Funciones para cargar noticias
function cargarNoticias() {
    fetch("/noticias")
        .then(response => response.json())
        .then(data => {
            const noticiaPrincipal = document.querySelector(".main-news");
            const noticiaSecundaria1 = document.getElementById("noticia-secundaria-1");
            const noticiaSecundaria2 = document.getElementById("noticia-secundaria-2");
            const noticiasLista = document.getElementById("noticias-lista");

            if (data.length > 0) {
                const principal = data[0];
                noticiaPrincipal.innerHTML = `
                    <img src="${principal.imagen}" alt="${principal.titulo}">
                    <div class="main-news-text">
                        <h1>${principal.titulo}</h1>
                        <p>${principal.descripcion}</p>
                        <a href="${principal.url}" target="_blank">Leer más</a>
                    </div>`;
            }

            if (data.length > 1) {
                noticiaSecundaria1.innerHTML = `
                    <img src="${data[1].imagen}" alt="${data[1].titulo}">
                    <h2>${data[1].titulo}</h2>
                    <p>${data[1].descripcion}</p>
                    <a href="${data[1].url}" target="_blank">Leer más</a>`;
            }

            if (data.length > 2) {
                noticiaSecundaria2.innerHTML = `
                    <img src="${data[2].imagen}" alt="${data[2].titulo}">
                    <h2>${data[2].titulo}</h2>
                    <p>${data[2].descripcion}</p>
                    <a href="${data[2].url}" target="_blank">Leer más</a>`;
            }

            noticiasLista.innerHTML = "";
            data.slice(3, 11).forEach(noticia => {
                const item = document.createElement("div");
                item.className = "news-card";
                item.innerHTML = `
                    <img src="${noticia.imagen}" alt="${noticia.titulo}">
                    <h3>${noticia.titulo}</h3>
                    <p>${noticia.descripcion}</p>
                    <a href="${noticia.url}" target="_blank">Leer más</a>`;
                noticiasLista.appendChild(item);
            });
        })
        .catch(error => console.error("Error al cargar noticias:", error));
}

// Funciones para cargar artículos
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

// Función para agregar un artículo nuevo
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
            cargarArticulos();
            document.getElementById("form-articulo").reset();
        } else {
            console.error("Error al agregar artículo");
        }
    })
    .catch(error => console.error("Error en la solicitud:", error));
}
