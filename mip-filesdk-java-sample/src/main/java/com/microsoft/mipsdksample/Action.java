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

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.microsoft.informationprotection.ApplicationInfo;
import com.microsoft.informationprotection.CacheStorageType;
import com.microsoft.informationprotection.ContentLabel;
import com.microsoft.informationprotection.Identity;
import com.microsoft.informationprotection.Label;
import com.microsoft.informationprotection.LogLevel;
import com.microsoft.informationprotection.MIP;
import com.microsoft.informationprotection.MipComponent;
import com.microsoft.informationprotection.MipConfiguration;
import com.microsoft.informationprotection.MipContext;
import com.microsoft.informationprotection.ProtectionDescriptor;
import com.microsoft.informationprotection.file.IFileProfile;
import com.microsoft.informationprotection.file.LabelingOptions;
import com.microsoft.informationprotection.file.ProtectionSettings;
import com.microsoft.informationprotection.internal.callback.FileHandlerObserver;
import com.microsoft.informationprotection.file.FileEngineSettings;
import com.microsoft.informationprotection.file.FileProfileSettings;
import com.microsoft.informationprotection.file.IFileEngine;
import com.microsoft.informationprotection.file.IFileHandler;

public class Action {

    AuthDelegateImpl authDelegate;    
    IFileProfile fileProfile;
    IFileEngine fileEngine;
    MipContext mipContext;
    String userName;

    public Action(ApplicationInfo appInfo, String userName) throws InterruptedException, ExecutionException
    {
        this.userName = userName;
        authDelegate = new AuthDelegateImpl(appInfo);
        
        // Initialize MIP For File SDK components.        
        MIP.initialize(MipComponent.FILE, null);

        // Create MIP Configuration
        MipConfiguration mipConfiguration = new MipConfiguration(appInfo, "mip_data", LogLevel.TRACE, false);
        
        // Create MipContext from MipConfiguration
        mipContext = MIP.createMipContext(mipConfiguration);
        
        fileProfile = CreateFileProfile();
        fileEngine = CreateFileEngine(fileProfile);
    }

    private IFileProfile CreateFileProfile() throws InterruptedException, ExecutionException
    {
        ConsentDelegate consentDelegate = new ConsentDelegate();
        FileProfileSettings fileProfileSettings = new FileProfileSettings(mipContext, CacheStorageType.ON_DISK, consentDelegate);
        Future<IFileProfile> fileProfileFuture = MIP.loadFileProfileAsync(fileProfileSettings);
        IFileProfile fileProfile = fileProfileFuture.get();
        return fileProfile;    
    }

    private IFileEngine CreateFileEngine(IFileProfile profile) throws InterruptedException, ExecutionException
    {
        // Create the file engine, passing in the username as the first parameter.
        // This sets the engineId to the username, making it easier to load the cached engine. 
        // Using cached engines reduces service road trips and will use cached use licenses for protected content.
        FileEngineSettings engineSettings = new FileEngineSettings(userName, authDelegate, "", "en-US");
        
        // Set the user identity for the engine. This aids in service discovery.
        engineSettings.setIdentity(new Identity(userName));                

        // Add the engine and get the result. 
        Future<IFileEngine> fileEngineFuture = fileProfile.addEngineAsync(engineSettings);
        IFileEngine fileEngine = fileEngineFuture.get();
        return fileEngine;
    }

    private IFileHandler CreateFileHandler(FileOptions options, IFileEngine engine) throws InterruptedException, ExecutionException
    {
        FileHandlerObserver observer = new FileHandlerObserver();
        Future<IFileHandler> handlerFuture = engine.createFileHandlerAsync(options.InputFilePath, options.InputFilePath, options.GenerateChangeAuditEvent, observer, null);
        return handlerFuture.get();
    }

    public void ListLabels()
    {
        Collection<Label> labels = fileEngine.getSensitivityLabels();
        labels.forEach(label -> { 
            System.out.println(label.getName() + " : " + label.getId());
            if(label.getChildren().size() > 0)
            {
                label.getChildren().forEach(child -> {                
                    System.out.println("\t" + child.getName() + " : " + child.getId());
                });
            }
        });
    }

    public boolean SetLabel(FileOptions options) throws InterruptedException, ExecutionException
    {
        IFileHandler fileHandler = CreateFileHandler(options, fileEngine);

        LabelingOptions labelingOptions = new LabelingOptions();                    
        labelingOptions.setAssignmentMethod(options.AssignmentMethod);
        Label label = fileEngine.getLabelById(options.LabelId);

        fileHandler.setLabel(label, labelingOptions, new ProtectionSettings());

        // Check to see if handler has been modified. If not, skip commit. 
        boolean result = false;
        if(fileHandler.isModified())
        {
            result = fileHandler.commitAsync(options.OutputFilePath).get();
        }

        return result;
    }

    public ContentLabel GetLabel(FileOptions options) throws InterruptedException, ExecutionException
    {
        IFileHandler fileHandler = CreateFileHandler(options, fileEngine);
        return fileHandler.getLabel();
    }

    public ProtectionDescriptor GetProtection(FileOptions options) throws InterruptedException, ExecutionException
    {
        IFileHandler fileHandler = CreateFileHandler(options, fileEngine);
        return fileHandler.getProtection().getProtectionDescriptor();
    }

}
