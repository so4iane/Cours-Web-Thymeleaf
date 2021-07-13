package com.ipiecoles.communes.web.controller;

import com.ipiecoles.communes.web.model.Commune;
import com.ipiecoles.communes.web.repository.CommuneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.*;

@Controller
public class IndexController {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    CommuneRepository communeRepository;

    @GetMapping(value = "/")
    public String index(@RequestParam(defaultValue = "0", value = "page") Integer page,
                        @RequestParam(defaultValue = "10", value = "size") Integer size,
                        @RequestParam(defaultValue = "codeInsee") String sortProperty,
                        @RequestParam(defaultValue = "ASC") String sortDirection,
                        @RequestParam(required = false)String search,
                        final ModelMap model) {
        log.warn("page : "+page );
        log.warn("sortDirection : "+sortDirection);
        log.warn("sortProperty : "+sortProperty);
        log.warn("size : "+size);
        if(sortDirection!=null && !sortDirection.equals("ASC") && !sortDirection.equals("DESC")){
            throw new IllegalArgumentException("Le paramètre sortDirection ne peut être que ASC ou DESC");
        }
        List<Integer> sizeList = new ArrayList<>();
        sizeList.addAll(Arrays.asList(5,10,20,50,100));
        if(size != null && !sizeList.contains(size)){
            throw new IllegalArgumentException("Taille de la page non respectée");
        }
        if(sortProperty!=null && !sortProperty.equals("codeInsee") && !sortProperty.equals("nom") && !sortProperty.equals("codePostal") && !sortProperty.equals("latitude") && !sortProperty.equals("longitude")){
            throw new IllegalArgumentException("Merci d'affecter le nom d'une colonne au sorProperty");
        }

        //On pagine la request pour savoir à quelle page on est et si on est sur la dernière page
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(sortDirection), sortProperty);
        Page<Commune>communes;
        Optional<Commune> commune;
        if (search!=null && !search.isEmpty() && !search.equals("null")){
            commune = communeRepository.findById(search);
            if(commune != null && !commune.isEmpty()){
                return "redirect:/communes/" + commune.get().getCodeInsee();
            }else{
                log.warn("search : "+search);
                communes = communeRepository.findByNomContainingIgnoreCase(search, pageRequest);
                if(communes.isEmpty()){
                    throw new EntityNotFoundException("Impossible de trouver la commune avec la recherche : " + search);
                }
            }
        }
        else{
            communes = communeRepository.findAll(pageRequest);
        }
        if (page<0 || (page+1) > communes.getTotalElements()/size)
        {
            throw new IllegalArgumentException("La page choisi n'existe pas");
        }
        model.put("communes", communes);
        model.put("nbCommunes", communes.getTotalElements());
        //model.put("pageSizes", Arrays.asList("5", "10", "20", "50", "100"));
        model.put("start", (page+1)*size-(size-1));
        model.put("end", (page+1)*size);
        model.put("page", page);
        model.put("sortDirection", sortDirection);
        model.put("sortProperty", sortProperty);
        model.put("size", size);
        model.put("search", search);
        model.put("sizeList", sizeList);

        //Fragments
        model.put("fragment", "communeList");
        model.put("template", "list");
        return "main";
    }
}
