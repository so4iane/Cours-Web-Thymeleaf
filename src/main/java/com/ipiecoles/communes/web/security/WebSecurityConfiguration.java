package com.ipiecoles.communes.web.security;

import com.ipiecoles.communes.web.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

//Indique à spring qu'il doit prendre en compte les éléments définis
@Configuration
//Annotation permettant d'activer la sécurité pour notre appli web
@EnableWebSecurity
//Classe définissant un certain nombre de comportement par défaut
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private MyUserDetailsService userDetailsService;



    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                //Service chargé des opérations d'authentification
                .userDetailsService(userDetailsService)
                //Définit l'algo de hachage des password
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        //Mise en place de l'encoder BCrypt
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .authorizeRequests()
                //La page d'accueil
                .antMatchers("/", "/register")
                //est accessible à tous
                .permitAll()
                .antMatchers(HttpMethod.POST,"/communes/*")
                .hasRole("ADMIN")
                .antMatchers(HttpMethod.POST,"/communes")
                .hasRole("ADMIN")
                .antMatchers(HttpMethod.GET,"/communes//delete")
                .hasRole("ADMIN")
                //Toutes les autres pages
                .anyRequest()
                //demandent de s'identifier
                .authenticated()
                //Activation de la connexion par formulaire
        .and().formLogin()
                //Redirection de l'utiliateur lorsqu'il tente d'accéder à une page protégée
                .loginPage("/login") //Defaut /login
                .permitAll()
                //Redirection de l'utilisateur si la connexion échoue
                .failureUrl("/loginFail")//Defaut /login?error
                //Redirection si la connexion réussit
                .defaultSuccessUrl("/successfulConnection")//pas de valeur par defaut
                //Définition du nom du paramètre contenant l'utilisateur
                .usernameParameter("username")//Defaut username
                //Définition du nom du paramètre contenant le mot de passe
                .passwordParameter("password")//Defaut password
        //Correspond à http.logout
        .and().logout()
                .logoutUrl("/logout")//defaut logout
                .logoutSuccessUrl("/login?logout=true");//Defaut /login?logout

    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/webjars/**");// * => /webjars/test.js ** => //webjars/test/test/test.js
    }
}
