package com.alfie51m.pronounsMC;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class LangManager {

    private final PronounsMC plugin;
    private FileConfiguration langConfig;

    public LangManager(PronounsMC plugin){ this.plugin = plugin; load(); }
    public void reload(){ load(); }

    private void load(){
        String langFileName = plugin.getPluginConfig().getString("langFile","en_US");
        File langFolder = new File(plugin.getDataFolder(),"lang");
        if(!langFolder.exists()) langFolder.mkdirs();

        List<String> defaults = List.of("de_DE.yml","en_US.yml","es_ES.yml","fr_FR.yml","it_IT.yml","ja_JP.yml","pt_BR.yml","uk_UA.yml","zh_CN.yml");
        for(String name: defaults){
            File file = new File(langFolder,name);
            if(!file.exists()) plugin.saveResource("lang/"+name,false);
        }

        File langFile = new File(langFolder,langFileName+".yml");
        if(!langFile.exists()) plugin.saveResource("lang/"+langFileName+".yml",false);

        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String get(String path,String def){ return langConfig.getString(path,def); }
}
