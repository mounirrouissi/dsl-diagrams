package com.dsl.controller;

import com.dsl.grammar.AivaLexer;
import com.dsl.grammar.AivaParser;
import com.dsl.models.*;

// Import ANTLR runtime classes
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

// Import Spring Boot classes
import org.springframework.web.bind.annotation.*;

// Import standard Java classes
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller to handle the parsing of AIVA scripts.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class ParsingController {

    @PostMapping("/parse")
    public List<StateNode> parseScript(@RequestBody String script) {
        System.out.println("Received script for parsing:\n" + script);

        CharStream input = CharStreams.fromString(script);


        AivaLexer lexer = new AivaLexer(input);


        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Step 4: Create a parser that understands the grammar and feeds off the token buffer.
        AivaParser parser = new AivaParser(tokens);

        // Step 5: Begin parsing at the top-level 'script' rule defined in Aiva.g4.
        // This returns a "Parse Tree" which is ANTLR's representation of the script's structure.
        AivaParser.ScriptContext scriptContext = parser.script();

        // Step 6: Convert the ANTLR-specific Parse Tree into our own clean data model (AST).
        System.out.println("Parsing completed. Building AST..." + buildAstFromParseTree(scriptContext));
        return buildAstFromParseTree(scriptContext);
    }


    private List<StateNode> buildAstFromParseTree(AivaParser.ScriptContext scriptContext) {
        // Navigate down the tree: script -> track -> state list
        return scriptContext.track().state().stream().map(stateCtx -> {
            StateNode stateNode = new StateNode();
            // The first IDENTIFIER in a state rule is the state's ID (e.g., "S1", "S2")
            stateNode.setId(stateCtx.IDENTIFIER(0).getText());

            List<Action> actions = new ArrayList<>();

            for (AivaParser.ActionContext actionCtx : stateCtx.action()) {

                String actionType = actionCtx.getChild(0).getText();

                if ("CALL_FUNCTION_SWITCH".equals(actionType)) {
                    SwitchAction switchAction = new SwitchAction();

                    switchAction.setFunction(actionCtx.IDENTIFIER(0).getText() + " " + actionCtx.IDENTIFIER(1).getText());
                    switchAction.setBranches(new HashMap<>());

                    for (AivaParser.BranchContext branchCtx : actionCtx.branch()) {
                        String branchLabel = branchCtx.IDENTIFIER(0).getText(); // e.g., "VIP"
                        String targetState = branchCtx.IDENTIFIER(1).getText(); // e.g., "R1"
                        switchAction.getBranches().put(branchLabel, targetState);
                    }
                    actions.add(switchAction);

                } else if ("SEND_TEMPLATE".equals(actionType)) {
                    SendTemplateAction sendAction = new SendTemplateAction();
                    sendAction.setTemplateName(actionCtx.IDENTIFIER(0).getText());
                    actions.add(sendAction);

                } else if ("GOTO".equals(actionType)) {
                    GotoAction gotoAction = new GotoAction();
                    gotoAction.setTargetStateId(actionCtx.IDENTIFIER(0).getText());
                    actions.add(gotoAction);
                }
            }
            stateNode.setActions(actions);
            System.out.println("Parsed StateNode: " + stateNode + " with actions: " + actions);
            return stateNode;
        }).collect(Collectors.toList());
    }
}