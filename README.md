# ShopChest

ShopChest - Spigot/Bukkit Plugin

## API

For the moment, the project structure can change a lot, and there is no official API planned.
If you want to use ShopChest functionality, just add the .jar with the version you want to use to your project.
Maybe in future I will post an official API through github packages, but currently I don't have the time.

## Build

You need Maven and a jdk.

- First, compile spigot with the BuildTools.jar and the '--remapped' argument.
  You need to do it for a lot of minecraft version, starting from 1.17.
  To get them all, just do the next step, an error message will tell you what minecraft version is missing.
  (I will try to suppress this step in future)
- Use ``mvn clean package`` or ``mvn clean install`` at the root of the project to build ShopChest artefact.
- After the build succeeded, the ShopChest.jar is found in the ``/plugin/target/`` folder.

## Issues

If you find any issues, please provide them in the [Issues Section](https://github.com/Flowsqy/ShopChest/issues) with a
good description of how to reproduce it. If you get any error messages in the console, please also provide them.

## Download

I don't post build for this branch as it's in progress. If you want to use it, build it.
