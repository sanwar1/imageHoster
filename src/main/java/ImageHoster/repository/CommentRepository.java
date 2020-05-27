package ImageHoster.repository;

import ImageHoster.model.Comment;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;
import org.springframework.stereotype.Repository;

@Repository
public class CommentRepository {

  @PersistenceUnit(unitName = "imageHoster")
  private EntityManagerFactory emf;

  public void updateImageComment(Comment newComment) {

    EntityManager em = emf.createEntityManager();
    EntityTransaction transaction = em.getTransaction();

    try {
      transaction.begin();
      em.persist(newComment);
      transaction.commit();
    } catch (Exception e) {
      transaction.rollback();
    }
  }
}
