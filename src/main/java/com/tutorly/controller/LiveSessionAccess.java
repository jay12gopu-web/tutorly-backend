package com.tutorly.controller;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
final class LiveSessionAccess {
    private LiveSessionAccess() {}
    static Long id(Authentication auth,String role){
        if(auth==null||!auth.isAuthenticated())throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please log in");
        if(auth.getAuthorities().stream().noneMatch(a->a.getAuthority().equals("ROLE_"+role)))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Wrong account type");
        try{long id=Long.parseLong(auth.getName());if(id>0)return id;}catch(NumberFormatException ignored){}
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid login");
    }
}
