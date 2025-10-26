package miroshka.allayideplugin

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

class AllayDslCompletionContributor : CompletionContributor() {
    
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            AllayDslCompletionProvider()
        )
    }
    
    private class AllayDslCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val position = parameters.position
            
            if (!isBuildGradleKts(position)) return
            
            if (isInsideAllayBlock(position)) {
                addAllayBlockCompletions(result)
            }
            
            if (isInsidePluginBlock(position)) {
                addPluginBlockCompletions(result)
            }
        }
        
        private fun isBuildGradleKts(element: PsiElement): Boolean {
            return element.containingFile?.name?.endsWith(".gradle.kts") == true
        }
        
        private fun isInsideAllayBlock(element: PsiElement): Boolean {
            var current: PsiElement? = element
            while (current != null) {
                if (current is KtCallExpression) {
                    val name = (current.calleeExpression as? KtNameReferenceExpression)?.getReferencedName()
                    if (name == "allay") return true
                }
                current = current.parent
            }
            return false
        }
        
        private fun isInsidePluginBlock(element: PsiElement): Boolean {
            var current: PsiElement? = element
            var foundPlugin = false
            var foundAllay = false
            
            while (current != null) {
                if (current is KtCallExpression) {
                    val name = (current.calleeExpression as? KtNameReferenceExpression)?.getReferencedName()
                    when (name) {
                        "plugin" -> foundPlugin = true
                        "allay" -> foundAllay = true
                    }
                }
                current = current.parent
            }
            
            return foundPlugin && foundAllay
        }
        
        private fun addAllayBlockCompletions(result: CompletionResultSet) {
            result.addElement(
                LookupElementBuilder.create("api")
                    .withTypeText("String?")
                    .withTailText(" = \"version\"")
                    .withInsertHandler { context, _ ->
                        context.document.insertString(context.tailOffset, " = \"\"")
                        context.editor.caretModel.moveToOffset(context.tailOffset - 1)
                    }
            )
            
            result.addElement(
                LookupElementBuilder.create("server")
                    .withTypeText("String")
                    .withTailText(" = \"+\"")
                    .withInsertHandler { context, _ ->
                        context.document.insertString(context.tailOffset, " = \"+\"")
                        context.editor.caretModel.moveToOffset(context.tailOffset - 1)
                    }
            )
            
            result.addElement(
                LookupElementBuilder.create("apiOnly")
                    .withTypeText("Boolean")
                    .withTailText(" = true")
                    .withInsertHandler { context, _ ->
                        context.document.insertString(context.tailOffset, " = true")
                        context.editor.caretModel.moveToOffset(context.tailOffset)
                    }
            )
            
            result.addElement(
                LookupElementBuilder.create("descriptorInjection")
                    .withTypeText("Boolean")
                    .withTailText(" = true")
                    .withInsertHandler { context, _ ->
                        context.document.insertString(context.tailOffset, " = true")
                        context.editor.caretModel.moveToOffset(context.tailOffset)
                    }
            )
            
            result.addElement(
                LookupElementBuilder.create("entrance")
                    .withTypeText("String")
                    .withTailText(" = \"...\"")
                    .withInsertHandler { context, _ ->
                        context.document.insertString(context.tailOffset, " = \"\"")
                        context.editor.caretModel.moveToOffset(context.tailOffset - 1)
                    }
            )
            
            result.addElement(
                LookupElementBuilder.create("plugin")
                    .withTypeText("Plugin")
                    .withTailText(" { ... }")
                    .withInsertHandler { context, _ ->
                        context.document.insertString(context.tailOffset, " {\n    \n}")
                        context.editor.caretModel.moveToOffset(context.tailOffset - 2)
                    }
            )
        }
        
        private fun addPluginBlockCompletions(result: CompletionResultSet) {
            val pluginProperties = listOf(
                Pair("name", "String"),
                Pair("entrance", "String"),
                Pair("version", "String"),
                Pair("description", "String"),
                Pair("website", "String"),
                Pair("api", "String")
            )
            
            pluginProperties.forEach { (name, type) ->
                result.addElement(
                    LookupElementBuilder.create(name)
                        .withTypeText(type)
                        .withTailText(" = \"...\"", true)
                        .withInsertHandler { context, _ ->
                            context.document.insertString(context.tailOffset, " = \"\"")
                            context.editor.caretModel.moveToOffset(context.tailOffset - 1)
                        }
                )
            }
            
            result.addElement(
                LookupElementBuilder.create("authors")
                    .withTypeText("ListProperty<String>")
                    .withTailText(" += \"author\"", true)
                    .withInsertHandler { context, _ ->
                        context.document.insertString(context.tailOffset, " += \"\"")
                        context.editor.caretModel.moveToOffset(context.tailOffset - 1)
                    }
            )
            
            result.addElement(
                LookupElementBuilder.create("dependencies")
                    .withTypeText("ListProperty<Dependency>")
                    .withTailText(" += dependency(...)", true)
                    .withInsertHandler { context, _ ->
                        context.document.insertString(context.tailOffset, " += dependency(\"\")")
                        context.editor.caretModel.moveToOffset(context.tailOffset - 2)
                    }
            )
            
            result.addElement(
                LookupElementBuilder.create("dependency")
                    .withTypeText("(String, String?, Boolean) -> Dependency")
                    .withTailText("(name, version, optional)", true)
                    .withInsertHandler { context, _ ->
                        context.document.insertString(context.tailOffset, "(\"\")")
                        context.editor.caretModel.moveToOffset(context.tailOffset - 2)
                    }
            )
        }
    }
}

