package org.terraform.command.contants;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public abstract class TerraCommandArgument<T> {

    private String name;

    private boolean isOptional;

    public TerraCommandArgument(String name, boolean isOptional) {
        this.name = name;
        this.isOptional = isOptional;
    }

    public abstract @Nullable T parse(CommandSender sender, String value);

    public abstract String validate(CommandSender sender, String value);

    /**
     * Should return a list of valid parameters
     * to show when tab completing the command
     */
    public @NotNull ArrayList<String> getTabOptions(String[] args) {
        return new ArrayList<>();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the isOptional
     */
    public boolean isOptional() {
        return isOptional;
    }

    /**
     * @param isOptional the isOptional to set
     */
    public void setOptional(boolean isOptional) {
        this.isOptional = isOptional;
    }

}