package com.microsoft.mipsdksample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

import com.microsoft.informationprotection.ApplicationInfo;
import com.microsoft.informationprotection.AssignmentMethod;
import com.microsoft.informationprotection.DataState;

public class App 
{
    public static void main( String[] args ) throws InterruptedException, ExecutionException, IOException
    {
        InputStreamReader input = new InputStreamReader(System.in);         
        BufferedReader reader = new BufferedReader(input);
        
        ApplicationInfo appInfo = new  ApplicationInfo();
        FileOptions options = new FileOptions();

        appInfo.setApplicationId("46d42b3f-c156-46b7-9f7b-55da52c07b8c");
        appInfo.setApplicationName("MIP SDK Java Sample");
        appInfo.setApplicationVersion("1.8.97");

        System.out.print("Enter a username: ");
        String userName = reader.readLine();

        Action action = new Action(appInfo, userName);
        
        action.ListLabels();

        System.out.print("Enter a label Id: ");
        options.LabelId = reader.readLine();

        System.out.print("Enter an input file full path: ");
        options.InputFilePath = reader.readLine();

        System.out.print("Enter an output file full path: ");
        options.OutputFilePath = reader.readLine();

        System.out.println("Applying label to file...");
        
        options.AssignmentMethod = AssignmentMethod.PRIVILEGED;
        options.DataState = DataState.REST;        
        options.IsAuditDiscoveryEnabled = true;

        action.SetLabel(options);

        System.out.println("Reading label from file...");

        options.InputFilePath = options.OutputFilePath;

        System.out.println("File Name: " + options.InputFilePath);
        System.out.println("File Label: " + action.GetLabel(options).label.getName());

        System.out.println("Reading owner from file...");
        System.out.println("File Label: " + action.GetProtection(options).getOwner());

        System.out.println("Press enter to quit.");
        reader.readLine();
    } 
}
