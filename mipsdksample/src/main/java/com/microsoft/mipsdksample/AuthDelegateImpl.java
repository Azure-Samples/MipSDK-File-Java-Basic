package com.microsoft.mipsdksample;

import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.MsalException;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.informationprotection.ApplicationInfo;
import com.microsoft.informationprotection.IAuthDelegate;
import com.microsoft.informationprotection.Identity;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

public class AuthDelegateImpl implements IAuthDelegate {

    private static String CLIENT_ID = "";
    private static String AUTHORITY = "";
    private static Set<String> SCOPE = Collections.singleton("");

    public AuthDelegateImpl(ApplicationInfo appInfo)
    {
        CLIENT_ID = appInfo.getApplicationId();
    }

    @Override
    public String acquireToken(Identity userName, String authority, String resource, String claims) {
        if(resource.endsWith("/")){
            SCOPE = Collections.singleton(resource + ".default");        
        }
        else {
            SCOPE = Collections.singleton(resource + "/.default");        
        }

        AUTHORITY = authority;
        String token = "";
        try {
            token = acquireTokenInteractive().accessToken();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return token;
    }

    
    private static IAuthenticationResult acquireTokenInteractive() throws Exception {

        // Load token cache from file and initialize token cache aspect. The token cache will have
        // dummy data, so the acquireTokenSilently call will fail.
        TokenCacheAspect tokenCacheAspect = new TokenCacheAspect("sample_cache.json");

        PublicClientApplication pca = PublicClientApplication.builder(CLIENT_ID)
                .authority(AUTHORITY)
                .setTokenCacheAccessAspect(tokenCacheAspect)
                .build();

        Set<IAccount> accountsInCache = pca.getAccounts().join();
        // Take first account in the cache. In a production application, you would filter
        // accountsInCache to get the right account for the user authenticating.
        IAccount account = accountsInCache.iterator().next();

        IAuthenticationResult result;
        try {
            SilentParameters silentParameters =
                    SilentParameters
                            .builder(SCOPE, account)
                            .build();

            // try to acquire token silently. This call will fail since the token cache
            // does not have any data for the user you are trying to acquire a token for
            result = pca.acquireTokenSilently(silentParameters).join();
        } catch (Exception ex) {
            if (ex.getCause() instanceof MsalException) {

                InteractiveRequestParameters parameters = InteractiveRequestParameters
                        .builder(new URI("http://localhost"))
                        .scopes(SCOPE)
                        .build();

                // Try to acquire a token interactively with system browser. If successful, you should see
                // the token and account information printed out to console
                result = pca.acquireToken(parameters).join();
            } else {
                // Handle other exceptions accordingly
                throw ex;
            }
        }
        return result;
    }
}
