package com.ipiecoles.communes.web.repository;

import com.ipiecoles.communes.web.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    //Permet de récupérer les roles liés à l'user lorsqu'on appelle cette fonction
    //@EntityGraph(attributePaths = "roles")
    User findByUserName(String userName);
}
