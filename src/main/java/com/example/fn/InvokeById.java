/***
 * @author shaunsmith
 * @author abhirockzz
 */

package com.example.fn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Supplier;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.functions.FunctionsInvokeClient;
import com.oracle.bmc.functions.requests.InvokeFunctionRequest;
import com.oracle.bmc.functions.responses.InvokeFunctionResponse;
import com.oracle.bmc.util.StreamUtils;

public class InvokeById {

    static String ERR_MSG = "Usage: java -jar <jar-name>.jar <invoke endpoint> <functionid> <(optional) payload string>";

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            throw new Exception(ERR_MSG);
        }

        String invokeEndpointURL = args[0];
        String functionId = args[1];
        String payload = args.length == 3 ? args[2] : "";

        SimpleAuthenticationDetailsProvider authDetails = getAuthDetails();
        try (FunctionsInvokeClient fnInvokeClient = new FunctionsInvokeClient(authDetails);) {
            fnInvokeClient.setEndpoint(invokeEndpointURL);
            
            InvokeFunctionRequest ifr = InvokeFunctionRequest.builder().functionId(functionId)
                    .invokeFunctionBody(StreamUtils.createByteArrayInputStream(payload.getBytes())).build();

            System.err.println("Invoking function endpoint - " + invokeEndpointURL + " with payload " + payload);
            InvokeFunctionResponse resp = fnInvokeClient.invokeFunction(ifr);
            System.out.println(IOUtils.toString(resp.getInputStream(), StandardCharsets.UTF_8));
        }

    }

    private static SimpleAuthenticationDetailsProvider getAuthDetails() throws Exception {
        String tenantId = System.getenv("TENANT_OCID");
        String userId = System.getenv("USER_OCID");
        String fingerprint = System.getenv("PUBLIC_KEY_FINGERPRINT");
        String privateKeyFile = System.getenv("PRIVATE_KEY_LOCATION");
        String passphrase = System.getenv("PASSPHRASE");

        if (tenantId == null || userId == null || fingerprint == null || privateKeyFile == null) {
            throw new Exception(
                    "Please ensure you have set the mandatory environment variables - TENANT_OCID, USER_OCID, PUBLIC_KEY_FINGERPRINT, PRIVATE_KEY_LOCATION");
        }

        Supplier<InputStream> privateKeySupplier = () -> {
            try {
                return new FileInputStream(new File(privateKeyFile));
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        };

        SimpleAuthenticationDetailsProvider authDetails = SimpleAuthenticationDetailsProvider.builder()
                .tenantId(tenantId).userId(userId).fingerprint(fingerprint).privateKeySupplier(privateKeySupplier)
                .passPhrase(passphrase).region(Region.US_PHOENIX_1).build();
        return authDetails;
    }

}
