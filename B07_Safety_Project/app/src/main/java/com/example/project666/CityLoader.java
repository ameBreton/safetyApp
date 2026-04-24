package com.example.project666;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

/**
 * CityLoader is used to load information from the support.json file into the fields sourcesMap
 * and descriptionsMap (which we use in Aid.java to generate the source descriptions)
 *
 * @author Amelie Breton
 *
 * @version 0.0
 */
public class CityLoader {

    /**
     * This method reads from a JSON file (support.json from raw) and generates the contents of
     * context, sourcesMap, and descriptionsMap
     * @param context contains the current context of the app
     * @param sourcesMap contains a hashmap mapping lists of source names to their city
     * @param descriptionsMap contains a hashmap mapping lists of source links to their city
     */
    public static void loadSupportData(Context context,
                                       HashMap<String, List<String>> sourcesMap,
                                       HashMap<String, List<String>> descriptionsMap) {
        try {
            InputStream input = context.getResources().openRawResource(R.raw.support);
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            input.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            Type listType = new TypeToken<List<SupportEntry>>() {}.getType();
            List<SupportEntry> entries = gson.fromJson(json, listType);

            for (SupportEntry entry : entries) {
                sourcesMap.put(entry.city, entry.supportNames);
                descriptionsMap.put(entry.city, entry.connection);
            }

        } catch (Exception ignored) {
        }
    }


    /**
     * SupportEntry is a helper class to match the JSON structure
     * It bundles the city, the sources, and the links together into a singular unit
     */
    public static class SupportEntry {
        public String city;
        public List<String> supportNames;
        public List<String> connection;
    }
}


