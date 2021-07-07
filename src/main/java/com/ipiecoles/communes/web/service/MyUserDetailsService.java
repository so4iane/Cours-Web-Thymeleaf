package com.ipiecoles.communes.web.service;

import com.ipiecoles.communes.web.model.Role;
import com.ipiecoles.communes.web.model.User;
import com.ipiecoles.communes.web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    //Le transactional permet de garder la requête bdd ouverte tout le temps de la fonction,
    //donc de récupérer l'objet et sa table jointe (user et roles)
    @Transactional
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        //Recupérer en base l'user correspondant au nom passé en paramètre
        User user = userRepository.findByUserName(userName);

        //Lever un exception si l'user n'existe pas
        if(user==null){
            throw new UsernameNotFoundException("Aucun utilisateur nommé "+userName+" n'a été trouvé");
        }

        //Construire UserDetails à partir de l'user récupéré

        return buildSpringUserFromMyUser(user);
    }

    private UserDetails buildSpringUserFromMyUser(User user){
        //Initialise la liste des droit des users à partir de leurs roles
        Set<GrantedAuthority> authorities = new HashSet<>();
        for(Role role: user.getRoles()){
            authorities.add(new SimpleGrantedAuthority(role.getRole()));
        }
        org.springframework.security.core.userdetails.User springUser =
                new org.springframework.security.core.userdetails.User(user.getUserName(), user.getPassword(),authorities);

        return springUser;
    }
}
