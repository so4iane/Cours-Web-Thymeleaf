package com.ipiecoles.communes.web.controller;

import com.ipiecoles.communes.web.model.Commune;
import com.ipiecoles.communes.web.repository.CommuneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@Validated
@RequestMapping("/communes")
public class CommunesController {
    Logger log = LoggerFactory.getLogger(this.getClass());

    public static final Double DEGRE_LAT_KM = 111d;
    public static final Double DEGRE_LONG_KM = 77d;

    @Autowired
    CommuneRepository communeRepository;

    //@PreAuthorize("permitAll()")
    @GetMapping(value = "/{codeInsee}")
    public String commune(@PathVariable("codeInsee") String codeInsee,
                          @RequestParam(defaultValue = "10") @Min(value = 1, message = "Le périmètre ne peut être ni négatif, ni null")
                          @Max(value=20, message = "Merci de choisir un périmètre inférieur ou égal à 20 km") Integer perimetre,
                          final ModelMap model) {
        log.warn("périmètre : "+perimetre);
        Optional<Commune> commune = communeRepository.findById(codeInsee);
        if (commune.isEmpty() || commune == null){
            throw new EntityNotFoundException("Impossible de récupérer la commune, le code Insee n'existe pas");
        }
        model.put("commune", commune.get());
        model.put("communeProches", this.findCommunesProches(commune.get(), perimetre));
        model.put("perimetre", perimetre);
        model.put("newCommune", false);
        return "detail";
    }

    @GetMapping(value = "/new")
    public String newCommune(final ModelMap model) {
        model.put("commune", new Commune());
        model.put("newCommune", true);
        return "detail";
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String postNewCommune(@Valid Commune commune,final BindingResult result,
                                 RedirectAttributes attributes, ModelMap model) {
        log.warn("on est dans le create!");
        if(!result.hasErrors()){
            if(communeRepository.findById(commune.getCodeInsee())!=null && !communeRepository.findById(commune.getCodeInsee()).isEmpty()){
                throw new IllegalArgumentException("Le code Insee choisi pour cette commune existe déjà!");
            }else {
                log.warn("tout est OK lol");
                try {
                    commune = communeRepository.save(commune);
                    attributes.addFlashAttribute("type", "success");
                    attributes.addFlashAttribute("message", "Création de la Commune effectuée");

                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Impossible de sauvegarder la commune : " + e.getMessage());
                }
                log.warn("create tout va bien!");
                return "redirect:/communes/" + commune.getCodeInsee();
            }
        }
        else{
            //Avant
            if(commune.getCodeInsee()==null || commune.getCodeInsee().isEmpty() || commune.getCodeInsee()==""){
                throw new IllegalArgumentException("Impossible de sauvegarder une commune sans code insee !");
            }
            //Possibilité 1 faire un return sur la page d'erreur
            //Possibilité 2 on reste sur la même page en passant les messages d'erreur
            model.addAttribute("type", "danger");
            model.addAttribute("message", "Erreur lors de la sauvegarde de la commune");
            return "detail";
        }
    }

    @PostMapping(value = "/{codeInsee}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String updateCommune(@PathVariable("codeInsee") String codeInsee, @Valid Commune commune,
                                final BindingResult result, RedirectAttributes attributes, final ModelMap model) {
        log.warn("on est dans le update!");
        //Si pas d'erreurs de validation
        if(!result.hasErrors()){
            try{
                commune = communeRepository.save(commune);
                attributes.addFlashAttribute("type", "success");
                attributes.addFlashAttribute("message", "Modification de la Commune effectuée");
            }catch (IllegalArgumentException e){
                throw new IllegalArgumentException("Impossible de sauvegarder la commune : "+e.getMessage());
            }
            log.warn("update tout va bien!");
            return "redirect:/communes/" + commune.getCodeInsee();
        }
        else{
            //Possibilité 1 faire un return sur la page d'erreur
            //Possibilité 2 on reste sur la même page en passant les messages d'erreur
            model.addAttribute("type", "danger");
            model.addAttribute("message", "Erreur lors de la sauvegarde de la commune");
            return "detail";
        }
    }

    @GetMapping("/{codeInsee}/delete")
    public String deleteCommune(
            @PathVariable String codeInsee, RedirectAttributes attributes) {
        Optional<Commune> commune = communeRepository.findById(codeInsee);
        if (commune.isEmpty() || commune == null){
            throw new EntityNotFoundException("Impossible de supprimer la commune, le code Insee n'existe pas");
        }
        communeRepository.deleteById(codeInsee);
        attributes.addFlashAttribute("type", "success");
        attributes.addFlashAttribute("message", "Suppression de la Commune effectuée");
        return "redirect:/";
    }

    /**
     * Récupère une liste des communes dans un périmètre autour d'une commune
     *
     * @param commune       La commune sur laquelle porte la recherche
     * @param perimetreEnKm Le périmètre de recherche en kilomètre
     * @return La liste des communes triées de la plus proche à la plus lointaine
     */
    private List<Commune> findCommunesProches(Commune commune, Integer perimetreEnKm) {
        Double latMin, latMax, longMin, longMax, degreLat, degreLong;
        //1 degré latitude = 111km, 1 degré longitude = 77km
        degreLat = perimetreEnKm / DEGRE_LAT_KM;
        degreLong = perimetreEnKm / DEGRE_LONG_KM;
        latMin = commune.getLatitude() - degreLat;
        latMax = commune.getLatitude() + degreLat;
        longMin = commune.getLongitude() - degreLong;
        longMax = commune.getLongitude() + degreLong;
        List<Commune> communesProches = communeRepository.findByLatitudeBetweenAndLongitudeBetween(latMin, latMax, longMin, longMax);
        ;
        return communesProches.stream().
                filter(commune1 -> !commune1.getNom().equals(commune.getNom()) && commune1.getDistance(commune.getLatitude(), commune.getLongitude()) <= perimetreEnKm).
                sorted(Comparator.comparing(o -> o.getDistance(commune.getLatitude(), commune.getLongitude()))).
                collect(Collectors.toList());
    }

}
