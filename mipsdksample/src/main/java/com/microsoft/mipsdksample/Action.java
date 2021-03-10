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
        MIP.initialize(MipComponent.FILE, null);
        mipContext = MIP.createMipContext(appInfo, "mip_data", LogLevel.TRACE, null, null);
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
        FileEngineSettings engineSettings = new FileEngineSettings(userName, authDelegate, "", "en-US");
        engineSettings.setIdentity(new Identity(userName));        
        engineSettings.setCloud(com.microsoft.informationprotection.Cloud.COMMERCIAL);
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
        return fileHandler.commitAsync(options.OutputFilePath).get();
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
