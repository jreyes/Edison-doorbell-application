package org.vaporwarecorp.doorbell.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.drive.model.File;
import com.twilio.Twilio;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.vaporwarecorp.doorbell.model.DoorbellConfiguration;
import org.vaporwarecorp.doorbell.process.YoutubeEncoderProcess;
import org.vaporwarecorp.doorbell.repository.DoorbellConfigurationRepository;
import org.vaporwarecorp.doorbell.util.GoogleUtil;

import java.io.IOException;
import java.util.Map;

import static com.asosyalbebe.moment4j.Moment.moment;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.vaporwarecorp.doorbell.util.GoogleUtil.*;
import static org.vaporwarecorp.doorbell.util.TwilioUtil.sendMMS;
import static org.vaporwarecorp.doorbell.util.TwilioUtil.sendSMS;

@Service
@Transactional(readOnly = true)
public class DoorbellService implements InitializingBean {
// ------------------------------ FIELDS ------------------------------

    private DoorbellConfiguration configuration;
    private Credential credential;
    private YoutubeEncoderProcess encoder;
    private DateTime eventExpiration;
    private boolean notificationSent;
    private DoorbellConfigurationRepository repository;
    private TemplateEngine templateEngine;
    private String videoUrl;

// --------------------------- CONSTRUCTORS ---------------------------

    public DoorbellService(DoorbellConfigurationRepository repository, TemplateEngine templateEngine) {
        this.repository = repository;
        this.templateEngine = templateEngine;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public DoorbellConfiguration getConfiguration() {
        return configuration;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface InitializingBean ---------------------

    @Override
    public void afterPropertiesSet() throws Exception {
        this.encoder = new YoutubeEncoderProcess();

        if (configuration == null) {
            for (DoorbellConfiguration configuration : repository.findAll()) {
                this.configuration = configuration;
            }
        }

        reloadConfiguration();
    }

// -------------------------- OTHER METHODS --------------------------

    public String getRedirectURL() {
        return GoogleUtil.getRedirectURL(configuration);
    }

    public void ping() throws Exception {
        refreshCredential();
        if (credential == null) {
            return;
        }
        if (videoUrl == null) {
            startLiveStream();
        }
        if (videoUrl == null) {
            sendSMS(configuration, videoUrl);
        }
    }

    public void resetMotionEvent() {
        eventExpiration = DateTime.now();
        notificationSent = false;
        videoUrl = null;
    }

    @Transactional
    public void sendNotification(String fileName) throws Exception {
        refreshCredential();
        if (credential == null || videoUrl == null || notificationSent) {
            return;
        }

        notificationSent = true;

        File picture = uploadPicture(getDrive(credential), fileName);
        if (picture != null) {
            sendMMS(configuration, picture.getWebContentLink(), videoUrl);
            sendEmail(getGmail(credential), configuration, getEmailBody(picture.getWebContentLink()));
        }
    }

    @Transactional
    public void startMotionEvent() throws Exception {
        refreshCredential();
        if (credential == null || eventExpiration.isAfterNow() || videoUrl != null) {
            return;
        }
        startLiveStream();
    }

    @Scheduled(cron = "* * * * * ?")
    public void stopLiveStream() {
        if (eventExpiration.isAfterNow() || videoUrl == null) {
            return;
        }
        stopMotionEvent();
    }

    public void stopMotionEvent() {
        eventExpiration = DateTime.now();
        notificationSent = false;
        credential = null;
        videoUrl = null;

        if (encoder != null) {
            encoder.stop();
            encoder = null;
        }
    }

    @Transactional
    public void updateCredentialByCode(String code) throws IOException {
        GoogleTokenResponse response = GoogleUtil.getTokenByCode(configuration, code);
        configuration.setGoogleAccessToken(response.getAccessToken());
        configuration.setGoogleRefreshToken(response.getRefreshToken());
        repository.save(configuration);
    }

    @Transactional
    public void updateGoogleConfiguration(DoorbellConfiguration updatedConfiguration)
            throws IOException {
        configuration.setGoogleClientId(trimToNull(updatedConfiguration.getGoogleClientId()));
        configuration.setGoogleClientSecret(trimToNull(updatedConfiguration.getGoogleClientSecret()));
        configuration.setGoogleRedirectUrl(trimToNull(updatedConfiguration.getGoogleRedirectUrl()));
        configuration.setFromEmail(toLowerCase(updatedConfiguration.getFromEmail()));
        configuration.setToEmail(toLowerCase(updatedConfiguration.getToEmail()));
        configuration.setVideoDuration(updatedConfiguration.getVideoDuration());
        repository.save(configuration);
        reloadConfiguration();
    }

    @Transactional
    public void updateTwilioConfiguration(DoorbellConfiguration updatedConfiguration)
            throws IOException {
        configuration.setTwilioAccountSid(trimToNull(updatedConfiguration.getTwilioAccountSid()));
        configuration.setTwilioAuthToken(trimToNull(updatedConfiguration.getTwilioAuthToken()));
        configuration.setFromPhoneNumber(toLowerCase(updatedConfiguration.getFromPhoneNumber()));
        configuration.setToPhoneNumber(toLowerCase(updatedConfiguration.getToPhoneNumber()));
        repository.save(configuration);
        reloadConfiguration();
    }

    private String getEmailBody(String pictureUrl) {
        Context context = new Context();
        context.setVariable("time", moment().format("hh:mm a"));
        context.setVariable("videoUrl", videoUrl);
        context.setVariable("pictureUrl", pictureUrl);
        return templateEngine.process("notification", context);
    }

    private void refreshCredential() throws IOException {
        if (credential != null || !isRefreshTokenValid(configuration)) {
            return;
        }
        GoogleTokenResponse response = getTokenByRefresh(configuration);
        configuration.setGoogleAccessToken(response.getAccessToken());
        credential = getCredential(configuration, response);
    }

    private void reloadConfiguration() throws IOException {
        if (configuration == null) {
            configuration = new DoorbellConfiguration();
        }
        if (configuration.getTwilioAccountSid() != null && configuration.getTwilioAuthToken() != null) {
            Twilio.init(configuration.getTwilioAccountSid(), configuration.getTwilioAuthToken());
        }

        eventExpiration = DateTime.now();
        notificationSent = false;
        videoUrl = null;
    }

    private void startLiveStream() throws IOException, InterruptedException {
        eventExpiration = DateTime.now().plusMinutes(configuration.getVideoDuration());

        Map<String, String> streamInfo = GoogleUtil.startLiveStream(getYoutube(credential));
        if (!streamInfo.isEmpty()) {
            videoUrl = streamInfo.get("videoUrl");
            encoder.start(streamInfo.get("streamUrl"));
        }
    }

    private String toLowerCase(String s) {
        return StringUtils.lowerCase(trimToNull(s));
    }
}
