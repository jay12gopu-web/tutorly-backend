package com.tutorly.service;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class MeetingUrlValidator {
    private MeetingUrlValidator() {}
    static String validate(String input){
        if(input==null||input.length()>2048)throw invalid();
        String value=input.strip();
        try {
            URI uri=new URI(value);
            String host=uri.getHost()==null?"":uri.getHost().toLowerCase(Locale.ROOT);
            boolean allowed=host.equals("meet.google.com")||host.equals("zoom.us")||host.endsWith(".zoom.us")
                    ||host.equals("teams.microsoft.com")||host.equals("teams.live.com");
            if(!"https".equalsIgnoreCase(uri.getScheme())||!allowed||uri.getRawUserInfo()!=null
                    ||(uri.getPort()!=-1&&uri.getPort()!=443)||uri.getRawFragment()!=null
                    ||uri.getRawPath()==null||uri.getRawPath().length()<2||value.contains("\\")
                    ||value.codePoints().anyMatch(c->Character.isISOControl(c)||Character.isWhitespace(c)))throw invalid();
            // Preserve encoded meeting IDs and query passcodes. Never fetch or follow the URL.
            if(uri.toASCIIString().length()>2048)throw invalid();
            return uri.toASCIIString();
        } catch(URISyntaxException ex){throw invalid();}
    }
    private static ResponseStatusException invalid(){return new ResponseStatusException(HttpStatus.BAD_REQUEST,
        "Use a full HTTPS participant meeting link from Google Meet, Zoom (zoom.us), or Teams; no user info, fragment or non-443 port");}
}
