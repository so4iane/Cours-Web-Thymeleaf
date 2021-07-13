package com.ipiecoles.communes.web.controller;

import com.ipiecoles.communes.web.model.Role;
import com.ipiecoles.communes.web.model.User;
import com.ipiecoles.communes.web.repository.RoleRepository;
import com.ipiecoles.communes.web.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Collections;

@Controller
public class UserController {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/login")
    public String login(ModelMap model){
        log.warn("Page d'authentification");
        model.addAttribute("user", new User());
        return "login";
    }
    @GetMapping("/successfulConnection")
    public String registerOk(RedirectAttributes attributes){
        log.info("Authentification réussie");
        attributes.addFlashAttribute("type", "success");
        attributes.addFlashAttribute("message", "Connexion réussie");
        return "redirect:/?successfulConnection=true";
    }
    @GetMapping("/loginFail")
    public String registerKo(ModelMap model, RedirectAttributes attributes){
        log.info("Authentification échouée");
        model.addAttribute("user", new User());
        attributes.addFlashAttribute("type", "failure");
        attributes.addFlashAttribute("message", "La combinaison utilisateur/mot de passe est incorrecte");
        return "redirect:/login?error=true";
    }

    @GetMapping("/logout")
    public String logout(RedirectAttributes attributes){
        log.info("Déconnexion");
        attributes.addFlashAttribute("type", "success");
        attributes.addFlashAttribute("message", "Déconnexion réussie");
        return "redirect:/login?logout=true";
    }

    @GetMapping("/register")
    public String registerNewUser(){
        return "register";
    }

    @PostMapping("/register")
    public String createNewUser(@Valid User user, BindingResult bindingResult,
                                final ModelMap model,
                                RedirectAttributes attributes){
        //Vérifier si un User existe déjà avec le même nom
        User userExists = userRepository.findByUserName(user.getUserName());
        if(userExists != null){
            bindingResult.rejectValue("userName", "error.username",
                    "Nom d'utilisateur déjà pris");
        }
        log.warn("new user ok");

        //Gérer les erreurs de validation
        if(bindingResult.hasErrors()){
            log.warn("des erreurs présentes dans le form");
            //Si pas OK je reste sur la page d'inscription
            // en indiquant les erreurs pour chaque champ
            model.addAttribute("type", "danger");
            model.addAttribute("message", "Erreur lors de l'inscription de l'utilisateur");
            return "register";
        }

        //Si OK je sauvegarde le User en hâchant son mot de passe
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        //Affecter le rôle USER...
        Role userRole = roleRepository.findByRole("ROLE_USER");
        user.setRoles(Collections.singletonList(userRole));
        //Gérer une validation par email... Ici valide par défaut
        user.setActive(true);
        //Sauvegarde en BDD
        userRepository.save(user);

        //Redirige vers Login avec un message de succès
        attributes.addFlashAttribute("type", "success");
        attributes.addFlashAttribute("message", "Inscription réussie, vous pouvez vous connecter");
        return "redirect:/login";
    }


}
