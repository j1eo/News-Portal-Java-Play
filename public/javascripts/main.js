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

// Simulación de registros por usuario
let registroLikes = {};


// Función para toggle "Me gusta"
function toggleMeGusta(idArticulo) {
    const botonMeGusta = document.querySelector(`button[onclick="toggleMeGusta(${idArticulo})"]`);
    const contadorMeGusta = document.getElementById(`me-gusta-${idArticulo}`);
    const estado = registroLikes[idArticulo];

    if (!estado.meGusta) {
        // Activar "Me gusta"
        contadorMeGusta.textContent = parseInt(contadorMeGusta.textContent) + 1;
        estado.meGusta = true;
        botonMeGusta.classList.add("active");

        // Si ya tenía "No me gusta", lo desactiva
        if (estado.noMeGusta) {
            toggleNoMeGusta(idArticulo);
        }
    } else {
        // Desactivar "Me gusta"
        contadorMeGusta.textContent = parseInt(contadorMeGusta.textContent) - 1;
        estado.meGusta = false;
        botonMeGusta.classList.remove("active");
    }
}

// Función para toggle "No me gusta"
function toggleNoMeGusta(idArticulo) {
    const botonNoMeGusta = document.querySelector(`button[onclick="toggleNoMeGusta(${idArticulo})"]`);
    const contadorNoMeGusta = document.getElementById(`no-me-gusta-${idArticulo}`);
    const estado = registroLikes[idArticulo];

    if (!estado.noMeGusta) {
        // Activar "No me gusta"
        contadorNoMeGusta.textContent = parseInt(contadorNoMeGusta.textContent) + 1;
        estado.noMeGusta = true;
        botonNoMeGusta.classList.add("active");

        // Si ya tenía "Me gusta", lo desactiva
        if (estado.meGusta) {
            toggleMeGusta(idArticulo);
        }
    } else {
        // Desactivar "No me gusta"
        contadorNoMeGusta.textContent = parseInt(contadorNoMeGusta.textContent) - 1;
        estado.noMeGusta = false;
        botonNoMeGusta.classList.remove("active");
    }
}


function eliminarArticulo(id) {
    fetch(`/articulos/${id}`, { method: "DELETE" })
        .then(response => {
            if (!response.ok) {
                throw new Error("Error al eliminar el artículo");
            }
            return response.json();
        })
        .then(() => {
            alert("Artículo eliminado correctamente");
            cargarArticulos(); // Recargar la lista después de eliminar
        })
        .catch(error => console.error("Error al eliminar el artículo:", error));
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
