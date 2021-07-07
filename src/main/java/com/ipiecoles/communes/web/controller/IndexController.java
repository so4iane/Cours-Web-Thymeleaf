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
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

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

        //On pagine la request pour savoir à quelle page on est et si on est sur la dernière page
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(sortDirection), sortProperty);
        Page<Commune>communes;
        Optional<Commune> commune;
        if (search!=null && !search.isEmpty()){
            commune = communeRepository.findById(search);
            if(commune != null && !commune.isEmpty()){
                return "redirect:/communes/" + commune.get().getCodeInsee();
            }else{
                log.warn("search : "+search);
                log.warn("pageRequest : "+pageRequest);
                communes = communeRepository.findByNomContainingIgnoreCase(search, pageRequest);
                if(commune.isEmpty()){
                    throw new EntityNotFoundException("Impossible de trouver la commune avec la recherche " + search);
                }
            }
        }
        else{
            communes = communeRepository.findAll(pageRequest);
        }
        model.put("communes", communes);
        model.put("nbCommunes", communes.getTotalElements());
        model.put("pageSizes", Arrays.asList("5", "10", "20", "50", "100"));
        model.put("start", (page+1)*size-(size-1));
        model.put("end", (page+1)*size);
        model.put("page", page);
        model.put("sortDirection", sortDirection);
        model.put("sortProperty", sortProperty);
        model.put("size", size);
        model.put("search", search);
        return "list";
    }
}
