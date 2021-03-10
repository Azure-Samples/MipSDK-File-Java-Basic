package com.microsoft.mipsdksample;

import com.microsoft.informationprotection.DataState;
import com.microsoft.informationprotection.AssignmentMethod;

public class FileOptions {
    public String InputFilePath;
    public String OutputFilePath;
    public String LabelId;
    public DataState DataState;
    public AssignmentMethod AssignmentMethod;    
    public boolean IsAuditDiscoveryEnabled;
    public boolean GenerateChangeAuditEvent;
}
