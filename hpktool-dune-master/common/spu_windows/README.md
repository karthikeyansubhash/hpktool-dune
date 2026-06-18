# OXPd2 Solution Packaging Utility (SPU) Command Line Interface

This tool allows command-line interaction with the OXPd2 Solution Packaging sequence. It's designed to work with
"ready to package" solution assets.

## Prerequisites & Dependencies

In order to use this tool in Windows, you must have the Windows Subsystem for Linux installed. The link [Install WSL on Windows 10](https://docs.microsoft.com/en-us/windows/wsl/install-win10) provides instructions for setting up the WSL 1 on your Windows 10 machine. It's expected that you use WSL version 1 and the Ubuntu 20.04 distribution as your default WSL container. If using a MAC operating system, depending on version you may need to install OPENSSL in order to sign your solution [Install OPENSSL Information](https://www.openssl.org/source/).

## Available Actions

### Help
Access the built-in help menu.

Example execution:
```com
c:\>spu-cli help
spu-cli 1.0.0
Copyright (C) 2021 HP Inc.

  squashfs    Create a Squash FS from a directory.

  verity      Create DM-Verity hash-image and root-hash from provided squashfs.

  tar         Create gzip'd tarball of specified directory.

  signtar     Create a (detached) signature of the specificed tarball.

  fimpak      Create a FIM PAK.

  fimbdl      Create a FIM BDL.

  fimdump     Dump a FIM PAK/BDL File.

  batch       Execute one-or-more actions using a manifest

  zip         Create zip file from directory (Deprecated).

  help        Display more information on a specific command.

  version     Display version information.

  validate    Validate a solution manifest JSON file
c:\>
c:\>spu-cli help tar
spu-cli 1.0.0
Copyright (C) 2021 HP Inc.
USAGE:
Windows:
  spu-cli tar --input /mnt/d/stage-dir --output /mnt/d/solution.tar.gz
Linux:
  spu-cli tar --input ./stage-dir --output ./solution.tar.gz

  -i, --input     Required. The directory to tarball (must be in Linux format!

  -o, --output    Required. The outname filename of the gzip'd tarball (must be Linux format!)

  --wait          Wait for a key-press to exit upon completion

  --help          Display this help screen.

  --version       Display version information.
```

### Batch
Define one-or-more actions in a single manifest file, and have all defined actions execute in sequence.

Example execution:

```com
c:\>spu-cli batch -m manifest.json -l .\stage\solution.json
```

Here's an example manifest file that demonstrates performing a Squash, Verity, Tar, SignTar, Pak, and Bdl in sequence:
```json
{
  "actions": [ "squashfs", "verity", "tar", "signtar", "fimpak", "fimbdl" ],
  "squashFs": {
    "inputFolderPath": "./test-files/batch/stage",
    "outputFilePath": "./test-files/output/stagetar/solution.squash"
  },
  "verity": {
    "inputFilePath": "./test-files/output/stagetar/solution.squash",
    "outputHashImageFilePath": "./test-files/output/stagetar/solution.squash.hashimg",
    "outputRootHashFilePath": "./test-files/output/stagetar/solution.squash.roothash"
  },
  "tar": {
    "inputFolderPath": "./test-files/output/stagetar",
    "outputFilePath": "./test-files/output/stagepak/solution.tar.gz"
  },
  "signTar": {
    "inputFilePath": "./test-files/output/stagepak/solution.tar.gz",
    "outputFilePath": "./test-files/output/stagepak/solution.tar.gz.sig"
    "privateKeyPath": "./test-files/signTar/rsa.private.pem"
  },
  "fimPak": {
    "inputFolderPath": ".\\test-files\\output\\stagepak",
    "outputFilePath": ".\\test-files\\output\\batched.solution.pak",
    "vendor": "HP",
    "name": "OXPd2-Demo",
    "revision": "1.0.1",
    "description": "Another awesome OXPd2 solution!"
  },
  "fimBdl": {
    "inputFilePath": ".\\test-files\\output\\batched.solution.pak",
    "outputFilePath": ".\\test-files\\output\\batched.solution.bdl",
    "vendor": "HP",
    "name": "OXPd2-Demo",
    "id": "2ADC7CB9-1FDE-464F-98F4-82736571450C",
    "revision": "1.0.1",
    "description": "Another awesome OXPd2 solution!",
    "bundleType": "unverified",
    "serialNumber": "ABC123",
    "supportEmail": "support@hp.com",
    "supportPhone": "(555)555-5555",
    "supportUrl": "support.hp.com"
  }
}
```
### SquashFS
Takes a given input folder path and generates a corresponding SQUASHFS file from it. This action expects paths to be in Linux
format since the action will be performed using the Windows Subsystem for Linux (WSL) when executed on Windows machine.

Example execution:
```com
c:\>spu-cli squashfs -i /mnt/d/projects/solution-assets -o /mnt/d/projects/solution.squash
```

### Verity
The Verity action generates DM-Verity hashes for a given squashfs. These are required for the installation process to succeed, as the HP FW uses the DM-Verity process to check/protect read only filesystems from offline manipulation. This action expects paths to be in Linux format since the action will be performed using the WSL when executed on a Windows machine. NOTE that while the output options (-h, -r) allow for any name, the values shown in the help/example should be used as that is the naming format expected by the installation process.

Example execution:
```com
c:\>spu-cli verity -i ./solution.squash -h ./solution.squash.hashimg -r ./solution.squash.roothash
```

### Tar
The Tar action will tarball and gzip a provided input folder and generate the provided output file. The HP FW installation process for solutions expects the tar/gzip format to be contained within the FIM PAK. This action expects paths to be in Linux format since the action will be performed using the WSL when executed on a Windows machine. Similar to Verity, while the output option allows arbitrary naming, the output filename portion should be "solution.tar.gz" in order to match what the installation

Example execution:
```com
c:\>spu-cli tar -i /mnt/d/stage-dir -o /mnt/d/solution.tar.gz
```

### Sign Tar
The Sign Tar action is used to generate a detached signature of a specified tarball. This signature is needed when generating a FIM PAK, as the PAK for an extensibility solution expects to have both the archive as well as a signature for the archive. This action expects paths to be in Linux format since the action will be performed using the WSL when executed on a Windows machine. And once again, while the output option allows arbitrary naming, the output filename portion should be "solution.tar.gz.sig" to match the filename expected by the installation pipeline.

Example execution:
```com
c:\>spu-cli signtar -i /mnt/d/stage-dir/solution.tar.gz -o /mnt/d/stage-dir/solution.tar.gz.sig -k /mnt/d/[pathToPrivateKey]/rsa.private.pem
```

### FIM PAK
Creates an HP Firmware Installation Manager (FIM) formatted package file (PAK) from a given file or folder.
Note that one should use either the -i or the -f option but not both. If the -i option is provided, any -f option is ignored.

Example execution:
```com
c:\>spu-cli fimpak -i .\solution.zip -o .\solution.pak -v ACME -name "ACME Awesome App" -r 1.0.0 -d "Another awesome app by ACME"
```
Another example execution:
```com
c:\>spu-cli fimpak -f .\stage_dir -o .\solution.pak -v ACME -name "ACME Awesome App" -r 1.0.0 -d "Another awesome app by ACME"
```
### FIM BDL
Creates an HP Firmware Installation Manager (FIM) formatted bundle file (BDL) from a given PAK file. The supported options for the -t (solution type) argument are:
- unverified
- restricted
- unrestricted

The --serial, --email, --phone, --url flags are optional.

Example execution:
```com
c:\>spu-cli fimbdl -i .\solution.pak -o .\solution.bdl -t unverified -v HP -n OXPd2-Demo --id 2ADC7CB9-1FDE-464F-98F4-82736571450C
--revision 1.0.1 -d "Another awesome OXPd 2.0 solution!" [--serial ABC123] [--email support@hp.com] [--phone (555)555-5555]
[--url support.hp.com]
```
### FIM Dump
Takes the provided PAK or BDL file and dumps it out to the specified folder

Example execution:
```com
c:\>spu-cli fimdump -i .\solution.bdl -o .\dump -x true
```

### Zip
Takes a given input folder path and generates a corresponding ZIP file from it (Deprecated).

Example execution:
```com
c:\>spu-cli zip -i .\solution.squash -o .\solution.zip
```

### Validate
Takes a given solution manifest file and determines if it represents a valid solution bundle

Example execution
Example execution:
```com
c:\>spu-cli validate -m .\solution.json
```
