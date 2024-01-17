package pl.dawad.blpriceupdater.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class AppChecker {
    @RequestMapping("/bl-price-updater")
    public String printText() {
        return "App is working...";
    }
}