package thpmc.vanilla_source.lang;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TextContainer {
    
    private final String lang;
    
    private final Map<String, String> textMap = new HashMap<>();
    
    public TextContainer(String lang, YamlConfiguration yml) {
        this.lang = lang;
        
        for (String name : Objects.requireNonNull(yml.getConfigurationSection("")).getKeys(false)) {
            textMap.put(name, yml.getString(name));
        }
    }
    
    public String getLang() {return lang;}
    
    public String getText(String name) {
        String text = textMap.get(name);
        if (text == null) {
            return "Text '" + name + "' is not found.";
        } else {
            return ChatColor.translateAlternateColorCodes('&', text);
        }
    }
    
}
