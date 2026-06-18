# HP Workpath Solution Utility 
### Requirement
+ This program requires WSL1 and dotnet 6.0 runtime to be installed on windows system.
+ This program requires dotnet 6.0 runtime to be installed on linux system.
+ [dotnet](https://dotnet.microsoft.com/download/dotnet/6.0)
+ [WSL1](https://docs.microsoft.com/en-us/windows/wsl/install)

### This project uses hp codes
1. extensibility-2-0/e2-sdk - java library jar
2. sdk/linkpackagemanager - cpk library codes

### This project uses forked opensource library
1. workpath-dune/Apktool

### This project uses opensource libraries
check ```LICENSE``` file

### Command Line Interface Program
```
cli/HP Workpath Solution Utility.exe --help
```

### Attestation Update Feature

#### New File-based Input Support
Unlike JEDI/JOLT systems that require direct JSON string input, this utility now supports file-based attestation data input for improved usability and maintainability.

**Note**: With the transition to Java 21, the data receiving method from PowerShell and Command Prompt has changed.

#### Usage Examples

**Power Shell Example**
```bash
cli/HP Workpath Solution Utility.exe --attestation-update --host 192.168.1.1 --username myuser --ldbkey mykey --attestation-data '{"client_id":"test_client_id", "client_secret":"test_client_secret"}'
```

**CMD input Example**
```bash
cli/HP Workpath Solution Utility.exe --attestation-update --host 192.168.1.1 --username myuser --ldbkey mykey --attestation-data "[{\\\"client_id\\\":\\\"test_client_id\\\",\\\"client_secret\\\":\\\"test_client_secret\\\"}]"
```


### Graphic User Interface Program
```
gui/HP Workpath Solution Utility.exe
```
