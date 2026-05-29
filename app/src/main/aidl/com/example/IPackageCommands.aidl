package com.example;

interface IPackageCommands {
    boolean disablePackage(String packageName);
    boolean enablePackage(String packageName);
    void forceStop(String packageName);
    int getPackageState(String packageName);
}
