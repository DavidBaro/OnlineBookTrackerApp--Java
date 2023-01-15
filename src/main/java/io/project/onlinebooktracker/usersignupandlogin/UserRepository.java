package io.project.onlinebooktracker.usersignupandlogin;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CassandraRepository<User, String>{

    
    // @Query("SELECT * from user_by_id WHERE user_email = ?0 and user_password = ?0 ALLOW FILTERING")
    // @AllowFiltering
    // List<User> findbyUserandPassword(String userEmail, String userPass);

    User findByName(String name);
    
}
