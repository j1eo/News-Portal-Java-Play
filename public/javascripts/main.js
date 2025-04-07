document.addEventListener("DOMContentLoaded", function () {
    // Only load what's needed for the current page
    if (document.getElementById("noticias-lista-milenio")) {
        cargarNoticiasMilenio();
    }
    if (document.getElementById("noticias-lista-elpais")) {
        cargarNoticiasElPais();
    }
    if (document.getElementById("form-articulo")) {
        document.getElementById("form-articulo").addEventListener("submit", function (event) {
            event.preventDefault();
            agregarArticulo();
        });
    }

    // These are probably safe to keep global
    document.getElementById("overlay").addEventListener("click", closeNav);

    function openNav() {
        document.getElementById("mySidenav").style.width = "450px";
        document.getElementById("overlay").style.display = "block";
    }

    function closeNav() {
        document.getElementById("mySidenav").style.width = "0";
        document.getElementById("overlay").style.display = "none";
    }

    // Null check for sidenav elements
    const closeBtn = document.querySelector(".closebtn");
    const openBtn = document.querySelector("[onclick='openNav()']");
    
    if (closeBtn) closeBtn.addEventListener("click", closeNav);
    if (openBtn) openBtn.addEventListener("click", openNav);
});

// Funciones para cargar noticias de Milenio
function cargarNoticiasMilenio() {
    fetch("/noticias/milenio")
        .then(response => response.json())
        .then(data => {
            const noticiasListaMilenio = document.getElementById("noticias-lista-milenio");
            noticiasListaMilenio.innerHTML = "";
            
            data.slice(0, 8).forEach(noticia => {
                const item = document.createElement("div");
                item.className = "news-card";
                item.innerHTML = `
                    <img src="${noticia.imagen || 'images/imagenplaceholder.png'}" alt="${noticia.titulo}">
                    <h3>${noticia.titulo}</h3>
                    <p>${noticia.descripcion || ''}</p>
                    <a href="${noticia.url}" target="_blank">Leer más</a>`;
                noticiasListaMilenio.appendChild(item);
            });
        })
        .catch(error => console.error("Error al cargar noticias de Milenio:", error));
}

/// Funciones para cargar noticias de El País
function cargarNoticiasElPais() {
    fetch("/noticias/elpais")
        .then(response => response.json())
        .then(data => {
            const noticiasListaElPais = document.getElementById("noticias-lista-elpais");
            noticiasListaElPais.innerHTML = "";
            
            // Obtener la ruta base de las imágenes desde un atributo data
            const basePath = document.body.getAttribute('data-assets-path') || '';
            const imagenDefault = basePath + 'images/elpais.png';
            
            data.slice(0, 8).forEach(noticia => {
                const item = document.createElement("div");
                item.className = "news-card";
                
                // Usar imagen por defecto si no hay imagen
                const imagenUrl = noticia.imagen || imagenDefault;
                
                item.innerHTML = `
                    <img src="${imagenUrl}" alt="${noticia.titulo}" onerror="this.src='${imagenDefault}'">
                    <h3>${noticia.titulo}</h3>
                    <p>${noticia.descripcion || ''}</p>
                    <a href="${noticia.url}" target="_blank">Leer más</a>`;
                
                noticiasListaElPais.appendChild(item);
            });
        })
        .catch(error => console.error("Error al cargar noticias de El País:", error));
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
