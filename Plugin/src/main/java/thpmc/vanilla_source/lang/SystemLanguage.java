package thpmc.vanilla_source.lang;

import org.bukkit.configuration.file.YamlConfiguration;
import thpmc.vanilla_source.VanillaSource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SystemLanguage {
    
    private static String lang = "en_US";
    
    private static TextContainer textContainer = null;
    
    private static final Map<String, TextContainer> langMap = new HashMap<>();
    
    public static String getLang() {return lang;}
    
    public static void setLang(String lang) {SystemLanguage.lang = lang;}
    
    @SuppressWarnings("all")
    public static void loadTexts() {
        File dir = new File("plugins/VanillaSource/lang");
    
        dir.getParentFile().mkdir();
        dir.mkdir();
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IllegalStateException("File IO error.");
        }
        if (files.length == 0) {
            VanillaSource.getPlugin().saveResource("en_US.yml", false);
            VanillaSource.getPlugin().saveResource("ja_JP.yml", false);
        }
    
        for (File file : files) {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            String name = file.getName().replace(".yml", "");
            TextContainer textContainer = new TextContainer(name, yml);
            langMap.put(name, textContainer);
        }
        
        textContainer = langMap.get(lang);
    }
    
    
    public static String getText(String name) {
        if (textContainer == null) {
            return "Language file is not found.";
        }
        return textContainer.getText(name);
    }
    
    public static String getText(String name, Object... objects) {
        String text = getText(name);
        return String.format(text, objects);
    }
    
    
}
