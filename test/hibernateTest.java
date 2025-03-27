import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

//import models.ArticuloHibernate;
//
//import java.util.Date;
//
//public class hibernateTest {
//    public static void main(String[] args) {
//        // Configurar EntityManager
//        EntityManagerFactory emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit");
//        EntityManager em = emf.createEntityManager();
//
//        try {
//            // Insertar un registro
//            em.getTransaction().begin();
//
//            ArticuloHibernate articulo = new ArticuloHibernate();
//            articulo.setTitulo("Título de prueba");
//            articulo.setContenido("Este es un contenido de prueba.");
//            articulo.setFechaPublicacion(new Date());
//
//            em.persist(articulo);
//            em.getTransaction().commit();
//
//            System.out.println("Artículo insertado con ID: " + articulo.getId());
//        } catch (Exception e) {
//            e.printStackTrace();
//            if (em.getTransaction().isActive()) {
//                em.getTransaction().rollback();
//            }
//        } finally {
//            em.close();
//            emf.close();
//        }
//    }
//}