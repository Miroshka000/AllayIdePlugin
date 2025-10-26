package miroshka.allayideplugin

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*

class AllayDslAnnotator : Annotator {
    
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is KtCallExpression) return
        
        val callName = (element.calleeExpression as? KtNameReferenceExpression)?.getReferencedName()
        
        if (callName == "allay") {
            validateAllayBlock(element, holder)
        }
    }
    
    private fun validateAllayBlock(element: KtCallExpression, holder: AnnotationHolder) {
        val lambda = element.lambdaArguments.firstOrNull()?.getLambdaExpression()
        val bodyExpression = lambda?.bodyExpression ?: return
        
        var hasApi = false
        var hasPluginBlock = false
        
        bodyExpression.statements.forEach { statement ->
            when {
                isPropertyAssignment(statement, "api") -> hasApi = true
                isCallExpression(statement, "plugin") -> {
                    hasPluginBlock = true
                    validatePluginBlock(statement as? KtCallExpression, holder)
                }
            }
        }
        
        if (!hasApi) {
            holder.newAnnotation(
                HighlightSeverity.WARNING,
                "Property 'api' should be specified when apiOnly = true"
            ).range(element.calleeExpression!!).create()
        }
        
        if (!hasPluginBlock) {
            holder.newAnnotation(
                HighlightSeverity.WARNING,
                "Plugin descriptor block 'plugin { }' is recommended"
            ).range(element.calleeExpression!!).create()
        }
    }
    
    private fun validatePluginBlock(element: KtCallExpression?, holder: AnnotationHolder) {
        if (element == null) return
        
        val lambda = element.lambdaArguments.firstOrNull()?.getLambdaExpression()
        val bodyExpression = lambda?.bodyExpression ?: return
        
        var hasEntrance = false
        var hasName = false
        var hasVersion = false
        
        bodyExpression.statements.forEach { statement ->
            when {
                isPropertyAssignment(statement, "entrance") -> hasEntrance = true
                isPropertyAssignment(statement, "name") -> hasName = true
                isPropertyAssignment(statement, "version") -> hasVersion = true
            }
        }
        
        if (!hasEntrance) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "Required property 'entrance' is not specified"
            ).range(element.calleeExpression!!).create()
        }
        
        if (!hasName) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "Required property 'name' is not specified"
            ).range(element.calleeExpression!!).create()
        }
        
        if (!hasVersion) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "Required property 'version' is not specified"
            ).range(element.calleeExpression!!).create()
        }
    }
    
    private fun isPropertyAssignment(statement: KtExpression, propertyName: String): Boolean {
        if (statement is KtBinaryExpression) {
            val left = statement.left as? KtNameReferenceExpression
            return left?.getReferencedName() == propertyName
        }
        return false
    }
    
    private fun isCallExpression(statement: KtExpression, functionName: String): Boolean {
        if (statement is KtCallExpression) {
            val callee = statement.calleeExpression as? KtNameReferenceExpression
            return callee?.getReferencedName() == functionName
        }
        return false
    }
}

