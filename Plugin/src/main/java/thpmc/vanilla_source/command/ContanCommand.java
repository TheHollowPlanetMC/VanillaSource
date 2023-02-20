package thpmc.vanilla_source.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import org.bukkit.ChatColor;
import org.contan_lang.ContanEngine;
import org.contan_lang.ContanModule;
import org.contan_lang.environment.Environment;
import org.contan_lang.evaluators.Evaluator;
import org.contan_lang.evaluators.FunctionBlock;
import org.contan_lang.syntax.tokens.Token;
import org.contan_lang.variables.ContanObject;
import org.contan_lang.variables.primitive.ContanFunctionExpression;
import org.contan_lang.variables.primitive.ContanVoidObject;
import org.contan_lang.variables.primitive.JavaClassInstance;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.contan.ContanManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ContanCommand {

    public static void register() {

        new CommandAPICommand("contan").withSubcommands(
                new CommandAPICommand("test")
                        .withArguments(new TextArgument("code"))
                        .executes(((sender, args) -> {
                            ContanEngine contanEngine = VanillaSourceAPI.getInstance().getContanEngine();

                            //Compile and run script.
                            try {
                                ContanModule contanModule = contanEngine.compile(".command", (String) args[0]);
                                Evaluator globalEvaluator = contanModule.getGlobalEvaluator();
                                Environment environment = new Environment(contanEngine, contanModule.getModuleEnvironment(),
                                        contanEngine.getMainThread(), globalEvaluator, true);
                                environment.createOrSetVariable("sender", new JavaClassInstance(contanEngine, sender));

                                ContanObject<?> result = globalEvaluator.eval(environment);
                                if (result == ContanVoidObject.INSTANCE) {
                                    result = environment.getReturnValue();
                                    if (result == null) {
                                        result = ContanVoidObject.INSTANCE;
                                    }
                                }

                                if (result instanceof ContanFunctionExpression) {
                                    ContanFunctionExpression functionExpression = (ContanFunctionExpression) result;
                                    FunctionBlock functionBlock = functionExpression.getBasedJavaObject();
                                    Token token = functionBlock.getFunctionName();
                                    ContanObject<?> argument = new JavaClassInstance(contanEngine, sender);

                                    result = functionExpression.eval(contanEngine.getMainThread(), token, argument);
                                }

                                sender.sendMessage("§aResult : " + result);
                            } catch (Exception e) {
                                String[] lines = e.getMessage().split("\n");
                                for (String line : lines) {
                                    sender.sendMessage(ChatColor.RED + line.replace("\n", ""));
                                }
                            }
                        })),
                new CommandAPICommand("run")
                        .withArguments(new TextArgument("module").replaceSuggestions(ArgumentSuggestions.strings(info -> ContanManager.loadedModuleNames.toArray(new String[0]))))
                        .executes((sender, args) -> {
                            String moduleName = (String) args[0];

                            ContanEngine contanEngine = VanillaSourceAPI.getInstance().getContanEngine();
                            ContanModule contanModule = contanEngine.getModule(moduleName);
                            if (contanModule == null) {
                                sender.sendMessage(ChatColor.RED + "Module '" + moduleName + "' is not found.");
                                return;
                            }

                            //Run script.
                            try {
                                Evaluator globalEvaluator = contanModule.getGlobalEvaluator();
                                Environment environment = new Environment(contanEngine, contanModule.getModuleEnvironment(),
                                        contanEngine.getMainThread(), globalEvaluator, true);
                                environment.createOrSetVariable("sender", new JavaClassInstance(contanEngine, sender));

                                ContanObject<?> result = globalEvaluator.eval(environment);
                                if (result == ContanVoidObject.INSTANCE) {
                                    result = environment.getReturnValue();
                                    if (result == null) {
                                        result = ContanVoidObject.INSTANCE;
                                    }
                                }

                                if (result instanceof ContanFunctionExpression) {
                                    ContanFunctionExpression functionExpression = (ContanFunctionExpression) result;
                                    FunctionBlock functionBlock = functionExpression.getBasedJavaObject();
                                    Token token = functionBlock.getFunctionName();
                                    ContanObject<?> argument = new JavaClassInstance(contanEngine, sender);

                                    result = functionExpression.eval(contanEngine.getMainThread(), token, argument);
                                }

                                sender.sendMessage("§aResult : " + result);
                            } catch (Exception e) {
                                String[] lines = e.getMessage().split("\n");
                                for (String line : lines) {
                                    sender.sendMessage(ChatColor.RED + line);
                                }
                            }
                        }),
                new CommandAPICommand("reload")
                        .withArguments(new TextArgument("module").replaceSuggestions(ArgumentSuggestions.strings(info -> ContanManager.loadedModuleNames.toArray(new String[0]))))
                        .executes((sender, args) -> {
                            String moduleName = (String) args[0];

                            StringBuilder script = new StringBuilder();

                            if (!moduleName.endsWith(".cntn")) {
                                moduleName = moduleName + ".cntn";
                            }

                            String moduleFilePath = ContanManager.SCRIPT_PATH_NAME + "/" + moduleName;

                            File file = new File(moduleFilePath);
                            if (!file.exists()) {
                                sender.sendMessage(ChatColor.RED + "Module '" + moduleName + "' not found.");
                                return;
                            }

                            try{
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file) , StandardCharsets.UTF_8));
                                String data;
                                while ((data = bufferedReader.readLine()) != null) {
                                    script.append(data);
                                    script.append('\n');
                                }
                                bufferedReader.close();

                                ContanEngine contanEngine = VanillaSourceAPI.getInstance().getContanEngine();
                                ContanModule contanModule = contanEngine.compile(moduleName, script.toString());
                                contanModule.initialize(contanEngine.getMainThread());

                                ContanManager.loadedModuleNames.add(moduleName);

                                sender.sendMessage(ChatColor.GREEN + "Script '" + moduleName + "' has been loaded and initialized.");
                            }catch(Exception e){
                                throw new IllegalStateException("Failed to load script file '" + moduleName + "'.", e);
                            }
                        }))
                .withPermission("vanilla_source.contan")
                .register();

    }

}
