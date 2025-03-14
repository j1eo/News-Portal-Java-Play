package services;

import models.Articulo;
import repository.ArticuloRepository;

import javax.inject.Inject;
import java.util.List;

public class ArticuloService {
    private final ArticuloRepository articuloRepo;

    @Inject
    public ArticuloService(ArticuloRepository articuloRepo) {
        this.articuloRepo = articuloRepo;
    }

    public List<Articulo> obtenerTodos() { return articuloRepo.obtenerTodos(); }
    public void agregarArticulo(Articulo articulo) { articuloRepo.guardar(articulo); }
}
