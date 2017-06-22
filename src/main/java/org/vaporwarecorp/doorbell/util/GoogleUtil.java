package org.vaporwarecorp.doorbell.util;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.*;
import org.joda.time.DateTime;
import org.vaporwarecorp.doorbell.model.DoorbellConfiguration;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GoogleUtil {
// ------------------------------ FIELDS ------------------------------

    /**
     * Define a global instance of the HTTP transport.
     */
    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /**
     * Define a global instance of the JSON factory.
     */
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();

// -------------------------- STATIC METHODS --------------------------

    public static LiveBroadcast createBroadcast(YouTube youtube, DateTime expiration) throws IOException {
        // Create a snippet with the title and scheduled start and end
        // times for the broadcast. Currently, those times are hard-coded.
        LiveBroadcastSnippet broadcastSnippet = new LiveBroadcastSnippet()
                .setTitle("Edison Doorbell event")
                .setScheduledStartTime(new com.google.api.client.util.DateTime(DateTime.now().getMillis()))
                .setScheduledEndTime(new com.google.api.client.util.DateTime(expiration.getMillis()));

        // Set the broadcast's privacy status to "unlisted". See:
        // https://developers.google.com/youtube/v3/live/docs/liveBroadcasts#status.privacyStatus
        LiveBroadcastStatus status = new LiveBroadcastStatus().setPrivacyStatus("unlisted");

        LiveBroadcastContentDetails broadcastContentDetails = new LiveBroadcastContentDetails()
                .setEnableLowLatency(true)
                .setEnableDvr(true);

        LiveBroadcast broadcast = new LiveBroadcast()
                .setKind("youtube#liveBroadcast")
                .setSnippet(broadcastSnippet)
                .setStatus(status)
                .setContentDetails(broadcastContentDetails);

        // Construct and execute the API request to insert the broadcast.
        return youtube.liveBroadcasts().insert("snippet,status,contentDetails", broadcast).execute();
    }

    public static LiveStream createLiveStream(YouTube youtube) throws IOException {
        // Create a snippet with the video stream's title.
        LiveStreamSnippet streamSnippet = new LiveStreamSnippet().setTitle("Edison Doorbell event");

        // Define the content distribution network settings for the
        // video stream. The settings specify the stream's format and
        // ingestion type. See:
        // https://developers.google.com/youtube/v3/live/docs/liveStreams#cdn
        CdnSettings cdnSettings = new CdnSettings().setFormat("360p").setIngestionType("rtmp");

        LiveStream stream = new LiveStream()
                .setKind("youtube#liveStream")
                .setSnippet(streamSnippet)
                .setCdn(cdnSettings);

        // Construct and execute the API request to insert the stream.
        return youtube.liveStreams().insert("snippet,cdn", stream).execute();
    }

    public static Credential getCredential(DoorbellConfiguration configuration, TokenResponse tokenResponse) {
        return new GoogleCredential.Builder()
                .setClientSecrets(configuration.getGoogleClientId(), configuration.getGoogleClientSecret())
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .build()
                .setFromTokenResponse(tokenResponse);
    }

    public static Drive getDrive(Credential credential) {
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("edison-doorbell")
                .build();
    }

    public static Gmail getGmail(Credential credential) {
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("edison-doorbell")
                .build();
    }

    public static String getRedirectURL(DoorbellConfiguration configuration) {
        return new AuthorizationCodeRequestUrl(
                "https://accounts.google.com/o/oauth2/auth", configuration.getGoogleClientId())
                .setScopes(Arrays.asList(YouTubeScopes.YOUTUBE, DriveScopes.DRIVE, GmailScopes.GMAIL_SEND))
                .setRedirectUri(configuration.getGoogleRedirectUrl())
                .set("access_type", "offline")
                .set("prompt", "consent")
                .build();
    }

    public static GoogleTokenResponse getTokenByCode(DoorbellConfiguration configuration, String code) throws IOException {
        return new GoogleAuthorizationCodeTokenRequest(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                configuration.getGoogleClientId(),
                configuration.getGoogleClientSecret(),
                code,
                configuration.getGoogleRedirectUrl()
        ).execute();
    }

    public static GoogleTokenResponse getTokenByRefresh(DoorbellConfiguration configuration) throws IOException {
        return new GoogleRefreshTokenRequest(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                configuration.getGoogleRefreshToken(),
                configuration.getGoogleClientId(),
                configuration.getGoogleClientSecret()
        ).execute();
    }

    public static YouTube getYoutube(Credential credential) {
        return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("edison-doorbell")
                .build();
    }

    public static boolean isGmailValid(DoorbellConfiguration configuration) {
        return configuration.getFromEmail() != null && configuration.getToEmail() != null;
    }

    public static boolean isRefreshTokenValid(DoorbellConfiguration configuration) {
        return configuration.getGoogleRefreshToken() != null &&
                configuration.getGoogleClientId() != null &&
                configuration.getGoogleClientSecret() != null;
    }

    public static void sendEmail(Gmail gmail, DoorbellConfiguration configuration, String body)
            throws MessagingException, IOException {
        if (!isGmailValid(configuration)) {
            return;
        }

        MimeMessage email = new MimeMessage(Session.getDefaultInstance(new Properties()));
        email.setFrom(new InternetAddress(configuration.getFromEmail()));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(configuration.getToEmail()));
        email.setSubject("Edison Doorbell Motion Detected");
        email.setText(body, "UTF-8", "html");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        String encodedEmail = Base64.encodeBase64URLSafeString(buffer.toByteArray());
        gmail.users().messages().send("me", new Message().setRaw(encodedEmail)).execute();
    }

    public static Map<String, String> startLiveStream(YouTube youtube) throws IOException {
        // Construct and execute a request to bind the new broadcast and stream.
        Map<String, String> streamInfo = new HashMap<>();
        for (LiveBroadcast liveBroadcast : youtube.liveBroadcasts()
                .list("contentDetails")
                .setFields("items/contentDetails/boundStreamId")
                .setBroadcastType("persistent")
                .setMine(true)
                .execute()
                .getItems()) {
            for (LiveStream liveStream : youtube.liveStreams()
                    .list("id,snippet,cdn")
                    .setId(liveBroadcast.getContentDetails().getBoundStreamId())
                    .execute()
                    .getItems()) {
                streamInfo.put("videoUrl", "https://youtu.be/" + liveStream.getId());

                IngestionInfo info = ((CdnSettings) liveStream.get("cdn")).getIngestionInfo();
                streamInfo.put("streamUrl", info.getIngestionAddress() + "/" + info.getStreamName());
            }
        }
        return streamInfo;
    }

    public static com.google.api.services.drive.model.File uploadPicture(Drive drive, String fileName) {
        File mediaFile = new File(fileName);
        try {
            Drive.Files.Create create = drive.files().create(
                    new com.google.api.services.drive.model.File().setName(fileName),
                    new FileContent("image/jpeg", mediaFile).setType("image/jpeg")
            );
            create.getMediaHttpUploader().setDirectUploadEnabled(true);
            com.google.api.services.drive.model.File createdFile = create.setFields("id,webContentLink").execute();

            drive.permissions()
                    .create(createdFile.getId(), new Permission().setType("anyone").setRole("reader"))
                    .execute();

            return createdFile;
        } catch (IOException e) {
            return null;
        }
    }
}
