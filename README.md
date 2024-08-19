![Screenshot](https://imgur.com/R84KoN2.png)

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]


<!-- TABLE OF CONTENTS -->

# Table of Contents

<details open="open">
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#building">Building</a></li>
    <li><a href="#support">Support</a></li>
    <li><a href="#about">About Project & Wiki</a></li>
    <li><a href="#getting-started">Getting started</a></li>
    <li><a href="#images">Images</a></li>
    <li><a href="#known-bugs">Known-Bugs</a>
    <ul>
        <li><a href="#sort-of-resolved">Sort of resolved</a></li>
    </ul>
    </li>
  </ol>
</details>

<h2 name="building">Building</h2>

```bash
git clone https://github.com/Hex27/TerraformGenerator.git
cd TerraformGenerator
gradlew buildProj:shadowJar
```

The jar at `buildProj/build/libs/buildProj-all.jar` is the plugin.

<h2 name="support">Support</h2>

Support Discord: https://discord.gg/yW7JcqM

<h2 name="about">About project & Wiki</h2>

Wiki: https://github.com/Hex27/TerraformGenerator/wiki/Configuration

TerraformGenerator is a world generator plugin that aims to provide an enhanced vanilla feel. As of now, it is in an
Alpha stage, meaning some core features are not implemented yet (a list of known issues and to-dos is at the bottom).
However, the world should be playable to an extent, though I will not recommend putting it on any production environment
that has strict requirements, because as of now, I am in mandatory National service, and support will be bottle-necked.

There are commands, but they are developer commands and only available via /op. Try not to use them unless you
understand what they do.

On another note, for 1.15.2, the version released on 23rd Feb 2020 is the recommended version, as it fixes an itchy
spigot issue. More details at the bottom under "bugs". 1.14.4 does not have this fix.

There is also support for Java 14, but be warned that it will spit some minor errors, because the plugin does some Java
illegal magic to get things done.

<h2 name="getting-started">Getting started</h2>

<h3>Step one: Install TerraformGenerator</h3>

Make sure that you have installed the latest version of [TerraformGenerator][spigot-tfg].  
To do that, stop your server and move the jar file to your server's `plugins` directory. Afterwards can you start your
server again.

<h3>Step two: Create the world</h3>
You can choose between two methods on how to generate the world with TerraformGenerator

<h4>Method One</h4>

1. Turn off the server if it is running
2. Open the `bukkit.yml` and add the following info to it.
   ```yaml
   # By default does this section not exist within the bukkit.yml
   # and you have to add it yourself.
   worlds:
       world: # Replace this with the World name you want to use.
           generator: TerraformGenerator
   ```
3. If present delete the folder of the world that you want TerraformGenerator to create.
4. Start your Server again.

<h4>Method two</h4>
This method requires the usage of a World Management plugin. *DO METHOD ONE FIRST.*
In this example are we using Multiverse-Core. TerraformGenerator *should* work with any other World Manager, but we
won't guarantee it!

Before creating the world, make sure it doesn't exist already. If it does will you need to delete it
using `/mvdelete <world>` followed by `/mvconfirm`.  
To create a world with TerraformGenerator, execute `/mvcreate <world> normal -g TerraformGenerator` where `<world>` is
the name of the world to create.

**Note:**  
There is a rare chance that the world might not get loaded with TerraformGenerator as the World Generator set. In those
cases could newly loaded chunks break the look of the world by being normal vanilla chunks. That may mean that you
haven't done method one. If you have, try doing method one alone without the world management plugin. If the problem
persists, report it on github or discord.

<h2 name="images">Images</h2>
Find images on the wiki

https://github.com/Hex27/TerraformGenerator/wiki/Biomes

https://github.com/Hex27/TerraformGenerator/wiki/Structures

<h2 name="known-bugs">Known bugs</h2>
Not maintained here. You can check the issue tracker. If you've found a bug, you can open an issue, or report it on
discord

<!-- MARKDOWN LINKS -->

[contributors-shield]: https://img.shields.io/github/contributors/Hex27/terraformgenerator.svg?style=for-the-badge

[contributors-url]: https://github.com/Hex27/terraformgenerator/graphs/contributors

[forks-shield]: https://img.shields.io/github/forks/Hex27/terraformgenerator.svg?style=for-the-badge

[forks-url]: https://github.com/Hex27/terraformgenerator/network/members

[stars-shield]: https://img.shields.io/github/stars/Hex27/terraformgenerator.svg?style=for-the-badge

[stars-url]: https://github.com/Hex27/terraformgenerator/stargazers

[issues-shield]: https://img.shields.io/github/issues/Hex27/terraformgenerator.svg?style=for-the-badge

[issues-url]: https://github.com/Hex27/terraformgenerator/issues

[spigot-tfg]: https://www.spigotmc.org/resources/75132/
