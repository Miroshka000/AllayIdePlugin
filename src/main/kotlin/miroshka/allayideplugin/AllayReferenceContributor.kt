package miroshka.allayideplugin

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

class AllayReferenceContributor : PsiReferenceContributor() {
    
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(KtStringTemplateExpression::class.java),
            AllayEntranceReferenceProvider()
        )
    }
    
    private class AllayEntranceReferenceProvider : PsiReferenceProvider() {
        override fun getReferencesByElement(
            element: PsiElement,
            context: ProcessingContext
        ): Array<PsiReference> {
            if (element !is KtStringTemplateExpression) {
                return PsiReference.EMPTY_ARRAY
            }
            
            val text = element.text
            
            if (!isEntranceProperty(element)) {
                return PsiReference.EMPTY_ARRAY
            }
            
            val className = text.removeSurrounding("\"")
            if (className.isEmpty()) {
                return PsiReference.EMPTY_ARRAY
            }
            
            return arrayOf(AllayEntranceReference(element, TextRange(1, text.length - 1)))
        }
        
        private fun isEntranceProperty(element: PsiElement): Boolean {
            val parent = element.parent?.parent
            val text = parent?.text ?: return false
            return text.contains("entrance")
        }
    }
    
    private class AllayEntranceReference(
        element: KtStringTemplateExpression,
        textRange: TextRange
    ) : PsiReferenceBase<KtStringTemplateExpression>(element, textRange) {
        
        override fun resolve(): PsiElement? {
            val className = element.text.removeSurrounding("\"")
            
            val project = element.project
            val scope = element.resolveScope
            
            val javaPsiFacade = JavaPsiFacade.getInstance(project)
            return javaPsiFacade.findClass(className, scope)
        }
        
        override fun getVariants(): Array<Any> {
            return emptyArray()
        }
    }
}

