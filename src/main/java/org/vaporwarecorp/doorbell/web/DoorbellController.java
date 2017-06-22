package org.vaporwarecorp.doorbell.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.vaporwarecorp.doorbell.model.DoorbellConfiguration;
import org.vaporwarecorp.doorbell.service.DoorbellService;

import java.io.IOException;

@Controller
public class DoorbellController {
// ------------------------------ FIELDS ------------------------------

    private final Logger log = LoggerFactory.getLogger(DoorbellController.class);

    private final DoorbellService service;

// --------------------------- CONSTRUCTORS ---------------------------

    public DoorbellController(DoorbellService service) {
        this.service = service;
    }

// -------------------------- OTHER METHODS --------------------------

    @GetMapping("/")
    public String displayConfiguration(Model model) {
        model.addAttribute("configuration", service.getConfiguration());
        return "settings";
    }

    @RequestMapping(value = "/oauth/google")
    public String oauthGoogle() {
        return "redirect:" + service.getRedirectURL();
    }

    @RequestMapping(value = "/oauth/google/callback")
    public String oauthGoogleCallback(@RequestParam("code") String code) {
        try {
            service.updateCredentialByCode(code);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return "redirect:/";
    }

    @RequestMapping(value = "/event/ping")
    public ResponseEntity ping() {
        try {
            service.ping();
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/event/notification")
    public ResponseEntity sendNotification(@RequestParam("fileName") String fileName) {
        try {
            service.sendNotification(fileName);
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/event/start")
    public ResponseEntity startMotionEvent() {
        try {
            service.startMotionEvent();
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            service.resetMotionEvent();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/event/stop")
    public ResponseEntity stopMotionEvent() {
        try {
            service.stopMotionEvent();
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            service.resetMotionEvent();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/configuration/google")
    public String updateGoogleConfiguration(@ModelAttribute DoorbellConfiguration configuration,
                                            RedirectAttributes redirectAttributes) {
        try {
            service.updateGoogleConfiguration(configuration);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/configuration/twilio")
    public String updateTwilioConfiguration(@ModelAttribute DoorbellConfiguration configuration,
                                            RedirectAttributes redirectAttributes) {
        try {
            service.updateTwilioConfiguration(configuration);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }
}
