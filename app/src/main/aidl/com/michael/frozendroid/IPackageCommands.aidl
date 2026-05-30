package com.michael.frozendroid;

interface IPackageCommands {
    boolean disablePackage(String packageName);
    boolean enablePackage(String packageName);
    void forceStop(String packageName);
    int getPackageState(String packageName);
    String executeShellCommand(String command);
}
