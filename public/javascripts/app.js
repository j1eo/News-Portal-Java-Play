'use strict';

// Crear un componente React
const App = () => {
    return React.createElement('h1', null, '¡Hola desde React en Play Framework!');
};

// Renderizar el componente en el contenedor con id "root"
const rootElement = document.getElementById('root');
ReactDOM.render(React.createElement(App), rootElement);
