/*
*
* Copyright (c) Microsoft Corporation.
* All rights reserved.
*
* This code is licensed under the MIT License.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files(the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions :
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*
*/
package com.microsoft.mipsdksample;

import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.MsalException;
import com.microsoft.aad.msal4j.Prompt;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.SystemBrowserOptions;
import com.microsoft.informationprotection.ApplicationInfo;
import com.microsoft.informationprotection.IAuthDelegate;
import com.microsoft.informationprotection.Identity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

public class AuthDelegateImpl implements IAuthDelegate {

    // Set to true if application registration is multi-tenant
    private static boolean isMultiTenant = true;

    // Only required if application registratio is set to single tenant
    private static String tenantId = "d86993cb-5731-4a0e-a83c-464c851bf053";
    
    private static ApplicationInfo _appInfo;

    public AuthDelegateImpl(ApplicationInfo appInfo) {
        _appInfo = appInfo;
    }

    @Override
    public String acquireToken(Identity userName, String authority, String resource, String claims) {

        // If the application is a single tenant application, replace /common with the
        // tenant Id.
        if (authority.toLowerCase().contains("common") && isMultiTenant == false) {

            URI authorityUri;
            try {
                authorityUri = new URI(authority);
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "";
            }

            authority = String.format("https://%s/%s", authorityUri.getHost(), tenantId);
        }

        String token = "";
        try {
            token = acquireTokenInteractive(authority, resource, claims)
                    .accessToken();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return token;
    }

    private static IAuthenticationResult acquireTokenInteractive(String authority, String resource, String claims)
            throws Exception {

        Set<String> scopes = Collections.singleton("");

        // Append .default to resource to generate scopes
        if (resource.endsWith("/")) {
            scopes = Collections.singleton(resource + ".default");
        } else {
            scopes = Collections.singleton(resource + "/.default");
        }

        // Load token cache from file and initialize token cache aspect. The token cache
        // will have dummy data, so the acquireTokenSilently call will fail.
        TokenCacheAspect tokenCacheAspect = new TokenCacheAspect("sample_cache.json");

        PublicClientApplication pca = PublicClientApplication.builder(_appInfo.getApplicationId())
                .authority(authority)
                .setTokenCacheAccessAspect(tokenCacheAspect)
                .build();

        Set<IAccount> accountsInCache = pca.getAccounts().join();
        // Take first account in the cache. In a production application, you would
        // filter
        // accountsInCache to get the right account for the user authenticating.
        IAccount account = accountsInCache.iterator().next();

        IAuthenticationResult result;
        try {
            SilentParameters silentParameters = SilentParameters
                    .builder(scopes, account)
                    .build();

            // try to acquire token silently. This call will fail since the token cache
            // does not have any data for the user you are trying to acquire a token for
            result = pca.acquireTokenSilently(silentParameters).join();
        } catch (Exception ex) {
            if (ex.getCause() instanceof MsalException) {

                InteractiveRequestParameters parameters = InteractiveRequestParameters
                        .builder(new URI("http://localhost"))
                        .prompt(Prompt.SELECT_ACCOUNT) // Change this value to avoid repeated auth prompts.
                        .scopes(scopes)
                        .claimsChallenge(claims)
                        .build();

                result = pca.acquireToken(parameters).join();
            } else {
                // Handle other exceptions accordingly
                throw ex;
            }
        }
        return result;
    }
}
