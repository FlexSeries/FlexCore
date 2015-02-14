package me.st28.flexseries.flexcore.terms;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

public final class TermsManager extends FlexModule<FlexCore> {

    private Map<String, Terms> registeredTerms = new HashMap<>();
    private Map<UUID, List<String>> agreedTerms = new HashMap<>();

    private YamlFileManager agreedTermsFile;

    public TermsManager(FlexCore plugin) {
        super(plugin, "termsofservice", "Allows players to accept/decline various TOS required for different server features");
    }

    @Override
    public void handleLoad() throws Exception {
        agreedTermsFile = new YamlFileManager(plugin.getDataFolder() + File.separator + "agreedTerms.yml");
        FileConfiguration config = agreedTermsFile.getConfig();
        for (String rawUuid : config.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(rawUuid);
            } catch (Exception ex) {
                LogHelper.warning(FlexCore.class, "Invalid UUID in agreedTerms.yml: " + rawUuid);
                continue;
            }

            agreedTerms.put(uuid, config.getStringList(rawUuid));
        }
    }

    @Override
    public void handleReload() {
        registeredTerms.clear();

        ConfigurationSection config = getConfig().getConfigurationSection("Terms");
        for (String key : config.getKeys(false)) {
            String name = config.getString(key + ".name");
            String description = config.getString(key + ".description");
            List<String> content = config.getStringList(key + ".content");

            registerTerms(new Terms(name, description, content.toArray(new String[content.size()])));
        }
    }

    @Override
    public void handleSave(boolean async) {
        FileConfiguration config = agreedTermsFile.getConfig();

        for (Entry<UUID, List<String>> entry : agreedTerms.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
        }

        agreedTermsFile.save();
    }

    public boolean registerTerms(Terms terms) {
        String name = terms.name.toLowerCase();
        if (registeredTerms.containsKey(name.toLowerCase())) {
            return false;
        }

        registeredTerms.put(name.toLowerCase(), terms);
        return true;
    }

    public Collection<Terms> getRegisteredTerms() {
        return Collections.unmodifiableCollection(registeredTerms.values());
    }

    public Terms getTerms(String name) {
        return registeredTerms.get(name.toLowerCase());
    }

    public boolean hasPlayerAgreed(UUID uuid, Terms terms) {
        return agreedTerms.containsKey(uuid) && agreedTerms.get(uuid).contains(terms.name.toLowerCase());
    }

    public boolean setAgreement(UUID uuid, Terms terms, boolean agree) {
        if (hasPlayerAgreed(uuid, terms) == agree) return false;

        if (agree) {
            if (!agreedTerms.containsKey(uuid)) {
                agreedTerms.put(uuid, new ArrayList<String>());
            }

            agreedTerms.get(uuid).add(terms.name.toLowerCase());
        } else {
            agreedTerms.get(uuid).remove(terms.name.toLowerCase());
        }
        return true;
    }

}