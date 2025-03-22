document.addEventListener("DOMContentLoaded", function () {
    const toggler = document.querySelector(".navbar-toggler");
    const sidebar = document.querySelector("#sidebar");

    toggler.addEventListener("click", function () {
        sidebar.classList.toggle("show");
    });
});
