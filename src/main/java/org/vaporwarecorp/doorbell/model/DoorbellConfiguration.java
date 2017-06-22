package org.vaporwarecorp.doorbell.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(name = "doorbell")
public class DoorbellConfiguration implements Serializable {
// ------------------------------ FIELDS ------------------------------

    @Column(name = "auth0_client_Id", length = 100, unique = true)
    @Size(max = 100)
    private String auth0ClientId;

    @Column(name = "auth0_client_secret", length = 100)
    @Size(max = 100)
    private String auth0ClientSecret;

    @Column(name = "auth0_domain", length = 50)
    @Size(max = 50)
    private String auth0Domain;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "configuration_id")
    private Integer configurationId;

    @Column(name = "from_email", length = 50)
    @Size(max = 50)
    private String fromEmail;

    @Column(name = "from_phone_number", length = 20)
    @Size(max = 20)
    private String fromPhoneNumber;

    @Column(name = "google_access_token", length = 200)
    @Size(max = 200)
    private String googleAccessToken;

    @Column(name = "google_client_Id", length = 100, unique = true)
    @Size(max = 100)
    private String googleClientId;

    @Column(name = "google_client_secret", length = 100)
    @Size(max = 100)
    private String googleClientSecret;

    @Column(name = "google_redirect_url", length = 100)
    @Size(max = 100)
    private String googleRedirectUrl;

    @Column(name = "google_refresh_token", length = 100)
    @Size(max = 100)
    private String googleRefreshToken;

    @Column(name = "to_email", length = 50)
    @Size(max = 50)
    private String toEmail;

    @Column(name = "to_phone_number", length = 20)
    @Size(max = 20)
    private String toPhoneNumber;

    @Column(name = "twilio_account_sid", length = 50)
    @Size(max = 50)
    private String twilioAccountSid;

    @Column(name = "twilio_auth_token", length = 50)
    @Size(max = 50)
    private String twilioAuthToken;

    @Column(name = "video_duration", nullable = false)
    @NotNull
    private Integer videoDuration;

// --------------------------- CONSTRUCTORS ---------------------------

    public DoorbellConfiguration() {
        setVideoDuration(10);
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getAuth0ClientId() {
        return auth0ClientId;
    }

    public void setAuth0ClientId(String auth0ClientId) {
        this.auth0ClientId = auth0ClientId;
    }

    public String getAuth0ClientSecret() {
        return auth0ClientSecret;
    }

    public void setAuth0ClientSecret(String auth0ClientSecret) {
        this.auth0ClientSecret = auth0ClientSecret;
    }

    public String getAuth0Domain() {
        return auth0Domain;
    }

    public void setAuth0Domain(String auth0Domain) {
        this.auth0Domain = auth0Domain;
    }

    public Integer getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(Integer configurationId) {
        this.configurationId = configurationId;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getFromPhoneNumber() {
        return fromPhoneNumber;
    }

    public void setFromPhoneNumber(String fromPhoneNumber) {
        this.fromPhoneNumber = fromPhoneNumber;
    }

    public String getGoogleAccessToken() {
        return googleAccessToken;
    }

    public void setGoogleAccessToken(String googleAccessToken) {
        this.googleAccessToken = googleAccessToken;
    }

    public String getGoogleClientId() {
        return googleClientId;
    }

    public void setGoogleClientId(String googleClientId) {
        this.googleClientId = googleClientId;
    }

    public String getGoogleClientSecret() {
        return googleClientSecret;
    }

    public void setGoogleClientSecret(String googleClientSecret) {
        this.googleClientSecret = googleClientSecret;
    }

    public String getGoogleRedirectUrl() {
        return googleRedirectUrl;
    }

    public void setGoogleRedirectUrl(String googleRedirectUrl) {
        this.googleRedirectUrl = googleRedirectUrl;
    }

    public String getGoogleRefreshToken() {
        return googleRefreshToken;
    }

    public void setGoogleRefreshToken(String googleRefreshToken) {
        this.googleRefreshToken = googleRefreshToken;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getToPhoneNumber() {
        return toPhoneNumber;
    }

    public void setToPhoneNumber(String toPhoneNumber) {
        this.toPhoneNumber = toPhoneNumber;
    }

    public String getTwilioAccountSid() {
        return twilioAccountSid;
    }

    public void setTwilioAccountSid(String twilioAccountSid) {
        this.twilioAccountSid = twilioAccountSid;
    }

    public String getTwilioAuthToken() {
        return twilioAuthToken;
    }

    public void setTwilioAuthToken(String twilioAuthToken) {
        this.twilioAuthToken = twilioAuthToken;
    }

    public Integer getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(Integer videoDuration) {
        this.videoDuration = videoDuration;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public String toString() {
        return "DoorbellConfiguration{" +
                "auth0ClientId='" + auth0ClientId + '\'' +
                ", auth0ClientSecret='" + auth0ClientSecret + '\'' +
                ", auth0Domain='" + auth0Domain + '\'' +
                ", configurationId=" + configurationId +
                ", fromEmail='" + fromEmail + '\'' +
                ", fromPhoneNumber='" + fromPhoneNumber + '\'' +
                ", toEmail='" + toEmail + '\'' +
                ", toPhoneNumber='" + toPhoneNumber + '\'' +
                ", twilioAccountSid='" + twilioAccountSid + '\'' +
                ", twilioAuthToken='" + twilioAuthToken + '\'' +
                ", videoDuration=" + videoDuration +
                '}';
    }
}
