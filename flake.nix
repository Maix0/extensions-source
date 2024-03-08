{
  description = "Flake utils demo";

  inputs.flake-utils.url = "github:numtide/flake-utils";

  outputs = {
    self,
    nixpkgs,
    flake-utils,
  }:
    flake-utils.lib.eachDefaultSystem (
      system: let
        pkgs = import nixpkgs {
          inherit system;
          config.allowUnfree = true;
          config.android_sdk.accept_license = true;
        };
        sdk =
          (pkgs.androidenv.composeAndroidPackages {
            buildToolsVersions = ["34.0.0"];
            platformVersions = ["34"];
            abiVersions = ["arm64-v8a"];
            systemImageTypes = [];
            cmdLineToolsVersion = "8.0";
          })
          .androidsdk;
      in {
        devShell = pkgs.mkShell {
          packages = [
            sdk
            pkgs.gradle
          ];
          ANDROID_SDK_ROOT = "${sdk}/libexec/android-sdk";
        };
      }
    );
}
