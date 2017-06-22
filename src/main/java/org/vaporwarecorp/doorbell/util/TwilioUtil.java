package org.vaporwarecorp.doorbell.util;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.vaporwarecorp.doorbell.model.DoorbellConfiguration;

import java.net.URI;
import java.net.URISyntaxException;

import static com.asosyalbebe.moment4j.Moment.moment;

public class TwilioUtil {
// -------------------------- STATIC METHODS --------------------------

    public static boolean isTwilioValid(DoorbellConfiguration configuration) {
        return configuration.getTwilioAccountSid() != null &&
                configuration.getTwilioAuthToken() != null &&
                configuration.getFromPhoneNumber() != null &&
                configuration.getToPhoneNumber() != null;
    }

    public static void sendMMS(DoorbellConfiguration configuration, String pictureUrl, String videoUrl)
            throws URISyntaxException {
        if (!isTwilioValid(configuration)) {
            return;
        }
        final String time = moment().format("hh:mm a");
        Message
                .creator(new PhoneNumber(configuration.getToPhoneNumber()),
                        new PhoneNumber(configuration.getFromPhoneNumber()),
                        "Motion detected at " + time + " - " + videoUrl)
                .setMediaUrl(new URI(pictureUrl))
                .create();
    }

    public static void sendSMS(DoorbellConfiguration configuration, String videoUrl) {
        if (!isTwilioValid(configuration)) {
            return;
        }
        Message
                .creator(new PhoneNumber(configuration.getToPhoneNumber()),
                        new PhoneNumber(configuration.getFromPhoneNumber()),
                        "DING DONG! Someone is ringing your door! - " + videoUrl)
                .create();
    }
}
