package net.rikkido;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;

import net.rikkido.Lang.Messages;

public class LanguageLoader {

    private String _lang;
    private Messages _msgs;

    public LanguageLoader(Zipline zipline) throws Exception {
        var folder = zipline.getDataFolder();
        _lang = zipline.config.language.Lang.value;
        var dest = Path.of(folder.getPath(), "ja.json");
        String regex = "^w{2}\\.json";
        File[] jsons = folder.listFiles(f -> f.getName().matches(regex));
        if (!Files.exists(dest)) {

            var resStream = zipline.getClass().getResourceAsStream("/ja.json");
            if (resStream == null) {
                throw new Exception("cant find ja.json at resouce");
            }
            Files.copy(resStream, dest);
        }
        File languagefile = null;
        dest = Path.of(folder.getPath(), _lang + ".json");
        zipline.getLogger().info("state: " + Files.exists(dest));
        if (Files.exists(dest))
            languagefile = new File(dest.toString());
        if (languagefile == null) {
            throw new FileNotFoundException("No Such a LanguageFile " + _lang + ".json");
        }
        var json = Files.readString(languagefile.toPath());
        parseLanguageFile(json);
    }

    private void parseLanguageFile(String json) throws Exception {
        Gson gson = new Gson();
        _msgs = gson.fromJson(json, Messages.class);
        if (_msgs == null) {
            throw new Exception("language File is Null");
        }

    }

    public String getMessage(String id) {
        if (_msgs == null)
            return null;
        var res = _msgs.lang.stream().filter(f -> f.id.equals(id)).toList();
        if (res.size() <= 0)
            return null;
        return res.get(0).msg;
    }
}