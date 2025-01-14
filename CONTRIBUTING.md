# Contributing to Dimple

## Setting up development environment

Create `dimple` development environment from `environment.yml` file:

```bash
conda env create -f environment.yml
conda activate dimple
```

This environment includes its own Java JVM.

Configure your IDE to use that environment.

## Building garpy-dimple conda package

To build the `garpy-dimple` package, you only need to run:

```bash
gradle buildConda
```

You can install this into local conda environments using:

```bash
conda install --use-local garpy-dimple
```

If you make changes and want to build again (for local testing) 
you should first increment the build number in the meta.yaml file. 
Reset it to zero when you increment the version number.

## Uploading garpy-dimple to artifactory.analog.com

You must have been granted write permission to upload to the
[garage-conda-local] repository in order to upload this or
any other package.

### Using garconda

If you have installed [garconda], you can use [garconda upload][garconda-upload] to 
upload the package to the Analog Artifactory [garage-conda-local] repository:

```bash
garconda upload -c garage-conda-local --latest 'soar-*' 
```
You can add `--dry-run` to see what would be uploaded before actually uploading.
Add `--help` to see information about more options.

### Using the web UI

Alternatively, you can simply upload via the artifactory web interface:

1. Open [garage-conda-local] in your browser
2. Log in with your Analog credentials using the button in the upper right corner.
3. Click on `garage-conda-local` in the left pane to see the list of subdirectories
4. Select the directory appropriate to the architecture you built (e.g. `osx-arm64`)
5. Press the `Deploy` button in the upper right corner
6. Use `Select File` in the dialog select the file to upload from your local
    file system (in the `conda-bld` directory). Then press `Deploy` to complete the upload.


[Artifactory]: https://artifactory.analog.com/
[garconda]: http://boston-garage.pages.gitlab.analog.com/garconda/
[garage-conda-local]: https://artifactory.analog.com/ui/repos/tree/General/garage-conda-local
[garconda-upload]: http://boston-garage.pages.gitlab.analog.com/garconda/cli/garconda-upload/
