# UR NEWS – News-Portal-Java-Play

UR NEWS es una aplicación web desarrollada como proyecto final para la materia Aplicaciones Web en la U-ERRE. Esta plataforma permite la creación, publicación y gestión de noticias por parte de usuarios registrados, fomentando la interacción mediante comentarios, reacciones y suscripciones.

## Descripción General

UR NEWS es una plataforma de noticias que:
- Permite a los usuarios registrarse, publicar noticias y artículos.
- Integra funcionalidades sociales como “Me gusta”, comentarios y suscripciones.
- Incluye un sistema de roles: usuarios y administradores.
- Está diseñada con arquitectura MVC usando el Play Framework y desarrollada en Java, Scala, HTML/CSS/JS y Bootstrap 5.

## Objetivo

Desarrollar un portal informativo que combine diseño visual, interacción social y una arquitectura robusta, cumpliendo con estándares de veracidad, transparencia, diversidad e innovación.

## Equipo de Desarrollo

- Jesus Leonardo Jimenez Gonzalez  
- Rogelio Jimenez Cuellar  
- José Ricardo Martínez Arquieta  
- Jesus Caleb Cuevas Rosas  
- Carlos Alberto Castro Luna  

## Tecnologías Utilizadas

- Backend: Java 1.8, Scala  
- Frontend: HTML, CSS, JavaScript, Bootstrap 5  
- Framework: Play Framework (MVC)  
- Base de Datos: MySQL (vía XAMPP)  
- Motor de plantillas: Twirl (Scala)  
- Seguridad: JWT, jBCrypt (hash de contraseñas)  
- Scraping de noticias: JSoup  
- Control de versiones: Git y GitHub  
- Diseño visual: Figma (wireframes y mockups)  

## Autenticación y Seguridad

- Registro e inicio de sesión con cifrado seguro de contraseñas (jBCrypt)  
- Uso de JWT para autenticación basada en tokens  
- Gestión de sesiones mediante cookies  
- Control de roles: usuarios y administradores  

## Funcionalidades Principales

- Registro de cuenta con imagen de perfil  
- Inicio de sesión y autenticación de usuarios  
- Publicación y edición de noticias y artículos  
- Interacción con publicaciones: comentarios, reacciones  
- Gestión de suscripciones (simulada)  
- Panel de administrador para moderación de contenido y usuarios  

## Casos de Prueba Incluidos

- Registro y autenticación de usuarios  
- Publicación y edición de noticias y artículos  
- Reacciones y comentarios  
- Gestión de suscripciones (interfaz simulada)  
- Acciones administrativas como eliminación de contenido  

## Arquitectura del Proyecto

- Modelo: Entidad-Relación (conceptual y lógico)  
- Controlador: Java con Play Framework  
- Vista: Twirl Templates (Scala)  
- Servicios: conexión con base de datos MySQL  
- Seguridad: implementación de JWT, jBCrypt  
- Scraping: automatización con JSoup  

## Capturas del Proyecto

> A continuación se muestran capturas representativas de la plataforma UR NEWS. Estas ilustran el diseño visual y las funcionalidades principales del sistema.

### Página principal
![PaginaPrincipal](https://github.com/user-attachments/assets/a0dbf109-3b57-4f64-9308-461c8f99fc56)

### Registro e inicio de sesión
![Registro](https://github.com/user-attachments/assets/204b4b21-116d-4757-b70a-542b4d49757e)
![InicioSesion](https://github.com/user-attachments/assets/d4dbf89b-036b-4db4-b1a4-91f312f2b227)

### Panel de usuario y creación de noticia
![PaginaUsuario](https://github.com/user-attachments/assets/56a46341-3ead-4c45-82fd-c570bbc5656f)
![CrearNoticia](https://github.com/user-attachments/assets/392b371c-f4e7-43a0-ad57-73a2179e3e57)

### Visualización de noticias y artículos
![Noticias](https://github.com/user-attachments/assets/7145dc79-5acf-4d2c-bb97-97c4a7df98b6)

### Interacción social: “Me gusta”, comentarios
![MeGusta](https://github.com/user-attachments/assets/bdcb7f0f-d9a5-40be-98af-c9a751e8e11d)
![Comentarios](https://github.com/user-attachments/assets/edcace69-8776-4695-ab86-7bcaae37d85f)

### Menú lateral y resultados de búsqueda
![BarraLateral](https://github.com/user-attachments/assets/5d6bdf1f-f0b3-40e4-9910-923f03365c87)
![ResultadoBusqueda](https://github.com/user-attachments/assets/93df7dbe-6b1d-4316-be71-d380c197bb8f)

### Panel de administrador
![PaginaAdmin](https://github.com/user-attachments/assets/169afa19-50af-4a1a-952c-ab133786a750)
![GestionArticulos](https://github.com/user-attachments/assets/450b2b0c-f5c3-48a0-9082-e946189c49c2)

### Cambio de modalidad de suscripción
![Suscripcion](https://github.com/user-attachments/assets/df0b42ec-93db-489c-a8dd-19d8fb3ad2e6)

**Nota importante:** las funcionalidades de suscripción están simuladas únicamente con fines académicos. No se realiza ningún proceso real de pago ni verificación de datos bancarios.

## Licencia

Este proyecto fue desarrollado exclusivamente con fines educativos. No se utilizaron fuentes externas ni bibliografía adicional.
