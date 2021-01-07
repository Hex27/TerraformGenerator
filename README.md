![Screenshot](images/title.png)

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]


<!-- TABLE OF CONTENTS -->
# Table of Contents
<details open="open">
  <summary>Table of Contents</summary>
  <ol>
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

## Support <a name="support"></a>

Support Discord: https://discord.gg/yW7JcqM

## About project & Wiki <a name="about"></a>

Wiki: https://github.com/Hex27/TerraformGenerator/wiki/Configuration

TerraformGenerator is a world generator plugin that aims to provide an enhanced vanilla feel. As of now, it is in an Alpha stage, meaning some core features are not implemented yet (a list of known issues and to-dos is at the bottom). However, the world should be playable to an extent, though I will not recommend putting it on any production environment that has strict requirements, because as of now, I am in mandatory National service, and support will be bottle-necked. 

There are commands, but they are developer commands and only available via /op. Try not to use them unless you understand what they do.

On another note, for 1.15.2, the version released on 23rd Feb 2020 is the recommended version, as it fixes an itchy spigot issue. More details at the bottom under "bugs". 1.14.4 does not have this fix.

There is also support for Java 14, but be warned that it will spit some minor errors, because the plugin does some Java illegal magic to get things done.

## Getting started <a name="getting-started"></a>

### Method One
- Turn off the server if it is running
- Install Drycell
- Place TerraformGenerator.jar in your plugins folder
- Inside bukkit.yml, add this, where "world" is your world name:
```YAML
worlds:
    world:
        generator: TerraformGenerator
```
- Delete the old world folder (if present), and start the server

### Method two
- Install Drycell
- Place TerraformGenerator.jar in your plugins folder
- Inside bukkit.yml, add this, where "world" is your world name:
```YAML
worlds:
    world:
        generator: TerraformGenerator
```
- Delete the old world folder (if present), and start the server
- Use the command "/mv create genworld normal -g TerraformGenerator" from Multiverse

##### Probably works with multiworld, but I didn't try it.

<a name="images"></a>

Find images on the wiki 

https://github.com/Hex27/TerraformGenerator/wiki/Biomes

https://github.com/Hex27/TerraformGenerator/wiki/Structures

<!-- MARKDOWN LINKS -->
[contributors-shield]: https://img.shields.io/github/contributors/Hex27/terraformgenerator.svg?style=for-the-badge
[contributors-url]: https://github.com/Hex27/terraformgenerator/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/Hex27/terraformgenerator.svg?style=for-the-badge
[forks-url]: https://github.com/Hex27/terraformgenerator/network/members
[stars-shield]: https://img.shields.io/github/stars/Hex27/terraformgenerator.svg?style=for-the-badge
[stars-url]: https://github.com/Hex27/terraformgenerator/stargazers
[issues-shield]: https://img.shields.io/github/issues/Hex27/terraformgenerator.svg?style=for-the-badge
[issues-url]: https://github.com/Hex27/terraformgenerator/issues
