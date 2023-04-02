package thpmc.vanilla_source.api.contan;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.contan_lang.variables.primitive.ContanFunctionExpression;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class HoverText {

    private static final AtomicLong globalEventId = new AtomicLong(0);

    private static final Map<Long, HoverText> hoverTextMap = new ConcurrentHashMap<>();

    public static HoverText getHoverText(long id) {return hoverTextMap.get(id);}




    private final TextComponent textComponent;
    private ContanFunctionExpression event = null;
    private final Set<UUID> allowPlayers = new HashSet<>();
    private final List<HoverText> joinedTextList = new ArrayList<>();


    public HoverText(String text) {
        this.textComponent = new TextComponent(TextComponent.fromLegacyText(text));
    }

    public void setClickEvent(ContanFunctionExpression event) {
        this.event = event;
        long id = globalEventId.getAndAdd(1);
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vanilla_source_hover_text_event " + id));
        hoverTextMap.put(id, this);
    }

    public ContanFunctionExpression getClickEvent() {return event;}

    public boolean isAllowed(Player player) {return this.allowPlayers.contains(player.getUniqueId());}

    public void setAllowed(Player player) {
        this.allowPlayers.add(player.getUniqueId());
        for (HoverText text : this.joinedTextList) {
            text.setAllowed(player);
        }
    }

    public TextComponent getTextComponent() {return textComponent;}

    public void join(HoverText hoverText) {
        this.textComponent.addExtra(hoverText.textComponent);
        this.joinedTextList.add(hoverText);
    }

    public void sendMessage(Player player) {
        setAllowed(player);
        player.spigot().sendMessage(textComponent);
    }

}
