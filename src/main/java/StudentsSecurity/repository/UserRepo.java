package StudentsSecurity.repository;
import StudentsSecurity.entity.OurUsers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository <OurUsers,Integer> {
  Optional<OurUsers> findByEmail(String email);
}
