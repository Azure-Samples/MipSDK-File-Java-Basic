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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

import com.microsoft.informationprotection.ApplicationInfo;
import com.microsoft.informationprotection.AssignmentMethod;
import com.microsoft.informationprotection.DataState;

public class App {
    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        // ApplicationInfo is used to store the application name, clientId, and version.
        ApplicationInfo appInfo = new ApplicationInfo();
        FileOptions options = new FileOptions();

        appInfo.setApplicationId("46d42b3f-c156-46b7-9f7b-55da52c07b8c");
        appInfo.setApplicationName("MIP SDK Java Sample");
        appInfo.setApplicationVersion("1.10");

        System.out.print("Enter a username: ");
        String userName = reader.readLine();

        Action action = new Action(appInfo, userName);

        // Fetch the list of labels for the authenticated user and display.
        action.ListLabels();

        // Copy a label Id from the output and paste into the prompt.
        System.out.print("Enter a label Id: ");
        options.LabelId = reader.readLine();

        // Provide an input file that should be labeled.
        System.out.print("Enter an input file full path: ");
        options.InputFilePath = reader.readLine();

        // Provide the output path for the file. The original file remains intact and a
        // copy is created.
        System.out.print("Enter an output file full path: ");
        options.OutputFilePath = reader.readLine();

        System.out.println("Applying label to file...");

        // The privileged AssignmentMethod is used when users label files, and can
        // override STANDARD.
        options.AssignmentMethod = AssignmentMethod.PRIVILEGED;

        // Information on datastate is used to populate audit information. This field
        // doesn't impact the SDK behavior.
        options.DataState = DataState.REST;
        
        // Sets whether the sample should generate an audit event.
        options.IsAuditDiscoveryEnabled = true;

        // Apply the chosen label to the input file.

        boolean result = action.SetLabel(options);

        if (result) {
            // This section attempts to read the label from the file, if one was applied.
            System.out.println("Reading label from file...");

            options.InputFilePath = options.OutputFilePath;

            System.out.println("File Name: " + options.InputFilePath);
            System.out.println("File Label: " + action.GetLabel(options).label.getName());

            System.out.println("Reading owner from file...");
            System.out.println("File Label: " + action.GetProtection(options).getOwner());
        }

        else 
        {
            System.out.println("No changes were written to the input file.");
        }

        System.out.println("Press enter to quit.");
        reader.readLine();
    }
}
