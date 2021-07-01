package com.ipiecoles.communes.web.controller;

import com.ipiecoles.communes.web.model.Commune;
import com.ipiecoles.communes.web.repository.CommuneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Controller
public class IndexController {

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
        if(search==null || search.isEmpty()){
            communes = communeRepository.findAll(pageRequest);
        }else{
            commune = communeRepository.findById(search);
            if(commune != null){
                return "redirect:/communes/" + commune.get().getCodeInsee();
            }
            communes = communeRepository.findByNomContainingIgnoreCase(search, pageRequest);
        }

        model.put("communes", communes);
        model.put("nbCommunes", communes.getTotalElements());
        model.put("pageSizes", Arrays.asList("5", "10", "20", "50", "100"));
        model.put("start", (page+1)*size-(size-1));
        model.put("end", (page+1)*size);
        model.put("page", page);
        return "list";
    }
}
