package thpmc.vanilla_source.contan;

import org.contan_lang.ContanEngine;
import org.contan_lang.ContanModule;
import thpmc.vanilla_source.VanillaSource;
import thpmc.vanilla_source.api.VanillaSourceAPI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContanManager {

    private static final String SCRIPT_PATH = "plugins/VanillaSource/scripts";

    public static Set<String> loadedModuleNames = new HashSet<>();

    public static void loadAllModules() throws Exception {
        loadedModuleNames = new HashSet<>();

        VanillaSource.getPlugin().getLogger().info("Load all Contan modules.");

        File file = new File(SCRIPT_PATH);
        file.mkdirs();

        ContanEngine contanEngine = VanillaSourceAPI.getInstance().getContanEngine();

        //Load all files
        List<Path> scriptFilePaths;
        try (Stream<Path> paths = Files.walk(Paths.get(SCRIPT_PATH))) {
            scriptFilePaths = paths.filter(Files::isRegularFile).collect(Collectors.toList());
        }


        List<ContanModule> modules = new ArrayList<>();
        ContanModule initializer = null;

        //Compile all source codes
        for (Path path : scriptFilePaths) {
            if (!path.toString().endsWith(".cntn")) {
                continue;
            }

            StringBuilder script = new StringBuilder();

            try{
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path.toFile()) , StandardCharsets.UTF_8));
                String data;
                while ((data = bufferedReader.readLine()) != null) {
                    script.append(data);
                    script.append('\n');
                }
                bufferedReader.close();
            }catch(Exception e){
                throw new IllegalStateException("Failed to load script file '" + path.toFile().getName() + "'.", e);
            }

            String modulePathName = file.toURI().relativize(path.toFile().toURI()).toString();

            VanillaSource.getPlugin().getLogger().info("Compile script : " + modulePathName);
            ContanModule contanModule = contanEngine.compile(modulePathName, script.toString());

            loadedModuleNames.add(modulePathName);

            if (modulePathName.endsWith("_initializer.cntn")) {
                initializer = contanModule;
            } else {
                modules.add(contanModule);
            }
        }


        if (initializer != null) {
            initializer.initialize(contanEngine.getMainThread());
        }


        for (ContanModule contanModule : modules) {
            VanillaSource.getPlugin().getLogger().info("Initialize module : " + contanModule.getRootName());
            contanModule.initialize(contanEngine.getMainThread());
        }
    }


}

