Nover Package stands for 'No versioned package'

This utility library can be used to dynamically alter your plugin's binary to 
point to the current minecraft-versioned packages. It uses the powerful ASM
bytecode manipulation library to accomplish this without any noticeable loss 
of performance. This utility library can be used in multiple ways.

Plugin references to net.minecraft.server and org.bukkit.craftbukkit are automatically 
redirected to point to an existent package. This means that on a 1.4.5 MC version:

    net.minecraft.server -> net.minecraft.server.v1_4_5
    net.minecraft.server.v1_4_5 -> net.minecraft.server.v1_4_5
    org.bukkit.craftbukkit.v1_5_1 -> org.bukkit.craftbukkit.v1_4_5
    
If your plugin is running on a CraftBukkit build prior to the package versioning changes, 
it will still function. In that case:

    net.minecraft.server.v1_4_5 -> net.minecraft.server
    org.bukkit.craftbukkit.v1_5_1 -> org.bukkit.craftbukkit
    
This way maximum flexibility is achieved.

Important:
* You are completely free to use all code used in this library.
* You do not have to credit me (bergerkiller) for writing this library.
* I am in no way responsable for any damages of any kind that this library may cause.
* This library uses an altered class loader, and class loader hacks can become unstable.
* Use this library responsably. Safeguard your code so it won't cause world corruption.

Referencing this library
-----------------

If you wish your plugin to use this library, you can reference it without including it.
To use the fixed plugin loader of this library, you can do two things:

plugin.yml
-------

Add the following lines to the plugin.yml of your plugin:

    depend: [Nover Package]
    class-loader-of: 'Nover Package'

Static block
-------

Add the following static block of code to your plugin's main class:

    static {
        NoverPackage.undoPackageVersioning(MyPlugin.class);
    }

Change MyPlugin to the main class name of your plugin.

Including (shading) this library
-----------------

This avoids having to download this plugin or having to provide links to it on your main plugin page.

Maven
-------

First, you need access to the code itself. If you use Maven, include the following to your pom.xml:

    [code todo]

Manual
-------

You can copy-paste all the java source files into your plugin's package root.
For example, if my plugin has package

    com.bergerkiller.bukkit.myplugin
    
You place all java files of this library in there in such a way that the root is still used:

    com.bergerkiller.bukkit.myplugin.noverpackage
    com.bergerkiller.bukkit.myplugin.noverpackage.org.objectweb.asm
    
And so on. This is very important: If you do not do the above, you risk the same noverpackage with the same root being used twice.
This can cause several incompatibilities if one plugin uses an older version than another, so this is to prevent that.
The maven method automatically takes are of all of this.